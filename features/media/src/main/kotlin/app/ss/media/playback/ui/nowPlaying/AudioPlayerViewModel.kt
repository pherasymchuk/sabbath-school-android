/*
 * Copyright (c) 2025. Adventech <info@adventech.io>
 *
 * Permission is hereby granted, free of charge, to any person obtaining a copy
 * of this software and associated documentation files (the "Software"), to deal
 * in the Software without restriction, including without limitation the rights
 * to use, copy, modify, merge, publish, distribute, sublicense, and/or sell
 * copies of the Software, and to permit persons to whom the Software is
 * furnished to do so, subject to the following conditions:
 *
 * The above copyright notice and this permission notice shall be included in
 * all copies or substantial portions of the Software.
 *
 * THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR
 * IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY,
 * FITNESS FOR A PARTICULAR PURPOSE AND NON-INFRINGEMENT. IN NO EVENT SHALL THE
 * AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER
 * LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM,
 * OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN
 * THE SOFTWARE.
 */

package app.ss.media.playback.ui.nowPlaying

import android.content.Intent
import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import app.ss.models.media.AudioFile
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharedFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asSharedFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.launch
import ss.libraries.media.api.MediaRepository
import ss.libraries.media.model.extensions.id
import ss.libraries.media.model.toAudio
import ss.libraries.navigation3.NavKey
import ss.services.media.ui.PlaybackConnection
import javax.inject.Inject

sealed interface AudioPlayerNavEvent {
    data class NavigateTo(val key: NavKey) : AudioPlayerNavEvent
    data class LaunchIntent(val intent: Intent) : AudioPlayerNavEvent
    data object NavigateBack : AudioPlayerNavEvent
}

@HiltViewModel
class AudioPlayerViewModel @Inject constructor(
    savedStateHandle: SavedStateHandle,
    private val repository: MediaRepository,
    val playbackConnection: PlaybackConnection,
) : ViewModel() {

    private val resourceId: String = savedStateHandle["resourceId"] ?: ""
    private val segmentId: String? = savedStateHandle["segmentId"]

    private val _uiState = MutableStateFlow<AudioPlayerState>(AudioPlayerState.Loading)
    val uiState: StateFlow<AudioPlayerState> = _uiState.asStateFlow()

    private val _navEvents = MutableSharedFlow<AudioPlayerNavEvent>()
    val navEvents: SharedFlow<AudioPlayerNavEvent> = _navEvents.asSharedFlow()

    init {
        observePlayback()
        observeConnection()
    }

    private fun observeConnection() {
        viewModelScope.launch {
            playbackConnection.isConnected.collect { isConnected ->
                if (isConnected) {
                    generateQueue()
                }
            }
        }
    }

    private fun observePlayback() {
        viewModelScope.launch {
            val nowPlayingFlow = playbackConnection.nowPlaying.map { metaData ->
                val def = metaData.toAudio()
                if (metaData.id.isEmpty()) {
                    def
                } else {
                    repository.findAudioFile(metaData.id) ?: def
                }
            }

            combine(
                nowPlayingFlow,
                playbackConnection.playbackQueue,
                playbackConnection.playbackState,
                playbackConnection.playbackProgress,
                playbackConnection.playbackSpeed
            ) { audioFile, queue, state, progress, speed ->
                NowPlayingScreenSpec(
                    nowPlayingAudio = audioFile,
                    playbackQueue = queue,
                    playbackState = state,
                    playbackProgressState = progress,
                    playbackConnection = playbackConnection,
                    playbackSpeed = speed,
                    isDraggable = { isDraggable -> }
                )
            }.collect { spec ->
                _uiState.value = AudioPlayerState.NowPlaying(spec)
            }
        }
    }

    private suspend fun generateQueue() {
        val nowPlaying = playbackConnection.nowPlaying.first()
        val lessonIndex = resourceId

        val playlist = repository.getPlayList(lessonIndex)

        if (nowPlaying.id.isEmpty()) {
            setAudioQueue(playlist)
        } else {
            val state = playbackConnection.playbackState.first()
            val audio = repository.findAudioFile(nowPlaying.id)

            if (audio == null || audio.targetIndex.startsWith(lessonIndex) == false || !state.isPlaying) {
                if (state.isPlaying) {
                    playbackConnection.playPause()
                }
                setAudioQueue(playlist)
            }
        }
    }

    private fun setAudioQueue(playlist: List<AudioFile>) {
        if (playlist.isEmpty()) return

        val index = playlist.indexOfFirst { it.targetIndex == segmentId }
        val position = index.coerceAtLeast(0)
        playbackConnection.setQueue(playlist, position)
    }

    fun navigateBack() {
        viewModelScope.launch {
            _navEvents.emit(AudioPlayerNavEvent.NavigateBack)
        }
    }

    fun navigateTo(key: NavKey) {
        viewModelScope.launch {
            _navEvents.emit(AudioPlayerNavEvent.NavigateTo(key))
        }
    }
}

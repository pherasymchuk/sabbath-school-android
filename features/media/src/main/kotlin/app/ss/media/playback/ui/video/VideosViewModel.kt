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

package app.ss.media.playback.ui.video

import android.content.Context
import android.content.Intent
import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import app.ss.media.playback.ui.video.player.VideoPlayerActivity
import app.ss.models.media.SSVideo
import app.ss.models.media.SSVideosInfo
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharedFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asSharedFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.launch
import ss.libraries.media.api.MediaRepository
import javax.inject.Inject

sealed interface VideosNavEvent {
    data class LaunchIntent(val intent: Intent) : VideosNavEvent
}

@HiltViewModel
class VideosViewModel @Inject constructor(
    savedStateHandle: SavedStateHandle,
    private val repository: MediaRepository,
) : ViewModel() {

    private val documentIndex: String = savedStateHandle["documentIndex"] ?: ""
    private val documentId: String? = savedStateHandle["documentId"]

    private val _uiState = MutableStateFlow(VideosScreenState())
    val uiState: StateFlow<VideosScreenState> = _uiState.asStateFlow()

    private val _navEvents = MutableSharedFlow<VideosNavEvent>()
    val navEvents: SharedFlow<VideosNavEvent> = _navEvents.asSharedFlow()

    init {
        loadVideos()
    }

    private fun loadVideos() {
        viewModelScope.launch {
            repository.getVideo(documentIndex)
                .map { it.toData(documentId) }
                .collect { data ->
                    _uiState.value = VideosScreenState(data = data)
                }
        }
    }

    private fun List<SSVideosInfo>.toData(documentId: String?): VideoListData =
        if (size == 1 && first().clips.isNotEmpty()) {
            val allVideos = first().clips
            val featuredVideo = allVideos.firstOrNull { it.targetIndex == documentId } ?: allVideos.first()
            VideoListData.Vertical(
                featured = featuredVideo,
                clips = allVideos.subtract(setOf(featuredVideo)).toList(),
            )
        } else {
            VideoListData.Horizontal(
                data = this,
                target = documentId,
            )
        }

    fun onVideoSelected(context: Context, video: SSVideo) {
        viewModelScope.launch {
            _navEvents.emit(
                VideosNavEvent.LaunchIntent(
                    VideoPlayerActivity.launchIntent(context, video)
                )
            )
        }
    }
}

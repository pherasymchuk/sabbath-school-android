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

package ss.document.segment.components.video

import android.content.Context
import android.content.Intent
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import app.ss.models.media.SSVideo
import dagger.assisted.Assisted
import dagger.assisted.AssistedFactory
import dagger.assisted.AssistedInject
import dagger.hilt.android.lifecycle.HiltViewModel
import io.adventech.blockkit.model.BlockItem
import io.adventech.blockkit.model.resource.Segment
import io.adventech.blockkit.model.resource.VideoClipSegment
import kotlinx.collections.immutable.ImmutableList
import kotlinx.collections.immutable.toImmutableList
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharedFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asSharedFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import ss.document.NavEvent
import ss.document.components.DocumentTopAppBarAction
import ss.libraries.media.api.MediaNavigation
import ss.resources.api.ResourcesRepository

/** UI state for VideoSegment screen. */
data class VideoSegmentState(
    val title: String = "",
    val videos: ImmutableList<BlockItem.Video> = emptyList<BlockItem.Video>().toImmutableList(),
    val blocks: List<BlockItem> = emptyList(),
    val showReaderOptions: Boolean = false,
)

/** Navigation events for VideoSegment screen. */
sealed interface VideoSegmentNavEvent {
    data object Pop : VideoSegmentNavEvent
    data class LaunchIntent(val intent: Intent) : VideoSegmentNavEvent
    data class GoTo(val event: NavEvent) : VideoSegmentNavEvent
}

@HiltViewModel(assistedFactory = VideoSegmentViewModel.Factory::class)
class VideoSegmentViewModel @AssistedInject constructor(
    @Assisted("id") private val id: String,
    @Assisted("index") private val index: String,
    @Assisted("documentId") val documentId: String,
    private val resourcesRepository: ResourcesRepository,
    private val mediaNavigation: MediaNavigation,
) : ViewModel() {

    private val _uiState = MutableStateFlow(VideoSegmentState())
    val uiState: StateFlow<VideoSegmentState> = _uiState.asStateFlow()

    private val _navEvents = MutableSharedFlow<VideoSegmentNavEvent>()
    val navEvents: SharedFlow<VideoSegmentNavEvent> = _navEvents.asSharedFlow()

    init {
        viewModelScope.launch {
            resourcesRepository.segment(id, index).collect { segment ->
                segment?.let { updateState(it) }
            }
        }
    }

    private fun updateState(segment: Segment) {
        _uiState.update { state ->
            state.copy(
                title = segment.title.orEmpty(),
                videos = segment.video.orEmpty().map { it.asBlock() }.toImmutableList(),
                blocks = segment.blocks.orEmpty(),
            )
        }
    }

    fun onNavBack() {
        viewModelScope.launch {
            _navEvents.emit(VideoSegmentNavEvent.Pop)
        }
    }

    fun playVideo(context: Context, video: VideoClipSegment) {
        val ssVideo = SSVideo(
            artist = video.artist.orEmpty(),
            id = "",
            src = video.src,
            target = "",
            targetIndex = "",
            thumbnail = video.thumbnail.orEmpty(),
            title = video.title.orEmpty(),
            hls = video.hls,
        )
        viewModelScope.launch {
            _navEvents.emit(VideoSegmentNavEvent.LaunchIntent(mediaNavigation.videoPlayer(context, ssVideo)))
        }
    }

    fun onTopAppBarAction(action: DocumentTopAppBarAction) {
        when (action) {
            DocumentTopAppBarAction.DisplayOptions -> {
                _uiState.update { it.copy(showReaderOptions = true) }
            }
            else -> Unit
        }
    }

    fun dismissReaderOptions() {
        _uiState.update { it.copy(showReaderOptions = false) }
    }

    private fun VideoClipSegment.asBlock() = BlockItem.Video(
        id = src,
        style = null,
        data = null,
        nested = null,
        src = hls ?: src,
        caption = null,
    )

    @AssistedFactory
    interface Factory {
        fun create(
            @Assisted("id") id: String,
            @Assisted("index") index: String,
            @Assisted("documentId") documentId: String
        ): VideoSegmentViewModel
    }
}

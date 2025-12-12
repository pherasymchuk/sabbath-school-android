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

package app.ss.media.playback.ui.nowPlaying.mini

import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.material3.Surface
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.ui.Modifier
import androidx.compose.ui.tooling.preview.PreviewLightDark
import androidx.hilt.navigation.compose.hiltViewModel
import app.ss.design.compose.theme.SsTheme
import ss.libraries.navigation3.LocalSsNavigator
import ss.services.media.ui.PlaybackMiniControls
import ss.services.media.ui.spec.NowPlayingSpec
import ss.services.media.ui.spec.PlaybackStateSpec

/**
 * Mini audio player screen composable.
 * @param modifier Modifier for this composable.
 * @param viewModel The ViewModel that manages mini player state.
 */
@Suppress("DEPRECATION")
@Composable
fun MiniPlayerScreen(
    modifier: Modifier = Modifier,
    viewModel: MiniPlayerViewModel = hiltViewModel(),
) {
    val navigator = LocalSsNavigator.current

    LaunchedEffect(Unit) {
        viewModel.navEvents.collect { event ->
            when (event) {
                is MiniPlayerNavEvent.NavigateTo -> navigator.goTo(event.key)
            }
        }
    }

    PlaybackMiniControls(
        playbackConnection = viewModel.playbackConnection,
        modifier = modifier.navigationBarsPadding()
    ) {
        viewModel.onExpand()
    }
}

@PreviewLightDark
@Composable
private fun MiniPlayerPreview() {
    SsTheme {
        Surface {
            PlaybackMiniControls(
                spec = PlaybackStateSpec(
                    isPlaying = true,
                    isPlayEnabled = true,
                    isError = false,
                    isBuffering = false,
                    canShowMini = true,
                ),
                nowPlayingSpec = NowPlayingSpec(
                    id = "1",
                    title = "Sabbath Rest",
                    artist = "Adult Sabbath School",
                ),
                playbackConnection = NoOpPlaybackConnection,
                onExpand = {},
            )
        }
    }
}

private object NoOpPlaybackConnection : ss.services.media.ui.PlaybackConnection {
    override val isConnected = kotlinx.coroutines.flow.MutableStateFlow(false)
    override val playbackState = kotlinx.coroutines.flow.MutableStateFlow(PlaybackStateSpec.NONE)
    override val nowPlaying = kotlinx.coroutines.flow.MutableStateFlow(androidx.media3.common.MediaMetadata.EMPTY)
    override val playbackQueue = kotlinx.coroutines.flow.MutableStateFlow(ss.libraries.media.model.PlaybackQueue())
    override val playbackProgress = kotlinx.coroutines.flow.MutableStateFlow(ss.libraries.media.model.PlaybackProgressState())
    override val playbackSpeed = kotlinx.coroutines.flow.MutableStateFlow(ss.libraries.media.model.PlaybackSpeed.NORMAL)
    override fun playPause() = Unit
    override fun playAudio(audio: app.ss.models.media.AudioFile) = Unit
    override fun playAudios(audios: List<app.ss.models.media.AudioFile>, index: Int) = Unit
    override fun toggleSpeed() = Unit
    override fun setQueue(audios: List<app.ss.models.media.AudioFile>, index: Int) = Unit
    override fun skipToItem(position: Int) = Unit
    override fun seekTo(progress: Long) = Unit
    override fun rewind() = Unit
    override fun fastForward() = Unit
    override fun stop() = Unit
    override fun releaseMini() = Unit
}

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

import androidx.compose.animation.core.Spring
import androidx.compose.animation.core.animateDpAsState
import androidx.compose.animation.core.spring
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyListState
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.material3.Surface
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.input.nestedscroll.NestedScrollConnection
import androidx.compose.ui.input.nestedscroll.NestedScrollSource
import androidx.compose.ui.input.nestedscroll.nestedScroll
import androidx.compose.ui.tooling.preview.PreviewLightDark
import androidx.compose.ui.unit.Velocity
import androidx.compose.ui.unit.dp
import androidx.hilt.lifecycle.viewmodel.compose.hiltViewModel

import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.viewmodel.compose.viewModel
import app.ss.design.compose.theme.SsTheme
import app.ss.media.playback.ui.nowPlaying.components.BottomControls
import app.ss.media.playback.ui.nowPlaying.components.BoxState
import app.ss.media.playback.ui.nowPlaying.components.PlayBackControls
import app.ss.media.playback.ui.nowPlaying.components.PlaybackProgressDuration

/**
 * Audio player screen composable that renders based on [AudioPlayerState].
 * @param resourceId The resource ID to load audio for.
 * @param segmentId Optional segment ID to start from.
 * @param modifier Modifier for this composable.
 * @param viewModel The ViewModel that manages audio player state.
 */

@Composable
fun AudioPlayerScreen(
    resourceId: String,
    segmentId: String?,
    modifier: Modifier = Modifier,
    viewModel: AudioPlayerViewModel = hiltViewModel(),
) {
    val state by viewModel.uiState.collectAsStateWithLifecycle()

    AudioPlayerScreenContent(state = state, modifier = modifier)
}

@Composable
internal fun AudioPlayerScreenContent(
    state: AudioPlayerState,
    modifier: Modifier = Modifier,
) {
    when (state) {
        is AudioPlayerState.NowPlaying -> {
            NowPlayingScreen(
                spec = state.spec,
                modifier = modifier
            )
        }
        AudioPlayerState.Loading -> {
            // Loading state
        }
    }
}

@Composable
internal fun NowPlayingScreen(
    viewModel: NowPlayingViewModel = viewModel(),
    isDraggable: (Boolean) -> Unit = {}
) {
    val playbackConnection = viewModel.playbackConnection
    val playbackState by playbackConnection.playbackState
        .collectAsStateWithLifecycle()
    val nowPlaying by viewModel.nowPlayingAudio
        .collectAsStateWithLifecycle()
    val playbackQueue by playbackConnection.playbackQueue
        .collectAsStateWithLifecycle()
    val playbackSpeed by playbackConnection.playbackSpeed
        .collectAsStateWithLifecycle()
    val playbackProgressState by playbackConnection.playbackProgress
        .collectAsStateWithLifecycle()
    val nowPlayingAudio = if (nowPlaying.id.isEmpty()) {
        playbackQueue.currentAudio ?: nowPlaying
    } else {
        nowPlaying
    }

    NowPlayingScreen(
        spec = NowPlayingScreenSpec(
            nowPlayingAudio,
            playbackQueue,
            playbackState,
            playbackProgressState,
            playbackConnection,
            playbackSpeed,
            isDraggable
        )
    )
}

@Composable
internal fun NowPlayingScreen(
    spec: NowPlayingScreenSpec,
    modifier: Modifier = Modifier,
    listState: LazyListState = rememberLazyListState(),
) {
    val (_, _, playbackState, playbackProgressState, playbackConnection, playbackSpeed, isDraggable) = spec

    var boxState by remember { mutableStateOf(BoxState.Expanded) }
    val expanded = boxState == BoxState.Expanded

    val spacing by animateDpAsState(
        if (expanded) 32.dp else 0.dp,
        animationSpec = spring(
            Spring.DampingRatioMediumBouncy, stiffness = Spring.StiffnessLow
        ), label = ""
    )

    val nestedScrollConnection = remember {
        object : NestedScrollConnection {
            override fun onPreScroll(available: Offset, source: NestedScrollSource): Offset {
                isDraggable(false)
                return super.onPreScroll(available, source)
            }

            override suspend fun onPostFling(consumed: Velocity, available: Velocity): Velocity {
                isDraggable(true)
                return super.onPostFling(consumed, available)
            }
        }
    }

    Column(
        modifier = modifier
            .fillMaxSize()
            .padding(vertical = 16.dp)
            .nestedScroll(connection = nestedScrollConnection),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        NowPlayingDetail(
            spec = spec,
            boxState = boxState,
            listState = listState,
            modifier = Modifier.weight(1f)
        )

        PlaybackProgressDuration(
            isBuffering = playbackState.isBuffering,
            progressState = playbackProgressState,
            onSeekTo = { progress ->
                playbackConnection.seekTo(progress)
            },
            modifier = Modifier
        )

        Spacer(modifier = Modifier.height(spacing))

        PlayBackControls(
            spec = playbackState,
            contentColor = SsTheme.colors.playbackMiniContent,
            playbackConnection = playbackConnection
        )

        Spacer(modifier = Modifier.height(spacing))

        BottomControls(
            playbackSpeed = playbackSpeed,
            toggleSpeed = { playbackConnection.toggleSpeed() },
            toggleExpand = {
                boxState = when (boxState) {
                    BoxState.Collapsed -> BoxState.Expanded
                    BoxState.Expanded -> BoxState.Collapsed
                }
            }
        )
    }
}

@PreviewLightDark
@Composable
private fun Preview () {
    SsTheme {
        Surface {
            NowPlayingScreen(spec = PreviewData.nowPlayScreenSpec())
        }
    }
}

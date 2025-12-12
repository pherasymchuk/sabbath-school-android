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

import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.Surface
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.tooling.preview.PreviewLightDark
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import app.ss.design.compose.theme.SsTheme
import ss.libraries.navigation3.LocalSsNavigator

/**
 * Videos screen composable that displays a list of videos.
 * @param documentIndex The document index to load videos for.
 * @param documentId Optional document ID.
 * @param modifier Modifier for this composable.
 * @param viewModel The ViewModel that manages videos state.
 */
@Suppress("DEPRECATION")
@Composable
fun VideosScreen(
    documentIndex: String,
    documentId: String?,
    modifier: Modifier = Modifier,
    viewModel: VideosViewModel = hiltViewModel(),
) {
    val navigator = LocalSsNavigator.current
    val context = LocalContext.current
    val state by viewModel.uiState.collectAsStateWithLifecycle()

    LaunchedEffect(Unit) {
        viewModel.navEvents.collect { event ->
            when (event) {
                is VideosNavEvent.LaunchIntent -> navigator.launchIntent(event.intent)
            }
        }
    }

    VideoListScreen(
        videoList = state.data,
        modifier = modifier.fillMaxSize(),
        onVideoClick = { viewModel.onVideoSelected(context, it) },
    )
}

@PreviewLightDark
@Composable
private fun VideosScreenPreview() {
    SsTheme {
        Surface {
            VideoListScreen(
                videoList = VideoListData.Empty,
                modifier = Modifier.fillMaxSize(),
            )
        }
    }
}

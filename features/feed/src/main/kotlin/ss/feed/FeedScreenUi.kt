/*
 * Copyright (c) 2024. Adventech <info@adventech.io>
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

package ss.feed

import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Surface
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.input.nestedscroll.nestedScroll
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.tooling.preview.PreviewLightDark
import androidx.hilt.navigation.compose.hiltViewModel
import app.ss.design.compose.extensions.haptics.LocalSsHapticFeedback
import app.ss.design.compose.theme.SsTheme
import app.ss.design.compose.widget.appbar.FeedTopAppBar
import app.ss.design.compose.widget.scaffold.HazeScaffold
import io.adventech.blockkit.model.feed.FeedGroup
import kotlinx.collections.immutable.persistentListOf
import ss.feed.components.FeedLazyColum
import ss.feed.components.view.FeedLoadingView
import ss.libraries.navigation3.LocalSsNavigator
import ss.services.auth.overlay.AccountDialog

@Suppress("DEPRECATION")
@Composable
fun FeedScreen(
    modifier: Modifier = Modifier,
    viewModel: FeedViewModel = hiltViewModel(),
) {
    val navigator = LocalSsNavigator.current
    LaunchedEffect(navigator) {
        viewModel.setNavigator(navigator)
    }

    val uiState by viewModel.uiState.collectAsState()
    val overlayState by viewModel.overlayState.collectAsState()
    val context = LocalContext.current

    FeedScreenContent(
        state = uiState,
        modifier = modifier,
        onProfileClick = viewModel::onProfileClick,
        onFilterLanguagesClick = viewModel::onFilterLanguagesClick,
        onItemClick = viewModel::onItemClick,
        onSeeAllClick = viewModel::onSeeAllClick,
    )

    overlayState?.let { state ->
        when (state) {
            is FeedOverlayState.AccountInfo -> {
                AccountDialog(
                    userInfo = state.userInfo,
                    onDismiss = viewModel::onOverlayDismiss,
                    onSignOut = { viewModel.onOverlayResult(FeedViewModel.OverlayResult.SignOut, context) },
                    onGoToSettings = { viewModel.onOverlayResult(FeedViewModel.OverlayResult.GoToSettings, context) },
                    onShareApp = { viewModel.onOverlayResult(FeedViewModel.OverlayResult.ShareApp(context), context) },
                    onGoToAbout = { viewModel.onOverlayResult(FeedViewModel.OverlayResult.GoToAbout, context) },
                    onGoToPrivacyPolicy = { viewModel.onOverlayResult(FeedViewModel.OverlayResult.GoToPrivacyPolicy(context), context) },
                )
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
internal fun FeedScreenContent(
    state: FeedUiState,
    modifier: Modifier = Modifier,
    onProfileClick: () -> Unit = {},
    onFilterLanguagesClick: () -> Unit = {},
    onItemClick: (String) -> Unit = {},
    onSeeAllClick: (FeedGroup) -> Unit = {},
) {
    val scrollBehavior = TopAppBarDefaults.exitUntilCollapsedScrollBehavior()
    val listState = rememberLazyListState()
    val hapticFeedback = LocalSsHapticFeedback.current

    HazeScaffold(
        modifier = modifier.nestedScroll(scrollBehavior.nestedScrollConnection),
        topBar = {
            FeedTopAppBar(
                photoUrl = state.photoUrl,
                title = state.title,
                scrollBehavior = scrollBehavior,
                onNavigationClick = onProfileClick,
                onFilterLanguagesClick = onFilterLanguagesClick,
            )
        },
        blurTopBar = true,
    ) { contentPadding ->
        when (state) {
            is FeedUiState.Loading -> {
                FeedLoadingView(
                    modifier = Modifier.fillMaxSize(),
                    contentPadding = contentPadding,
                )
            }

            is FeedUiState.Group -> {
                FeedLazyColum(
                    groups = state.groups,
                    modifier = Modifier,
                    state = listState,
                    contentPadding = contentPadding,
                    seeAllClick = { group ->
                        onSeeAllClick(group)
                        hapticFeedback.performScreenView()
                    },
                    itemClick = { resource ->
                        onItemClick(resource.index)
                        hapticFeedback.performScreenView()
                    }
                )
            }

            is FeedUiState.ResourceList -> {
                FeedLazyColum(
                    resources = state.resources,
                    modifier = Modifier,
                    state = listState,
                    contentPadding = contentPadding,
                    itemClick = { index -> onItemClick(index) }
                )
            }
        }
    }
}

@PreviewLightDark
@Composable
private fun LoadingPreview() {
    SsTheme {
        Surface {
            FeedScreenContent(
                state = FeedUiState.Loading("", null)
            )
        }
    }
}

@PreviewLightDark
@Composable
private fun GroupPreview() {
    SsTheme {
        Surface {
            FeedScreenContent(
                state = FeedUiState.Group(
                    photoUrl = null,
                    title = "Sabbath School",
                    groups = persistentListOf()
                )
            )
        }
    }
}

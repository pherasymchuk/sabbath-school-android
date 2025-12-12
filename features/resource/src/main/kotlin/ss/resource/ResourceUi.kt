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

package ss.resource

import androidx.compose.animation.animateColorAsState
import androidx.compose.foundation.background
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyListState
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.derivedStateOf
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.input.nestedscroll.nestedScroll
import androidx.compose.ui.platform.LocalContext
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import app.ss.design.compose.extensions.color.parse
import app.ss.design.compose.extensions.haptics.LocalSsHapticFeedback
import app.ss.design.compose.theme.SsTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.ui.tooling.preview.PreviewLightDark
import app.ss.design.compose.widget.scaffold.HazeScaffold
import app.ss.design.compose.widget.scaffold.SystemUiEffect
import io.adventech.blockkit.model.resource.ProgressTracking
import io.adventech.blockkit.ui.style.font.LocalFontFamilyProvider
import kotlinx.collections.immutable.persistentListOf
import ss.libraries.navigation3.LocalSsNavigator
import ss.resource.components.CoverContent
import ss.resource.components.ResourceCover
import ss.resource.components.ResourceLoadingView
import ss.resource.components.ResourceOverlayContent
import ss.resource.components.ResourceTopAppBar
import ss.resource.components.content.ResourceSectionsStateProducer
import ss.resource.components.footer
import ss.resource.components.footerBackgroundColor
import ss.resource.components.resourceSections
import ss.resource.components.spec.SharePosition
import ss.resource.producer.CtaScreenState
import ss.resource.producer.ResourceCtaScreenProducer

@Suppress("DEPRECATION")
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ResourceScreen(
    index: String,
    modifier: Modifier = Modifier,
    viewModel: ResourceViewModel = hiltViewModel(),
    resourceCtaScreenProducer: ResourceCtaScreenProducer,
    resourceSectionsStateProducer: ResourceSectionsStateProducer,
) {
    val navigator = LocalSsNavigator.current
    val hapticFeedback = LocalSsHapticFeedback.current
    val context = LocalContext.current

    LaunchedEffect(index) { viewModel.setIndex(index) }

    LaunchedEffect(Unit) {
        viewModel.navEvents.collect { event ->
            when (event) {
                is ResourceNavEvent.NavigateTo -> navigator.goTo(event.key)
                ResourceNavEvent.NavigateBack -> navigator.pop()
            }
        }
    }

    val scrollBehavior = TopAppBarDefaults.pinnedScrollBehavior()
    val listState: LazyListState = rememberLazyListState()
    val collapsed by remember { derivedStateOf { listState.firstVisibleItemIndex > 0 } }
    val isSystemInDarkTheme = isSystemInDarkTheme()
    val lightStatusBar by remember(collapsed, isSystemInDarkTheme) { derivedStateOf { !isSystemInDarkTheme && collapsed } }

    val resource by viewModel.resource.collectAsStateWithLifecycle()
    val overlayState by viewModel.overlayState.collectAsStateWithLifecycle()
    val credits by viewModel.credits.collectAsStateWithLifecycle()
    val features by viewModel.features.collectAsStateWithLifecycle()
    val sharePosition by viewModel.sharePosition.collectAsStateWithLifecycle()

    val sections = resource?.let { res ->
        resourceSectionsStateProducer(navigator, res).specs
    } ?: persistentListOf()

    val ctaScreen = resourceCtaScreenProducer(resource)
    val readDocumentTitle = remember(resource, ctaScreen) {
        (ctaScreen as? CtaScreenState.NavigateToKey)?.title?.takeIf {
            resource?.progressTracking in setOf(ProgressTracking.AUTOMATIC, ProgressTracking.AUTOMATIC)
        } ?: ""
    }

    HazeScaffold(
        modifier = modifier.nestedScroll(scrollBehavior.nestedScrollConnection),
        topBar = {
            ResourceTopAppBar(
                isShowingNavigationBar = collapsed,
                title = resource?.title ?: "",
                modifier = Modifier,
                iconTint = resource?.primaryColorDark?.let { Color.parse(it) },
                showShare = sharePosition == SharePosition.TOOLBAR,
                scrollBehavior = scrollBehavior,
                onNavBack = {
                    hapticFeedback.performClick()
                    viewModel.navigateBack()
                },
                onShareClick = {
                    hapticFeedback.performClick()
                    viewModel.onShareClick(context)
                }
            )
        },
        blurTopBar = collapsed,
    ) {
        val color by animateColorAsState(
            targetValue = if (resource != null) {
                footerBackgroundColor()
            } else {
                SsTheme.colors.primaryBackground
            }, label = "background-color"
        )

        if (resource == null) {
            ResourceLoadingView(state = listState)
        } else {
            val res = resource!!

            CompositionLocalProvider(
                LocalFontFamilyProvider provides viewModel.fontFamilyProvider,
            ) {
                LazyColumn(
                    modifier = Modifier.background(color),
                    state = listState,
                ) {
                    item("cover") {
                        ResourceCover(
                            resource = res,
                            scrollOffset = { listState.firstVisibleItemScrollOffset.toFloat() },
                            modifier = Modifier,
                            content = {
                                CoverContent(
                                    resource = res,
                                    documentTitle = readDocumentTitle.takeUnless { it.isBlank() },
                                    type = it,
                                    ctaOnClick = {
                                        hapticFeedback.performScreenView()
                                        when (ctaScreen) {
                                            is CtaScreenState.NavigateToKey -> navigator.goTo(ctaScreen.key)
                                            is CtaScreenState.LaunchIntent -> navigator.launchIntent(ctaScreen.intent)
                                            CtaScreenState.None -> Unit
                                        }
                                    },
                                    readMoreClick = {
                                        viewModel.onReadMoreClick()
                                    },
                                    shareClick = {
                                        hapticFeedback.performClick()
                                        viewModel.onShareClick(context)
                                    }
                                )
                            }
                        )
                    }

                    resourceSections(sections)

                    footer(credits, features)
                }

                ResourceOverlayContent(
                    state = overlayState,
                    onDismiss = viewModel::dismissOverlay,
                    onNavigate = navigator::goTo,
                )
            }
        }
    }

    SystemUiEffect(lightStatusBar)
}

@PreviewLightDark
@Composable
private fun ResourceScreenPreview() {
    SsTheme {
        Surface {
            // ResourceScreen requires complex dependencies (ViewModel, producers)
            // This preview shows a loading state placeholder
            ss.resource.components.ResourceLoadingView()
        }
    }
}

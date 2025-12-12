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

package ss.navigation.suite

import androidx.compose.animation.AnimatedContent
import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.togetherWith
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.Icon
import androidx.compose.material3.LoadingIndicator
import androidx.compose.material3.NavigationBar
import androidx.compose.material3.NavigationBarItem
import androidx.compose.material3.adaptive.currentWindowAdaptiveInfo
import androidx.compose.material3.adaptive.navigationsuite.NavigationSuiteScaffold
import androidx.compose.material3.adaptive.navigationsuite.NavigationSuiteScaffoldDefaults
import androidx.compose.material3.adaptive.navigationsuite.NavigationSuiteType
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.hilt.navigation.compose.hiltViewModel
import app.ss.design.compose.extensions.haptics.LocalSsHapticFeedback
import app.ss.design.compose.widget.scaffold.HazeScaffold
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import dagger.multibindings.IntoSet
import kotlinx.collections.immutable.ImmutableList
import ss.feed.FeedScreen
import ss.feed.FeedType
import ss.libraries.navigation3.EntryProviderBuilder
import ss.libraries.navigation3.HomeNavKey

@Module
@InstallIn(SingletonComponent::class)
object HomeNavigationNavModule {

    @Provides
    @IntoSet
    fun provideHomeNavigationEntry(): EntryProviderBuilder = {
        entry<HomeNavKey> { _ ->
            @Suppress("DEPRECATION")
            val viewModel: HomeNavigationViewModel = hiltViewModel()
            HomeNavigationScreen(viewModel = viewModel)
        }
    }
}

@Composable
internal fun HomeNavigationScreen(
    viewModel: HomeNavigationViewModel,
    modifier: Modifier = Modifier,
) {
    val navigationItems by viewModel.navigationItems.collectAsState()
    val selectedItem by viewModel.selectedItem.collectAsState()

    HomeNavigationScreenContent(
        navigationItems = navigationItems,
        selectedItem = selectedItem,
        onItemSelected = viewModel::selectItem,
        modifier = modifier,
    )
}

@Composable
private fun HomeNavigationScreenContent(
    navigationItems: ImmutableList<NavbarItemNav3>?,
    selectedItem: NavbarItemNav3?,
    onItemSelected: (NavbarItemNav3) -> Unit,
    modifier: Modifier = Modifier,
) {
    // Set default selected item when items load
    LaunchedEffect(navigationItems) {
        if (navigationItems != null && selectedItem == null && navigationItems.isNotEmpty()) {
            onItemSelected(navigationItems.first())
        }
    }

    when {
        navigationItems == null -> {
            Box(modifier = modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                LoadingIndicator()
            }
        }
        navigationItems.isEmpty() -> {
            // Fallback: show default feed
            FeedScreen(
                feedType = FeedType.SABBATH_SCHOOL,
                modifier = modifier.fillMaxSize(),
            )
        }
        selectedItem != null -> {
            NavigationSuiteContent(
                items = navigationItems,
                selectedItem = selectedItem,
                onItemSelected = onItemSelected,
                modifier = modifier,
            )
        }
    }
}

@Composable
private fun NavigationSuiteContent(
    items: ImmutableList<NavbarItemNav3>,
    selectedItem: NavbarItemNav3,
    onItemSelected: (NavbarItemNav3) -> Unit,
    modifier: Modifier = Modifier,
) {
    val layoutType = NavigationSuiteScaffoldDefaults.calculateFromAdaptiveInfo(currentWindowAdaptiveInfo())
    val hapticFeedback = LocalSsHapticFeedback.current

    val content: @Composable (PaddingValues) -> Unit = {
        AnimatedContent(
            targetState = selectedItem,
            transitionSpec = {
                fadeIn(animationSpec = tween(300)).togetherWith(fadeOut(animationSpec = tween(300)))
            },
            label = "content",
        ) { item ->
            FeedScreen(
                feedType = item.toFeedType(),
                modifier = Modifier.fillMaxSize(),
            )
        }
    }

    when (layoutType) {
        NavigationSuiteType.NavigationBar -> {
            HazeScaffold(
                modifier = modifier,
                bottomBar = {
                    NavigationBar(
                        modifier = Modifier,
                        containerColor = Color.Transparent,
                    ) {
                        items.forEach { model ->
                            NavigationBarItem(
                                icon = {
                                    Icon(
                                        painter = painterResource(model.iconRes),
                                        contentDescription = stringResource(model.title),
                                    )
                                },
                                selected = model == selectedItem,
                                onClick = {
                                    onItemSelected(model)
                                    hapticFeedback.performSegmentSwitch()
                                },
                            )
                        }
                    }
                },
                blurBottomBar = true,
                content = content,
            )
        }
        else -> {
            NavigationSuiteScaffold(
                navigationSuiteItems = {
                    items.forEach { model ->
                        item(
                            icon = {
                                Icon(
                                    painter = painterResource(model.iconRes),
                                    contentDescription = stringResource(model.title),
                                )
                            },
                            selected = selectedItem == model,
                            onClick = {
                                onItemSelected(model)
                                hapticFeedback.performSegmentSwitch()
                            },
                        )
                    }
                },
                modifier = modifier,
            ) {
                content(PaddingValues())
            }
        }
    }
}

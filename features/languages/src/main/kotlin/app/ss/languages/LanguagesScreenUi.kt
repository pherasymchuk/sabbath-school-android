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

package app.ss.languages

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.WindowInsetsSides
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.only
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.safeDrawing
import androidx.compose.foundation.layout.windowInsetsPadding
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.IconButton
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Surface
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.tooling.preview.PreviewLightDark
import app.ss.design.compose.extensions.haptics.LocalSsHapticFeedback
import app.ss.design.compose.theme.SsTheme
import app.ss.design.compose.widget.divider.Divider
import app.ss.design.compose.widget.icon.IconBox
import app.ss.design.compose.widget.icon.Icons
import app.ss.design.compose.widget.search.SearchInput
import app.ss.languages.list.LanguagesList
import app.ss.languages.state.LanguageUiModel
import kotlinx.collections.immutable.ImmutableList
import kotlinx.collections.immutable.persistentListOf
import ss.libraries.navigation3.LocalSsNavigator

@Composable
fun LanguagesScreen(
    viewModel: LanguagesViewModel,
    modifier: Modifier = Modifier,
) {
    val navigator = LocalSsNavigator.current
    LaunchedEffect(navigator) {
        viewModel.setNavigator(navigator)
    }

    val state by viewModel.state.collectAsState()

    LanguagesScreenContent(
        state = state,
        modifier = modifier,
        onSearch = viewModel::search,
        onSelectLanguage = viewModel::selectLanguage,
        onNavigateBack = viewModel::navigateBack,
    )
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
internal fun LanguagesScreenContent(
    state: LanguagesUiState,
    modifier: Modifier = Modifier,
    onSearch: (String?) -> Unit = {},
    onSelectLanguage: (LanguageUiModel) -> Unit = {},
    onNavigateBack: () -> Unit = {},
) {
    val hapticFeedback = LocalSsHapticFeedback.current
    Scaffold(
        modifier = modifier,
        topBar = {
            SearchView(
                onQuery = onSearch,
                onNavBack = {
                    hapticFeedback.performClick()
                    onNavigateBack()
                },
                modifier = Modifier.windowInsetsPadding(
                    WindowInsets.safeDrawing.only(WindowInsetsSides.Top),
                ),
            )
        },
    ) { paddingValues ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues),
        ) {
            Divider()

            when (state) {
                is LanguagesUiState.Loading -> Box(Modifier.weight(1f))
                is LanguagesUiState.Languages ->
                    LanguagesList(
                        models = state.models,
                        onItemClick = {
                            onSelectLanguage(it)
                            hapticFeedback.performClick()
                        },
                        modifier = Modifier.weight(1f),
                    )
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun SearchView(
    onQuery: (String?) -> Unit,
    onNavBack: () -> Unit,
    modifier: Modifier = Modifier
) {
    var queryValue by rememberSaveable { mutableStateOf("") }

    TopAppBar(
        title = {
            SearchInput(
                value = queryValue,
                onQueryChange = {
                    queryValue = it
                    onQuery(it.ifEmpty { null })
                },
                modifier = Modifier.fillMaxWidth(),
                placeholder = "Search Languages",
            )
        },
        modifier = modifier,
        navigationIcon = { IconButton(onClick = onNavBack) { IconBox(icon = Icons.ArrowBack) } },
    )
}

@PreviewLightDark
@Composable
private fun SearchPreview() {
    SsTheme { Surface { SearchView(onQuery = {}, onNavBack = {}) } }
}

@PreviewLightDark
@Composable
private fun LoadingPreview() {
    SsTheme {
        Surface {
            LanguagesScreenContent(state = LanguagesUiState.Loading)
        }
    }
}

@PreviewLightDark
@Composable
private fun LanguagesPreview() {
    SsTheme {
        Surface {
            LanguagesScreenContent(
                state = LanguagesUiState.Languages(
                    models = persistentListOf(
                        LanguageUiModel("en", "English", "English", true),
                        LanguageUiModel("es", "Spanish", "Español", false),
                    )
                )
            )
        }
    }
}

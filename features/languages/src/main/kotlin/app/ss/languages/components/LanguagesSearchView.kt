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

package app.ss.languages.components

import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.IconButton
import androidx.compose.material3.Surface
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.tooling.preview.PreviewLightDark
import app.ss.design.compose.theme.SsTheme
import app.ss.design.compose.widget.icon.IconBox
import app.ss.design.compose.widget.icon.Icons
import app.ss.design.compose.widget.search.SearchInput

/**
 * Search view component for languages screen.
 *
 * @param onQuery Callback when search query changes. Null means empty query.
 * @param onNavBack Callback when back navigation is triggered.
 * @param modifier Modifier for this composable.
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
internal fun LanguagesSearchView(
    onQuery: (String?) -> Unit,
    onNavBack: () -> Unit,
    modifier: Modifier = Modifier,
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
private fun LanguagesSearchViewPreview() {
    SsTheme {
        Surface {
            LanguagesSearchView(
                onQuery = {},
                onNavBack = {},
            )
        }
    }
}

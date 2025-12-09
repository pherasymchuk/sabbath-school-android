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

package ss.document.reader

import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import io.adventech.blockkit.ui.style.ReaderStyle
import io.adventech.blockkit.ui.style.ReaderStyleConfig

/** UI State for reader options. */
data class ReaderOptionsState(
    val config: ReaderStyleConfig = ReaderStyleConfig(),
)

/** Events for reader options. */
sealed interface ReaderOptionsEvent {
    data class OnThemeChanged(val theme: ReaderStyle.Theme) : ReaderOptionsEvent
    data class OnTypefaceChanged(val typeface: ReaderStyle.Typeface) : ReaderOptionsEvent
    data class OnFontSizeChanged(val size: ReaderStyle.Size) : ReaderOptionsEvent
}

/**
 * Composable entry point for reader options screen.
 */
@Suppress("DEPRECATION")
@Composable
fun ReaderOptionsScreen(
    viewModel: ReaderOptionsViewModel = hiltViewModel(),
) {
    val state by viewModel.uiState.collectAsStateWithLifecycle()

    ReaderOptionsContent(
        state = state,
        onEvent = viewModel::onEvent,
    )
}

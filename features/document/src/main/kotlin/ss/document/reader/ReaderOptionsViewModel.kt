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

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import dagger.hilt.android.lifecycle.HiltViewModel
import io.adventech.blockkit.ui.style.ReaderStyle
import io.adventech.blockkit.ui.style.ReaderStyleConfig
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.launchIn
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.onEach
import kotlinx.coroutines.flow.update
import ss.prefs.api.SSPrefs
import ss.prefs.model.SSReadingDisplayOptions
import javax.inject.Inject

@HiltViewModel
class ReaderOptionsViewModel @Inject constructor(
    private val ssPrefs: SSPrefs,
) : ViewModel() {

    private val _uiState = MutableStateFlow(ReaderOptionsState())
    val uiState: StateFlow<ReaderOptionsState> = _uiState.asStateFlow()

    init {
        ssPrefs.displayOptionsFlow()
            .map { options ->
                ReaderStyleConfig(
                    theme = ReaderStyle.Theme.entries.firstOrNull { it.value == options.theme } ?: ReaderStyle.Theme.Auto,
                    typeface = ReaderStyle.Typeface.entries.firstOrNull { it.value == options.font } ?: ReaderStyle.Typeface.Lato,
                    size = ReaderStyle.Size.entries.firstOrNull { it.value == options.size } ?: ReaderStyle.Size.Medium,
                )
            }
            .onEach { config ->
                _uiState.update { it.copy(config = config) }
            }
            .launchIn(viewModelScope)
    }

    fun onEvent(event: ReaderOptionsEvent) {
        val displayOptions = _uiState.value.config.toDisplayOptions()

        when (event) {
            is ReaderOptionsEvent.OnThemeChanged -> {
                ssPrefs.setDisplayOptions(displayOptions.copy(theme = event.theme.value))
            }
            is ReaderOptionsEvent.OnTypefaceChanged -> {
                ssPrefs.setDisplayOptions(displayOptions.copy(font = event.typeface.value))
            }
            is ReaderOptionsEvent.OnFontSizeChanged -> {
                ssPrefs.setDisplayOptions(displayOptions.copy(size = event.size.value))
            }
        }
    }

    private fun ReaderStyleConfig.toDisplayOptions() = SSReadingDisplayOptions(
        theme = theme.value,
        font = typeface.value,
        size = size.value
    )
}

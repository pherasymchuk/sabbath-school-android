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

package app.ss.languages

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import app.ss.languages.state.LanguageUiModel
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.collections.immutable.ImmutableList
import kotlinx.collections.immutable.toImmutableList
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.flatMapLatest
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.stateIn
import ss.libraries.appwidget.api.AppWidgetHelper
import ss.libraries.navigation3.HomeNavKey
import ss.libraries.navigation3.SsNavigator
import ss.prefs.api.SSPrefs
import ss.resources.api.ResourcesRepository
import ss.resources.model.LanguageModel
import javax.inject.Inject

sealed interface LanguagesUiState {
    data object Loading : LanguagesUiState
    data class Languages(val models: ImmutableList<LanguageUiModel>) : LanguagesUiState
}

@HiltViewModel
class LanguagesViewModel @Inject constructor(
    private val repository: ResourcesRepository,
    private val ssPrefs: SSPrefs,
    private val appWidgetHelper: AppWidgetHelper,
) : ViewModel() {

    private val _query = MutableStateFlow<String?>(null)
    private var navigator: SsNavigator? = null

    @OptIn(ExperimentalCoroutinesApi::class)
    val state: StateFlow<LanguagesUiState> = _query
        .flatMapLatest { query ->
            repository.languages(query)
                .map { languages ->
                    LanguagesUiState.Languages(languages.toModels())
                }
        }
        .stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5_000),
            initialValue = LanguagesUiState.Loading
        )

    fun setNavigator(navigator: SsNavigator) {
        this.navigator = navigator
    }

    fun search(query: String?) {
        _query.value = query?.trim()
    }

    fun selectLanguage(model: LanguageUiModel) {
        val languageChanged = model.code != ssPrefs.getLanguageCode()
        ssPrefs.setLanguageCode(model.code)
        if (languageChanged) {
            appWidgetHelper.languageChanged()
            navigator?.resetRoot(HomeNavKey)
        } else {
            navigator?.pop()
        }
    }

    fun navigateBack() {
        navigator?.pop()
    }

    private fun List<LanguageModel>.toModels() =
        map {
            LanguageUiModel(
                code = it.code,
                nativeName = it.nativeName,
                name = it.name,
                selected = it.code == ssPrefs.getLanguageCode(),
            )
        }.toImmutableList()
}

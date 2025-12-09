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

package ss.document.segment.hidden

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import dagger.assisted.Assisted
import dagger.assisted.AssistedFactory
import dagger.assisted.AssistedInject
import dagger.hilt.android.lifecycle.HiltViewModel
import io.adventech.blockkit.model.BlockItem
import io.adventech.blockkit.model.Style
import io.adventech.blockkit.ui.style.font.FontFamilyProvider
import kotlinx.collections.immutable.ImmutableList
import kotlinx.collections.immutable.toImmutableList
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.filterNotNull
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import ss.resources.api.ResourcesRepository

/** UI state for HiddenSegment screen. */
sealed interface HiddenSegmentState {
    val isLoading: Boolean

    data class Loading(
        override val isLoading: Boolean = true,
    ) : HiddenSegmentState

    data class Success(
        val title: String,
        val subtitle: String?,
        val date: String?,
        val blocks: ImmutableList<BlockItem>,
        val style: Style?,
        val fontFamilyProvider: FontFamilyProvider,
        override val isLoading: Boolean = false,
    ) : HiddenSegmentState
}

@HiltViewModel(assistedFactory = HiddenSegmentViewModel.Factory::class)
class HiddenSegmentViewModel @AssistedInject constructor(
    @Assisted("id") private val id: String,
    @Assisted("index") private val index: String,
    @Assisted("documentIndex") private val documentIndex: String,
    private val fontFamilyProvider: FontFamilyProvider,
    private val resourcesRepository: ResourcesRepository,
) : ViewModel() {

    private val _uiState = MutableStateFlow<HiddenSegmentState>(HiddenSegmentState.Loading())
    val uiState: StateFlow<HiddenSegmentState> = _uiState.asStateFlow()

    var documentId: String? = null
        private set

    init {
        viewModelScope.launch {
            combine(
                resourcesRepository.segment(id, index).filterNotNull(),
                resourcesRepository.document(documentIndex).filterNotNull()
            ) { segment, document ->
                documentId = document.id
                HiddenSegmentState.Success(
                    title = segment.title.orEmpty(),
                    subtitle = segment.subtitle,
                    date = segment.date,
                    blocks = segment.blocks.orEmpty().toImmutableList(),
                    style = segment.style,
                    fontFamilyProvider = fontFamilyProvider,
                )
            }.collect { state ->
                _uiState.update { state }
            }
        }
    }

    @AssistedFactory
    interface Factory {
        fun create(
            @Assisted("id") id: String,
            @Assisted("index") index: String,
            @Assisted("documentIndex") documentIndex: String
        ): HiddenSegmentViewModel
    }
}

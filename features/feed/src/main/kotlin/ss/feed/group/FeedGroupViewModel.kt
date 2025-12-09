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

package ss.feed.group

import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import dagger.hilt.android.lifecycle.HiltViewModel
import io.adventech.blockkit.model.feed.FeedType
import kotlinx.collections.immutable.ImmutableList
import kotlinx.collections.immutable.persistentListOf
import kotlinx.collections.immutable.toImmutableList
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import ss.feed.model.FeedResourceSpec
import ss.feed.model.toSpec
import ss.libraries.navigation3.ResourceKey
import ss.libraries.navigation3.SsNavigator
import ss.resources.api.ResourcesRepository
import javax.inject.Inject

@HiltViewModel
class FeedGroupViewModel @Inject constructor(
    savedStateHandle: SavedStateHandle,
    private val resourcesRepository: ResourcesRepository,
) : ViewModel() {

    private val groupId: String = savedStateHandle.get<String>("id") ?: ""
    private val initialTitle: String = savedStateHandle.get<String>("title") ?: ""
    private val feedType: FeedType = savedStateHandle.get<String>("feedType")
        ?.let { FeedType.valueOf(it) }
        ?: FeedType.SS

    private val _uiState = MutableStateFlow<FeedGroupUiState>(
        FeedGroupUiState.Loading(title = initialTitle)
    )
    val uiState: StateFlow<FeedGroupUiState> = _uiState.asStateFlow()

    private var navigator: SsNavigator? = null

    init {
        loadFeedGroup()
    }

    fun setNavigator(navigator: SsNavigator) {
        this.navigator = navigator
    }

    private fun loadFeedGroup() {
        viewModelScope.launch {
            resourcesRepository.feedGroup(groupId, feedType).collect { group ->
                val resources = group.resources?.map { resource ->
                    resource.toSpec(group, FeedResourceSpec.ContentDirection.HORIZONTAL)
                }?.toImmutableList() ?: persistentListOf()

                _uiState.value = FeedGroupUiState.Success(
                    title = group.title ?: initialTitle,
                    resources = resources
                )
            }
        }
    }

    fun onNavBack() {
        navigator?.pop()
    }

    fun onItemClick(index: String) {
        navigator?.goTo(ResourceKey(index))
    }
}

sealed interface FeedGroupUiState {
    val title: String

    data class Loading(
        override val title: String
    ) : FeedGroupUiState

    data class Success(
        override val title: String,
        val resources: ImmutableList<FeedResourceSpec>
    ) : FeedGroupUiState
}

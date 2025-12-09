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

package ss.resource

import android.content.Context
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import dagger.Lazy
import dagger.hilt.android.lifecycle.HiltViewModel
import dagger.hilt.android.qualifiers.ApplicationContext
import io.adventech.blockkit.model.resource.Resource
import io.adventech.blockkit.model.resource.ShareGroup
import io.adventech.blockkit.model.resource.ShareOptions
import io.adventech.blockkit.ui.style.font.FontFamilyProvider
import kotlinx.collections.immutable.ImmutableList
import kotlinx.collections.immutable.persistentListOf
import kotlinx.collections.immutable.toImmutableList
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharedFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asSharedFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import ss.foundation.android.intent.ShareIntentHelper
import ss.libraries.navigation3.NavKey
import ss.resource.components.spec.CreditSpec
import ss.resource.components.spec.FeatureSpec
import ss.resource.components.spec.SharePosition
import ss.resource.components.spec.toSpec
import ss.resources.api.ResourcesRepository
import javax.inject.Inject

sealed interface ResourceNavEvent {
    data class NavigateTo(val key: NavKey) : ResourceNavEvent
    data object NavigateBack : ResourceNavEvent
}

@HiltViewModel
class ResourceViewModel @Inject constructor(
    @param:ApplicationContext private val context: Context,
    private val resourcesRepository: ResourcesRepository,
    val fontFamilyProvider: FontFamilyProvider,
    private val shareIntentHelper: Lazy<ShareIntentHelper>,
) : ViewModel() {

    private var resourceIndex: String = ""
    
    private val _resource = MutableStateFlow<Resource?>(null)
    val resource: StateFlow<Resource?> = _resource.asStateFlow()

    private val _overlayState = MutableStateFlow<ResourceOverlayState?>(null)
    val overlayState: StateFlow<ResourceOverlayState?> = _overlayState.asStateFlow()

    private val _navEvents = MutableSharedFlow<ResourceNavEvent>()
    val navEvents: SharedFlow<ResourceNavEvent> = _navEvents.asSharedFlow()

    val credits: StateFlow<ImmutableList<CreditSpec>> = _resource
        .map { it?.credits?.map { credit -> credit.toSpec() }?.toImmutableList() ?: persistentListOf() }
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5_000), persistentListOf())

    val features: StateFlow<ImmutableList<FeatureSpec>> = _resource
        .map { it?.features?.map { feature -> feature.toSpec() }?.toImmutableList() ?: persistentListOf() }
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5_000), persistentListOf())

    val sharePosition: StateFlow<SharePosition> = _resource
        .map { SharePosition.fromResource(it) }
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5_000), SharePosition.HIDDEN)

    fun setIndex(index: String) {
        if (resourceIndex == index) return
        resourceIndex = index
        loadResource()
    }

    private fun loadResource() {
        viewModelScope.launch {
            resourcesRepository.resource(resourceIndex).collect { resource ->
                _resource.value = resource
            }
        }
    }

    fun navigateBack() {
        viewModelScope.launch {
            _navEvents.emit(ResourceNavEvent.NavigateBack)
        }
    }

    fun navigateTo(key: NavKey) {
        viewModelScope.launch {
            _navEvents.emit(ResourceNavEvent.NavigateTo(key))
        }
    }

    fun onReadMoreClick() {
        val resource = _resource.value ?: return
        val markdown = resource.introduction ?: resource.markdownDescription ?: resource.description ?: return
        _overlayState.update { ResourceOverlayState.IntroductionBottomSheet(markdown) }
    }

    fun onShareClick(context: Context) {
        val resource = _resource.value ?: return
        val options = resource.share ?: return
        val shareGroups = options.shareGroups
        val linkGroup = shareGroups.firstOrNull()

        if (shareGroups.size == 1 && linkGroup is ShareGroup.Link && linkGroup.links.size == 1) {
            val shareLink = linkGroup.links.first().src
            shareIntentHelper.get().shareText(context, shareLink)
        } else {
            _overlayState.update {
                ResourceOverlayState.ShareBottomSheet(
                    options = options,
                    primaryColorDark = resource.primaryColorDark,
                    title = resource.title,
                )
            }
        }
    }

    fun dismissOverlay() {
        _overlayState.update { null }
    }
}

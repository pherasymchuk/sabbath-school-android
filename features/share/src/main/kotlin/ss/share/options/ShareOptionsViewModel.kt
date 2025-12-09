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

package ss.share.options

import android.content.Context
import androidx.compose.ui.graphics.Color
import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import app.ss.design.compose.extensions.color.parse
import dagger.Lazy
import dagger.hilt.android.lifecycle.HiltViewModel
import io.adventech.blockkit.model.resource.ShareFileURL
import io.adventech.blockkit.model.resource.ShareGroup
import io.adventech.blockkit.model.resource.ShareLinkURL
import io.adventech.blockkit.model.resource.ShareOptions
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.flow.launchIn
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.onEach
import kotlinx.coroutines.flow.onStart
import kotlinx.coroutines.flow.update
import ss.foundation.android.intent.ShareIntentHelper
import timber.log.Timber
import javax.inject.Inject

@HiltViewModel
class ShareOptionsViewModel @Inject constructor(
    savedStateHandle: SavedStateHandle,
    private val shareIntentHelper: Lazy<ShareIntentHelper>,
) : ViewModel() {

    private var title: String = ""
    private var shareGroups: List<ShareGroup> = emptyList()

    private val _state = MutableStateFlow(
        ShareState(
            segments = emptyList(),
            selectedGroup = ShareGroup.Unknown(),
            shareButtonState = ShareButtonState.LOADING,
            themeColor = null,
        )
    )
    val state: StateFlow<ShareState> = _state.asStateFlow()

    fun setArgs(options: ShareOptions, title: String, resourceColor: String?) {
        this.title = title
        this.shareGroups = options.shareGroups

        val selectedGroup = shareGroups.firstOrNull { it.selected == true } ?: shareGroups.first()
        _state.update {
            it.copy(
                segments = shareGroups.map { group -> group.title },
                selectedGroup = selectedGroup,
                shareButtonState = ShareButtonState.DISABLED,
                themeColor = resourceColor?.takeUnless { color -> color.isBlank() }?.let { color -> Color.parse(color) },
            )
        }
    }

    fun onSegmentSelected(segment: String) {
        val selectedGroup = shareGroups.firstOrNull { it.title == segment } ?: shareGroups.first()
        _state.update { it.copy(selectedGroup = selectedGroup, shareButtonState = ShareButtonState.DISABLED) }
    }

    fun onShareUrlSelected(url: ShareLinkURL) {
        _state.update { it.copy(shareButtonState = ShareButtonState.ENABLED) }
    }

    fun onShareFileClicked(file: ShareFileURL) {
        shareIntentHelper.get()
            .fileExists(file.src, file.fileName ?: title)
            .map { downloaded -> if (downloaded) ShareButtonState.ENABLED else ShareButtonState.DISABLED }
            .onStart { _state.update { it.copy(shareButtonState = ShareButtonState.LOADING) } }
            .onEach { buttonState -> _state.update { it.copy(shareButtonState = buttonState) } }
            .catch { Timber.e(it) }
            .launchIn(viewModelScope)
    }

    fun shareLink(context: Context, link: ShareLinkURL) {
        shareIntentHelper.get().shareText(
            context = context,
            text = link.src,
            chooserTitle = title
        )
    }

    fun shareFile(context: Context, file: ShareFileURL) {
        shareIntentHelper.get().shareFile(
            context = context,
            fileUrl = file.src,
            fileName = file.fileName ?: title,
            chooserTitle = title
        )
    }
}

data class ShareState(
    val segments: List<String>,
    val selectedGroup: ShareGroup,
    val shareButtonState: ShareButtonState,
    val themeColor: Color?,
)

enum class ShareButtonState {
    ENABLED,
    DISABLED,
    LOADING
}

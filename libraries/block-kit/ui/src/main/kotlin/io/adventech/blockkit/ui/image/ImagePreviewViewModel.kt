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

package io.adventech.blockkit.ui.image

import android.app.DownloadManager
import android.content.Context
import android.os.Environment
import android.widget.Toast
import androidx.core.net.toUri
import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharedFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asSharedFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import javax.inject.Inject
import app.ss.translations.R as L10nR

sealed interface ImagePreviewNavEvent {
    data object Close : ImagePreviewNavEvent
}

@HiltViewModel
class ImagePreviewViewModel @Inject constructor(
    savedStateHandle: SavedStateHandle,
) : ViewModel() {

    private val id: String = savedStateHandle["id"] ?: ""
    private val src: String = savedStateHandle["src"] ?: ""
    private val caption: String? = savedStateHandle["caption"]

    private val _uiState = MutableStateFlow(
        ImagePreviewScreenState(
            src = src,
            caption = caption,
        )
    )
    val uiState: StateFlow<ImagePreviewScreenState> = _uiState.asStateFlow()

    private val _navEvents = MutableSharedFlow<ImagePreviewNavEvent>()
    val navEvents: SharedFlow<ImagePreviewNavEvent> = _navEvents.asSharedFlow()

    fun onClose() {
        viewModelScope.launch {
            _navEvents.emit(ImagePreviewNavEvent.Close)
        }
    }

    fun onDownload(context: Context) {
        downloadImage(context, src, id)
    }

    private fun downloadImage(context: Context, url: String, fileName: String) {
        val downloadTitle = "$fileName.png"
        val request = DownloadManager.Request(url.toUri())
            .setAllowedNetworkTypes(DownloadManager.Request.NETWORK_WIFI or DownloadManager.Request.NETWORK_MOBILE)
            .setTitle(downloadTitle)
            .setNotificationVisibility(DownloadManager.Request.VISIBILITY_VISIBLE_NOTIFY_COMPLETED)
            .setAllowedOverMetered(true)
            .setAllowedOverRoaming(false)
            .setDestinationInExternalPublicDir(Environment.DIRECTORY_DOWNLOADS, downloadTitle)
        val downloadManager = context.getSystemService(Context.DOWNLOAD_SERVICE) as DownloadManager
        downloadManager.enqueue(request)

        Toast.makeText(context, context.getString(L10nR.string.ss_downloading), Toast.LENGTH_SHORT).show()
    }
}

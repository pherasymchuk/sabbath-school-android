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

package ss.document

import android.content.Intent
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import app.ss.models.PDFAux
import dagger.hilt.android.lifecycle.HiltViewModel
import io.adventech.blockkit.model.resource.ResourceDocument
import io.adventech.blockkit.model.resource.Segment
import io.adventech.blockkit.ui.style.font.FontFamilyProvider
import kotlinx.collections.immutable.ImmutableList
import kotlinx.collections.immutable.persistentListOf
import kotlinx.collections.immutable.toImmutableList
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharedFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asSharedFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import org.joda.time.DateTime
import ss.libraries.navigation3.NavKey
import ss.libraries.navigation3.PdfKey
import ss.libraries.pdf.api.PdfReader
import ss.misc.DateHelper
import ss.resources.api.ResourcesRepository
import javax.inject.Inject

sealed interface DocumentNavEvent {
    data class NavigateTo(val key: NavKey) : DocumentNavEvent
    data class LaunchIntent(val intent: Intent) : DocumentNavEvent
    data object NavigateBack : DocumentNavEvent
}

@HiltViewModel
class DocumentViewModel @Inject constructor(
    private val resourcesRepository: ResourcesRepository,
    val fontFamilyProvider: FontFamilyProvider,
    private val pdfReader: PdfReader,
) : ViewModel() {

    private var documentIndex: String = ""
    private var segmentIndex: String? = null
    private val today get() = DateTime.now().withTimeAtStartOfDay()

    private val _document = MutableStateFlow<ResourceDocument?>(null)
    val document: StateFlow<ResourceDocument?> = _document.asStateFlow()

    private val _segments = MutableStateFlow<ImmutableList<Segment>>(persistentListOf())
    val segments: StateFlow<ImmutableList<Segment>> = _segments.asStateFlow()

    private val _selectedSegment = MutableStateFlow<Segment?>(null)
    val selectedSegment: StateFlow<Segment?> = _selectedSegment.asStateFlow()

    private val _navEvents = MutableSharedFlow<DocumentNavEvent>()
    val navEvents: SharedFlow<DocumentNavEvent> = _navEvents.asSharedFlow()

    fun setDocumentIndex(index: String, segmentIndex: String? = null) {
        if (documentIndex == index && this.segmentIndex == segmentIndex) return
        documentIndex = index
        this.segmentIndex = segmentIndex
        loadDocument()
    }

    private fun loadDocument() {
        viewModelScope.launch {
            resourcesRepository.document(documentIndex).collect { doc ->
                _document.value = doc
                doc?.let { processDocument(it) }
            }
        }
    }

    private fun processDocument(doc: ResourceDocument) {
        val documentSegments = (doc.segments?.map { it.copy(cover = it.cover ?: doc.cover) } ?: emptyList())
            .toImmutableList()
        _segments.value = documentSegments

        // Set default selected segment
        if (_selectedSegment.value == null) {
            _selectedSegment.value = documentSegments.defaultPage()
        }

        // Check if this is a PDF-only document
        checkPdfOnlySegment(doc)
    }

    private fun ImmutableList<Segment>.defaultPage(): Segment? {
        // 1. Check for index and return immediately if found
        val indexedSegment = segmentIndex
            ?.toIntOrNull()
            ?.let { getOrNull(it) }

        if (indexedSegment != null) {
            return indexedSegment
        }

        // 2. Fallback to today's date or the first segment
        return firstOrNull { segment ->
            val date = segment.date?.let { DateHelper.parseDate(it) }
            date?.isEqual(today) == true
        } ?: firstOrNull()
    }

    private fun checkPdfOnlySegment(doc: ResourceDocument) {
        val segments = doc.segments ?: return
        val blocks = segments.flatMap { it.blocks.orEmpty() }
        val pdfs = segments.flatMap { it.pdf.orEmpty() }

        if (blocks.isEmpty() && pdfs.isNotEmpty()) {
            val pdfKey = PdfKey(
                documentId = doc.id,
                resourceId = doc.resourceId,
                resourceIndex = doc.resourceIndex,
                documentIndex = doc.index,
                segmentId = null,
                pdfs = pdfs.map {
                    PDFAux(
                        id = it.id,
                        src = it.src,
                        title = it.title,
                        target = it.target,
                        targetIndex = it.targetIndex,
                    )
                },
            )
            viewModelScope.launch {
                _navEvents.emit(DocumentNavEvent.NavigateBack)
                _navEvents.emit(DocumentNavEvent.LaunchIntent(pdfReader.launchIntent(pdfKey)))
            }
        }
    }

    fun onPageChange(page: Int) {
        _selectedSegment.update { _segments.value.getOrNull(page) }
    }

    fun onSegmentSelection(segment: Segment) {
        _selectedSegment.update { segment }
    }

    fun navigateBack() {
        viewModelScope.launch {
            _navEvents.emit(DocumentNavEvent.NavigateBack)
        }
    }

    fun navigateTo(key: NavKey) {
        viewModelScope.launch {
            _navEvents.emit(DocumentNavEvent.NavigateTo(key))
        }
    }

    fun launchIntent(intent: Intent) {
        viewModelScope.launch {
            _navEvents.emit(DocumentNavEvent.LaunchIntent(intent))
        }
    }
}

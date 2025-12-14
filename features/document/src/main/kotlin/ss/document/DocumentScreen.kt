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

import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.hilt.lifecycle.viewmodel.compose.hiltViewModel

import androidx.lifecycle.compose.collectAsStateWithLifecycle
import io.adventech.blockkit.model.ReferenceScope
import kotlinx.collections.immutable.persistentListOf
import ss.document.components.DocumentTopAppBarAction
import ss.document.producer.ReaderStyleStateProducer
import ss.document.producer.TopAppbarActionsProducer
import ss.document.producer.TopAppbarActionsState
import ss.document.producer.UserInputStateProducer
import ss.document.segment.producer.SegmentOverlayStateProducer
import ss.libraries.navigation3.DocumentKey
import ss.libraries.navigation3.ExpandedAudioPlayerKey
import ss.libraries.navigation3.LocalSsNavigator
import ss.libraries.navigation3.ResourceKey
import ss.document.producer.TopAppbarActionsState.Event as TopAppbarEvent
import ss.document.segment.producer.SegmentOverlayStateProducer.Event as SegmentOverlayEvent


@Composable
fun DocumentScreen(
    index: String,
    segmentIndex: String?,
    actionsProducer: TopAppbarActionsProducer,
    readerStyleStateProducer: ReaderStyleStateProducer,
    segmentOverlayStateProducer: SegmentOverlayStateProducer,
    userInputStateProducer: UserInputStateProducer,
    modifier: Modifier = Modifier,
    viewModel: DocumentViewModel = hiltViewModel(),
) {
    val navigator = LocalSsNavigator.current

    // Initialize the ViewModel with screen parameters
    LaunchedEffect(index, segmentIndex) {
        viewModel.setDocumentIndex(index, segmentIndex)
    }

    // Collect ViewModel state
    val document by viewModel.document.collectAsStateWithLifecycle()
    val segments by viewModel.segments.collectAsStateWithLifecycle()
    val selectedSegment by viewModel.selectedSegment.collectAsStateWithLifecycle()

    // Handle navigation events
    LaunchedEffect(Unit) {
        viewModel.navEvents.collect { event ->
            when (event) {
                is DocumentNavEvent.NavigateTo -> navigator.goTo(event.key)
                is DocumentNavEvent.LaunchIntent -> navigator.launchIntent(event.intent)
                DocumentNavEvent.NavigateBack -> navigator.pop()
            }
        }
    }

    // Produce state from producers
    val actionsState = document?.let {
        actionsProducer(
            navigator = navigator,
            resourceId = it.resourceId,
            resourceIndex = it.resourceIndex,
            documentIndex = index,
            documentId = it.id,
            segment = selectedSegment,
            shareOptions = it.share,
        )
    } ?: TopAppbarActionsState.Empty

    val userInputState = userInputStateProducer(documentId = document?.id)
    val segmentOverlayState = segmentOverlayStateProducer(navigator, userInputState)
    val overlayState = remember(actionsState.overlayState, segmentOverlayState) {
        actionsState.overlayState ?: segmentOverlayState
    }

    val readerStyle = readerStyleStateProducer()

    val eventSink: (Event) -> Unit = { event ->
        when (event) {
            Event.OnNavBack -> navigator.pop()
            is Event.OnActionClick -> {
                actionsState.eventSink(TopAppbarEvent.OnActionClick(event.action, event.context))
            }

            is SuccessEvent.OnPageChange -> {
                viewModel.onPageChange(event.page)
            }

            is SuccessEvent.OnSegmentSelection -> {
                viewModel.onSegmentSelection(event.segment)
            }

            is SuccessEvent.OnNavEvent -> {
                when (val navEvent = event.event) {
                    is NavEvent.GoTo -> {
                        if (navEvent.key is ExpandedAudioPlayerKey) {
                            actionsState.eventSink(TopAppbarEvent.OnActionClick(DocumentTopAppBarAction.Audio, event.context))
                        } else {
                            navigator.goTo(navEvent.key)
                        }
                    }
                    NavEvent.Pop -> navigator.pop()
                }
            }

            is SuccessEvent.OnHandleUri -> {
                val segmentEvent = SegmentOverlayEvent.OnHandleUri(event.uri, event.data)
                sendSegmentOverlayEvent(segmentOverlayState, segmentEvent)
            }

            is SuccessEvent.OnHandleReference -> {
                val (scope, segment, resource, referenceDocument) = event.model

                if (segment != null && document != null && scope == ReferenceScope.SEGMENT) {
                    val segmentEvent = SegmentOverlayEvent.OnHiddenSegment(
                        segment = segment,
                        documentId = document!!.id,
                        documentIndex = document!!.index,
                    )
                    sendSegmentOverlayEvent(segmentOverlayState, segmentEvent)
                } else if (referenceDocument != null && scope == ReferenceScope.DOCUMENT) {
                    navigator.goTo(DocumentKey(referenceDocument.index))
                } else if (resource != null && scope == ReferenceScope.RESOURCE) {
                    navigator.goTo(ResourceKey(resource.index))
                }
            }
        }
    }

    val state: State = when {
        document == null || segments.isEmpty() -> State.Loading(
            hasCover = selectedSegment?.cover != null,
            eventSink = eventSink
        )
        else -> State.Success(
            title = selectedSegment?.title ?: document!!.title,
            hasCover = selectedSegment?.cover != null,
            actions = actionsState.actions,
            initialPage = segments.indexOf(selectedSegment),
            segments = segments,
            selectedSegment = selectedSegment,
            titleBelowCover = document!!.titleBelowCover == true,
            style = document!!.style,
            readerStyle = readerStyle,
            fontFamilyProvider = viewModel.fontFamilyProvider,
            documentId = document!!.id,
            documentIndex = document!!.index,
            resourceIndex = document!!.resourceIndex,
            eventSink = eventSink,
            overlayState = overlayState,
            userInputState = userInputState,
        )
    }

    DocumentScreenUi(
        state = state,
        modifier = modifier,
        readerStyleStateProducer = readerStyleStateProducer,
        segmentOverlayStateProducer = segmentOverlayStateProducer,
        userInputStateProducer = userInputStateProducer,
    )
}

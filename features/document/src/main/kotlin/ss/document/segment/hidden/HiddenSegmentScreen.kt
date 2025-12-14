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

import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.hilt.lifecycle.viewmodel.compose.hiltViewModel

import androidx.lifecycle.compose.collectAsStateWithLifecycle
import io.adventech.blockkit.model.BlockData
import io.adventech.blockkit.ui.input.UserInputState
import io.adventech.blockkit.ui.style.ReaderStyleConfig
import ss.document.DocumentOverlayState
import ss.document.NavEvent
import ss.document.producer.ReaderStyleStateProducer
import ss.document.producer.UserInputStateProducer
import ss.document.segment.producer.SegmentOverlayStateProducer
import ss.libraries.navigation3.SsNavigator

/** Events for HiddenSegment UI. */
sealed interface HiddenSegmentEvent {
    data class OnHandleUri(val uri: String, val data: BlockData?) : HiddenSegmentEvent
    data class OnNavEvent(val navEvent: NavEvent) : HiddenSegmentEvent
}

/**
 * Composable entry point for HiddenSegment screen.
 */

@Composable
fun HiddenSegmentScreen(
    id: String,
    index: String,
    documentIndex: String,
    navigator: SsNavigator,
    readerStyleStateProducer: ReaderStyleStateProducer,
    segmentOverlayStateProducer: SegmentOverlayStateProducer,
    userInputStateProducer: UserInputStateProducer,
    viewModel: HiddenSegmentViewModel = hiltViewModel(
        creationCallback = { factory: HiddenSegmentViewModel.Factory ->
            factory.create(id, index, documentIndex)
        }
    ),
) {
    val state by viewModel.uiState.collectAsStateWithLifecycle()
    val readerStyle = readerStyleStateProducer()
    val userInputState = userInputStateProducer(viewModel.documentId)
    val overlayState = segmentOverlayStateProducer(navigator, userInputState)

    HiddenSegmentContent(
        state = state,
        readerStyle = readerStyle,
        overlayState = overlayState,
        userInputState = userInputState,
        onEvent = { event ->
            when (event) {
                is HiddenSegmentEvent.OnHandleUri -> {
                    ss.document.sendSegmentOverlayEvent(
                        overlayState,
                        SegmentOverlayStateProducer.Event.OnHandleUri(event.uri, event.data)
                    )
                }
                is HiddenSegmentEvent.OnNavEvent -> {
                    when (val navEvent = event.navEvent) {
                        is NavEvent.GoTo -> navigator.goTo(navEvent.key)
                        NavEvent.Pop -> navigator.pop()
                    }
                }
            }
        }
    )
}

/*
 * Copyright (c) 2024. Adventech <info@adventech.io>
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

import android.content.Context
import androidx.compose.runtime.Immutable
import io.adventech.blockkit.model.BlockData
import io.adventech.blockkit.model.Style
import io.adventech.blockkit.model.resource.ReferenceModel
import io.adventech.blockkit.model.resource.Segment
import io.adventech.blockkit.ui.input.UserInputState
import io.adventech.blockkit.ui.style.ReaderStyleConfig
import io.adventech.blockkit.ui.style.font.FontFamilyProvider
import kotlinx.collections.immutable.ImmutableList
import ss.document.components.DocumentTopAppBarAction
import ss.document.segment.components.overlay.BlocksOverlayState
import ss.document.segment.components.overlay.ExcerptOverlayState
import ss.document.segment.producer.SegmentOverlayStateProducer
import ss.libraries.navigation3.NavKey

/** Navigation event for internal navigation actions. */
sealed interface NavEvent {
    /** Navigate to a destination with the given key. */
    data class GoTo(val key: NavKey) : NavEvent
    /** Navigate back. */
    data object Pop : NavEvent
}

sealed interface State {
    val hasCover: Boolean
    val eventSink: (Event) -> Unit

    data class Loading(
        override val hasCover: Boolean,
        override val eventSink: (Event) -> Unit
    ) : State

    data class Success(
        val title: String,
        override val hasCover: Boolean,
        override val eventSink: (Event) -> Unit,
        val documentId: String,
        val documentIndex: String,
        val resourceIndex: String,
        val actions: ImmutableList<DocumentTopAppBarAction>,
        val initialPage: Int,
        val segments: ImmutableList<Segment>,
        val selectedSegment: Segment?,
        val titleBelowCover: Boolean,
        val style: Style?,
        val readerStyle: ReaderStyleConfig,
        val fontFamilyProvider: FontFamilyProvider,
        val overlayState: DocumentOverlayState?,
        val userInputState: UserInputState,
    ) : State

}

sealed interface Event {

    /** Navigation icon is clicked. */
    data object OnNavBack : Event

    /** AppBar action is clicked. */
    data class OnActionClick(val action: DocumentTopAppBarAction, val context: Context) : Event
}

sealed interface SuccessEvent : Event {
    data class OnPageChange(val page: Int) : SuccessEvent
    data class OnSegmentSelection(val segment: Segment) : SuccessEvent
    data class OnNavEvent(val event: NavEvent, val context: Context) : SuccessEvent
    data class OnHandleUri(val uri: String, val data: BlockData?) : SuccessEvent
    data class OnHandleReference(val model: ReferenceModel): SuccessEvent
}


sealed interface DocumentOverlayState {

    /** Overlay state for a bottom sheet showing a nested screen. */
    @Immutable
    data class BottomSheet(
        val key: NavKey,
        val skipPartiallyExpanded: Boolean,
        val themed: Boolean,
        val feedback: Boolean,
        val onDismiss: () -> Unit,
    ) : DocumentOverlayState

    sealed interface Segment : DocumentOverlayState {

        @Immutable
        data class None(
            val eventSink: (SegmentOverlayStateProducer.Event) -> Unit
        ) : Segment

        @Immutable
        data class Excerpt(
            val state: ExcerptOverlayState,
            val onDismiss: () -> Unit,
        ) : Segment

        @Immutable
        data class Blocks(
            val state: BlocksOverlayState,
            val onDismiss: () -> Unit,
        ) : Segment
    }

}

/** Helper function to send events to the segment overlay state. */
fun sendSegmentOverlayEvent(
    state: DocumentOverlayState?,
    event: SegmentOverlayStateProducer.Event
) {
    when (state) {
        is DocumentOverlayState.Segment.None -> state.eventSink(event)
        else -> Unit
    }
}

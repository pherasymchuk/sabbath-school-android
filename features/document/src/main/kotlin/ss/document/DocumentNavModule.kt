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

import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import dagger.multibindings.IntoSet
import ss.document.producer.ReaderStyleStateProducer
import ss.document.producer.TopAppbarActionsProducer
import ss.document.producer.UserInputStateProducer
import ss.document.segment.producer.SegmentOverlayStateProducer
import ss.libraries.navigation3.DocumentKey
import ss.libraries.navigation3.EntryProviderBuilder

@Module
@InstallIn(SingletonComponent::class)
object DocumentNavModule {

    @Provides
    @IntoSet
    fun provideDocumentEntry(
        actionsProducer: TopAppbarActionsProducer,
        readerStyleStateProducer: ReaderStyleStateProducer,
        segmentOverlayStateProducer: SegmentOverlayStateProducer,
        userInputStateProducer: UserInputStateProducer,
    ): EntryProviderBuilder = {
        entry<DocumentKey> { key ->
            DocumentScreen(
                index = key.index,
                segmentIndex = key.segmentIndex,
                actionsProducer = actionsProducer,
                readerStyleStateProducer = readerStyleStateProducer,
                segmentOverlayStateProducer = segmentOverlayStateProducer,
                userInputStateProducer = userInputStateProducer,
            )
        }
    }
}

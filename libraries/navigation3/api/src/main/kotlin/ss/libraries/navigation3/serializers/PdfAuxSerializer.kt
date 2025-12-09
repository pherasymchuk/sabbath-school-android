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

package ss.libraries.navigation3.serializers

import app.ss.models.PDFAux
import kotlinx.serialization.ExperimentalSerializationApi
import kotlinx.serialization.KSerializer
import kotlinx.serialization.builtins.ListSerializer
import kotlinx.serialization.descriptors.SerialDescriptor
import kotlinx.serialization.descriptors.buildClassSerialDescriptor
import kotlinx.serialization.descriptors.element
import kotlinx.serialization.encoding.CompositeDecoder
import kotlinx.serialization.encoding.Decoder
import kotlinx.serialization.encoding.Encoder
import kotlinx.serialization.encoding.decodeStructure
import kotlinx.serialization.encoding.encodeStructure

/** Serializer for [PDFAux]. */
@OptIn(ExperimentalSerializationApi::class)
object PdfAuxSerializer : KSerializer<PDFAux> {
    override val descriptor: SerialDescriptor = buildClassSerialDescriptor("PDFAux") {
        element<String>("id")
        element<String>("src")
        element<String>("title")
        element<String?>("target")
        element<String?>("targetIndex")
    }

    override fun serialize(encoder: Encoder, value: PDFAux) {
        encoder.encodeStructure(descriptor) {
            encodeStringElement(descriptor, 0, value.id)
            encodeStringElement(descriptor, 1, value.src)
            encodeStringElement(descriptor, 2, value.title)
            encodeNullableSerializableElement(
                descriptor, 3,
                kotlinx.serialization.serializer<String?>(),
                value.target
            )
            encodeNullableSerializableElement(
                descriptor, 4,
                kotlinx.serialization.serializer<String?>(),
                value.targetIndex
            )
        }
    }

    override fun deserialize(decoder: Decoder): PDFAux {
        return decoder.decodeStructure(descriptor) {
            var id = ""
            var src = ""
            var title = ""
            var target: String? = null
            var targetIndex: String? = null

            while (true) {
                when (val index = decodeElementIndex(descriptor)) {
                    0 -> id = decodeStringElement(descriptor, 0)
                    1 -> src = decodeStringElement(descriptor, 1)
                    2 -> title = decodeStringElement(descriptor, 2)
                    3 -> target = decodeNullableSerializableElement(
                        descriptor, 3,
                        kotlinx.serialization.serializer<String?>()
                    )
                    4 -> targetIndex = decodeNullableSerializableElement(
                        descriptor, 4,
                        kotlinx.serialization.serializer<String?>()
                    )
                    CompositeDecoder.DECODE_DONE -> break
                    else -> error("Unexpected index: $index")
                }
            }
            PDFAux(id, src, title, target, targetIndex)
        }
    }
}

/** Serializer for List<PDFAux>. */
object PdfAuxListSerializer : KSerializer<List<PDFAux>> {
    private val delegateSerializer = ListSerializer(PdfAuxSerializer)

    override val descriptor: SerialDescriptor = delegateSerializer.descriptor

    override fun serialize(encoder: Encoder, value: List<PDFAux>) {
        delegateSerializer.serialize(encoder, value)
    }

    override fun deserialize(decoder: Decoder): List<PDFAux> {
        return delegateSerializer.deserialize(decoder)
    }
}

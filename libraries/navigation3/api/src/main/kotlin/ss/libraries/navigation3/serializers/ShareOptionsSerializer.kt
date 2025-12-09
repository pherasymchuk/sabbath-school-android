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

import io.adventech.blockkit.model.resource.ShareFileURL
import io.adventech.blockkit.model.resource.ShareGroup
import io.adventech.blockkit.model.resource.ShareLinkURL
import io.adventech.blockkit.model.resource.ShareOptions
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

/** Serializer for [ShareLinkURL]. */
@OptIn(ExperimentalSerializationApi::class)
object ShareLinkURLSerializer : KSerializer<ShareLinkURL> {
    override val descriptor: SerialDescriptor = buildClassSerialDescriptor("ShareLinkURL") {
        element<String?>("title")
        element<String>("src")
    }

    override fun serialize(encoder: Encoder, value: ShareLinkURL) {
        encoder.encodeStructure(descriptor) {
            encodeNullableSerializableElement(
                descriptor, 0,
                kotlinx.serialization.serializer<String?>(),
                value.title
            )
            encodeStringElement(descriptor, 1, value.src)
        }
    }

    override fun deserialize(decoder: Decoder): ShareLinkURL {
        return decoder.decodeStructure(descriptor) {
            var title: String? = null
            var src = ""
            while (true) {
                when (val index = decodeElementIndex(descriptor)) {
                    0 -> title = decodeNullableSerializableElement(
                        descriptor, 0,
                        kotlinx.serialization.serializer<String?>()
                    )
                    1 -> src = decodeStringElement(descriptor, 1)
                    CompositeDecoder.DECODE_DONE -> break
                    else -> error("Unexpected index: $index")
                }
            }
            ShareLinkURL(title, src)
        }
    }
}

/** Serializer for [ShareFileURL]. */
@OptIn(ExperimentalSerializationApi::class)
object ShareFileURLSerializer : KSerializer<ShareFileURL> {
    override val descriptor: SerialDescriptor = buildClassSerialDescriptor("ShareFileURL") {
        element<String?>("title")
        element<String?>("fileName")
        element<String>("src")
    }

    override fun serialize(encoder: Encoder, value: ShareFileURL) {
        encoder.encodeStructure(descriptor) {
            encodeNullableSerializableElement(
                descriptor, 0,
                kotlinx.serialization.serializer<String?>(),
                value.title
            )
            encodeNullableSerializableElement(
                descriptor, 1,
                kotlinx.serialization.serializer<String?>(),
                value.fileName
            )
            encodeStringElement(descriptor, 2, value.src)
        }
    }

    override fun deserialize(decoder: Decoder): ShareFileURL {
        return decoder.decodeStructure(descriptor) {
            var title: String? = null
            var fileName: String? = null
            var src = ""
            while (true) {
                when (val index = decodeElementIndex(descriptor)) {
                    0 -> title = decodeNullableSerializableElement(
                        descriptor, 0,
                        kotlinx.serialization.serializer<String?>()
                    )
                    1 -> fileName = decodeNullableSerializableElement(
                        descriptor, 1,
                        kotlinx.serialization.serializer<String?>()
                    )
                    2 -> src = decodeStringElement(descriptor, 2)
                    CompositeDecoder.DECODE_DONE -> break
                    else -> error("Unexpected index: $index")
                }
            }
            ShareFileURL(title, fileName, src)
        }
    }
}

/** Serializer for [ShareGroup]. */
@OptIn(ExperimentalSerializationApi::class)
object ShareGroupSerializer : KSerializer<ShareGroup> {
    override val descriptor: SerialDescriptor = buildClassSerialDescriptor("ShareGroup") {
        element<String>("type")
        element<String>("title")
        element<Boolean?>("selected")
        element<String>("data") // JSON encoded links or files
    }

    override fun serialize(encoder: Encoder, value: ShareGroup) {
        encoder.encodeStructure(descriptor) {
            when (value) {
                is ShareGroup.Link -> {
                    encodeStringElement(descriptor, 0, "link")
                    encodeStringElement(descriptor, 1, value.title)
                    encodeNullableSerializableElement(
                        descriptor, 2,
                        kotlinx.serialization.serializer<Boolean?>(),
                        value.selected
                    )
                    encodeSerializableElement(
                        descriptor, 3,
                        ListSerializer(ShareLinkURLSerializer),
                        value.links
                    )
                }
                is ShareGroup.File -> {
                    encodeStringElement(descriptor, 0, "file")
                    encodeStringElement(descriptor, 1, value.title)
                    encodeNullableSerializableElement(
                        descriptor, 2,
                        kotlinx.serialization.serializer<Boolean?>(),
                        value.selected
                    )
                    encodeSerializableElement(
                        descriptor, 3,
                        ListSerializer(ShareFileURLSerializer),
                        value.files
                    )
                }
                is ShareGroup.Unknown -> {
                    encodeStringElement(descriptor, 0, "unknown")
                    encodeStringElement(descriptor, 1, value.title)
                    encodeNullableSerializableElement(
                        descriptor, 2,
                        kotlinx.serialization.serializer<Boolean?>(),
                        value.selected
                    )
                    encodeSerializableElement(
                        descriptor, 3,
                        ListSerializer(ShareLinkURLSerializer),
                        emptyList()
                    )
                }
            }
        }
    }

    override fun deserialize(decoder: Decoder): ShareGroup {
        return decoder.decodeStructure(descriptor) {
            var type = ""
            var title = ""
            var selected: Boolean? = null
            var links: List<ShareLinkURL> = emptyList()
            var files: List<ShareFileURL> = emptyList()

            while (true) {
                when (val index = decodeElementIndex(descriptor)) {
                    0 -> type = decodeStringElement(descriptor, 0)
                    1 -> title = decodeStringElement(descriptor, 1)
                    2 -> selected = decodeNullableSerializableElement(
                        descriptor, 2,
                        kotlinx.serialization.serializer<Boolean?>()
                    )
                    3 -> {
                        when (type) {
                            "link" -> links = decodeSerializableElement(
                                descriptor, 3,
                                ListSerializer(ShareLinkURLSerializer)
                            )
                            "file" -> files = decodeSerializableElement(
                                descriptor, 3,
                                ListSerializer(ShareFileURLSerializer)
                            )
                        }
                    }
                    CompositeDecoder.DECODE_DONE -> break
                    else -> error("Unexpected index: $index")
                }
            }

            when (type) {
                "link" -> ShareGroup.Link(title, selected, links)
                "file" -> ShareGroup.File(title, selected, files)
                else -> ShareGroup.Unknown(title, selected)
            }
        }
    }
}

/** Serializer for [ShareOptions]. */
@OptIn(ExperimentalSerializationApi::class)
object ShareOptionsSerializer : KSerializer<ShareOptions> {
    override val descriptor: SerialDescriptor = buildClassSerialDescriptor("ShareOptions") {
        element<List<ShareGroup>>("shareGroups")
        element<String>("shareText")
        element<Boolean?>("shareCTA")
        element<Boolean?>("personalize")
    }

    override fun serialize(encoder: Encoder, value: ShareOptions) {
        encoder.encodeStructure(descriptor) {
            encodeSerializableElement(
                descriptor, 0,
                ListSerializer(ShareGroupSerializer),
                value.shareGroups
            )
            encodeStringElement(descriptor, 1, value.shareText)
            encodeNullableSerializableElement(
                descriptor, 2,
                kotlinx.serialization.serializer<Boolean?>(),
                value.shareCTA
            )
            encodeNullableSerializableElement(
                descriptor, 3,
                kotlinx.serialization.serializer<Boolean?>(),
                value.personalize
            )
        }
    }

    override fun deserialize(decoder: Decoder): ShareOptions {
        return decoder.decodeStructure(descriptor) {
            var shareGroups: List<ShareGroup> = emptyList()
            var shareText = ""
            var shareCTA: Boolean? = null
            var personalize: Boolean? = null

            while (true) {
                when (val index = decodeElementIndex(descriptor)) {
                    0 -> shareGroups = decodeSerializableElement(
                        descriptor, 0,
                        ListSerializer(ShareGroupSerializer)
                    )
                    1 -> shareText = decodeStringElement(descriptor, 1)
                    2 -> shareCTA = decodeNullableSerializableElement(
                        descriptor, 2,
                        kotlinx.serialization.serializer<Boolean?>()
                    )
                    3 -> personalize = decodeNullableSerializableElement(
                        descriptor, 3,
                        kotlinx.serialization.serializer<Boolean?>()
                    )
                    CompositeDecoder.DECODE_DONE -> break
                    else -> error("Unexpected index: $index")
                }
            }
            ShareOptions(shareGroups, shareText, shareCTA, personalize)
        }
    }
}

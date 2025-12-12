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

package ss.document.components

import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.rememberModalBottomSheetState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.ui.graphics.Color
import app.ss.design.compose.extensions.haptics.LocalSsHapticFeedback
import app.ss.design.compose.theme.SsTheme
import app.ss.media.playback.ui.nowPlaying.AudioPlayerScreen
import app.ss.media.playback.ui.video.VideosScreen
import io.adventech.blockkit.ui.style.ReaderStyleConfig
import io.adventech.blockkit.ui.style.background
import io.adventech.blockkit.ui.style.primaryForeground
import ss.document.DocumentOverlayState
import ss.document.segment.components.overlay.BlocksOverlayContent
import ss.document.segment.components.overlay.ExcerptOverlayContent
import ss.libraries.navigation3.AudioPlayerKey
import ss.libraries.navigation3.LocalSsNavigator
import ss.libraries.navigation3.ReaderOptionsKey
import ss.libraries.navigation3.ShareOptionsKey
import ss.libraries.navigation3.VideosKey
import ss.share.options.ShareOptionsScreen

/**
 * Simplified overlay that handles all key types except HiddenSegmentKey.
 * Used when producers are not available.
 *
 * @param documentOverlayState Current overlay state.
 * @param readerStyle Current reader style configuration.
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
internal fun DocumentOverlaySimple(
    documentOverlayState: DocumentOverlayState?,
    readerStyle: ReaderStyleConfig,
) {
    val hapticFeedback = LocalSsHapticFeedback.current
    val containerColor = readerStyle.theme.background()
    val contentColor = readerStyle.theme.primaryForeground()
    val navigator = LocalSsNavigator.current

    when (val overlayState = documentOverlayState) {
        is DocumentOverlayState.BottomSheet -> {
            val sheetState = rememberModalBottomSheetState(
                skipPartiallyExpanded = overlayState.skipPartiallyExpanded
            )

            ModalBottomSheet(
                onDismissRequest = overlayState.onDismiss,
                sheetState = sheetState,
                containerColor = if (overlayState.themed) containerColor else SsTheme.colors.primaryBackground,
                contentColor = if (overlayState.themed) contentColor else SsTheme.colors.primaryForeground,
            ) {
                LaunchedEffect(overlayState.key) {
                    if (overlayState.feedback) {
                        hapticFeedback.performScreenView()
                    }
                }
                when (val key = overlayState.key) {
                    is ReaderOptionsKey -> {
                        ss.document.reader.ReaderOptionsScreen()
                    }
                    is AudioPlayerKey -> {
                        AudioPlayerScreen(
                            resourceId = key.resourceId,
                            segmentId = key.segmentId,
                        )
                    }
                    is VideosKey -> {
                        VideosScreen(
                            documentIndex = key.documentIndex,
                            documentId = key.documentId,
                        )
                    }
                    is ShareOptionsKey -> {
                        ShareOptionsScreen(
                            options = key.options,
                            title = key.title,
                            resourceColor = key.resourceColor,
                        )
                    }
                    else -> {
                        // For unknown keys (including HiddenSegmentKey without producers), just dismiss
                        LaunchedEffect(key) {
                            overlayState.onDismiss()
                        }
                    }
                }
            }
        }

        is DocumentOverlayState.Segment.Blocks -> {
            BlocksOverlayContent(
                state = overlayState.state,
                onDismiss = overlayState.onDismiss,
            )
        }

        is DocumentOverlayState.Segment.Excerpt -> {
            ExcerptOverlayContent(
                state = overlayState.state,
                onDismiss = overlayState.onDismiss,
            )
        }

        is DocumentOverlayState.Segment.None -> Unit
        null -> Unit
    }
}

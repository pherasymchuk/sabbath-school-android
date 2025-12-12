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

package ss.resource.components

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.rememberModalBottomSheetState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import app.ss.design.compose.extensions.haptics.LocalSsHapticFeedback
import ss.libraries.navigation3.NavKey
import ss.libraries.navigation3.ShareOptionsKey
import ss.resource.ResourceOverlayState

/**
 * Overlay content component for displaying resource overlays like introduction or share sheets.
 *
 * @param state Current overlay state.
 * @param onDismiss Callback when overlay is dismissed.
 * @param onNavigate Callback for navigation events.
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
internal fun ResourceOverlayContent(
    state: ResourceOverlayState?,
    onDismiss: () -> Unit,
    onNavigate: (NavKey) -> Unit,
) {
    val hapticFeedback = LocalSsHapticFeedback.current
    val sheetState = rememberModalBottomSheetState(skipPartiallyExpanded = true)

    when (state) {
        is ResourceOverlayState.IntroductionBottomSheet -> {
            ModalBottomSheet(
                onDismissRequest = onDismiss,
                sheetState = sheetState,
            ) {
                Column(
                    modifier = Modifier
                        .fillMaxSize()
                        .verticalScroll(rememberScrollState())
                        .padding(horizontal = 16.dp)
                ) {
                    MarkdownText(
                        text = state.markdown,
                        modifier = Modifier
                            .fillMaxSize()
                            .padding(horizontal = 8.dp, vertical = 16.dp),
                    )

                    Spacer(modifier = Modifier.height(48.dp))
                }

                LaunchedEffect(Unit) { hapticFeedback.performScreenView() }
            }
        }
        is ResourceOverlayState.ShareBottomSheet -> {
            ModalBottomSheet(
                onDismissRequest = onDismiss,
                sheetState = sheetState,
            ) {
                // Navigate to ShareOptions screen
                LaunchedEffect(Unit) {
                    onNavigate(
                        ShareOptionsKey(
                            options = state.options,
                            resourceColor = state.primaryColorDark,
                            title = state.title,
                        )
                    )
                    onDismiss()
                }
            }
        }
        null -> Unit
    }
}

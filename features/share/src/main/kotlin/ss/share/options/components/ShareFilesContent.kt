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

package ss.share.options.components

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.platform.LocalWindowInfo
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.tooling.preview.PreviewLightDark
import androidx.compose.ui.unit.DpOffset
import androidx.compose.ui.unit.dp
import app.ss.design.compose.extensions.haptics.SsHapticFeedback
import app.ss.design.compose.theme.SsTheme
import app.ss.design.compose.widget.icon.IconBox
import app.ss.design.compose.widget.icon.Icons
import io.adventech.blockkit.model.resource.ShareFileURL
import me.saket.cascade.CascadeDropdownMenu
import me.saket.cascade.rememberCascadeState

/**
 * Content component for displaying and selecting share files.
 *
 * @param files List of available share files.
 * @param hapticFeedback Haptic feedback handler.
 * @param onFile Callback when a file is selected.
 */
@Composable
internal fun ShareFilesContent(
    files: List<ShareFileURL>,
    hapticFeedback: SsHapticFeedback,
    onFile: (ShareFileURL) -> Unit,
) {
    var selectedFile by remember { mutableStateOf(files.firstOrNull()) }
    var showMenu by remember { mutableStateOf(false) }
    val cascadeState = rememberCascadeState()
    val screenWidth = (LocalWindowInfo.current.containerSize.width / LocalDensity.current.density).dp

    Box(
        modifier = Modifier.fillMaxWidth(),
        contentAlignment = Alignment.Center,
    ) {
        selectedFile?.takeUnless { files.size <= 1 || it.title.isNullOrEmpty() }?.let { file ->
            TextButton(
                onClick = {
                    hapticFeedback.performClick()
                    showMenu = true
                },
                enabled = files.size > 1,
            ) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.Center
                ) {
                    Text(
                        text = file.title ?: file.fileName ?: "",
                        style = SsTheme.typography.titleMedium.copy(
                            color = SsTheme.colors.primaryForeground
                        ),
                        modifier = Modifier
                            .padding(horizontal = 16.dp)
                            .padding(vertical = 8.dp, horizontal = 8.dp),
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis,
                        textAlign = TextAlign.Center
                    )

                    if (files.size > 1) {
                        IconBox(
                            icon = Icons.ArrowDropDown,
                            contentColor = SsTheme.colors.primaryForeground
                        )
                    }
                }
            }
        }

        CascadeDropdownMenu(
            expanded = showMenu,
            onDismissRequest = { showMenu = false },
            modifier = Modifier,
            state = cascadeState,
            offset = DpOffset(screenWidth / 4, 0.dp),
            shape = RoundedCornerShape(8.dp)
        ) {
            files.forEach { file ->
                DropdownMenuItem(
                    text = { Text(file.title ?: file.fileName ?: "") },
                    onClick = {
                        hapticFeedback.performClick()
                        selectedFile = file
                        showMenu = false
                    }
                )
            }
        }

    }

    LaunchedEffect(selectedFile) {
        selectedFile?.let { onFile(it) }
    }
}

@PreviewLightDark
@Composable
private fun ShareFilesContentPreview() {
    SsTheme {
        Surface {
            ShareFilesContent(
                files = listOf(
                    ShareFileURL(
                        src = "https://example.com/file.pdf",
                        title = "Example Document",
                        fileName = "document.pdf"
                    ),
                    ShareFileURL(
                        src = "https://example.com/file2.pdf",
                        title = "Another Document",
                        fileName = "document2.pdf"
                    )
                ),
                hapticFeedback = NoOpSsHapticFeedback,
                onFile = {},
            )
        }
    }
}

@PreviewLightDark
@Composable
private fun ShareFilesContentSinglePreview() {
    SsTheme {
        Surface {
            ShareFilesContent(
                files = listOf(
                    ShareFileURL(
                        src = "https://example.com/file.pdf",
                        title = "Example Document",
                        fileName = "document.pdf"
                    )
                ),
                hapticFeedback = NoOpSsHapticFeedback,
                onFile = {},
            )
        }
    }
}

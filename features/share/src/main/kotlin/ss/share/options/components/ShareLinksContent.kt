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

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
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
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.tooling.preview.PreviewLightDark
import androidx.compose.ui.unit.DpOffset
import androidx.compose.ui.unit.dp
import app.ss.design.compose.extensions.haptics.SsHapticFeedback
import app.ss.design.compose.theme.SsTheme
import app.ss.design.compose.widget.icon.IconBox
import app.ss.design.compose.widget.icon.Icons
import io.adventech.blockkit.model.resource.ShareLinkURL
import me.saket.cascade.CascadeDropdownMenu
import me.saket.cascade.rememberCascadeState

/**
 * Content component for displaying and selecting share links.
 *
 * @param links List of available share links.
 * @param hapticFeedback Haptic feedback handler.
 * @param onLink Callback when a link is selected.
 */
@Composable
internal fun ShareLinksContent(
    links: List<ShareLinkURL>,
    hapticFeedback: SsHapticFeedback,
    onLink: (ShareLinkURL) -> Unit,
) {
    var selectedLink by remember { mutableStateOf(links.firstOrNull()) }
    var showMenu by remember { mutableStateOf(false) }
    val cascadeState = rememberCascadeState()
    val screenWidth = (LocalWindowInfo.current.containerSize.width / LocalDensity.current.density).dp

    selectedLink?.let { link ->
        Box(
            modifier = Modifier.fillMaxWidth(),
            contentAlignment = Alignment.Center,
        ) {

            OutlinedButton(
                onClick = {
                    hapticFeedback.performClick()
                    showMenu = true
                },
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(8.dp),
                border = BorderStroke(1.dp, SsTheme.colors.dividers),
                enabled = links.size > 1
            ) {
                Text(
                    text = link.src,
                    style = SsTheme.typography.bodyLarge.copy(
                        color = SsTheme.colors.primaryForeground
                    ),
                    modifier = Modifier.weight(1f),
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis,
                )

                if (links.size > 1) {
                    IconBox(
                        icon = Icons.ArrowDropDown,
                        contentColor = SsTheme.colors.primaryForeground
                    )
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
                links.forEach { link ->
                    DropdownMenuItem(
                        text = { Text(link.title?.takeUnless { it.isEmpty() } ?: link.src) },
                        onClick = {
                            hapticFeedback.performClick()
                            selectedLink = link
                            showMenu = false
                        }
                    )
                }
            }
        }
    }

    LaunchedEffect(selectedLink) {
        selectedLink?.let { onLink(it) }
    }
}

@PreviewLightDark
@Composable
private fun ShareLinksContentPreview() {
    SsTheme {
        Surface {
            ShareLinksContent(
                links = listOf(
                    ShareLinkURL(
                        src = "https://example.com/resource/document",
                        title = "Example Resource",
                    ),
                    ShareLinkURL(
                        src = "https://example.com/resource/another",
                        title = "Another Resource",
                    )
                ),
                hapticFeedback = NoOpSsHapticFeedback,
                onLink = {},
            )
        }
    }
}

@PreviewLightDark
@Composable
private fun ShareLinksContentSinglePreview() {
    SsTheme {
        Surface {
            ShareLinksContent(
                links = listOf(
                    ShareLinkURL(
                        src = "https://example.com/resource/document",
                        title = "Example Resource",
                    )
                ),
                hapticFeedback = NoOpSsHapticFeedback,
                onLink = {},
            )
        }
    }
}

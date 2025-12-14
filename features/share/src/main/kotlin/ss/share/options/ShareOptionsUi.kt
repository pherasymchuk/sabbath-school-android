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

package ss.share.options

import androidx.compose.animation.AnimatedContent
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.material3.ElevatedButton
import androidx.compose.material3.LoadingIndicator
import androidx.compose.material3.SegmentedButton
import androidx.compose.material3.SegmentedButtonDefaults
import androidx.compose.material3.SingleChoiceSegmentedButtonRow
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.tooling.preview.PreviewLightDark
import androidx.compose.ui.unit.dp
import androidx.hilt.lifecycle.viewmodel.compose.hiltViewModel

import app.ss.design.compose.extensions.haptics.LocalSsHapticFeedback
import app.ss.design.compose.theme.SsTheme
import app.ss.design.compose.widget.button.SsButtonDefaults
import app.ss.design.compose.widget.icon.IconBox
import app.ss.design.compose.widget.icon.Icons
import io.adventech.blockkit.model.resource.ShareFileURL
import io.adventech.blockkit.model.resource.ShareGroup
import io.adventech.blockkit.model.resource.ShareLinkURL
import io.adventech.blockkit.model.resource.ShareOptions
import ss.share.options.components.ShareFilesContent
import ss.share.options.components.ShareLinksContent

@Composable
fun ShareOptionsScreen(
    options: ShareOptions,
    title: String,
    resourceColor: String?,
    modifier: Modifier = Modifier,
    viewModel: ShareOptionsViewModel = hiltViewModel(),
) {
    LaunchedEffect(options, title, resourceColor) {
        viewModel.setArgs(options, title, resourceColor)
    }

    val state by viewModel.state.collectAsState()

    ShareOptionsContent(
        state = state,
        modifier = modifier,
        onSegmentSelected = viewModel::onSegmentSelected,
        onShareUrlSelected = viewModel::onShareUrlSelected,
        onShareFileClicked = viewModel::onShareFileClicked,
        onShareLink = viewModel::shareLink,
        onShareFile = viewModel::shareFile,
    )
}

@Composable
private fun ShareOptionsContent(
    state: ShareState,
    modifier: Modifier = Modifier,
    onSegmentSelected: (String) -> Unit = {},
    onShareUrlSelected: (ShareLinkURL) -> Unit = {},
    onShareFileClicked: (ShareFileURL) -> Unit = {},
    onShareLink: (android.content.Context, ShareLinkURL) -> Unit = { _, _ -> },
    onShareFile: (android.content.Context, ShareFileURL) -> Unit = { _, _ -> },
) {
    val hapticFeedback = LocalSsHapticFeedback.current
    val context = LocalContext.current
    val buttonColors = SsButtonDefaults.colors(containerColor = state.themeColor ?: SsTheme.colors.primary)

    var selectedLink by remember { mutableStateOf<ShareLinkURL?>(null) }
    var selectedFile by remember { mutableStateOf<ShareFileURL?>(null) }

    // Reset selections when group changes
    LaunchedEffect(state.selectedGroup) {
        selectedLink = null
        selectedFile = null
    }

    Column(
        modifier = modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp)
            .padding(bottom = 32.dp),
        verticalArrangement = Arrangement.spacedBy(12.dp),
    ) {
        SingleChoiceSegmentedButtonRow(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 16.dp)
        ) {
            state.segments.forEachIndexed { index, group ->
                SegmentedButton(
                    selected = state.selectedGroup.title == group,
                    onClick = {
                        hapticFeedback.performSegmentSwitch()
                        onSegmentSelected(group)
                    },
                    colors = SegmentedButtonDefaults.colors(),
                    shape = SegmentedButtonDefaults.itemShape(index = index, count = state.segments.size)
                ) {
                    Text(
                        text = group,
                        style = SsTheme.typography.titleMedium
                    )
                }
            }
        }

        AnimatedContent(state.selectedGroup, label = "share_group") { group ->
            when (group) {
                is ShareGroup.File -> ShareFilesContent(group.files, hapticFeedback) { file ->
                    selectedFile = file
                    onShareFileClicked(file)
                }

                is ShareGroup.Link -> ShareLinksContent(group.links, hapticFeedback) { link ->
                    selectedLink = link
                    onShareUrlSelected(link)
                }

                is ShareGroup.Unknown -> Unit
            }
        }

        Spacer(Modifier.height(8.dp))

        ElevatedButton(
            onClick = {
                hapticFeedback.performClick()
                selectedLink?.let { onShareLink(context, it) }
                selectedFile?.let { onShareFile(context, it) }
            },
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 16.dp),
            enabled = state.shareButtonState == ShareButtonState.ENABLED,
            colors = buttonColors,
        ) {
            if (state.shareButtonState == ShareButtonState.LOADING) {
                LoadingIndicator(Modifier.size(32.dp))
            } else {
                Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    IconBox(
                        icon = Icons.Share,
                        contentColor = Color.White
                    )
                    Text(
                        text = "Share",
                        style = SsTheme.typography.titleMedium,
                        color = Color.White,
                    )
                }
            }
        }
    }
}

@PreviewLightDark
@Composable
private fun Preview() {
    SsTheme {
        Surface {
            ShareOptionsContent(
                state = ShareState(
                    segments = listOf("Link", "File"),
                    selectedGroup = ShareGroup.Link(
                        title = "Link",
                        links = listOf(
                            ShareLinkURL(
                                src = "https://example.com/resource/hdhdud/dhdhd",
                                title = "Example Resource",
                            ),
                            ShareLinkURL(
                                src = "https://example.com/resource",
                                title = "Example Resource",
                            )
                        ),
                        selected = null,
                    ),
                    shareButtonState = ShareButtonState.ENABLED,
                    themeColor = null,
                ),
                modifier = Modifier.padding(16.dp),
            )
        }
    }
}

@PreviewLightDark
@Composable
private fun PreviewFile() {
    SsTheme {
        Surface {
            ShareOptionsContent(
                state = ShareState(
                    segments = listOf("Link", "File"),
                    selectedGroup = ShareGroup.File(
                        title = "File",
                        files = listOf(
                            ShareFileURL(
                                src = "https://example.com/resource/hdhdud/dhdhd",
                                title = "Example Resource",
                                fileName = null
                            ),
                            ShareFileURL(
                                src = "https://example.com/resource",
                                title = "Example Resource",
                                fileName = null
                            )
                        ),
                        selected = true
                    ),
                    shareButtonState = ShareButtonState.LOADING,
                    themeColor = null,
                ),
                modifier = Modifier.padding(16.dp),
            )
        }
    }
}

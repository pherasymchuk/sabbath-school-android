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

package app.ss.media.playback.ui.nowPlaying.components

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.IconButton
import androidx.compose.material3.Surface
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.tooling.preview.PreviewLightDark
import androidx.compose.ui.unit.dp
import app.ss.design.compose.theme.SsTheme
import app.ss.design.compose.widget.icon.IconBox
import app.ss.design.compose.widget.icon.IconSlot
import app.ss.media.playback.ui.common.PlaybackSpeedLabel
import ss.libraries.media.model.PlaybackSpeed
import app.ss.translations.R as L10nR
import ss.libraries.media.resources.R as MediaR

/**
 * Bottom controls component for audio player with playback speed and playlist toggle.
 *
 * @param playbackSpeed Current playback speed.
 * @param modifier Modifier for this composable.
 * @param toggleSpeed Callback when speed toggle is clicked.
 * @param toggleExpand Callback when expand/playlist toggle is clicked.
 */
@Composable
internal fun BottomControls(
    playbackSpeed: PlaybackSpeed,
    modifier: Modifier = Modifier,
    toggleSpeed: () -> Unit = {},
    toggleExpand: () -> Unit = {},
) {
    Row(
        modifier = modifier
            .fillMaxWidth()
            .padding(end = 8.dp),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.SpaceBetween
    ) {

        PlaybackSpeedLabel(
            playbackSpeed = playbackSpeed,
            toggleSpeed = { toggleSpeed() },
            contentColor = SsTheme.colors.iconsSecondary
        )

        IconButton(onClick = toggleExpand) {
            IconBox(
                icon = IconSlot.fromResource(
                    MediaR.drawable.ic_audio_icon_playlist,
                    contentDescription = L10nR.string.ss_action_playlist
                ),
                contentColor = SsTheme.colors.iconsSecondary
            )
        }
    }
}

@PreviewLightDark
@Composable
private fun BottomControlsPreview() {
    SsTheme {
        Surface {
            BottomControls(
                playbackSpeed = PlaybackSpeed.NORMAL,
                modifier = Modifier.padding(16.dp),
            )
        }
    }
}

@PreviewLightDark
@Composable
private fun BottomControlsFastPreview() {
    SsTheme {
        Surface {
            BottomControls(
                playbackSpeed = PlaybackSpeed.FAST,
                modifier = Modifier.padding(16.dp),
            )
        }
    }
}

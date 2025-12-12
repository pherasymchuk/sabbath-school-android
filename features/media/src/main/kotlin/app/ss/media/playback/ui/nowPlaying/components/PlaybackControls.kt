/*
 * Copyright (c) 2023. Adventech <info@adventech.io>
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

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.ErrorOutline
import androidx.compose.material3.IconButtonColors
import androidx.compose.material3.IconButtonDefaults
import androidx.compose.material3.LocalContentColor
import androidx.compose.material3.Surface
import androidx.compose.material3.minimumInteractiveComponentSize
import androidx.compose.material3.ripple
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.Stable
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.rememberVectorPainter
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.tooling.preview.PreviewLightDark
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import app.ss.design.compose.theme.Dimens
import app.ss.design.compose.theme.SsTheme
import app.ss.design.compose.widget.icon.IconBox
import app.ss.design.compose.widget.icon.IconSlot
import ss.services.media.ui.PlaybackConnection
import ss.services.media.ui.spec.PlaybackStateSpec
import app.ss.translations.R.string as RString
import ss.libraries.media.resources.R as MediaR

internal object PlayBackControlsDefaults {
    val nonPlayButtonSize = 38.dp
    val nonPlayButtonStateLayerSize = 54.dp
    val playButtonSize = 46.dp
    val playButtonStateLayerSize = 62.dp
    val playButtonHorizontalPadding = 46.dp
}

@Composable
internal fun PlayBackControls(
    spec: PlaybackStateSpec,
    contentColor: Color,
    modifier: Modifier = Modifier,
    playbackConnection: PlaybackConnection
) {
    Row(
        modifier = modifier
            .fillMaxWidth()
            .padding(horizontal = Dimens.grid_6),
        horizontalArrangement = Arrangement.Center,
        verticalAlignment = Alignment.CenterVertically
    ) {
        Spacer(modifier = Modifier.weight(1f))

        CustomIconButton(
            onClick = { playbackConnection.rewind() },
            stateLayerSize = PlayBackControlsDefaults.nonPlayButtonStateLayerSize
        ) {
            IconBox(
                icon = IconSlot.fromResource(
                    MediaR.drawable.ic_audio_icon_backward,
                    contentDescription = RString.ss_action_rewind
                ),
                contentColor = contentColor,
                modifier = Modifier.size(PlayBackControlsDefaults.nonPlayButtonSize)
            )
        }

        Spacer(modifier = Modifier.width(PlayBackControlsDefaults.playButtonHorizontalPadding))

        CustomIconButton(
            onClick = { playbackConnection.playPause() },
            stateLayerSize = PlayBackControlsDefaults.playButtonStateLayerSize
        ) {
            val painter = when {
                spec.isPlaying -> painterResource(id = MediaR.drawable.ic_audio_icon_pause)
                spec.isError -> rememberVectorPainter(Icons.Rounded.ErrorOutline)
                spec.isPlayEnabled -> painterResource(id = MediaR.drawable.ic_audio_icon_play)
                else -> painterResource(id = MediaR.drawable.ic_audio_icon_play)
            }
            IconBox(
                icon = IconSlot.fromPainter(
                    painter = painter,
                    contentDescription = stringResource(id = RString.ss_action_play_pause)
                ),
                contentColor = contentColor,
                modifier = Modifier.size(PlayBackControlsDefaults.playButtonSize)
            )
        }

        Spacer(modifier = Modifier.width(PlayBackControlsDefaults.playButtonHorizontalPadding))

        CustomIconButton(
            onClick = { playbackConnection.fastForward() },
            stateLayerSize = PlayBackControlsDefaults.nonPlayButtonStateLayerSize
        ) {
            IconBox(
                icon = IconSlot.fromResource(
                    MediaR.drawable.ic_audio_icon_forward,
                    contentDescription = RString.ss_action_forward
                ),
                contentColor = contentColor,
                modifier = Modifier.size(PlayBackControlsDefaults.nonPlayButtonSize)
            )
        }

        Spacer(modifier = Modifier.weight(1f))
    }
}

@Composable
private fun CustomIconButton(
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
    enabled: Boolean = true,
    colors: IconButtonColors = IconButtonDefaults.iconButtonColors(),
    interactionSource: MutableInteractionSource = remember { MutableInteractionSource() },
    stateLayerSize: Dp = 48.dp,
    content: @Composable () -> Unit
) {
    Box(
        modifier = modifier
            .minimumInteractiveComponentSize()
            .size(stateLayerSize)
            .clip(CircleShape)
            .background(color = colors.containerColor(enabled))
            .clickable(
                onClick = onClick,
                enabled = enabled,
                role = Role.Button,
                interactionSource = interactionSource,
                indication = ripple(
                    bounded = false,
                    radius = stateLayerSize / 2
                )
            ),
        contentAlignment = Alignment.Center
    ) {
        val contentColor = colors.contentColor(enabled)
        CompositionLocalProvider(LocalContentColor provides contentColor, content = content)
    }
}

@Stable
private fun IconButtonColors.containerColor(enabled: Boolean): Color {
    return if (enabled) containerColor else disabledContainerColor
}

@Stable
private fun IconButtonColors.contentColor(enabled: Boolean): Color {
    return if (enabled) contentColor else disabledContentColor
}

@PreviewLightDark
@Composable
private fun PlayBackControlsPreview() {
    SsTheme {
        Surface {
            PlayBackControls(
                spec = PlaybackStateSpec(
                    isPlaying = false,
                    isPlayEnabled = true,
                    isError = false,
                    isBuffering = false,
                    canShowMini = false,
                ),
                contentColor = SsTheme.colors.primaryForeground,
                playbackConnection = NoOpPlaybackConnection,
            )
        }
    }
}

@PreviewLightDark
@Composable
private fun PlayBackControlsPlayingPreview() {
    SsTheme {
        Surface {
            PlayBackControls(
                spec = PlaybackStateSpec(
                    isPlaying = true,
                    isPlayEnabled = true,
                    isError = false,
                    isBuffering = false,
                    canShowMini = false,
                ),
                contentColor = SsTheme.colors.primaryForeground,
                playbackConnection = NoOpPlaybackConnection,
            )
        }
    }
}

private object NoOpPlaybackConnection : PlaybackConnection {
    override val isConnected = kotlinx.coroutines.flow.MutableStateFlow(false)
    override val playbackState = kotlinx.coroutines.flow.MutableStateFlow(PlaybackStateSpec.NONE)
    override val nowPlaying = kotlinx.coroutines.flow.MutableStateFlow(androidx.media3.common.MediaMetadata.EMPTY)
    override val playbackQueue = kotlinx.coroutines.flow.MutableStateFlow(ss.libraries.media.model.PlaybackQueue())
    override val playbackProgress = kotlinx.coroutines.flow.MutableStateFlow(ss.libraries.media.model.PlaybackProgressState())
    override val playbackSpeed = kotlinx.coroutines.flow.MutableStateFlow(ss.libraries.media.model.PlaybackSpeed.NORMAL)
    override fun playPause() = Unit
    override fun playAudio(audio: app.ss.models.media.AudioFile) = Unit
    override fun playAudios(audios: List<app.ss.models.media.AudioFile>, index: Int) = Unit
    override fun toggleSpeed() = Unit
    override fun setQueue(audios: List<app.ss.models.media.AudioFile>, index: Int) = Unit
    override fun skipToItem(position: Int) = Unit
    override fun seekTo(progress: Long) = Unit
    override fun rewind() = Unit
    override fun fastForward() = Unit
    override fun stop() = Unit
    override fun releaseMini() = Unit
}

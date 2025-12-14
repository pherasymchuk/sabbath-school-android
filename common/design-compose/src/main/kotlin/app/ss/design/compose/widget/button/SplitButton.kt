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

package app.ss.design.compose.widget.button

import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.size
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.KeyboardArrowDown
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.ExperimentalMaterial3ExpressiveApi
import androidx.compose.material3.Icon
import androidx.compose.material3.PlainTooltip
import androidx.compose.material3.SplitButtonDefaults
import androidx.compose.material3.SplitButtonLayout
import androidx.compose.material3.Text
import androidx.compose.material3.TooltipAnchorPosition
import androidx.compose.material3.TooltipBox
import androidx.compose.material3.TooltipDefaults
import androidx.compose.material3.rememberTooltipState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import kotlinx.collections.immutable.ImmutableList
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.graphics.painter.Painter
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.semantics.stateDescription
import app.ss.design.compose.extensions.haptics.LocalSsHapticFeedback

/**
 * A data class representing an option in the SplitButton dropdown menu.
 *
 * @param id Unique identifier for the option
 * @param label Display label for the option
 * @param icon Optional icon for the option
 */
data class SplitButtonOption(
    val id: String,
    val label: String,
    val icon: Painter? = null,
)

/**
 * A Material 3 Expressive SplitButton for primary actions with dropdown options.
 *
 * The SplitButton consists of:
 * - A leading button for the primary action
 * - A trailing toggle button that reveals additional options in a dropdown menu
 *
 * This follows M3 Expressive design guidelines for split button interactions.
 *
 * @param leadingIcon Icon for the primary action button
 * @param leadingText Text for the primary action button
 * @param options List of dropdown options
 * @param onLeadingClick Callback when the primary button is clicked
 * @param onOptionSelected Callback when a dropdown option is selected
 * @param modifier Modifier to apply to the split button
 * @param leadingContentDescription Content description for accessibility
 */
@OptIn(ExperimentalMaterial3ExpressiveApi::class)
@Composable
fun SsSplitButton(
    leadingIcon: Painter,
    leadingText: String,
    options: ImmutableList<SplitButtonOption>,
    onLeadingClick: () -> Unit,
    onOptionSelected: (SplitButtonOption) -> Unit,
    modifier: Modifier = Modifier,
    leadingContentDescription: String? = null,
) {
    val hapticFeedback = LocalSsHapticFeedback.current
    var expanded by remember { mutableStateOf(false) }

    SplitButtonLayout(
        modifier = modifier,
        leadingButton = {
            SplitButtonDefaults.LeadingButton(
                onClick = {
                    hapticFeedback.performClick()
                    onLeadingClick()
                }
            ) {
                Icon(
                    painter = leadingIcon,
                    modifier = Modifier.size(SplitButtonDefaults.LeadingIconSize),
                    contentDescription = leadingContentDescription,
                )
                Spacer(Modifier.size(ButtonDefaults.IconSpacing))
                Text(leadingText)
            }
        },
        trailingButton = {
            val description = "More options"
            TooltipBox(
                positionProvider = TooltipDefaults.rememberTooltipPositionProvider(
                    TooltipAnchorPosition.Above
                ),
                tooltip = { PlainTooltip { Text(description) } },
                state = rememberTooltipState(),
            ) {
                SplitButtonDefaults.TrailingButton(
                    checked = expanded,
                    onCheckedChange = {
                        expanded = it
                        hapticFeedback.performClick()
                    },
                    modifier = Modifier.semantics {
                        stateDescription = if (expanded) "Expanded" else "Collapsed"
                        contentDescription = description
                    },
                ) {
                    val rotation: Float by animateFloatAsState(
                        targetValue = if (expanded) 180f else 0f,
                        label = "Trailing Icon Rotation",
                    )
                    Icon(
                        imageVector = Icons.Filled.KeyboardArrowDown,
                        modifier = Modifier
                            .size(SplitButtonDefaults.TrailingIconSize)
                            .graphicsLayer { rotationZ = rotation },
                        contentDescription = null,
                    )
                }
            }
        },
    )

    DropdownMenu(
        expanded = expanded,
        onDismissRequest = { expanded = false }
    ) {
        options.forEach { option ->
            DropdownMenuItem(
                text = { Text(option.label) },
                onClick = {
                    expanded = false
                    hapticFeedback.performClick()
                    onOptionSelected(option)
                },
                leadingIcon = option.icon?.let { icon ->
                    { Icon(painter = icon, contentDescription = null) }
                },
            )
        }
    }
}

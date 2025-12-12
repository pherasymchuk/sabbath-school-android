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

import androidx.activity.compose.BackHandler
import androidx.compose.foundation.layout.padding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Close
import androidx.compose.material3.ExperimentalMaterial3ExpressiveApi
import androidx.compose.material3.FloatingActionButtonMenu
import androidx.compose.material3.FloatingActionButtonMenuItem
import androidx.compose.material3.Icon
import androidx.compose.material3.PlainTooltip
import androidx.compose.material3.Text
import androidx.compose.material3.ToggleFloatingActionButton
import androidx.compose.material3.ToggleFloatingActionButtonDefaults.animateIcon
import androidx.compose.material3.TooltipAnchorPosition
import androidx.compose.material3.TooltipBox
import androidx.compose.material3.TooltipDefaults
import androidx.compose.material3.rememberTooltipState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.derivedStateOf
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.vector.rememberVectorPainter
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.semantics.stateDescription
import androidx.compose.ui.semantics.traversalIndex
import androidx.compose.ui.unit.dp
import app.ss.design.compose.extensions.haptics.LocalSsHapticFeedback
import kotlinx.collections.immutable.ImmutableList
import app.ss.translations.R as L10nR

/**
 * A Material 3 Expressive FloatingActionButtonMenu for quick document actions.
 *
 * This component displays a morphing FAB that expands into a menu of actions
 * following M3 Expressive design guidelines.
 *
 * @param actions The list of actions to display in the menu
 * @param onActionClick Callback when an action is clicked
 * @param modifier Modifier to apply to the FAB menu
 */
@OptIn(ExperimentalMaterial3ExpressiveApi::class)
@Composable
internal fun DocumentActionsFabMenu(
    actions: ImmutableList<DocumentTopAppBarAction>,
    onActionClick: (DocumentTopAppBarAction) -> Unit,
    modifier: Modifier = Modifier,
) {
    val hapticFeedback = LocalSsHapticFeedback.current
    var fabMenuExpanded by rememberSaveable { mutableStateOf(false) }

    // Handle back press to close menu
    BackHandler(fabMenuExpanded) { fabMenuExpanded = false }

    // Filter to non-primary actions for the FAB menu
    val menuActions = remember(actions) {
        actions.filter { !it.primary }
    }

    if (menuActions.isEmpty()) return

    FloatingActionButtonMenu(
        modifier = modifier.padding(16.dp),
        expanded = fabMenuExpanded,
        button = {
            TooltipBox(
                positionProvider = TooltipDefaults.rememberTooltipPositionProvider(
                    if (fabMenuExpanded) {
                        TooltipAnchorPosition.Start
                    } else {
                        TooltipAnchorPosition.Above
                    }
                ),
                tooltip = { PlainTooltip { Text(stringResource(L10nR.string.ss_more)) } },
                state = rememberTooltipState(),
            ) {
                ToggleFloatingActionButton(
                    modifier = Modifier.semantics {
                        traversalIndex = -1f
                        stateDescription = if (fabMenuExpanded) "Expanded" else "Collapsed"
                        contentDescription = "Toggle actions menu"
                    },
                    checked = fabMenuExpanded,
                    onCheckedChange = {
                        fabMenuExpanded = !fabMenuExpanded
                        hapticFeedback.performClick()
                    },
                ) {
                    val imageVector by remember {
                        derivedStateOf {
                            if (checkedProgress > 0.5f) Icons.Filled.Close else Icons.Filled.Add
                        }
                    }
                    Icon(
                        painter = rememberVectorPainter(imageVector),
                        contentDescription = null,
                        modifier = Modifier.animateIcon({ checkedProgress }),
                    )
                }
            }
        },
    ) {
        menuActions.forEachIndexed { index, action ->
            FloatingActionButtonMenuItem(
                modifier = Modifier,
                onClick = {
                    fabMenuExpanded = false
                    hapticFeedback.performClick()
                    onActionClick(action)
                },
                icon = {
                    Icon(
                        painter = painterResource(action.iconRes),
                        contentDescription = null
                    )
                },
                text = { Text(text = stringResource(action.title)) },
            )
        }
    }
}

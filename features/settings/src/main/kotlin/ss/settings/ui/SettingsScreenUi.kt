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

package ss.settings.ui

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.IconButton
import androidx.compose.material3.LargeTopAppBar
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.input.nestedscroll.nestedScroll
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import app.ss.design.compose.extensions.haptics.LocalSsHapticFeedback
import app.ss.design.compose.widget.icon.IconBox
import app.ss.design.compose.widget.icon.Icons
import app.ss.design.compose.widget.scaffold.HazeScaffold
import kotlinx.collections.immutable.toImmutableList
import ss.libraries.navigation3.CustomTabsKey
import ss.libraries.navigation3.LocalSsNavigator
import ss.libraries.navigation3.LoginKey
import ss.settings.Overlay
import ss.settings.SettingsNavEvent
import ss.settings.SettingsViewModel
import ss.settings.ui.dialogs.ConfirmDeleteAccountDialog
import ss.settings.ui.dialogs.ConfirmRemoveDownloadsDialog
import ss.settings.ui.dialogs.ReminderTimePickerDialog
import androidx.compose.material3.Surface
import androidx.compose.ui.tooling.preview.PreviewLightDark
import app.ss.design.compose.extensions.list.ListEntity
import app.ss.design.compose.theme.SsTheme
import kotlinx.collections.immutable.persistentListOf
import app.ss.translations.R as L10nR

@Suppress("DEPRECATION")
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SettingsScreen(
    modifier: Modifier = Modifier,
    viewModel: SettingsViewModel = hiltViewModel(),
) {
    val navigator = LocalSsNavigator.current
    val entities by viewModel.entities.collectAsState()
    val overlay by viewModel.overlay.collectAsState()

    LaunchedEffect(Unit) {
        viewModel.navEvents.collect { event ->
            when (event) {
                is SettingsNavEvent.OpenUrl -> navigator.goTo(CustomTabsKey(event.url))
                SettingsNavEvent.NavigateToLogin -> navigator.resetRoot(LoginKey)
                SettingsNavEvent.NavigateBack -> navigator.pop()
            }
        }
    }

    SettingsContent(
        entities = entities.toImmutableList(),
        overlay = overlay,
        modifier = modifier,
        onBackClick = viewModel::navigateBack,
        onDismissOverlay = viewModel::dismissOverlay,
        onConfirmDeleteAccount = viewModel::confirmDeleteAccount,
        onSetReminderTime = viewModel::setReminderTime,
        onConfirmRemoveDownloads = viewModel::confirmRemoveDownloads,
    )
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun SettingsContent(
    entities: kotlinx.collections.immutable.ImmutableList<app.ss.design.compose.extensions.list.ListEntity>,
    overlay: Overlay?,
    modifier: Modifier = Modifier,
    onBackClick: () -> Unit = {},
    onDismissOverlay: () -> Unit = {},
    onConfirmDeleteAccount: () -> Unit = {},
    onSetReminderTime: (Int, Int) -> Unit = { _, _ -> },
    onConfirmRemoveDownloads: () -> Unit = {},
) {
    val scrollBehavior = TopAppBarDefaults.exitUntilCollapsedScrollBehavior()
    val hapticFeedback = LocalSsHapticFeedback.current

    HazeScaffold(
        modifier = modifier.nestedScroll(scrollBehavior.nestedScrollConnection),
        topBar = {
            LargeTopAppBar(
                title = { Text(text = stringResource(id = L10nR.string.ss_settings)) },
                navigationIcon = {
                    IconButton(onClick = {
                        hapticFeedback.performClick()
                        onBackClick()
                    }) {
                        IconBox(icon = Icons.ArrowBack)
                    }
                },
                scrollBehavior = scrollBehavior,
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = Color.Transparent,
                    scrolledContainerColor = Color.Transparent,
                )
            )
        },
        blurTopBar = true
    ) { contentPadding ->

        LazyColumn(
            modifier = Modifier,
            contentPadding = contentPadding,
            verticalArrangement = Arrangement.spacedBy(4.dp)
        ) {
            items(
                items = entities,
                key = { it.id }
            ) { item -> item.Content() }

            item { Spacer(Modifier.navigationBarsPadding()) }
        }
    }

    // Show dialogs based on overlay state
    when (val currentOverlay = overlay) {
        is Overlay.SelectReminderTime -> {
            ReminderTimePickerDialog(
                hour = currentOverlay.hour,
                minute = currentOverlay.minute,
                onDismiss = onDismissOverlay,
                onConfirm = onSetReminderTime,
            )
        }
        Overlay.ConfirmDeleteAccount -> {
            ConfirmDeleteAccountDialog(
                onDismiss = onDismissOverlay,
                onConfirm = onConfirmDeleteAccount,
            )
        }
        Overlay.ConfirmRemoveDownloads -> {
            ConfirmRemoveDownloadsDialog(
                onDismiss = onDismissOverlay,
                onConfirm = onConfirmRemoveDownloads,
            )
        }
        null -> {}
    }

    LaunchedEffect(overlay) {
        if (overlay != null) {
            hapticFeedback.performScreenView()
        }
    }
}

@PreviewLightDark
@Composable
private fun SettingsContentPreview() {
    SsTheme {
        Surface {
            SettingsContent(
                entities = persistentListOf(),
                overlay = null,
            )
        }
    }
}

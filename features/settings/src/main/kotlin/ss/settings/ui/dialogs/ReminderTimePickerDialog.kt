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

package ss.settings.ui.dialogs

import androidx.compose.material3.AlertDialog
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TimePicker
import androidx.compose.material3.rememberTimePickerState
import androidx.compose.runtime.Composable
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.tooling.preview.PreviewLightDark
import app.ss.design.compose.extensions.haptics.LocalSsHapticFeedback
import app.ss.design.compose.theme.SsTheme
import app.ss.translations.R as L10nR

/**
 * Dialog for selecting reminder time.
 *
 * @param hour Initial hour value (0-23).
 * @param minute Initial minute value (0-59).
 * @param onDismiss Callback when dialog is dismissed.
 * @param onConfirm Callback when time is confirmed with selected hour and minute.
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
internal fun ReminderTimePickerDialog(
    hour: Int,
    minute: Int,
    onDismiss: () -> Unit,
    onConfirm: (Int, Int) -> Unit,
) {
    val hapticFeedback = LocalSsHapticFeedback.current
    val timePickerState = rememberTimePickerState(
        initialHour = hour,
        initialMinute = minute,
    )

    AlertDialog(
        onDismissRequest = {
            hapticFeedback.performClick()
            onDismiss()
        },
        title = { Text(stringResource(L10nR.string.ss_settings_reminder_time)) },
        text = {
            TimePicker(state = timePickerState)
        },
        confirmButton = {
            TextButton(onClick = {
                hapticFeedback.performSuccess()
                onConfirm(timePickerState.hour, timePickerState.minute)
            }) {
                Text(stringResource(android.R.string.ok))
            }
        },
        dismissButton = {
            TextButton(onClick = {
                hapticFeedback.performClick()
                onDismiss()
            }) {
                Text(stringResource(android.R.string.cancel))
            }
        }
    )
}

@OptIn(ExperimentalMaterial3Api::class)
@PreviewLightDark
@Composable
private fun ReminderTimePickerDialogPreview() {
    SsTheme {
        Surface {
            ReminderTimePickerDialog(
                hour = 8,
                minute = 30,
                onDismiss = {},
                onConfirm = { _, _ -> },
            )
        }
    }
}

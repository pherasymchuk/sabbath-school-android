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
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.tooling.preview.PreviewLightDark
import app.ss.design.compose.extensions.haptics.LocalSsHapticFeedback
import app.ss.design.compose.theme.SsTheme
import app.ss.translations.R as L10nR

/**
 * Confirmation dialog for deleting user account.
 *
 * @param onDismiss Callback when dialog is dismissed.
 * @param onConfirm Callback when user confirms account deletion.
 */
@Composable
internal fun ConfirmDeleteAccountDialog(
    onDismiss: () -> Unit,
    onConfirm: () -> Unit,
) {
    val hapticFeedback = LocalSsHapticFeedback.current

    AlertDialog(
        onDismissRequest = {
            hapticFeedback.performSuccess()
            onDismiss()
        },
        title = { Text(stringResource(L10nR.string.ss_delete_account_question)) },
        text = {
            Text(
                text = stringResource(id = L10nR.string.ss_delete_account_warning),
                style = SsTheme.typography.bodyMedium
            )
        },
        confirmButton = {
            TextButton(onClick = {
                hapticFeedback.performError()
                onConfirm()
            }) {
                Text(stringResource(L10nR.string.ss_login_anonymously_dialog_positive))
            }
        },
        dismissButton = {
            TextButton(onClick = {
                hapticFeedback.performSuccess()
                onDismiss()
            }) {
                Text(stringResource(L10nR.string.ss_login_anonymously_dialog_negative))
            }
        }
    )
}

@PreviewLightDark
@Composable
private fun ConfirmDeleteAccountDialogPreview() {
    SsTheme {
        Surface {
            ConfirmDeleteAccountDialog(
                onDismiss = {},
                onConfirm = {},
            )
        }
    }
}

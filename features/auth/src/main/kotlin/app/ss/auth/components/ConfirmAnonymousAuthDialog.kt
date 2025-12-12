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

package app.ss.auth.components

import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.tooling.preview.PreviewLightDark
import app.ss.design.compose.theme.SsTheme
import app.ss.translations.R as L10nR

/**
 * Dialog to confirm anonymous authentication.
 *
 * @param onConfirm Callback when user confirms anonymous sign-in.
 * @param onDismiss Callback when user dismisses the dialog.
 */
@Composable
internal fun ConfirmAnonymousAuthDialog(
    onConfirm: () -> Unit,
    onDismiss: () -> Unit,
) {
    AlertDialog(
        onDismissRequest = onDismiss,
        title = {
            Text(text = stringResource(id = L10nR.string.ss_login_anonymously_dialog_title))
        },
        text = {
            Text(text = stringResource(id = L10nR.string.ss_login_anonymously_dialog_description))
        },
        confirmButton = {
            TextButton(onClick = onConfirm) {
                Text(text = stringResource(id = L10nR.string.ss_login_anonymously_dialog_positive))
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) {
                Text(text = stringResource(id = L10nR.string.ss_login_anonymously_dialog_negative))
            }
        }
    )
}

@PreviewLightDark
@Composable
private fun ConfirmAnonymousAuthDialogPreview() {
    SsTheme {
        Surface {
            ConfirmAnonymousAuthDialog(
                onConfirm = {},
                onDismiss = {},
            )
        }
    }
}

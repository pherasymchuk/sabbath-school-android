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

package app.ss.auth

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.foundation.Image
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Snackbar
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.ColorFilter
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.LinkAnnotation
import androidx.compose.ui.text.SpanStyle
import androidx.compose.ui.text.TextLinkStyles
import androidx.compose.ui.text.buildAnnotatedString
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextDecoration
import androidx.compose.ui.text.withLink
import androidx.compose.ui.tooling.preview.PreviewLightDark
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import app.ss.design.compose.extensions.snackbar.rememberSsSnackbarState
import app.ss.design.compose.theme.LatoFontFamily
import app.ss.design.compose.theme.SsTheme
import app.ss.design.compose.theme.color.SsColors
import ss.libraries.navigation3.LocalSsNavigator
import app.ss.auth.R as AuthR
import app.ss.translations.R as L10nR

@Composable
fun LoginScreen(
    viewModel: LoginViewModel,
    modifier: Modifier = Modifier,
) {
    val navigator = LocalSsNavigator.current
    LaunchedEffect(navigator) {
        viewModel.setNavigator(navigator)
    }

    val state by viewModel.state.collectAsState()
    val context = LocalContext.current

    LoginScreenContent(
        state = state,
        modifier = modifier,
        onSignInWithGoogle = { viewModel.signInWithGoogle(context) },
        onSignInAnonymously = viewModel::showConfirmAnonymousAuth,
        onConfirmAnonymous = viewModel::confirmAnonymousAuth,
        onDismissAnonymous = viewModel::dismissAnonymousAuth,
    )
}

@Composable
internal fun LoginScreenContent(
    state: LoginState,
    modifier: Modifier = Modifier,
    onSignInWithGoogle: () -> Unit = {},
    onSignInAnonymously: () -> Unit = {},
    onConfirmAnonymous: () -> Unit = {},
    onDismissAnonymous: () -> Unit = {},
) {
    Scaffold(
        snackbarHost = {
            if (state is LoginState.Default && state.snackbarState != null) {
                val snackbarState = rememberSsSnackbarState(state.snackbarState)
                SnackbarHost(snackbarState, modifier = Modifier) { data -> Snackbar(snackbarData = data) }
            }
        }
    ) { paddingValues ->
        Column(
            modifier = modifier
                .padding(paddingValues)
                .fillMaxSize()
                .padding(22.dp)
                .verticalScroll(rememberScrollState()),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center,
        ) {
            Spacer(modifier = Modifier.weight(0.2f))

            Image(
                painter = painterResource(id = AuthR.drawable.ic_logo_sspm_scaled),
                contentDescription = null,
                colorFilter = ColorFilter.tint(
                    color = if (SsTheme.colors.isDark) Color.White else SsColors.BaseBlue
                )
            )

            Spacer(modifier = Modifier.height(16.dp))

            Text(
                text = stringResource(id = L10nR.string.ss_app_name),
                style = SsTheme.typography.headlineSmall,
                color = if (SsTheme.colors.isDark) Color.White else SsColors.BaseBlue
            )

            Spacer(modifier = Modifier.weight(1f))

            AnimatedVisibility(visible = state is LoginState.Loading) {
                Column {
                    CircularProgressIndicator(
                        modifier = Modifier
                            .size(48.dp)
                            .align(Alignment.CenterHorizontally)
                    )

                    Spacer(modifier = Modifier.height(32.dp))
                }
            }

            Buttons(
                enabled = state !is LoginState.Loading,
                modifier = Modifier.align(Alignment.CenterHorizontally),
                onSignInWithGoogle = onSignInWithGoogle,
                onSignInAnonymously = onSignInAnonymously,
            )

            Spacer(modifier = Modifier.weight(0.2f))
        }
    }

    // Show confirm anonymous auth dialog
    if (state is LoginState.ConfirmSignInAnonymously) {
        ConfirmAnonymousAuthDialog(
            onConfirm = onConfirmAnonymous,
            onDismiss = onDismissAnonymous,
        )
    }
}

@Composable
private fun Buttons(
    enabled: Boolean,
    modifier: Modifier = Modifier,
    onSignInWithGoogle: () -> Unit = {},
    onSignInAnonymously: () -> Unit = {},
) {
    val context = LocalContext.current

    Column(modifier = modifier.width(270.dp)) {

        Button(
            onClick = onSignInWithGoogle,
            modifier = Modifier
                .fillMaxWidth()
                .padding(vertical = 4.dp),
            enabled = enabled,
            shape = RoundedCornerShape(4.dp),
            colors = ButtonDefaults.buttonColors(
                containerColor = Color.White,
                contentColor = SsColors.BaseGrey3
            ),
            elevation = ButtonDefaults.buttonElevation(
                defaultElevation = 4.dp
            )
        ) {

            Icon(
                painter = painterResource(id = AuthR.drawable.ic_google_logo),
                contentDescription = null,
                tint = Color.Unspecified
            )

            Spacer(modifier = Modifier.width(8.dp))

            Text(
                text = "Sign in with Google",
                style = MaterialTheme.typography.titleMedium.copy(
                    fontWeight = FontWeight.Bold
                )
            )
        }

        TextButton(
            onClick = onSignInAnonymously,
            modifier = Modifier
                .fillMaxWidth(),
            enabled = enabled,
        ) {
            Text(
                text = stringResource(id = L10nR.string.ss_login_button_anonymous),
                style = MaterialTheme.typography.titleMedium.copy(
                    fontWeight = FontWeight.Bold
                ),
                color = SsTheme.colors.primaryForeground
            )
        }

        Spacer(modifier = Modifier.height(8.dp))

        val textColor = SsTheme.colors.primaryForeground
        val annotatedText by remember {
            mutableStateOf(
                buildAnnotatedString {
                    append("By continuing, you agree to our Terms of Service as described in our ")
                    withLink(
                        link = LinkAnnotation.Url(
                            url = context.getString(L10nR.string.ss_privacy_policy_url),
                            styles = TextLinkStyles(
                                style = SpanStyle(
                                    color = textColor,
                                    fontWeight = FontWeight.Bold,
                                    fontFamily = LatoFontFamily,
                                    textDecoration = TextDecoration.Underline,
                                )
                            )
                        )
                    ) {
                        append("Privacy Policy")
                    }
                    append(".")
                    append(" Sabbath School collects User IDs to help identify and restore user saved content.")
                }
            )
        }
        Text(
            text = annotatedText,
            modifier = Modifier.fillMaxWidth(),
            style = SsTheme.typography.bodySmall.copy(fontSize = 11.sp, color = textColor),
        )
    }
}

@Composable
private fun ConfirmAnonymousAuthDialog(
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
private fun UiPreview() {
    SsTheme(useDynamicTheme = false) {
        Surface {
            LoginScreenContent(
                state = LoginState.Default(null),
                modifier = Modifier,
            )
        }
    }
}

@PreviewLightDark
@Composable
private fun UiPreviewLoading() {
    SsTheme(useDynamicTheme = false) {
        Surface {
            LoginScreenContent(
                state = LoginState.Loading,
                modifier = Modifier,
            )
        }
    }
}

@PreviewLightDark
@Composable
private fun UiPreviewDialog() {
    SsTheme {
        Surface {
            LoginScreenContent(
                state = LoginState.ConfirmSignInAnonymously,
                modifier = Modifier,
            )
        }
    }
}

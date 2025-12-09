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

package app.ss.auth

import android.content.Context
import androidx.credentials.CustomCredential
import androidx.credentials.GetCredentialRequest
import androidx.credentials.GetCredentialResponse
import androidx.credentials.exceptions.GetCredentialException
import androidx.credentials.exceptions.NoCredentialException
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import app.ss.design.compose.extensions.content.ContentSpec
import app.ss.design.compose.extensions.snackbar.SsSnackbarState
import app.ss.models.config.AppConfig
import com.google.android.libraries.identity.googleid.GetSignInWithGoogleOption
import com.google.android.libraries.identity.googleid.GoogleIdTokenCredential
import com.google.android.libraries.identity.googleid.GoogleIdTokenParsingException
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import ss.foundation.coroutines.DispatcherProvider
import ss.libraries.navigation3.CustomTabsKey
import ss.libraries.navigation3.HomeNavKey
import ss.libraries.navigation3.SsNavigator
import timber.log.Timber
import javax.inject.Inject
import app.ss.translations.R as L10nR

sealed interface LoginState {
    data class Default(
        val snackbarState: SsSnackbarState?,
    ) : LoginState

    data object Loading : LoginState
    data object ConfirmSignInAnonymously : LoginState
}

@HiltViewModel
class LoginViewModel @Inject constructor(
    private val appConfig: AppConfig,
    private val credentialManager: CredentialManagerWrapper,
    private val authRepository: AuthRepository,
    private val dispatcherProvider: DispatcherProvider,
) : ViewModel() {

    private val _state = MutableStateFlow<LoginState>(LoginState.Default(snackbarState = null))
    val state: StateFlow<LoginState> = _state.asStateFlow()

    private var navigator: SsNavigator? = null

    private val getCredentialRequest: GetCredentialRequest by lazy {
        GetCredentialRequest.Builder()
            .addCredentialOption(GetSignInWithGoogleOption.Builder(appConfig.webClientId).build())
            .build()
    }

    fun setNavigator(navigator: SsNavigator) {
        this.navigator = navigator
    }

    fun signInWithGoogle(context: Context) {
        _state.value = LoginState.Loading
        viewModelScope.launch {
            val isAuthenticated = authWithGoogle(context).getOrElse { false }
            if (isAuthenticated) {
                navigator?.resetRoot(HomeNavKey)
            } else {
                onAuthError()
            }
        }
    }

    fun showConfirmAnonymousAuth() {
        _state.value = LoginState.ConfirmSignInAnonymously
    }

    fun confirmAnonymousAuth() {
        _state.value = LoginState.Loading
        viewModelScope.launch {
            val isAuthenticated = authAnonymously().getOrElse { false }
            if (isAuthenticated) {
                navigator?.resetRoot(HomeNavKey)
            } else {
                onAuthError()
            }
        }
    }

    fun dismissAnonymousAuth() {
        _state.value = LoginState.Default(snackbarState = null)
    }

    fun dismissSnackbar() {
        _state.value = LoginState.Default(snackbarState = null)
    }

    fun openPrivacyPolicy(context: Context) {
        navigator?.goTo(CustomTabsKey(context.getString(L10nR.string.ss_privacy_policy_url)))
    }

    private fun onAuthError() {
        _state.value = LoginState.Default(
            snackbarState = SsSnackbarState(message = ContentSpec.Res(L10nR.string.ss_login_failed)) {
                dismissSnackbar()
            }
        )
    }

    private suspend fun authWithGoogle(context: Context): Result<Boolean> {
        return try {
            val response = credentialManager.getCredential(context, getCredentialRequest)
            val authenticated = handleCredentialResponse(response)
            Result.success(authenticated)
        } catch (ex: GetCredentialException) {
            Timber.e(ex)
            Result.failure(ex)
        } catch (ex: NoCredentialException) {
            Timber.e(ex)
            Result.failure(ex)
        }
    }

    private suspend fun handleCredentialResponse(response: GetCredentialResponse): Boolean {
        return (response.credential as? CustomCredential)?.let { credential ->
            if (credential.type == GoogleIdTokenCredential.TYPE_GOOGLE_ID_TOKEN_CREDENTIAL) {
                try {
                    val googleIdTokenCredential = GoogleIdTokenCredential
                        .createFrom(credential.data)
                    val token = googleIdTokenCredential.idToken
                    val signInResult = withContext(dispatcherProvider.default) { authRepository.signIn(token) }

                    signInResult.getOrNull() is AuthResponse.Authenticated

                } catch (e: GoogleIdTokenParsingException) {
                    Timber.e("Received an invalid google id token response - $e")
                    false
                }
            } else {
                Timber.e("Unexpected type of credential - ${credential.type}")
                false
            }
        } ?: run {
            Timber.e("Unexpected type of credential - ${response.credential.type}")
            false
        }
    }

    private suspend fun authAnonymously(): Result<Boolean> {
        val signInResult = withContext(dispatcherProvider.default) { authRepository.signIn() }
        return Result.success(signInResult.getOrNull() is AuthResponse.Authenticated)
    }
}

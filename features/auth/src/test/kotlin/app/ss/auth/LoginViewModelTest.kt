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
import androidx.credentials.GetCredentialRequest
import androidx.credentials.GetCredentialResponse
import androidx.test.core.app.ApplicationProvider
import androidx.test.ext.junit.runners.AndroidJUnit4
import app.cash.turbine.test
import app.ss.auth.test.FakeAuthRepository
import app.ss.design.compose.extensions.content.ContentSpec
import app.ss.models.auth.SSUser
import app.ss.models.config.AppConfig
import com.google.android.libraries.identity.googleid.GetSignInWithGoogleOption
import com.google.android.libraries.identity.googleid.GoogleIdTokenCredential
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.test.StandardTestDispatcher
import kotlinx.coroutines.test.resetMain
import kotlinx.coroutines.test.runTest
import kotlinx.coroutines.test.setMain
import org.amshove.kluent.shouldBeEqualTo
import org.amshove.kluent.shouldBeInstanceOf
import org.amshove.kluent.shouldNotBeNull
import org.junit.After
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith
import ss.foundation.coroutines.test.TestDispatcherProvider
import ss.libraries.navigation3.CustomTabsKey
import ss.libraries.navigation3.HomeNavKey
import ss.libraries.navigation3.test.FakeSsNavigator
import app.ss.translations.R as L10nR

private const val WEB_CLIENT_ID = "web_id"

/** Tests for [LoginViewModel]. */
@OptIn(ExperimentalCoroutinesApi::class)
@RunWith(AndroidJUnit4::class)
class LoginViewModelTest {

    private val testDispatcher = StandardTestDispatcher()
    private val appConfig = AppConfig(version = "", versionCode = 1, webClientId = WEB_CLIENT_ID)
    private val fakeCredentialManager = FakeCredentialManagerWrapper()
    private val fakeAuthRepository = FakeAuthRepository()
    private val fakeNavigator = FakeSsNavigator()

    private val context: Context = ApplicationProvider.getApplicationContext()

    private lateinit var underTest: LoginViewModel

    @Before
    fun setUp() {
        Dispatchers.setMain(testDispatcher)
        underTest = LoginViewModel(
            appConfig = appConfig,
            credentialManager = fakeCredentialManager,
            authRepository = fakeAuthRepository,
            dispatcherProvider = TestDispatcherProvider(testDispatcher),
        )
        underTest.setNavigator(fakeNavigator)
    }

    @After
    fun tearDown() {
        Dispatchers.resetMain()
    }

    @Test
    fun `state - default state`() = runTest {
        underTest.state.test {
            val state = awaitItem()
            state shouldBeInstanceOf LoginState.Default::class.java
            (state as LoginState.Default).snackbarState shouldBeEqualTo null

            cancelAndConsumeRemainingEvents()
        }
    }

    @Test
    fun `showConfirmAnonymousAuth - transitions to ConfirmSignInAnonymously`() = runTest {
        underTest.state.test {
            awaitItem() // Default

            underTest.showConfirmAnonymousAuth()

            awaitItem() shouldBeEqualTo LoginState.ConfirmSignInAnonymously

            cancelAndConsumeRemainingEvents()
        }
    }

    @Test
    fun `dismissAnonymousAuth - returns to Default state`() = runTest {
        underTest.state.test {
            awaitItem() // Default

            underTest.showConfirmAnonymousAuth()
            awaitItem() // ConfirmSignInAnonymously

            underTest.dismissAnonymousAuth()

            val state = awaitItem() as LoginState.Default
            state.snackbarState shouldBeEqualTo null

            cancelAndConsumeRemainingEvents()
        }
    }

    @Test
    fun `confirmAnonymousAuth - success - navigates to home`() = runTest {
        fakeAuthRepository.signInDelegate = {
            Result.success(AuthResponse.Authenticated(SSUser.fake()))
        }

        underTest.state.test {
            awaitItem() // Default

            underTest.confirmAnonymousAuth()

            awaitItem() shouldBeEqualTo LoginState.Loading

            testDispatcher.scheduler.advanceUntilIdle()

            fakeNavigator.resetRootKey shouldBeEqualTo HomeNavKey

            cancelAndConsumeRemainingEvents()
        }
    }

    @Test
    fun `confirmAnonymousAuth - failure - shows error snackbar`() = runTest {
        fakeAuthRepository.signInDelegate = {
            Result.success(AuthResponse.Error)
        }

        underTest.state.test {
            awaitItem() // Default

            underTest.confirmAnonymousAuth()

            awaitItem() shouldBeEqualTo LoginState.Loading

            testDispatcher.scheduler.advanceUntilIdle()

            val state = awaitItem() as LoginState.Default
            state.snackbarState.shouldNotBeNull()
            state.snackbarState!!.message shouldBeEqualTo ContentSpec.Res(L10nR.string.ss_login_failed)

            cancelAndConsumeRemainingEvents()
        }
    }

    @Test
    fun `signInWithGoogle - success - navigates to home`() = runTest {
        val token = "token"
        fakeCredentialManager.getCredentialDelegate = { _ ->
            GetCredentialResponse(
                GoogleIdTokenCredential(
                    id = "id",
                    idToken = token,
                    displayName = "",
                    familyName = "",
                    givenName = "",
                    profilePictureUri = null,
                    phoneNumber = null
                )
            )
        }
        fakeAuthRepository.signInWithTokenDelegate = {
            if (it == token) {
                Result.success(AuthResponse.Authenticated(SSUser.fake()))
            } else {
                throw IllegalStateException("Invalid token")
            }
        }

        underTest.state.test {
            awaitItem() // Default

            underTest.signInWithGoogle(context)

            awaitItem() shouldBeEqualTo LoginState.Loading

            testDispatcher.scheduler.advanceUntilIdle()

            fakeNavigator.resetRootKey shouldBeEqualTo HomeNavKey

            cancelAndConsumeRemainingEvents()
        }
    }

    @Test
    fun `openPrivacyPolicy - navigates to privacy policy url`() = runTest {
        underTest.openPrivacyPolicy(context)

        fakeNavigator.lastNavigatedKey shouldBeInstanceOf CustomTabsKey::class.java
        (fakeNavigator.lastNavigatedKey as CustomTabsKey).url shouldBeEqualTo context.getString(L10nR.string.ss_privacy_policy_url)
    }
}

private class FakeCredentialManagerWrapper : CredentialManagerWrapper {
    var getCredentialDelegate: (GetCredentialRequest) -> GetCredentialResponse = { throw NotImplementedError() }

    override suspend fun getCredential(context: Context, request: GetCredentialRequest): GetCredentialResponse {
        return getCredentialDelegate(request)
    }
}


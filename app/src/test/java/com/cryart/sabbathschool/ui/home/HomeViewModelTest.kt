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

package com.cryart.sabbathschool.ui.home

import android.content.Context
import app.cash.turbine.test
import app.ss.auth.test.FakeAuthRepository
import app.ss.models.auth.SSUser
import com.cryart.sabbathschool.reminder.DailyReminderManager
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.test.StandardTestDispatcher
import kotlinx.coroutines.test.advanceUntilIdle
import kotlinx.coroutines.test.resetMain
import kotlinx.coroutines.test.runTest
import kotlinx.coroutines.test.setMain
import org.amshove.kluent.shouldBeEqualTo
import org.junit.After
import org.junit.Before
import org.junit.Test
import ss.libraries.appwidget.api.FakeAppWidgetHelper
import ss.libraries.navigation3.HomeNavKey
import ss.libraries.navigation3.LoginKey
import ss.prefs.api.test.FakeSSPrefs

/** Tests for [HomeViewModel]. */
@OptIn(ExperimentalCoroutinesApi::class)
class HomeViewModelTest {

    private val testDispatcher = StandardTestDispatcher()
    private val fakeAuthRepository = FakeAuthRepository()
    private val fakePrefs = FakeSSPrefs()
    private val fakeDailyReminderManager = FakeDailyReminderManager()
    private val fakeAppWidgetHelper = FakeAppWidgetHelper()

    @Before
    fun setUp() {
        Dispatchers.setMain(testDispatcher)
    }

    @After
    fun tearDown() {
        Dispatchers.resetMain()
    }

    @Test
    fun `startupState - user authed - schedule reminder`() = runTest {
        fakeAuthRepository.userDelegate = { Result.success(SSUser.fake()) }
        with(fakePrefs) {
            reminderEnabledDelegate = { true }
            reminderScheduledDelegate = { false }
        }

        val underTest = HomeViewModel(
            ssPrefs = fakePrefs,
            authRepository = fakeAuthRepository,
            dailyReminderManager = fakeDailyReminderManager,
            appWidgetHelper = fakeAppWidgetHelper,
        )

        underTest.startupState.test {
            awaitItem() shouldBeEqualTo StartupState.Loading

            advanceUntilIdle()

            val readyState = awaitItem() as StartupState.Ready
            readyState.startKey shouldBeEqualTo HomeNavKey
            fakeDailyReminderManager.reminderScheduled shouldBeEqualTo true

            cancelAndConsumeRemainingEvents()
        }
    }

    @Test
    fun `startupState - user authed - reminder already scheduled`() = runTest {
        fakeAuthRepository.userDelegate = { Result.success(SSUser.fake()) }
        with(fakePrefs) {
            reminderEnabledDelegate = { true }
            reminderScheduledDelegate = { true }
        }

        val underTest = HomeViewModel(
            ssPrefs = fakePrefs,
            authRepository = fakeAuthRepository,
            dailyReminderManager = fakeDailyReminderManager,
            appWidgetHelper = fakeAppWidgetHelper,
        )

        underTest.startupState.test {
            awaitItem() shouldBeEqualTo StartupState.Loading

            advanceUntilIdle()

            val readyState = awaitItem() as StartupState.Ready
            readyState.startKey shouldBeEqualTo HomeNavKey
            fakeDailyReminderManager.reminderScheduled shouldBeEqualTo false

            cancelAndConsumeRemainingEvents()
        }
    }

    @Test
    fun `startupState - no user - go to Login`() = runTest {
        fakeAuthRepository.userDelegate = { Result.success(null) }

        val underTest = HomeViewModel(
            ssPrefs = fakePrefs,
            authRepository = fakeAuthRepository,
            dailyReminderManager = fakeDailyReminderManager,
            appWidgetHelper = fakeAppWidgetHelper,
        )

        underTest.startupState.test {
            awaitItem() shouldBeEqualTo StartupState.Loading

            advanceUntilIdle()

            val readyState = awaitItem() as StartupState.Ready
            readyState.startKey shouldBeEqualTo LoginKey

            cancelAndConsumeRemainingEvents()
        }
    }

    @Test
    fun `startupState - refresh app widgets`() = runTest {
        fakeAuthRepository.userDelegate = { Result.success(SSUser.fake()) }
        fakePrefs.reminderEnabledDelegate = { false }

        val underTest = HomeViewModel(
            ssPrefs = fakePrefs,
            authRepository = fakeAuthRepository,
            dailyReminderManager = fakeDailyReminderManager,
            appWidgetHelper = fakeAppWidgetHelper,
        )

        underTest.startupState.test {
            awaitItem() shouldBeEqualTo StartupState.Loading

            advanceUntilIdle()

            val readyState = awaitItem() as StartupState.Ready
            readyState.startKey shouldBeEqualTo HomeNavKey
            fakeAppWidgetHelper.isRefreshAllCalled shouldBeEqualTo true

            cancelAndConsumeRemainingEvents()
        }
    }
}

private class FakeDailyReminderManager : DailyReminderManager {
    var reminderScheduled: Boolean = false
        private set

    override fun scheduleReminder() {
        reminderScheduled = true
    }

    override fun reSchedule() {}
    override fun showNotification(context: Context) {}
    override fun cancel() {}
}


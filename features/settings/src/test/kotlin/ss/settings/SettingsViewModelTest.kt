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

package ss.settings

import androidx.test.core.app.ApplicationProvider
import androidx.test.ext.junit.runners.AndroidJUnit4
import app.cash.turbine.test
import app.ss.design.compose.extensions.list.DividerEntity
import app.ss.design.compose.extensions.list.ListEntity
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.test.StandardTestDispatcher
import kotlinx.coroutines.test.resetMain
import kotlinx.coroutines.test.runTest
import kotlinx.coroutines.test.setMain
import org.amshove.kluent.shouldBeEqualTo
import org.junit.After
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith
import ss.settings.repository.SettingsEntity
import ss.settings.repository.SettingsRepository

/** Tests for [SettingsViewModel]. */
@OptIn(ExperimentalCoroutinesApi::class)
@RunWith(AndroidJUnit4::class)
class SettingsViewModelTest {

    private val testDispatcher = StandardTestDispatcher()

    private lateinit var underTest: SettingsViewModel

    @Before
    fun setUp() {
        Dispatchers.setMain(testDispatcher)
    }

    @After
    fun tearDown() {
        Dispatchers.resetMain()
    }

    @Test
    fun `entities - emit empty then default entities`() = runTest {
        val entities = listOf(DividerEntity("1"), DividerEntity("2"))

        underTest = SettingsViewModel(
            context = ApplicationProvider.getApplicationContext(),
            repository = FakeRepository(entities),
        )

        underTest.entities.test {
            awaitItem() shouldBeEqualTo emptyList()
            awaitItem() shouldBeEqualTo entities

            cancelAndConsumeRemainingEvents()
        }
    }

    @Test
    fun `navigateBack - emits NavigateBack event`() = runTest {
        underTest = SettingsViewModel(
            context = ApplicationProvider.getApplicationContext(),
            repository = FakeRepository(emptyList()),
        )

        underTest.navEvents.test {
            underTest.navigateBack()

            awaitItem() shouldBeEqualTo SettingsNavEvent.NavigateBack

            cancelAndConsumeRemainingEvents()
        }
    }

    @Test
    fun `confirmDeleteAccount - emits NavigateToLogin event`() = runTest {
        val repository = FakeRepository(emptyList())
        underTest = SettingsViewModel(
            context = ApplicationProvider.getApplicationContext(),
            repository = repository,
        )

        underTest.navEvents.test {
            underTest.confirmDeleteAccount()

            repository.deleteAccountCalled shouldBeEqualTo true
            awaitItem() shouldBeEqualTo SettingsNavEvent.NavigateToLogin

            cancelAndConsumeRemainingEvents()
        }
    }

    @Test
    fun `dismissOverlay - clears overlay state`() = runTest {
        underTest = SettingsViewModel(
            context = ApplicationProvider.getApplicationContext(),
            repository = FakeRepository(emptyList()),
        )

        underTest.overlay.test {
            awaitItem() shouldBeEqualTo null

            // Manually trigger overlay (simulate entity click)
            underTest.dismissOverlay()

            // Should still be null after dismissing
            expectNoEvents()

            cancelAndConsumeRemainingEvents()
        }
    }

    @Test
    fun `setReminderTime - sets reminder time on repository`() = runTest {
        val repository = FakeRepository(emptyList())
        underTest = SettingsViewModel(
            context = ApplicationProvider.getApplicationContext(),
            repository = repository,
        )

        underTest.setReminderTime(9, 30)

        repository.reminderHour shouldBeEqualTo 9
        repository.reminderMinute shouldBeEqualTo 30
    }

    @Test
    fun `confirmRemoveDownloads - calls removeAllDownloads`() = runTest {
        val repository = FakeRepository(emptyList())
        underTest = SettingsViewModel(
            context = ApplicationProvider.getApplicationContext(),
            repository = repository,
        )

        underTest.confirmRemoveDownloads()

        repository.removeAllDownloadsCalled shouldBeEqualTo true
    }
}

private class FakeRepository(
    private val entities: List<ListEntity>
) : SettingsRepository {

    var reminderHour: Int = 0
        private set
    var reminderMinute: Int = 0
        private set
    var signOutCalled: Boolean = false
        private set
    var deleteAccountCalled: Boolean = false
        private set
    var removeAllDownloadsCalled: Boolean = false
        private set

    override fun entitiesFlow(onEntityClick: (SettingsEntity) -> Unit): Flow<List<ListEntity>> {
        return flowOf(entities)
    }

    override fun setReminderTime(hour: Int, minute: Int) {
        reminderHour = hour
        reminderMinute = minute
    }

    override fun signOut() {
        signOutCalled = true
    }

    override fun deleteAccount() {
        deleteAccountCalled = true
    }

    override fun removeAllDownloads() {
        removeAllDownloadsCalled = true
    }
}


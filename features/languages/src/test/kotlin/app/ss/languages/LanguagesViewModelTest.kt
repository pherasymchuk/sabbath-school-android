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

package app.ss.languages

import app.cash.turbine.test
import app.ss.languages.state.LanguageUiModel
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.test.UnconfinedTestDispatcher
import kotlinx.coroutines.test.resetMain
import kotlinx.coroutines.test.runTest
import kotlinx.coroutines.test.setMain
import org.amshove.kluent.shouldBeEqualTo
import org.amshove.kluent.shouldBeInstanceOf
import org.junit.After
import org.junit.Before
import org.junit.Test
import ss.libraries.appwidget.api.FakeAppWidgetHelper
import ss.libraries.navigation3.HomeNavKey
import ss.libraries.navigation3.test.FakeSsNavigator
import ss.prefs.api.test.FakeSSPrefs
import ss.resources.api.test.FakeResourcesRepository
import ss.resources.model.LanguageModel

/** Tests for [LanguagesViewModel]. */
@OptIn(ExperimentalCoroutinesApi::class)
class LanguagesViewModelTest {

    private val testDispatcher = UnconfinedTestDispatcher()
    private val languagesFlow = MutableSharedFlow<List<LanguageModel>>()
    private val fakeRepository = FakeResourcesRepository(languagesFlow)
    private val fakeSSPrefs = FakeSSPrefs(MutableStateFlow("en"))
    private val fakeAppWidgetHelper = FakeAppWidgetHelper()
    private val fakeNavigator = FakeSsNavigator()

    private lateinit var underTest: LanguagesViewModel

    @Before
    fun setUp() {
        Dispatchers.setMain(testDispatcher)
        underTest = LanguagesViewModel(
            repository = fakeRepository,
            ssPrefs = fakeSSPrefs,
            appWidgetHelper = fakeAppWidgetHelper,
        )
        underTest.setNavigator(fakeNavigator)
    }

    @After
    fun tearDown() {
        Dispatchers.resetMain()
    }

    @Test
    fun `state - initial loading state`() = runTest {
        underTest.state.test {
            awaitItem() shouldBeInstanceOf LanguagesUiState.Loading::class.java

            cancelAndConsumeRemainingEvents()
        }
    }

    @Test
    fun `state - emit models with properly formatted native language name`() = runTest {
        underTest.state.test {
            awaitItem() shouldBeInstanceOf LanguagesUiState.Loading::class.java

            languagesFlow.emit(
                listOf(
                    LanguageModel("en", "English", "English"),
                    LanguageModel("es", "Spanish", "Español"),
                    LanguageModel("fr", "French", "Français"),
                )
            )

            val state = awaitItem() as LanguagesUiState.Languages
            state.models.toList() shouldBeEqualTo listOf(
                LanguageUiModel("en", "English", "English", true),
                LanguageUiModel("es", "Spanish", "Español", false),
                LanguageUiModel("fr", "French", "Français", false),
            )

            cancelAndConsumeRemainingEvents()
        }
    }

    @Test
    fun `search - filters languages`() = runTest {
        val query = "english"

        underTest.state.test {
            awaitItem() shouldBeInstanceOf LanguagesUiState.Loading::class.java

            languagesFlow.emit(emptyList())
            awaitItem() as LanguagesUiState.Languages

            underTest.search(query)

            languagesFlow.emit(listOf(LanguageModel("en", "English", "English")))

            val state = awaitItem() as LanguagesUiState.Languages
            state.models.first().code shouldBeEqualTo "en"

            cancelAndConsumeRemainingEvents()
        }
    }

    @Test
    fun `search - trims query`() = runTest {
        underTest.state.test {
            awaitItem() shouldBeInstanceOf LanguagesUiState.Loading::class.java

            languagesFlow.emit(emptyList())
            awaitItem() as LanguagesUiState.Languages

            underTest.search("   hello   ")

            languagesFlow.emit(listOf(LanguageModel("hello", "Hello Lang", "Hello")))

            val state = awaitItem() as LanguagesUiState.Languages
            state.models.first().code shouldBeEqualTo "hello"

            cancelAndConsumeRemainingEvents()
        }
    }

    @Test
    fun `selectLanguage - saves language code`() = runTest {
        underTest.selectLanguage(LanguageUiModel("es", "Spanish", "Español", false))

        fakeSSPrefs.setLanguageCode shouldBeEqualTo "es"
    }

    @Test
    fun `selectLanguage - language changed - calls languageChanged and resets root`() = runTest {
        fakeSSPrefs.setLanguageCode("en")

        underTest.selectLanguage(LanguageUiModel("es", "Spanish", "Español", false))

        fakeSSPrefs.setLanguageCode shouldBeEqualTo "es"
        fakeAppWidgetHelper.isLanguageChangedCalled shouldBeEqualTo true
        fakeNavigator.resetRootKey shouldBeEqualTo HomeNavKey
    }

    @Test
    fun `selectLanguage - no language change - pops navigation`() = runTest {
        fakeSSPrefs.setLanguageCode("en")

        underTest.selectLanguage(LanguageUiModel("en", "English", "English", false))

        fakeSSPrefs.setLanguageCode shouldBeEqualTo "en"
        fakeNavigator.popCalled shouldBeEqualTo true
    }

    @Test
    fun `navigateBack - pops navigation`() = runTest {
        underTest.navigateBack()

        fakeNavigator.popCalled shouldBeEqualTo true
    }
}


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

package com.cryart.sabbathschool.navigation

import android.app.Activity
import androidx.appcompat.app.AppCompatActivity
import androidx.core.net.toUri
import androidx.test.ext.junit.runners.AndroidJUnit4
import app.ss.auth.test.FakeAuthRepository
import app.ss.models.auth.SSUser
import com.cryart.sabbathschool.core.navigation.AppNavigator
import com.cryart.sabbathschool.core.navigation.Destination
import com.cryart.sabbathschool.ui.home.HomeActivity
import org.amshove.kluent.shouldBeEqualTo
import org.amshove.kluent.shouldBeNull
import org.amshove.kluent.shouldNotBeNull
import org.junit.After
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.Robolectric
import org.robolectric.Shadows
import org.robolectric.android.controller.ActivityController
import ss.foundation.coroutines.test.TestDispatcherProvider

@RunWith(AndroidJUnit4::class)
class AppNavigatorImplTest {

    private val fakeAuthRepository = FakeAuthRepository()

    private lateinit var controller: ActivityController<AppCompatActivity>
    private lateinit var activity: Activity

    private lateinit var navigator: AppNavigator

    @Before
    fun setup() {
        controller = Robolectric.buildActivity(AppCompatActivity::class.java)
        activity = controller.create().start().resume().get()

        fakeAuthRepository.userDelegate = { Result.success(SSUser.fake()) }

        navigator = AppNavigatorImpl(
            authRepository = fakeAuthRepository,
            dispatcherProvider = TestDispatcherProvider()
        )
    }

    @After
    fun tearDown() {
        activity.finish()
        controller.destroy()
    }

    @Test
    fun `should navigate to HomeActivity when not authenticated`() {
        fakeAuthRepository.userDelegate = { Result.success(null) }

        navigator.navigate(activity, Destination.ABOUT)

        val shadow = Shadows.shadowOf(activity)
        val intent = shadow.nextStartedActivity

        intent.shouldNotBeNull()
        intent.component?.className shouldBeEqualTo HomeActivity::class.java.name
    }

    @Test
    fun `should ignore invalid deep-link`() {
        val uri = "https://stackoverflow.com/".toUri()
        navigator.navigate(activity, uri)

        val shadow = Shadows.shadowOf(activity)
        val intent = shadow.nextStartedActivity

        intent.shouldBeNull()
    }

    @Test
    fun `should navigate to HomeActivity when not authenticated - web-link`() {
        fakeAuthRepository.userDelegate = { Result.success(null) }

        val uri = "https://sabbath-school.adventech.io/en/2021-03".toUri()

        navigator.navigate(activity, uri)

        val shadow = Shadows.shadowOf(activity)
        val intent = shadow.nextStartedActivity

        intent.shouldNotBeNull()
        intent.component?.className shouldBeEqualTo HomeActivity::class.java.name
    }

    @Test
    fun `should navigate to HomeActivity with resource index - web-link`() {
        val uri = "https://sabbath-school.adventech.io/en/2021-03".toUri()
        navigator.navigate(activity, uri)

        val shadow = Shadows.shadowOf(activity)
        val intent = shadow.nextStartedActivity

        intent.shouldNotBeNull()
        intent.component?.className shouldBeEqualTo HomeActivity::class.java.name
        intent.getStringExtra(AppNavigatorImpl.EXTRA_RESOURCE_INDEX) shouldBeEqualTo "en/ss/2021-03"
    }

    @Test
    fun `should navigate to HomeActivity with resource index - aij - web-link`() {
        val uri = "https://sabbath-school.adventech.io/resources/en/aij/2025-00-bb-pb".toUri()
        navigator.navigate(activity, uri)

        val shadow = Shadows.shadowOf(activity)
        val intent = shadow.nextStartedActivity

        intent.shouldNotBeNull()
        intent.component?.className shouldBeEqualTo HomeActivity::class.java.name
        intent.getStringExtra(AppNavigatorImpl.EXTRA_RESOURCE_INDEX) shouldBeEqualTo "en/aij/2025-00-bb-pb"
    }

    @Test
    fun `should navigate to HomeActivity with document index - web-link`() {
        val uri = "https://sabbath-school.adventech.io/en/2021-03/03/07-friday-further-thought/".toUri()
        navigator.navigate(activity, uri)

        val shadow = Shadows.shadowOf(activity)
        val intent = shadow.nextStartedActivity

        intent.shouldNotBeNull()
        intent.component?.className shouldBeEqualTo HomeActivity::class.java.name
        intent.getStringExtra(AppNavigatorImpl.EXTRA_RESOURCE_INDEX) shouldBeEqualTo "en/ss/2021-03"
        intent.getStringExtra(AppNavigatorImpl.EXTRA_DOCUMENT_INDEX) shouldBeEqualTo "en/ss/2021-03/03"
    }

    @Test
    fun `should launch normal flow for invalid web-link`() {
        val uri = "https://sabbath-school.adventech.io/03/07-friday-further-thought/".toUri()
        navigator.navigate(activity, uri)

        val shadow = Shadows.shadowOf(activity)
        val intent = shadow.nextStartedActivity

        intent.shouldNotBeNull()
        intent.component?.className shouldBeEqualTo HomeActivity::class.java.name
    }
}

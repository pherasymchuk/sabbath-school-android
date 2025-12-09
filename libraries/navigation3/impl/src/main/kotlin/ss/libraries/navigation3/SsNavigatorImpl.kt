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

package ss.libraries.navigation3

import android.content.Intent
import androidx.activity.ComponentActivity
import androidx.navigation3.runtime.NavBackStack
import androidx.navigation3.runtime.NavKey
import com.cryart.sabbathschool.core.extensions.context.launchWebUrl
import com.cryart.sabbathschool.core.navigation.AppNavigator

/**
 * Implementation of [SsNavigator] that wraps a Navigation 3 back stack.
 * Handles special navigation cases like custom tabs and legacy destinations.
 */
class SsNavigatorImpl(
    private val backStack: NavBackStack<NavKey>,
    private val activity: ComponentActivity,
    private val appNavigator: AppNavigator,
) : SsNavigator {

    override fun goTo(key: NavKey) {
        when (key) {
            is CustomTabsKey -> activity.launchWebUrl(key.url)
            is LegacyDestinationKey -> appNavigator.navigate(activity, key.destination, null)
            else -> backStack.add(key)
        }
    }

    override fun pop(): Boolean {
        return backStack.removeLastOrNull() != null
    }

    override fun resetRoot(key: NavKey) {
        backStack.clear()
        backStack.add(key)
    }
    
    override fun launchIntent(intent: Intent) {
        activity.startActivity(intent)
    }
}

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

package ss.libraries.navigation3.test

import android.content.Intent
import androidx.annotation.VisibleForTesting
import androidx.navigation3.runtime.NavKey
import ss.libraries.navigation3.SsNavigator

/** Fake implementation of [SsNavigator] for use in tests. */
@VisibleForTesting(otherwise = VisibleForTesting.NONE)
class FakeSsNavigator : SsNavigator {

    private val backStack = mutableListOf<NavKey>()

    var lastNavigatedKey: NavKey? = null
        private set

    var resetRootKey: NavKey? = null
        private set

    var popCalled: Boolean = false
        private set

    var lastIntent: Intent? = null
        private set

    override fun goTo(key: NavKey) {
        lastNavigatedKey = key
        backStack.add(key)
    }

    override fun pop(): Boolean {
        popCalled = true
        return if (backStack.isNotEmpty()) {
            backStack.removeAt(backStack.lastIndex)
            true
        } else {
            false
        }
    }

    override fun resetRoot(key: NavKey) {
        resetRootKey = key
        backStack.clear()
        backStack.add(key)
    }

    override fun launchIntent(intent: Intent) {
        lastIntent = intent
    }

    /** Resets all tracking state. */
    fun reset() {
        backStack.clear()
        lastNavigatedKey = null
        resetRootKey = null
        popCalled = false
        lastIntent = null
    }
}


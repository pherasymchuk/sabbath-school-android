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

import androidx.activity.ComponentActivity
import androidx.compose.animation.slideInHorizontally
import androidx.compose.animation.slideOutHorizontally
import androidx.compose.animation.togetherWith
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.remember
import androidx.compose.runtime.saveable.rememberSerializable
import androidx.compose.ui.Modifier
import androidx.lifecycle.viewmodel.navigation3.rememberViewModelStoreNavEntryDecorator
import androidx.navigation3.runtime.NavBackStack
import androidx.navigation3.runtime.NavKey
import androidx.navigation3.runtime.entryProvider
import androidx.navigation3.runtime.rememberSaveableStateHolderNavEntryDecorator
import androidx.navigation3.runtime.serialization.NavBackStackSerializer
import androidx.navigation3.runtime.serialization.NavKeySerializer
import androidx.navigation3.ui.NavDisplay
import com.cryart.sabbathschool.core.navigation.AppNavigator

/**
 * Creates and remembers an [SsNavigator] instance with a serializable back stack.
 *
 * @param startKey The initial navigation key to start with.
 * @param activity The component activity for handling special navigation.
 * @param appNavigator The app navigator for legacy destinations.
 * @return A pair of the navigator and the back stack for use with [SsNavHost].
 */
@Composable
fun rememberSsNavigator(
    startKey: NavKey,
    activity: ComponentActivity,
    appNavigator: AppNavigator,
): SsNavigatorState {
    val backStack = rememberSerializable(
        serializer = NavBackStackSerializer(elementSerializer = NavKeySerializer())
    ) {
        NavBackStack(startKey)
    }
    val navigator = remember(backStack, activity, appNavigator) {
        SsNavigatorImpl(backStack, activity, appNavigator)
    }
    return SsNavigatorState(navigator, backStack)
}

/**
 * State holder for navigation containing the navigator and back stack.
 */
data class SsNavigatorState(
    val navigator: SsNavigator,
    val backStack: NavBackStack<NavKey>,
)

/**
 * The main navigation host composable that displays content based on the back stack.
 *
 * @param navigatorState The navigation state from [rememberSsNavigator].
 * @param entryBuilders Set of entry provider builders from feature modules.
 * @param modifier Modifier for the NavDisplay.
 */
@Composable
fun SsNavHost(
    navigatorState: SsNavigatorState,
    entryBuilders: Set<EntryProviderBuilder>,
    modifier: Modifier = Modifier,
) {
    CompositionLocalProvider(LocalSsNavigator provides navigatorState.navigator) {
        NavDisplay(
            backStack = navigatorState.backStack,
            onBack = { navigatorState.navigator.pop() },
            modifier = modifier,
            entryDecorators = listOf(
                rememberSaveableStateHolderNavEntryDecorator(),
                rememberViewModelStoreNavEntryDecorator(),
            ),
            transitionSpec = {
                slideInHorizontally(initialOffsetX = { it }) togetherWith
                    slideOutHorizontally(targetOffsetX = { -it })
            },
            popTransitionSpec = {
                slideInHorizontally(initialOffsetX = { -it }) togetherWith
                    slideOutHorizontally(targetOffsetX = { it })
            },
            predictivePopTransitionSpec = {
                slideInHorizontally(initialOffsetX = { -it }) togetherWith
                    slideOutHorizontally(targetOffsetX = { it })
            },
            entryProvider = entryProvider {
                entryBuilders.forEach { builder -> this.builder() }
            },
        )
    }
}

/*
 * Copyright (c) 2023. Adventech <info@adventech.io>
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

package app.ss.tv.presentation

import androidx.compose.runtime.Composable
import app.ss.tv.presentation.home.HomeScreen
import app.ss.tv.presentation.theme.SSTvTheme
import com.slack.circuit.backstack.rememberSaveableBackStack
import com.slack.circuit.foundation.CircuitConfig
import com.slack.circuit.foundation.NavigableCircuitContent
import com.slack.circuit.foundation.push
import com.slack.circuit.foundation.rememberCircuitNavigator

/**
 * Entry-point for the Sabbath School TV app.
 */
@Composable
fun TvApp(circuitConfig: CircuitConfig) {
    SSTvTheme {
        val backstack = rememberSaveableBackStack { push(HomeScreen) }
        val navigator = rememberCircuitNavigator(backstack)
        NavigableCircuitContent(navigator, backstack, circuitConfig = circuitConfig)
    }
}
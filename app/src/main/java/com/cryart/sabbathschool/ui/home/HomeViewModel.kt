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

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import app.ss.auth.AuthRepository
import com.cryart.sabbathschool.reminder.DailyReminderManager
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import ss.libraries.appwidget.api.AppWidgetHelper
import ss.libraries.navigation3.HomeNavKey
import ss.libraries.navigation3.LoginKey
import ss.prefs.api.SSPrefs
import javax.inject.Inject

/** Initial startup state - determines whether user needs to log in or can proceed to home. */
sealed interface StartupState {
    data object Loading : StartupState
    data class Ready(val startKey: androidx.navigation3.runtime.NavKey) : StartupState
}

@HiltViewModel
class HomeViewModel @Inject constructor(
    private val ssPrefs: SSPrefs,
    private val authRepository: AuthRepository,
    private val dailyReminderManager: DailyReminderManager,
    private val appWidgetHelper: AppWidgetHelper,
) : ViewModel() {

    private val _startupState = MutableStateFlow<StartupState>(StartupState.Loading)
    val startupState: StateFlow<StartupState> = _startupState.asStateFlow()

    init {
        determineStartScreen()
        refreshAppWidget()
    }

    private fun determineStartScreen() {
        viewModelScope.launch {
            val user = authRepository.getUser().getOrNull()

            if (user != null && ssPrefs.reminderEnabled() && ssPrefs.isReminderScheduled().not()) {
                dailyReminderManager.scheduleReminder()
            }

            val startKey: androidx.navigation3.runtime.NavKey = when {
                user == null -> LoginKey
                else -> HomeNavKey
            }

            _startupState.value = StartupState.Ready(startKey)
        }
    }

    private fun refreshAppWidget() {
        viewModelScope.launch {
            appWidgetHelper.refreshAll()
        }
    }
}

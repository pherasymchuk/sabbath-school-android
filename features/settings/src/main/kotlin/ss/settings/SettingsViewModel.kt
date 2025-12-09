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

import android.content.Context
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import app.ss.design.compose.extensions.list.ListEntity
import dagger.hilt.android.lifecycle.HiltViewModel
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.collections.immutable.toImmutableList
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharedFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asSharedFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import ss.settings.repository.SettingsEntity
import ss.settings.repository.SettingsRepository
import javax.inject.Inject

sealed interface SettingsNavEvent {
    data class OpenUrl(val url: String) : SettingsNavEvent
    data object NavigateToLogin : SettingsNavEvent
    data object NavigateBack : SettingsNavEvent
}

@HiltViewModel
class SettingsViewModel @Inject constructor(
    @param:ApplicationContext private val context: Context,
    private val repository: SettingsRepository,
) : ViewModel() {

    private val _overlay = MutableStateFlow<Overlay?>(null)
    val overlay: StateFlow<Overlay?> = _overlay.asStateFlow()

    private val _navEvents = MutableSharedFlow<SettingsNavEvent>()
    val navEvents: SharedFlow<SettingsNavEvent> = _navEvents.asSharedFlow()

    // Trigger for switch changes to recompose entities
    private val switchTrigger = MutableStateFlow(0)

    val entities: StateFlow<List<ListEntity>> = combine(
        repository.entitiesFlow { entity -> handleEntityClick(entity) },
        switchTrigger
    ) { entities, _ -> entities }
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5_000), emptyList())

    private fun handleEntityClick(entity: SettingsEntity) {
        when (entity) {
            SettingsEntity.Account.Delete -> {
                _overlay.update { Overlay.ConfirmDeleteAccount }
            }

            SettingsEntity.Account.SignOut -> {
                repository.signOut()
                viewModelScope.launch {
                    _navEvents.emit(SettingsNavEvent.NavigateToLogin)
                }
            }

            is SettingsEntity.Reminder.Switch -> {
                switchTrigger.update { it + 1 }
            }

            is SettingsEntity.Reminder.Time -> {
                _overlay.update { Overlay.SelectReminderTime(entity.hour, entity.minute) }
            }

            is SettingsEntity.About -> {
                val url = context.getString(entity.resId)
                viewModelScope.launch {
                    _navEvents.emit(SettingsNavEvent.OpenUrl(url))
                }
            }

            SettingsEntity.Account.DeleteContent -> {
                _overlay.update { Overlay.ConfirmRemoveDownloads }
            }
        }
    }

    fun navigateBack() {
        viewModelScope.launch {
            _navEvents.emit(SettingsNavEvent.NavigateBack)
        }
    }

    fun dismissOverlay() {
        _overlay.update { null }
    }

    fun confirmDeleteAccount() {
        _overlay.update { null }
        repository.deleteAccount()
        viewModelScope.launch {
            _navEvents.emit(SettingsNavEvent.NavigateToLogin)
        }
    }

    fun setReminderTime(hour: Int, minute: Int) {
        repository.setReminderTime(hour, minute)
        _overlay.update { null }
    }

    fun confirmRemoveDownloads() {
        repository.removeAllDownloads()
        _overlay.update { null }
    }
}

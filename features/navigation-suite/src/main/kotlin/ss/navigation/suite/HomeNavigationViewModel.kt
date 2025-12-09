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

package ss.navigation.suite

import androidx.annotation.DrawableRes
import androidx.annotation.StringRes
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.collections.immutable.ImmutableList
import kotlinx.collections.immutable.persistentListOf
import kotlinx.collections.immutable.toImmutableList
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.flow.flatMapLatest
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.stateIn
import ss.feed.FeedType
import ss.prefs.api.SSPrefs
import ss.resources.api.ResourcesRepository
import ss.resources.model.LanguageModel
import timber.log.Timber
import javax.inject.Inject
import app.ss.translations.R as L10nR

@HiltViewModel
class HomeNavigationViewModel @Inject constructor(
    private val resourcesRepository: ResourcesRepository,
    private val ssPrefs: SSPrefs,
) : ViewModel() {

    private val _selectedItem = MutableStateFlow<NavbarItemNav3?>(null)
    val selectedItem: StateFlow<NavbarItemNav3?> = _selectedItem

    @OptIn(ExperimentalCoroutinesApi::class)
    val navigationItems: StateFlow<ImmutableList<NavbarItemNav3>?> = ssPrefs.getLanguageCodeFlow()
        .flatMapLatest { language ->
            resourcesRepository.language(language)
                .map { it.toNavbarItems() }
                .catch {
                    Timber.e(it, "Failed to get Navbar items for language: $language")
                    emit(persistentListOf())
                }
        }
        .stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5_000),
            initialValue = null
        )

    fun selectItem(item: NavbarItemNav3) {
        _selectedItem.value = item
    }

    private fun LanguageModel.toNavbarItems(): ImmutableList<NavbarItemNav3> {
        if (!aij && !pm && !devo && !explore) {
            return persistentListOf()
        }
        return buildList {
            add(NavbarItemNav3.SabbathSchool)
            if (aij) add(NavbarItemNav3.AliveInJesus)
            if (pm) add(NavbarItemNav3.PersonalMinistries)
            if (devo) add(NavbarItemNav3.Devotionals)
            if (explore) add(NavbarItemNav3.Explore)
        }.toImmutableList()
    }
}

/** Navigation 3 version of NavbarItem that uses FeedType instead of Circuit Screen. */
enum class NavbarItemNav3(@param:DrawableRes val iconRes: Int, @param:StringRes val title: Int) {
    SabbathSchool(R.drawable.ss_ic_sabbath_school, L10nR.string.ss_app_name),
    AliveInJesus(R.drawable.ss_ic_aij, L10nR.string.ss_alive_in_jesus),
    PersonalMinistries(R.drawable.ss_ic_pm, L10nR.string.ss_personal_ministries),
    Devotionals(R.drawable.ss_ic_devotion, L10nR.string.ss_devotionals),
    Explore(R.drawable.ss_ic_explore, L10nR.string.ss_explore),
}

fun NavbarItemNav3.toFeedType(): FeedType = when (this) {
    NavbarItemNav3.SabbathSchool -> FeedType.SABBATH_SCHOOL
    NavbarItemNav3.AliveInJesus -> FeedType.ALIVE_IN_JESUS
    NavbarItemNav3.PersonalMinistries -> FeedType.PERSONAL_MINISTRIES
    NavbarItemNav3.Devotionals -> FeedType.DEVOTIONALS
    NavbarItemNav3.Explore -> FeedType.EXPLORE
}

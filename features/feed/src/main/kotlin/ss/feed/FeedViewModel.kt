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

package ss.feed

import android.content.Context
import android.content.Intent
import androidx.compose.runtime.Immutable
import androidx.compose.runtime.Stable
import androidx.core.app.ShareCompat
import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import app.ss.auth.AuthRepository
import com.cryart.sabbathschool.core.navigation.Destination
import dagger.hilt.android.lifecycle.HiltViewModel
import io.adventech.blockkit.model.feed.FeedGroup
import io.adventech.blockkit.model.feed.FeedType
import kotlinx.collections.immutable.ImmutableList
import kotlinx.collections.immutable.persistentListOf
import kotlinx.collections.immutable.toImmutableList
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import ss.feed.model.FeedResourceSpec
import ss.feed.model.toSpec
import ss.libraries.navigation3.CustomTabsKey
import ss.libraries.navigation3.FeedGroupKey
import ss.libraries.navigation3.FeedKey
import ss.libraries.navigation3.LanguagesKey
import ss.libraries.navigation3.LegacyDestinationKey
import ss.libraries.navigation3.LoginKey
import ss.libraries.navigation3.ResourceKey
import ss.libraries.navigation3.SettingsKey
import ss.libraries.navigation3.SsNavigator
import ss.misc.SSConstants
import ss.resources.api.ResourcesRepository
import ss.services.auth.overlay.UserInfo
import javax.inject.Inject
import app.ss.translations.R as L10nR

@HiltViewModel
class FeedViewModel @Inject constructor(
    savedStateHandle: SavedStateHandle,
    private val authRepository: AuthRepository,
    private val resourcesRepository: ResourcesRepository,
) : ViewModel() {

    private val feedType: FeedType = savedStateHandle.get<String>("feedType")
        ?.let { FeedType.valueOf(it) }
        ?: FeedType.SS

    private val _uiState = MutableStateFlow<FeedUiState>(FeedUiState.Loading("", null))
    val uiState: StateFlow<FeedUiState> = _uiState.asStateFlow()

    private val _overlayState = MutableStateFlow<FeedOverlayState?>(null)
    val overlayState: StateFlow<FeedOverlayState?> = _overlayState.asStateFlow()

    private var navigator: SsNavigator? = null
    private var userInfo: UserInfo? = null

    init {
        loadUserInfo()
        loadFeed()
    }

    fun setNavigator(navigator: SsNavigator) {
        this.navigator = navigator
    }

    private fun loadUserInfo() {
        viewModelScope.launch {
            authRepository.getUser().getOrNull()?.let { user ->
                userInfo = UserInfo(user.displayName, user.email, user.photo)
                _uiState.update { state ->
                    when (state) {
                        is FeedUiState.Loading -> state.copy(photoUrl = userInfo?.photo)
                        is FeedUiState.Group -> state.copy(photoUrl = userInfo?.photo)
                        is FeedUiState.ResourceList -> state.copy(photoUrl = userInfo?.photo)
                    }
                }
            }
        }
    }

    private fun loadFeed() {
        viewModelScope.launch {
            resourcesRepository.feed(feedType).collect { feedModel ->
                _uiState.value = when {
                    feedModel.groups.size == 1 -> FeedUiState.ResourceList(
                        photoUrl = userInfo?.photo,
                        title = feedModel.title,
                        resources = feedModel.groups.first().toSpec(),
                    )
                    else -> FeedUiState.Group(
                        photoUrl = userInfo?.photo,
                        title = feedModel.title,
                        groups = feedModel.groups.toImmutableList(),
                    )
                }
            }
        }
    }

    fun onProfileClick() {
        userInfo?.let { info ->
            _overlayState.value = FeedOverlayState.AccountInfo(info)
        }
    }

    fun onFilterLanguagesClick() {
        navigator?.goTo(LanguagesKey)
    }

    fun onItemClick(index: String) {
        navigator?.goTo(ResourceKey(index))
    }

    fun onSeeAllClick(group: FeedGroup) {
        navigator?.goTo(
            FeedGroupKey(
                id = group.id,
                title = group.title,
                feedType = feedType
            )
        )
    }

    fun onOverlayDismiss() {
        _overlayState.value = null
    }

    fun onOverlayResult(result: OverlayResult, context: Context) {
        _overlayState.value = null
        when (result) {
            OverlayResult.Dismiss -> Unit
            OverlayResult.GoToAbout -> navigator?.goTo(LegacyDestinationKey(Destination.ABOUT))
            OverlayResult.GoToSettings -> navigator?.goTo(SettingsKey)
            is OverlayResult.ShareApp -> shareApp(context)
            OverlayResult.SignOut -> signOut()
            is OverlayResult.GoToPrivacyPolicy -> {
                navigator?.goTo(CustomTabsKey(context.getString(L10nR.string.ss_privacy_policy_url)))
            }
        }
    }

    private fun shareApp(context: Context) {
        val shareIntent = ShareCompat.IntentBuilder(context)
            .setType("text/plain")
            .setText(context.getString(L10nR.string.ss_menu_share_app_text, SSConstants.SS_APP_PLAY_STORE_LINK))
            .intent
        if (shareIntent.resolveActivity(context.packageManager) != null) {
            context.startActivity(Intent.createChooser(shareIntent, context.getString(L10nR.string.ss_menu_share_app)))
        }
    }

    private fun signOut() {
        viewModelScope.launch {
            authRepository.logout()
            navigator?.resetRoot(LoginKey)
        }
    }

    private fun FeedGroup?.toSpec(): ImmutableList<FeedResourceSpec> =
        this?.resources?.map {
            it.toSpec(this, FeedResourceSpec.ContentDirection.HORIZONTAL)
        }?.toImmutableList() ?: persistentListOf()

    sealed interface OverlayResult {
        data object Dismiss : OverlayResult
        data object GoToAbout : OverlayResult
        data object GoToSettings : OverlayResult
        data class ShareApp(val context: Context) : OverlayResult
        data object SignOut : OverlayResult
        data class GoToPrivacyPolicy(val context: Context) : OverlayResult
    }
}

sealed interface FeedUiState {
    val photoUrl: String?
    val title: String

    data class Loading(
        override val title: String,
        override val photoUrl: String?,
    ) : FeedUiState

    data class Group(
        override val photoUrl: String?,
        override val title: String,
        val groups: ImmutableList<FeedGroup>,
    ) : FeedUiState

    data class ResourceList(
        override val photoUrl: String?,
        override val title: String,
        val resources: ImmutableList<FeedResourceSpec>,
    ) : FeedUiState
}

@Stable
sealed interface FeedOverlayState {
    @Immutable
    data class AccountInfo(val userInfo: UserInfo) : FeedOverlayState
}

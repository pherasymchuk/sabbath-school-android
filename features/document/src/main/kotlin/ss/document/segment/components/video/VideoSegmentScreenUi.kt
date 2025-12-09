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

package ss.document.segment.components.video

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.systemBars
import androidx.compose.foundation.layout.windowInsetsBottomHeight
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.Text
import androidx.compose.material3.rememberModalBottomSheetState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import app.ss.design.compose.extensions.haptics.LocalSsHapticFeedback
import app.ss.design.compose.theme.Dimens
import app.ss.design.compose.widget.scaffold.HazeScaffold
import io.adventech.blockkit.model.BlockItem
import io.adventech.blockkit.ui.BlockContent
import io.adventech.blockkit.ui.VideoContent
import io.adventech.blockkit.ui.input.UserInputState
import io.adventech.blockkit.ui.style.LocalReaderStyle
import io.adventech.blockkit.ui.style.Styler
import io.adventech.blockkit.ui.style.background
import io.adventech.blockkit.ui.style.primaryForeground
import kotlinx.collections.immutable.ImmutableList
import kotlinx.collections.immutable.persistentListOf
import kotlinx.collections.immutable.persistentMapOf
import ss.document.NavEvent
import ss.document.components.DocumentTopAppBar
import ss.document.components.DocumentTopAppBarAction
import ss.document.producer.UserInputStateProducer
import ss.libraries.navigation3.LocalSsNavigator

/**
 * Composable wrapper that sets up the VideoSegment screen with ViewModel.
 */
@Suppress("DEPRECATION")
@Composable
internal fun VideoSegmentContent(
    id: String,
    index: String,
    documentId: String,
    modifier: Modifier = Modifier,
    userInputStateProducer: UserInputStateProducer? = null,
    onNavEvent: (NavEvent) -> Unit = {},
    viewModel: VideoSegmentViewModel = hiltViewModel(
        creationCallback = { factory: VideoSegmentViewModel.Factory ->
            factory.create(id, index, documentId)
        }
    ),
) {
    val navigator = LocalSsNavigator.current
    val state by viewModel.uiState.collectAsStateWithLifecycle()
    val userInputState = userInputStateProducer?.invoke(documentId) ?: UserInputState(
        input = persistentListOf(),
        bibleVersion = null,
        collapseContent = persistentMapOf(),
        eventSink = {}
    )

    LaunchedEffect(viewModel) {
        viewModel.navEvents.collect { event ->
            when (event) {
                is VideoSegmentNavEvent.Pop -> onNavEvent(NavEvent.Pop)
                is VideoSegmentNavEvent.LaunchIntent -> navigator.launchIntent(event.intent)
                is VideoSegmentNavEvent.GoTo -> onNavEvent(event.event)
            }
        }
    }

    VideoSegmentScreenUi(
        state = state,
        userInputState = userInputState,
        modifier = modifier,
        onNavBack = viewModel::onNavBack,
        onTopAppBarAction = viewModel::onTopAppBarAction,
        onDismissReaderOptions = viewModel::dismissReaderOptions,
    )
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
internal fun VideoSegmentScreenUi(
    state: VideoSegmentState,
    userInputState: UserInputState,
    modifier: Modifier = Modifier,
    onNavBack: () -> Unit = {},
    onTopAppBarAction: (DocumentTopAppBarAction) -> Unit = {},
    onDismissReaderOptions: () -> Unit = {},
) {
    val readerStyle = LocalReaderStyle.current
    val containerColor = readerStyle.theme.background()
    val contentColor = readerStyle.theme.primaryForeground()
    val hapticFeedback = LocalSsHapticFeedback.current

    HazeScaffold(
        modifier = modifier,
        topBar = {
            DocumentTopAppBar(
                title = {
                    Text(
                        text = state.title,
                        color = contentColor,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis,
                    )
                },
                collapsed = true,
                actions = persistentListOf(
                    DocumentTopAppBarAction.DisplayOptions,
                ),
                onNavBack = {
                    hapticFeedback.performClick()
                    onNavBack()
                },
                onActionClick = onTopAppBarAction
            )
        },
        blurTopBar = true,
        containerColor = containerColor,
        contentColor = contentColor,
    ) { contentPadding ->
        LazyColumn(
            modifier = Modifier
                .fillMaxWidth()
                .padding(top = 12.dp),
            contentPadding = contentPadding,
            verticalArrangement = Arrangement.spacedBy(20.dp),
        ) {
            items(state.videos) { video ->
                VideoContent(
                    blockItem = video,
                    modifier = Modifier
                        .padding(horizontal = Dimens.grid_4),
                )
            }

            item {
                Text(
                    text = state.title,
                    modifier = Modifier
                        .padding(horizontal = Dimens.grid_4),
                    style = Styler.textStyle(null).copy(
                        fontSize = 28.sp,
                        fontWeight = FontWeight.Bold,
                        color = contentColor,
                    ),
                    color = contentColor,
                )
            }

            items(state.blocks) { block ->
                BlockContent(
                    blockItem = block,
                    modifier = Modifier,
                    userInputState = userInputState,
                    onHandleUri = { _, _ -> },
                )
            }

            item {
                Spacer(Modifier.windowInsetsBottomHeight(WindowInsets.systemBars))
            }
        }
    }

    // Reader options bottom sheet
    if (state.showReaderOptions) {
        val sheetState = rememberModalBottomSheetState(skipPartiallyExpanded = false)
        ModalBottomSheet(
            onDismissRequest = onDismissReaderOptions,
            sheetState = sheetState,
        ) {
            ss.document.reader.ReaderOptionsScreen()
        }
    }
}

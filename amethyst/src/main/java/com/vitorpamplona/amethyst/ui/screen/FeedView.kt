/**
 * Copyright (c) 2024 Vitor Pamplona
 *
 * Permission is hereby granted, free of charge, to any person obtaining a copy of
 * this software and associated documentation files (the "Software"), to deal in
 * the Software without restriction, including without limitation the rights to use,
 * copy, modify, merge, publish, distribute, sublicense, and/or sell copies of the
 * Software, and to permit persons to whom the Software is furnished to do so,
 * subject to the following conditions:
 *
 * The above copyright notice and this permission notice shall be included in all
 * copies or substantial portions of the Software.
 *
 * THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR
 * IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY, FITNESS
 * FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE AUTHORS OR
 * COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER LIABILITY, WHETHER IN
 * AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM, OUT OF OR IN CONNECTION
 * WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN THE SOFTWARE.
 */
package com.vitorpamplona.amethyst.ui.screen

import androidx.compose.animation.core.tween
import androidx.compose.foundation.lazy.LazyListState
import androidx.compose.foundation.lazy.grid.LazyGridState
import androidx.compose.foundation.lazy.grid.rememberLazyGridState
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.vitorpamplona.amethyst.ui.actions.CrossfadeIfEnabled
import com.vitorpamplona.amethyst.ui.feeds.FeedEmpty
import com.vitorpamplona.amethyst.ui.feeds.FeedError
import com.vitorpamplona.amethyst.ui.feeds.FeedState
import com.vitorpamplona.amethyst.ui.feeds.LoadingFeed
import com.vitorpamplona.amethyst.ui.feeds.RefresheableBox
import com.vitorpamplona.amethyst.ui.feeds.rememberForeverLazyGridState
import com.vitorpamplona.amethyst.ui.feeds.rememberForeverLazyListState
import com.vitorpamplona.amethyst.ui.screen.loggedIn.AccountViewModel

@Composable
fun RefresheableFeedView(
    viewModel: FeedViewModel,
    routeForLastRead: String?,
    enablePullRefresh: Boolean = true,
    scrollStateKey: String? = null,
    accountViewModel: AccountViewModel,
    nav: (String) -> Unit,
) {
    RefresheableBox(viewModel, enablePullRefresh) {
        SaveableFeedState(viewModel, scrollStateKey) { listState ->
            RenderFeedState(viewModel, accountViewModel, listState, nav, routeForLastRead)
        }
    }
}

@Composable
fun SaveableFeedState(
    viewModel: FeedViewModel,
    scrollStateKey: String? = null,
    content: @Composable (LazyListState) -> Unit,
) {
    val listState =
        if (scrollStateKey != null) {
            rememberForeverLazyListState(scrollStateKey)
        } else {
            rememberLazyListState()
        }

    WatchScrollToTop(viewModel, listState)

    content(listState)
}

@Composable
fun SaveableGridFeedState(
    viewModel: FeedViewModel,
    scrollStateKey: String? = null,
    content: @Composable (LazyGridState) -> Unit,
) {
    val gridState =
        if (scrollStateKey != null) {
            rememberForeverLazyGridState(scrollStateKey)
        } else {
            rememberLazyGridState()
        }

    WatchScrollToTop(viewModel, gridState)

    content(gridState)
}

@Composable
fun RenderFeedState(
    viewModel: FeedViewModel,
    accountViewModel: AccountViewModel,
    listState: LazyListState,
    nav: (String) -> Unit,
    routeForLastRead: String?,
    onLoaded: @Composable (FeedState.Loaded) -> Unit = {
        com.vitorpamplona.amethyst.ui.feeds
            .FeedLoaded(it, listState, routeForLastRead, accountViewModel, nav)
    },
    onEmpty: @Composable () -> Unit = { FeedEmpty { viewModel.invalidateData() } },
    onError: @Composable (String) -> Unit = { FeedError(it) { viewModel.invalidateData() } },
    onLoading: @Composable () -> Unit = { LoadingFeed() },
) {
    val feedState by viewModel.feedContent.collectAsStateWithLifecycle()

    CrossfadeIfEnabled(
        targetState = feedState,
        animationSpec = tween(durationMillis = 100),
        accountViewModel = accountViewModel,
    ) { state ->
        when (state) {
            is FeedState.Empty -> onEmpty()
            is FeedState.FeedError -> onError(state.errorMessage)
            is FeedState.Loaded -> onLoaded(state)
            is FeedState.Loading -> onLoading()
        }
    }
}

@Composable
private fun WatchScrollToTop(
    viewModel: FeedViewModel,
    listState: LazyListState,
) {
    val scrollToTop by viewModel.scrollToTop.collectAsStateWithLifecycle()

    LaunchedEffect(scrollToTop) {
        if (scrollToTop > 0 && viewModel.scrolltoTopPending) {
            listState.scrollToItem(index = 0)
            viewModel.sentToTop()
        }
    }
}

@Composable
private fun WatchScrollToTop(
    viewModel: FeedViewModel,
    listState: LazyGridState,
) {
    val scrollToTop by viewModel.scrollToTop.collectAsStateWithLifecycle()

    LaunchedEffect(scrollToTop) {
        if (scrollToTop > 0 && viewModel.scrolltoTopPending) {
            listState.scrollToItem(index = 0)
            viewModel.sentToTop()
        }
    }
}

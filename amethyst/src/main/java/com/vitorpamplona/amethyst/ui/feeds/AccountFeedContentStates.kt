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
package com.vitorpamplona.amethyst.ui.feeds

import androidx.lifecycle.viewModelScope
import com.vitorpamplona.amethyst.ui.dal.ChatroomListKnownFeedFilter
import com.vitorpamplona.amethyst.ui.dal.ChatroomListNewFeedFilter
import com.vitorpamplona.amethyst.ui.dal.DiscoverChatFeedFilter
import com.vitorpamplona.amethyst.ui.dal.DiscoverCommunityFeedFilter
import com.vitorpamplona.amethyst.ui.dal.DiscoverLiveFeedFilter
import com.vitorpamplona.amethyst.ui.dal.DiscoverMarketplaceFeedFilter
import com.vitorpamplona.amethyst.ui.dal.DiscoverNIP89FeedFilter
import com.vitorpamplona.amethyst.ui.dal.HomeConversationsFeedFilter
import com.vitorpamplona.amethyst.ui.dal.HomeNewThreadFeedFilter
import com.vitorpamplona.amethyst.ui.dal.VideoFeedFilter
import com.vitorpamplona.amethyst.ui.screen.loggedIn.AccountViewModel

class AccountFeedContentStates(
    val accountViewModel: AccountViewModel,
) {
    val homeNewThreads = FeedContentState(HomeNewThreadFeedFilter(accountViewModel.account), accountViewModel.viewModelScope)
    val homeReplies = FeedContentState(HomeConversationsFeedFilter(accountViewModel.account), accountViewModel.viewModelScope)

    val dmKnown = FeedContentState(ChatroomListKnownFeedFilter(accountViewModel.account), accountViewModel.viewModelScope)
    val dmNew = FeedContentState(ChatroomListNewFeedFilter(accountViewModel.account), accountViewModel.viewModelScope)

    val videoFeed = FeedContentState(VideoFeedFilter(accountViewModel.account), accountViewModel.viewModelScope)

    val discoverMarketplace = FeedContentState(DiscoverMarketplaceFeedFilter(accountViewModel.account), accountViewModel.viewModelScope)
    val discoverDVMs = FeedContentState(DiscoverNIP89FeedFilter(accountViewModel.account), accountViewModel.viewModelScope)
    val discoverLive = FeedContentState(DiscoverLiveFeedFilter(accountViewModel.account), accountViewModel.viewModelScope)
    val discoverCommunities = FeedContentState(DiscoverCommunityFeedFilter(accountViewModel.account), accountViewModel.viewModelScope)
    val discoverPublicChats = FeedContentState(DiscoverChatFeedFilter(accountViewModel.account), accountViewModel.viewModelScope)
}

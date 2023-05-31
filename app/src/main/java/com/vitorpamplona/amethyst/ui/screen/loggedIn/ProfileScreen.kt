package com.vitorpamplona.amethyst.ui.screen.loggedIn

import android.content.Context
import android.content.Intent
import android.net.Uri
import android.widget.Toast
import androidx.compose.foundation.*
import androidx.compose.foundation.gestures.scrollBy
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.pager.HorizontalPager
import androidx.compose.foundation.pager.rememberPagerState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.ClickableText
import androidx.compose.material.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Bolt
import androidx.compose.material.icons.filled.ContentCopy
import androidx.compose.material.icons.filled.EditNote
import androidx.compose.material.icons.filled.Link
import androidx.compose.material.icons.filled.MoreVert
import androidx.compose.runtime.*
import androidx.compose.runtime.livedata.observeAsState
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.input.nestedscroll.NestedScrollConnection
import androidx.compose.ui.input.nestedscroll.NestedScrollSource
import androidx.compose.ui.input.nestedscroll.nestedScroll
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.layout.onSizeChanged
import androidx.compose.ui.platform.LocalClipboardManager
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.platform.LocalLifecycleOwner
import androidx.compose.ui.platform.LocalUriHandler
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.AnnotatedString
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.IntSize
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.core.content.ContextCompat
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.LifecycleEventObserver
import androidx.lifecycle.viewmodel.compose.viewModel
import coil.compose.AsyncImage
import com.vitorpamplona.amethyst.R
import com.vitorpamplona.amethyst.model.Account
import com.vitorpamplona.amethyst.model.LocalCache
import com.vitorpamplona.amethyst.model.Note
import com.vitorpamplona.amethyst.model.User
import com.vitorpamplona.amethyst.service.NostrUserProfileDataSource
import com.vitorpamplona.amethyst.service.model.BadgeDefinitionEvent
import com.vitorpamplona.amethyst.service.model.BadgeProfilesEvent
import com.vitorpamplona.amethyst.service.model.IdentityClaim
import com.vitorpamplona.amethyst.service.model.PayInvoiceErrorResponse
import com.vitorpamplona.amethyst.service.model.PayInvoiceSuccessResponse
import com.vitorpamplona.amethyst.service.model.ReportEvent
import com.vitorpamplona.amethyst.ui.actions.NewUserMetadataView
import com.vitorpamplona.amethyst.ui.components.CreateTextWithEmoji
import com.vitorpamplona.amethyst.ui.components.DisplayNip05ProfileStatus
import com.vitorpamplona.amethyst.ui.components.InvoiceRequest
import com.vitorpamplona.amethyst.ui.components.ResizeImage
import com.vitorpamplona.amethyst.ui.components.RobohashAsyncImage
import com.vitorpamplona.amethyst.ui.components.RobohashFallbackAsyncImage
import com.vitorpamplona.amethyst.ui.components.TranslatableRichTextViewer
import com.vitorpamplona.amethyst.ui.components.ZoomableImageDialog
import com.vitorpamplona.amethyst.ui.components.figureOutMimeType
import com.vitorpamplona.amethyst.ui.dal.UserProfileBookmarksFeedFilter
import com.vitorpamplona.amethyst.ui.dal.UserProfileConversationsFeedFilter
import com.vitorpamplona.amethyst.ui.dal.UserProfileFollowersFeedFilter
import com.vitorpamplona.amethyst.ui.dal.UserProfileFollowsFeedFilter
import com.vitorpamplona.amethyst.ui.dal.UserProfileNewThreadFeedFilter
import com.vitorpamplona.amethyst.ui.dal.UserProfileReportsFeedFilter
import com.vitorpamplona.amethyst.ui.dal.UserProfileZapsFeedFilter
import com.vitorpamplona.amethyst.ui.navigation.ShowQRDialog
import com.vitorpamplona.amethyst.ui.note.UserPicture
import com.vitorpamplona.amethyst.ui.note.showAmount
import com.vitorpamplona.amethyst.ui.screen.LnZapFeedView
import com.vitorpamplona.amethyst.ui.screen.NostrUserProfileBookmarksFeedViewModel
import com.vitorpamplona.amethyst.ui.screen.NostrUserProfileConversationsFeedViewModel
import com.vitorpamplona.amethyst.ui.screen.NostrUserProfileFollowersUserFeedViewModel
import com.vitorpamplona.amethyst.ui.screen.NostrUserProfileFollowsUserFeedViewModel
import com.vitorpamplona.amethyst.ui.screen.NostrUserProfileNewThreadsFeedViewModel
import com.vitorpamplona.amethyst.ui.screen.NostrUserProfileReportFeedViewModel
import com.vitorpamplona.amethyst.ui.screen.NostrUserProfileZapsFeedViewModel
import com.vitorpamplona.amethyst.ui.screen.RefresheableView
import com.vitorpamplona.amethyst.ui.screen.RelayFeedView
import com.vitorpamplona.amethyst.ui.screen.RelayFeedViewModel
import com.vitorpamplona.amethyst.ui.screen.UserFeedView
import com.vitorpamplona.amethyst.ui.theme.BitcoinOrange
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import java.math.BigDecimal

@Composable
fun ProfileScreen(userId: String?, accountViewModel: AccountViewModel, nav: (String) -> Unit) {
    if (userId == null) return

    var userBase by remember { mutableStateOf<User?>(null) }

    LaunchedEffect(Unit) {
        withContext(Dispatchers.IO) {
            userBase = LocalCache.checkGetOrCreateUser(userId)
        }
    }

    userBase?.let {
        ProfileScreen(
            baseUser = it,
            accountViewModel = accountViewModel,
            nav = nav
        )
    }
}

@OptIn(ExperimentalFoundationApi::class)
@Composable
fun ProfileScreen(baseUser: User, accountViewModel: AccountViewModel, nav: (String) -> Unit) {
    UserProfileNewThreadFeedFilter.loadUserProfile(accountViewModel.account, baseUser)
    UserProfileConversationsFeedFilter.loadUserProfile(accountViewModel.account, baseUser)
    UserProfileFollowersFeedFilter.loadUserProfile(accountViewModel.account, baseUser)
    UserProfileFollowsFeedFilter.loadUserProfile(accountViewModel.account, baseUser)
    UserProfileZapsFeedFilter.loadUserProfile(baseUser)
    UserProfileReportsFeedFilter.loadUserProfile(baseUser)
    UserProfileBookmarksFeedFilter.loadUserProfile(accountViewModel.account, baseUser)

    NostrUserProfileDataSource.loadUserProfile(baseUser)

    val lifeCycleOwner = LocalLifecycleOwner.current

    LaunchedEffect(Unit) {
        NostrUserProfileDataSource.start()
    }

    DisposableEffect(accountViewModel) {
        val observer = LifecycleEventObserver { _, event ->
            if (event == Lifecycle.Event.ON_RESUME) {
                println("Profidle Start")
                NostrUserProfileDataSource.loadUserProfile(baseUser)
                NostrUserProfileDataSource.start()
            }
            if (event == Lifecycle.Event.ON_PAUSE) {
                println("Profile Stop")
                NostrUserProfileDataSource.loadUserProfile(null)
                NostrUserProfileDataSource.stop()
            }
        }

        lifeCycleOwner.lifecycle.addObserver(observer)
        onDispose {
            lifeCycleOwner.lifecycle.removeObserver(observer)
            println("Profile Dispose")
            NostrUserProfileDataSource.loadUserProfile(null)
            NostrUserProfileDataSource.stop()
        }
    }

    var columnSize by remember { mutableStateOf(IntSize.Zero) }
    var tabsSize by remember { mutableStateOf(IntSize.Zero) }

    Surface(
        modifier = Modifier.fillMaxWidth(),
        color = MaterialTheme.colors.background
    ) {
        val pagerState = rememberPagerState()
        val coroutineScope = rememberCoroutineScope()
        val scrollState = rememberScrollState()

        Column(
            modifier = Modifier
                .fillMaxSize()
                .onSizeChanged {
                    columnSize = it
                }
        ) {
            Box(
                modifier = Modifier
                    .verticalScroll(scrollState)
                    .nestedScroll(object : NestedScrollConnection {
                        override fun onPreScroll(
                            available: Offset,
                            source: NestedScrollSource
                        ): Offset {
                            // When scrolling vertically, scroll the container first.
                            return if (available.y < 0 && scrollState.canScrollForward) {
                                coroutineScope.launch {
                                    scrollState.scrollBy(-available.y)
                                }
                                Offset(0f, available.y)
                            } else {
                                Offset.Zero
                            }
                        }
                    })
                    .fillMaxHeight()
            ) {
                Column(modifier = Modifier.padding()) {
                    ProfileHeader(baseUser, nav, accountViewModel)
                    ScrollableTabRow(
                        backgroundColor = MaterialTheme.colors.background,
                        selectedTabIndex = pagerState.currentPage,
                        edgePadding = 8.dp,
                        modifier = Modifier.onSizeChanged {
                            tabsSize = it
                        }
                    ) {
                        val tabs = listOf<@Composable() (() -> Unit)?>(
                            { Text(text = stringResource(R.string.notes)) },
                            { Text(text = stringResource(R.string.replies)) },
                            { FollowTabHeader(baseUser) },
                            { FollowersTabHeader(baseUser) },
                            { ZapTabHeader(baseUser) },
                            { BookmarkTabHeader(baseUser) },
                            { ReportsTabHeader(baseUser) },
                            { RelaysTabHeader(baseUser) }
                        )

                        tabs.forEachIndexed { index, function ->
                            Tab(
                                selected = pagerState.currentPage == index,
                                onClick = {
                                    coroutineScope.launch { pagerState.animateScrollToPage(index) }
                                },
                                text = function
                            )
                        }
                    }
                    HorizontalPager(
                        pageCount = 8,
                        state = pagerState,
                        modifier = with(LocalDensity.current) {
                            Modifier.height((columnSize.height - tabsSize.height).toDp())
                        }
                    ) { page ->
                        when (page) {
                            0 -> TabNotesNewThreads(accountViewModel, nav)
                            1 -> TabNotesConversations(accountViewModel, nav)
                            2 -> TabFollows(baseUser, accountViewModel, nav)
                            3 -> TabFollowers(baseUser, accountViewModel, nav)
                            4 -> TabReceivedZaps(baseUser, accountViewModel, nav)
                            5 -> TabBookmarks(baseUser, accountViewModel, nav)
                            6 -> TabReports(baseUser, accountViewModel, nav)
                            7 -> TabRelays(baseUser, accountViewModel)
                        }
                    }
                }
            }
        }
    }
}

@Composable
private fun RelaysTabHeader(baseUser: User) {
    val userState by baseUser.live().relays.observeAsState()
    val userRelaysBeingUsed = remember(userState) { userState?.user?.relaysBeingUsed?.size ?: "--" }

    val userStateRelayInfo by baseUser.live().relayInfo.observeAsState()
    val userRelays = remember(userStateRelayInfo) { userStateRelayInfo?.user?.latestContactList?.relays()?.size ?: "--" }

    Text(text = "$userRelaysBeingUsed / $userRelays ${stringResource(R.string.relays)}")
}

@Composable
private fun ReportsTabHeader(baseUser: User) {
    val userState by baseUser.live().reports.observeAsState()
    var userReports by remember { mutableStateOf(0) }

    LaunchedEffect(key1 = userState) {
        UserProfileReportsFeedFilter.user = baseUser
        val newSize = UserProfileReportsFeedFilter.feed().size

        if (newSize != userReports) {
            userReports = newSize
        }
    }

    Text(text = "$userReports ${stringResource(R.string.reports)}")
}

@Composable
private fun BookmarkTabHeader(baseUser: User) {
    val userState by baseUser.live().bookmarks.observeAsState()
    val userBookmarks = remember(userState) {
        val bookmarkList = userState?.user?.latestBookmarkList
        (bookmarkList?.taggedEvents()?.count() ?: 0) + (
            bookmarkList?.taggedAddresses()?.count()
                ?: 0
            )
    }

    Text(text = "$userBookmarks ${stringResource(R.string.bookmarks)}")
}

@Composable
private fun ZapTabHeader(baseUser: User) {
    val userState by baseUser.live().zaps.observeAsState()
    var zapAmount by remember { mutableStateOf<BigDecimal?>(null) }

    LaunchedEffect(key1 = userState) {
        withContext(Dispatchers.IO) {
            val tempAmount = baseUser.zappedAmount()
            withContext(Dispatchers.Main) {
                zapAmount = tempAmount
            }
        }
    }

    Text(text = "${showAmount(zapAmount)} ${stringResource(id = R.string.zaps)}")
}

@Composable
private fun FollowersTabHeader(baseUser: User) {
    val userState by baseUser.live().follows.observeAsState()
    val userFollowers = remember(userState) { userState?.user?.transientFollowerCount() ?: "--" }

    Text(text = "$userFollowers ${stringResource(id = R.string.followers)}")
}

@Composable
private fun FollowTabHeader(baseUser: User) {
    val userState by baseUser.live().follows.observeAsState()
    val userFollows = remember(userState) { userState?.user?.transientFollowCount() ?: "--" }

    Text(text = "$userFollows ${stringResource(R.string.follows)}")
}

@Composable
private fun ProfileHeader(
    baseUser: User,
    nav: (String) -> Unit,
    accountViewModel: AccountViewModel
) {
    var popupExpanded by remember { mutableStateOf(false) }
    var zoomImageDialogOpen by remember { mutableStateOf(false) }

    val coroutineScope = rememberCoroutineScope()
    val clipboardManager = LocalClipboardManager.current

    Box {
        DrawBanner(baseUser)

        Box(
            modifier = Modifier
                .padding(horizontal = 10.dp)
                .size(40.dp)
                .align(Alignment.TopEnd)
        ) {
            Button(
                modifier = Modifier
                    .size(30.dp)
                    .align(Alignment.Center),
                onClick = { popupExpanded = true },
                shape = RoundedCornerShape(20.dp),
                colors = ButtonDefaults
                    .buttonColors(
                        backgroundColor = MaterialTheme.colors.background
                    ),
                contentPadding = PaddingValues(0.dp)
            ) {
                Icon(
                    tint = MaterialTheme.colors.onSurface.copy(alpha = 0.32f),
                    imageVector = Icons.Default.MoreVert,
                    contentDescription = stringResource(R.string.more_options)
                )

                UserProfileDropDownMenu(baseUser, popupExpanded, { popupExpanded = false }, accountViewModel)
            }
        }

        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 10.dp)
                .padding(top = 75.dp)
        ) {
            Row(
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.Bottom
            ) {
                UserPicture(
                    baseUser = baseUser,
                    baseUserAccount = accountViewModel.userProfile(),
                    size = 100.dp,
                    modifier = Modifier.border(
                        3.dp,
                        MaterialTheme.colors.background,
                        CircleShape
                    ),
                    onClick = {
                        if (baseUser.profilePicture() != null) {
                            zoomImageDialogOpen = true
                        }
                    },
                    onLongClick = {
                        ResizeImage(it.info?.picture, 100.dp).proxyUrl()?.let { it1 ->
                            clipboardManager.setText(
                                AnnotatedString(it1)
                            )
                        }
                    }
                )

                Spacer(Modifier.weight(1f))

                Row(
                    modifier = Modifier
                        .height(35.dp)
                        .padding(bottom = 3.dp)
                ) {
                    MessageButton(baseUser, nav)

                    // No need for this button anymore
                    // NPubCopyButton(baseUser)

                    ProfileActions(baseUser, accountViewModel, coroutineScope)
                }
            }

            DrawAdditionalInfo(baseUser, accountViewModel, nav)

            Divider(modifier = Modifier.padding(top = 6.dp))
        }
    }

    val profilePic = baseUser.profilePicture()
    if (zoomImageDialogOpen && profilePic != null) {
        ZoomableImageDialog(figureOutMimeType(profilePic), onDismiss = { zoomImageDialogOpen = false })
    }
}

@Composable
private fun ProfileActions(
    baseUser: User,
    accountViewModel: AccountViewModel,
    coroutineScope: CoroutineScope
) {
    val accountLocalUserState by accountViewModel.accountLiveData.observeAsState()
    val account = remember(accountLocalUserState) { accountLocalUserState?.account } ?: return

    val accountUserState by accountViewModel.account.userProfile().live().follows.observeAsState()

    val accountUser = remember(accountUserState) { accountUserState?.user } ?: return

    val isHidden by remember(accountUserState, accountLocalUserState) {
        derivedStateOf {
            account.isHidden(baseUser)
        }
    }

    val isLoggedInFollowingUser by remember(accountUserState, accountLocalUserState) {
        derivedStateOf {
            accountUser.isFollowingCached(baseUser)
        }
    }

    val isUserFollowingLoggedIn by remember(accountUserState, accountLocalUserState) {
        derivedStateOf {
            baseUser.isFollowingCached(accountUser)
        }
    }

    if (accountUser == baseUser) {
        EditButton(account)
    }

    if (isHidden) {
        ShowUserButton {
            account.showUser(baseUser.pubkeyHex)
        }
    } else if (isLoggedInFollowingUser) {
        UnfollowButton { coroutineScope.launch(Dispatchers.IO) { account.unfollow(baseUser) } }
    } else {
        if (isUserFollowingLoggedIn) {
            FollowButton(
                { coroutineScope.launch(Dispatchers.IO) { account.follow(baseUser) } },
                R.string.follow_back
            )
        } else {
            FollowButton(
                { coroutineScope.launch(Dispatchers.IO) { account.follow(baseUser) } },
                R.string.follow
            )
        }
    }
}

@Composable
private fun DrawAdditionalInfo(baseUser: User, accountViewModel: AccountViewModel, nav: (String) -> Unit) {
    val userState by baseUser.live().metadata.observeAsState()
    val user = remember(userState) { userState?.user } ?: return
    val tags = remember(userState) { userState?.user?.info?.latestMetadata?.tags }

    val uri = LocalUriHandler.current
    val clipboardManager = LocalClipboardManager.current
    val context = LocalContext.current
    val scope = rememberCoroutineScope()

    (user.bestDisplayName() ?: user.bestUsername())?.let {
        Row(verticalAlignment = Alignment.Bottom, modifier = Modifier.padding(top = 7.dp)) {
            CreateTextWithEmoji(
                text = it,
                tags = tags,
                fontWeight = FontWeight.Bold,
                fontSize = 25.sp
            )
        }
    }

    if (user.bestDisplayName() != null) {
        user.bestUsername()?.let {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                modifier = Modifier.padding(top = 1.dp, bottom = 1.dp)
            ) {
                CreateTextWithEmoji(
                    text = "@$it",
                    tags = tags,
                    color = MaterialTheme.colors.onSurface.copy(alpha = 0.32f)
                )
            }
        }
    }

    Row(verticalAlignment = Alignment.CenterVertically) {
        Text(
            text = user.pubkeyDisplayHex(),
            modifier = Modifier.padding(top = 1.dp, bottom = 1.dp),
            color = MaterialTheme.colors.onSurface.copy(alpha = 0.32f)
        )

        IconButton(
            modifier = Modifier
                .size(25.dp)
                .padding(start = 5.dp),
            onClick = { clipboardManager.setText(AnnotatedString(user.pubkeyNpub())); }
        ) {
            Icon(
                imageVector = Icons.Default.ContentCopy,
                null,
                modifier = Modifier.size(15.dp),
                tint = MaterialTheme.colors.onSurface.copy(alpha = 0.32f)
            )
        }

        var dialogOpen by remember {
            mutableStateOf(false)
        }

        if (dialogOpen) {
            ShowQRDialog(
                user,
                onScan = {
                    dialogOpen = false
                    nav(it)
                },
                onClose = { dialogOpen = false }
            )
        }

        IconButton(
            modifier = Modifier.size(25.dp),
            onClick = { dialogOpen = true }
        ) {
            Icon(
                painter = painterResource(R.drawable.ic_qrcode),
                null,
                modifier = Modifier.size(15.dp),
                tint = MaterialTheme.colors.onSurface.copy(alpha = 0.32f)
            )
        }
    }

    DisplayBadges(baseUser, nav)

    DisplayNip05ProfileStatus(user)

    val website = user.info?.website
    if (!website.isNullOrEmpty()) {
        Row(verticalAlignment = Alignment.CenterVertically) {
            Icon(
                tint = MaterialTheme.colors.onSurface.copy(alpha = 0.32f),
                imageVector = Icons.Default.Link,
                contentDescription = stringResource(R.string.website),
                modifier = Modifier.size(16.dp)
            )

            ClickableText(
                text = AnnotatedString(website.removePrefix("https://")),
                onClick = { website.let { runCatching { uri.openUri(it) } } },
                style = LocalTextStyle.current.copy(color = MaterialTheme.colors.primary),
                modifier = Modifier.padding(top = 1.dp, bottom = 1.dp, start = 5.dp)
            )
        }
    }

    val lud16 = remember(userState) { user.info?.lud16?.trim() ?: user.info?.lud06?.trim() }
    val pubkeyHex = remember { baseUser.pubkeyHex }
    DisplayLNAddress(lud16, pubkeyHex, accountViewModel.account, scope, context)

    val identities = user.info?.latestMetadata?.identityClaims()
    if (!identities.isNullOrEmpty()) {
        identities.forEach { identity: IdentityClaim ->
            Row(verticalAlignment = Alignment.CenterVertically) {
                Icon(
                    tint = Color.Unspecified,
                    painter = painterResource(id = identity.toIcon()),
                    contentDescription = stringResource(identity.toDescriptor()),
                    modifier = Modifier.size(16.dp)
                )

                ClickableText(
                    text = AnnotatedString(identity.identity),
                    onClick = { runCatching { uri.openUri(identity.toProofUrl()) } },
                    style = LocalTextStyle.current.copy(color = MaterialTheme.colors.primary),
                    modifier = Modifier
                        .padding(top = 1.dp, bottom = 1.dp, start = 5.dp)
                        .weight(1f)
                )
            }
        }
    }

    user.info?.about?.let {
        Row(
            modifier = Modifier.padding(top = 5.dp, bottom = 5.dp)
        ) {
            TranslatableRichTextViewer(
                content = it,
                canPreview = false,
                tags = null,
                backgroundColor = MaterialTheme.colors.background,
                accountViewModel = accountViewModel,
                nav = nav
            )
        }
    }
}

@Composable
private fun DisplayLNAddress(
    lud16: String?,
    userHex: String,
    account: Account,
    scope: CoroutineScope,
    context: Context
) {
    var zapExpanded by remember { mutableStateOf(false) }

    if (!lud16.isNullOrEmpty()) {
        Row(verticalAlignment = Alignment.CenterVertically) {
            Icon(
                tint = BitcoinOrange,
                imageVector = Icons.Default.Bolt,
                contentDescription = stringResource(R.string.lightning_address),
                modifier = Modifier.size(16.dp)
            )

            ClickableText(
                text = AnnotatedString(lud16),
                onClick = { zapExpanded = !zapExpanded },
                style = LocalTextStyle.current.copy(color = MaterialTheme.colors.primary),
                modifier = Modifier
                    .padding(top = 1.dp, bottom = 1.dp, start = 5.dp)
                    .weight(1f)
            )
        }

        if (zapExpanded) {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                modifier = Modifier.padding(vertical = 5.dp)
            ) {
                InvoiceRequest(
                    lud16,
                    userHex,
                    account,
                    onSuccess = {
                        // pay directly
                        if (account.hasWalletConnectSetup()) {
                            account.sendZapPaymentRequestFor(it, null) { response ->
                                if (response is PayInvoiceSuccessResponse) {
                                    scope.launch {
                                        Toast.makeText(
                                            context,
                                            "Payment Successful", // Turn this into a UI animation
                                            Toast.LENGTH_LONG
                                        ).show()
                                    }
                                } else if (response is PayInvoiceErrorResponse) {
                                    scope.launch {
                                        Toast.makeText(
                                            context,
                                            response.error?.message
                                                ?: response.error?.code?.toString()
                                                ?: "Error parsing error message",
                                            Toast.LENGTH_LONG
                                        ).show()
                                    }
                                }
                            }
                        } else {
                            runCatching {
                                val intent = Intent(Intent.ACTION_VIEW, Uri.parse("lightning:$it"))
                                ContextCompat.startActivity(context, intent, null)
                            }
                        }
                    },
                    onClose = {
                        zapExpanded = false
                    }
                )
            }
        }
    }
}

@Composable
@OptIn(ExperimentalLayoutApi::class)
private fun DisplayBadges(
    baseUser: User,
    nav: (String) -> Unit
) {
    val userBadgeState by baseUser.live().badges.observeAsState()
    val badgeList by remember(userBadgeState) {
        derivedStateOf {
            val list = (userBadgeState?.user?.acceptedBadges?.event as? BadgeProfilesEvent)?.badgeAwardEvents()
            if (list.isNullOrEmpty()) {
                null
            } else {
                list
            }
        }
    }

    badgeList?.let { list ->
        FlowRow(
            verticalAlignment = Alignment.CenterVertically,
            modifier = Modifier.padding(vertical = 5.dp)
        ) {
            list.forEach { badgeAwardEvent ->
                LoadAndRenderBadge(badgeAwardEvent, nav)
            }
        }
    }
}

@Composable
private fun LoadAndRenderBadge(badgeAwardEventHex: String, nav: (String) -> Unit) {
    var baseNote by remember {
        mutableStateOf<Note?>(null)
    }

    val scope = rememberCoroutineScope()
    LaunchedEffect(key1 = badgeAwardEventHex) {
        scope.launch(Dispatchers.IO) {
            baseNote = LocalCache.getOrCreateNote(badgeAwardEventHex)
        }
    }

    baseNote?.let {
        val badgeAwardState by it.live().metadata.observeAsState()
        val baseBadgeDefinition by remember(badgeAwardState) {
            derivedStateOf {
                badgeAwardState?.note?.replyTo?.firstOrNull()
            }
        }

        baseBadgeDefinition?.let {
            BadgeThumb(it, nav, 35.dp)
        }
    }
}

@Composable
fun BadgeThumb(
    note: Note,
    nav: (String) -> Unit,
    size: Dp,
    pictureModifier: Modifier = Modifier
) {
    BadgeThumb(note, size, pictureModifier) {
        nav("Note/$it")
    }
}

@Composable
fun BadgeThumb(
    baseNote: Note,
    size: Dp,
    pictureModifier: Modifier = Modifier,
    onClick: ((String) -> Unit)? = null
) {
    Box(
        remember {
            Modifier
                .width(size)
                .height(size)
        }
    ) {
        val noteState by baseNote.live().metadata.observeAsState()
        val event = remember(noteState) { noteState?.note?.event as? BadgeDefinitionEvent } ?: return
        val image = remember(noteState) { event.thumb() ?: event.image() }

        if (image == null) {
            RobohashAsyncImage(
                robot = "authornotfound",
                robotSize = size,
                contentDescription = stringResource(R.string.unknown_author),
                modifier = pictureModifier
                    .width(size)
                    .height(size)
                    .background(MaterialTheme.colors.background)
            )
        } else {
            RobohashFallbackAsyncImage(
                robot = event.id,
                robotSize = size,
                model = image,
                contentDescription = stringResource(id = R.string.profile_image),
                modifier = pictureModifier
                    .width(size)
                    .height(size)
                    .clip(shape = CircleShape)
                    .background(MaterialTheme.colors.background)
                    .run {
                        if (onClick != null) {
                            this.clickable(onClick = { onClick(event.id) })
                        } else {
                            this
                        }
                    }

            )
        }
    }
}

@OptIn(ExperimentalFoundationApi::class)
@Composable
private fun DrawBanner(baseUser: User) {
    val userState by baseUser.live().metadata.observeAsState()
    val banner = remember(userState) { userState?.user?.info?.banner }

    val clipboardManager = LocalClipboardManager.current
    var zoomImageDialogOpen by remember { mutableStateOf(false) }

    if (!banner.isNullOrBlank()) {
        AsyncImage(
            model = banner,
            contentDescription = stringResource(id = R.string.profile_image),
            contentScale = ContentScale.FillWidth,
            modifier = Modifier
                .fillMaxWidth()
                .height(125.dp)
                .combinedClickable(
                    onClick = {},
                    onLongClick = {
                        clipboardManager.setText(AnnotatedString(banner))
                    }
                )
                .clickable { zoomImageDialogOpen = true }
        )

        if (zoomImageDialogOpen) {
            ZoomableImageDialog(imageUrl = figureOutMimeType(banner), onDismiss = { zoomImageDialogOpen = false })
        }
    } else {
        Image(
            painter = painterResource(R.drawable.profile_banner),
            contentDescription = stringResource(id = R.string.profile_banner),
            contentScale = ContentScale.FillWidth,
            modifier = Modifier
                .fillMaxWidth()
                .height(125.dp)
        )
    }
}

@Composable
fun TabNotesNewThreads(accountViewModel: AccountViewModel, nav: (String) -> Unit) {
    val feedViewModel: NostrUserProfileNewThreadsFeedViewModel = viewModel()

    LaunchedEffect(Unit) {
        feedViewModel.invalidateData()
    }

    Column(Modifier.fillMaxHeight()) {
        Column(
            modifier = Modifier.padding(vertical = 0.dp)
        ) {
            RefresheableView(feedViewModel, accountViewModel, nav, null, enablePullRefresh = false)
        }
    }
}

@Composable
fun TabNotesConversations(accountViewModel: AccountViewModel, nav: (String) -> Unit) {
    val feedViewModel: NostrUserProfileConversationsFeedViewModel = viewModel()

    LaunchedEffect(Unit) {
        feedViewModel.invalidateData()
    }

    Column(Modifier.fillMaxHeight()) {
        Column(
            modifier = Modifier.padding(vertical = 0.dp)
        ) {
            RefresheableView(feedViewModel, accountViewModel, nav, null, enablePullRefresh = false)
        }
    }
}

@Composable
fun TabBookmarks(baseUser: User, accountViewModel: AccountViewModel, nav: (String) -> Unit) {
    val feedViewModel: NostrUserProfileBookmarksFeedViewModel = viewModel()

    LaunchedEffect(Unit) {
        feedViewModel.invalidateData()
    }

    Column(Modifier.fillMaxHeight()) {
        Column(
            modifier = Modifier.padding(vertical = 0.dp)
        ) {
            RefresheableView(feedViewModel, accountViewModel, nav, null, enablePullRefresh = false)
        }
    }
}

@Composable
fun TabFollows(baseUser: User, accountViewModel: AccountViewModel, nav: (String) -> Unit) {
    val feedViewModel: NostrUserProfileFollowsUserFeedViewModel = viewModel()

    val userState by baseUser.live().follows.observeAsState()

    LaunchedEffect(userState) {
        feedViewModel.invalidateData()
    }

    Column(Modifier.fillMaxHeight()) {
        Column(
            modifier = Modifier.padding(vertical = 0.dp)
        ) {
            UserFeedView(feedViewModel, accountViewModel, nav, enablePullRefresh = false)
        }
    }
}

@Composable
fun TabFollowers(baseUser: User, accountViewModel: AccountViewModel, nav: (String) -> Unit) {
    val feedViewModel: NostrUserProfileFollowersUserFeedViewModel = viewModel()

    val userState by baseUser.live().follows.observeAsState()

    LaunchedEffect(userState) {
        feedViewModel.invalidateData()
    }

    Column(Modifier.fillMaxHeight()) {
        Column(
            modifier = Modifier.padding(vertical = 0.dp)
        ) {
            UserFeedView(feedViewModel, accountViewModel, nav, enablePullRefresh = false)
        }
    }
}

@Composable
fun TabReceivedZaps(baseUser: User, accountViewModel: AccountViewModel, nav: (String) -> Unit) {
    val feedViewModel: NostrUserProfileZapsFeedViewModel = viewModel()

    val userState by baseUser.live().zaps.observeAsState()

    LaunchedEffect(userState) {
        feedViewModel.invalidateData()
    }

    Column(Modifier.fillMaxHeight()) {
        Column(
            modifier = Modifier.padding(vertical = 0.dp)
        ) {
            LnZapFeedView(feedViewModel, accountViewModel, nav, enablePullRefresh = false)
        }
    }
}

@Composable
fun TabReports(baseUser: User, accountViewModel: AccountViewModel, nav: (String) -> Unit) {
    val feedViewModel: NostrUserProfileReportFeedViewModel = viewModel()

    val userState by baseUser.live().reports.observeAsState()

    LaunchedEffect(userState) {
        feedViewModel.invalidateData()
    }

    Column(Modifier.fillMaxHeight()) {
        Column(
            modifier = Modifier.padding(vertical = 0.dp)
        ) {
            RefresheableView(feedViewModel, accountViewModel, nav, null, enablePullRefresh = false)
        }
    }
}

@Composable
fun TabRelays(user: User, accountViewModel: AccountViewModel) {
    val feedViewModel: RelayFeedViewModel = viewModel()

    val lifeCycleOwner = LocalLifecycleOwner.current

    DisposableEffect(user) {
        val observer = LifecycleEventObserver { _, event ->
            if (event == Lifecycle.Event.ON_RESUME) {
                println("Profile Relay Start")
                feedViewModel.subscribeTo(user)
            }
            if (event == Lifecycle.Event.ON_PAUSE) {
                println("Profile Relay Stop")
                feedViewModel.unsubscribeTo(user)
            }
        }

        lifeCycleOwner.lifecycle.addObserver(observer)
        onDispose {
            lifeCycleOwner.lifecycle.removeObserver(observer)
            println("Profile Relay Dispose")
            feedViewModel.unsubscribeTo(user)
        }
    }

    Column(Modifier.fillMaxHeight()) {
        Column(
            modifier = Modifier.padding(vertical = 0.dp)
        ) {
            RelayFeedView(feedViewModel, accountViewModel, enablePullRefresh = false)
        }
    }
}

@Composable
private fun MessageButton(user: User, nav: (String) -> Unit) {
    Button(
        modifier = Modifier
            .padding(horizontal = 3.dp)
            .width(50.dp),
        onClick = { nav("Room/${user.pubkeyHex}") },
        shape = RoundedCornerShape(20.dp),
        colors = ButtonDefaults
            .buttonColors(
                backgroundColor = MaterialTheme.colors.onSurface.copy(alpha = 0.32f)
            )
    ) {
        Icon(
            painter = painterResource(R.drawable.ic_dm),
            stringResource(R.string.send_a_direct_message),
            modifier = Modifier.size(20.dp),
            tint = Color.White
        )
    }
}

@Composable
private fun EditButton(account: Account) {
    var wantsToEdit by remember {
        mutableStateOf(false)
    }

    if (wantsToEdit) {
        NewUserMetadataView({ wantsToEdit = false }, account)
    }

    Button(
        modifier = Modifier
            .padding(horizontal = 3.dp)
            .width(50.dp),
        onClick = { wantsToEdit = true },
        shape = RoundedCornerShape(20.dp),
        colors = ButtonDefaults
            .buttonColors(
                backgroundColor = MaterialTheme.colors.primary
            )
    ) {
        Icon(
            tint = Color.White,
            imageVector = Icons.Default.EditNote,
            contentDescription = stringResource(R.string.edits_the_user_s_metadata)
        )
    }
}

@Composable
fun UnfollowButton(onClick: () -> Unit) {
    Button(
        modifier = Modifier.padding(horizontal = 3.dp),
        onClick = onClick,
        shape = RoundedCornerShape(20.dp),
        colors = ButtonDefaults
            .buttonColors(
                backgroundColor = MaterialTheme.colors.primary
            ),
        contentPadding = PaddingValues(vertical = 6.dp, horizontal = 16.dp)
    ) {
        Text(text = stringResource(R.string.unfollow), color = Color.White)
    }
}

@Composable
fun FollowButton(onClick: () -> Unit, text: Int = R.string.follow) {
    Button(
        modifier = Modifier.padding(start = 3.dp),
        onClick = onClick,
        shape = RoundedCornerShape(20.dp),
        colors = ButtonDefaults
            .buttonColors(
                backgroundColor = MaterialTheme.colors.primary
            ),
        contentPadding = PaddingValues(vertical = 6.dp, horizontal = 16.dp)
    ) {
        Text(text = stringResource(text), color = Color.White, textAlign = TextAlign.Center)
    }
}

@Composable
fun ShowUserButton(onClick: () -> Unit) {
    Button(
        modifier = Modifier.padding(start = 3.dp),
        onClick = onClick,
        shape = RoundedCornerShape(20.dp),
        colors = ButtonDefaults
            .buttonColors(
                backgroundColor = MaterialTheme.colors.primary
            ),
        contentPadding = PaddingValues(vertical = 6.dp, horizontal = 16.dp)
    ) {
        Text(text = stringResource(R.string.unblock), color = Color.White)
    }
}

@Composable
fun UserProfileDropDownMenu(user: User, popupExpanded: Boolean, onDismiss: () -> Unit, accountViewModel: AccountViewModel) {
    val clipboardManager = LocalClipboardManager.current
    val accountState by accountViewModel.accountLiveData.observeAsState()
    val account = accountState?.account ?: return

    DropdownMenu(
        expanded = popupExpanded,
        onDismissRequest = onDismiss
    ) {
        DropdownMenuItem(onClick = { clipboardManager.setText(AnnotatedString(user.pubkeyNpub())); onDismiss() }) {
            Text(stringResource(R.string.copy_user_id))
        }

        if (account.userProfile() != user) {
            Divider()
            if (account.isHidden(user)) {
                DropdownMenuItem(onClick = {
                    accountViewModel.show(user)
                    onDismiss()
                }) {
                    Text(stringResource(R.string.unblock_user))
                }
            } else {
                DropdownMenuItem(onClick = {
                    accountViewModel.hide(user)
                    onDismiss()
                }) {
                    Text(stringResource(id = R.string.block_hide_user))
                }
            }
            Divider()
            DropdownMenuItem(onClick = {
                accountViewModel.report(user, ReportEvent.ReportType.SPAM)
                accountViewModel.hide(user)
                onDismiss()
            }) {
                Text(stringResource(id = R.string.report_spam_scam))
            }
            DropdownMenuItem(onClick = {
                accountViewModel.report(user, ReportEvent.ReportType.PROFANITY)
                accountViewModel.hide(user)
                onDismiss()
            }) {
                Text(stringResource(R.string.report_hateful_speech))
            }
            DropdownMenuItem(onClick = {
                accountViewModel.report(user, ReportEvent.ReportType.IMPERSONATION)
                accountViewModel.hide(user)
                onDismiss()
            }) {
                Text(stringResource(id = R.string.report_impersonation))
            }
            DropdownMenuItem(onClick = {
                accountViewModel.report(user, ReportEvent.ReportType.NUDITY)
                accountViewModel.hide(user)
                onDismiss()
            }) {
                Text(stringResource(R.string.report_nudity_porn))
            }
            DropdownMenuItem(onClick = {
                accountViewModel.report(user, ReportEvent.ReportType.ILLEGAL)
                accountViewModel.hide(user)
                onDismiss()
            }) {
                Text(stringResource(id = R.string.report_illegal_behaviour))
            }
        }
    }
}

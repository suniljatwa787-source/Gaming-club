package com.example

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.BackHandler
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.AccountBalanceWallet
import androidx.compose.material.icons.filled.AccountCircle
import androidx.compose.material.icons.filled.SportsEsports
import androidx.compose.material.icons.filled.VideoLibrary
import androidx.compose.material3.Icon
import androidx.compose.material3.NavigationBar
import androidx.compose.material3.NavigationBarItem
import androidx.compose.material3.NavigationBarItemDefaults
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.sp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.model.PaymentGatewayType
import com.example.model.GameOwnership
import com.example.ui.components.OwnerAdminSheet
import com.example.ui.components.PaymentGatewaySheet
import com.example.ui.components.ReceiptDialog
import com.example.ui.components.SecurityComplianceSheet
import com.example.ui.game.PlayableArcadeGame
import com.example.ui.screens.GameDetailSheet
import com.example.ui.screens.LibraryScreen
import com.example.ui.screens.StoreScreen
import com.example.ui.screens.UserProfileScreen
import com.example.ui.screens.WalletScreen
import com.example.ui.theme.CardSurfaceDark
import com.example.ui.theme.CyberCyanPrimary
import com.example.ui.theme.DeepNavySurface
import com.example.ui.theme.MyApplicationTheme
import com.example.ui.theme.TextMutedDark
import com.example.ui.theme.TextPrimaryDark
import com.example.viewmodel.AppNavTab
import com.example.viewmodel.MainViewModel
import com.example.viewmodel.PaymentIntent
import kotlinx.coroutines.launch

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {
            MyApplicationTheme {
                PlayVaultApp()
            }
        }
    }
}

@Composable
fun PlayVaultApp(viewModel: MainViewModel = viewModel()) {
    val currentTab by viewModel.currentTab.collectAsState()
    val searchQuery by viewModel.searchQuery.collectAsState()
    val selectedCategory by viewModel.selectedCategory.collectAsState()
    val filteredCatalog by viewModel.filteredCatalog.collectAsState()
    val ownershipMap by viewModel.ownershipMap.collectAsState()
    val ownedGames by viewModel.ownedGames.collectAsState()
    val walletBalance by viewModel.walletBalance.collectAsState()
    val savedCards by viewModel.savedCards.collectAsState()
    val transactions by viewModel.transactions.collectAsState()
    val userProfile by viewModel.userProfile.collectAsState()

    val selectedGameDetail by viewModel.selectedGameDetail.collectAsState()
    val activePaymentIntent by viewModel.activePaymentIntent.collectAsState()
    val activeInstantGame by viewModel.activeInstantGame.collectAsState()
    val viewingReceipt by viewModel.viewingReceipt.collectAsState()
    val showSecurityCompliance by viewModel.showSecurityCompliance.collectAsState()
    val showOwnerAdminDialog by viewModel.showOwnerAdminDialog.collectAsState()
    val isOwnerModeActive by viewModel.isOwnerModeActive.collectAsState()
    val snackbarMessage by viewModel.snackbarMessage.collectAsState()

    val snackbarHostState = remember { SnackbarHostState() }
    val coroutineScope = rememberCoroutineScope()

    LaunchedEffect(snackbarMessage) {
        snackbarMessage?.let { msg ->
            coroutineScope.launch {
                snackbarHostState.showSnackbar(msg)
                viewModel.clearMessage()
            }
        }
    }

    // Handle back button when sub-screens or games are open
    BackHandler(
        enabled = activeInstantGame != null ||
                selectedGameDetail != null ||
                activePaymentIntent != null ||
                viewingReceipt != null ||
                showSecurityCompliance ||
                showOwnerAdminDialog ||
                currentTab != AppNavTab.STORE
    ) {
        when {
            activeInstantGame != null -> viewModel.exitInstantPlay()
            selectedGameDetail != null -> viewModel.closeGameDetail()
            activePaymentIntent != null -> viewModel.dismissCheckout()
            viewingReceipt != null -> viewModel.closeReceipt()
            showSecurityCompliance -> viewModel.toggleSecurityCompliance(false)
            showOwnerAdminDialog -> viewModel.closeOwnerAdmin()
            currentTab != AppNavTab.STORE -> viewModel.selectTab(AppNavTab.STORE)
        }
    }

    if (activeInstantGame != null) {
        // Full screen interactive arcade mini-game
        PlayableArcadeGame(
            game = activeInstantGame!!,
            onExitGame = { viewModel.exitInstantPlay() },
            onRewardEarned = { reward, title -> viewModel.onGameRewardEarned(reward, title) },
            onGameOverRecord = { gameId, score, mins -> viewModel.onGameOverScore(gameId, score, mins) }
        )
    } else {
        Scaffold(
            modifier = Modifier
                .fillMaxSize()
                .testTag("main_scaffold"),
            containerColor = DeepNavySurface,
            snackbarHost = { SnackbarHost(hostState = snackbarHostState) },
            bottomBar = {
                NavigationBar(
                    containerColor = CardSurfaceDark,
                    modifier = Modifier.testTag("bottom_navigation_bar")
                ) {
                    NavigationBarItem(
                        selected = currentTab == AppNavTab.STORE,
                        onClick = { viewModel.selectTab(AppNavTab.STORE) },
                        icon = { Icon(Icons.Default.SportsEsports, contentDescription = "Store") },
                        label = { Text("Store", fontSize = 11.sp, fontWeight = FontWeight.Bold) },
                        colors = NavigationBarItemDefaults.colors(
                            selectedIconColor = DeepNavySurface,
                            selectedTextColor = CyberCyanPrimary,
                            indicatorColor = CyberCyanPrimary,
                            unselectedIconColor = TextMutedDark,
                            unselectedTextColor = TextMutedDark
                        ),
                        modifier = Modifier.testTag("nav_tab_store")
                    )

                    NavigationBarItem(
                        selected = currentTab == AppNavTab.LIBRARY,
                        onClick = { viewModel.selectTab(AppNavTab.LIBRARY) },
                        icon = { Icon(Icons.Default.VideoLibrary, contentDescription = "Library") },
                        label = { Text("Library", fontSize = 11.sp, fontWeight = FontWeight.Bold) },
                        colors = NavigationBarItemDefaults.colors(
                            selectedIconColor = DeepNavySurface,
                            selectedTextColor = CyberCyanPrimary,
                            indicatorColor = CyberCyanPrimary,
                            unselectedIconColor = TextMutedDark,
                            unselectedTextColor = TextMutedDark
                        ),
                        modifier = Modifier.testTag("nav_tab_library")
                    )

                    NavigationBarItem(
                        selected = currentTab == AppNavTab.WALLET,
                        onClick = { viewModel.selectTab(AppNavTab.WALLET) },
                        icon = { Icon(Icons.Default.AccountBalanceWallet, contentDescription = "Wallet") },
                        label = { Text("Wallet", fontSize = 11.sp, fontWeight = FontWeight.Bold) },
                        colors = NavigationBarItemDefaults.colors(
                            selectedIconColor = DeepNavySurface,
                            selectedTextColor = CyberCyanPrimary,
                            indicatorColor = CyberCyanPrimary,
                            unselectedIconColor = TextMutedDark,
                            unselectedTextColor = TextMutedDark
                        ),
                        modifier = Modifier.testTag("nav_tab_wallet")
                    )

                    NavigationBarItem(
                        selected = currentTab == AppNavTab.PROFILE,
                        onClick = { viewModel.selectTab(AppNavTab.PROFILE) },
                        icon = { Icon(Icons.Default.AccountCircle, contentDescription = "Profile") },
                        label = { Text("Profile", fontSize = 11.sp, fontWeight = FontWeight.Bold) },
                        colors = NavigationBarItemDefaults.colors(
                            selectedIconColor = DeepNavySurface,
                            selectedTextColor = CyberCyanPrimary,
                            indicatorColor = CyberCyanPrimary,
                            unselectedIconColor = TextMutedDark,
                            unselectedTextColor = TextMutedDark
                        ),
                        modifier = Modifier.testTag("nav_tab_profile")
                    )
                }
            }
        ) { innerPadding ->
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(innerPadding)
            ) {
                when (currentTab) {
                    AppNavTab.STORE -> {
                        StoreScreen(
                            catalog = filteredCatalog,
                            ownershipMap = ownershipMap,
                            walletBalance = walletBalance,
                            searchQuery = searchQuery,
                            onSearchQueryChange = { viewModel.updateSearchQuery(it) },
                            selectedCategory = selectedCategory,
                            onSelectCategory = { viewModel.selectCategory(it) },
                            onGameClick = { viewModel.openGameDetail(it) },
                            onStartCheckout = { viewModel.startCheckoutForGame(it) },
                            onLaunchInstantPlay = { viewModel.launchInstantPlay(it) },
                            onOpenTopUp = { viewModel.startCheckoutForTopUp(25.0) },
                            onOpenSecurityCompliance = { viewModel.toggleSecurityCompliance(true) },
                            isOwnerModeActive = isOwnerModeActive,
                            onOpenOwnerAdmin = { viewModel.openOwnerAdmin() }
                        )
                    }
                    AppNavTab.LIBRARY -> {
                        LibraryScreen(
                            ownedGames = ownedGames,
                            onGameClick = { viewModel.openGameDetail(it) },
                            onLaunchPlay = { viewModel.launchInstantPlay(it) },
                            onStartDownload = { viewModel.startDownload(it) },
                            onPauseDownload = { viewModel.pauseDownload(it) },
                            onCancelDownload = { viewModel.cancelDownload(it) },
                            onUninstallGame = { viewModel.uninstallGame(it) },
                            onBrowseStore = { viewModel.selectTab(AppNavTab.STORE) }
                        )
                    }
                    AppNavTab.WALLET -> {
                        WalletScreen(
                            walletBalance = walletBalance,
                            savedCards = savedCards,
                            transactions = transactions,
                            onTopUpClick = { amount -> viewModel.startCheckoutForTopUp(amount) },
                            onViewReceipt = { tx -> viewModel.viewReceipt(tx) },
                            onOpenSecurityCompliance = { viewModel.toggleSecurityCompliance(true) }
                        )
                    }
                    AppNavTab.PROFILE -> {
                        UserProfileScreen(
                            profile = userProfile,
                            ownedGames = ownedGames,
                            transactions = transactions,
                            walletBalance = walletBalance,
                            isOwnerModeActive = isOwnerModeActive,
                            onUpdateProfile = { name, tag, email -> viewModel.updateProfile(name, tag, email) },
                            onUpdateAccountSetting = { bio, twoFa, wifi, autoUp, highP, sound, notifs, defG ->
                                viewModel.updateAccountSetting(bio, twoFa, wifi, autoUp, highP, sound, notifs, defG)
                            },
                            onClearCache = { viewModel.clearAppCache() },
                            onLaunchGame = { viewModel.launchInstantPlay(it) },
                            onDownloadGame = { viewModel.startDownload(it.id) },
                            onOpenGameDetail = { viewModel.openGameDetail(it) },
                            onViewReceipt = { tx -> viewModel.viewReceipt(tx) },
                            onOpenOwnerAdmin = { viewModel.openOwnerAdmin() },
                            onOpenSecurityCompliance = { viewModel.toggleSecurityCompliance(true) },
                            onBrowseStore = { viewModel.selectTab(AppNavTab.STORE) }
                        )
                    }
                }
            }
        }

        // Game Detail Modal Sheet (with free owner license auto-applied when owner mode active)
        selectedGameDetail?.let { game ->
            val ownerLicense = if (isOwnerModeActive) {
                ownershipMap[game.id]?.copy(isOwned = true) ?: GameOwnership(gameId = game.id, isOwned = true)
            } else {
                ownershipMap[game.id]
            }

            GameDetailSheet(
                game = game,
                ownership = ownerLicense,
                onDismiss = { viewModel.closeGameDetail() },
                onStartCheckout = { viewModel.startCheckoutForGame(it) },
                onLaunchInstantPlay = { viewModel.launchInstantPlay(it) },
                onStartDownload = { viewModel.startDownload(it) },
                onPauseDownload = { viewModel.pauseDownload(it) },
                onRequestRefund = { viewModel.requestRefund(it) }
            )
        }

        // Secure Payment Gateway Bottom Sheet
        activePaymentIntent?.let { intent ->
            PaymentGatewaySheet(
                intent = intent,
                walletBalance = walletBalance,
                savedCards = savedCards,
                onDismiss = { viewModel.dismissCheckout() },
                onConfirmPayment = { gateway, descriptor, cryptoAmount, cryptoCurrency ->
                    when (intent) {
                        is PaymentIntent.BuyGame -> {
                            viewModel.processPurchase(intent.game, gateway, descriptor, cryptoAmount, cryptoCurrency)
                        }
                        is PaymentIntent.TopUpWallet -> {
                            viewModel.processTopUp(intent.amountUsd, gateway, descriptor, cryptoAmount, cryptoCurrency)
                        }
                    }
                }
            )
        }

        // Encrypted Digital Receipt Dialog
        viewingReceipt?.let { record ->
            ReceiptDialog(
                record = record,
                onDismiss = { viewModel.closeReceipt() },
                onNavigateLibrary = {
                    viewModel.closeReceipt()
                    viewModel.selectTab(AppNavTab.LIBRARY)
                }
            )
        }

        // Security & Compliance Modal
        if (showSecurityCompliance) {
            SecurityComplianceSheet(
                onDismiss = { viewModel.toggleSecurityCompliance(false) }
            )
        }

        // Exclusive Owner Admin Modal (Gaming Club Administrator)
        if (showOwnerAdminDialog) {
            OwnerAdminSheet(
                catalog = filteredCatalog,
                isOwnerModeActive = isOwnerModeActive,
                onToggleOwnerMode = { viewModel.toggleOwnerMode(it) },
                onUpdateGamePrice = { id, price -> viewModel.updateGamePrice(id, price) },
                onToggleGameFeatured = { id -> viewModel.toggleGameFeatured(id) },
                onDismiss = { viewModel.closeOwnerAdmin() }
            )
        }
    }
}

package com.example.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.model.GameCategory
import com.example.model.GameItem
import com.example.model.GameOwnership
import com.example.model.MerchantAccountConfig
import com.example.model.PaymentGatewayType
import com.example.model.PlayMode
import com.example.model.SavedPaymentCard
import com.example.model.TransactionRecord
import com.example.model.UserAccountProfile
import com.example.repository.GameStoreRepository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.stateIn

enum class AppNavTab {
    STORE,
    LIBRARY,
    WALLET,
    PROFILE
}

sealed class PaymentIntent {
    data class BuyGame(val game: GameItem) : PaymentIntent()
    data class TopUpWallet(val amountUsd: Double) : PaymentIntent()
}

class MainViewModel(
    val repository: GameStoreRepository = GameStoreRepository()
) : ViewModel() {

    private val _currentTab = MutableStateFlow(AppNavTab.STORE)
    val currentTab: StateFlow<AppNavTab> = _currentTab.asStateFlow()

    private val _searchQuery = MutableStateFlow("")
    val searchQuery: StateFlow<String> = _searchQuery.asStateFlow()

    private val _selectedCategory = MutableStateFlow(GameCategory.ALL)
    val selectedCategory: StateFlow<GameCategory> = _selectedCategory.asStateFlow()

    private val _selectedGameDetail = MutableStateFlow<GameItem?>(null)
    val selectedGameDetail: StateFlow<GameItem?> = _selectedGameDetail.asStateFlow()

    // Active payment checkout intent (Game purchase or Wallet top-up)
    private val _activePaymentIntent = MutableStateFlow<PaymentIntent?>(null)
    val activePaymentIntent: StateFlow<PaymentIntent?> = _activePaymentIntent.asStateFlow()

    // Active instant game in progress
    private val _activeInstantGame = MutableStateFlow<GameItem?>(null)
    val activeInstantGame: StateFlow<GameItem?> = _activeInstantGame.asStateFlow()

    // Transaction receipt dialog record
    private val _viewingReceipt = MutableStateFlow<TransactionRecord?>(null)
    val viewingReceipt: StateFlow<TransactionRecord?> = _viewingReceipt.asStateFlow()

    // Security compliance sheet toggle
    private val _showSecurityCompliance = MutableStateFlow(false)
    val showSecurityCompliance: StateFlow<Boolean> = _showSecurityCompliance.asStateFlow()

    // Owner Admin Panel toggle
    private val _showOwnerAdminDialog = MutableStateFlow(false)
    val showOwnerAdminDialog: StateFlow<Boolean> = _showOwnerAdminDialog.asStateFlow()

    val isOwnerModeActive: StateFlow<Boolean> = repository.isOwnerModeActive

    // User status/snackbar message
    private val _snackbarMessage = MutableStateFlow<String?>(null)
    val snackbarMessage: StateFlow<String?> = _snackbarMessage.asStateFlow()

    val catalog: StateFlow<List<GameItem>> = repository.catalog
    val ownershipMap: StateFlow<Map<String, GameOwnership>> = repository.ownershipMap
    val walletBalance: StateFlow<Double> = repository.walletBalance
    val savedCards: StateFlow<List<SavedPaymentCard>> = repository.savedCards
    val transactions: StateFlow<List<TransactionRecord>> = repository.transactions
    val userProfile: StateFlow<UserAccountProfile> = repository.userProfile

    private fun computeFilteredCatalog(list: List<GameItem>, query: String, category: GameCategory): List<GameItem> {
        return list.filter { game ->
            val matchesCategory = when (category) {
                GameCategory.ALL -> true
                GameCategory.FREE_GAMES -> game.isFree
                GameCategory.OFFLINE_GAMES -> game.playMode == PlayMode.OFFLINE || game.playMode == PlayMode.BOTH
                GameCategory.ONLINE_GAMES -> game.playMode == PlayMode.ONLINE || game.playMode == PlayMode.BOTH
                else -> game.category == category
            }
            val matchesQuery = query.isBlank() ||
                    game.title.contains(query, ignoreCase = true) ||
                    game.tagline.contains(query, ignoreCase = true) ||
                    game.description.contains(query, ignoreCase = true)
            matchesCategory && matchesQuery
        }
    }

    // Filtered catalog based on search query and category
    val filteredCatalog: StateFlow<List<GameItem>> = combine(
        catalog,
        _searchQuery,
        _selectedCategory
    ) { list, query, category ->
        computeFilteredCatalog(list, query, category)
    }.stateIn(viewModelScope, SharingStarted.Eagerly, computeFilteredCatalog(repository.catalog.value, "", GameCategory.ALL))

    // Owned games list - Owner automatically receives 100% free unlocked access to all games!
    val ownedGames: StateFlow<List<Pair<GameItem, GameOwnership>>> = combine(
        catalog,
        ownershipMap,
        isOwnerModeActive
    ) { list, map, isOwner ->
        list.mapNotNull { game ->
            val ownership = map[game.id]
            if (isOwner) {
                game to (ownership?.copy(isOwned = true) ?: GameOwnership(gameId = game.id, isOwned = true))
            } else if (ownership?.isOwned == true) {
                game to ownership
            } else null
        }
    }.stateIn(
        viewModelScope,
        SharingStarted.Eagerly,
        repository.catalog.value.map { it to GameOwnership(gameId = it.id, isOwned = true) }
    )

    fun selectTab(tab: AppNavTab) {
        _currentTab.value = tab
    }

    fun updateSearchQuery(query: String) {
        _searchQuery.value = query
    }

    fun selectCategory(category: GameCategory) {
        _selectedCategory.value = category
    }

    fun getFilteredGames(): List<GameItem> {
        return computeFilteredCatalog(catalog.value, _searchQuery.value, _selectedCategory.value)
    }

    fun openGameDetail(game: GameItem) {
        _selectedGameDetail.value = game
    }

    fun closeGameDetail() {
        _selectedGameDetail.value = null
    }

    fun startCheckoutForGame(game: GameItem) {
        if (game.isFree) {
            claimFreeGame(game)
        } else {
            _activePaymentIntent.value = PaymentIntent.BuyGame(game)
        }
    }

    fun claimFreeGame(game: GameItem) {
        val result = repository.claimFreeGame(game)
        if (result.isSuccess) {
            val tx = result.getOrThrow()
            _viewingReceipt.value = tx
            showMessage("🎉 \"${game.title}\" claimed for FREE! Unlocked and added to Library.")
        } else {
            showMessage(result.exceptionOrNull()?.message ?: "Could not claim game.")
        }
    }

    fun startCheckoutForTopUp(amount: Double) {
        _activePaymentIntent.value = PaymentIntent.TopUpWallet(amount)
    }

    fun dismissCheckout() {
        _activePaymentIntent.value = null
    }

    fun launchInstantPlay(game: GameItem) {
        _activeInstantGame.value = game
    }

    fun exitInstantPlay() {
        _activeInstantGame.value = null
    }

    fun viewReceipt(record: TransactionRecord) {
        _viewingReceipt.value = record
    }

    fun closeReceipt() {
        _viewingReceipt.value = null
    }

    fun toggleSecurityCompliance(show: Boolean) {
        _showSecurityCompliance.value = show
    }

    fun showMessage(msg: String) {
        _snackbarMessage.value = msg
    }

    fun clearMessage() {
        _snackbarMessage.value = null
    }

    fun processPurchase(
        game: GameItem,
        gatewayType: PaymentGatewayType,
        paymentDescriptor: String,
        cryptoAmount: String? = null,
        cryptoCurrency: String? = null
    ): Boolean {
        val result = repository.purchaseGame(game, gatewayType, paymentDescriptor, cryptoAmount, cryptoCurrency)
        return if (result.isSuccess) {
            val tx = result.getOrNull()
            dismissCheckout()
            _selectedGameDetail.value = null
            _viewingReceipt.value = tx
            val successMsg = if (gatewayType == PaymentGatewayType.BINANCE_PAY && cryptoAmount != null) {
                "Binance Pay Confirmed! $cryptoAmount ${cryptoCurrency ?: "USDT"} received by User-96658. \"${game.title}\" unlocked!"
            } else if (gatewayType == PaymentGatewayType.UPI) {
                "UPI Payment Received! ₹${MerchantAccountConfig.getInrAmount(game.priceUsd)} credited to ${MerchantAccountConfig.PAYEE_NAME}. \"${game.title}\" unlocked!"
            } else {
                "Payment Confirmed! \"${game.title}\" unlocked & added to Library."
            }
            showMessage(successMsg)
            true
        } else {
            showMessage(result.exceptionOrNull()?.message ?: "Payment failed.")
            false
        }
    }

    fun processTopUp(
        amount: Double,
        gatewayType: PaymentGatewayType,
        paymentDescriptor: String,
        cryptoAmount: String? = null,
        cryptoCurrency: String? = null
    ): Boolean {
        val tx = repository.topUpWallet(amount, gatewayType, paymentDescriptor, cryptoAmount, cryptoCurrency)
        dismissCheckout()
        _viewingReceipt.value = tx
        val successMsg = if (gatewayType == PaymentGatewayType.BINANCE_PAY && cryptoAmount != null) {
            "Binance Pay Deposit! $cryptoAmount ${cryptoCurrency ?: "USDT"} credited directly to User-96658."
        } else if (gatewayType == PaymentGatewayType.UPI) {
            "UPI Top-Up Verified! ₹${MerchantAccountConfig.getInrAmount(amount)} received by Bank of Baroda."
        } else {
            "Wallet Loaded! $${String.format("%.2f", amount)} added securely."
        }
        showMessage(successMsg)
        return true
    }

    fun requestRefund(gameId: String) {
        val result = repository.refundGame(gameId)
        if (result.isSuccess) {
            val tx = result.getOrNull()
            _selectedGameDetail.value = null
            _viewingReceipt.value = tx
            showMessage("Refund Processed! Funds returned to your wallet.")
        } else {
            showMessage(result.exceptionOrNull()?.message ?: "Refund error.")
        }
    }

    fun startDownload(gameId: String) {
        repository.startDownload(gameId)
        showMessage("Download initiated with end-to-end checksum verification.")
    }

    fun pauseDownload(gameId: String) {
        repository.pauseDownload(gameId)
    }

    fun cancelDownload(gameId: String) {
        repository.cancelDownload(gameId)
        showMessage("Download cancelled.")
    }

    fun uninstallGame(gameId: String) {
        repository.uninstallGame(gameId)
        showMessage("Game package uninstalled.")
    }

    fun onGameRewardEarned(rewardUsd: Double, gameTitle: String) {
        repository.addRewardCoins(rewardUsd, gameTitle)
        showMessage("Reward Unlocked! +$${String.format("%.2f", rewardUsd)} credited to your wallet!")
    }

    fun onGameOverScore(gameId: String, score: Int, minutes: Int) {
        repository.recordGameScore(gameId, score, minutes)
    }

    fun openOwnerAdmin() {
        _showOwnerAdminDialog.value = true
    }

    fun closeOwnerAdmin() {
        _showOwnerAdminDialog.value = false
    }

    fun toggleOwnerMode(active: Boolean) {
        repository.setOwnerMode(active)
        if (active) {
            showMessage("👑 Owner VIP Active: All Games Unlocked Free for Gaming Club Administrator!")
        } else {
            showMessage("Customer Preview Mode: Regular Store Pricing Active")
        }
    }

    fun updateGamePrice(gameId: String, newPrice: Double) {
        repository.updateGamePrice(gameId, newPrice)
        showMessage("Price updated successfully by Owner.")
    }

    fun toggleGameFeatured(gameId: String) {
        repository.toggleGameFeatured(gameId)
        showMessage("Featured status updated by Owner.")
    }

    fun updateProfile(fullName: String, gamerTag: String, email: String) {
        repository.updateProfile(fullName, gamerTag, email)
        showMessage("Profile updated successfully")
    }

    fun updateAccountSetting(
        biometric: Boolean? = null,
        twoFactor: Boolean? = null,
        downloadWifiOnly: Boolean? = null,
        autoUpdate: Boolean? = null,
        highPerf: Boolean? = null,
        soundHaptics: Boolean? = null,
        notifications: Boolean? = null,
        defaultGateway: PaymentGatewayType? = null
    ) {
        repository.updateAccountSetting(
            biometric = biometric,
            twoFactor = twoFactor,
            downloadWifiOnly = downloadWifiOnly,
            autoUpdate = autoUpdate,
            highPerf = highPerf,
            soundHaptics = soundHaptics,
            notifications = notifications,
            defaultGateway = defaultGateway
        )
    }

    fun clearAppCache() {
        val size = repository.clearGameCache()
        showMessage("Cleared ${String.format(java.util.Locale.US, "%.1f", size)} MB cache")
    }
}

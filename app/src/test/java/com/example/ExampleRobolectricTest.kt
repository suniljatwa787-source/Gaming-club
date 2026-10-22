package com.example

import android.content.Context
import androidx.test.core.app.ApplicationProvider
import com.example.model.MerchantAccountConfig
import com.example.repository.GameStoreRepository
import com.example.viewmodel.AppNavTab
import com.example.viewmodel.MainViewModel
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNotNull
import org.junit.Assert.assertTrue
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import org.robolectric.annotation.Config

@RunWith(RobolectricTestRunner::class)
@Config(sdk = [36])
class ExampleRobolectricTest {

  @Test
  fun `read string from context`() {
    val context = ApplicationProvider.getApplicationContext<Context>()
    val appName = context.getString(R.string.app_name)
    assertEquals("PlayVault", appName)
  }

  @Test
  fun `user profile initializes with anonymous Gaming Club data`() {
    val repository = GameStoreRepository()
    val profile = repository.userProfile.value
    assertEquals("Gaming Club Member", profile.fullName)
    assertEquals("vip@gamingclub.io", profile.email)
    assertEquals("ClubChampion_787", profile.gamerTag)
    assertTrue(profile.isOwnerVerified)
    assertEquals(MerchantAccountConfig.PAYEE_UPI_ID, profile.linkedUpiId)
  }

  @Test
  fun `update profile and account settings works as expected`() {
    val repository = GameStoreRepository()
    val viewModel = MainViewModel(repository)

    viewModel.updateProfile("Club Member", "Pro_Apex787", "member@gamingclub.io")
    val updatedProfile = viewModel.userProfile.value
    assertEquals("Club Member", updatedProfile.fullName)
    assertEquals("Pro_Apex787", updatedProfile.gamerTag)

    viewModel.updateAccountSetting(downloadWifiOnly = true, highPerf = false)
    val settingsProfile = viewModel.userProfile.value
    assertTrue(settingsProfile.downloadWifiOnly)
    assertEquals(false, settingsProfile.highPerformanceMode)

    viewModel.clearAppCache()
    assertEquals(0.0, viewModel.userProfile.value.cacheSizeMb, 0.01)
  }

  @Test
  fun `owner VIP mode unlocks all games in user profile library`() {
    val repository = GameStoreRepository()
    val viewModel = MainViewModel(repository)

    assertTrue(viewModel.isOwnerModeActive.value)
    val ownedGames = viewModel.ownedGames.value
    assertEquals(repository.catalog.value.size, ownedGames.size)
  }

  @Test
  fun `transaction history contains ledger records`() {
    val repository = GameStoreRepository()
    val txs = repository.transactions.value
    assertTrue(txs.isNotEmpty())
    assertNotNull(txs.first().pciDssAuthCode)
    assertEquals(MerchantAccountConfig.PAYEE_UPI_ID, "gamingclub@barodampay")
    assertEquals(MerchantAccountConfig.BINANCE_PAY_ID, "User-96658")
    assertEquals(MerchantAccountConfig.BINANCE_PAYEE_NAME, "Gaming Club")
    assertEquals(MerchantAccountConfig.PAYEE_NAME, "Gaming Club")
  }

  @Test
  fun `binance pay cryptocurrency checkout generates valid settlement record`() {
    val repository = GameStoreRepository()
    val game = repository.catalog.value.first()
    val cryptoToken = com.example.model.CryptoToken.USDT
    val cryptoAmount = MerchantAccountConfig.formatCryptoAmount(game.priceUsd, cryptoToken)

    val result = repository.purchaseGame(
      game = game,
      gatewayType = com.example.model.PaymentGatewayType.BINANCE_PAY,
      paymentDescriptor = "Binance Pay (USDT) • User-96658",
      cryptoAmount = cryptoAmount,
      cryptoCurrency = "USDT"
    )

    assertTrue(result.isSuccess)
    val tx = result.getOrNull()
    assertNotNull(tx)
    assertEquals(com.example.model.PaymentGatewayType.BINANCE_PAY, tx?.gatewayType)
    assertEquals("User-96658", tx?.receivingAddress)
    assertEquals(cryptoAmount, tx?.cryptoAmount)
    assertEquals("USDT", tx?.cryptoCurrency)
    assertEquals(com.example.model.TransactionStatus.COMPLETED, tx?.status)
  }

  @Test
  fun `dual currency rate conversions support both INR and crypto`() {
    val usd = 10.0
    val inr = MerchantAccountConfig.getInrAmount(usd)
    assertEquals(840, inr)

    val usdt = MerchantAccountConfig.formatCryptoAmount(usd, com.example.model.CryptoToken.USDT)
    assertEquals("10.00", usdt)

    val uri = MerchantAccountConfig.getBinancePayUri(usd, com.example.model.CryptoToken.USDT)
    assertTrue(uri.contains("merchantId=User-96658"))
    assertTrue(uri.contains("currency=USDT"))
  }

  @Test
  fun `navigation tabs include profile tab`() {
    val viewModel = MainViewModel()
    viewModel.selectTab(AppNavTab.PROFILE)
    assertEquals(AppNavTab.PROFILE, viewModel.currentTab.value)
  }

  @Test
  fun `free games are present and claimable without charge`() {
    val repository = GameStoreRepository()
    val freeGames = repository.catalog.value.filter { it.isFree }
    assertTrue(freeGames.size >= 5)

    val offlineFreeGame = freeGames.find { it.playMode == com.example.model.PlayMode.OFFLINE }
    assertNotNull(offlineFreeGame)
    assertEquals(0.0, offlineFreeGame!!.priceUsd, 0.001)

    val onlineFreeGame = freeGames.find { it.playMode == com.example.model.PlayMode.ONLINE }
    assertNotNull(onlineFreeGame)
    assertEquals(0.0, onlineFreeGame!!.priceUsd, 0.001)

    val claimResult = repository.claimFreeGame(offlineFreeGame)
    assertTrue(claimResult.isSuccess)
    val ownership = repository.ownershipMap.value[offlineFreeGame.id]
    assertNotNull(ownership)
    assertTrue(ownership!!.isOwned)
    assertEquals(0.0, ownership.purchasePriceUsd, 0.001)
  }

  @Test
  fun `viewmodel filters free and offline categories accurately`() {
    val viewModel = MainViewModel()
    viewModel.selectCategory(com.example.model.GameCategory.FREE_GAMES)
    val freeFiltered = viewModel.getFilteredGames()
    assertTrue(freeFiltered.isNotEmpty())
    assertTrue(freeFiltered.all { it.isFree })

    viewModel.selectCategory(com.example.model.GameCategory.OFFLINE_GAMES)
    val offlineFiltered = viewModel.getFilteredGames()
    assertTrue(offlineFiltered.isNotEmpty())
    assertTrue(offlineFiltered.all { it.playMode == com.example.model.PlayMode.OFFLINE || it.playMode == com.example.model.PlayMode.BOTH })
  }
}



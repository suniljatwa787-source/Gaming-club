package com.example.model

data class UserAccountProfile(
    val fullName: String = "Gaming Club Member",
    val email: String = "vip@gamingclub.io",
    val gamerTag: String = "ClubChampion_787",
    val avatarEmoji: String = "👑",
    val playerLevel: Int = 42,
    val xpCurrent: Int = 8450,
    val xpNextLevel: Int = 10000,
    val isOwnerVerified: Boolean = true,
    val memberSince: String = "October 2024",
    // Account Settings
    val biometricAuthEnabled: Boolean = true,
    val twoFactorAuthEnabled: Boolean = true,
    val downloadWifiOnly: Boolean = false,
    val autoUpdateGames: Boolean = true,
    val highPerformanceMode: Boolean = true,
    val soundHapticsEnabled: Boolean = true,
    val pushNotificationsEnabled: Boolean = true,
    val defaultPaymentGateway: PaymentGatewayType = PaymentGatewayType.UPI,
    val linkedUpiId: String = MerchantAccountConfig.PAYEE_UPI_ID,
    val linkedBankName: String = MerchantAccountConfig.BANK_NAME,
    val linkedBinancePayId: String = MerchantAccountConfig.BINANCE_PAY_ID,
    val cacheSizeMb: Double = 142.8
)

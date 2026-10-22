package com.example.model

enum class PaymentGatewayType {
    UPI,
    BINANCE_PAY,
    CREDIT_CARD,
    PAYPAL,
    APPLE_PAY,
    GOOGLE_PAY,
    WALLET_BALANCE
}

enum class CryptoToken(
    val symbol: String,
    val tokenName: String,
    val network: String,
    val rateToUsd: Double,
    val decimals: Int
) {
    USDT("USDT", "Tether USD", "TRC-20 / BEP-20", 1.0, 2),
    BTC("BTC", "Bitcoin", "BTC Network", 68000.0, 6),
    ETH("ETH", "Ethereum", "Arbitrum / ERC-20", 3500.0, 5),
    BNB("BNB", "BNB Chain", "BEP-20", 590.0, 4),
    SOL("SOL", "Solana", "Solana SPL", 175.0, 3)
}

object MerchantAccountConfig {
    const val PAYEE_NAME = "Gaming Club"
    const val PAYEE_UPI_ID = "gamingclub@barodampay"
    const val BANK_NAME = "Bank of Baroda"
    const val USD_TO_INR_RATE = 84.0

    // Binance Pay Crypto Receiving Account (Automatic 0% Fee Payout)
    const val BINANCE_PAY_ID = "User-96658"
    const val BINANCE_PAYEE_NAME = "Gaming Club"
    const val BINANCE_APP_DEEPLINK = "binancepay://pay?merchantId=User-96658"

    fun getInrAmount(usdAmount: Double): Int {
        return kotlin.math.round(usdAmount * USD_TO_INR_RATE).toInt()
    }

    fun formatCryptoAmount(usdAmount: Double, token: CryptoToken): String {
        val cryptoVal = usdAmount / token.rateToUsd
        return String.format(java.util.Locale.US, "%.${token.decimals}f", cryptoVal)
    }

    fun getUpiUri(amountInr: Int, transactionRef: String, note: String): String {
        val encodedName = java.net.URLEncoder.encode(PAYEE_NAME, "UTF-8")
        val encodedNote = java.net.URLEncoder.encode(note, "UTF-8")
        return "upi://pay?pa=$PAYEE_UPI_ID&pn=$encodedName&am=$amountInr&cu=INR&tn=$encodedNote&tr=$transactionRef"
    }

    fun getBinancePayUri(amountUsd: Double, token: CryptoToken = CryptoToken.USDT): String {
        val cryptoAmt = formatCryptoAmount(amountUsd, token)
        return "binancepay://pay?merchantId=$BINANCE_PAY_ID&amount=$cryptoAmt&currency=${token.symbol}"
    }
}

enum class CardBrand(val displayName: String) {
    VISA("Visa"),
    MASTERCARD("MasterCard"),
    AMEX("American Express"),
    DISCOVER("Discover"),
    UNKNOWN("Card")
}

data class SavedPaymentCard(
    val id: String,
    val cardholderName: String,
    val lastFour: String,
    val brand: CardBrand,
    val expiryMonthYear: String,
    val isDefault: Boolean = false,
    val tokenizedHash: String
)

enum class TransactionType {
    GAME_PURCHASE,
    WALLET_TOPUP,
    GAME_REFUND,
    COIN_REWARD,
    OWNER_WITHDRAWAL
}

enum class TransactionStatus {
    COMPLETED,
    REFUNDED,
    FAILED
}

data class TransactionRecord(
    val id: String,
    val title: String,
    val type: TransactionType,
    val amountUsd: Double,
    val timestamp: Long,
    val gatewayType: PaymentGatewayType,
    val methodDescriptor: String,
    val status: TransactionStatus,
    val encryptionToken: String,
    val pciDssAuthCode: String,
    val receiptId: String,
    val gameId: String? = null,
    val cryptoAmount: String? = null,
    val cryptoCurrency: String? = null,
    val receivingAddress: String? = null
)

data class PaymentSecurityReport(
    val encryptionCipher: String = "AES-256-GCM / TLS 1.3",
    val pciDssStatus: String = "PCI-DSS Level 1 Certified",
    val tokenizationService: String = "VaultKey Secure Enclave v4.2",
    val fraudRiskScore: String = "0.01 (Extremely Low)",
    val threeDSecureStatus: String = "3DS 2.0 Biometric / OTP Challenge Supported"
)


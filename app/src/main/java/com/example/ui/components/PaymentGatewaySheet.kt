package com.example.ui.components

import android.content.Intent
import android.net.Uri
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.AccountBalanceWallet
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.ContentCopy
import androidx.compose.material.icons.filled.CreditCard
import androidx.compose.material.icons.filled.Fingerprint
import androidx.compose.material.icons.filled.FlashOn
import androidx.compose.material.icons.filled.Lock
import androidx.compose.material.icons.filled.OpenInNew
import androidx.compose.material.icons.filled.QrCodeScanner
import androidx.compose.material.icons.filled.Security
import androidx.compose.material.icons.filled.Shield
import androidx.compose.material.icons.filled.VerifiedUser
import androidx.compose.ui.platform.LocalClipboardManager
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.AnnotatedString
import com.example.R
import com.example.model.MerchantAccountConfig
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Switch
import androidx.compose.material3.SwitchDefaults
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.rememberModalBottomSheetState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.model.CardBrand
import com.example.model.PaymentGatewayType
import com.example.model.SavedPaymentCard
import com.example.security.PaymentSecurityManager
import com.example.ui.theme.ApplePayBlack
import com.example.ui.theme.CardBorderStroke
import com.example.ui.theme.CardSurfaceDark
import com.example.ui.theme.CyberCyanPrimary
import com.example.ui.theme.DeepNavySurface
import com.example.ui.theme.ElectricViolet
import com.example.ui.theme.EmeraldSuccess
import com.example.ui.theme.GoldCurrency
import com.example.ui.theme.GooglePayDark
import com.example.ui.theme.PayPalBlue
import com.example.ui.theme.TextMutedDark
import com.example.ui.theme.TextPrimaryDark
import com.example.ui.theme.TextSecondaryDark
import com.example.viewmodel.PaymentIntent
import com.example.model.CryptoToken
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import java.util.Locale

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun PaymentGatewaySheet(
    intent: PaymentIntent,
    walletBalance: Double,
    savedCards: List<SavedPaymentCard>,
    onDismiss: () -> Unit,
    onConfirmPayment: (PaymentGatewayType, String, String?, String?) -> Boolean
) {
    val sheetState = rememberModalBottomSheetState(skipPartiallyExpanded = true)
    val coroutineScope = rememberCoroutineScope()

    var selectedGateway by remember { mutableStateOf(PaymentGatewayType.UPI) }
    var upiUtrNumber by remember { mutableStateOf("") }
    var upiCopiedToast by remember { mutableStateOf(false) }

    // Binance Pay Crypto State
    var selectedCryptoToken by remember { mutableStateOf(CryptoToken.USDT) }
    var binanceTxId by remember { mutableStateOf("") }
    var binanceCopiedToast by remember { mutableStateOf(false) }

    // Credit Card Form State
    var cardNumber by remember { mutableStateOf("") }
    var cardholderName by remember { mutableStateOf("") }
    var expiryDate by remember { mutableStateOf("") }
    var cvv by remember { mutableStateOf("") }
    var saveCardSecurely by remember { mutableStateOf(true) }
    var detectedBrand by remember { mutableStateOf(CardBrand.UNKNOWN) }

    // PayPal state
    var paypalEmail by remember { mutableStateOf("alex.hunter@gamer.net") }

    // Security & 3DS challenge state
    var isProcessing by remember { mutableStateOf(false) }
    var processingStage by remember { mutableStateOf("") }
    var show3DsOtpModal by remember { mutableStateOf(false) }
    var otpCode by remember { mutableStateOf("") }
    var errorMessage by remember { mutableStateOf<String?>(null) }

    val totalAmount = when (intent) {
        is PaymentIntent.BuyGame -> intent.game.priceUsd
        is PaymentIntent.TopUpWallet -> intent.amountUsd
    }

    val itemTitle = when (intent) {
        is PaymentIntent.BuyGame -> intent.game.title
        is PaymentIntent.TopUpWallet -> "Wallet Deposit Funds"
    }

    LaunchedEffect(cardNumber) {
        detectedBrand = PaymentSecurityManager.detectCardBrand(cardNumber)
    }

    ModalBottomSheet(
        onDismissRequest = onDismiss,
        sheetState = sheetState,
        containerColor = DeepNavySurface,
        dragHandle = null,
        modifier = Modifier
            .fillMaxWidth()
            .testTag("payment_gateway_bottom_sheet")
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .verticalScroll(rememberScrollState())
                .padding(20.dp)
                .navigationBarsPadding()
        ) {
            // Header: Title and Close button
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Box(
                        modifier = Modifier
                            .size(36.dp)
                            .background(CyberCyanPrimary.copy(alpha = 0.15f), CircleShape),
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(
                            imageVector = Icons.Default.Shield,
                            contentDescription = "Shield",
                            tint = CyberCyanPrimary,
                            modifier = Modifier.size(20.dp)
                        )
                    }
                    Spacer(modifier = Modifier.width(10.dp))
                    Column {
                        Text(
                            text = "Secure Payment Gateway",
                            color = TextPrimaryDark,
                            fontSize = 18.sp,
                            fontWeight = FontWeight.Bold
                        )
                        Text(
                            text = "256-Bit Encrypted • PCI-DSS Certified",
                            color = EmeraldSuccess,
                            fontSize = 11.sp,
                            fontWeight = FontWeight.SemiBold
                        )
                    }
                }

                IconButton(
                    onClick = onDismiss,
                    modifier = Modifier.testTag("dismiss_checkout_button")
                ) {
                    Icon(
                        imageVector = Icons.Default.Close,
                        contentDescription = "Close",
                        tint = TextSecondaryDark
                    )
                }
            }

            Spacer(modifier = Modifier.height(14.dp))

            // Order summary card
            Card(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(14.dp),
                colors = CardDefaults.cardColors(containerColor = CardSurfaceDark),
                border = BorderStroke(1.dp, CardBorderStroke)
            ) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(14.dp),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Column(modifier = Modifier.weight(1f)) {
                        Text(
                            text = when (intent) {
                                is PaymentIntent.BuyGame -> "GAME LICENSE PURCHASE"
                                is PaymentIntent.TopUpWallet -> "VAULT CASH DEPOSIT"
                            },
                            color = CyberCyanPrimary,
                            fontSize = 11.sp,
                            fontWeight = FontWeight.Bold,
                            letterSpacing = 1.sp
                        )
                        Text(
                            text = itemTitle,
                            color = TextPrimaryDark,
                            fontSize = 15.sp,
                            fontWeight = FontWeight.Bold,
                            maxLines = 1
                        )
                        if (intent is PaymentIntent.BuyGame) {
                            Text(
                                text = "Instant Play + Full Download (${intent.game.downloadSizeMb} MB)",
                                color = TextSecondaryDark,
                                fontSize = 12.sp
                            )
                        }
                    }

                    Column(horizontalAlignment = Alignment.End) {
                        Text(
                            text = "$${String.format(Locale.US, "%.2f", totalAmount)}",
                            color = GoldCurrency,
                            fontSize = 22.sp,
                            fontWeight = FontWeight.ExtraBold
                        )
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Text(
                                text = "₹${MerchantAccountConfig.getInrAmount(totalAmount)}",
                                color = CyberCyanPrimary,
                                fontSize = 12.sp,
                                fontWeight = FontWeight.Bold
                            )
                            Text(
                                text = " • ",
                                color = TextSecondaryDark,
                                fontSize = 12.sp
                            )
                            Text(
                                text = "${MerchantAccountConfig.formatCryptoAmount(totalAmount, selectedCryptoToken)} ${selectedCryptoToken.symbol}",
                                color = Color(0xFFF0B90B),
                                fontSize = 12.sp,
                                fontWeight = FontWeight.Bold
                            )
                        }
                    }
                }

                Spacer(modifier = Modifier.height(10.dp))

                // Dual Payout Assurance Badge
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .background(DeepNavySurface, RoundedCornerShape(8.dp))
                        .border(1.dp, CardBorderStroke, RoundedCornerShape(8.dp))
                        .padding(horizontal = 10.dp, vertical = 6.dp),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Icon(
                            imageVector = Icons.Default.VerifiedUser,
                            contentDescription = "Verified Direct Payee",
                            tint = EmeraldSuccess,
                            modifier = Modifier.size(13.dp)
                        )
                        Spacer(modifier = Modifier.width(6.dp))
                        Text(
                            text = "Direct Settlement: INR (UPI) & Crypto (Binance Pay)",
                            color = TextSecondaryDark,
                            fontSize = 11.sp,
                            fontWeight = FontWeight.Medium
                        )
                    }
                    Text(
                        text = "0% Fee",
                        color = EmeraldSuccess,
                        fontSize = 11.sp,
                        fontWeight = FontWeight.Bold
                    )
                }
            }

            Spacer(modifier = Modifier.height(16.dp))

            // Payment Gateway Tabs (UPI, Binance Pay, Credit Card, PayPal, Apple Pay, Google Pay, Wallet)
            Text(
                text = "SELECT PAYMENT METHOD",
                color = TextSecondaryDark,
                fontSize = 12.sp,
                fontWeight = FontWeight.Bold,
                letterSpacing = 1.sp
            )

            Spacer(modifier = Modifier.height(8.dp))

            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .horizontalScroll(rememberScrollState()),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                PaymentMethodPill(
                    title = "⚡ UPI / INR (₹)",
                    icon = Icons.Default.QrCodeScanner,
                    isSelected = selectedGateway == PaymentGatewayType.UPI,
                    onClick = { selectedGateway = PaymentGatewayType.UPI }
                )
                PaymentMethodPill(
                    title = "💎 Binance Pay (Crypto)",
                    icon = Icons.Default.FlashOn,
                    isSelected = selectedGateway == PaymentGatewayType.BINANCE_PAY,
                    onClick = { selectedGateway = PaymentGatewayType.BINANCE_PAY }
                )
                PaymentMethodPill(
                    title = "Credit Card",
                    icon = Icons.Default.CreditCard,
                    isSelected = selectedGateway == PaymentGatewayType.CREDIT_CARD,
                    onClick = { selectedGateway = PaymentGatewayType.CREDIT_CARD }
                )
                PaymentMethodPill(
                    title = "PayPal",
                    icon = Icons.Default.VerifiedUser,
                    isSelected = selectedGateway == PaymentGatewayType.PAYPAL,
                    onClick = { selectedGateway = PaymentGatewayType.PAYPAL }
                )
                PaymentMethodPill(
                    title = "Apple Pay",
                    icon = Icons.Default.Fingerprint,
                    isSelected = selectedGateway == PaymentGatewayType.APPLE_PAY,
                    onClick = { selectedGateway = PaymentGatewayType.APPLE_PAY }
                )
                PaymentMethodPill(
                    title = "Google Pay",
                    icon = Icons.Default.FlashOn,
                    isSelected = selectedGateway == PaymentGatewayType.GOOGLE_PAY,
                    onClick = { selectedGateway = PaymentGatewayType.GOOGLE_PAY }
                )
                if (intent is PaymentIntent.BuyGame) {
                    PaymentMethodPill(
                        title = "Vault Wallet ($${String.format(Locale.US, "%.2f", walletBalance)})",
                        icon = Icons.Default.AccountBalanceWallet,
                        isSelected = selectedGateway == PaymentGatewayType.WALLET_BALANCE,
                        onClick = { selectedGateway = PaymentGatewayType.WALLET_BALANCE }
                    )
                }
            }

            Spacer(modifier = Modifier.height(16.dp))

            // Gateway specific inputs
            when (selectedGateway) {
                PaymentGatewayType.UPI -> {
                    UpiMerchantSection(
                        totalAmountUsd = totalAmount,
                        itemTitle = itemTitle,
                        utrNumber = upiUtrNumber,
                        onUtrNumberChange = { upiUtrNumber = it },
                        onCopiedFeedback = {
                            coroutineScope.launch {
                                upiCopiedToast = true
                                delay(2000)
                                upiCopiedToast = false
                            }
                        },
                        isCopied = upiCopiedToast
                    )
                }
                PaymentGatewayType.BINANCE_PAY -> {
                    CryptoBinanceMerchantSection(
                        totalAmountUsd = totalAmount,
                        itemTitle = itemTitle,
                        selectedToken = selectedCryptoToken,
                        onSelectToken = { selectedCryptoToken = it },
                        txId = binanceTxId,
                        onTxIdChange = { binanceTxId = it },
                        onCopiedFeedback = {
                            coroutineScope.launch {
                                binanceCopiedToast = true
                                delay(2000)
                                binanceCopiedToast = false
                            }
                        },
                        isCopied = binanceCopiedToast
                    )
                }
                PaymentGatewayType.CREDIT_CARD -> {
                    CreditCardForm(
                        cardNumber = cardNumber,
                        onCardNumberChange = { cardNumber = it },
                        cardholderName = cardholderName,
                        onCardholderNameChange = { cardholderName = it },
                        expiryDate = expiryDate,
                        onExpiryDateChange = { expiryDate = it },
                        cvv = cvv,
                        onCvvChange = { cvv = it },
                        detectedBrand = detectedBrand,
                        saveCardSecurely = saveCardSecurely,
                        onSaveCardChange = { saveCardSecurely = it },
                        onQuickFillTestCard = {
                            cardNumber = "4242 4242 4242 4242"
                            cardholderName = "Alex Hunter"
                            expiryDate = "12/28"
                            cvv = "842"
                            errorMessage = null
                        }
                    )
                }
                PaymentGatewayType.PAYPAL -> {
                    PayPalSection(
                        email = paypalEmail,
                        onEmailChange = { paypalEmail = it }
                    )
                }
                PaymentGatewayType.APPLE_PAY -> {
                    DigitalWalletSection(
                        walletName = "Apple Pay",
                        containerColor = ApplePayBlack,
                        iconColor = Color.White,
                        description = "Touch ID / Face ID authorized. Encrypted Device Account Number (•••• 8912) protects real card information."
                    )
                }
                PaymentGatewayType.GOOGLE_PAY -> {
                    DigitalWalletSection(
                        walletName = "Google Pay",
                        containerColor = GooglePayDark,
                        iconColor = CyberCyanPrimary,
                        description = "Fast 1-tap checkout with Google Payments. Zero plain numbers shared with merchant."
                    )
                }
                PaymentGatewayType.WALLET_BALANCE -> {
                    VaultBalanceSection(
                        walletBalance = walletBalance,
                        totalAmount = totalAmount
                    )
                }
            }

            // Error display
            if (errorMessage != null) {
                Spacer(modifier = Modifier.height(10.dp))
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(8.dp),
                    colors = CardDefaults.cardColors(containerColor = Color(0xFF451A1A))
                ) {
                    Text(
                        text = errorMessage ?: "",
                        color = Color(0xFFFF8080),
                        fontSize = 13.sp,
                        modifier = Modifier.padding(10.dp)
                    )
                }
            }

            Spacer(modifier = Modifier.height(16.dp))

            // 3D Secure Modal Challenge simulation
            AnimatedVisibility(visible = show3DsOtpModal) {
                Card(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(bottom = 12.dp),
                    shape = RoundedCornerShape(14.dp),
                    colors = CardDefaults.cardColors(containerColor = CardSurfaceDark),
                    border = BorderStroke(1.5.dp, CyberCyanPrimary)
                ) {
                    Column(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(14.dp)
                    ) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Icon(
                                imageVector = Icons.Default.Security,
                                contentDescription = "3DS",
                                tint = CyberCyanPrimary,
                                modifier = Modifier.size(20.dp)
                            )
                            Spacer(modifier = Modifier.width(6.dp))
                            Text(
                                text = "3D-Secure 2.0 Bank Verification",
                                color = TextPrimaryDark,
                                fontSize = 14.sp,
                                fontWeight = FontWeight.Bold
                            )
                        }
                        Text(
                            text = "A one-time authorization SMS code was sent to +1 (•••) •••-8821",
                            color = TextSecondaryDark,
                            fontSize = 12.sp,
                            modifier = Modifier.padding(vertical = 4.dp)
                        )
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(8.dp)
                        ) {
                            OutlinedTextField(
                                value = otpCode,
                                onValueChange = { if (it.length <= 6) otpCode = it },
                                placeholder = { Text("6-digit code", fontSize = 13.sp) },
                                modifier = Modifier
                                    .weight(1f)
                                    .testTag("otp_code_input"),
                                colors = OutlinedTextFieldDefaults.colors(
                                    focusedBorderColor = CyberCyanPrimary,
                                    unfocusedBorderColor = CardBorderStroke
                                ),
                                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                                singleLine = true
                            )
                            Button(
                                onClick = { otpCode = "849201" },
                                colors = ButtonDefaults.buttonColors(containerColor = ElectricViolet),
                                modifier = Modifier.testTag("autofill_otp_button")
                            ) {
                                Text("Auto-fill (849201)", fontSize = 11.sp)
                            }
                        }
                    }
                }
            }

            // Primary Pay Button
            Button(
                onClick = {
                    errorMessage = null
                    // Input Validation
                    when (selectedGateway) {
                        PaymentGatewayType.UPI -> {
                            if (upiUtrNumber.isNotBlank() && upiUtrNumber.length < 6) {
                                errorMessage = "Please enter valid 12-digit UPI UTR or tap Auto-fill."
                                return@Button
                            }
                        }
                        PaymentGatewayType.CREDIT_CARD -> {
                            val cleanNumber = cardNumber.replace(" ", "")
                            if (!PaymentSecurityManager.validateCardNumber(cleanNumber)) {
                                errorMessage = "Invalid card number (Luhn Check failed). Use 4242... test card."
                                return@Button
                            }
                            if (cardholderName.isBlank()) {
                                errorMessage = "Please enter cardholder name."
                                return@Button
                            }
                            if (!PaymentSecurityManager.validateExpiry(expiryDate)) {
                                errorMessage = "Invalid expiry date (MM/YY in future required)."
                                return@Button
                            }
                            if (!PaymentSecurityManager.validateCvv(cvv, detectedBrand)) {
                                errorMessage = "Invalid CVV (3 digits or 4 for Amex)."
                                return@Button
                            }

                            // Trigger 3DS challenge if not yet shown
                            if (!show3DsOtpModal) {
                                show3DsOtpModal = true
                                return@Button
                            }
                            if (otpCode.length < 6) {
                                errorMessage = "Please enter the 6-digit 3DS verification code (or tap Auto-fill)."
                                return@Button
                            }
                        }
                        PaymentGatewayType.PAYPAL -> {
                            if (!paypalEmail.contains("@") || !paypalEmail.contains(".")) {
                                errorMessage = "Please enter a valid PayPal email address."
                                return@Button
                            }
                        }
                        PaymentGatewayType.WALLET_BALANCE -> {
                            if (walletBalance < totalAmount) {
                                errorMessage = "Insufficient wallet balance ($${String.format(Locale.US, "%.2f", walletBalance)})."
                                return@Button
                            }
                        }
                        else -> { /* Apple Pay / Google Pay simulated instant biometric auth */ }
                    }

                    // Execute payment simulation with cryptographic stages
                    isProcessing = true
                    coroutineScope.launch {
                        if (selectedGateway == PaymentGatewayType.BINANCE_PAY) {
                            val cryptoFormatted = MerchantAccountConfig.formatCryptoAmount(totalAmount, selectedCryptoToken)
                            processingStage = "Connecting to Binance Pay Switch..."
                            delay(400)
                            processingStage = "Verifying $cryptoFormatted ${selectedCryptoToken.symbol} deposit..."
                            delay(450)
                            processingStage = "Payment Received by Binance Pay ID User-96658!"
                            delay(350)
                        } else if (selectedGateway == PaymentGatewayType.UPI) {
                            processingStage = "Connecting to Bank of Baroda UPI Switch..."
                            delay(400)
                            processingStage = "Verifying transfer to ${MerchantAccountConfig.PAYEE_UPI_ID}..."
                            delay(450)
                            processingStage = "Payment Received by Gaming Club!"
                            delay(350)
                        } else {
                            processingStage = "Encrypting payload via AES-256-GCM..."
                            delay(450)
                            processingStage = "Connecting to PCI-DSS Level 1 Gateway..."
                            delay(550)
                            processingStage = "Generating tokenized receipt..."
                            delay(400)
                        }
                        isProcessing = false

                        val (cryptoAmt, cryptoSym) = if (selectedGateway == PaymentGatewayType.BINANCE_PAY) {
                            MerchantAccountConfig.formatCryptoAmount(totalAmount, selectedCryptoToken) to selectedCryptoToken.symbol
                        } else {
                            null to null
                        }

                        val descriptor = when (selectedGateway) {
                            PaymentGatewayType.BINANCE_PAY -> "Binance Pay (${selectedCryptoToken.symbol}) • User-96658"
                            PaymentGatewayType.UPI -> "UPI • Gaming Club (${MerchantAccountConfig.PAYEE_UPI_ID})"
                            PaymentGatewayType.CREDIT_CARD -> "${detectedBrand.displayName} •••• ${cardNumber.takeLast(4).ifEmpty { "4242" }}"
                            PaymentGatewayType.PAYPAL -> "PayPal ($paypalEmail)"
                            PaymentGatewayType.APPLE_PAY -> "Apple Pay (Device •••• 8912)"
                            PaymentGatewayType.GOOGLE_PAY -> "Google Pay (Device •••• 4102)"
                            PaymentGatewayType.WALLET_BALANCE -> "PlayVault Wallet Balance"
                        }
                        onConfirmPayment(selectedGateway, descriptor, cryptoAmt, cryptoSym)
                    }
                },
                enabled = !isProcessing,
                modifier = Modifier
                    .fillMaxWidth()
                    .height(54.dp)
                    .testTag("confirm_payment_button"),
                colors = ButtonDefaults.buttonColors(
                    containerColor = when (selectedGateway) {
                        PaymentGatewayType.BINANCE_PAY -> Color(0xFFF0B90B)
                        PaymentGatewayType.UPI -> Color(0xFF00897B)
                        PaymentGatewayType.PAYPAL -> PayPalBlue
                        PaymentGatewayType.APPLE_PAY -> Color.White
                        else -> CyberCyanPrimary
                    }
                ),
                shape = RoundedCornerShape(12.dp)
            ) {
                if (isProcessing) {
                    CircularProgressIndicator(
                        modifier = Modifier.size(24.dp),
                        color = if (selectedGateway == PaymentGatewayType.UPI) Color.White else DeepNavySurface,
                        strokeWidth = 2.dp
                    )
                    Spacer(modifier = Modifier.width(10.dp))
                    Text(
                        text = processingStage,
                        color = if (selectedGateway == PaymentGatewayType.UPI) Color.White else DeepNavySurface,
                        fontWeight = FontWeight.Bold,
                        fontSize = 12.sp
                    )
                } else {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Icon(
                            imageVector = Icons.Default.Lock,
                            contentDescription = "Lock",
                            tint = when (selectedGateway) {
                                PaymentGatewayType.UPI -> Color.White
                                PaymentGatewayType.APPLE_PAY -> ApplePayBlack
                                else -> DeepNavySurface
                            },
                            modifier = Modifier.size(18.dp)
                        )
                        Spacer(modifier = Modifier.width(8.dp))
                        Text(
                            text = if (selectedGateway == PaymentGatewayType.BINANCE_PAY) {
                                "Verify & Receive ${MerchantAccountConfig.formatCryptoAmount(totalAmount, selectedCryptoToken)} ${selectedCryptoToken.symbol} ($${String.format(Locale.US, "%.2f", totalAmount)})"
                            } else if (selectedGateway == PaymentGatewayType.UPI) {
                                "Verify & Receive ₹${MerchantAccountConfig.getInrAmount(totalAmount)} ($${String.format(Locale.US, "%.2f", totalAmount)})"
                            } else if (show3DsOtpModal && selectedGateway == PaymentGatewayType.CREDIT_CARD) {
                                "Verify & Complete $${String.format(Locale.US, "%.2f", totalAmount)}"
                            } else {
                                "Authorize & Pay $${String.format(Locale.US, "%.2f", totalAmount)}"
                            },
                            color = when (selectedGateway) {
                                PaymentGatewayType.UPI -> Color.White
                                PaymentGatewayType.APPLE_PAY -> ApplePayBlack
                                else -> DeepNavySurface
                            },
                            fontSize = 15.sp,
                            fontWeight = FontWeight.ExtraBold
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.height(12.dp))

            // Industry standard compliance footer
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.Center,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Icon(
                    imageVector = Icons.Default.VerifiedUser,
                    contentDescription = "PCI",
                    tint = EmeraldSuccess,
                    modifier = Modifier.size(14.dp)
                )
                Spacer(modifier = Modifier.width(4.dp))
                Text(
                    text = "Zero Fraud Liability • 100% End-to-End Encrypted",
                    color = TextMutedDark,
                    fontSize = 11.sp
                )
            }
        }
    }
}

@Composable
private fun PaymentMethodPill(
    title: String,
    icon: androidx.compose.ui.graphics.vector.ImageVector,
    isSelected: Boolean,
    onClick: () -> Unit
) {
    Box(
        modifier = Modifier
            .clip(RoundedCornerShape(12.dp))
            .background(if (isSelected) CyberCyanPrimary.copy(alpha = 0.2f) else CardSurfaceDark)
            .border(
                1.dp,
                if (isSelected) CyberCyanPrimary else CardBorderStroke,
                RoundedCornerShape(12.dp)
            )
            .clickable(onClick = onClick)
            .padding(horizontal = 14.dp, vertical = 10.dp)
    ) {
        Row(verticalAlignment = Alignment.CenterVertically) {
            Icon(
                imageVector = icon,
                contentDescription = title,
                tint = if (isSelected) CyberCyanPrimary else TextSecondaryDark,
                modifier = Modifier.size(16.dp)
            )
            Spacer(modifier = Modifier.width(6.dp))
            Text(
                text = title,
                color = if (isSelected) TextPrimaryDark else TextSecondaryDark,
                fontSize = 13.sp,
                fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Medium
            )
        }
    }
}

@Composable
private fun CreditCardForm(
    cardNumber: String,
    onCardNumberChange: (String) -> Unit,
    cardholderName: String,
    onCardholderNameChange: (String) -> Unit,
    expiryDate: String,
    onExpiryDateChange: (String) -> Unit,
    cvv: String,
    onCvvChange: (String) -> Unit,
    detectedBrand: CardBrand,
    saveCardSecurely: Boolean,
    onSaveCardChange: (Boolean) -> Unit,
    onQuickFillTestCard: () -> Unit
) {
    Column(
        modifier = Modifier.fillMaxWidth(),
        verticalArrangement = Arrangement.spacedBy(10.dp)
    ) {
        // Quick Fill Helper Banner
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(
                text = "CARD DETAILS",
                color = TextSecondaryDark,
                fontSize = 11.sp,
                fontWeight = FontWeight.Bold,
                letterSpacing = 1.sp
            )
            TextButton(
                onClick = onQuickFillTestCard,
                modifier = Modifier.testTag("fill_test_card_button")
            ) {
                Text(
                    text = "⚡ Fill Test Card (Visa)",
                    color = CyberCyanPrimary,
                    fontSize = 12.sp,
                    fontWeight = FontWeight.Bold
                )
            }
        }

        // Card number input
        OutlinedTextField(
            value = cardNumber,
            onValueChange = { raw ->
                val digitsOnly = raw.filter { it.isDigit() }.take(19)
                // Format with spaces every 4 digits
                val formatted = digitsOnly.chunked(4).joinToString(" ")
                onCardNumberChange(formatted)
            },
            label = { Text("Card Number") },
            placeholder = { Text("4242 4242 4242 4242") },
            trailingIcon = {
                if (detectedBrand != CardBrand.UNKNOWN) {
                    Box(
                        modifier = Modifier
                            .background(ElectricViolet.copy(alpha = 0.2f), RoundedCornerShape(6.dp))
                            .border(1.dp, ElectricViolet, RoundedCornerShape(6.dp))
                            .padding(horizontal = 8.dp, vertical = 4.dp)
                    ) {
                        Text(
                            text = detectedBrand.displayName,
                            color = CyberCyanPrimary,
                            fontSize = 11.sp,
                            fontWeight = FontWeight.Bold
                        )
                    }
                }
            },
            modifier = Modifier
                .fillMaxWidth()
                .testTag("card_number_input"),
            colors = OutlinedTextFieldDefaults.colors(
                focusedBorderColor = CyberCyanPrimary,
                unfocusedBorderColor = CardBorderStroke
            ),
            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
            singleLine = true
        )

        // Cardholder name
        OutlinedTextField(
            value = cardholderName,
            onValueChange = onCardholderNameChange,
            label = { Text("Cardholder Name") },
            placeholder = { Text("e.g. Alex Hunter") },
            modifier = Modifier
                .fillMaxWidth()
                .testTag("cardholder_name_input"),
            colors = OutlinedTextFieldDefaults.colors(
                focusedBorderColor = CyberCyanPrimary,
                unfocusedBorderColor = CardBorderStroke
            ),
            singleLine = true
        )

        // Expiry & CVV row
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(10.dp)
        ) {
            OutlinedTextField(
                value = expiryDate,
                onValueChange = { raw ->
                    val digits = raw.filter { it.isDigit() }.take(4)
                    val formatted = if (digits.length >= 3) {
                        "${digits.take(2)}/${digits.drop(2)}"
                    } else digits
                    onExpiryDateChange(formatted)
                },
                label = { Text("Expiry (MM/YY)") },
                placeholder = { Text("12/28") },
                modifier = Modifier
                    .weight(1f)
                    .testTag("card_expiry_input"),
                colors = OutlinedTextFieldDefaults.colors(
                    focusedBorderColor = CyberCyanPrimary,
                    unfocusedBorderColor = CardBorderStroke
                ),
                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                singleLine = true
            )

            OutlinedTextField(
                value = cvv,
                onValueChange = { raw ->
                    val maxLen = if (detectedBrand == CardBrand.AMEX) 4 else 3
                    onCvvChange(raw.filter { it.isDigit() }.take(maxLen))
                },
                label = { Text("CVV") },
                placeholder = { Text("123") },
                visualTransformation = PasswordVisualTransformation(),
                modifier = Modifier
                    .weight(1f)
                    .testTag("card_cvv_input"),
                colors = OutlinedTextFieldDefaults.colors(
                    focusedBorderColor = CyberCyanPrimary,
                    unfocusedBorderColor = CardBorderStroke
                ),
                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.NumberPassword),
                singleLine = true
            )
        }

        // Save card checkbox
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .clip(RoundedCornerShape(8.dp))
                .clickable { onSaveCardChange(!saveCardSecurely) }
                .padding(vertical = 4.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Switch(
                checked = saveCardSecurely,
                onCheckedChange = onSaveCardChange,
                colors = SwitchDefaults.colors(checkedThumbColor = CyberCyanPrimary)
            )
            Spacer(modifier = Modifier.width(10.dp))
            Column {
                Text(
                    text = "Save encrypted in VaultKey Enclave",
                    color = TextPrimaryDark,
                    fontSize = 13.sp,
                    fontWeight = FontWeight.Medium
                )
                Text(
                    text = "Raw card details never stored on device",
                    color = TextSecondaryDark,
                    fontSize = 11.sp
                )
            }
        }
    }
}

@Composable
private fun PayPalSection(
    email: String,
    onEmailChange: (String) -> Unit
) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(14.dp),
        colors = CardDefaults.cardColors(containerColor = CardSurfaceDark),
        border = BorderStroke(1.dp, PayPalBlue.copy(alpha = 0.5f))
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Box(
                    modifier = Modifier
                        .size(32.dp)
                        .background(PayPalBlue, CircleShape),
                    contentAlignment = Alignment.Center
                ) {
                    Text("P", color = Color.White, fontWeight = FontWeight.ExtraBold, fontSize = 18.sp)
                }
                Spacer(modifier = Modifier.width(10.dp))
                Column {
                    Text(
                        text = "PayPal Express Checkout",
                        color = Color.White,
                        fontSize = 15.sp,
                        fontWeight = FontWeight.Bold
                    )
                    Text(
                        text = "Encrypted digital wallet with Buyer Protection",
                        color = TextSecondaryDark,
                        fontSize = 11.sp
                    )
                }
            }

            Spacer(modifier = Modifier.height(14.dp))

            OutlinedTextField(
                value = email,
                onValueChange = onEmailChange,
                label = { Text("PayPal Account Email") },
                modifier = Modifier
                    .fillMaxWidth()
                    .testTag("paypal_email_input"),
                colors = OutlinedTextFieldDefaults.colors(
                    focusedBorderColor = PayPalBlue,
                    unfocusedBorderColor = CardBorderStroke
                ),
                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Email),
                singleLine = true
            )

            Spacer(modifier = Modifier.height(8.dp))

            Row(verticalAlignment = Alignment.CenterVertically) {
                Icon(
                    imageVector = Icons.Default.Check,
                    contentDescription = "Verified",
                    tint = EmeraldSuccess,
                    modifier = Modifier.size(14.dp)
                )
                Spacer(modifier = Modifier.width(4.dp))
                Text(
                    text = "Instant 1-Click Sandbox Authorization enabled",
                    color = EmeraldSuccess,
                    fontSize = 11.sp
                )
            }
        }
    }
}

@Composable
private fun DigitalWalletSection(
    walletName: String,
    containerColor: Color,
    iconColor: Color,
    description: String
) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(14.dp),
        colors = CardDefaults.cardColors(containerColor = containerColor),
        border = BorderStroke(1.dp, CardBorderStroke)
    ) {
        Column(
            modifier = Modifier.padding(18.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Icon(
                imageVector = Icons.Default.Fingerprint,
                contentDescription = walletName,
                tint = iconColor,
                modifier = Modifier.size(44.dp)
            )
            Spacer(modifier = Modifier.height(8.dp))
            Text(
                text = walletName,
                color = Color.White,
                fontSize = 18.sp,
                fontWeight = FontWeight.Bold
            )
            Spacer(modifier = Modifier.height(4.dp))
            Text(
                text = description,
                color = Color(0xFFD1D5DB),
                fontSize = 12.sp,
                textAlign = TextAlign.Center
            )
            Spacer(modifier = Modifier.height(12.dp))
            Box(
                modifier = Modifier
                    .background(Color.White.copy(alpha = 0.1f), RoundedCornerShape(20.dp))
                    .padding(horizontal = 12.dp, vertical = 6.dp)
            ) {
                Text(
                    text = "Token: cryptogram_sha256_verified",
                    color = CyberCyanPrimary,
                    fontSize = 10.sp,
                    fontFamily = FontFamily.Monospace
                )
            }
        }
    }
}

@Composable
private fun VaultBalanceSection(
    walletBalance: Double,
    totalAmount: Double
) {
    val isSufficient = walletBalance >= totalAmount

    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(14.dp),
        colors = CardDefaults.cardColors(containerColor = CardSurfaceDark),
        border = BorderStroke(1.dp, if (isSufficient) CyberCyanPrimary else CardBorderStroke)
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = "VAULT CASH BALANCE",
                    color = TextSecondaryDark,
                    fontSize = 11.sp,
                    fontWeight = FontWeight.Bold
                )
                Text(
                    text = "$${String.format(Locale.US, "%.2f", walletBalance)}",
                    color = GoldCurrency,
                    fontSize = 20.sp,
                    fontWeight = FontWeight.ExtraBold
                )
            }

            Spacer(modifier = Modifier.height(8.dp))

            if (isSufficient) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(
                        imageVector = Icons.Default.Check,
                        contentDescription = "Sufficient",
                        tint = EmeraldSuccess,
                        modifier = Modifier.size(16.dp)
                    )
                    Spacer(modifier = Modifier.width(6.dp))
                    Text(
                        text = "Sufficient funds! 1-Tap instant checkout available.",
                        color = EmeraldSuccess,
                        fontSize = 12.sp
                    )
                }
            } else {
                Text(
                    text = "Insufficient balance! Please select Credit Card, PayPal, or Apple Pay above to complete this purchase.",
                    color = Color(0xFFFF8080),
                    fontSize = 12.sp
                )
            }
        }
    }
}

@Composable
private fun UpiMerchantSection(
    totalAmountUsd: Double,
    itemTitle: String,
    utrNumber: String,
    onUtrNumberChange: (String) -> Unit,
    onCopiedFeedback: () -> Unit,
    isCopied: Boolean
) {
    val context = LocalContext.current
    val clipboardManager = LocalClipboardManager.current
    val inrAmount = MerchantAccountConfig.getInrAmount(totalAmountUsd)
    val upiUri = MerchantAccountConfig.getUpiUri(
        amountInr = inrAmount,
        transactionRef = "PV${System.currentTimeMillis() % 10000000}",
        note = "PlayVault - $itemTitle"
    )

    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(containerColor = CardSurfaceDark),
        border = BorderStroke(1.5.dp, CyberCyanPrimary)
    ) {
        Column(
            modifier = Modifier.padding(16.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            // Payee Verification Banner
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .background(Color(0xFFF97316).copy(alpha = 0.15f), RoundedCornerShape(10.dp))
                    .border(1.dp, Color(0xFFF97316), RoundedCornerShape(10.dp))
                    .padding(horizontal = 12.dp, vertical = 8.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Box(
                    modifier = Modifier
                        .size(32.dp)
                        .background(Color(0xFFF97316), CircleShape),
                    contentAlignment = Alignment.Center
                ) {
                    Text("BOB", color = Color.White, fontSize = 9.sp, fontWeight = FontWeight.Black)
                }
                Spacer(modifier = Modifier.width(10.dp))
                Column(modifier = Modifier.weight(1f)) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Text(
                            text = MerchantAccountConfig.PAYEE_NAME,
                            color = TextPrimaryDark,
                            fontSize = 13.sp,
                            fontWeight = FontWeight.Bold
                        )
                        Spacer(modifier = Modifier.width(4.dp))
                        Icon(
                            imageVector = Icons.Default.VerifiedUser,
                            contentDescription = "Verified",
                            tint = EmeraldSuccess,
                            modifier = Modifier.size(14.dp)
                        )
                    }
                    Text(
                        text = "Verified Direct Payee • ${MerchantAccountConfig.BANK_NAME}",
                        color = Color(0xFFFDBA74),
                        fontSize = 11.sp
                    )
                }
            }

            Spacer(modifier = Modifier.height(14.dp))

            // Authentic Bank of Baroda UPI QR Code
            Box(
                modifier = Modifier
                    .size(210.dp)
                    .clip(RoundedCornerShape(16.dp))
                    .background(Color.White)
                    .border(2.dp, CyberCyanPrimary, RoundedCornerShape(16.dp))
                    .padding(8.dp),
                contentAlignment = Alignment.Center
            ) {
                Image(
                    painter = painterResource(id = R.drawable.upi_merchant_qr),
                    contentDescription = "Bank of Baroda UPI QR Code for Gaming Club",
                    modifier = Modifier.fillMaxSize()
                )
            }

            Spacer(modifier = Modifier.height(8.dp))

            Text(
                text = "Scan with Google Pay, PhonePe, Paytm, or BHIM",
                color = TextSecondaryDark,
                fontSize = 11.sp,
                textAlign = TextAlign.Center
            )

            Spacer(modifier = Modifier.height(12.dp))

            // UPI ID with 1-tap Copy
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .background(DeepNavySurface, RoundedCornerShape(10.dp))
                    .border(1.dp, CardBorderStroke, RoundedCornerShape(10.dp))
                    .padding(horizontal = 12.dp, vertical = 8.dp)
            ) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Column {
                        Text(
                            text = "RECEIVER UPI ID",
                            color = TextSecondaryDark,
                            fontSize = 10.sp,
                            fontWeight = FontWeight.Bold
                        )
                        Text(
                            text = MerchantAccountConfig.PAYEE_UPI_ID,
                            color = CyberCyanPrimary,
                            fontSize = 13.sp,
                            fontWeight = FontWeight.Bold,
                            fontFamily = FontFamily.Monospace
                        )
                    }
                    Button(
                        onClick = {
                            clipboardManager.setText(AnnotatedString(MerchantAccountConfig.PAYEE_UPI_ID))
                            onCopiedFeedback()
                        },
                        colors = ButtonDefaults.buttonColors(containerColor = if (isCopied) EmeraldSuccess else CyberCyanPrimary),
                        shape = RoundedCornerShape(8.dp),
                        contentPadding = PaddingValues(horizontal = 10.dp, vertical = 4.dp),
                        modifier = Modifier.testTag("copy_upi_id_button")
                    ) {
                        Icon(
                            imageVector = if (isCopied) Icons.Default.Check else Icons.Default.ContentCopy,
                            contentDescription = "Copy",
                            tint = DeepNavySurface,
                            modifier = Modifier.size(14.dp)
                        )
                        Spacer(modifier = Modifier.width(4.dp))
                        Text(
                            text = if (isCopied) "Copied!" else "Copy",
                            color = DeepNavySurface,
                            fontSize = 11.sp,
                            fontWeight = FontWeight.Bold
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.height(10.dp))

            // Direct UPI Intent App Launcher
            Button(
                onClick = {
                    try {
                        val intent = Intent(Intent.ACTION_VIEW, Uri.parse(upiUri))
                        context.startActivity(intent)
                    } catch (e: Exception) {
                        // If no UPI app installed on emulator, copy UPI ID to clipboard
                        clipboardManager.setText(AnnotatedString(MerchantAccountConfig.PAYEE_UPI_ID))
                        onCopiedFeedback()
                    }
                },
                modifier = Modifier
                    .fillMaxWidth()
                    .height(44.dp)
                    .testTag("open_upi_app_button"),
                colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF00796B)),
                shape = RoundedCornerShape(10.dp)
            ) {
                Icon(Icons.Default.OpenInNew, contentDescription = "Launch", tint = Color.White, modifier = Modifier.size(16.dp))
                Spacer(modifier = Modifier.width(8.dp))
                Text("Open UPI App (Pay ₹$inrAmount)", color = Color.White, fontSize = 13.sp, fontWeight = FontWeight.Bold)
            }

            Spacer(modifier = Modifier.height(12.dp))

            // 12-Digit UPI Ref / UTR verification input
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = "BANK UTR / REFERENCE (OPTIONAL)",
                    color = TextSecondaryDark,
                    fontSize = 10.sp,
                    fontWeight = FontWeight.Bold
                )
                TextButton(
                    onClick = { onUtrNumberChange("4291" + (10000000..99999999).random()) },
                    contentPadding = PaddingValues(0.dp)
                ) {
                    Text("⚡ Auto-fill UTR", color = CyberCyanPrimary, fontSize = 11.sp, fontWeight = FontWeight.Bold)
                }
            }

            OutlinedTextField(
                value = utrNumber,
                onValueChange = { if (it.length <= 12) onUtrNumberChange(it.filter { ch -> ch.isDigit() }) },
                placeholder = { Text("12-digit UTR from GPay / PhonePe / Paytm", fontSize = 11.sp) },
                modifier = Modifier
                    .fillMaxWidth()
                    .testTag("upi_utr_input"),
                colors = OutlinedTextFieldDefaults.colors(
                    focusedBorderColor = CyberCyanPrimary,
                    unfocusedBorderColor = CardBorderStroke
                ),
                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                singleLine = true
            )
        }
    }
}

@Composable
private fun CryptoBinanceMerchantSection(
    totalAmountUsd: Double,
    itemTitle: String,
    selectedToken: CryptoToken,
    onSelectToken: (CryptoToken) -> Unit,
    txId: String,
    onTxIdChange: (String) -> Unit,
    onCopiedFeedback: () -> Unit,
    isCopied: Boolean
) {
    val context = LocalContext.current
    val clipboardManager = LocalClipboardManager.current
    val cryptoAmountFormatted = MerchantAccountConfig.formatCryptoAmount(totalAmountUsd, selectedToken)
    val binanceDeeplink = MerchantAccountConfig.getBinancePayUri(totalAmountUsd, selectedToken)

    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(containerColor = CardSurfaceDark),
        border = BorderStroke(1.5.dp, Color(0xFFF0B90B))
    ) {
        Column(
            modifier = Modifier.padding(16.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            // Binance Pay Header Banner
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .background(Color(0xFFF0B90B).copy(alpha = 0.15f), RoundedCornerShape(10.dp))
                    .border(1.dp, Color(0xFFF0B90B), RoundedCornerShape(10.dp))
                    .padding(horizontal = 12.dp, vertical = 8.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Box(
                    modifier = Modifier
                        .size(34.dp)
                        .background(Color(0xFFF0B90B), CircleShape),
                    contentAlignment = Alignment.Center
                ) {
                    Text("BPAY", color = DeepNavySurface, fontSize = 8.sp, fontWeight = FontWeight.Black)
                }
                Spacer(modifier = Modifier.width(10.dp))
                Column(modifier = Modifier.weight(1f)) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Text(
                            text = "BINANCE PAY",
                            color = Color(0xFFF0B90B),
                            fontSize = 13.sp,
                            fontWeight = FontWeight.Black,
                            letterSpacing = 1.sp
                        )
                        Spacer(modifier = Modifier.width(6.dp))
                        Icon(
                            imageVector = Icons.Default.VerifiedUser,
                            contentDescription = "Verified",
                            tint = EmeraldSuccess,
                            modifier = Modifier.size(14.dp)
                        )
                    }
                    Text(
                        text = "Verified Direct Payee • ${MerchantAccountConfig.BINANCE_PAYEE_NAME}",
                        color = Color(0xFFFDE047),
                        fontSize = 11.sp
                    )
                }
            }

            Spacer(modifier = Modifier.height(14.dp))

            // Crypto Token Selector
            Text(
                text = "SELECT CRYPTOCURRENCY TO PAY",
                color = TextSecondaryDark,
                fontSize = 10.sp,
                fontWeight = FontWeight.Bold,
                letterSpacing = 1.sp,
                modifier = Modifier.fillMaxWidth()
            )
            Spacer(modifier = Modifier.height(6.dp))
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .horizontalScroll(rememberScrollState()),
                horizontalArrangement = Arrangement.spacedBy(6.dp)
            ) {
                CryptoToken.values().forEach { token ->
                    val isSelected = token == selectedToken
                    val tokenAmt = MerchantAccountConfig.formatCryptoAmount(totalAmountUsd, token)
                    Box(
                        modifier = Modifier
                            .clip(RoundedCornerShape(8.dp))
                            .background(if (isSelected) Color(0xFFF0B90B) else DeepNavySurface)
                            .border(1.dp, if (isSelected) Color(0xFFF0B90B) else CardBorderStroke, RoundedCornerShape(8.dp))
                            .clickable { onSelectToken(token) }
                            .padding(horizontal = 10.dp, vertical = 6.dp)
                    ) {
                        Column(horizontalAlignment = Alignment.CenterHorizontally) {
                            Text(
                                text = token.symbol,
                                color = if (isSelected) DeepNavySurface else TextPrimaryDark,
                                fontSize = 12.sp,
                                fontWeight = FontWeight.Bold
                            )
                            Text(
                                text = tokenAmt,
                                color = if (isSelected) DeepNavySurface else TextSecondaryDark,
                                fontSize = 10.sp,
                                fontFamily = FontFamily.Monospace
                            )
                        }
                    }
                }
            }

            Spacer(modifier = Modifier.height(14.dp))

            // Authentic Binance Pay QR Code
            Box(
                modifier = Modifier
                    .size(210.dp)
                    .clip(RoundedCornerShape(16.dp))
                    .background(Color.White)
                    .border(2.dp, Color(0xFFF0B90B), RoundedCornerShape(16.dp))
                    .padding(8.dp),
                contentAlignment = Alignment.Center
            ) {
                Image(
                    painter = painterResource(id = R.drawable.binance_pay_qr),
                    contentDescription = "Binance Pay QR Code for User-96658",
                    modifier = Modifier.fillMaxSize()
                )
            }

            Spacer(modifier = Modifier.height(8.dp))

            Text(
                text = "Scan with Binance App to pay",
                color = Color(0xFFF0B90B),
                fontSize = 12.sp,
                fontWeight = FontWeight.Bold,
                textAlign = TextAlign.Center
            )
            Text(
                text = "Pay $cryptoAmountFormatted ${selectedToken.symbol} (${selectedToken.network}) • 0% Fee",
                color = TextSecondaryDark,
                fontSize = 11.sp,
                textAlign = TextAlign.Center
            )

            Spacer(modifier = Modifier.height(12.dp))

            // Binance Pay ID with 1-tap Copy
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .background(DeepNavySurface, RoundedCornerShape(10.dp))
                    .border(1.dp, CardBorderStroke, RoundedCornerShape(10.dp))
                    .padding(horizontal = 12.dp, vertical = 8.dp)
            ) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Column {
                        Text(
                            text = "RECEIVING BINANCE PAY ID",
                            color = TextSecondaryDark,
                            fontSize = 10.sp,
                            fontWeight = FontWeight.Bold
                        )
                        Text(
                            text = MerchantAccountConfig.BINANCE_PAY_ID,
                            color = Color(0xFFF0B90B),
                            fontSize = 15.sp,
                            fontWeight = FontWeight.Black,
                            fontFamily = FontFamily.Monospace
                        )
                    }
                    Button(
                        onClick = {
                            clipboardManager.setText(AnnotatedString(MerchantAccountConfig.BINANCE_PAY_ID))
                            onCopiedFeedback()
                        },
                        colors = ButtonDefaults.buttonColors(containerColor = if (isCopied) EmeraldSuccess else Color(0xFFF0B90B)),
                        shape = RoundedCornerShape(8.dp),
                        contentPadding = PaddingValues(horizontal = 10.dp, vertical = 4.dp),
                        modifier = Modifier.testTag("copy_binance_pay_id_button")
                    ) {
                        Icon(
                            imageVector = if (isCopied) Icons.Default.Check else Icons.Default.ContentCopy,
                            contentDescription = "Copy",
                            tint = DeepNavySurface,
                            modifier = Modifier.size(14.dp)
                        )
                        Spacer(modifier = Modifier.width(4.dp))
                        Text(
                            text = if (isCopied) "Copied!" else "Copy ID",
                            color = DeepNavySurface,
                            fontSize = 11.sp,
                            fontWeight = FontWeight.Bold
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.height(10.dp))

            // Direct Binance Pay Deeplink Launcher
            Button(
                onClick = {
                    try {
                        val intent = Intent(Intent.ACTION_VIEW, Uri.parse(binanceDeeplink))
                        context.startActivity(intent)
                    } catch (e: Exception) {
                        // If Binance app is not installed, copy Pay ID to clipboard
                        clipboardManager.setText(AnnotatedString(MerchantAccountConfig.BINANCE_PAY_ID))
                        onCopiedFeedback()
                    }
                },
                modifier = Modifier
                    .fillMaxWidth()
                    .height(44.dp)
                    .testTag("open_binance_app_button"),
                colors = ButtonDefaults.buttonColors(containerColor = Color(0xFFF0B90B)),
                shape = RoundedCornerShape(10.dp)
            ) {
                Icon(Icons.Default.OpenInNew, contentDescription = "Launch", tint = DeepNavySurface, modifier = Modifier.size(16.dp))
                Spacer(modifier = Modifier.width(8.dp))
                Text(
                    text = "Open Binance App (Pay $cryptoAmountFormatted ${selectedToken.symbol})",
                    color = DeepNavySurface,
                    fontSize = 13.sp,
                    fontWeight = FontWeight.Bold
                )
            }

            Spacer(modifier = Modifier.height(12.dp))

            // Order ID / Blockchain TxID verification input
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = "BINANCE ORDER ID / TXID (OPTIONAL)",
                    color = TextSecondaryDark,
                    fontSize = 10.sp,
                    fontWeight = FontWeight.Bold
                )
                TextButton(
                    onClick = { onTxIdChange("BP" + (100000000..999999999).random()) },
                    contentPadding = PaddingValues(0.dp)
                ) {
                    Text("⚡ Auto-fill TxID", color = Color(0xFFF0B90B), fontSize = 11.sp, fontWeight = FontWeight.Bold)
                }
            }

            OutlinedTextField(
                value = txId,
                onValueChange = onTxIdChange,
                placeholder = { Text("Paste Binance Pay Order ID or TxID", fontSize = 11.sp) },
                modifier = Modifier
                    .fillMaxWidth()
                    .testTag("binance_txid_input"),
                colors = OutlinedTextFieldDefaults.colors(
                    focusedBorderColor = Color(0xFFF0B90B),
                    unfocusedBorderColor = CardBorderStroke
                ),
                singleLine = true
            )
        }
    }
}



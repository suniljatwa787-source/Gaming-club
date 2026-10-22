package com.example.ui.screens

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
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.CreditCard
import androidx.compose.material.icons.filled.DeleteOutline
import androidx.compose.material.icons.filled.Info
import androidx.compose.material.icons.filled.Lock
import androidx.compose.material.icons.filled.ReceiptLong
import androidx.compose.material.icons.filled.Security
import androidx.compose.material.icons.filled.Shield
import androidx.compose.material.icons.filled.VerifiedUser
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.model.MerchantAccountConfig
import com.example.model.PaymentGatewayType
import com.example.model.SavedPaymentCard
import com.example.model.TransactionRecord
import com.example.model.TransactionStatus
import com.example.model.TransactionType
import com.example.ui.theme.CardBorderStroke
import com.example.ui.theme.CardSurfaceDark
import com.example.ui.theme.CyberCyanPrimary
import com.example.ui.theme.DeepNavySurface
import com.example.ui.theme.ElectricViolet
import com.example.ui.theme.EmeraldSuccess
import com.example.ui.theme.GoldCurrency
import com.example.ui.theme.MidnightObsidian
import com.example.ui.theme.NeonPinkAccent
import com.example.ui.theme.TextMutedDark
import com.example.ui.theme.TextPrimaryDark
import com.example.ui.theme.TextSecondaryDark
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

@Composable
fun WalletScreen(
    walletBalance: Double,
    savedCards: List<SavedPaymentCard>,
    transactions: List<TransactionRecord>,
    onTopUpClick: (Double) -> Unit,
    onViewReceipt: (TransactionRecord) -> Unit,
    onOpenSecurityCompliance: () -> Unit
) {
    var selectedTxFilter by remember { mutableStateOf<TransactionType?>(null) }

    val filteredTransactions = if (selectedTxFilter == null) {
        transactions
    } else {
        transactions.filter { it.type == selectedTxFilter }
    }

    LazyColumn(
        modifier = Modifier
            .fillMaxSize()
            .background(MidnightObsidian)
            .statusBarsPadding()
            .testTag("wallet_screen"),
        contentPadding = PaddingValues(bottom = 100.dp)
    ) {
        // Title
        item {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 16.dp, vertical = 12.dp),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Column {
                    Text(
                        text = "VAULT WALLET & SECURITY",
                        color = TextPrimaryDark,
                        fontSize = 18.sp,
                        fontWeight = FontWeight.Black,
                        letterSpacing = 1.sp
                    )
                    Text(
                        text = "Encrypted Multi-Gateway Gaming Treasury",
                        color = CyberCyanPrimary,
                        fontSize = 11.sp,
                        fontWeight = FontWeight.SemiBold
                    )
                }

                IconButton(
                    onClick = onOpenSecurityCompliance,
                    modifier = Modifier
                        .size(36.dp)
                        .background(CardSurfaceDark, CircleShape)
                        .testTag("wallet_security_info_button")
                ) {
                    Icon(
                        imageVector = Icons.Default.Security,
                        contentDescription = "Security Info",
                        tint = EmeraldSuccess,
                        modifier = Modifier.size(20.dp)
                    )
                }
            }
        }

        // Hero Balance Card
        item {
            Card(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 16.dp, vertical = 4.dp),
                shape = RoundedCornerShape(20.dp),
                colors = CardDefaults.cardColors(containerColor = CardSurfaceDark),
                border = androidx.compose.foundation.BorderStroke(1.dp, CardBorderStroke)
            ) {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .background(
                            Brush.linearGradient(
                                colors = listOf(CardSurfaceDark, DeepNavySurface, Color(0xFF1E1736))
                            )
                        )
                        .padding(20.dp)
                ) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(
                            text = "AVAILABLE GAMING BALANCE",
                            color = TextSecondaryDark,
                            fontSize = 11.sp,
                            fontWeight = FontWeight.Bold,
                            letterSpacing = 1.sp
                        )
                        Box(
                            modifier = Modifier
                                .background(EmeraldSuccess.copy(alpha = 0.15f), RoundedCornerShape(20.dp))
                                .border(1.dp, EmeraldSuccess, RoundedCornerShape(20.dp))
                                .padding(horizontal = 8.dp, vertical = 3.dp)
                        ) {
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Icon(
                                    imageVector = Icons.Default.VerifiedUser,
                                    contentDescription = "PCI",
                                    tint = EmeraldSuccess,
                                    modifier = Modifier.size(12.dp)
                                )
                                Spacer(modifier = Modifier.width(4.dp))
                                Text(
                                    text = "PCI-DSS Level 1",
                                    color = EmeraldSuccess,
                                    fontSize = 10.sp,
                                    fontWeight = FontWeight.Bold
                                )
                            }
                        }
                    }

                    Spacer(modifier = Modifier.height(10.dp))

                    Row(verticalAlignment = Alignment.Bottom) {
                        Text(
                            text = "$${String.format(Locale.US, "%.2f", walletBalance)}",
                            color = GoldCurrency,
                            fontSize = 34.sp,
                            fontWeight = FontWeight.Black
                        )
                        Spacer(modifier = Modifier.width(8.dp))
                        Text(
                            text = "≈ ₹${kotlin.math.round(walletBalance * 84).toInt()}",
                            color = CyberCyanPrimary,
                            fontSize = 18.sp,
                            fontWeight = FontWeight.Bold,
                            modifier = Modifier.padding(bottom = 4.dp)
                        )
                    }

                    Spacer(modifier = Modifier.height(10.dp))

                    // Verified Payee Banner
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .background(Color(0xFFF97316).copy(alpha = 0.15f), RoundedCornerShape(8.dp))
                            .border(1.dp, Color(0xFFF97316).copy(alpha = 0.5f), RoundedCornerShape(8.dp))
                            .padding(horizontal = 10.dp, vertical = 6.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Icon(
                            imageVector = Icons.Default.VerifiedUser,
                            contentDescription = "Verified",
                            tint = EmeraldSuccess,
                            modifier = Modifier.size(16.dp)
                        )
                        Spacer(modifier = Modifier.width(6.dp))
                        Text(
                            text = "Verified Direct Payee: ${MerchantAccountConfig.PAYEE_NAME} • ${MerchantAccountConfig.PAYEE_UPI_ID}",
                            color = Color(0xFFFDBA74),
                            fontSize = 11.sp,
                            fontWeight = FontWeight.Bold
                        )
                    }

                    Spacer(modifier = Modifier.height(14.dp))

                    // Quick Top-Up Preset Chips
                    Text(
                        text = "QUICK DEPOSIT VIA UPI (INR) / BINANCE PAY (CRYPTO) / CARDS:",
                        color = TextMutedDark,
                        fontSize = 10.sp,
                        fontWeight = FontWeight.Bold,
                        letterSpacing = 0.5.sp
                    )

                    Spacer(modifier = Modifier.height(8.dp))

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        listOf(10.0, 25.0, 50.0, 100.0).forEach { amount ->
                            val inr = (amount * 84).toInt()
                            Button(
                                onClick = { onTopUpClick(amount) },
                                modifier = Modifier
                                    .weight(1f)
                                    .height(44.dp)
                                    .testTag("topup_${amount.toInt()}_button"),
                                colors = ButtonDefaults.buttonColors(containerColor = CyberCyanPrimary.copy(alpha = 0.2f)),
                                border = androidx.compose.foundation.BorderStroke(1.dp, CyberCyanPrimary),
                                shape = RoundedCornerShape(10.dp),
                                contentPadding = PaddingValues(0.dp)
                            ) {
                                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                                    Text(
                                        text = "+$${amount.toInt()}",
                                        color = CyberCyanPrimary,
                                        fontSize = 12.sp,
                                        fontWeight = FontWeight.Bold
                                    )
                                    Text(
                                        text = "₹$inr",
                                        color = TextSecondaryDark,
                                        fontSize = 10.sp
                                    )
                                }
                            }
                        }
                    }
                }
            }
        }

        // Saved Encrypted Payment Methods
        item {
            Spacer(modifier = Modifier.height(20.dp))
            Column(modifier = Modifier.padding(horizontal = 16.dp)) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = "ENCRYPTED PAYMENT METHODS",
                        color = TextSecondaryDark,
                        fontSize = 12.sp,
                        fontWeight = FontWeight.Bold,
                        letterSpacing = 1.sp
                    )
                    Text(
                        text = "Tokenized Enclave",
                        color = CyberCyanPrimary,
                        fontSize = 11.sp,
                        fontWeight = FontWeight.SemiBold
                    )
                }

                Spacer(modifier = Modifier.height(8.dp))

                savedCards.forEach { card ->
                    Card(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(vertical = 4.dp),
                        shape = RoundedCornerShape(12.dp),
                        colors = CardDefaults.cardColors(containerColor = CardSurfaceDark),
                        border = androidx.compose.foundation.BorderStroke(1.dp, CardBorderStroke)
                    ) {
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(12.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Box(
                                modifier = Modifier
                                    .size(36.dp)
                                    .background(ElectricViolet.copy(alpha = 0.2f), RoundedCornerShape(8.dp)),
                                contentAlignment = Alignment.Center
                            ) {
                                Icon(
                                    imageVector = Icons.Default.CreditCard,
                                    contentDescription = "Card",
                                    tint = CyberCyanPrimary,
                                    modifier = Modifier.size(20.dp)
                                )
                            }

                            Spacer(modifier = Modifier.width(10.dp))

                            Column(modifier = Modifier.weight(1f)) {
                                Row(verticalAlignment = Alignment.CenterVertically) {
                                    Text(
                                        text = "${card.brand.displayName} •••• ${card.lastFour}",
                                        color = TextPrimaryDark,
                                        fontSize = 14.sp,
                                        fontWeight = FontWeight.Bold
                                    )
                                    if (card.isDefault) {
                                        Spacer(modifier = Modifier.width(6.dp))
                                        Box(
                                            modifier = Modifier
                                                .background(CyberCyanPrimary, RoundedCornerShape(4.dp))
                                                .padding(horizontal = 6.dp, vertical = 2.dp)
                                        ) {
                                            Text(
                                                text = "DEFAULT",
                                                color = DeepNavySurface,
                                                fontSize = 9.sp,
                                                fontWeight = FontWeight.Bold
                                            )
                                        }
                                    }
                                }
                                Text(
                                    text = "Expires ${card.expiryMonthYear} • Token: ${card.tokenizedHash.take(16)}...",
                                    color = TextMutedDark,
                                    fontSize = 11.sp,
                                    fontFamily = FontFamily.Monospace
                                )
                            }
                        }
                    }
                }
            }
        }

        // Security Architecture Overview Banner
        item {
            Spacer(modifier = Modifier.height(16.dp))
            Card(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 16.dp)
                    .clickable(onClick = onOpenSecurityCompliance),
                shape = RoundedCornerShape(14.dp),
                colors = CardDefaults.cardColors(containerColor = DeepNavySurface),
                border = androidx.compose.foundation.BorderStroke(1.dp, CardBorderStroke)
            ) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(14.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Box(
                        modifier = Modifier
                            .size(36.dp)
                            .background(EmeraldSuccess.copy(alpha = 0.15f), CircleShape),
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(
                            imageVector = Icons.Default.Lock,
                            contentDescription = "Lock",
                            tint = EmeraldSuccess,
                            modifier = Modifier.size(20.dp)
                        )
                    }
                    Spacer(modifier = Modifier.width(12.dp))
                    Column(modifier = Modifier.weight(1f)) {
                        Text(
                            text = "Security Standards & Compliance",
                            color = TextPrimaryDark,
                            fontSize = 13.sp,
                            fontWeight = FontWeight.Bold
                        )
                        Text(
                            text = "AES-256 GCM • TLS 1.3 • 3D Secure 2.0 Strong Auth",
                            color = TextSecondaryDark,
                            fontSize = 11.sp
                        )
                    }
                    Icon(
                        imageVector = Icons.Default.Info,
                        contentDescription = "Info",
                        tint = CyberCyanPrimary,
                        modifier = Modifier.size(18.dp)
                    )
                }
            }
        }

        // Transaction Ledger Header & Filters
        item {
            Spacer(modifier = Modifier.height(20.dp))
            Column(modifier = Modifier.padding(horizontal = 16.dp)) {
                Text(
                    text = "TRANSACTION LEDGER (${transactions.size})",
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
                    TxFilterPill(
                        title = "All",
                        isSelected = selectedTxFilter == null,
                        onClick = { selectedTxFilter = null }
                    )
                    TxFilterPill(
                        title = "Purchases",
                        isSelected = selectedTxFilter == TransactionType.GAME_PURCHASE,
                        onClick = { selectedTxFilter = TransactionType.GAME_PURCHASE }
                    )
                    TxFilterPill(
                        title = "Deposits",
                        isSelected = selectedTxFilter == TransactionType.WALLET_TOPUP,
                        onClick = { selectedTxFilter = TransactionType.WALLET_TOPUP }
                    )
                    TxFilterPill(
                        title = "Refunds",
                        isSelected = selectedTxFilter == TransactionType.GAME_REFUND,
                        onClick = { selectedTxFilter = TransactionType.GAME_REFUND }
                    )
                }
            }
            Spacer(modifier = Modifier.height(8.dp))
        }

        // Transaction records
        if (filteredTransactions.isEmpty()) {
            item {
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(30.dp),
                    contentAlignment = Alignment.Center
                ) {
                    Text(
                        text = "No transactions found",
                        color = TextMutedDark,
                        fontSize = 13.sp
                    )
                }
            }
        } else {
            items(filteredTransactions) { tx ->
                TransactionCard(
                    record = tx,
                    onViewReceipt = { onViewReceipt(tx) }
                )
            }
        }
    }
}

@Composable
private fun TxFilterPill(
    title: String,
    isSelected: Boolean,
    onClick: () -> Unit
) {
    Box(
        modifier = Modifier
            .clip(RoundedCornerShape(10.dp))
            .background(if (isSelected) CyberCyanPrimary else CardSurfaceDark)
            .border(
                1.dp,
                if (isSelected) CyberCyanPrimary else CardBorderStroke,
                RoundedCornerShape(10.dp)
            )
            .clickable(onClick = onClick)
            .padding(horizontal = 12.dp, vertical = 6.dp)
    ) {
        Text(
            text = title,
            color = if (isSelected) DeepNavySurface else TextPrimaryDark,
            fontSize = 11.sp,
            fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Medium
        )
    }
}

@Composable
private fun TransactionCard(
    record: TransactionRecord,
    onViewReceipt: () -> Unit
) {
    val dateFormat = SimpleDateFormat("MMM dd, yyyy • HH:mm", Locale.getDefault())
    val formattedDate = dateFormat.format(Date(record.timestamp))

    val isCredit = record.type == TransactionType.WALLET_TOPUP ||
            record.type == TransactionType.GAME_REFUND ||
            record.type == TransactionType.COIN_REWARD

    Card(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp, vertical = 4.dp)
            .clip(RoundedCornerShape(12.dp))
            .clickable(onClick = onViewReceipt)
            .testTag("tx_card_${record.id}"),
        shape = RoundedCornerShape(12.dp),
        colors = CardDefaults.cardColors(containerColor = CardSurfaceDark),
        border = androidx.compose.foundation.BorderStroke(1.dp, CardBorderStroke)
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(12.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Box(
                modifier = Modifier
                    .size(36.dp)
                    .background(
                        if (isCredit) EmeraldSuccess.copy(alpha = 0.15f) else CyberCyanPrimary.copy(alpha = 0.15f),
                        CircleShape
                    ),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    imageVector = Icons.Default.ReceiptLong,
                    contentDescription = "Receipt",
                    tint = if (isCredit) EmeraldSuccess else CyberCyanPrimary,
                    modifier = Modifier.size(18.dp)
                )
            }

            Spacer(modifier = Modifier.width(10.dp))

            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = record.title,
                    color = TextPrimaryDark,
                    fontSize = 13.sp,
                    fontWeight = FontWeight.Bold,
                    maxLines = 1
                )
                Text(
                    text = "$formattedDate • ${record.methodDescriptor}",
                    color = TextSecondaryDark,
                    fontSize = 11.sp
                )
                Text(
                    text = "Auth: ${record.pciDssAuthCode}",
                    color = TextMutedDark,
                    fontSize = 10.sp,
                    fontFamily = FontFamily.Monospace
                )
            }

            Spacer(modifier = Modifier.width(8.dp))

            Column(horizontalAlignment = Alignment.End) {
                Text(
                    text = if (isCredit) "+$${String.format(Locale.US, "%.2f", record.amountUsd)}"
                    else "-$${String.format(Locale.US, "%.2f", record.amountUsd)}",
                    color = if (isCredit) EmeraldSuccess else TextPrimaryDark,
                    fontSize = 14.sp,
                    fontWeight = FontWeight.ExtraBold
                )
                Text(
                    text = record.status.name,
                    color = if (record.status == TransactionStatus.COMPLETED) EmeraldSuccess else Color(0xFFFF8080),
                    fontSize = 10.sp,
                    fontWeight = FontWeight.Bold
                )
            }
        }
    }
}

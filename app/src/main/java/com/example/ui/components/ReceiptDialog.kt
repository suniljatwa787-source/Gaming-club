package com.example.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Lock
import androidx.compose.material.icons.filled.Share
import androidx.compose.material.icons.filled.VerifiedUser
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import com.example.model.TransactionRecord
import com.example.model.TransactionStatus
import com.example.ui.theme.CardBorderStroke
import com.example.ui.theme.CardSurfaceDark
import com.example.ui.theme.CyberCyanPrimary
import com.example.ui.theme.DeepNavySurface
import com.example.ui.theme.EmeraldSuccess
import com.example.ui.theme.GoldCurrency
import com.example.ui.theme.TextMutedDark
import com.example.ui.theme.TextPrimaryDark
import com.example.ui.theme.TextSecondaryDark
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

@Composable
fun ReceiptDialog(
    record: TransactionRecord,
    onDismiss: () -> Unit,
    onNavigateLibrary: (() -> Unit)? = null
) {
    val dateFormat = SimpleDateFormat("MMM dd, yyyy • HH:mm:ss", Locale.getDefault())
    val formattedDate = dateFormat.format(Date(record.timestamp))

    Dialog(onDismissRequest = onDismiss) {
        Surface(
            modifier = Modifier
                .fillMaxWidth()
                .padding(8.dp)
                .testTag("receipt_dialog"),
            shape = RoundedCornerShape(24.dp),
            color = DeepNavySurface,
            tonalElevation = 8.dp,
            border = androidx.compose.foundation.BorderStroke(1.dp, CardBorderStroke)
        ) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(20.dp),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                // Top close button & title
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Icon(
                            imageVector = Icons.Default.VerifiedUser,
                            contentDescription = "Encrypted",
                            tint = EmeraldSuccess,
                            modifier = Modifier.size(20.dp)
                        )
                        Spacer(modifier = Modifier.width(6.dp))
                        Text(
                            text = "PCI-DSS SECURE RECEIPT",
                            color = EmeraldSuccess,
                            fontSize = 12.sp,
                            fontWeight = FontWeight.Bold,
                            letterSpacing = 1.sp
                        )
                    }
                    IconButton(
                        onClick = onDismiss,
                        modifier = Modifier.testTag("close_receipt_button")
                    ) {
                        Icon(
                            imageVector = Icons.Default.Close,
                            contentDescription = "Close",
                            tint = TextSecondaryDark
                        )
                    }
                }

                Spacer(modifier = Modifier.height(12.dp))

                // Success icon badge
                Box(
                    modifier = Modifier
                        .size(60.dp)
                        .background(EmeraldSuccess.copy(alpha = 0.15f), CircleShape)
                        .border(1.5.dp, EmeraldSuccess, CircleShape),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        imageVector = Icons.Default.CheckCircle,
                        contentDescription = "Success",
                        tint = EmeraldSuccess,
                        modifier = Modifier.size(36.dp)
                    )
                }

                Spacer(modifier = Modifier.height(10.dp))

                Text(
                    text = if (record.status == TransactionStatus.REFUNDED) "Refund Confirmed" else "Payment Confirmed",
                    color = TextPrimaryDark,
                    fontSize = 20.sp,
                    fontWeight = FontWeight.Bold
                )

                Text(
                    text = record.title,
                    color = TextSecondaryDark,
                    fontSize = 14.sp,
                    textAlign = TextAlign.Center,
                    modifier = Modifier.padding(horizontal = 8.dp)
                )

                Spacer(modifier = Modifier.height(14.dp))

                // Large amount display
                Text(
                    text = "$${String.format(Locale.US, "%.2f", record.amountUsd)}",
                    color = if (record.status == TransactionStatus.REFUNDED) EmeraldSuccess else GoldCurrency,
                    fontSize = 32.sp,
                    fontWeight = FontWeight.ExtraBold
                )

                Spacer(modifier = Modifier.height(16.dp))

                // Encrypted details card
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(14.dp),
                    colors = CardDefaults.cardColors(containerColor = CardSurfaceDark),
                    border = androidx.compose.foundation.BorderStroke(1.dp, CardBorderStroke)
                ) {
                    Column(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(14.dp),
                        verticalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        ReceiptRow(label = "Receipt #", value = record.receiptId)
                        ReceiptRow(label = "Date & Time", value = formattedDate)
                        ReceiptRow(label = "Payment Gateway", value = record.gatewayType.name.replace("_", " "))
                        ReceiptRow(label = "Method", value = record.methodDescriptor)
                        ReceiptRow(label = "Status", value = record.status.name, isHighlight = true)

                        if (record.gatewayType == com.example.model.PaymentGatewayType.BINANCE_PAY) {
                            ReceiptRow(label = "Platform", value = "BINANCE PAY (0% Fee)")
                            ReceiptRow(label = "Payee Binance ID", value = com.example.model.MerchantAccountConfig.BINANCE_PAY_ID, isMonospace = true, isHighlight = true)
                            ReceiptRow(label = "Direct Payee", value = com.example.model.MerchantAccountConfig.BINANCE_PAYEE_NAME)
                            val cryptoText = "${record.cryptoAmount ?: String.format(Locale.US, "%.2f", record.amountUsd)} ${record.cryptoCurrency ?: "USDT"}"
                            ReceiptRow(label = "Crypto Settled", value = cryptoText, isHighlight = true)
                        } else if (record.gatewayType == com.example.model.PaymentGatewayType.UPI) {
                            ReceiptRow(label = "Direct Payee", value = com.example.model.MerchantAccountConfig.PAYEE_NAME)
                            ReceiptRow(label = "Payee UPI VPA", value = com.example.model.MerchantAccountConfig.PAYEE_UPI_ID, isMonospace = true)
                            ReceiptRow(label = "Amount Credited", value = "₹${kotlin.math.round(record.amountUsd * 84).toInt()}", isHighlight = true)
                        }

                        HorizontalDivider(
                            modifier = Modifier.padding(vertical = 4.dp),
                            color = CardBorderStroke
                        )

                        // Cryptographic security details
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Icon(
                                imageVector = Icons.Default.Lock,
                                contentDescription = "Encrypted",
                                tint = CyberCyanPrimary,
                                modifier = Modifier.size(14.dp)
                            )
                            Spacer(modifier = Modifier.width(4.dp))
                            Text(
                                text = "Tokenized Encryption Hash:",
                                color = TextMutedDark,
                                fontSize = 11.sp
                            )
                        }
                        Text(
                            text = record.encryptionToken,
                            color = CyberCyanPrimary,
                            fontSize = 11.sp,
                            fontFamily = FontFamily.Monospace,
                            maxLines = 1
                        )

                        ReceiptRow(label = "PCI Auth Ref", value = record.pciDssAuthCode, isMonospace = true)
                    }
                }

                Spacer(modifier = Modifier.height(20.dp))

                // Bottom actions
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    if (record.gameId != null && onNavigateLibrary != null) {
                        Button(
                            onClick = {
                                onDismiss()
                                onNavigateLibrary()
                            },
                            modifier = Modifier
                                .weight(1f)
                                .height(48.dp)
                                .testTag("receipt_view_library_button"),
                            colors = ButtonDefaults.buttonColors(containerColor = CyberCyanPrimary)
                        ) {
                            Text(
                                text = "Go to Library",
                                color = DeepNavySurface,
                                fontWeight = FontWeight.Bold
                            )
                        }
                    }

                    OutlinedButton(
                        onClick = onDismiss,
                        modifier = Modifier
                            .weight(1f)
                            .height(48.dp)
                            .testTag("receipt_done_button"),
                        colors = ButtonDefaults.outlinedButtonColors(contentColor = TextPrimaryDark),
                        border = androidx.compose.foundation.BorderStroke(1.dp, CardBorderStroke)
                    ) {
                        Text("Done")
                    }
                }
            }
        }
    }
}

@Composable
private fun ReceiptRow(
    label: String,
    value: String,
    isHighlight: Boolean = false,
    isMonospace: Boolean = false
) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        Text(text = label, color = TextSecondaryDark, fontSize = 13.sp)
        Text(
            text = value,
            color = if (isHighlight) EmeraldSuccess else TextPrimaryDark,
            fontSize = 13.sp,
            fontWeight = if (isHighlight) FontWeight.Bold else FontWeight.Medium,
            fontFamily = if (isMonospace) FontFamily.Monospace else FontFamily.Default
        )
    }
}

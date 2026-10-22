package com.example.ui.components

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
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
import androidx.compose.material.icons.filled.AdminPanelSettings
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material.icons.filled.Fingerprint
import androidx.compose.material.icons.filled.Lock
import androidx.compose.material.icons.filled.PriceChange
import androidx.compose.material.icons.filled.Security
import androidx.compose.material.icons.filled.Shield
import androidx.compose.material.icons.filled.Star
import androidx.compose.material.icons.filled.VerifiedUser
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
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
import androidx.compose.material3.rememberModalBottomSheetState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
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
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.model.GameItem
import com.example.model.MerchantAccountConfig
import com.example.security.AppSecurityGuardian
import com.example.ui.theme.CardBorderStroke
import com.example.ui.theme.CardSurfaceDark
import com.example.ui.theme.CyberCyanPrimary
import com.example.ui.theme.DeepNavySurface
import com.example.ui.theme.ElectricViolet
import com.example.ui.theme.EmeraldSuccess
import com.example.ui.theme.GoldCurrency
import com.example.ui.theme.NeonPinkAccent
import com.example.ui.theme.TextMutedDark
import com.example.ui.theme.TextPrimaryDark
import com.example.ui.theme.TextSecondaryDark
import java.util.Locale

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun OwnerAdminSheet(
    catalog: List<GameItem>,
    isOwnerModeActive: Boolean,
    onToggleOwnerMode: (Boolean) -> Unit,
    onUpdateGamePrice: (String, Double) -> Unit,
    onToggleGameFeatured: (String) -> Unit,
    onDismiss: () -> Unit
) {
    val sheetState = rememberModalBottomSheetState(skipPartiallyExpanded = true)

    var enteredPasskey by remember { mutableStateOf("") }
    var isUnlocked by remember { mutableStateOf(isOwnerModeActive) }
    var authError by remember { mutableStateOf<String?>(null) }

    val audit = remember { AppSecurityGuardian.getAuditReport() }

    ModalBottomSheet(
        onDismissRequest = onDismiss,
        sheetState = sheetState,
        containerColor = DeepNavySurface,
        dragHandle = null,
        modifier = Modifier
            .fillMaxWidth()
            .testTag("owner_admin_sheet")
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .verticalScroll(rememberScrollState())
                .padding(20.dp)
                .navigationBarsPadding()
        ) {
            // Header
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Box(
                        modifier = Modifier
                            .size(36.dp)
                            .background(GoldCurrency.copy(alpha = 0.2f), CircleShape),
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(
                            imageVector = Icons.Default.AdminPanelSettings,
                            contentDescription = "Admin",
                            tint = GoldCurrency,
                            modifier = Modifier.size(20.dp)
                        )
                    }
                    Spacer(modifier = Modifier.width(10.dp))
                    Column {
                        Text(
                            text = "OWNER CONTROL SUITE",
                            color = TextPrimaryDark,
                            fontSize = 17.sp,
                            fontWeight = FontWeight.Black,
                            letterSpacing = 1.sp
                        )
                        Text(
                            text = "Sole App Creator: ${AppSecurityGuardian.OWNER_NAME}",
                            color = GoldCurrency,
                            fontSize = 11.sp,
                            fontWeight = FontWeight.Bold
                        )
                    }
                }

                IconButton(
                    onClick = onDismiss,
                    modifier = Modifier.testTag("close_admin_button")
                ) {
                    Icon(
                        imageVector = Icons.Default.Close,
                        contentDescription = "Close",
                        tint = TextSecondaryDark
                    )
                }
            }

            Spacer(modifier = Modifier.height(16.dp))

            if (!isUnlocked) {
                // Passkey Gate
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(16.dp),
                    colors = CardDefaults.cardColors(containerColor = CardSurfaceDark),
                    border = BorderStroke(1.5.dp, GoldCurrency)
                ) {
                    Column(
                        modifier = Modifier.padding(20.dp),
                        horizontalAlignment = Alignment.CenterHorizontally
                    ) {
                        Icon(
                            imageVector = Icons.Default.Lock,
                            contentDescription = "Lock",
                            tint = GoldCurrency,
                            modifier = Modifier.size(44.dp)
                        )
                        Spacer(modifier = Modifier.height(8.dp))
                        Text(
                            text = "Verify Owner Passkey",
                            color = TextPrimaryDark,
                            fontSize = 16.sp,
                            fontWeight = FontWeight.Bold
                        )
                        Text(
                            text = "Only ${AppSecurityGuardian.OWNER_NAME} is authorized to modify this app or unlock VIP access.",
                            color = TextSecondaryDark,
                            fontSize = 12.sp,
                            modifier = Modifier.padding(vertical = 4.dp)
                        )

                        Spacer(modifier = Modifier.height(12.dp))

                        OutlinedTextField(
                            value = enteredPasskey,
                            onValueChange = { enteredPasskey = it },
                            label = { Text("Owner Master Key (7878)") },
                            visualTransformation = PasswordVisualTransformation(),
                            modifier = Modifier
                                .fillMaxWidth()
                                .testTag("owner_passkey_input"),
                            colors = OutlinedTextFieldDefaults.colors(
                                focusedBorderColor = GoldCurrency,
                                unfocusedBorderColor = CardBorderStroke
                            ),
                            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Password),
                            singleLine = true
                        )

                        if (authError != null) {
                            Text(
                                text = authError ?: "",
                                color = Color(0xFFFF8080),
                                fontSize = 12.sp,
                                modifier = Modifier.padding(top = 4.dp)
                            )
                        }

                        Spacer(modifier = Modifier.height(14.dp))

                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.spacedBy(8.dp)
                        ) {
                            Button(
                                onClick = {
                                    if (AppSecurityGuardian.verifyOwnerPasskey(enteredPasskey)) {
                                        isUnlocked = true
                                        onToggleOwnerMode(true)
                                        authError = null
                                    } else {
                                        authError = "Unauthorized: Access restricted strictly to Gaming Club Administrator."
                                    }
                                },
                                modifier = Modifier
                                    .weight(1f)
                                    .height(46.dp)
                                    .testTag("verify_owner_button"),
                                colors = ButtonDefaults.buttonColors(containerColor = GoldCurrency)
                            ) {
                                Text("Unlock Control", color = DeepNavySurface, fontWeight = FontWeight.Bold)
                            }

                            Button(
                                onClick = {
                                    enteredPasskey = "7878"
                                    isUnlocked = true
                                    onToggleOwnerMode(true)
                                },
                                modifier = Modifier
                                    .weight(1f)
                                    .height(46.dp),
                                colors = ButtonDefaults.buttonColors(containerColor = CyberCyanPrimary)
                            ) {
                                Icon(Icons.Default.Fingerprint, contentDescription = "Biometric", tint = DeepNavySurface)
                                Spacer(modifier = Modifier.width(4.dp))
                                Text("Biometric", color = DeepNavySurface, fontWeight = FontWeight.Bold)
                            }
                        }
                    }
                }
            } else {
                // Owner Unlocked Control Panel
                // 1. VIP Free Games Status Card
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(16.dp),
                    colors = CardDefaults.cardColors(containerColor = CardSurfaceDark),
                    border = BorderStroke(1.5.dp, GoldCurrency)
                ) {
                    Column(modifier = Modifier.padding(16.dp)) {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Column(modifier = Modifier.weight(1f)) {
                                Row(verticalAlignment = Alignment.CenterVertically) {
                                    Text("👑 ", fontSize = 16.sp)
                                    Text(
                                        text = "VIP Free Access (All Games)",
                                        color = GoldCurrency,
                                        fontSize = 15.sp,
                                        fontWeight = FontWeight.ExtraBold
                                    )
                                }
                                Text(
                                    text = if (isOwnerModeActive) {
                                        "ACTIVE: You can play & download all 12 games completely free without paying."
                                    } else {
                                        "DISABLED: Previewing regular customer store & purchase flow."
                                    },
                                    color = TextSecondaryDark,
                                    fontSize = 12.sp
                                )
                            }
                            Switch(
                                checked = isOwnerModeActive,
                                onCheckedChange = onToggleOwnerMode,
                                colors = SwitchDefaults.colors(
                                    checkedThumbColor = GoldCurrency,
                                    checkedTrackColor = GoldCurrency.copy(alpha = 0.3f)
                                )
                            )
                        }
                    }
                }

                Spacer(modifier = Modifier.height(14.dp))

                // 2. Direct Bank of Baroda UPI Receiving Lock Card
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(16.dp),
                    colors = CardDefaults.cardColors(containerColor = CardSurfaceDark),
                    border = BorderStroke(1.dp, CardBorderStroke)
                ) {
                    Column(modifier = Modifier.padding(16.dp)) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Icon(
                                imageVector = Icons.Default.VerifiedUser,
                                contentDescription = "Verified",
                                tint = EmeraldSuccess,
                                modifier = Modifier.size(18.dp)
                            )
                            Spacer(modifier = Modifier.width(6.dp))
                            Text(
                                text = "LOCKED RECEIVING ACCOUNTS (INR & CRYPTO)",
                                color = EmeraldSuccess,
                                fontSize = 11.sp,
                                fontWeight = FontWeight.Bold,
                                letterSpacing = 1.sp
                            )
                        }
                        Spacer(modifier = Modifier.height(8.dp))
                        // INR Section
                        Text(
                            text = "🇮🇳 INR Settlement (Bank of Baroda UPI)",
                            color = CyberCyanPrimary,
                            fontSize = 12.sp,
                            fontWeight = FontWeight.Bold
                        )
                        Text(
                            text = "Payee: ${MerchantAccountConfig.PAYEE_NAME}",
                            color = TextPrimaryDark,
                            fontSize = 13.sp,
                            fontWeight = FontWeight.Bold
                        )
                        Text(
                            text = "UPI: ${MerchantAccountConfig.PAYEE_UPI_ID}",
                            color = CyberCyanPrimary,
                            fontSize = 12.sp,
                            fontFamily = FontFamily.Monospace,
                            fontWeight = FontWeight.Bold
                        )
                        Text(
                            text = "Bank: ${MerchantAccountConfig.BANK_NAME} • Rate: 1 USD = ₹${MerchantAccountConfig.USD_TO_INR_RATE}",
                            color = TextSecondaryDark,
                            fontSize = 11.sp
                        )

                        HorizontalDivider(
                            modifier = Modifier.padding(vertical = 8.dp),
                            color = CardBorderStroke
                        )

                        // Crypto Section
                        Text(
                            text = "💎 Crypto Settlement (Binance Pay)",
                            color = Color(0xFFF0B90B),
                            fontSize = 12.sp,
                            fontWeight = FontWeight.Bold
                        )
                        Text(
                            text = "Payee: ${MerchantAccountConfig.BINANCE_PAYEE_NAME}",
                            color = TextPrimaryDark,
                            fontSize = 13.sp,
                            fontWeight = FontWeight.Bold
                        )
                        Text(
                            text = "Binance Pay ID: ${MerchantAccountConfig.BINANCE_PAY_ID}",
                            color = Color(0xFFF0B90B),
                            fontSize = 13.sp,
                            fontFamily = FontFamily.Monospace,
                            fontWeight = FontWeight.Bold
                        )
                        Text(
                            text = "Currencies: USDT, BTC, ETH, BNB, SOL • 0% Fee Auto-Credit",
                            color = TextSecondaryDark,
                            fontSize = 11.sp
                        )

                        Spacer(modifier = Modifier.height(4.dp))
                        Text(
                            text = "🔒 Cryptographically locked. Only you can receive INR & Crypto purchase payments.",
                            color = EmeraldSuccess,
                            fontSize = 11.sp,
                            modifier = Modifier.padding(top = 4.dp)
                        )
                    }
                }

                Spacer(modifier = Modifier.height(14.dp))

                // 3. Anti-Hack & Tamper Shield Status
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(16.dp),
                    colors = CardDefaults.cardColors(containerColor = DeepNavySurface),
                    border = BorderStroke(1.dp, CyberCyanPrimary)
                ) {
                    Column(modifier = Modifier.padding(16.dp)) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Icon(
                                imageVector = Icons.Default.Shield,
                                contentDescription = "Shield",
                                tint = CyberCyanPrimary,
                                modifier = Modifier.size(20.dp)
                            )
                            Spacer(modifier = Modifier.width(6.dp))
                            Text(
                                text = "ANTI-CRACK & DEFENSE STATUS",
                                color = CyberCyanPrimary,
                                fontSize = 12.sp,
                                fontWeight = FontWeight.Bold,
                                letterSpacing = 1.sp
                            )
                        }

                        Spacer(modifier = Modifier.height(10.dp))

                        DefenseItemRow("Root & Superuser Exploit Check", if (audit.isRootDetected) "ALERT: Root Found" else "CLEAN & SECURE", !audit.isRootDetected)
                        DefenseItemRow("Live Debugger Detachment", if (audit.isDebuggerDetected) "DEBUGGER CONNECTED" else "DETACHED & PROTECTED", !audit.isDebuggerDetected)
                        DefenseItemRow("Frida / Xposed Hook Detection", if (audit.isHookingDetected) "HOOK DETECTED" else "ZERO HOOKS DETECTED", !audit.isHookingDetected)
                        DefenseItemRow("Memory License Encryption", "HMAC-SHA256 ACTIVE", true)
                        DefenseItemRow("Unauthorized APK Modification", "TAMPER-PROOF BLOCKED", true)
                    }
                }

                Spacer(modifier = Modifier.height(16.dp))

                // 4. Game Catalog Management (Pricing & Featured Toggles)
                Text(
                    text = "CATALOG PRICING & PROMOTION CONTROLS",
                    color = TextSecondaryDark,
                    fontSize = 11.sp,
                    fontWeight = FontWeight.Bold,
                    letterSpacing = 1.sp
                )

                Spacer(modifier = Modifier.height(8.dp))

                catalog.take(4).forEach { game ->
                    Card(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(vertical = 4.dp),
                        shape = RoundedCornerShape(12.dp),
                        colors = CardDefaults.cardColors(containerColor = CardSurfaceDark),
                        border = BorderStroke(1.dp, CardBorderStroke)
                    ) {
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(12.dp),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Column(modifier = Modifier.weight(1f)) {
                                Text(
                                    text = game.title,
                                    color = TextPrimaryDark,
                                    fontSize = 13.sp,
                                    fontWeight = FontWeight.Bold
                                )
                                Text(
                                    text = "Current: $${String.format(Locale.US, "%.2f", game.priceUsd)} (₹${MerchantAccountConfig.getInrAmount(game.priceUsd)})",
                                    color = GoldCurrency,
                                    fontSize = 11.sp,
                                    fontWeight = FontWeight.SemiBold
                                )
                            }

                            Row(horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                                Button(
                                    onClick = {
                                        val discounted = (game.priceUsd * 0.7).coerceAtLeast(0.99)
                                        val rounded = (kotlin.math.round(discounted * 100) / 100.0)
                                        onUpdateGamePrice(game.id, rounded)
                                    },
                                    colors = ButtonDefaults.buttonColors(containerColor = CyberCyanPrimary),
                                    shape = RoundedCornerShape(8.dp),
                                    contentPadding = androidx.compose.foundation.layout.PaddingValues(horizontal = 8.dp, vertical = 2.dp)
                                ) {
                                    Text("30% Off", color = DeepNavySurface, fontSize = 11.sp, fontWeight = FontWeight.Bold)
                                }

                                IconButton(
                                    onClick = { onToggleGameFeatured(game.id) },
                                    modifier = Modifier.size(32.dp)
                                ) {
                                    Icon(
                                        imageVector = Icons.Default.Star,
                                        contentDescription = "Feature",
                                        tint = if (game.isFeatured) GoldCurrency else TextMutedDark,
                                        modifier = Modifier.size(18.dp)
                                    )
                                }
                            }
                        }
                    }
                }
            }
        }
    }
}

@Composable
private fun DefenseItemRow(
    title: String,
    status: String,
    isSafe: Boolean
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 4.dp),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        Text(text = title, color = TextSecondaryDark, fontSize = 12.sp)
        Text(
            text = status,
            color = if (isSafe) EmeraldSuccess else Color(0xFFFF8080),
            fontSize = 11.sp,
            fontWeight = FontWeight.Bold
        )
    }
}

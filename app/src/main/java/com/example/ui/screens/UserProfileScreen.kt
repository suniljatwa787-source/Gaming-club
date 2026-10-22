package com.example.ui.screens

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
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
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.AccountBalance
import androidx.compose.material.icons.filled.AccountCircle
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.ContentCopy
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Download
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material.icons.filled.Fingerprint
import androidx.compose.material.icons.filled.FlashOn
import androidx.compose.material.icons.filled.History
import androidx.compose.material.icons.filled.Lock
import androidx.compose.material.icons.filled.Notifications
import androidx.compose.material.icons.filled.PlayArrow
import androidx.compose.material.icons.filled.QrCode
import androidx.compose.material.icons.filled.Receipt
import androidx.compose.material.icons.filled.Security
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material.icons.filled.Shield
import androidx.compose.material.icons.filled.Speed
import androidx.compose.material.icons.filled.SportsEsports
import androidx.compose.material.icons.filled.Verified
import androidx.compose.material.icons.filled.VolumeUp
import androidx.compose.material.icons.filled.Wifi
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Switch
import androidx.compose.material3.SwitchDefaults
import androidx.compose.material3.Tab
import androidx.compose.material3.TabRow
import androidx.compose.material3.TabRowDefaults
import androidx.compose.material3.TabRowDefaults.tabIndicatorOffset
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalClipboardManager
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.AnnotatedString
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.model.DownloadState
import com.example.model.GameItem
import com.example.model.GameOwnership
import com.example.model.MerchantAccountConfig
import com.example.model.PaymentGatewayType
import com.example.model.TransactionRecord
import com.example.model.TransactionStatus
import com.example.model.TransactionType
import com.example.model.UserAccountProfile
import com.example.ui.theme.CardBorderStroke
import com.example.ui.theme.CardSurfaceDark
import com.example.ui.theme.CrimsonError
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

enum class ProfileSubTab(val title: String) {
    PURCHASED_GAMES("Purchased Games"),
    TRANSACTIONS("Transactions"),
    SETTINGS("Settings")
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun UserProfileScreen(
    profile: UserAccountProfile,
    ownedGames: List<Pair<GameItem, GameOwnership>>,
    transactions: List<TransactionRecord>,
    walletBalance: Double,
    isOwnerModeActive: Boolean,
    onUpdateProfile: (fullName: String, gamerTag: String, email: String) -> Unit,
    onUpdateAccountSetting: (
        biometric: Boolean?,
        twoFactor: Boolean?,
        downloadWifiOnly: Boolean?,
        autoUpdate: Boolean?,
        highPerf: Boolean?,
        soundHaptics: Boolean?,
        notifications: Boolean?,
        defaultGateway: PaymentGatewayType?
    ) -> Unit,
    onClearCache: () -> Unit,
    onLaunchGame: (GameItem) -> Unit,
    onDownloadGame: (GameItem) -> Unit,
    onOpenGameDetail: (GameItem) -> Unit,
    onViewReceipt: (TransactionRecord) -> Unit,
    onOpenOwnerAdmin: () -> Unit,
    onOpenSecurityCompliance: () -> Unit,
    onBrowseStore: () -> Unit,
    modifier: Modifier = Modifier
) {
    var selectedTab by remember { mutableStateOf(ProfileSubTab.PURCHASED_GAMES) }
    var showEditProfileDialog by remember { mutableStateOf(false) }

    val clipboardManager = LocalClipboardManager.current
    var copiedCode by remember { mutableStateOf<String?>(null) }

    // Dialog for editing profile info
    if (showEditProfileDialog) {
        var editName by remember { mutableStateOf(profile.fullName) }
        var editTag by remember { mutableStateOf(profile.gamerTag) }
        var editEmail by remember { mutableStateOf(profile.email) }

        AlertDialog(
            onDismissRequest = { showEditProfileDialog = false },
            title = {
                Text(
                    text = "Edit Gamer Profile",
                    color = TextPrimaryDark,
                    fontWeight = FontWeight.Bold,
                    fontSize = 18.sp
                )
            },
            text = {
                Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
                    OutlinedTextField(
                        value = editName,
                        onValueChange = { editName = it },
                        label = { Text("Full Legal / Payee Name") },
                        singleLine = true,
                        colors = OutlinedTextFieldDefaults.colors(
                            focusedBorderColor = CyberCyanPrimary,
                            unfocusedBorderColor = CardBorderStroke,
                            focusedTextColor = TextPrimaryDark,
                            unfocusedTextColor = TextPrimaryDark
                        ),
                        modifier = Modifier.fillMaxWidth().testTag("edit_profile_name_input")
                    )
                    OutlinedTextField(
                        value = editTag,
                        onValueChange = { editTag = it },
                        label = { Text("Gamer Tag / Handle") },
                        singleLine = true,
                        colors = OutlinedTextFieldDefaults.colors(
                            focusedBorderColor = CyberCyanPrimary,
                            unfocusedBorderColor = CardBorderStroke,
                            focusedTextColor = TextPrimaryDark,
                            unfocusedTextColor = TextPrimaryDark
                        ),
                        modifier = Modifier.fillMaxWidth().testTag("edit_profile_tag_input")
                    )
                    OutlinedTextField(
                        value = editEmail,
                        onValueChange = { editEmail = it },
                        label = { Text("Account Email") },
                        singleLine = true,
                        colors = OutlinedTextFieldDefaults.colors(
                            focusedBorderColor = CyberCyanPrimary,
                            unfocusedBorderColor = CardBorderStroke,
                            focusedTextColor = TextPrimaryDark,
                            unfocusedTextColor = TextPrimaryDark
                        ),
                        modifier = Modifier.fillMaxWidth().testTag("edit_profile_email_input")
                    )
                }
            },
            confirmButton = {
                Button(
                    onClick = {
                        onUpdateProfile(editName.trim(), editTag.trim(), editEmail.trim())
                        showEditProfileDialog = false
                    },
                    colors = ButtonDefaults.buttonColors(containerColor = CyberCyanPrimary),
                    modifier = Modifier.testTag("save_profile_button")
                ) {
                    Text("Save Changes", color = DeepNavySurface, fontWeight = FontWeight.Bold)
                }
            },
            dismissButton = {
                TextButton(
                    onClick = { showEditProfileDialog = false }
                ) {
                    Text("Cancel", color = TextSecondaryDark)
                }
            },
            containerColor = CardSurfaceDark
        )
    }

    LazyColumn(
        modifier = modifier
            .fillMaxSize()
            .background(MidnightObsidian)
            .testTag("user_profile_screen"),
        contentPadding = PaddingValues(bottom = 80.dp)
    ) {
        // 1. Profile Header Hero
        item {
            ProfileHeroSection(
                profile = profile,
                walletBalance = walletBalance,
                ownedCount = ownedGames.size,
                isOwnerModeActive = isOwnerModeActive,
                onEditClick = { showEditProfileDialog = true },
                onOpenOwnerAdmin = onOpenOwnerAdmin
            )
        }

        // 2. Sub-Navigation Tabs
        item {
            SubNavigationTabs(
                selectedTab = selectedTab,
                onSelectTab = { selectedTab = it },
                gamesCount = ownedGames.size,
                txCount = transactions.size
            )
        }

        // 3. Tab Contents
        when (selectedTab) {
            ProfileSubTab.PURCHASED_GAMES -> {
                if (ownedGames.isEmpty()) {
                    item {
                        EmptyPurchasedGamesView(onBrowseStore = onBrowseStore)
                    }
                } else {
                    item {
                        PurchasedGamesHeader(
                            ownedCount = ownedGames.size,
                            isOwnerModeActive = isOwnerModeActive
                        )
                    }
                    items(ownedGames, key = { it.first.id }) { (game, ownership) ->
                        PurchasedGameCard(
                            game = game,
                            ownership = ownership,
                            isOwner = isOwnerModeActive,
                            isCopied = copiedCode == game.id,
                            onCopyLicense = {
                                val licKey = "PV-LIC-787-${game.id.take(6).uppercase()}-89X"
                                clipboardManager.setText(AnnotatedString(licKey))
                                copiedCode = game.id
                            },
                            onPlay = { onLaunchGame(game) },
                            onDownload = { onDownloadGame(game) },
                            onCardClick = { onOpenGameDetail(game) }
                        )
                    }
                }
            }

            ProfileSubTab.TRANSACTIONS -> {
                item {
                    TransactionSummaryCard(
                        transactions = transactions,
                        onOpenSecurityCompliance = onOpenSecurityCompliance
                    )
                }
                if (transactions.isEmpty()) {
                    item {
                        EmptyTransactionsView(onBrowseStore = onBrowseStore)
                    }
                } else {
                    items(transactions, key = { it.id }) { tx ->
                        ProfileTransactionRow(
                            transaction = tx,
                            onViewReceipt = { onViewReceipt(tx) }
                        )
                    }
                }
            }

            ProfileSubTab.SETTINGS -> {
                item {
                    AccountSettingsSection(
                        profile = profile,
                        isOwnerModeActive = isOwnerModeActive,
                        onUpdateSetting = onUpdateAccountSetting,
                        onClearCache = onClearCache,
                        onOpenOwnerAdmin = onOpenOwnerAdmin,
                        onOpenSecurityCompliance = onOpenSecurityCompliance,
                        onEditProfile = { showEditProfileDialog = true }
                    )
                }
            }
        }
    }
}

@Composable
private fun ProfileHeroSection(
    profile: UserAccountProfile,
    walletBalance: Double,
    ownedCount: Int,
    isOwnerModeActive: Boolean,
    onEditClick: () -> Unit,
    onOpenOwnerAdmin: () -> Unit
) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .padding(16.dp)
            .testTag("profile_hero_card"),
        shape = RoundedCornerShape(20.dp),
        colors = CardDefaults.cardColors(containerColor = CardSurfaceDark),
        border = BorderStroke(1.dp, if (isOwnerModeActive) CyberCyanPrimary.copy(alpha = 0.5f) else CardBorderStroke)
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .background(
                    Brush.verticalGradient(
                        colors = listOf(
                            ElectricViolet.copy(alpha = 0.2f),
                            DeepNavySurface
                        )
                    )
                )
                .padding(16.dp)
        ) {
            // Top Row: Avatar, Identity, Edit Button
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically
            ) {
                // Cyber Avatar with Ring
                Box(
                    modifier = Modifier
                        .size(68.dp)
                        .clip(CircleShape)
                        .background(
                            Brush.linearGradient(
                                colors = listOf(CyberCyanPrimary, ElectricViolet, NeonPinkAccent)
                            )
                        )
                        .padding(3.dp),
                    contentAlignment = Alignment.Center
                ) {
                    Box(
                        modifier = Modifier
                            .fillMaxSize()
                            .clip(CircleShape)
                            .background(DeepNavySurface),
                        contentAlignment = Alignment.Center
                    ) {
                        Text(
                            text = if (isOwnerModeActive) "👑" else "🎮",
                            fontSize = 32.sp
                        )
                    }
                }

                Spacer(modifier = Modifier.width(14.dp))

                Column(modifier = Modifier.weight(1f)) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Text(
                            text = profile.gamerTag,
                            color = TextPrimaryDark,
                            fontSize = 18.sp,
                            fontWeight = FontWeight.Bold
                        )
                        Spacer(modifier = Modifier.width(6.dp))
                        Icon(
                            imageVector = Icons.Default.Verified,
                            contentDescription = "Verified",
                            tint = CyberCyanPrimary,
                            modifier = Modifier.size(18.dp)
                        )
                    }

                    Text(
                        text = profile.fullName,
                        color = TextSecondaryDark,
                        fontSize = 13.sp,
                        fontWeight = FontWeight.Medium
                    )

                    Text(
                        text = profile.email,
                        color = TextMutedDark,
                        fontSize = 11.sp,
                        fontFamily = FontFamily.Monospace
                    )
                }

                IconButton(
                    onClick = onEditClick,
                    modifier = Modifier
                        .size(36.dp)
                        .background(DeepNavySurface, CircleShape)
                        .border(1.dp, CardBorderStroke, CircleShape)
                        .testTag("edit_profile_icon_button")
                ) {
                    Icon(
                        imageVector = Icons.Default.Edit,
                        contentDescription = "Edit Profile",
                        tint = CyberCyanPrimary,
                        modifier = Modifier.size(16.dp)
                    )
                }
            }

            Spacer(modifier = Modifier.height(14.dp))

            // Verified Badges Row
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                if (isOwnerModeActive) {
                    OwnerVipBadge(onClick = onOpenOwnerAdmin)
                }
                BadgeChip(
                    label = "⚡ LIFETIME VIP",
                    color = GoldCurrency,
                    bgColor = GoldCurrency.copy(alpha = 0.15f)
                )
                BadgeChip(
                    label = "🛡️ E2EE SECURE",
                    color = EmeraldSuccess,
                    bgColor = EmeraldSuccess.copy(alpha = 0.15f)
                )
            }

            Spacer(modifier = Modifier.height(16.dp))

            // Gamer Level & XP Bar
            Column(modifier = Modifier.fillMaxWidth()) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Text(
                            text = "LEVEL ${profile.playerLevel}",
                            color = CyberCyanPrimary,
                            fontSize = 12.sp,
                            fontWeight = FontWeight.ExtraBold,
                            fontFamily = FontFamily.Monospace
                        )
                        Spacer(modifier = Modifier.width(6.dp))
                        Text(
                            text = "• Cyber Legend",
                            color = TextSecondaryDark,
                            fontSize = 11.sp
                        )
                    }
                    Text(
                        text = "${profile.xpCurrent} / ${profile.xpNextLevel} XP",
                        color = TextMutedDark,
                        fontSize = 10.sp,
                        fontFamily = FontFamily.Monospace
                    )
                }

                Spacer(modifier = Modifier.height(6.dp))

                LinearProgressIndicator(
                    progress = { profile.xpCurrent.toFloat() / profile.xpNextLevel.toFloat() },
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(6.dp)
                        .clip(RoundedCornerShape(3.dp)),
                    color = CyberCyanPrimary,
                    trackColor = DeepNavySurface
                )
            }

            Spacer(modifier = Modifier.height(16.dp))

            // 4 Stats Cards Grid
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                StatCard(
                    title = "GAMES",
                    value = "$ownedCount",
                    subtext = "Unlocked",
                    icon = Icons.Default.SportsEsports,
                    accentColor = CyberCyanPrimary,
                    modifier = Modifier.weight(1f)
                )
                StatCard(
                    title = "PLAYTIME",
                    value = "34.5h",
                    subtext = "Logged",
                    icon = Icons.Default.History,
                    accentColor = ElectricViolet,
                    modifier = Modifier.weight(1f)
                )
                StatCard(
                    title = "BALANCE",
                    value = "$${String.format(Locale.US, "%.0f", walletBalance)}",
                    subtext = "₹${MerchantAccountConfig.getInrAmount(walletBalance)}",
                    icon = Icons.Default.AccountBalance,
                    accentColor = EmeraldSuccess,
                    modifier = Modifier.weight(1f)
                )
                StatCard(
                    title = "SECURITY",
                    value = "100%",
                    subtext = "Encrypted",
                    icon = Icons.Default.Shield,
                    accentColor = GoldCurrency,
                    modifier = Modifier.weight(1f)
                )
            }
        }
    }
}

@Composable
private fun OwnerVipBadge(onClick: () -> Unit) {
    Row(
        modifier = Modifier
            .clip(RoundedCornerShape(8.dp))
            .background(Color(0xFF2E1A47))
            .border(1.dp, GoldCurrency, RoundedCornerShape(8.dp))
            .clickable { onClick() }
            .padding(horizontal = 8.dp, vertical = 4.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Text("👑", fontSize = 12.sp)
        Spacer(modifier = Modifier.width(4.dp))
        Text(
            text = "OWNER VIP",
            color = GoldCurrency,
            fontSize = 11.sp,
            fontWeight = FontWeight.Bold,
            fontFamily = FontFamily.Monospace
        )
    }
}

@Composable
private fun BadgeChip(label: String, color: Color, bgColor: Color) {
    Box(
        modifier = Modifier
            .clip(RoundedCornerShape(8.dp))
            .background(bgColor)
            .border(1.dp, color.copy(alpha = 0.4f), RoundedCornerShape(8.dp))
            .padding(horizontal = 8.dp, vertical = 4.dp)
    ) {
        Text(
            text = label,
            color = color,
            fontSize = 10.sp,
            fontWeight = FontWeight.Bold,
            fontFamily = FontFamily.Monospace
        )
    }
}

@Composable
private fun StatCard(
    title: String,
    value: String,
    subtext: String,
    icon: androidx.compose.ui.graphics.vector.ImageVector,
    accentColor: Color,
    modifier: Modifier = Modifier
) {
    Box(
        modifier = modifier
            .clip(RoundedCornerShape(12.dp))
            .background(DeepNavySurface)
            .border(1.dp, CardBorderStroke, RoundedCornerShape(12.dp))
            .padding(8.dp)
    ) {
        Column(horizontalAlignment = Alignment.Start) {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween,
                modifier = Modifier.fillMaxWidth()
            ) {
                Text(
                    text = title,
                    color = TextMutedDark,
                    fontSize = 9.sp,
                    fontWeight = FontWeight.Bold,
                    fontFamily = FontFamily.Monospace
                )
                Icon(
                    imageVector = icon,
                    contentDescription = null,
                    tint = accentColor,
                    modifier = Modifier.size(12.dp)
                )
            }
            Spacer(modifier = Modifier.height(4.dp))
            Text(
                text = value,
                color = TextPrimaryDark,
                fontSize = 14.sp,
                fontWeight = FontWeight.Bold
            )
            Text(
                text = subtext,
                color = accentColor,
                fontSize = 9.sp,
                fontWeight = FontWeight.Medium
            )
        }
    }
}

@Composable
private fun SubNavigationTabs(
    selectedTab: ProfileSubTab,
    onSelectTab: (ProfileSubTab) -> Unit,
    gamesCount: Int,
    txCount: Int
) {
    val tabs = ProfileSubTab.values()
    val selectedIndex = tabs.indexOf(selectedTab)

    TabRow(
        selectedTabIndex = selectedIndex,
        containerColor = DeepNavySurface,
        contentColor = CyberCyanPrimary,
        indicator = { tabPositions ->
            TabRowDefaults.SecondaryIndicator(
                Modifier.tabIndicatorOffset(tabPositions[selectedIndex]),
                color = CyberCyanPrimary,
                height = 3.dp
            )
        },
        divider = { HorizontalDivider(color = CardBorderStroke) },
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp)
            .clip(RoundedCornerShape(12.dp))
            .testTag("profile_sub_tabs")
    ) {
        tabs.forEach { tab ->
            val labelText = when (tab) {
                ProfileSubTab.PURCHASED_GAMES -> "Games ($gamesCount)"
                ProfileSubTab.TRANSACTIONS -> "History ($txCount)"
                ProfileSubTab.SETTINGS -> "Settings"
            }
            val icon = when (tab) {
                ProfileSubTab.PURCHASED_GAMES -> Icons.Default.SportsEsports
                ProfileSubTab.TRANSACTIONS -> Icons.Default.Receipt
                ProfileSubTab.SETTINGS -> Icons.Default.Settings
            }

            Tab(
                selected = selectedTab == tab,
                onClick = { onSelectTab(tab) },
                text = {
                    Text(
                        text = labelText,
                        fontSize = 11.sp,
                        fontWeight = if (selectedTab == tab) FontWeight.Bold else FontWeight.Medium,
                        color = if (selectedTab == tab) CyberCyanPrimary else TextSecondaryDark
                    )
                },
                icon = {
                    Icon(
                        imageVector = icon,
                        contentDescription = tab.title,
                        modifier = Modifier.size(16.dp),
                        tint = if (selectedTab == tab) CyberCyanPrimary else TextMutedDark
                    )
                },
                modifier = Modifier.testTag("tab_${tab.name.lowercase()}")
            )
        }
    }
}

@Composable
private fun PurchasedGamesHeader(ownedCount: Int, isOwnerModeActive: Boolean) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp, vertical = 12.dp),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        Column {
            Text(
                text = "LICENSED & PURCHASED GAMES",
                color = TextSecondaryDark,
                fontSize = 11.sp,
                fontWeight = FontWeight.Bold,
                fontFamily = FontFamily.Monospace
            )
            Text(
                text = if (isOwnerModeActive) "$ownedCount Titles • 100% Free Owner VIP Pass" else "$ownedCount Titles in your library",
                color = if (isOwnerModeActive) EmeraldSuccess else TextMutedDark,
                fontSize = 12.sp
            )
        }

        Box(
            modifier = Modifier
                .clip(RoundedCornerShape(8.dp))
                .background(EmeraldSuccess.copy(alpha = 0.15f))
                .border(1.dp, EmeraldSuccess.copy(alpha = 0.4f), RoundedCornerShape(8.dp))
                .padding(horizontal = 10.dp, vertical = 4.dp)
        ) {
            Text(
                text = "✓ ALL ACTIVE",
                color = EmeraldSuccess,
                fontSize = 10.sp,
                fontWeight = FontWeight.Bold,
                fontFamily = FontFamily.Monospace
            )
        }
    }
}

@Composable
private fun PurchasedGameCard(
    game: GameItem,
    ownership: GameOwnership,
    isOwner: Boolean,
    isCopied: Boolean,
    onCopyLicense: () -> Unit,
    onPlay: () -> Unit,
    onDownload: () -> Unit,
    onCardClick: () -> Unit
) {
    val gradientBrush = remember(game.gradientColors) {
        Brush.linearGradient(
            colors = game.gradientColors.map { Color(it) }
        )
    }

    val licenseKey = remember(game.id) {
        "PV-LIC-787-${game.id.take(6).uppercase()}-89X"
    }

    Card(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp, vertical = 6.dp)
            .clickable { onCardClick() }
            .testTag("purchased_game_card_${game.id}"),
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(containerColor = CardSurfaceDark),
        border = BorderStroke(1.dp, CardBorderStroke)
    ) {
        Column(modifier = Modifier.fillMaxWidth().padding(12.dp)) {
            // Game Row: Thumbnail banner, Title, Category, Price/Free pill
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically
            ) {
                // Game Cover Gradient Thumbnail
                Box(
                    modifier = Modifier
                        .size(56.dp)
                        .clip(RoundedCornerShape(12.dp))
                        .background(gradientBrush),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        imageVector = Icons.Default.SportsEsports,
                        contentDescription = null,
                        tint = Color.White.copy(alpha = 0.9f),
                        modifier = Modifier.size(28.dp)
                    )
                }

                Spacer(modifier = Modifier.width(12.dp))

                Column(modifier = Modifier.weight(1f)) {
                    Text(
                        text = game.title,
                        color = TextPrimaryDark,
                        fontSize = 15.sp,
                        fontWeight = FontWeight.Bold,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis
                    )
                    Spacer(modifier = Modifier.height(2.dp))
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Text(
                            text = game.category.name,
                            color = CyberCyanPrimary,
                            fontSize = 11.sp,
                            fontWeight = FontWeight.Bold,
                            fontFamily = FontFamily.Monospace
                        )
                        Spacer(modifier = Modifier.width(8.dp))
                        Text(
                            text = "${game.downloadSizeMb} MB",
                            color = TextMutedDark,
                            fontSize = 11.sp
                        )
                    }
                }

                // Ownership Pill
                Box(
                    modifier = Modifier
                        .clip(RoundedCornerShape(8.dp))
                        .background(if (isOwner) GoldCurrency.copy(alpha = 0.15f) else EmeraldSuccess.copy(alpha = 0.15f))
                        .border(
                            1.dp,
                            if (isOwner) GoldCurrency.copy(alpha = 0.5f) else EmeraldSuccess.copy(alpha = 0.5f),
                            RoundedCornerShape(8.dp)
                        )
                        .padding(horizontal = 8.dp, vertical = 4.dp)
                ) {
                    Text(
                        text = if (isOwner) "👑 VIP FREE" else "OWNED",
                        color = if (isOwner) GoldCurrency else EmeraldSuccess,
                        fontSize = 10.sp,
                        fontWeight = FontWeight.Bold,
                        fontFamily = FontFamily.Monospace
                    )
                }
            }

            Spacer(modifier = Modifier.height(10.dp))

            // License Key & Playtime Row
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .background(DeepNavySurface, RoundedCornerShape(8.dp))
                    .border(1.dp, CardBorderStroke, RoundedCornerShape(8.dp))
                    .padding(horizontal = 10.dp, vertical = 6.dp)
            ) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Icon(
                            imageVector = Icons.Default.Lock,
                            contentDescription = "License",
                            tint = TextMutedDark,
                            modifier = Modifier.size(12.dp)
                        )
                        Spacer(modifier = Modifier.width(6.dp))
                        Text(
                            text = licenseKey,
                            color = TextSecondaryDark,
                            fontSize = 11.sp,
                            fontFamily = FontFamily.Monospace
                        )
                    }

                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        modifier = Modifier.clickable { onCopyLicense() }
                    ) {
                        Icon(
                            imageVector = if (isCopied) Icons.Default.Check else Icons.Default.ContentCopy,
                            contentDescription = "Copy License",
                            tint = if (isCopied) EmeraldSuccess else CyberCyanPrimary,
                            modifier = Modifier.size(14.dp)
                        )
                        Spacer(modifier = Modifier.width(4.dp))
                        Text(
                            text = if (isCopied) "Copied" else "Copy",
                            color = if (isCopied) EmeraldSuccess else CyberCyanPrimary,
                            fontSize = 10.sp,
                            fontWeight = FontWeight.Bold
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.height(10.dp))

            // Action Buttons: Play Instant & Download/Install
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                Button(
                    onClick = onPlay,
                    modifier = Modifier
                        .weight(1f)
                        .height(38.dp)
                        .testTag("play_game_button_${game.id}"),
                    colors = ButtonDefaults.buttonColors(containerColor = CyberCyanPrimary),
                    shape = RoundedCornerShape(8.dp),
                    contentPadding = PaddingValues(horizontal = 10.dp, vertical = 4.dp)
                ) {
                    Icon(
                        imageVector = Icons.Default.PlayArrow,
                        contentDescription = "Play",
                        tint = DeepNavySurface,
                        modifier = Modifier.size(16.dp)
                    )
                    Spacer(modifier = Modifier.width(4.dp))
                    Text(
                        text = "Play Instant",
                        color = DeepNavySurface,
                        fontSize = 12.sp,
                        fontWeight = FontWeight.Bold
                    )
                }

                OutlinedButton(
                    onClick = onDownload,
                    modifier = Modifier
                        .weight(1f)
                        .height(38.dp)
                        .testTag("download_game_button_${game.id}"),
                    border = BorderStroke(1.dp, CardBorderStroke),
                    shape = RoundedCornerShape(8.dp),
                    contentPadding = PaddingValues(horizontal = 10.dp, vertical = 4.dp)
                ) {
                    val (icon, label) = when (ownership.downloadState) {
                        DownloadState.INSTALLED -> Icons.Default.CheckCircle to "Installed"
                        DownloadState.DOWNLOADING -> Icons.Default.Download to "${ownership.downloadProgressPercent}%"
                        else -> Icons.Default.Download to "Download"
                    }
                    Icon(
                        imageVector = icon,
                        contentDescription = label,
                        tint = if (ownership.downloadState == DownloadState.INSTALLED) EmeraldSuccess else TextPrimaryDark,
                        modifier = Modifier.size(14.dp)
                    )
                    Spacer(modifier = Modifier.width(4.dp))
                    Text(
                        text = label,
                        color = TextPrimaryDark,
                        fontSize = 12.sp,
                        fontWeight = FontWeight.Medium
                    )
                }
            }
        }
    }
}

@Composable
private fun TransactionSummaryCard(
    transactions: List<TransactionRecord>,
    onOpenSecurityCompliance: () -> Unit
) {
    val totalPurchases = transactions.filter { it.type == TransactionType.GAME_PURCHASE }.sumOf { it.amountUsd }
    val totalTopups = transactions.filter { it.type == TransactionType.WALLET_TOPUP }.sumOf { it.amountUsd }

    Card(
        modifier = Modifier
            .fillMaxWidth()
            .padding(16.dp)
            .testTag("transaction_summary_card"),
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(containerColor = CardSurfaceDark),
        border = BorderStroke(1.dp, CardBorderStroke)
    ) {
        Column(modifier = Modifier.fillMaxWidth().padding(14.dp)) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = "TRANSACTION LEDGER & SETTLEMENTS",
                    color = TextSecondaryDark,
                    fontSize = 11.sp,
                    fontWeight = FontWeight.Bold,
                    fontFamily = FontFamily.Monospace
                )
                Row(
                    modifier = Modifier
                        .clickable { onOpenSecurityCompliance() }
                        .padding(4.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Icon(Icons.Default.Shield, contentDescription = "PCI-DSS", tint = EmeraldSuccess, modifier = Modifier.size(13.dp))
                    Spacer(modifier = Modifier.width(3.dp))
                    Text("PCI-DSS Audited", color = EmeraldSuccess, fontSize = 10.sp, fontWeight = FontWeight.Bold)
                }
            }

            Spacer(modifier = Modifier.height(10.dp))

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                Box(
                    modifier = Modifier
                        .weight(1f)
                        .background(DeepNavySurface, RoundedCornerShape(10.dp))
                        .padding(10.dp)
                ) {
                    Column {
                        Text("TOTAL SPENT", color = TextMutedDark, fontSize = 9.sp, fontWeight = FontWeight.Bold)
                        Text(
                            text = "$${String.format(Locale.US, "%.2f", totalPurchases)}",
                            color = TextPrimaryDark,
                            fontSize = 15.sp,
                            fontWeight = FontWeight.Bold
                        )
                        Text(
                            text = "₹${MerchantAccountConfig.getInrAmount(totalPurchases)}",
                            color = CyberCyanPrimary,
                            fontSize = 10.sp,
                            fontWeight = FontWeight.Medium
                        )
                    }
                }

                Box(
                    modifier = Modifier
                        .weight(1f)
                        .background(DeepNavySurface, RoundedCornerShape(10.dp))
                        .padding(10.dp)
                ) {
                    Column {
                        Text("TOTAL LOADED", color = TextMutedDark, fontSize = 9.sp, fontWeight = FontWeight.Bold)
                        Text(
                            text = "$${String.format(Locale.US, "%.2f", totalTopups)}",
                            color = TextPrimaryDark,
                            fontSize = 15.sp,
                            fontWeight = FontWeight.Bold
                        )
                        Text(
                            text = "₹${MerchantAccountConfig.getInrAmount(totalTopups)}",
                            color = EmeraldSuccess,
                            fontSize = 10.sp,
                            fontWeight = FontWeight.Medium
                        )
                    }
                }
            }
        }
    }
}

@Composable
private fun ProfileTransactionRow(
    transaction: TransactionRecord,
    onViewReceipt: () -> Unit
) {
    val sdf = remember { SimpleDateFormat("dd MMM yyyy, hh:mm a", Locale.getDefault()) }
    val formattedDate = remember(transaction.timestamp) { sdf.format(Date(transaction.timestamp)) }
    val inrVal = remember(transaction.amountUsd) { MerchantAccountConfig.getInrAmount(transaction.amountUsd) }

    Card(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp, vertical = 4.dp)
            .clickable { onViewReceipt() }
            .testTag("tx_row_${transaction.id}"),
        shape = RoundedCornerShape(12.dp),
        colors = CardDefaults.cardColors(containerColor = CardSurfaceDark),
        border = BorderStroke(1.dp, CardBorderStroke)
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(12.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            // Gateway Icon Box
            Box(
                modifier = Modifier
                    .size(42.dp)
                    .clip(RoundedCornerShape(10.dp))
                    .background(
                        when {
                            transaction.gatewayType == PaymentGatewayType.BINANCE_PAY -> Color(0xFFF0B90B).copy(alpha = 0.15f)
                            transaction.type == TransactionType.WALLET_TOPUP -> EmeraldSuccess.copy(alpha = 0.15f)
                            transaction.type == TransactionType.GAME_REFUND -> GoldCurrency.copy(alpha = 0.15f)
                            transaction.type == TransactionType.COIN_REWARD -> GoldCurrency.copy(alpha = 0.15f)
                            else -> CyberCyanPrimary.copy(alpha = 0.15f)
                        }
                    ),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    imageVector = when (transaction.gatewayType) {
                        PaymentGatewayType.BINANCE_PAY -> Icons.Default.FlashOn
                        PaymentGatewayType.UPI -> Icons.Default.QrCode
                        PaymentGatewayType.CREDIT_CARD -> Icons.Default.AccountBalance
                        PaymentGatewayType.PAYPAL -> Icons.Default.AccountBalance
                        else -> Icons.Default.Receipt
                    },
                    contentDescription = null,
                    tint = when {
                        transaction.gatewayType == PaymentGatewayType.BINANCE_PAY -> Color(0xFFF0B90B)
                        transaction.type == TransactionType.WALLET_TOPUP -> EmeraldSuccess
                        transaction.type == TransactionType.GAME_REFUND -> GoldCurrency
                        else -> CyberCyanPrimary
                    },
                    modifier = Modifier.size(20.dp)
                )
            }

            Spacer(modifier = Modifier.width(12.dp))

            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = transaction.title,
                    color = TextPrimaryDark,
                    fontSize = 13.sp,
                    fontWeight = FontWeight.Bold,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis
                )
                Spacer(modifier = Modifier.height(2.dp))
                Text(
                    text = transaction.methodDescriptor,
                    color = if (transaction.gatewayType == PaymentGatewayType.BINANCE_PAY) Color(0xFFF0B90B) else TextSecondaryDark,
                    fontSize = 11.sp,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis
                )
                Text(
                    text = formattedDate,
                    color = TextMutedDark,
                    fontSize = 10.sp
                )
            }

            Spacer(modifier = Modifier.width(8.dp))

            Column(horizontalAlignment = Alignment.End) {
                val prefix = when (transaction.type) {
                    TransactionType.WALLET_TOPUP, TransactionType.COIN_REWARD -> "+"
                    TransactionType.GAME_REFUND -> "+"
                    else -> "-"
                }
                val amountColor = when {
                    transaction.gatewayType == PaymentGatewayType.BINANCE_PAY -> Color(0xFFF0B90B)
                    transaction.type == TransactionType.WALLET_TOPUP || transaction.type == TransactionType.COIN_REWARD -> EmeraldSuccess
                    transaction.type == TransactionType.GAME_REFUND -> GoldCurrency
                    else -> TextPrimaryDark
                }

                if (transaction.cryptoAmount != null) {
                    Text(
                        text = "$prefix${transaction.cryptoAmount} ${transaction.cryptoCurrency ?: "USDT"}",
                        color = Color(0xFFF0B90B),
                        fontSize = 13.sp,
                        fontWeight = FontWeight.Bold,
                        fontFamily = FontFamily.Monospace
                    )
                    Text(
                        text = "≈ $prefix₹$inrVal ($${String.format(Locale.US, "%.2f", transaction.amountUsd)})",
                        color = TextSecondaryDark,
                        fontSize = 10.sp,
                        fontFamily = FontFamily.Monospace
                    )
                } else {
                    Text(
                        text = "$prefix$${String.format(Locale.US, "%.2f", transaction.amountUsd)}",
                        color = amountColor,
                        fontSize = 14.sp,
                        fontWeight = FontWeight.Bold,
                        fontFamily = FontFamily.Monospace
                    )
                    Text(
                        text = "$prefix₹$inrVal",
                        color = TextSecondaryDark,
                        fontSize = 10.sp,
                        fontFamily = FontFamily.Monospace
                    )
                }

                Spacer(modifier = Modifier.height(4.dp))

                Box(
                    modifier = Modifier
                        .clip(RoundedCornerShape(6.dp))
                        .background(
                            when (transaction.status) {
                                TransactionStatus.COMPLETED -> EmeraldSuccess.copy(alpha = 0.15f)
                                TransactionStatus.REFUNDED -> GoldCurrency.copy(alpha = 0.15f)
                                TransactionStatus.FAILED -> CrimsonError.copy(alpha = 0.15f)
                            }
                        )
                        .padding(horizontal = 6.dp, vertical = 2.dp)
                ) {
                    Text(
                        text = transaction.status.name,
                        color = when (transaction.status) {
                            TransactionStatus.COMPLETED -> EmeraldSuccess
                            TransactionStatus.REFUNDED -> GoldCurrency
                            TransactionStatus.FAILED -> CrimsonError
                        },
                        fontSize = 9.sp,
                        fontWeight = FontWeight.Bold
                    )
                }
            }
        }
    }
}

@Composable
private fun AccountSettingsSection(
    profile: UserAccountProfile,
    isOwnerModeActive: Boolean,
    onUpdateSetting: (
        biometric: Boolean?,
        twoFactor: Boolean?,
        downloadWifiOnly: Boolean?,
        autoUpdate: Boolean?,
        highPerf: Boolean?,
        soundHaptics: Boolean?,
        notifications: Boolean?,
        defaultGateway: PaymentGatewayType?
    ) -> Unit,
    onClearCache: () -> Unit,
    onOpenOwnerAdmin: () -> Unit,
    onOpenSecurityCompliance: () -> Unit,
    onEditProfile: () -> Unit
) {
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(14.dp)
    ) {
        // Card 1: Dual Automatic Receiving Accounts (INR via UPI & Crypto via Binance Pay)
        SettingsCard(title = "AUTOMATIC SETTLEMENT ACCOUNTS", icon = Icons.Default.AccountBalance) {
            Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                // Bank of Baroda UPI (INR Payout)
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .background(DeepNavySurface, RoundedCornerShape(10.dp))
                        .border(1.dp, CardBorderStroke, RoundedCornerShape(10.dp))
                        .padding(12.dp)
                ) {
                    Column {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Text(
                                    text = "🇮🇳 ${MerchantAccountConfig.BANK_NAME}",
                                    color = CyberCyanPrimary,
                                    fontSize = 13.sp,
                                    fontWeight = FontWeight.Bold
                                )
                                Spacer(modifier = Modifier.width(6.dp))
                                Text(
                                    text = "INR",
                                    color = Color(0xFFF97316),
                                    fontSize = 10.sp,
                                    fontWeight = FontWeight.Bold
                                )
                            }
                            Text(
                                text = "VERIFIED RECEIVER",
                                color = EmeraldSuccess,
                                fontSize = 10.sp,
                                fontWeight = FontWeight.Bold,
                                fontFamily = FontFamily.Monospace
                            )
                        }
                        Spacer(modifier = Modifier.height(4.dp))
                        Text(
                            text = "Payee: ${MerchantAccountConfig.PAYEE_NAME}",
                            color = TextPrimaryDark,
                            fontSize = 12.sp,
                            fontWeight = FontWeight.Medium
                        )
                        Text(
                            text = "UPI: ${MerchantAccountConfig.PAYEE_UPI_ID}",
                            color = TextSecondaryDark,
                            fontSize = 11.sp,
                            fontFamily = FontFamily.Monospace
                        )
                        Spacer(modifier = Modifier.height(4.dp))
                        Text(
                            text = "All customer INR purchases route directly to this account with zero gateway fee.",
                            color = TextMutedDark,
                            fontSize = 10.sp
                        )
                    }
                }

                // Binance Pay (Crypto Payout)
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .background(DeepNavySurface, RoundedCornerShape(10.dp))
                        .border(1.dp, Color(0xFFF0B90B).copy(alpha = 0.5f), RoundedCornerShape(10.dp))
                        .padding(12.dp)
                ) {
                    Column {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Text(
                                    text = "💎 BINANCE PAY",
                                    color = Color(0xFFF0B90B),
                                    fontSize = 13.sp,
                                    fontWeight = FontWeight.Bold
                                )
                                Spacer(modifier = Modifier.width(6.dp))
                                Text(
                                    text = "CRYPTO",
                                    color = Color(0xFFF0B90B),
                                    fontSize = 10.sp,
                                    fontWeight = FontWeight.Bold
                                )
                            }
                            Text(
                                text = "0% FEE SETTLEMENT",
                                color = EmeraldSuccess,
                                fontSize = 10.sp,
                                fontWeight = FontWeight.Bold,
                                fontFamily = FontFamily.Monospace
                            )
                        }
                        Spacer(modifier = Modifier.height(4.dp))
                        Text(
                            text = "Payee: ${MerchantAccountConfig.BINANCE_PAYEE_NAME}",
                            color = TextPrimaryDark,
                            fontSize = 12.sp,
                            fontWeight = FontWeight.Medium
                        )
                        Text(
                            text = "Binance Pay ID: ${MerchantAccountConfig.BINANCE_PAY_ID}",
                            color = Color(0xFFF0B90B),
                            fontSize = 12.sp,
                            fontWeight = FontWeight.Bold,
                            fontFamily = FontFamily.Monospace
                        )
                        Spacer(modifier = Modifier.height(4.dp))
                        Text(
                            text = "Supported: USDT, BTC, ETH, BNB, SOL. Direct blockchain auto-credit to your Binance wallet.",
                            color = TextMutedDark,
                            fontSize = 10.sp
                        )
                    }
                }
            }
        }

        // Card 2: Security & Anti-Crack Guardian
        SettingsCard(title = "SECURITY & ANTI-CRACK GUARDIAN", icon = Icons.Default.Security) {
            SettingToggleRow(
                title = "Biometric / Face Authentication",
                subtitle = "Require fingerprint/face confirmation for checkouts",
                isChecked = profile.biometricAuthEnabled,
                onCheckedChange = { onUpdateSetting(it, null, null, null, null, null, null, null) },
                icon = Icons.Default.Fingerprint,
                testTag = "setting_toggle_biometric"
            )

            HorizontalDivider(color = CardBorderStroke.copy(alpha = 0.5f), modifier = Modifier.padding(vertical = 8.dp))

            SettingToggleRow(
                title = "Two-Factor Authentication (2FA)",
                subtitle = "Require 3DS 2.0 Strong Customer Authentication",
                isChecked = profile.twoFactorAuthEnabled,
                onCheckedChange = { onUpdateSetting(null, it, null, null, null, null, null, null) },
                icon = Icons.Default.Lock,
                testTag = "setting_toggle_2fa"
            )

            HorizontalDivider(color = CardBorderStroke.copy(alpha = 0.5f), modifier = Modifier.padding(vertical = 8.dp))

            // Anti-Crack Telemetry Row
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Column(modifier = Modifier.weight(1f)) {
                    Text("Anti-Tamper & Frida Shield", color = TextPrimaryDark, fontSize = 13.sp, fontWeight = FontWeight.Bold)
                    Text("SU binaries, hooking frameworks & debuggers blocked", color = TextMutedDark, fontSize = 11.sp)
                }
                Box(
                    modifier = Modifier
                        .clip(RoundedCornerShape(8.dp))
                        .background(EmeraldSuccess.copy(alpha = 0.15f))
                        .padding(horizontal = 8.dp, vertical = 4.dp)
                ) {
                    Text("ACTIVE", color = EmeraldSuccess, fontSize = 10.sp, fontWeight = FontWeight.Bold)
                }
            }

            Spacer(modifier = Modifier.height(10.dp))

            Button(
                onClick = onOpenSecurityCompliance,
                modifier = Modifier
                    .fillMaxWidth()
                    .height(40.dp)
                    .testTag("view_security_audit_button"),
                colors = ButtonDefaults.buttonColors(containerColor = DeepNavySurface),
                border = BorderStroke(1.dp, CardBorderStroke),
                shape = RoundedCornerShape(10.dp)
            ) {
                Icon(Icons.Default.Shield, contentDescription = null, tint = CyberCyanPrimary, modifier = Modifier.size(16.dp))
                Spacer(modifier = Modifier.width(8.dp))
                Text("View PCI-DSS & Security Audit Details", color = TextPrimaryDark, fontSize = 12.sp, fontWeight = FontWeight.Medium)
            }
        }

        // Card 3: Game & Network Preferences
        SettingsCard(title = "GAMING & DOWNLOAD PREFERENCES", icon = Icons.Default.Speed) {
            SettingToggleRow(
                title = "Download Over Wi-Fi Only",
                subtitle = "Save mobile cellular data when downloading heavy game assets",
                isChecked = profile.downloadWifiOnly,
                onCheckedChange = { onUpdateSetting(null, null, it, null, null, null, null, null) },
                icon = Icons.Default.Wifi,
                testTag = "setting_toggle_wifi"
            )

            HorizontalDivider(color = CardBorderStroke.copy(alpha = 0.5f), modifier = Modifier.padding(vertical = 8.dp))

            SettingToggleRow(
                title = "Auto-Update Installed Games",
                subtitle = "Silently download latest game patches and textures in background",
                isChecked = profile.autoUpdateGames,
                onCheckedChange = { onUpdateSetting(null, null, null, it, null, null, null, null) },
                icon = Icons.Default.Download,
                testTag = "setting_toggle_autoupdate"
            )

            HorizontalDivider(color = CardBorderStroke.copy(alpha = 0.5f), modifier = Modifier.padding(vertical = 8.dp))

            SettingToggleRow(
                title = "120Hz Ultra Performance Mode",
                subtitle = "Maximize GPU render rate for arcade games",
                isChecked = profile.highPerformanceMode,
                onCheckedChange = { onUpdateSetting(null, null, null, null, it, null, null, null) },
                icon = Icons.Default.Speed,
                testTag = "setting_toggle_highperf"
            )

            HorizontalDivider(color = CardBorderStroke.copy(alpha = 0.5f), modifier = Modifier.padding(vertical = 8.dp))

            SettingToggleRow(
                title = "Audio & Haptic Feedback",
                subtitle = "Vibration and retro sound effects on laser fire and coin collect",
                isChecked = profile.soundHapticsEnabled,
                onCheckedChange = { onUpdateSetting(null, null, null, null, null, it, null, null) },
                icon = Icons.Default.VolumeUp,
                testTag = "setting_toggle_haptics"
            )

            HorizontalDivider(color = CardBorderStroke.copy(alpha = 0.5f), modifier = Modifier.padding(vertical = 8.dp))

            SettingToggleRow(
                title = "Push Notifications",
                subtitle = "Alerts for price discounts and new game drops",
                isChecked = profile.pushNotificationsEnabled,
                onCheckedChange = { onUpdateSetting(null, null, null, null, null, null, it, null) },
                icon = Icons.Default.Notifications,
                testTag = "setting_toggle_notifications"
            )
        }

        // Card 4: Storage & Maintenance
        SettingsCard(title = "STORAGE & CACHE MANAGEMENT", icon = Icons.Default.Delete) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Column {
                    Text("Temporary Game Cache", color = TextPrimaryDark, fontSize = 13.sp, fontWeight = FontWeight.Bold)
                    Text(
                        text = if (profile.cacheSizeMb > 0) "${String.format(Locale.US, "%.1f", profile.cacheSizeMb)} MB cached assets" else "Cache is clean (0.0 MB)",
                        color = if (profile.cacheSizeMb > 0) TextSecondaryDark else EmeraldSuccess,
                        fontSize = 11.sp
                    )
                }

                Button(
                    onClick = onClearCache,
                    enabled = profile.cacheSizeMb > 0,
                    colors = ButtonDefaults.buttonColors(containerColor = CrimsonError.copy(alpha = 0.2f)),
                    shape = RoundedCornerShape(8.dp),
                    border = BorderStroke(1.dp, CrimsonError.copy(alpha = 0.5f)),
                    modifier = Modifier.testTag("clear_cache_button")
                ) {
                    Text(
                        text = if (profile.cacheSizeMb > 0) "Clear Cache" else "Cleaned",
                        color = if (profile.cacheSizeMb > 0) CrimsonError else TextMutedDark,
                        fontSize = 11.sp,
                        fontWeight = FontWeight.Bold
                    )
                }
            }
        }

        // Card 5: Exclusive Creator & Owner Console
        SettingsCard(title = "CREATOR & OWNER CONTROLS", icon = Icons.Default.Lock) {
            Column {
                Text(
                    text = "Master PIN Protection Active",
                    color = TextPrimaryDark,
                    fontSize = 13.sp,
                    fontWeight = FontWeight.Bold
                )
                Text(
                    text = "Manage pricing, toggle VIP free access, and inspect anti-tamper defenses.",
                    color = TextMutedDark,
                    fontSize = 11.sp
                )
                Spacer(modifier = Modifier.height(10.dp))
                Button(
                    onClick = onOpenOwnerAdmin,
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(42.dp)
                        .testTag("launch_owner_admin_button"),
                    colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF2E1A47)),
                    shape = RoundedCornerShape(10.dp),
                    border = BorderStroke(1.dp, GoldCurrency)
                ) {
                    Text("👑 Open Owner Admin Panel (PIN 7878)", color = GoldCurrency, fontSize = 13.sp, fontWeight = FontWeight.Bold)
                }
            }
        }
    }
}

@Composable
private fun SettingsCard(
    title: String,
    icon: androidx.compose.ui.graphics.vector.ImageVector,
    content: @Composable () -> Unit
) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(containerColor = CardSurfaceDark),
        border = BorderStroke(1.dp, CardBorderStroke)
    ) {
        Column(modifier = Modifier.fillMaxWidth().padding(14.dp)) {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                modifier = Modifier.fillMaxWidth()
            ) {
                Icon(imageVector = icon, contentDescription = null, tint = CyberCyanPrimary, modifier = Modifier.size(16.dp))
                Spacer(modifier = Modifier.width(8.dp))
                Text(
                    text = title,
                    color = TextSecondaryDark,
                    fontSize = 11.sp,
                    fontWeight = FontWeight.Bold,
                    fontFamily = FontFamily.Monospace
                )
            }
            Spacer(modifier = Modifier.height(12.dp))
            content()
        }
    }
}

@Composable
private fun SettingToggleRow(
    title: String,
    subtitle: String,
    isChecked: Boolean,
    onCheckedChange: (Boolean) -> Unit,
    icon: androidx.compose.ui.graphics.vector.ImageVector,
    testTag: String
) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        Row(
            modifier = Modifier.weight(1f),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Icon(
                imageVector = icon,
                contentDescription = null,
                tint = TextSecondaryDark,
                modifier = Modifier.size(18.dp)
            )
            Spacer(modifier = Modifier.width(10.dp))
            Column {
                Text(
                    text = title,
                    color = TextPrimaryDark,
                    fontSize = 13.sp,
                    fontWeight = FontWeight.Bold
                )
                Text(
                    text = subtitle,
                    color = TextMutedDark,
                    fontSize = 11.sp
                )
            }
        }

        Spacer(modifier = Modifier.width(12.dp))

        Switch(
            checked = isChecked,
            onCheckedChange = onCheckedChange,
            colors = SwitchDefaults.colors(
                checkedThumbColor = DeepNavySurface,
                checkedTrackColor = CyberCyanPrimary,
                uncheckedThumbColor = TextMutedDark,
                uncheckedTrackColor = DeepNavySurface
            ),
            modifier = Modifier.testTag(testTag)
        )
    }
}

@Composable
private fun EmptyPurchasedGamesView(onBrowseStore: () -> Unit) {
    Box(
        modifier = Modifier
            .fillMaxWidth()
            .padding(32.dp),
        contentAlignment = Alignment.Center
    ) {
        Column(horizontalAlignment = Alignment.CenterHorizontally) {
            Icon(
                imageVector = Icons.Default.SportsEsports,
                contentDescription = null,
                tint = TextMutedDark,
                modifier = Modifier.size(48.dp)
            )
            Spacer(modifier = Modifier.height(12.dp))
            Text("No Purchased Games Found", color = TextPrimaryDark, fontSize = 16.sp, fontWeight = FontWeight.Bold)
            Spacer(modifier = Modifier.height(6.dp))
            Text(
                "Browse the premium store and unlock games with UPI or Cards.",
                color = TextSecondaryDark,
                fontSize = 12.sp,
                textAlign = TextAlign.Center
            )
            Spacer(modifier = Modifier.height(16.dp))
            Button(
                onClick = onBrowseStore,
                colors = ButtonDefaults.buttonColors(containerColor = CyberCyanPrimary),
                shape = RoundedCornerShape(10.dp),
                modifier = Modifier.testTag("explore_store_button")
            ) {
                Text("Explore Game Store", color = DeepNavySurface, fontWeight = FontWeight.Bold)
            }
        }
    }
}

@Composable
private fun EmptyTransactionsView(onBrowseStore: () -> Unit) {
    Box(
        modifier = Modifier
            .fillMaxWidth()
            .padding(32.dp),
        contentAlignment = Alignment.Center
    ) {
        Column(horizontalAlignment = Alignment.CenterHorizontally) {
            Icon(
                imageVector = Icons.Default.Receipt,
                contentDescription = null,
                tint = TextMutedDark,
                modifier = Modifier.size(48.dp)
            )
            Spacer(modifier = Modifier.height(12.dp))
            Text("No Transactions Yet", color = TextPrimaryDark, fontSize = 16.sp, fontWeight = FontWeight.Bold)
            Spacer(modifier = Modifier.height(6.dp))
            Text(
                "Your game purchases and wallet top-ups will show here with verified receipts.",
                color = TextSecondaryDark,
                fontSize = 12.sp,
                textAlign = TextAlign.Center
            )
            Spacer(modifier = Modifier.height(16.dp))
            Button(
                onClick = onBrowseStore,
                colors = ButtonDefaults.buttonColors(containerColor = CyberCyanPrimary),
                shape = RoundedCornerShape(10.dp)
            ) {
                Text("Browse Store", color = DeepNavySurface, fontWeight = FontWeight.Bold)
            }
        }
    }
}

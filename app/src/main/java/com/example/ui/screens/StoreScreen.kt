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
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.AccountBalanceWallet
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.FlashOn
import androidx.compose.material.icons.filled.Search
import androidx.compose.material.icons.filled.Security
import androidx.compose.material.icons.filled.SportsEsports
import androidx.compose.material.icons.filled.Star
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.model.GameCategory
import com.example.model.GameItem
import com.example.model.GameOwnership
import com.example.model.PlayMode
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
import java.util.Locale

@Composable
fun StoreScreen(
    catalog: List<GameItem>,
    ownershipMap: Map<String, GameOwnership>,
    walletBalance: Double,
    searchQuery: String,
    onSearchQueryChange: (String) -> Unit,
    selectedCategory: GameCategory,
    onSelectCategory: (GameCategory) -> Unit,
    onGameClick: (GameItem) -> Unit,
    onStartCheckout: (GameItem) -> Unit,
    onLaunchInstantPlay: (GameItem) -> Unit,
    onOpenTopUp: () -> Unit,
    onOpenSecurityCompliance: () -> Unit,
    isOwnerModeActive: Boolean = true,
    onOpenOwnerAdmin: () -> Unit
) {
    val featuredGames = catalog.filter { it.isFeatured }

    LazyColumn(
        modifier = Modifier
            .fillMaxSize()
            .background(MidnightObsidian)
            .statusBarsPadding()
            .testTag("store_screen"),
        contentPadding = PaddingValues(bottom = 100.dp)
    ) {
        // App Header: Branding + Wallet Pill + Security Shield
        item {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 16.dp, vertical = 12.dp)
            ) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    // Logo and app title
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Box(
                            modifier = Modifier
                                .size(38.dp)
                                .background(
                                    Brush.linearGradient(listOf(CyberCyanPrimary, ElectricViolet)),
                                    RoundedCornerShape(10.dp)
                                ),
                            contentAlignment = Alignment.Center
                        ) {
                            Icon(
                                imageVector = Icons.Default.SportsEsports,
                                contentDescription = "Logo",
                                tint = DeepNavySurface,
                                modifier = Modifier.size(22.dp)
                            )
                        }
                        Spacer(modifier = Modifier.width(10.dp))
                        Column {
                            Text(
                                text = "PLAYVAULT",
                                color = TextPrimaryDark,
                                fontSize = 18.sp,
                                fontWeight = FontWeight.Black,
                                letterSpacing = 1.sp
                            )
                            Text(
                                text = "Premium Gaming & Arcade Store",
                                color = CyberCyanPrimary,
                                fontSize = 11.sp,
                                fontWeight = FontWeight.SemiBold
                            )
                        }
                    }

                    // Top Action Icons: Owner VIP + Wallet pill + Security shield
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        // Owner VIP Pill
                        Box(
                            modifier = Modifier
                                .clip(RoundedCornerShape(20.dp))
                                .background(if (isOwnerModeActive) GoldCurrency.copy(alpha = 0.2f) else CardSurfaceDark)
                                .border(1.dp, if (isOwnerModeActive) GoldCurrency else CardBorderStroke, RoundedCornerShape(20.dp))
                                .clickable(onClick = onOpenOwnerAdmin)
                                .padding(horizontal = 8.dp, vertical = 6.dp)
                                .testTag("owner_admin_button")
                        ) {
                            Text(
                                text = if (isOwnerModeActive) "👑 OWNER VIP" else "🔒 ADMIN",
                                fontSize = 10.sp,
                                fontWeight = FontWeight.Bold,
                                color = if (isOwnerModeActive) GoldCurrency else TextSecondaryDark
                            )
                        }

                        Spacer(modifier = Modifier.width(6.dp))

                        // Wallet Balance Pill
                        Box(
                            modifier = Modifier
                                .clip(RoundedCornerShape(20.dp))
                                .background(CardSurfaceDark)
                                .border(1.dp, CardBorderStroke, RoundedCornerShape(20.dp))
                                .clickable(onClick = onOpenTopUp)
                                .padding(horizontal = 10.dp, vertical = 6.dp)
                                .testTag("wallet_balance_pill")
                        ) {
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Icon(
                                    imageVector = Icons.Default.AccountBalanceWallet,
                                    contentDescription = "Wallet",
                                    tint = GoldCurrency,
                                    modifier = Modifier.size(16.dp)
                                )
                                Spacer(modifier = Modifier.width(4.dp))
                                Text(
                                    text = "$${String.format(Locale.US, "%.2f", walletBalance)}",
                                    color = TextPrimaryDark,
                                    fontSize = 13.sp,
                                    fontWeight = FontWeight.ExtraBold
                                )
                                Spacer(modifier = Modifier.width(4.dp))
                                Box(
                                    modifier = Modifier
                                        .size(16.dp)
                                        .background(EmeraldSuccess, CircleShape),
                                    contentAlignment = Alignment.Center
                                ) {
                                    Icon(
                                        imageVector = Icons.Default.Add,
                                        contentDescription = "Add",
                                        tint = DeepNavySurface,
                                        modifier = Modifier.size(12.dp)
                                    )
                                }
                            }
                        }

                        Spacer(modifier = Modifier.width(8.dp))

                        IconButton(
                            onClick = onOpenSecurityCompliance,
                            modifier = Modifier
                                .size(36.dp)
                                .background(CardSurfaceDark, CircleShape)
                                .testTag("security_compliance_button")
                        ) {
                            Icon(
                                imageVector = Icons.Default.Security,
                                contentDescription = "Security",
                                tint = EmeraldSuccess,
                                modifier = Modifier.size(18.dp)
                            )
                        }
                    }
                }

                Spacer(modifier = Modifier.height(14.dp))

                // Search Bar
                OutlinedTextField(
                    value = searchQuery,
                    onValueChange = onSearchQueryChange,
                    placeholder = { Text("Search premium games, racers, shooters...", fontSize = 13.sp) },
                    leadingIcon = {
                        Icon(
                            imageVector = Icons.Default.Search,
                            contentDescription = "Search",
                            tint = TextSecondaryDark
                        )
                    },
                    modifier = Modifier
                        .fillMaxWidth()
                        .testTag("search_game_input"),
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedContainerColor = CardSurfaceDark,
                        unfocusedContainerColor = CardSurfaceDark,
                        focusedBorderColor = CyberCyanPrimary,
                        unfocusedBorderColor = CardBorderStroke
                    ),
                    shape = RoundedCornerShape(14.dp),
                    singleLine = true
                )
            }
        }

        // Category Filter Chips
        item {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .horizontalScroll(rememberScrollState())
                    .padding(horizontal = 16.dp, vertical = 6.dp),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                GameCategory.values().forEach { category ->
                    val isSelected = category == selectedCategory
                    val categoryLabel = when (category) {
                        GameCategory.ALL -> "All Titles"
                        GameCategory.FREE_GAMES -> "🎁 Free to Play"
                        GameCategory.OFFLINE_GAMES -> "📶 100% Offline"
                        GameCategory.ONLINE_GAMES -> "⚡ Online Multiplayer"
                        GameCategory.ACTION -> "Action"
                        GameCategory.ARCADE -> "Arcade"
                        GameCategory.SCI_FI -> "Sci-Fi"
                        GameCategory.RACING -> "Racing"
                        GameCategory.RPG -> "RPG"
                        GameCategory.PUZZLE -> "Puzzle"
                    }
                    Box(
                        modifier = Modifier
                            .clip(RoundedCornerShape(12.dp))
                            .background(if (isSelected) CyberCyanPrimary else CardSurfaceDark)
                            .border(
                                1.dp,
                                if (isSelected) CyberCyanPrimary else CardBorderStroke,
                                RoundedCornerShape(12.dp)
                            )
                            .clickable { onSelectCategory(category) }
                            .padding(horizontal = 14.dp, vertical = 8.dp)
                    ) {
                        Text(
                            text = categoryLabel,
                            color = if (isSelected) DeepNavySurface else TextPrimaryDark,
                            fontSize = 12.sp,
                            fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Medium
                        )
                    }
                }
            }
        }

        // Featured Games Horizontal Showcase (if no search filter active)
        if (searchQuery.isBlank() && selectedCategory == GameCategory.ALL && featuredGames.isNotEmpty()) {
            item {
                Column(modifier = Modifier.padding(top = 16.dp)) {
                    Text(
                        text = "FEATURED PREMIUM TITLES",
                        color = TextSecondaryDark,
                        fontSize = 12.sp,
                        fontWeight = FontWeight.Bold,
                        letterSpacing = 1.sp,
                        modifier = Modifier.padding(horizontal = 16.dp)
                    )

                    Spacer(modifier = Modifier.height(8.dp))

                    LazyRow(
                        contentPadding = PaddingValues(horizontal = 16.dp),
                        horizontalArrangement = Arrangement.spacedBy(14.dp)
                    ) {
                        items(featuredGames) { game ->
                            FeaturedGameCard(
                                game = game,
                                isOwned = isOwnerModeActive || ownershipMap[game.id]?.isOwned == true,
                                onClick = { onGameClick(game) },
                                onBuyClick = { onStartCheckout(game) },
                                onPlayClick = { onLaunchInstantPlay(game) }
                            )
                        }
                    }
                }
            }
        }

        // All Games Catalog Header
        item {
            Spacer(modifier = Modifier.height(16.dp))
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 16.dp),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = "GAMES CATALOG (${catalog.size})",
                    color = TextSecondaryDark,
                    fontSize = 12.sp,
                    fontWeight = FontWeight.Bold,
                    letterSpacing = 1.sp
                )
                Text(
                    text = if (isOwnerModeActive) "👑 All Games Unlocked" else "PCI-DSS Verified",
                    color = if (isOwnerModeActive) GoldCurrency else EmeraldSuccess,
                    fontSize = 11.sp,
                    fontWeight = FontWeight.SemiBold
                )
            }
            Spacer(modifier = Modifier.height(8.dp))
        }

        // Catalog List items
        items(catalog) { game ->
            val isOwned = isOwnerModeActive || ownershipMap[game.id]?.isOwned == true
            GameCatalogCard(
                game = game,
                isOwned = isOwned,
                onClick = { onGameClick(game) },
                onBuyClick = { onStartCheckout(game) },
                onPlayClick = { onLaunchInstantPlay(game) }
            )
        }
    }
}

@Composable
private fun FeaturedGameCard(
    game: GameItem,
    isOwned: Boolean,
    onClick: () -> Unit,
    onBuyClick: () -> Unit,
    onPlayClick: () -> Unit
) {
    val gradientBrush = Brush.linearGradient(
        colors = game.gradientColors.map { Color(it) }
    )

    Card(
        modifier = Modifier
            .width(280.dp)
            .clip(RoundedCornerShape(18.dp))
            .clickable(onClick = onClick)
            .testTag("featured_game_${game.id}"),
        shape = RoundedCornerShape(18.dp),
        colors = CardDefaults.cardColors(containerColor = CardSurfaceDark),
        border = androidx.compose.foundation.BorderStroke(1.dp, CardBorderStroke)
    ) {
        Column {
            // Visual Art Banner
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(130.dp)
                    .background(gradientBrush)
                    .padding(12.dp)
            ) {
                if (game.badge != null) {
                    Box(
                        modifier = Modifier
                            .align(Alignment.TopStart)
                            .background(GoldCurrency, RoundedCornerShape(12.dp))
                            .padding(horizontal = 8.dp, vertical = 3.dp)
                    ) {
                        Text(
                            text = game.badge,
                            color = DeepNavySurface,
                            fontSize = 10.sp,
                            fontWeight = FontWeight.ExtraBold
                        )
                    }
                }

                if (game.hasInstantPlay) {
                    Box(
                        modifier = Modifier
                            .align(Alignment.TopEnd)
                            .background(Color.Black.copy(alpha = 0.6f), RoundedCornerShape(12.dp))
                            .padding(horizontal = 8.dp, vertical = 3.dp)
                    ) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Icon(
                                imageVector = Icons.Default.FlashOn,
                                contentDescription = "Instant",
                                tint = CyberCyanPrimary,
                                modifier = Modifier.size(12.dp)
                            )
                            Spacer(modifier = Modifier.width(3.dp))
                            Text(
                                text = "Instant Play",
                                color = CyberCyanPrimary,
                                fontSize = 10.sp,
                                fontWeight = FontWeight.Bold
                            )
                        }
                    }
                }

                Text(
                    text = game.title,
                    color = Color.White,
                    fontSize = 16.sp,
                    fontWeight = FontWeight.ExtraBold,
                    modifier = Modifier.align(Alignment.BottomStart)
                )
            }

            // Info Body
            Column(modifier = Modifier.padding(14.dp)) {
                Text(
                    text = game.tagline,
                    color = TextSecondaryDark,
                    fontSize = 12.sp,
                    maxLines = 2,
                    overflow = TextOverflow.Ellipsis
                )

                Spacer(modifier = Modifier.height(10.dp))

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Column {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Icon(
                                imageVector = Icons.Default.Star,
                                contentDescription = "Rating",
                                tint = GoldCurrency,
                                modifier = Modifier.size(14.dp)
                            )
                            Spacer(modifier = Modifier.width(3.dp))
                            Text(
                                text = "${game.rating}",
                                color = TextPrimaryDark,
                                fontSize = 12.sp,
                                fontWeight = FontWeight.Bold
                            )
                        }
                        Text(
                            text = "${game.downloadSizeMb} MB",
                            color = TextMutedDark,
                            fontSize = 11.sp
                        )
                    }

                    if (isOwned) {
                        Button(
                            onClick = onPlayClick,
                            colors = ButtonDefaults.buttonColors(containerColor = EmeraldSuccess),
                            shape = RoundedCornerShape(10.dp),
                            contentPadding = PaddingValues(horizontal = 12.dp, vertical = 6.dp)
                        ) {
                            Text("Play", color = DeepNavySurface, fontWeight = FontWeight.Bold, fontSize = 12.sp)
                        }
                    } else if (game.isFree) {
                        Button(
                            onClick = onBuyClick,
                            colors = ButtonDefaults.buttonColors(containerColor = EmeraldSuccess),
                            shape = RoundedCornerShape(10.dp),
                            contentPadding = PaddingValues(horizontal = 12.dp, vertical = 6.dp)
                        ) {
                            Text("GET FREE", color = DeepNavySurface, fontWeight = FontWeight.ExtraBold, fontSize = 12.sp)
                        }
                    } else {
                        Button(
                            onClick = onBuyClick,
                            colors = ButtonDefaults.buttonColors(containerColor = CyberCyanPrimary),
                            shape = RoundedCornerShape(10.dp),
                            contentPadding = PaddingValues(horizontal = 12.dp, vertical = 6.dp)
                        ) {
                            Text(
                                text = "$${String.format(Locale.US, "%.2f", game.priceUsd)}",
                                color = DeepNavySurface,
                                fontWeight = FontWeight.ExtraBold,
                                fontSize = 13.sp
                            )
                        }
                    }
                }
            }
        }
    }
}

@Composable
private fun GameCatalogCard(
    game: GameItem,
    isOwned: Boolean,
    onClick: () -> Unit,
    onBuyClick: () -> Unit,
    onPlayClick: () -> Unit
) {
    val gradientBrush = Brush.linearGradient(
        colors = game.gradientColors.map { Color(it) }
    )

    Card(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp, vertical = 6.dp)
            .clip(RoundedCornerShape(14.dp))
            .clickable(onClick = onClick)
            .testTag("catalog_game_${game.id}"),
        shape = RoundedCornerShape(14.dp),
        colors = CardDefaults.cardColors(containerColor = CardSurfaceDark),
        border = androidx.compose.foundation.BorderStroke(1.dp, CardBorderStroke)
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(12.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            // Game Mini Artwork
            Box(
                modifier = Modifier
                    .size(70.dp)
                    .clip(RoundedCornerShape(12.dp))
                    .background(gradientBrush),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    imageVector = Icons.Default.SportsEsports,
                    contentDescription = game.title,
                    tint = Color.White.copy(alpha = 0.85f),
                    modifier = Modifier.size(32.dp)
                )
            }

            Spacer(modifier = Modifier.width(12.dp))

            // Game metadata
            Column(modifier = Modifier.weight(1f)) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Text(
                        text = game.title,
                        color = TextPrimaryDark,
                        fontSize = 15.sp,
                        fontWeight = FontWeight.Bold,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis
                    )
                }

                Spacer(modifier = Modifier.height(2.dp))

                Row(verticalAlignment = Alignment.CenterVertically) {
                    val modeColor = when (game.playMode) {
                        PlayMode.OFFLINE -> EmeraldSuccess
                        PlayMode.ONLINE -> CyberCyanPrimary
                        PlayMode.BOTH -> Color(0xFFC084FC)
                    }
                    val modeLabel = when (game.playMode) {
                        PlayMode.OFFLINE -> "📶 Offline"
                        PlayMode.ONLINE -> "⚡ Online"
                        PlayMode.BOTH -> "🌐 Hybrid"
                    }
                    Box(
                        modifier = Modifier
                            .clip(RoundedCornerShape(4.dp))
                            .background(modeColor.copy(alpha = 0.15f))
                            .border(0.5.dp, modeColor.copy(alpha = 0.5f), RoundedCornerShape(4.dp))
                            .padding(horizontal = 5.dp, vertical = 1.dp)
                    ) {
                        Text(
                            text = modeLabel,
                            color = modeColor,
                            fontSize = 9.sp,
                            fontWeight = FontWeight.Bold
                        )
                    }

                    if (game.isFree) {
                        Spacer(modifier = Modifier.width(6.dp))
                        Box(
                            modifier = Modifier
                                .clip(RoundedCornerShape(4.dp))
                                .background(EmeraldSuccess.copy(alpha = 0.25f))
                                .border(0.5.dp, EmeraldSuccess, RoundedCornerShape(4.dp))
                                .padding(horizontal = 5.dp, vertical = 1.dp)
                        ) {
                            Text(
                                text = "FREE",
                                color = EmeraldSuccess,
                                fontSize = 9.sp,
                                fontWeight = FontWeight.Black
                            )
                        }
                    }
                }

                Spacer(modifier = Modifier.height(3.dp))

                Text(
                    text = game.tagline,
                    color = TextSecondaryDark,
                    fontSize = 12.sp,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis
                )

                Spacer(modifier = Modifier.height(4.dp))

                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(
                        imageVector = Icons.Default.Star,
                        contentDescription = "Rating",
                        tint = GoldCurrency,
                        modifier = Modifier.size(13.dp)
                    )
                    Spacer(modifier = Modifier.width(3.dp))
                    Text(
                        text = "${game.rating}",
                        color = TextPrimaryDark,
                        fontSize = 11.sp,
                        fontWeight = FontWeight.Bold
                    )
                    Spacer(modifier = Modifier.width(8.dp))
                    Text(
                        text = "•  ${game.downloadSizeMb} MB",
                        color = TextMutedDark,
                        fontSize = 11.sp
                    )
                    if (game.hasInstantPlay) {
                        Spacer(modifier = Modifier.width(6.dp))
                        Text(
                            text = "• Instant Play",
                            color = CyberCyanPrimary,
                            fontSize = 11.sp,
                            fontWeight = FontWeight.SemiBold
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.width(8.dp))

            // Purchase or Play Button
            if (isOwned) {
                Column(horizontalAlignment = Alignment.End) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Icon(
                            imageVector = Icons.Default.CheckCircle,
                            contentDescription = "Owned",
                            tint = EmeraldSuccess,
                            modifier = Modifier.size(14.dp)
                        )
                        Spacer(modifier = Modifier.width(3.dp))
                        Text(
                            text = "Owned",
                            color = EmeraldSuccess,
                            fontSize = 11.sp,
                            fontWeight = FontWeight.Bold
                        )
                    }
                    Spacer(modifier = Modifier.height(4.dp))
                    Button(
                        onClick = onPlayClick,
                        colors = ButtonDefaults.buttonColors(containerColor = EmeraldSuccess),
                        shape = RoundedCornerShape(8.dp),
                        contentPadding = PaddingValues(horizontal = 12.dp, vertical = 4.dp)
                    ) {
                        Text("Play", color = DeepNavySurface, fontSize = 12.sp, fontWeight = FontWeight.Bold)
                    }
                }
            } else if (game.isFree) {
                Button(
                    onClick = onBuyClick,
                    colors = ButtonDefaults.buttonColors(containerColor = EmeraldSuccess),
                    shape = RoundedCornerShape(10.dp),
                    contentPadding = PaddingValues(horizontal = 14.dp, vertical = 6.dp),
                    modifier = Modifier.testTag("free_button_${game.id}")
                ) {
                    Text(
                        text = "FREE",
                        color = DeepNavySurface,
                        fontSize = 13.sp,
                        fontWeight = FontWeight.ExtraBold
                    )
                }
            } else {
                Button(
                    onClick = onBuyClick,
                    colors = ButtonDefaults.buttonColors(containerColor = CyberCyanPrimary),
                    shape = RoundedCornerShape(10.dp),
                    contentPadding = PaddingValues(horizontal = 14.dp, vertical = 6.dp),
                    modifier = Modifier.testTag("buy_button_${game.id}")
                ) {
                    Text(
                        text = "$${String.format(Locale.US, "%.2f", game.priceUsd)}",
                        color = DeepNavySurface,
                        fontSize = 13.sp,
                        fontWeight = FontWeight.ExtraBold
                    )
                }
            }
        }
    }
}

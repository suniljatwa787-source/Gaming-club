package com.example.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.border
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
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.CardGiftcard
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Download
import androidx.compose.material.icons.filled.FlashOn
import androidx.compose.material.icons.filled.Lock
import androidx.compose.material.icons.filled.PlayArrow
import androidx.compose.material.icons.filled.Security
import androidx.compose.material.icons.filled.Star
import androidx.compose.material.icons.filled.VerifiedUser
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Text
import androidx.compose.material3.rememberModalBottomSheetState
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.model.DownloadState
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
import com.example.ui.theme.NeonPinkAccent
import com.example.ui.theme.TextMutedDark
import com.example.ui.theme.TextPrimaryDark
import com.example.ui.theme.TextSecondaryDark
import java.util.Locale

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun GameDetailSheet(
    game: GameItem,
    ownership: GameOwnership?,
    onDismiss: () -> Unit,
    onStartCheckout: (GameItem) -> Unit,
    onLaunchInstantPlay: (GameItem) -> Unit,
    onStartDownload: (String) -> Unit,
    onPauseDownload: (String) -> Unit,
    onRequestRefund: (String) -> Unit
) {
    val sheetState = rememberModalBottomSheetState(skipPartiallyExpanded = true)
    val isOwned = ownership?.isOwned == true
    val downloadState = ownership?.downloadState ?: DownloadState.NOT_DOWNLOADED

    val gradientBrush = Brush.linearGradient(
        colors = game.gradientColors.map { Color(it) }
    )

    ModalBottomSheet(
        onDismissRequest = onDismiss,
        sheetState = sheetState,
        containerColor = DeepNavySurface,
        dragHandle = null,
        modifier = Modifier
            .fillMaxWidth()
            .testTag("game_detail_sheet")
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .verticalScroll(rememberScrollState())
                .navigationBarsPadding()
        ) {
            // Hero Banner with close button and badge
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(200.dp)
                    .background(gradientBrush)
                    .padding(16.dp)
            ) {
                // Category & Badge tag
                Row(
                    modifier = Modifier.align(Alignment.TopStart),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Box(
                        modifier = Modifier
                            .background(Color.Black.copy(alpha = 0.5f), RoundedCornerShape(20.dp))
                            .padding(horizontal = 10.dp, vertical = 4.dp)
                    ) {
                        Text(
                            text = game.category.name,
                            color = CyberCyanPrimary,
                            fontSize = 11.sp,
                            fontWeight = FontWeight.Bold
                        )
                    }
                    if (game.badge != null) {
                        Spacer(modifier = Modifier.width(6.dp))
                        Box(
                            modifier = Modifier
                                .background(GoldCurrency.copy(alpha = 0.9f), RoundedCornerShape(20.dp))
                                .padding(horizontal = 10.dp, vertical = 4.dp)
                        ) {
                            Text(
                                text = game.badge,
                                color = DeepNavySurface,
                                fontSize = 11.sp,
                                fontWeight = FontWeight.Bold
                            )
                        }
                    }
                }

                // Close button
                IconButton(
                    onClick = onDismiss,
                    modifier = Modifier
                        .align(Alignment.TopEnd)
                        .background(Color.Black.copy(alpha = 0.4f), CircleShape)
                        .size(36.dp)
                        .testTag("close_detail_button")
                ) {
                    Icon(
                        imageVector = Icons.Default.Close,
                        contentDescription = "Close",
                        tint = Color.White,
                        modifier = Modifier.size(20.dp)
                    )
                }

                // Title and rating on bottom of hero
                Column(modifier = Modifier.align(Alignment.BottomStart)) {
                    Text(
                        text = game.title,
                        color = Color.White,
                        fontSize = 22.sp,
                        fontWeight = FontWeight.ExtraBold
                    )
                    Spacer(modifier = Modifier.height(2.dp))
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Icon(
                            imageVector = Icons.Default.Star,
                            contentDescription = "Rating",
                            tint = GoldCurrency,
                            modifier = Modifier.size(16.dp)
                        )
                        Spacer(modifier = Modifier.width(4.dp))
                        Text(
                            text = "${game.rating} (${game.reviewsCount} reviews)",
                            color = Color.White.copy(alpha = 0.9f),
                            fontSize = 12.sp,
                            fontWeight = FontWeight.SemiBold
                        )
                        Spacer(modifier = Modifier.width(12.dp))
                        Text(
                            text = "${game.downloadSizeMb} MB",
                            color = Color.White.copy(alpha = 0.8f),
                            fontSize = 12.sp
                        )
                    }
                    Spacer(modifier = Modifier.height(4.dp))
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        val modeColor = when (game.playMode) {
                            PlayMode.OFFLINE -> EmeraldSuccess
                            PlayMode.ONLINE -> CyberCyanPrimary
                            PlayMode.BOTH -> Color(0xFFC084FC)
                        }
                        Box(
                            modifier = Modifier
                                .clip(RoundedCornerShape(6.dp))
                                .background(modeColor.copy(alpha = 0.25f))
                                .border(1.dp, modeColor, RoundedCornerShape(6.dp))
                                .padding(horizontal = 8.dp, vertical = 2.dp)
                        ) {
                            Text(
                                text = game.playMode.label,
                                color = modeColor,
                                fontSize = 10.sp,
                                fontWeight = FontWeight.Bold
                            )
                        }
                        if (game.isFree) {
                            Spacer(modifier = Modifier.width(8.dp))
                            Box(
                                modifier = Modifier
                                    .clip(RoundedCornerShape(6.dp))
                                    .background(EmeraldSuccess.copy(alpha = 0.3f))
                                    .border(1.dp, EmeraldSuccess, RoundedCornerShape(6.dp))
                                    .padding(horizontal = 8.dp, vertical = 2.dp)
                            ) {
                                Text(
                                    text = "100% FREE",
                                    color = EmeraldSuccess,
                                    fontSize = 10.sp,
                                    fontWeight = FontWeight.Black
                                )
                            }
                        }
                    }
                }
            }

            // Main Content Body
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(20.dp)
            ) {
                // Primary Action Bar (Buy or Download / Play)
                if (!isOwned) {
                    // Not Owned: Buy Button + Instant Play Preview
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(10.dp)
                    ) {
                        if (game.isFree) {
                            Button(
                                onClick = { onStartCheckout(game) },
                                modifier = Modifier
                                    .weight(1.2f)
                                    .height(52.dp)
                                    .testTag("claim_free_game_button"),
                                colors = ButtonDefaults.buttonColors(containerColor = EmeraldSuccess),
                                shape = RoundedCornerShape(12.dp)
                            ) {
                                Icon(
                                    imageVector = Icons.Default.CardGiftcard,
                                    contentDescription = "Get Free",
                                    tint = DeepNavySurface,
                                    modifier = Modifier.size(18.dp)
                                )
                                Spacer(modifier = Modifier.width(6.dp))
                                Text(
                                    text = "GET FOR FREE",
                                    color = DeepNavySurface,
                                    fontSize = 15.sp,
                                    fontWeight = FontWeight.ExtraBold
                                )
                            }
                        } else {
                            Button(
                                onClick = { onStartCheckout(game) },
                                modifier = Modifier
                                    .weight(1.2f)
                                    .height(52.dp)
                                    .testTag("buy_game_now_button"),
                                colors = ButtonDefaults.buttonColors(containerColor = CyberCyanPrimary),
                                shape = RoundedCornerShape(12.dp)
                            ) {
                                Icon(
                                    imageVector = Icons.Default.Lock,
                                    contentDescription = "Buy",
                                    tint = DeepNavySurface,
                                    modifier = Modifier.size(18.dp)
                                )
                                Spacer(modifier = Modifier.width(6.dp))
                                Text(
                                    text = "Buy for $${String.format(Locale.US, "%.2f", game.priceUsd)}",
                                    color = DeepNavySurface,
                                    fontSize = 15.sp,
                                    fontWeight = FontWeight.ExtraBold
                                )
                            }
                        }

                        if (game.hasInstantPlay) {
                            OutlinedButton(
                                onClick = { onLaunchInstantPlay(game) },
                                modifier = Modifier
                                    .weight(1f)
                                    .height(52.dp)
                                    .testTag("play_instant_trial_button"),
                                colors = ButtonDefaults.outlinedButtonColors(contentColor = CyberCyanPrimary),
                                border = androidx.compose.foundation.BorderStroke(1.5.dp, CyberCyanPrimary),
                                shape = RoundedCornerShape(12.dp)
                            ) {
                                Icon(
                                    imageVector = Icons.Default.FlashOn,
                                    contentDescription = "Instant",
                                    tint = CyberCyanPrimary,
                                    modifier = Modifier.size(18.dp)
                                )
                                Spacer(modifier = Modifier.width(4.dp))
                                Text(
                                    text = "Instant Play",
                                    fontSize = 13.sp,
                                    fontWeight = FontWeight.Bold
                                )
                            }
                        }
                    }
                } else {
                    // Owned: Show Download / Installed State
                    Card(
                        modifier = Modifier.fillMaxWidth(),
                        shape = RoundedCornerShape(14.dp),
                        colors = CardDefaults.cardColors(containerColor = CardSurfaceDark),
                        border = androidx.compose.foundation.BorderStroke(1.dp, CardBorderStroke)
                    ) {
                        Column(modifier = Modifier.padding(14.dp)) {
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Row(verticalAlignment = Alignment.CenterVertically) {
                                    Icon(
                                        imageVector = Icons.Default.CheckCircle,
                                        contentDescription = "Owned",
                                        tint = EmeraldSuccess,
                                        modifier = Modifier.size(18.dp)
                                    )
                                    Spacer(modifier = Modifier.width(6.dp))
                                    Text(
                                        text = "License Owned in Library",
                                        color = EmeraldSuccess,
                                        fontSize = 13.sp,
                                        fontWeight = FontWeight.Bold
                                    )
                                }
                                Text(
                                    text = "Played ${ownership?.playTimeMinutes ?: 0}m",
                                    color = TextSecondaryDark,
                                    fontSize = 12.sp
                                )
                            }

                            Spacer(modifier = Modifier.height(10.dp))

                            when (downloadState) {
                                DownloadState.INSTALLED -> {
                                    Row(
                                        modifier = Modifier.fillMaxWidth(),
                                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                                    ) {
                                        Button(
                                            onClick = { onLaunchInstantPlay(game) },
                                            modifier = Modifier
                                                .weight(1f)
                                                .height(48.dp)
                                                .testTag("launch_game_button"),
                                            colors = ButtonDefaults.buttonColors(containerColor = EmeraldSuccess)
                                        ) {
                                            Icon(Icons.Default.PlayArrow, contentDescription = "Play", tint = DeepNavySurface)
                                            Spacer(modifier = Modifier.width(6.dp))
                                            Text("Launch Game", color = DeepNavySurface, fontWeight = FontWeight.Bold)
                                        }
                                    }
                                }
                                DownloadState.DOWNLOADING -> {
                                    Column {
                                        Row(
                                            modifier = Modifier.fillMaxWidth(),
                                            horizontalArrangement = Arrangement.SpaceBetween
                                        ) {
                                            Text(
                                                text = "Downloading package (${ownership.downloadProgressPercent}%)...",
                                                color = CyberCyanPrimary,
                                                fontSize = 12.sp
                                            )
                                            Text(
                                                text = "${ownership.downloadSpeedMbPerSec} MB/s",
                                                color = TextSecondaryDark,
                                                fontSize = 12.sp
                                            )
                                        }
                                        Spacer(modifier = Modifier.height(4.dp))
                                        LinearProgressIndicator(
                                            progress = { ownership.downloadProgressPercent / 100f },
                                            modifier = Modifier
                                                .fillMaxWidth()
                                                .height(6.dp),
                                            color = CyberCyanPrimary,
                                            trackColor = CardBorderStroke
                                        )
                                        Spacer(modifier = Modifier.height(8.dp))
                                        OutlinedButton(
                                            onClick = { onPauseDownload(game.id) },
                                            modifier = Modifier.fillMaxWidth()
                                        ) {
                                            Text("Pause Download", fontSize = 12.sp)
                                        }
                                    }
                                }
                                DownloadState.PAUSED -> {
                                    Row(
                                        modifier = Modifier.fillMaxWidth(),
                                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                                    ) {
                                        Button(
                                            onClick = { onStartDownload(game.id) },
                                            modifier = Modifier.weight(1f),
                                            colors = ButtonDefaults.buttonColors(containerColor = CyberCyanPrimary)
                                        ) {
                                            Text("Resume Download", color = DeepNavySurface)
                                        }
                                    }
                                }
                                else -> {
                                    // Not yet downloaded
                                    Row(
                                        modifier = Modifier.fillMaxWidth(),
                                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                                    ) {
                                        Button(
                                            onClick = { onStartDownload(game.id) },
                                            modifier = Modifier
                                                .weight(1f)
                                                .height(48.dp)
                                                .testTag("download_package_button"),
                                            colors = ButtonDefaults.buttonColors(containerColor = CyberCyanPrimary)
                                        ) {
                                            Icon(Icons.Default.Download, contentDescription = "Download", tint = DeepNavySurface)
                                            Spacer(modifier = Modifier.width(6.dp))
                                            Text("Download (${game.downloadSizeMb} MB)", color = DeepNavySurface, fontWeight = FontWeight.Bold)
                                        }
                                        if (game.hasInstantPlay) {
                                            OutlinedButton(
                                                onClick = { onLaunchInstantPlay(game) },
                                                modifier = Modifier
                                                    .weight(0.9f)
                                                    .height(48.dp)
                                            ) {
                                                Text("Play Instant", fontSize = 12.sp)
                                            }
                                        }
                                    }
                                }
                            }
                        }
                    }
                }

                Spacer(modifier = Modifier.height(16.dp))

                // Security Guarantee strip
                Card(
                    modifier = Modifier.fillMaxWidth(),
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
                        Icon(
                            imageVector = Icons.Default.VerifiedUser,
                            contentDescription = "Encrypted",
                            tint = EmeraldSuccess,
                            modifier = Modifier.size(20.dp)
                        )
                        Spacer(modifier = Modifier.width(8.dp))
                        Text(
                            text = "PCI-DSS Level 1 Encrypted • Zero Fraud Risk • Instant Digital License Delivery",
                            color = TextSecondaryDark,
                            fontSize = 11.sp
                        )
                    }
                }

                Spacer(modifier = Modifier.height(16.dp))

                // Game Overview
                Text(
                    text = "ABOUT THIS GAME",
                    color = TextSecondaryDark,
                    fontSize = 12.sp,
                    fontWeight = FontWeight.Bold,
                    letterSpacing = 1.sp
                )
                Spacer(modifier = Modifier.height(6.dp))
                Text(
                    text = game.description,
                    color = TextPrimaryDark,
                    fontSize = 14.sp,
                    lineHeight = 20.sp
                )

                Spacer(modifier = Modifier.height(16.dp))

                // Key Features List
                if (game.features.isNotEmpty()) {
                    Text(
                        text = "FEATURES",
                        color = TextSecondaryDark,
                        fontSize = 12.sp,
                        fontWeight = FontWeight.Bold,
                        letterSpacing = 1.sp
                    )
                    Spacer(modifier = Modifier.height(6.dp))
                    game.features.forEach { feature ->
                        Row(
                            modifier = Modifier.padding(vertical = 3.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Box(
                                modifier = Modifier
                                    .size(6.dp)
                                    .background(CyberCyanPrimary, CircleShape)
                            )
                            Spacer(modifier = Modifier.width(8.dp))
                            Text(
                                text = feature,
                                color = TextPrimaryDark,
                                fontSize = 13.sp
                            )
                        }
                    }
                }

                Spacer(modifier = Modifier.height(16.dp))

                // Developer & Release Info
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Column {
                        Text(text = "DEVELOPER", color = TextSecondaryDark, fontSize = 11.sp, fontWeight = FontWeight.Bold)
                        Text(text = game.developer, color = TextPrimaryDark, fontSize = 13.sp, fontWeight = FontWeight.Medium)
                    }
                    Column(horizontalAlignment = Alignment.End) {
                        Text(text = "RELEASE", color = TextSecondaryDark, fontSize = 11.sp, fontWeight = FontWeight.Bold)
                        Text(text = game.releaseDate, color = TextPrimaryDark, fontSize = 13.sp, fontWeight = FontWeight.Medium)
                    }
                }

                // Refund Option (if owned & playTime < 120m)
                if (isOwned) {
                    Spacer(modifier = Modifier.height(20.dp))
                    val eligible = (ownership?.playTimeMinutes ?: 0) < 120
                    Card(
                        modifier = Modifier.fillMaxWidth(),
                        shape = RoundedCornerShape(12.dp),
                        colors = CardDefaults.cardColors(containerColor = CardSurfaceDark),
                        border = androidx.compose.foundation.BorderStroke(1.dp, CardBorderStroke)
                    ) {
                        Column(modifier = Modifier.padding(14.dp)) {
                            Text(
                                text = "MONEY-BACK GUARANTEE",
                                color = GoldCurrency,
                                fontSize = 11.sp,
                                fontWeight = FontWeight.Bold
                            )
                            Spacer(modifier = Modifier.height(4.dp))
                            Text(
                                text = if (eligible) {
                                    "Eligible for full instant refund (played < 2 hours). Funds return to your wallet immediately."
                                } else {
                                    "Refund policy limit reached (> 2 hours played)."
                                },
                                color = TextSecondaryDark,
                                fontSize = 12.sp
                            )
                            if (eligible) {
                                Spacer(modifier = Modifier.height(8.dp))
                                OutlinedButton(
                                    onClick = { onRequestRefund(game.id) },
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .testTag("request_refund_button"),
                                    colors = ButtonDefaults.outlinedButtonColors(contentColor = Color(0xFFFF8080)),
                                    border = androidx.compose.foundation.BorderStroke(1.dp, Color(0xFF883333))
                                ) {
                                    Text("Request Instant Refund ($${String.format(Locale.US, "%.2f", game.priceUsd)})")
                                }
                            }
                        }
                    }
                }
            }
        }
    }
}

package com.example.ui.screens

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
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.DeleteOutline
import androidx.compose.material.icons.filled.Download
import androidx.compose.material.icons.filled.FlashOn
import androidx.compose.material.icons.filled.Pause
import androidx.compose.material.icons.filled.PlayArrow
import androidx.compose.material.icons.filled.SportsEsports
import androidx.compose.material.icons.filled.Warning
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.OutlinedButton
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
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.model.DownloadState
import com.example.model.GameItem
import com.example.model.GameOwnership
import com.example.ui.theme.CardBorderStroke
import com.example.ui.theme.CardSurfaceDark
import com.example.ui.theme.CyberCyanPrimary
import com.example.ui.theme.DeepNavySurface
import com.example.ui.theme.EmeraldSuccess
import com.example.ui.theme.GoldCurrency
import com.example.ui.theme.MidnightObsidian
import com.example.ui.theme.TextMutedDark
import com.example.ui.theme.TextPrimaryDark
import com.example.ui.theme.TextSecondaryDark

enum class LibraryFilter {
    ALL,
    DOWNLOADED,
    INSTANT_PLAY
}

@Composable
fun LibraryScreen(
    ownedGames: List<Pair<GameItem, GameOwnership>>,
    onGameClick: (GameItem) -> Unit,
    onLaunchPlay: (GameItem) -> Unit,
    onStartDownload: (String) -> Unit,
    onPauseDownload: (String) -> Unit,
    onCancelDownload: (String) -> Unit,
    onUninstallGame: (String) -> Unit,
    onBrowseStore: () -> Unit
) {
    var selectedFilter by remember { mutableStateOf(LibraryFilter.ALL) }

    val filteredList = when (selectedFilter) {
        LibraryFilter.ALL -> ownedGames
        LibraryFilter.DOWNLOADED -> ownedGames.filter { it.second.downloadState == DownloadState.INSTALLED }
        LibraryFilter.INSTANT_PLAY -> ownedGames.filter { it.first.hasInstantPlay }
    }

    LazyColumn(
        modifier = Modifier
            .fillMaxSize()
            .background(MidnightObsidian)
            .statusBarsPadding()
            .testTag("library_screen"),
        contentPadding = PaddingValues(bottom = 100.dp)
    ) {
        item {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 16.dp, vertical = 12.dp)
            ) {
                Text(
                    text = "MY GAME VAULT",
                    color = TextPrimaryDark,
                    fontSize = 20.sp,
                    fontWeight = FontWeight.ExtraBold,
                    letterSpacing = 1.sp
                )
                Text(
                    text = "${ownedGames.size} Digital Licenses Owned • Offline Ready",
                    color = CyberCyanPrimary,
                    fontSize = 12.sp
                )

                Spacer(modifier = Modifier.height(14.dp))

                // Filter tabs
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    LibraryFilterPill(
                        title = "All Owned (${ownedGames.size})",
                        isSelected = selectedFilter == LibraryFilter.ALL,
                        onClick = { selectedFilter = LibraryFilter.ALL }
                    )
                    LibraryFilterPill(
                        title = "Installed (${ownedGames.count { it.second.downloadState == DownloadState.INSTALLED }})",
                        isSelected = selectedFilter == LibraryFilter.DOWNLOADED,
                        onClick = { selectedFilter = LibraryFilter.DOWNLOADED }
                    )
                    LibraryFilterPill(
                        title = "Instant Play",
                        isSelected = selectedFilter == LibraryFilter.INSTANT_PLAY,
                        onClick = { selectedFilter = LibraryFilter.INSTANT_PLAY }
                    )
                }
            }
        }

        if (filteredList.isEmpty()) {
            item {
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(top = 60.dp, start = 20.dp, end = 20.dp),
                    contentAlignment = Alignment.Center
                ) {
                    Column(horizontalAlignment = Alignment.CenterHorizontally) {
                        Icon(
                            imageVector = Icons.Default.SportsEsports,
                            contentDescription = "Empty",
                            tint = TextMutedDark,
                            modifier = Modifier.size(56.dp)
                        )
                        Spacer(modifier = Modifier.height(12.dp))
                        Text(
                            text = "No Games in this section yet",
                            color = TextPrimaryDark,
                            fontSize = 16.sp,
                            fontWeight = FontWeight.Bold
                        )
                        Spacer(modifier = Modifier.height(6.dp))
                        Text(
                            text = "Browse the catalog to unlock and download top games.",
                            color = TextSecondaryDark,
                            fontSize = 13.sp
                        )
                        Spacer(modifier = Modifier.height(16.dp))
                        Button(
                            onClick = onBrowseStore,
                            colors = ButtonDefaults.buttonColors(containerColor = CyberCyanPrimary)
                        ) {
                            Text("Browse Game Store", color = DeepNavySurface, fontWeight = FontWeight.Bold)
                        }
                    }
                }
            }
        } else {
            items(filteredList) { (game, ownership) ->
                OwnedGameCard(
                    game = game,
                    ownership = ownership,
                    onClick = { onGameClick(game) },
                    onLaunchPlay = { onLaunchPlay(game) },
                    onStartDownload = { onStartDownload(game.id) },
                    onPauseDownload = { onPauseDownload(game.id) },
                    onCancelDownload = { onCancelDownload(game.id) },
                    onUninstall = { onUninstallGame(game.id) }
                )
            }
        }
    }
}

@Composable
private fun LibraryFilterPill(
    title: String,
    isSelected: Boolean,
    onClick: () -> Unit
) {
    Box(
        modifier = Modifier
            .clip(RoundedCornerShape(12.dp))
            .background(if (isSelected) CyberCyanPrimary else CardSurfaceDark)
            .border(
                1.dp,
                if (isSelected) CyberCyanPrimary else CardBorderStroke,
                RoundedCornerShape(12.dp)
            )
            .clickable(onClick = onClick)
            .padding(horizontal = 12.dp, vertical = 8.dp)
    ) {
        Text(
            text = title,
            color = if (isSelected) DeepNavySurface else TextPrimaryDark,
            fontSize = 12.sp,
            fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Medium
        )
    }
}

@Composable
private fun OwnedGameCard(
    game: GameItem,
    ownership: GameOwnership,
    onClick: () -> Unit,
    onLaunchPlay: () -> Unit,
    onStartDownload: () -> Unit,
    onPauseDownload: () -> Unit,
    onCancelDownload: () -> Unit,
    onUninstall: () -> Unit
) {
    val gradientBrush = Brush.linearGradient(
        colors = game.gradientColors.map { Color(it) }
    )

    Card(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp, vertical = 6.dp)
            .clip(RoundedCornerShape(16.dp))
            .clickable(onClick = onClick)
            .testTag("owned_game_${game.id}"),
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(containerColor = CardSurfaceDark),
        border = androidx.compose.foundation.BorderStroke(1.dp, CardBorderStroke)
    ) {
        Column(modifier = Modifier.padding(14.dp)) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically
            ) {
                // Game banner
                Box(
                    modifier = Modifier
                        .size(60.dp)
                        .clip(RoundedCornerShape(12.dp))
                        .background(gradientBrush),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        imageVector = Icons.Default.SportsEsports,
                        contentDescription = game.title,
                        tint = Color.White,
                        modifier = Modifier.size(28.dp)
                    )
                }

                Spacer(modifier = Modifier.width(12.dp))

                Column(modifier = Modifier.weight(1f)) {
                    Text(
                        text = game.title,
                        color = TextPrimaryDark,
                        fontSize = 15.sp,
                        fontWeight = FontWeight.Bold
                    )
                    Spacer(modifier = Modifier.height(2.dp))
                    Text(
                        text = "Played ${ownership.playTimeMinutes} mins • Highscore: ${ownership.highscore}",
                        color = TextSecondaryDark,
                        fontSize = 11.sp
                    )
                    Text(
                        text = "${game.downloadSizeMb} MB on disk",
                        color = TextMutedDark,
                        fontSize = 11.sp
                    )
                }

                // If installed, show uninstall trash icon
                if (ownership.downloadState == DownloadState.INSTALLED) {
                    IconButton(
                        onClick = onUninstall,
                        modifier = Modifier.size(32.dp)
                    ) {
                        Icon(
                            imageVector = Icons.Default.DeleteOutline,
                            contentDescription = "Uninstall",
                            tint = TextMutedDark,
                            modifier = Modifier.size(18.dp)
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.height(12.dp))

            // Download State or Play Actions
            when (ownership.downloadState) {
                DownloadState.INSTALLED -> {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        Button(
                            onClick = onLaunchPlay,
                            modifier = Modifier
                                .weight(1f)
                                .height(44.dp)
                                .testTag("play_installed_${game.id}"),
                            colors = ButtonDefaults.buttonColors(containerColor = EmeraldSuccess),
                            shape = RoundedCornerShape(10.dp)
                        ) {
                            Icon(Icons.Default.PlayArrow, contentDescription = "Play", tint = DeepNavySurface)
                            Spacer(modifier = Modifier.width(6.dp))
                            Text("Launch & Play", color = DeepNavySurface, fontWeight = FontWeight.Bold)
                        }
                    }
                }
                DownloadState.DOWNLOADING -> {
                    Column(modifier = Modifier.fillMaxWidth()) {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween
                        ) {
                            Text(
                                text = "Downloading package (${ownership.downloadProgressPercent}%)...",
                                color = CyberCyanPrimary,
                                fontSize = 12.sp,
                                fontWeight = FontWeight.SemiBold
                            )
                            Text(
                                text = "${ownership.downloadSpeedMbPerSec} MB/s",
                                color = TextSecondaryDark,
                                fontSize = 11.sp
                            )
                        }
                        Spacer(modifier = Modifier.height(6.dp))
                        LinearProgressIndicator(
                            progress = { ownership.downloadProgressPercent / 100f },
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(6.dp),
                            color = CyberCyanPrimary,
                            trackColor = CardBorderStroke
                        )
                        Spacer(modifier = Modifier.height(8.dp))
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.spacedBy(8.dp)
                        ) {
                            OutlinedButton(
                                onClick = onPauseDownload,
                                modifier = Modifier.weight(1f)
                            ) {
                                Text("Pause", fontSize = 12.sp)
                            }
                            OutlinedButton(
                                onClick = onCancelDownload,
                                modifier = Modifier.weight(1f)
                            ) {
                                Text("Cancel", fontSize = 12.sp)
                            }
                        }
                    }
                }
                DownloadState.PAUSED -> {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        Button(
                            onClick = onStartDownload,
                            modifier = Modifier.weight(1f),
                            colors = ButtonDefaults.buttonColors(containerColor = CyberCyanPrimary)
                        ) {
                            Text("Resume (${ownership.downloadProgressPercent}%)", color = DeepNavySurface)
                        }
                        OutlinedButton(
                            onClick = onCancelDownload,
                            modifier = Modifier.weight(1f)
                        ) {
                            Text("Cancel", fontSize = 12.sp)
                        }
                    }
                }
                DownloadState.VERIFYING -> {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(
                            text = "Verifying cryptographic SHA-256 checksum...",
                            color = CyberCyanPrimary,
                            fontSize = 12.sp
                        )
                    }
                }
                DownloadState.NOT_DOWNLOADED -> {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        Button(
                            onClick = onStartDownload,
                            modifier = Modifier
                                .weight(1f)
                                .height(42.dp),
                            colors = ButtonDefaults.buttonColors(containerColor = CyberCyanPrimary),
                            shape = RoundedCornerShape(10.dp)
                        ) {
                            Icon(Icons.Default.Download, contentDescription = "Download", tint = DeepNavySurface)
                            Spacer(modifier = Modifier.width(6.dp))
                            Text("Download (${game.downloadSizeMb} MB)", color = DeepNavySurface, fontSize = 12.sp, fontWeight = FontWeight.Bold)
                        }
                        if (game.hasInstantPlay) {
                            OutlinedButton(
                                onClick = onLaunchPlay,
                                modifier = Modifier
                                    .weight(0.9f)
                                    .height(42.dp),
                                shape = RoundedCornerShape(10.dp)
                            ) {
                                Icon(Icons.Default.FlashOn, contentDescription = "Instant", tint = CyberCyanPrimary, modifier = Modifier.size(16.dp))
                                Spacer(modifier = Modifier.width(4.dp))
                                Text("Instant Play", fontSize = 12.sp)
                            }
                        }
                    }
                }
            }
        }
    }
}

package com.example.model

enum class DownloadState {
    NOT_DOWNLOADED,
    DOWNLOADING,
    PAUSED,
    VERIFYING,
    INSTALLED
}

enum class GameCategory {
    ALL,
    FREE_GAMES,
    OFFLINE_GAMES,
    ONLINE_GAMES,
    ACTION,
    ARCADE,
    SCI_FI,
    RACING,
    RPG,
    PUZZLE
}

enum class PlayMode(val label: String) {
    ONLINE("Online Multiplayer"),
    OFFLINE("100% Offline (No Wi-Fi)"),
    BOTH("Online & Offline")
}

data class GameReview(
    val id: String,
    val author: String,
    val rating: Int,
    val date: String,
    val comment: String,
    val isVerifiedBuyer: Boolean = true
)

data class GameItem(
    val id: String,
    val title: String,
    val tagline: String,
    val description: String,
    val category: GameCategory,
    val priceUsd: Double,
    val rating: Float,
    val reviewsCount: Int,
    val downloadSizeMb: Int,
    val isFeatured: Boolean = false,
    val badge: String? = null,
    val hasInstantPlay: Boolean = true,
    val developer: String,
    val releaseDate: String,
    val gradientColors: List<Long>,
    val playMode: PlayMode = PlayMode.BOTH,
    val reviews: List<GameReview> = emptyList(),
    val features: List<String> = emptyList()
) {
    val isFree: Boolean get() = priceUsd <= 0.0
}

data class GameOwnership(
    val gameId: String,
    val isOwned: Boolean = false,
    val purchaseDate: Long? = null,
    val purchasePriceUsd: Double = 0.0,
    val downloadState: DownloadState = DownloadState.NOT_DOWNLOADED,
    val downloadProgressPercent: Int = 0,
    val downloadSpeedMbPerSec: Double = 0.0,
    val playTimeMinutes: Int = 0,
    val highscore: Int = 0,
    val lastPlayedTimestamp: Long? = null
)

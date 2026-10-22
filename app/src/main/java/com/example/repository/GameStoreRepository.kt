package com.example.repository

import com.example.model.CardBrand
import com.example.model.DownloadState
import com.example.model.GameCategory
import com.example.model.GameItem
import com.example.model.GameOwnership
import com.example.model.GameReview
import com.example.model.MerchantAccountConfig
import com.example.model.PaymentGatewayType
import com.example.model.PlayMode
import com.example.model.SavedPaymentCard
import com.example.model.TransactionRecord
import com.example.model.TransactionStatus
import com.example.model.TransactionType
import com.example.model.UserAccountProfile
import com.example.security.PaymentSecurityManager
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import java.util.UUID

class GameStoreRepository(
    private val scope: CoroutineScope = CoroutineScope(Dispatchers.Default)
) {
    private val downloadJobs = mutableMapOf<String, Job>()

    // Catalog of premium games
    private val _catalog = MutableStateFlow<List<GameItem>>(getInitialCatalog())
    val catalog: StateFlow<List<GameItem>> = _catalog.asStateFlow()

    // Ownership state: map of gameId -> GameOwnership
    private val _ownershipMap = MutableStateFlow<Map<String, GameOwnership>>(getInitialOwnership())
    val ownershipMap: StateFlow<Map<String, GameOwnership>> = _ownershipMap.asStateFlow()

    // User wallet balance ($ USD)
    private val _walletBalance = MutableStateFlow<Double>(45.00)
    val walletBalance: StateFlow<Double> = _walletBalance.asStateFlow()

    // Saved Encrypted Payment Cards
    private val _savedCards = MutableStateFlow<List<SavedPaymentCard>>(getInitialSavedCards())
    val savedCards: StateFlow<List<SavedPaymentCard>> = _savedCards.asStateFlow()

    // Transactions History
    private val _transactions = MutableStateFlow<List<TransactionRecord>>(getInitialTransactions())
    val transactions: StateFlow<List<TransactionRecord>> = _transactions.asStateFlow()

    // Exclusive Owner / Creator Mode (Gaming Club Administrator)
    // When enabled, owner has 100% free access to all games without purchasing
    private val _isOwnerModeActive = MutableStateFlow(true)
    val isOwnerModeActive: StateFlow<Boolean> = _isOwnerModeActive.asStateFlow()

    // User Account Profile & Preferences
    private val _userProfile = MutableStateFlow(UserAccountProfile())
    val userProfile: StateFlow<UserAccountProfile> = _userProfile.asStateFlow()

    fun updateProfile(fullName: String, gamerTag: String, email: String) {
        _userProfile.update { it.copy(fullName = fullName, gamerTag = gamerTag, email = email) }
    }

    fun updateAccountSetting(
        biometric: Boolean? = null,
        twoFactor: Boolean? = null,
        downloadWifiOnly: Boolean? = null,
        autoUpdate: Boolean? = null,
        highPerf: Boolean? = null,
        soundHaptics: Boolean? = null,
        notifications: Boolean? = null,
        defaultGateway: PaymentGatewayType? = null
    ) {
        _userProfile.update { current ->
            current.copy(
                biometricAuthEnabled = biometric ?: current.biometricAuthEnabled,
                twoFactorAuthEnabled = twoFactor ?: current.twoFactorAuthEnabled,
                downloadWifiOnly = downloadWifiOnly ?: current.downloadWifiOnly,
                autoUpdateGames = autoUpdate ?: current.autoUpdateGames,
                highPerformanceMode = highPerf ?: current.highPerformanceMode,
                soundHapticsEnabled = soundHaptics ?: current.soundHapticsEnabled,
                pushNotificationsEnabled = notifications ?: current.pushNotificationsEnabled,
                defaultPaymentGateway = defaultGateway ?: current.defaultPaymentGateway
            )
        }
    }

    fun clearGameCache(): Double {
        val size = _userProfile.value.cacheSizeMb
        _userProfile.update { it.copy(cacheSizeMb = 0.0) }
        return size
    }

    // Currently active instant game session
    private val _activeGameSessionId = MutableStateFlow<String?>(null)
    val activeGameSessionId: StateFlow<String?> = _activeGameSessionId.asStateFlow()

    fun setInstantGameSession(gameId: String?) {
        _activeGameSessionId.value = gameId
    }

    fun setOwnerMode(active: Boolean) {
        _isOwnerModeActive.value = active
    }

    /**
     * Owner can modify game pricing or discounts.
     */
    fun updateGamePrice(gameId: String, newPriceUsd: Double) {
        _catalog.update { list ->
            list.map { game ->
                if (game.id == gameId) game.copy(priceUsd = newPriceUsd) else game
            }
        }
    }

    /**
     * Owner can toggle featured status.
     */
    fun toggleGameFeatured(gameId: String) {
        _catalog.update { list ->
            list.map { game ->
                if (game.id == gameId) game.copy(isFeatured = !game.isFeatured) else game
            }
        }
    }

    /**
     * Executes purchase of a paid game through the secure payment gateway.
     */
    fun purchaseGame(
        game: GameItem,
        gatewayType: PaymentGatewayType,
        paymentDescriptor: String,
        cryptoAmount: String? = null,
        cryptoCurrency: String? = null
    ): Result<TransactionRecord> {
        // If paying via wallet balance, verify sufficient funds
        if (gatewayType == PaymentGatewayType.WALLET_BALANCE) {
            if (_walletBalance.value < game.priceUsd) {
                return Result.failure(Exception("Insufficient wallet balance ($${String.format("%.2f", _walletBalance.value)}). Please top up funds or select another payment gateway."))
            }
            _walletBalance.update { it - game.priceUsd }
        }

        val token = PaymentSecurityManager.generateSecureToken("${gatewayType.name}-${System.currentTimeMillis()}-${game.id}")
        val authCode = PaymentSecurityManager.generatePciAuthCode()
        val receiptId = PaymentSecurityManager.generateReceiptId()
        val receiving = when (gatewayType) {
            PaymentGatewayType.BINANCE_PAY -> MerchantAccountConfig.BINANCE_PAY_ID
            PaymentGatewayType.UPI -> MerchantAccountConfig.PAYEE_UPI_ID
            else -> MerchantAccountConfig.PAYEE_NAME
        }

        val record = TransactionRecord(
            id = UUID.randomUUID().toString(),
            title = "Purchased: ${game.title}",
            type = TransactionType.GAME_PURCHASE,
            amountUsd = game.priceUsd,
            timestamp = System.currentTimeMillis(),
            gatewayType = gatewayType,
            methodDescriptor = paymentDescriptor,
            status = TransactionStatus.COMPLETED,
            encryptionToken = token,
            pciDssAuthCode = authCode,
            receiptId = receiptId,
            gameId = game.id,
            cryptoAmount = cryptoAmount,
            cryptoCurrency = cryptoCurrency,
            receivingAddress = receiving
        )

        // Mark as owned
        _ownershipMap.update { current ->
            val existing = current[game.id] ?: GameOwnership(gameId = game.id)
            current + (game.id to existing.copy(
                isOwned = true,
                purchaseDate = System.currentTimeMillis(),
                purchasePriceUsd = game.priceUsd
            ))
        }

        // Add to transactions ledger
        _transactions.update { listOf(record) + it }

        return Result.success(record)
    }

    /**
     * Claims a 100% free game directly into the user's library without payment checkout.
     */
    fun claimFreeGame(game: GameItem): Result<TransactionRecord> {
        val token = PaymentSecurityManager.generateSecureToken("FREE-${System.currentTimeMillis()}-${game.id}")
        val authCode = PaymentSecurityManager.generatePciAuthCode()
        val receiptId = PaymentSecurityManager.generateReceiptId()

        val record = TransactionRecord(
            id = UUID.randomUUID().toString(),
            title = "Claimed Free: ${game.title}",
            type = TransactionType.GAME_PURCHASE,
            amountUsd = 0.0,
            timestamp = System.currentTimeMillis(),
            gatewayType = PaymentGatewayType.WALLET_BALANCE,
            methodDescriptor = "Free Claim (Promo License)",
            status = TransactionStatus.COMPLETED,
            encryptionToken = token,
            pciDssAuthCode = authCode,
            receiptId = receiptId,
            gameId = game.id,
            receivingAddress = "Free Library License"
        )

        _ownershipMap.update { current ->
            val existing = current[game.id] ?: GameOwnership(gameId = game.id)
            current + (game.id to existing.copy(
                isOwned = true,
                purchaseDate = System.currentTimeMillis(),
                purchasePriceUsd = 0.0,
                downloadState = DownloadState.INSTALLED,
                downloadProgressPercent = 100
            ))
        }

        _transactions.update { listOf(record) + it }
        return Result.success(record)
    }

    /**
     * Tops up wallet funds securely through credit card, PayPal, UPI, or Binance Pay.
     */
    fun topUpWallet(
        amount: Double,
        gatewayType: PaymentGatewayType,
        paymentDescriptor: String,
        cryptoAmount: String? = null,
        cryptoCurrency: String? = null
    ): TransactionRecord {
        val token = PaymentSecurityManager.generateSecureToken("TOPUP-$amount-${System.currentTimeMillis()}")
        val authCode = PaymentSecurityManager.generatePciAuthCode()
        val receiptId = PaymentSecurityManager.generateReceiptId()
        val receiving = when (gatewayType) {
            PaymentGatewayType.BINANCE_PAY -> MerchantAccountConfig.BINANCE_PAY_ID
            PaymentGatewayType.UPI -> MerchantAccountConfig.PAYEE_UPI_ID
            else -> MerchantAccountConfig.PAYEE_NAME
        }

        _walletBalance.update { it + amount }

        val record = TransactionRecord(
            id = UUID.randomUUID().toString(),
            title = "Wallet Deposit (Top-Up)",
            type = TransactionType.WALLET_TOPUP,
            amountUsd = amount,
            timestamp = System.currentTimeMillis(),
            gatewayType = gatewayType,
            methodDescriptor = paymentDescriptor,
            status = TransactionStatus.COMPLETED,
            encryptionToken = token,
            pciDssAuthCode = authCode,
            receiptId = receiptId,
            cryptoAmount = cryptoAmount,
            cryptoCurrency = cryptoCurrency,
            receivingAddress = receiving
        )

        _transactions.update { listOf(record) + it }
        return record
    }

    /**
     * Process game refund if eligible (< 120 minutes play time).
     */
    fun refundGame(gameId: String): Result<TransactionRecord> {
        val ownership = _ownershipMap.value[gameId]
            ?: return Result.failure(Exception("Game is not owned."))

        if (ownership.playTimeMinutes > 120) {
            return Result.failure(Exception("Refund policy limit exceeded (>2 hours of playtime)."))
        }

        val game = _catalog.value.find { it.id == gameId }
            ?: return Result.failure(Exception("Game not found."))

        // Cancel any active download
        cancelDownload(gameId)

        val refundAmount = if (ownership.purchasePriceUsd > 0) ownership.purchasePriceUsd else game.priceUsd
        _walletBalance.update { it + refundAmount }

        val record = TransactionRecord(
            id = UUID.randomUUID().toString(),
            title = "Refund: ${game.title}",
            type = TransactionType.GAME_REFUND,
            amountUsd = refundAmount,
            timestamp = System.currentTimeMillis(),
            gatewayType = PaymentGatewayType.WALLET_BALANCE,
            methodDescriptor = "Refund Credited to Vault Balance",
            status = TransactionStatus.REFUNDED,
            encryptionToken = PaymentSecurityManager.generateSecureToken("REFUND-$gameId"),
            pciDssAuthCode = PaymentSecurityManager.generatePciAuthCode(),
            receiptId = PaymentSecurityManager.generateReceiptId(),
            gameId = gameId
        )

        _ownershipMap.update { current ->
            val updated = current.toMutableMap()
            updated[gameId] = (current[gameId] ?: GameOwnership(gameId = gameId)).copy(
                isOwned = false,
                downloadState = DownloadState.NOT_DOWNLOADED,
                downloadProgressPercent = 0
            )
            updated
        }

        _transactions.update { listOf(record) + it }
        return Result.success(record)
    }

    /**
     * Starts game package download with realistic progress & speed simulation.
     */
    fun startDownload(gameId: String) {
        val isOwner = _isOwnerModeActive.value
        val currentOwnership = _ownershipMap.value[gameId] ?: GameOwnership(gameId = gameId, isOwned = isOwner)
        if (!isOwner && !currentOwnership.isOwned) return

        downloadJobs[gameId]?.cancel()

        downloadJobs[gameId] = scope.launch {
            _ownershipMap.update { current ->
                val prev = current[gameId] ?: GameOwnership(gameId = gameId)
                current + (gameId to prev.copy(
                    downloadState = DownloadState.DOWNLOADING,
                    downloadSpeedMbPerSec = 22.4
                ))
            }

            var progress = (_ownershipMap.value[gameId]?.downloadProgressPercent ?: 0)
            while (progress < 100) {
                delay(350)
                progress = (progress + (4..8).random()).coerceAtMost(100)
                val simulatedSpeed = (180..260).random() / 10.0

                _ownershipMap.update { current ->
                    val prev = current[gameId] ?: GameOwnership(gameId = gameId)
                    current + (gameId to prev.copy(
                        downloadProgressPercent = progress,
                        downloadSpeedMbPerSec = if (progress < 100) simulatedSpeed else 0.0,
                        downloadState = if (progress == 100) DownloadState.VERIFYING else DownloadState.DOWNLOADING
                    ))
                }
            }

            // Brief checksum integrity verification
            delay(600)
            _ownershipMap.update { current ->
                val prev = current[gameId] ?: GameOwnership(gameId = gameId)
                current + (gameId to prev.copy(
                    downloadState = DownloadState.INSTALLED,
                    downloadProgressPercent = 100,
                    downloadSpeedMbPerSec = 0.0
                ))
            }
        }
    }

    fun pauseDownload(gameId: String) {
        downloadJobs[gameId]?.cancel()
        _ownershipMap.update { current ->
            val prev = current[gameId] ?: GameOwnership(gameId = gameId)
            current + (gameId to prev.copy(
                downloadState = DownloadState.PAUSED,
                downloadSpeedMbPerSec = 0.0
            ))
        }
    }

    fun cancelDownload(gameId: String) {
        downloadJobs[gameId]?.cancel()
        _ownershipMap.update { current ->
            val prev = current[gameId] ?: GameOwnership(gameId = gameId)
            current + (gameId to prev.copy(
                downloadState = DownloadState.NOT_DOWNLOADED,
                downloadProgressPercent = 0,
                downloadSpeedMbPerSec = 0.0
            ))
        }
    }

    fun uninstallGame(gameId: String) {
        cancelDownload(gameId)
    }

    fun addRewardCoins(amountUsd: Double, gameTitle: String) {
        _walletBalance.update { it + amountUsd }
        val record = TransactionRecord(
            id = UUID.randomUUID().toString(),
            title = "Arcade Reward: $gameTitle",
            type = TransactionType.COIN_REWARD,
            amountUsd = amountUsd,
            timestamp = System.currentTimeMillis(),
            gatewayType = PaymentGatewayType.WALLET_BALANCE,
            methodDescriptor = "In-Game Victory Bonus",
            status = TransactionStatus.COMPLETED,
            encryptionToken = PaymentSecurityManager.generateSecureToken("REWARD-$amountUsd"),
            pciDssAuthCode = "REWARD-SEC-CLAIM",
            receiptId = PaymentSecurityManager.generateReceiptId()
        )
        _transactions.update { listOf(record) + it }
    }

    fun recordGameScore(gameId: String, score: Int, minutesPlayed: Int) {
        _ownershipMap.update { current ->
            val prev = current[gameId] ?: GameOwnership(gameId = gameId)
            current + (gameId to prev.copy(
                highscore = maxOf(prev.highscore, score),
                playTimeMinutes = prev.playTimeMinutes + minutesPlayed,
                lastPlayedTimestamp = System.currentTimeMillis()
            ))
        }
    }

    fun savePaymentCard(card: SavedPaymentCard) {
        _savedCards.update { current ->
            val updated = if (card.isDefault) {
                current.map { it.copy(isDefault = false) }
            } else {
                current
            }
            listOf(card) + updated
        }
    }

    fun removePaymentCard(cardId: String) {
        _savedCards.update { current -> current.filterNot { it.id == cardId } }
    }

    private fun getInitialCatalog(): List<GameItem> {
        return listOf(
            GameItem(
                id = "cyber_blaster",
                title = "Cyber Blaster: Neon Strike",
                tagline = "Hyper-octane vertical cyber arcade shooter with particle physics",
                description = "Piloting a photon-charged interceptor through neon defense grids, annihilate rogue AI sentinels, collect antimatter energy cores, and unleash hyper-beam cascades in this responsive arcade shooter.",
                category = GameCategory.ARCADE,
                priceUsd = 4.99,
                rating = 4.9f,
                reviewsCount = 1420,
                downloadSizeMb = 148,
                isFeatured = true,
                badge = "FEATURED ARCADE",
                hasInstantPlay = true,
                developer = "QuantumForge Studios",
                releaseDate = "2026",
                gradientColors = listOf(0xFF00E5FF, 0xFF8B5CF6, 0xFFFF2A85),
                features = listOf("Instant WebAssembly/Canvas Playable", "60 FPS Particle Physics", "Touch & Tilt Controls", "Global Leaderboards"),
                reviews = listOf(
                    GameReview("r1", "Alex Vance", 5, "2 days ago", "The instant play mode is completely smooth! Bought it right after."),
                    GameReview("r2", "Sarah Connor", 5, "1 week ago", "Best arcade shooter on mobile. Haptic feedback feels awesome.")
                )
            ),
            GameItem(
                id = "hyper_velocity",
                title = "HyperDrive: Velocity 2099",
                tagline = "Anti-gravity supersonic racing on futuristic skyway circuits",
                description = "Break the sound barrier with high-speed anti-gravity hovercrafts. Navigate high-g corkscrews, master drift-boost mechanics, and outrun rivals in neon-lit cyberpunk skylines.",
                category = GameCategory.RACING,
                priceUsd = 6.99,
                rating = 4.8f,
                reviewsCount = 890,
                downloadSizeMb = 512,
                isFeatured = true,
                badge = "BESTSELLER",
                hasInstantPlay = true,
                developer = "Veloce Interactive",
                releaseDate = "2026",
                gradientColors = listOf(0xFFFF3366, 0xFFF59E0B, 0xFF8B5CF6),
                features = listOf("Ultra 4K Textures", "Adaptive Sound Engine", "Real-time Ghost Racing", "Full Gamepad Support"),
                reviews = listOf(
                    GameReview("r3", "Kenji Sato", 5, "3 days ago", "Smooth frame rates and amazing synthwave soundtrack.")
                )
            ),
            GameItem(
                id = "chronicles_eldoria",
                title = "Chronicles of Eldoria",
                tagline = "Epic open-world action RPG with dynamic elemental combat",
                description = "Awaken as the Runebound Warrior in a realm shattered by celestial storms. Forge mystical weapons, tame elemental beasts, and unravel ancient ruins in deep story-driven quests.",
                category = GameCategory.RPG,
                priceUsd = 9.99,
                rating = 4.9f,
                reviewsCount = 3120,
                downloadSizeMb = 1240,
                isFeatured = true,
                badge = "EDITOR'S CHOICE",
                hasInstantPlay = false,
                developer = "Aetherium Games",
                releaseDate = "2026",
                gradientColors = listOf(0xFF10B981, 0xFF059669, 0xFF064E3B),
                features = listOf("40+ Hours of Storyline", "Branching Narrative Dialogs", "Rich Skill Trees", "Zero Microtransactions"),
                reviews = listOf(
                    GameReview("r4", "David K.", 5, "Yesterday", "Worth every penny. True console quality on Android.")
                )
            ),
            GameItem(
                id = "quantum_grid",
                title = "Quantum Grid: Mind Hack",
                tagline = "Cyber-neural sequence puzzle that bends logic and memory",
                description = "Infiltrate encrypted corporate mainframe circuits by matching neural frequencies, decoding matrix patterns, and overriding firewall algorithms against the ticking clock.",
                category = GameCategory.PUZZLE,
                priceUsd = 2.99,
                rating = 4.7f,
                reviewsCount = 650,
                downloadSizeMb = 85,
                isFeatured = false,
                badge = "TOP BRAIN GAME",
                hasInstantPlay = true,
                developer = "Synapse Labs",
                releaseDate = "2026",
                gradientColors = listOf(0xFF38BDF8, 0xFF6366F1, 0xFF4338CA),
                features = listOf("Playable Instantly", "200+ Neural Levels", "Adaptive Difficulty", "Minimalist Cyber Aesthetic")
            ),
            GameItem(
                id = "apex_shadow",
                title = "Apex Shadow: Rogue Ops",
                tagline = "Tactical isometric stealth shooter with drone support",
                description = "Command elite black-ops operatives navigating heavily guarded facilities. Utilize thermal night-vision, cloaking shields, EMP disrupters, and coordinated drone strikes.",
                category = GameCategory.ACTION,
                priceUsd = 7.49,
                rating = 4.8f,
                reviewsCount = 1100,
                downloadSizeMb = 780,
                isFeatured = false,
                badge = "TACTICAL HIT",
                hasInstantPlay = false,
                developer = "DarkSector Studios",
                releaseDate = "2026",
                gradientColors = listOf(0xFF64748B, 0xFF334155, 0xFF0F172A),
                features = listOf("Tactical Pause Mechanism", "Ragdoll Physics", "Dynamic Weather & Fog of War")
            ),
            GameItem(
                id = "star_strike_odyssey",
                title = "StarStrike: Galaxy Defense",
                tagline = "Deep space fleet command and planetary defense grid",
                description = "Defend frontier colonies against alien armada incursions. Build orbital laser defense arrays, deploy drone swarms, and upgrade your mothership flagship weapons.",
                category = GameCategory.SCI_FI,
                priceUsd = 3.99,
                rating = 4.6f,
                reviewsCount = 420,
                downloadSizeMb = 310,
                isFeatured = false,
                badge = "NEW RELEASE",
                hasInstantPlay = true,
                developer = "Nebula Interactive",
                releaseDate = "2026",
                gradientColors = listOf(0xFF8B5CF6, 0xFFC084FC, 0xFF4C1D95),
                features = listOf("Tower Defense & Space Combat", "Procedural Alien Waves", "Cloud Save Support")
            ),
            GameItem(
                id = "shadow_assassin",
                title = "Shadow Assassin: Bloodline",
                tagline = "Triple-A cinematic stealth action in feudal cyber-dynasty",
                description = "Master ancient ninja arts blended with high-tech cybernetics. Execute silent takedowns, wield the shadow katana, and eliminate corrupt shogunate warlords across breathtaking vertical fortresses.",
                category = GameCategory.ACTION,
                priceUsd = 8.99,
                rating = 4.9f,
                reviewsCount = 4280,
                downloadSizeMb = 1450,
                isFeatured = true,
                badge = "AAA MASTERPIECE",
                hasInstantPlay = false,
                developer = "Kurogane Studios",
                releaseDate = "2026",
                gradientColors = listOf(0xFF1E1B4B, 0xFF4338CA, 0xFFE11D48),
                features = listOf("Full Ray-Traced Reflections", "60 FPS Combat Engine", "Dual-Wield Katana Mastery", "Zero In-App Purchases"),
                reviews = listOf(
                    GameReview("r5", "Marcus Chen", 5, "Today", "Absolute console perfection. Worth every single cent!"),
                    GameReview("r6", "Vikram Patel", 5, "Yesterday", "The graphics and stealth mechanics are unreal.")
                )
            ),
            GameItem(
                id = "titan_mech",
                title = "Titan Mech: Iron Frontier",
                tagline = "Heavy armored mech combat simulator on post-war colonies",
                description = "Pilot 50-ton customized battle mechs equipped with railguns, plasma cannons, and jump-jets. Engage in destructive warzones with realistic locational damage and armor penetration physics.",
                category = GameCategory.SCI_FI,
                priceUsd = 5.99,
                rating = 4.8f,
                reviewsCount = 1890,
                downloadSizeMb = 920,
                isFeatured = true,
                badge = "TRENDING #1",
                hasInstantPlay = false,
                developer = "Vanguard Heavy Industries",
                releaseDate = "2026",
                gradientColors = listOf(0xFF334155, 0xFFD97706, 0xFF1E293B),
                features = listOf("Deep Mech Customization", "Destructible Terrain", "Dynamic Locational Damage"),
                reviews = listOf(
                    GameReview("r7", "Elena Rostova", 5, "3 days ago", "Incredible mech cockpit feel and brutal sound design.")
                )
            ),
            GameItem(
                id = "grand_velocity",
                title = "Grand Velocity: Tokyo Drift",
                tagline = "Ultra-realistic underground street drift and tuning simulator",
                description = "Build the ultimate Japanese street machines. Race across Shinjuku highways and midnight mountain passes with true-to-life tire friction, engine acoustics, and aesthetic bodykits.",
                category = GameCategory.RACING,
                priceUsd = 7.99,
                rating = 4.9f,
                reviewsCount = 2740,
                downloadSizeMb = 1150,
                isFeatured = true,
                badge = "CRITICS CHOICE",
                hasInstantPlay = false,
                developer = "Midnight Garage Works",
                releaseDate = "2026",
                gradientColors = listOf(0xFFE11D48, 0xFFF97316, 0xFF7C3AED),
                features = listOf("Licensed JDM Cars", "Advanced Telemetry Drift Physics", "Custom Vinyl Decal Studio"),
                reviews = listOf(
                    GameReview("r8", "Ryosuke T.", 5, "2 days ago", "The drift physics feel just like real assetto corsa!")
                )
            ),
            GameItem(
                id = "mythic_dungeon",
                title = "Mythic Dungeon: Dark Descent",
                tagline = "Hardcore tactical roguelike crawler with procedural crypts",
                description = "Delve into cursed catacombs teeming with lich necromancers and eldritch horrors. Collect ancient runes, trigger deadly dungeon traps, and survive permadeath dungeons.",
                category = GameCategory.RPG,
                priceUsd = 4.49,
                rating = 4.7f,
                reviewsCount = 940,
                downloadSizeMb = 460,
                isFeatured = false,
                badge = "ROGUELIKE HIT",
                hasInstantPlay = false,
                developer = "Runestone Games",
                releaseDate = "2026",
                gradientColors = listOf(0xFF581C87, 0xFF6B21A8, 0xFF0F172A),
                features = listOf("100+ Procedural Dungeon Floors", "Permadeath Mode", "Epic Boss Encounters")
            ),
            GameItem(
                id = "astro_miner",
                title = "Astro Miner: Void Odyssey",
                tagline = "Deep space asteroid drilling and orbital station tycoon",
                description = "Command deep-space mining rigs harvesting crystalline asteroidal minerals. Build refineries, stave off space pirates, and unlock quantum warp drives.",
                category = GameCategory.PUZZLE,
                priceUsd = 3.99,
                rating = 4.8f,
                reviewsCount = 820,
                downloadSizeMb = 280,
                isFeatured = false,
                badge = "TOP INDIE",
                hasInstantPlay = true,
                developer = "Cosmic Core Labs",
                releaseDate = "2026",
                gradientColors = listOf(0xFF0284C7, 0xFF0369A1, 0xFF082F49),
                features = listOf("Instant Play Mode", "Automation Factory Loops", "Atmospheric Ambient Audio")
            ),
            GameItem(
                id = "neon_cyberblade",
                title = "Neon Cyberblade: Ronin 2088",
                tagline = "High-octane synthwave slash-and-dash reflex action",
                description = "Unleash hypersonic sword dashes through neon skyscrapers. Deflect laser fire, slice through security androids, and synchronize your strikes with high-BPM synth beats.",
                category = GameCategory.ARCADE,
                priceUsd = 6.49,
                rating = 4.9f,
                reviewsCount = 2150,
                downloadSizeMb = 640,
                isFeatured = true,
                badge = "HOT RELEASE",
                hasInstantPlay = true,
                developer = "HyperWave Interactive",
                releaseDate = "2026",
                gradientColors = listOf(0xFFEC4899, 0xFF8B5CF6, 0xFF06B6D4),
                features = listOf("Playable Instantly", "Adrenaline Rhythm Slicing", "Leaderboard Speedrunning"),
                reviews = listOf(
                    GameReview("r9", "Kai Zen", 5, "5 hours ago", "Non-stop adrenaline! Instant play was super quick to try.")
                )
            ),
            GameItem(
                id = "apex_arena",
                title = "Apex Arena: Cyber Legends",
                tagline = "Free-to-play cross-platform online battle royale & 3v3 squad arena",
                description = "Drop into a futuristic neon metropolis with your squad. Master unique cyber abilities, coordinate real-time tactical airstrikes, and dominate global ranked leagues in fast-paced 10-minute battle royale matches.",
                category = GameCategory.ACTION,
                priceUsd = 0.0,
                rating = 4.9f,
                reviewsCount = 12450,
                downloadSizeMb = 380,
                isFeatured = true,
                badge = "FREE • ONLINE",
                hasInstantPlay = true,
                playMode = PlayMode.ONLINE,
                developer = "TitanForge Studios",
                releaseDate = "2026",
                gradientColors = listOf(0xFF00E5FF, 0xFF0284C7, 0xFF1E1B4B),
                features = listOf("100% Free Forever", "Online 3v3 Cross-Platform Squads", "Instant Playable Match", "Zero Pay-to-Win Mechanics"),
                reviews = listOf(
                    GameReview("fr1", "Rahul Sharma", 5, "Today", "Amazing free online game! Runs butter smooth on 60 FPS."),
                    GameReview("fr2", "Zack Miller", 5, "Yesterday", "Best free multiplayer game in the store. Instant play rocks.")
                )
            ),
            GameItem(
                id = "pixel_dungeon",
                title = "Pixel Dungeon: Offline Quest",
                tagline = "100% offline singleplayer procedural roguelike dungeon crawler",
                description = "Delve deep into subterranean catacombs without needing an internet connection. Encounter 80+ mythological monsters, craft legendary runic gear, and uncover hidden treasure vaults in this battery-optimized offline adventure.",
                category = GameCategory.RPG,
                priceUsd = 0.0,
                rating = 4.8f,
                reviewsCount = 8920,
                downloadSizeMb = 48,
                isFeatured = true,
                badge = "FREE • 100% OFFLINE",
                hasInstantPlay = true,
                playMode = PlayMode.OFFLINE,
                developer = "SoloPixel Works",
                releaseDate = "2026",
                gradientColors = listOf(0xFF10B981, 0xFF059669, 0xFF064E3B),
                features = listOf("No Wi-Fi or Cellular Required", "100% Offline Gameplay", "Instant Resume Anywhere", "Lightweight 48 MB"),
                reviews = listOf(
                    GameReview("fr3", "Priya Nair", 5, "2 days ago", "Perfect for flights and travel! Completely offline and deeply engaging.")
                )
            ),
            GameItem(
                id = "neon_rally",
                title = "Neon Rally: Cyber Pong",
                tagline = "High-speed arcade rally - Play solo offline vs AI or online PvP",
                description = "Classic arcade reflex rally reimagined with pulsing synthwave particles and dynamic velocity physics. Switch seamlessly between offline solo survival mode and real-time online 1v1 multiplayer duels.",
                category = GameCategory.ARCADE,
                priceUsd = 0.0,
                rating = 4.9f,
                reviewsCount = 5670,
                downloadSizeMb = 32,
                isFeatured = true,
                badge = "FREE • ON/OFFLINE",
                hasInstantPlay = true,
                playMode = PlayMode.BOTH,
                developer = "RetroWave Interactive",
                releaseDate = "2026",
                gradientColors = listOf(0xFFFF007A, 0xFF7928CA, 0xFF00DFD8),
                features = listOf("Offline vs Smart AI Bot", "Online 1v1 Peer Duels", "Instant Browser & Canvas Play", "Global Highscore Grid"),
                reviews = listOf(
                    GameReview("fr4", "Leo Gomez", 5, "3 hours ago", "Super addictive! Love that I can play offline or challenge friends.")
                )
            ),
            GameItem(
                id = "cosmic_chess",
                title = "Cosmic Chess: Tactics 3D",
                tagline = "Sci-fi tactical chess with offline neural bots & online matchmaking",
                description = "Command futuristic starfleet pieces across an interactive holographic chessboard. Challenge offline AI engines from Beginner to Grandmaster, or jump into online ranked blitz matches anytime.",
                category = GameCategory.PUZZLE,
                priceUsd = 0.0,
                rating = 4.8f,
                reviewsCount = 4310,
                downloadSizeMb = 65,
                isFeatured = false,
                badge = "FREE • STRATEGY",
                hasInstantPlay = true,
                playMode = PlayMode.BOTH,
                developer = "NeuralMind Games",
                releaseDate = "2026",
                gradientColors = listOf(0xFF6366F1, 0xFF4F46E5, 0xFF312E81),
                features = listOf("Offline Engine (5 Difficulty Levels)", "Online ELO Matchmaking", "Tactical Puzzle Mode", "Zero Microtransactions")
            ),
            GameItem(
                id = "velocity_runner",
                title = "Velocity Runner: Cyber Grid",
                tagline = "Endless high-speed obstacle runner playable 100% offline",
                description = "Sprint through endless procedural grid highways dodging high-voltage barriers, collecting anti-gravity coins, and setting local offline records with zero network latency.",
                category = GameCategory.RACING,
                priceUsd = 0.0,
                rating = 4.7f,
                reviewsCount = 6840,
                downloadSizeMb = 55,
                isFeatured = false,
                badge = "FREE • OFFLINE",
                hasInstantPlay = true,
                playMode = PlayMode.OFFLINE,
                developer = "ZeroLag Studios",
                releaseDate = "2026",
                gradientColors = listOf(0xFFF59E0B, 0xFFD97706, 0xFFB45309),
                features = listOf("Play Anywhere Without Wi-Fi", "Zero Load Time", "High-FPS Touch Controls", "Instant Playable")
            )
        )
    }

    private fun getInitialOwnership(): Map<String, GameOwnership> {
        // Pre-seed "Cyber Blaster: Neon Strike" as owned so the user can immediately test instant play and downloading
        return mapOf(
            "cyber_blaster" to GameOwnership(
                gameId = "cyber_blaster",
                isOwned = true,
                purchaseDate = System.currentTimeMillis() - 86400000L,
                purchasePriceUsd = 4.99,
                downloadState = DownloadState.INSTALLED,
                downloadProgressPercent = 100,
                playTimeMinutes = 24,
                highscore = 1450
            )
        )
    }

    private fun getInitialSavedCards(): List<SavedPaymentCard> {
        return listOf(
            SavedPaymentCard(
                id = "card_1",
                cardholderName = "Alex Hunter",
                lastFour = "4242",
                brand = CardBrand.VISA,
                expiryMonthYear = "08/28",
                isDefault = true,
                tokenizedHash = "tok_pci_e2ee_8a49c01f37e1"
            )
        )
    }

    private fun getInitialTransactions(): List<TransactionRecord> {
        return listOf(
            TransactionRecord(
                id = "tx_01",
                title = "Purchased: Cyber Blaster: Neon Strike",
                type = TransactionType.GAME_PURCHASE,
                amountUsd = 4.99,
                timestamp = System.currentTimeMillis() - 86400000L,
                gatewayType = PaymentGatewayType.CREDIT_CARD,
                methodDescriptor = "Visa ending in •••• 4242",
                status = TransactionStatus.COMPLETED,
                encryptionToken = "tok_pci_e2ee_8a49c01f37e1",
                pciDssAuthCode = "AUTH-87AF92-3DS2",
                receiptId = "PV-REC-892104",
                gameId = "cyber_blaster"
            ),
            TransactionRecord(
                id = "tx_00",
                title = "Initial Account Wallet Credit",
                type = TransactionType.WALLET_TOPUP,
                amountUsd = 50.00,
                timestamp = System.currentTimeMillis() - 172800000L,
                gatewayType = PaymentGatewayType.PAYPAL,
                methodDescriptor = "PayPal (alex.hunter@gamer.net)",
                status = TransactionStatus.COMPLETED,
                encryptionToken = "tok_pci_e2ee_paypal_7f21a",
                pciDssAuthCode = "AUTH-PP-910283",
                receiptId = "PV-REC-701923"
            )
        )
    }
}

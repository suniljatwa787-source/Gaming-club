package com.example.ui.game

import android.content.Context
import android.os.Build
import android.os.VibrationEffect
import android.os.Vibrator
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.gestures.detectDragGestures
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.MonetizationOn
import androidx.compose.material.icons.filled.Pause
import androidx.compose.material.icons.filled.PlayArrow
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableFloatStateOf
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableLongStateOf
import androidx.compose.runtime.mutableStateListOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.runtime.withFrameNanos
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.drawscope.DrawScope
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.model.GameItem
import com.example.model.PlayMode
import com.example.ui.theme.CardSurfaceDark
import com.example.ui.theme.CyberCyanPrimary
import com.example.ui.theme.DeepNavySurface
import com.example.ui.theme.EmeraldSuccess
import com.example.ui.theme.GoldCurrency
import com.example.ui.theme.MidnightObsidian
import com.example.ui.theme.NeonPinkAccent
import com.example.ui.theme.TextPrimaryDark
import com.example.ui.theme.TextSecondaryDark
import kotlin.math.hypot
import kotlin.random.Random

data class Bullet(
    var x: Float,
    var y: Float,
    var vy: Float = -24f
)

data class EnemyTarget(
    var x: Float,
    var y: Float,
    var radius: Float,
    var vy: Float,
    var hp: Int,
    var isDrone: Boolean
)

data class CoinCollectible(
    var x: Float,
    var y: Float,
    var vy: Float = 6f,
    var radius: Float = 16f
)

data class Particle(
    var x: Float,
    var y: Float,
    var vx: Float,
    var vy: Float,
    var life: Float,
    var color: Color
)

@Composable
fun PlayableArcadeGame(
    game: GameItem,
    onExitGame: () -> Unit,
    onRewardEarned: (Double, String) -> Unit,
    onGameOverRecord: (String, Int, Int) -> Unit
) {
    val context = LocalContext.current
    val vibrator = remember { context.getSystemService(Context.VIBRATOR_SERVICE) as? Vibrator }

    var playerX by remember { mutableFloatStateOf(0f) }
    var playerY by remember { mutableFloatStateOf(0f) }
    var isInitialized by remember { mutableStateOf(false) }

    var score by remember { mutableIntStateOf(0) }
    var coinsCollected by remember { mutableIntStateOf(0) }
    var shieldHealth by remember { mutableFloatStateOf(100f) }
    var isPaused by remember { mutableStateOf(false) }
    var isGameOver by remember { mutableStateOf(false) }

    var gameStartTime by remember { mutableLongStateOf(System.currentTimeMillis()) }

    val bullets = remember { mutableStateListOf<Bullet>() }
    val enemies = remember { mutableStateListOf<EnemyTarget>() }
    val coins = remember { mutableStateListOf<CoinCollectible>() }
    val particles = remember { mutableStateListOf<Particle>() }

    fun triggerHaptic(durationMs: Long = 40) {
        try {
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                vibrator?.vibrate(VibrationEffect.createOneShot(durationMs, VibrationEffect.DEFAULT_AMPLITUDE))
            } else {
                @Suppress("DEPRECATION")
                vibrator?.vibrate(durationMs)
            }
        } catch (_: Exception) {}
    }

    fun restartGame() {
        score = 0
        coinsCollected = 0
        shieldHealth = 100f
        isGameOver = false
        isPaused = false
        bullets.clear()
        enemies.clear()
        coins.clear()
        particles.clear()
        gameStartTime = System.currentTimeMillis()
    }

    // Main Game Loop
    LaunchedEffect(isPaused, isGameOver) {
        if (isPaused || isGameOver) return@LaunchedEffect

        var lastShotTime = 0L
        var lastSpawnTime = 0L

        while (!isGameOver && !isPaused) {
            withFrameNanos { frameNanos ->
                val now = frameNanos / 1_000_000

                // 1. Auto-fire laser bullets
                if (now - lastShotTime > 180) {
                    lastShotTime = now
                    bullets.add(Bullet(x = playerX - 16f, y = playerY - 30f))
                    bullets.add(Bullet(x = playerX + 16f, y = playerY - 30f))
                }

                // 2. Spawn enemies & coins
                if (now - lastSpawnTime > 900) {
                    lastSpawnTime = now
                    val spawnX = Random.nextFloat() * 800f + 50f
                    val isDrone = Random.nextBoolean()
                    enemies.add(
                        EnemyTarget(
                            x = spawnX,
                            y = -50f,
                            radius = if (isDrone) 26f else 34f,
                            vy = Random.nextFloat() * 4f + 5f,
                            hp = if (isDrone) 1 else 2,
                            isDrone = isDrone
                        )
                    )

                    // 30% chance to spawn floating gold coin
                    if (Random.nextFloat() < 0.35f) {
                        coins.add(
                            CoinCollectible(
                                x = Random.nextFloat() * 800f + 50f,
                                y = -40f,
                                vy = 5f
                            )
                        )
                    }
                }

                // 3. Move bullets
                val bulletIter = bullets.iterator()
                while (bulletIter.hasNext()) {
                    val b = bulletIter.next()
                    b.y += b.vy
                    if (b.y < -100f) {
                        bulletIter.remove()
                    }
                }

                // 4. Move enemies and check collisions with bullets
                val enemyIter = enemies.iterator()
                while (enemyIter.hasNext()) {
                    val enemy = enemyIter.next()
                    enemy.y += enemy.vy

                    // Collision with bullets
                    val bIter = bullets.iterator()
                    while (bIter.hasNext()) {
                        val b = bIter.next()
                        if (hypot(b.x - enemy.x, b.y - enemy.y) < enemy.radius + 10f) {
                            bIter.remove()
                            enemy.hp -= 1
                            if (enemy.hp <= 0) {
                                // Explosion particles
                                for (p in 0..12) {
                                    particles.add(
                                        Particle(
                                            x = enemy.x,
                                            y = enemy.y,
                                            vx = (Random.nextFloat() - 0.5f) * 16f,
                                            vy = (Random.nextFloat() - 0.5f) * 16f,
                                            life = 1f,
                                            color = if (enemy.isDrone) NeonPinkAccent else CyberCyanPrimary
                                        )
                                    )
                                }
                                score += if (enemy.isDrone) 100 else 150
                                triggerHaptic(30)
                                enemyIter.remove()
                                break
                            }
                        }
                    }

                    // Collision with player ship
                    if (hypot(enemy.x - playerX, enemy.y - playerY) < enemy.radius + 24f) {
                        shieldHealth -= 25f
                        triggerHaptic(80)
                        enemyIter.remove()
                        if (shieldHealth <= 0f) {
                            shieldHealth = 0f
                            isGameOver = true
                            val elapsedMinutes = ((System.currentTimeMillis() - gameStartTime) / 60000).toInt().coerceAtLeast(1)
                            onGameOverRecord(game.id, score, elapsedMinutes)
                        }
                    }

                    // Out of screen bounds
                    if (enemy.y > 2200f) {
                        enemyIter.remove()
                    }
                }

                // 5. Move Coins & Check collection by player
                val coinIter = coins.iterator()
                while (coinIter.hasNext()) {
                    val coin = coinIter.next()
                    coin.y += coin.vy

                    if (hypot(coin.x - playerX, coin.y - playerY) < coin.radius + 30f) {
                        coinsCollected += 1
                        score += 250
                        triggerHaptic(50)
                        coinIter.remove()
                        // Spawn sparkles
                        for (p in 0..8) {
                            particles.add(
                                Particle(
                                    x = coin.x,
                                    y = coin.y,
                                    vx = (Random.nextFloat() - 0.5f) * 10f,
                                    vy = (Random.nextFloat() - 0.5f) * 10f,
                                    life = 1f,
                                    color = GoldCurrency
                                )
                            )
                        }
                    } else if (coin.y > 2200f) {
                        coinIter.remove()
                    }
                }

                // 6. Update particles
                val pIter = particles.iterator()
                while (pIter.hasNext()) {
                    val p = pIter.next()
                    p.x += p.vx
                    p.y += p.vy
                    p.life -= 0.05f
                    if (p.life <= 0f) {
                        pIter.remove()
                    }
                }
            }
        }
    }

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(MidnightObsidian)
            .testTag("playable_arcade_game_screen")
    ) {
        // Interactive Canvas Layer
        Canvas(
            modifier = Modifier
                .fillMaxSize()
                .pointerInput(Unit) {
                    detectDragGestures { change, dragAmount ->
                        change.consume()
                        playerX = (playerX + dragAmount.x).coerceIn(40f, size.width.toFloat() - 40f)
                        playerY = (playerY + dragAmount.y).coerceIn(120f, size.height.toFloat() - 100f)
                    }
                }
        ) {
            // Initial positioning
            if (!isInitialized) {
                playerX = size.width / 2f
                playerY = size.height * 0.78f
                isInitialized = true
            }

            // Draw deep space starry background grid
            drawRect(
                brush = Brush.verticalGradient(
                    colors = listOf(MidnightObsidian, DeepNavySurface, MidnightObsidian)
                )
            )

            // Draw Bullets
            bullets.forEach { b ->
                drawCircle(
                    color = CyberCyanPrimary,
                    radius = 5f,
                    center = Offset(b.x, b.y)
                )
                drawLine(
                    color = CyberCyanPrimary.copy(alpha = 0.6f),
                    start = Offset(b.x, b.y),
                    end = Offset(b.x, b.y + 16f),
                    strokeWidth = 3f
                )
            }

            // Draw Enemies
            enemies.forEach { e ->
                if (e.isDrone) {
                    drawCircle(
                        brush = Brush.radialGradient(
                            colors = listOf(NeonPinkAccent, Color(0xFF990044)),
                            center = Offset(e.x, e.y),
                            radius = e.radius
                        ),
                        radius = e.radius,
                        center = Offset(e.x, e.y)
                    )
                } else {
                    // Asteroid / Mine
                    drawCircle(
                        color = Color(0xFF64748B),
                        radius = e.radius,
                        center = Offset(e.x, e.y)
                    )
                    drawCircle(
                        color = Color(0xFFEF4444),
                        radius = e.radius * 0.4f,
                        center = Offset(e.x, e.y)
                    )
                }
            }

            // Draw Coins
            coins.forEach { c ->
                drawCircle(
                    color = GoldCurrency,
                    radius = c.radius,
                    center = Offset(c.x, c.y)
                )
                drawCircle(
                    color = Color(0xFFFDE68A),
                    radius = c.radius * 0.6f,
                    center = Offset(c.x, c.y)
                )
            }

            // Draw Particles
            particles.forEach { p ->
                drawCircle(
                    color = p.color.copy(alpha = p.life.coerceIn(0f, 1f)),
                    radius = 4f * p.life,
                    center = Offset(p.x, p.y)
                )
            }

            // Draw Player Ship (Stylized Neon Jet Fighter)
            drawPlayerShip(playerX, playerY)
        }

        // Top HUD Overlay
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .statusBarsPadding()
                .padding(horizontal = 16.dp, vertical = 8.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Column {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    val modeColor = when (game.playMode) {
                        PlayMode.OFFLINE -> EmeraldSuccess
                        PlayMode.ONLINE -> CyberCyanPrimary
                        PlayMode.BOTH -> Color(0xFFC084FC)
                    }
                    val modeText = when (game.playMode) {
                        PlayMode.OFFLINE -> "OFFLINE ARCADE"
                        PlayMode.ONLINE -> "ONLINE MULTIPLAYER"
                        PlayMode.BOTH -> "OFFLINE READY"
                    }
                    Box(
                        modifier = Modifier
                            .clip(RoundedCornerShape(4.dp))
                            .background(modeColor.copy(alpha = 0.2f))
                            .padding(horizontal = 5.dp, vertical = 2.dp)
                    ) {
                        Text(
                            text = modeText,
                            color = modeColor,
                            fontSize = 8.sp,
                            fontWeight = FontWeight.Bold
                        )
                    }
                    Spacer(modifier = Modifier.width(6.dp))
                    Text(
                        text = "SCORE: $score",
                        color = CyberCyanPrimary,
                        fontSize = 17.sp,
                        fontWeight = FontWeight.ExtraBold
                    )
                }
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(
                        imageVector = Icons.Default.MonetizationOn,
                        contentDescription = "Coins",
                        tint = GoldCurrency,
                        modifier = Modifier.size(16.dp)
                    )
                    Spacer(modifier = Modifier.width(4.dp))
                    Text(
                        text = "+$coinsCollected ($${String.format("%.2f", coinsCollected * 0.10)})",
                        color = GoldCurrency,
                        fontSize = 13.sp,
                        fontWeight = FontWeight.Bold
                    )
                }
            }

            // Shield Bar
            Column(
                horizontalAlignment = Alignment.CenterHorizontally,
                modifier = Modifier.width(130.dp)
            ) {
                Text(
                    text = "SHIELD: ${shieldHealth.toInt()}%",
                    color = if (shieldHealth > 35f) EmeraldSuccess else Color(0xFFFF4D4D),
                    fontSize = 11.sp,
                    fontWeight = FontWeight.Bold
                )
                Spacer(modifier = Modifier.height(3.dp))
                LinearProgressIndicator(
                    progress = { (shieldHealth / 100f).coerceIn(0f, 1f) },
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(8.dp),
                    color = if (shieldHealth > 35f) EmeraldSuccess else Color(0xFFFF4D4D),
                    trackColor = CardSurfaceDark
                )
            }

            // Pause / Exit button
            Row {
                IconButton(
                    onClick = { isPaused = !isPaused },
                    modifier = Modifier.testTag("pause_game_button")
                ) {
                    Icon(
                        imageVector = if (isPaused) Icons.Default.PlayArrow else Icons.Default.Pause,
                        contentDescription = "Pause",
                        tint = TextPrimaryDark
                    )
                }
                IconButton(
                    onClick = onExitGame,
                    modifier = Modifier.testTag("exit_game_button")
                ) {
                    Icon(
                        imageVector = Icons.Default.Close,
                        contentDescription = "Exit",
                        tint = TextSecondaryDark
                    )
                }
            }
        }

        // Game Over Overlay
        if (isGameOver) {
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .background(Color.Black.copy(alpha = 0.85f)),
                contentAlignment = Alignment.Center
            ) {
                Card(
                    modifier = Modifier
                        .fillMaxWidth(0.85f)
                        .padding(16.dp),
                    shape = RoundedCornerShape(20.dp),
                    colors = CardDefaults.cardColors(containerColor = CardSurfaceDark),
                    border = androidx.compose.foundation.BorderStroke(1.5.dp, CyberCyanPrimary)
                ) {
                    Column(
                        modifier = Modifier.padding(20.dp),
                        horizontalAlignment = Alignment.CenterHorizontally
                    ) {
                        Text(
                            text = "MISSION TERMINATED",
                            color = NeonPinkAccent,
                            fontSize = 20.sp,
                            fontWeight = FontWeight.ExtraBold,
                            letterSpacing = 1.sp
                        )

                        Spacer(modifier = Modifier.height(10.dp))

                        Text(
                            text = "Final Score: $score",
                            color = TextPrimaryDark,
                            fontSize = 22.sp,
                            fontWeight = FontWeight.Bold
                        )

                        val earnedCash = coinsCollected * 0.10
                        if (earnedCash > 0) {
                            Spacer(modifier = Modifier.height(6.dp))
                            Text(
                                text = "Earned +$${String.format("%.2f", earnedCash)} in Gold Coins!",
                                color = GoldCurrency,
                                fontSize = 14.sp,
                                fontWeight = FontWeight.Bold
                            )
                        }

                        Spacer(modifier = Modifier.height(20.dp))

                        Button(
                            onClick = { restartGame() },
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(46.dp)
                                .testTag("play_again_button"),
                            colors = ButtonDefaults.buttonColors(containerColor = CyberCyanPrimary)
                        ) {
                            Icon(Icons.Default.Refresh, contentDescription = "Retry", tint = DeepNavySurface)
                            Spacer(modifier = Modifier.width(6.dp))
                            Text("Play Again", color = DeepNavySurface, fontWeight = FontWeight.Bold)
                        }

                        Spacer(modifier = Modifier.height(10.dp))

                        Button(
                            onClick = {
                                if (earnedCash > 0) {
                                    onRewardEarned(earnedCash, game.title)
                                }
                                onExitGame()
                            },
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(46.dp)
                                .testTag("claim_rewards_exit_button"),
                            colors = ButtonDefaults.buttonColors(containerColor = GoldCurrency)
                        ) {
                            Text("Claim Cash & Exit", color = DeepNavySurface, fontWeight = FontWeight.Bold)
                        }
                    }
                }
            }
        }
    }
}

private fun DrawScope.drawPlayerShip(x: Float, y: Float) {
    // Jet thruster glow
    drawCircle(
        color = CyberCyanPrimary.copy(alpha = 0.4f),
        radius = 24f,
        center = Offset(x, y + 24f)
    )

    // Ship Hull
    val shipPath = Path().apply {
        moveTo(x, y - 32f) // Nose
        lineTo(x + 28f, y + 20f) // Right wing
        lineTo(x + 14f, y + 14f)
        lineTo(x, y + 22f) // Engine center
        lineTo(x - 14f, y + 14f)
        lineTo(x - 28f, y + 20f) // Left wing
        close()
    }

    drawPath(
        path = shipPath,
        brush = Brush.verticalGradient(
            colors = listOf(CyberCyanPrimary, Color(0xFF6366F1), NeonPinkAccent),
            startY = y - 32f,
            endY = y + 24f
        )
    )

    // Cockpit
    drawCircle(
        color = Color.White,
        radius = 6f,
        center = Offset(x, y - 6f)
    )
}

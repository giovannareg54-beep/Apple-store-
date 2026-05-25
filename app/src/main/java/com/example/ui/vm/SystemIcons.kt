package com.example.ui.vm

import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.Icon
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp

@Composable
fun AdaptiveLauncherIcon(
    modifier: Modifier = Modifier,
    size: Dp = 56.dp,
    appId: String,
    contentDescription: String
) {
    Box(
        modifier = modifier
            .size(size)
            .shadow(4.dp, shape = RoundedCornerShape(14.dp))
            .clip(RoundedCornerShape(14.dp)),
        contentAlignment = Alignment.Center
    ) {
        when (appId) {
            "play_store" -> PlayStoreIconPure(size)
            "dialer" -> SystemCircleIcon(
                bgColors = listOf(Color(0xFF2ECC71), Color(0xFF27AE60)),
                icon = Icons.Filled.Phone,
                iconColor = Color.White,
                size = size
            )
            "messenger" -> SystemCircleIcon(
                bgColors = listOf(Color(0xFF3498DB), Color(0xFF2980B9)),
                icon = Icons.Filled.Send,
                iconColor = Color.White,
                size = size,
                iconRotation = -20f
            )
            "settings" -> SystemCircleIcon(
                bgColors = listOf(Color(0xFFBDC3C7), Color(0xFF7F8C8D)),
                icon = Icons.Filled.Settings,
                iconColor = Color(0xFF2C3E50),
                size = size
            )
            "terminal" -> SystemTerminalIconPure(size)
            "gallery" -> SystemGalleryIconPure(size)
            "camera" -> SystemCircleIcon(
                bgColors = listOf(Color(0xFF34495E), Color(0xFF2C3E50)),
                icon = Icons.Filled.PhotoCamera,
                iconColor = Color(0xFFE74C3C),
                size = size
            )
            "calculator" -> SystemCircleIcon(
                bgColors = listOf(Color(0xFFE67E22), Color(0xFFD35400)),
                icon = Icons.Filled.Calculate,
                iconColor = Color.White,
                size = size
            )
            else -> SystemCircleIcon(
                bgColors = listOf(Color(0xFF9B59B6), Color(0xFF8E44AD)),
                icon = Icons.Filled.Android,
                iconColor = Color.White,
                size = size
            )
        }
    }
}

@Composable
fun SystemCircleIcon(
    bgColors: List<Color>,
    icon: ImageVector,
    iconColor: Color,
    size: Dp,
    iconRotation: Float = 0f
) {
    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(
                Brush.verticalGradient(bgColors)
            ),
        contentAlignment = Alignment.Center
    ) {
        val iconSize = size * 0.55f
        Icon(
            imageVector = icon,
            contentDescription = null,
            tint = iconColor,
            modifier = Modifier
                .size(iconSize)
                .rotateIf(iconRotation)
        )
    }
}

private fun Modifier.rotateIf(deg: Float): Modifier {
    return if (deg != 0f) {
        this.graphicsLayer { rotationZ = deg }
    } else this
}

@Composable
fun PlayStoreIconPure(size: Dp) {
    // Beautiful exact triangular play emblem
    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(Color.White),
        contentAlignment = Alignment.Center
    ) {
        Canvas(modifier = Modifier.size(size * 0.6f)) {
            val width = this.size.width
            val height = this.size.height

            // We make 4 distinct connecting paths for Google Play colors
            // 1. Blue sub-triangle (top left quadrant)
            val pathBlue = Path().apply {
                moveTo(0f, 0f)
                lineTo(width * 0.55f, height * 0.5f)
                lineTo(0f, height * 0.5f)
                close()
            }
            drawPath(pathBlue, color = Color(0xFF00C6FF))

            // 2. Green sub-triangle (bottom left quadrant)
            val pathGreen = Path().apply {
                moveTo(0f, height * 0.5f)
                lineTo(width * 0.55f, height * 0.5f)
                lineTo(0f, height)
                close()
            }
            drawPath(pathGreen, color = Color(0xFF00E676))

            // 3. Red sub-triangle (top right/middle)
            val pathRed = Path().apply {
                moveTo(0f, 0f)
                lineTo(width, height * 0.5f)
                lineTo(width * 0.55f, height * 0.5f)
                close()
            }
            drawPath(pathRed, color = Color(0xFFFF1744))

            // 4. Yellow sub-triangle (bottom right/middle)
            val pathYellow = Path().apply {
                moveTo(width * 0.55f, height * 0.5f)
                lineTo(width, height * 0.5f)
                lineTo(0f, height)
                close()
            }
            drawPath(pathYellow, color = Color(0xFFFFEA00))
        }
    }
}

@Composable
fun SystemTerminalIconPure(size: Dp) {
    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(Color(0xFF1E1E1E)),
        contentAlignment = Alignment.Center
    ) {
        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center,
            modifier = Modifier.fillMaxSize()
        ) {
            Canvas(modifier = Modifier.size(size * 0.5f)) {
                val w = this.size.width
                val h = this.size.height

                // Draw root command ">_"
                val path = Path().apply {
                    moveTo(0f, h * 0.15f)
                    lineTo(w * 0.45f, h * 0.5f)
                    lineTo(0f, h * 0.85f)
                    lineTo(w * 0.15f, h * 0.85f)
                    lineTo(w * 0.6f, h * 0.5f)
                    lineTo(w * 0.15f, h * 0.15f)
                    close()
                }
                drawPath(path, color = Color(0xFF39FF14)) // terminal green neon

                // Cursor Line
                drawRect(
                    color = Color(0xFF39FF14),
                    topLeft = Offset(w * 0.7f, h * 0.75f),
                    size = androidx.compose.ui.geometry.Size(w * 0.3f, h * 0.12f)
                )
            }
        }
    }
}

@Composable
fun SystemGalleryIconPure(size: Dp) {
    // Beautiful Google Photos design
    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(Color.White),
        contentAlignment = Alignment.Center
    ) {
        Canvas(modifier = Modifier.size(size * 0.65f)) {
            val w = this.size.width
            val h = this.size.height
            val r = w * 0.25f // radius of half circles

            // Let's draw 4 stylized overlay colorful leaves
            // Top Left (Yellow)
            drawArc(
                color = Color(0xFFFFC107),
                startAngle = 180f,
                sweepAngle = 180f,
                useCenter = true,
                topLeft = Offset(0f, h * 0.18f),
                size = androidx.compose.ui.geometry.Size(r * 2, r * 2)
            )
            // Top Right (Red)
            drawArc(
                color = Color(0xFFE91E63),
                startAngle = 270f,
                sweepAngle = 180f,
                useCenter = true,
                topLeft = Offset(w * 0.3f, 0f),
                size = androidx.compose.ui.geometry.Size(r * 2, r * 2)
            )
            // Bottom Right (Blue)
            drawArc(
                color = Color(0xFF2196F3),
                startAngle = 0f,
                sweepAngle = 180f,
                useCenter = true,
                topLeft = Offset(w * 0.4f, h * 0.3f),
                size = androidx.compose.ui.geometry.Size(r * 2, r * 2)
            )
            // Bottom Left (Green)
            drawArc(
                color = Color(0xFF4CAF50),
                startAngle = 90f,
                sweepAngle = 180f,
                useCenter = true,
                topLeft = Offset(h * 0.1f, h * 0.4f),
                size = androidx.compose.ui.geometry.Size(r * 2, r * 2)
            )
        }
    }
}

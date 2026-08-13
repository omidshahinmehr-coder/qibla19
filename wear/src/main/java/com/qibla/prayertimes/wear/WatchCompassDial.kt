package com.qibla.prayertimes.wear

import androidx.compose.animation.animateColorAsState
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.tween
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.size
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.drawscope.rotate
import kotlin.math.cos
import kotlin.math.sin

// A small, self-contained palette for the watch face — mirrors the phone app's amber/brass/
// night theme closely enough to feel like the same app, without pulling in the phone module's
// full theme system.
val WatchNightDeep = Color(0xFF0B0E14)
val WatchNightMid = Color(0xFF141922)
val WatchBrass = Color(0xFFB08D57)
val WatchBrassLight = Color(0xFFE0C687)
val WatchBrassDark = Color(0xFF6E5324)
val WatchAmberText = Color(0xFFF3E3C2)
val WatchAmberMuted = Color(0xFFB8A788)
private val WatchAlignedGreen = Color(0xFF4CD964)

/**
 * A simplified qibla compass needle for the watch — same idea as the phone's CompassDial
 * (rotating tick ring + a needle pointing at [bearingDegrees], relative to the live device
 * heading via [dialRotationDegrees]), just fewer details so it stays legible on a small,
 * often-round screen.
 */
@Composable
fun WatchCompassDial(
    bearingDegrees: Float,
    dialRotationDegrees: Float,
    isAligned: Boolean,
    dialSize: androidx.compose.ui.unit.Dp,
    modifier: Modifier = Modifier
) {
    val animatedBearing by animateFloatAsState(
        targetValue = bearingDegrees,
        animationSpec = tween(400),
        label = "bearing"
    )
    val animatedDialRotation by animateFloatAsState(
        targetValue = dialRotationDegrees,
        animationSpec = tween(400),
        label = "dialRotation"
    )
    val needleColor by animateColorAsState(
        targetValue = if (isAligned) WatchAlignedGreen else WatchBrassLight,
        label = "needleColor"
    )

    Box(modifier = modifier.size(dialSize), contentAlignment = Alignment.Center) {
        Canvas(modifier = Modifier.size(dialSize)) {
            val radius = size.minDimension / 2
            val center = Offset(size.width / 2, size.height / 2)

            drawCircle(
                brush = Brush.sweepGradient(listOf(WatchBrass, WatchBrassLight, WatchBrass, WatchBrassDark, WatchBrass)),
                radius = radius,
                center = center
            )
            drawCircle(
                brush = Brush.radialGradient(listOf(WatchNightDeep, WatchNightMid), center = center, radius = radius),
                radius = radius * 0.94f,
                center = center
            )

            rotate(degrees = animatedDialRotation, pivot = center) {
                for (i in 0 until 12) {
                    val deg = i * 30
                    val major = deg % 90 == 0
                    val angleRad = Math.toRadians(deg.toDouble() - 90.0)
                    val outer = radius * 0.92f
                    val inner = if (major) outer - radius * 0.12f else outer - radius * 0.06f
                    val startX = center.x + (inner * cos(angleRad)).toFloat()
                    val startY = center.y + (inner * sin(angleRad)).toFloat()
                    val endX = center.x + (outer * cos(angleRad)).toFloat()
                    val endY = center.y + (outer * sin(angleRad)).toFloat()
                    drawLine(
                        color = if (major) WatchBrassLight else WatchBrass.copy(alpha = 0.45f),
                        start = Offset(startX, startY),
                        end = Offset(endX, endY),
                        strokeWidth = if (major) radius * 0.035f else radius * 0.02f
                    )
                }
            }

            rotate(degrees = animatedBearing, pivot = center) {
                val tipY = center.y - (radius * 0.78f)
                val arrowHalf = radius * 0.1f
                drawLine(
                    color = needleColor,
                    start = center,
                    end = Offset(center.x, tipY),
                    strokeWidth = radius * 0.05f
                )
                val arrowPath = Path().apply {
                    moveTo(center.x, tipY - radius * 0.16f)
                    lineTo(center.x - arrowHalf, tipY + radius * 0.07f)
                    lineTo(center.x + arrowHalf, tipY + radius * 0.07f)
                    close()
                }
                drawPath(arrowPath, color = needleColor)
                drawLine(
                    color = Color(0xFF7A8BA0).copy(alpha = 0.5f),
                    start = center,
                    end = Offset(center.x, center.y + radius * 0.6f),
                    strokeWidth = radius * 0.03f
                )
            }
        }
    }
}

package com.qibla.prayertimes.ui

import androidx.compose.animation.animateColorAsState
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.tween
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.size
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableFloatStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.drawscope.rotate as rotateCanvas
import androidx.compose.ui.text.font.Font
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.qibla.prayertimes.R
import com.qibla.prayertimes.ui.theme.AmberMuted
import com.qibla.prayertimes.ui.theme.AmberText
import com.qibla.prayertimes.ui.theme.Brass
import com.qibla.prayertimes.ui.theme.BrassDark
import com.qibla.prayertimes.ui.theme.BrassLight
import com.qibla.prayertimes.ui.theme.NightDeep
import com.qibla.prayertimes.ui.theme.NightMid
import kotlin.math.cos
import kotlin.math.sin

private val logoFontFamily = FontFamily(Font(R.font.logo_serif_bold))
private val AlignedGreen = Color(0xFF4CD964)

@Composable
private fun directionLabels(): List<Pair<String, Float>> = listOf(
    stringResource(R.string.dir_n) to 0f,
    stringResource(R.string.dir_ne) to 45f,
    stringResource(R.string.dir_e) to 90f,
    stringResource(R.string.dir_se) to 135f,
    stringResource(R.string.dir_s) to 180f,
    stringResource(R.string.dir_sw) to 225f,
    stringResource(R.string.dir_w) to 270f,
    stringResource(R.string.dir_nw) to 315f
)

/** Tracks an unwrapped (non-0..360-clamped) angle so animations always take the shortest path —
 *  without this, crossing the 0°/360° boundary would spin almost a full turn instead of a few
 *  degrees, which is very noticeable with live sensor input. */
@Composable
private fun rememberContinuousAngle(target: Float): Float {
    var continuousTarget by remember { mutableFloatStateOf(target) }
    androidx.compose.runtime.LaunchedEffect(target) {
        var delta = (target - continuousTarget) % 360f
        if (delta > 180f) delta -= 360f
        if (delta < -180f) delta += 360f
        continuousTarget += delta
    }
    return continuousTarget
}

@Composable
fun CompassDial(
    bearingDegrees: Float,
    modifier: Modifier = Modifier,
    dialSize: androidx.compose.ui.unit.Dp = 260.dp,
    animationMillis: Int = 700,
    centerLabel: String? = null,
    captionText: String = stringResource(R.string.compass_static_caption),
    /** How much to rotate the tick marks and N/E/S/W labels — pass the *negative* of the live
     *  device heading so the dial face stays aligned with true compass directions as the phone
     *  turns (0 keeps it fixed north-up, for when there's no sensor). */
    dialRotationDegrees: Float = 0f,
    /** True once the phone is currently facing the qibla direction — the needle changes color
     *  to make that moment obvious. */
    isAligned: Boolean = false
) {
    val animatedBearing by animateFloatAsState(
        targetValue = rememberContinuousAngle(bearingDegrees),
        animationSpec = tween(durationMillis = animationMillis),
        label = "bearing"
    )
    val animatedDialRotation by animateFloatAsState(
        targetValue = rememberContinuousAngle(dialRotationDegrees),
        animationSpec = tween(durationMillis = animationMillis),
        label = "dialRotation"
    )
    val needleColor by animateColorAsState(
        targetValue = if (isAligned) AlignedGreen else BrassLight,
        label = "needleColor"
    )
    val labelRadiusDp = dialSize.value * 0.37f

    Box(modifier = modifier.size(dialSize), contentAlignment = Alignment.Center) {
        Canvas(modifier = Modifier.size(dialSize)) {
            val radius = size.minDimension / 2
            val center = Offset(size.width / 2, size.height / 2)

            // Brass outer ring
            drawCircle(
                brush = Brush.sweepGradient(
                    listOf(Brass, BrassLight, Brass, BrassDark, Brass)
                ),
                radius = radius,
                center = center
            )
            // Inner dial face
            drawCircle(
                brush = Brush.radialGradient(
                    listOf(NightDeep, NightMid),
                    center = center,
                    radius = radius
                ),
                radius = radius * 0.97f,
                center = center
            )

            // Tick marks every 5 degrees, major every 30 — rotated with the live heading so
            // they always represent true compass directions.
            rotateCanvas(degrees = animatedDialRotation, pivot = center) {
                for (i in 0 until 72) {
                    val deg = i * 5
                    val major = deg % 30 == 0
                    val angleRad = Math.toRadians(deg.toDouble() - 90.0)
                    val outer = radius * 0.945f
                    val inner = if (major) outer - radius * 0.09f else outer - radius * 0.045f
                    val startX = center.x + (inner * cos(angleRad)).toFloat()
                    val startY = center.y + (inner * sin(angleRad)).toFloat()
                    val endX = center.x + (outer * cos(angleRad)).toFloat()
                    val endY = center.y + (outer * sin(angleRad)).toFloat()
                    drawLine(
                        color = if (major) BrassLight else Brass.copy(alpha = 0.4f),
                        start = Offset(startX, startY),
                        end = Offset(endX, endY),
                        strokeWidth = if (major) radius * 0.02f else radius * 0.01f
                    )
                }
            }

            // Qibla needle, rotated to bearing (0 = up/forward). Turns green when the phone is
            // currently pointed at the qibla.
            rotateCanvas(degrees = animatedBearing, pivot = center) {
                val tipY = center.y - (radius * 0.845f)
                val arrowHalf = radius * 0.09f
                drawLine(
                    color = needleColor,
                    start = center,
                    end = Offset(center.x, tipY),
                    strokeWidth = radius * 0.035f
                )
                val arrowPath = androidx.compose.ui.graphics.Path().apply {
                    moveTo(center.x, tipY - radius * 0.135f)
                    lineTo(center.x - arrowHalf, tipY + radius * 0.06f)
                    lineTo(center.x + arrowHalf, tipY + radius * 0.06f)
                    close()
                }
                drawPath(arrowPath, color = needleColor)
                drawLine(
                    color = Color(0xFF7A8BA0).copy(alpha = 0.5f),
                    start = center,
                    end = Offset(center.x, center.y + radius * 0.69f),
                    strokeWidth = radius * 0.02f
                )
            }
        }

        // Direction labels — rotated together with the tick marks (same live-heading offset),
        // but each label's own text stays upright and readable rather than spinning in place.
        directionLabels().forEach { (label, deg) ->
            val angleRad = Math.toRadians(deg.toDouble() - 90.0 + animatedDialRotation)
            val x = (labelRadiusDp * cos(angleRad)).toFloat().dp
            val y = (labelRadiusDp * sin(angleRad)).toFloat().dp
            Text(
                text = label,
                color = AmberMuted,
                fontSize = 11.sp,
                fontWeight = FontWeight.Bold,
                modifier = Modifier.offset(x = x, y = y)
            )
        }

        // Center readout
        Box(contentAlignment = Alignment.Center) {
            androidx.compose.foundation.layout.Column(horizontalAlignment = Alignment.CenterHorizontally) {
                Text(
                    text = "LBo",
                    color = BrassLight,
                    fontSize = (dialSize.value * 0.09f).sp,
                    fontWeight = FontWeight.Bold,
                    fontFamily = logoFontFamily
                )
                Text(
                    text = centerLabel ?: "${"%.1f".format(bearingDegrees)}°",
                    color = AmberText,
                    fontSize = 22.sp,
                    fontWeight = FontWeight.ExtraBold
                )
                Text(
                    text = captionText,
                    color = AmberMuted,
                    fontSize = 10.sp,
                    textAlign = TextAlign.Center
                )
            }
        }
    }
}

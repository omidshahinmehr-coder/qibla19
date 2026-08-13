package com.qibla.prayertimes.ui

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Remove
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.qibla.prayertimes.R
import com.qibla.prayertimes.data.PrayerTimesState
import com.qibla.prayertimes.ui.theme.*
import com.qibla.prayertimes.util.HijriCalendar
import com.qibla.prayertimes.util.HijriCorrectionPrefs
import com.qibla.prayertimes.viewmodel.QiblaViewModel

@Composable
fun HijriCorrectionScreen(viewModel: QiblaViewModel, onBack: () -> Unit) {
    val context = LocalContext.current
    val correctionDays by viewModel.hijriCorrectionDays.collectAsState()
    val prayerState by viewModel.prayerState.collectAsState()
    val eraSuffix = stringResource(R.string.hijri_era_suffix)

    // Same Hijri date the home screen and widgets show — from the online source when the app
    // is online, or the offline calculation when it isn't (see PrayerTimesRepository) — so this
    // screen is never out of sync with what the rest of the app is displaying.
    val hijriFromApp = (prayerState as? PrayerTimesState.Success)?.result?.hijri

    val correctedText = hijriFromApp?.let { "${it.day} ${it.monthName(context)} ${it.year}$eraSuffix" }
        ?: run {
            // Not loaded yet (e.g. screen opened before the first fetch completes) — show a
            // quick local estimate so the screen isn't blank.
            val h = HijriCalendar.today(correctionDays)
            "${h.day} ${h.monthName(context)} ${h.year}$eraSuffix"
        }

    // The "without correction" reference line: un-shift whichever date is actually being shown
    // (online or offline) by the currently-applied correction, rather than recomputing from
    // scratch — so it always matches the same source as [correctedText] above.
    val rawText = hijriFromApp?.let { h ->
        val year = h.year.toIntOrNull()
        val day = h.day.toIntOrNull()
        if (year != null && day != null && h.monthNumber in 1..12) {
            val unshifted = HijriCalendar.shift(year, h.monthNumber, day, -correctionDays)
            "${unshifted.day} ${unshifted.monthName(context)} ${unshifted.year}$eraSuffix"
        } else null
    } ?: run {
        val h = HijriCalendar.today(0)
        "${h.day} ${h.monthName(context)} ${h.year}$eraSuffix"
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(NightMid)
            .padding(horizontal = 20.dp)
            .padding(top = 28.dp, bottom = 24.dp)
    ) {
        Row(verticalAlignment = Alignment.CenterVertically) {
            IconButton(onClick = onBack) {
                Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = stringResource(R.string.back), tint = AmberText)
            }
            Spacer(Modifier.width(4.dp))
            Text(stringResource(R.string.hijri_correction_title), color = AmberText, fontWeight = FontWeight.Bold, fontSize = 19.sp)
        }

        Spacer(Modifier.height(12.dp))

        Text(
            text = stringResource(R.string.hijri_correction_explanation),
            color = AmberMuted,
            fontSize = 13.sp,
            lineHeight = 19.sp
        )

        Spacer(Modifier.height(24.dp))

        // Preview card: today's Hijri date with the correction applied.
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .clip(RoundedCornerShape(14.dp))
                .background(CardSurface)
                .border(1.dp, CardBorder, RoundedCornerShape(14.dp))
                .padding(horizontal = 14.dp, vertical = 14.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Text(stringResource(R.string.hijri_correction_preview_label), color = AmberMuted, fontSize = 12.sp)
            Spacer(Modifier.height(4.dp))
            Text(correctedText, color = AmberText, fontWeight = FontWeight.Bold, fontSize = 18.sp)
            if (hijriFromApp != null && (prayerState as? PrayerTimesState.Success)?.result?.isOffline == true) {
                Spacer(Modifier.height(4.dp))
                Text(stringResource(R.string.hijri_correction_offline_note), color = AmberFaint, fontSize = 11.sp, textAlign = TextAlign.Center)
            }
            if (correctionDays != 0) {
                Spacer(Modifier.height(4.dp))
                Text(
                    text = stringResource(R.string.hijri_correction_uncorrected, rawText),
                    color = AmberFaint,
                    fontSize = 12.sp
                )
            }
        }

        Spacer(Modifier.height(24.dp))

        // Stepper: −N days ... 0 ... +N days
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.Center,
            verticalAlignment = Alignment.CenterVertically
        ) {
            StepperButton(
                icon = Icons.Filled.Remove,
                contentDescription = stringResource(R.string.hijri_correction_decrease),
                enabled = correctionDays > HijriCorrectionPrefs.MIN_OFFSET,
                onClick = { viewModel.setHijriCorrection(correctionDays - 1) }
            )

            Spacer(Modifier.width(20.dp))

            Text(
                text = if (correctionDays > 0) "+$correctionDays" else "$correctionDays",
                color = AmberText,
                fontWeight = FontWeight.Bold,
                fontSize = 26.sp,
                textAlign = TextAlign.Center,
                modifier = Modifier.widthIn(min = 56.dp)
            )

            Spacer(Modifier.width(20.dp))

            StepperButton(
                icon = Icons.Filled.Add,
                contentDescription = stringResource(R.string.hijri_correction_increase),
                enabled = correctionDays < HijriCorrectionPrefs.MAX_OFFSET,
                onClick = { viewModel.setHijriCorrection(correctionDays + 1) }
            )
        }

        Spacer(Modifier.height(8.dp))

        Text(
            text = stringResource(R.string.hijri_correction_days_label, correctionDays),
            color = AmberMuted,
            fontSize = 13.sp,
            textAlign = TextAlign.Center,
            modifier = Modifier.fillMaxWidth()
        )

        if (correctionDays != 0) {
            Spacer(Modifier.height(16.dp))
            Text(
                text = stringResource(R.string.hijri_correction_reset),
                color = BrassLight,
                fontSize = 13.sp,
                fontWeight = FontWeight.Medium,
                textAlign = TextAlign.Center,
                modifier = Modifier
                    .fillMaxWidth()
                    .clip(RoundedCornerShape(10.dp))
                    .clickable { viewModel.setHijriCorrection(0) }
                    .padding(vertical = 10.dp)
            )
        }
    }
}

@Composable
private fun StepperButton(
    icon: androidx.compose.ui.graphics.vector.ImageVector,
    contentDescription: String,
    enabled: Boolean,
    onClick: () -> Unit
) {
    Icon(
        icon,
        contentDescription = contentDescription,
        tint = if (enabled) AmberText else AmberFaint,
        modifier = Modifier
            .size(44.dp)
            .clip(CircleShape)
            .background(CardSurface)
            .border(1.dp, CardBorder, CircleShape)
            .clickable(enabled = enabled, onClick = onClick)
            .padding(10.dp)
    )
}

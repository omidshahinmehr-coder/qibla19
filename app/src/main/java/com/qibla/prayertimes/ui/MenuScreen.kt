package com.qibla.prayertimes.ui

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.automirrored.filled.ArrowForwardIos
import androidx.compose.material.icons.filled.CalendarMonth
import androidx.compose.material.icons.filled.EditCalendar
import androidx.compose.material.icons.filled.Info
import androidx.compose.material.icons.filled.Language
import androidx.compose.material.icons.filled.LocationCity
import androidx.compose.material.icons.filled.NotificationsActive
import androidx.compose.material.icons.filled.Palette
import androidx.compose.material.icons.filled.Rule
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.qibla.prayertimes.R
import com.qibla.prayertimes.ui.theme.*

@Composable
fun MenuScreen(
    onBack: () -> Unit,
    onOpenAlarms: () -> Unit,
    onOpenMonthly: () -> Unit,
    onOpenCityPicker: () -> Unit,
    onOpenLanguage: () -> Unit,
    onOpenTheme: () -> Unit,
    onOpenHijriCorrection: () -> Unit,
    onOpenPrayerMethod: () -> Unit,
    onOpenAbout: () -> Unit
) {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(NightMid)
            .padding(horizontal = 20.dp)
            .padding(top = 28.dp, bottom = 24.dp)
            .verticalScroll(rememberScrollState())   // ← اسکرول برای کل منو
    ) {
        Row(verticalAlignment = Alignment.CenterVertically) {
            IconButton(onClick = onBack) {
                Icon(
                    Icons.AutoMirrored.Filled.ArrowBack,
                    contentDescription = stringResource(R.string.back),
                    tint = AmberText
                )
            }
            Spacer(Modifier.width(4.dp))
            Text(
                text = stringResource(R.string.menu_title),
                color = AmberText,
                fontWeight = FontWeight.Bold,
                fontSize = 19.sp
            )
        }

        Spacer(Modifier.height(20.dp))

        MenuRow(Icons.Filled.LocationCity, stringResource(R.string.city_picker_title), onOpenCityPicker)
        Spacer(Modifier.height(10.dp))
        MenuRow(Icons.Filled.NotificationsActive, stringResource(R.string.nav_alarms), onOpenAlarms)
        Spacer(Modifier.height(10.dp))
        MenuRow(Icons.Filled.CalendarMonth, stringResource(R.string.nav_monthly), onOpenMonthly)
        Spacer(Modifier.height(10.dp))
        MenuRow(Icons.Filled.Language, stringResource(R.string.menu_language), onOpenLanguage)
        Spacer(Modifier.height(10.dp))
        MenuRow(Icons.Filled.Palette, stringResource(R.string.menu_theme), onOpenTheme)
        Spacer(Modifier.height(10.dp))
        MenuRow(Icons.Filled.EditCalendar, stringResource(R.string.menu_hijri_correction), onOpenHijriCorrection)
        Spacer(Modifier.height(10.dp))
        MenuRow(Icons.Filled.Rule, stringResource(R.string.menu_prayer_method), onOpenPrayerMethod)
        Spacer(Modifier.height(10.dp))
        MenuRow(Icons.Filled.Info, stringResource(R.string.nav_about), onOpenAbout)
    }
}

@Composable
private fun MenuRow(icon: ImageVector, label: String, onClick: () -> Unit) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(16.dp))
            .background(CardSurface)
            .border(1.dp, CardBorder, RoundedCornerShape(16.dp))
            .clickable(onClick = onClick)
            .padding(horizontal = 16.dp, vertical = 16.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Icon(
            icon,
            contentDescription = null,
            tint = BrassLight,
            modifier = Modifier.size(20.dp)
        )
        Spacer(Modifier.width(14.dp))
        Text(
            text = label,
            color = AmberText,
            fontSize = 15.sp,
            fontWeight = FontWeight.Medium,
            modifier = Modifier.weight(1f)
        )
        Icon(
            Icons.AutoMirrored.Filled.ArrowForwardIos,
            contentDescription = null,
            tint = AmberFaint,
            modifier = Modifier.size(14.dp)
        )
    }
}

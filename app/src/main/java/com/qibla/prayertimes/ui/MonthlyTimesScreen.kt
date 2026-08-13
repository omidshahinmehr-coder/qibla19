package com.qibla.prayertimes.ui

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.ChevronLeft
import androidx.compose.material.icons.filled.ChevronRight
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.platform.LocalLayoutDirection
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.LayoutDirection
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.qibla.prayertimes.R
import com.qibla.prayertimes.data.CityStore
import com.qibla.prayertimes.data.DayPrayerTimes
import com.qibla.prayertimes.data.MonthPrayerTimes
import com.qibla.prayertimes.data.MonthlyPrayerTimesRepository
import com.qibla.prayertimes.data.PrayerMethodPrefs
import com.qibla.prayertimes.model.defaultCities
import com.qibla.prayertimes.ui.theme.*
import com.qibla.prayertimes.util.JalaliCalendar
import java.text.SimpleDateFormat
import java.util.Calendar

private val TABLE_COLUMNS = listOf("Fajr", "Sunrise", "Dhuhr", "Sunset", "Maghrib")
private enum class CalendarMode { GREGORIAN, JALALI }

@Composable
private fun columnLabels(): Map<String, String> = mapOf(
    "Fajr" to stringResource(R.string.col_fajr),
    "Sunrise" to stringResource(R.string.col_sunrise),
    "Dhuhr" to stringResource(R.string.col_dhuhr),
    "Sunset" to stringResource(R.string.col_sunset),
    "Maghrib" to stringResource(R.string.col_maghrib)
)

@Composable
fun MonthlyTimesScreen(onBack: () -> Unit) {
    val context = LocalContext.current
    val city = remember { CityStore(context).loadSelectedCity() ?: defaultCities(context).first() }
    val repo = remember { MonthlyPrayerTimesRepository() }
    // Read once per screen entry — same convention selected in the menu, kept in sync with the
    // home screen and the widgets.
    val method = remember { PrayerMethodPrefs.get(context) }
    val columnLabels = columnLabels()
    val isRtl = LocalLayoutDirection.current == LayoutDirection.Rtl

    var mode by remember { mutableStateOf(CalendarMode.GREGORIAN) }

    val nowCal = remember { Calendar.getInstance() }
    val todayJalali = remember { JalaliCalendar.today() }

    var gYear by remember { mutableIntStateOf(nowCal.get(Calendar.YEAR)) }
    var gMonth by remember { mutableIntStateOf(nowCal.get(Calendar.MONTH) + 1) }
    var jYear by remember { mutableIntStateOf(todayJalali.year) }
    var jMonth by remember { mutableIntStateOf(todayJalali.month) }

    val todayDay = when (mode) {
        CalendarMode.GREGORIAN ->
            if (gYear == nowCal.get(Calendar.YEAR) && gMonth == nowCal.get(Calendar.MONTH) + 1) nowCal.get(Calendar.DAY_OF_MONTH) else -1
        CalendarMode.JALALI ->
            if (jYear == todayJalali.year && jMonth == todayJalali.month) todayJalali.day else -1
    }

    var data by remember { mutableStateOf<MonthPrayerTimes?>(null) }
    var loading by remember { mutableStateOf(true) }

    LaunchedEffect(mode, gYear, gMonth, jYear, jMonth) {
        loading = true
        data = when (mode) {
            CalendarMode.GREGORIAN -> repo.fetchMonth(city.lat, city.lon, gYear, gMonth, method)
            CalendarMode.JALALI -> fetchJalaliMonth(repo, city.lat, city.lon, jYear, jMonth, method)
        }
        loading = false
    }

    val monthLabel = remember(mode, gYear, gMonth, jYear, jMonth) {
        when (mode) {
            CalendarMode.GREGORIAN -> {
                val cal = Calendar.getInstance().apply { set(gYear, gMonth - 1, 1) }
                SimpleDateFormat("MMMM yyyy", context.resources.configuration.locales[0]).format(cal.time)
            }
            CalendarMode.JALALI -> {
                val monthName = com.qibla.prayertimes.util.JalaliDate(jYear, jMonth, 1).monthName
                "$monthName $jYear"
            }
        }
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(NightMid)
            .padding(horizontal = 16.dp)
            .padding(top = 28.dp, bottom = 16.dp)
    ) {
        Row(verticalAlignment = Alignment.CenterVertically) {
            IconButton(onClick = onBack) {
                Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = stringResource(R.string.back), tint = AmberText)
            }
            Spacer(Modifier.width(4.dp))
            Column {
                Text(stringResource(R.string.monthly_title), color = AmberText, fontWeight = FontWeight.Bold, fontSize = 17.sp)
                Text(city.name, color = AmberMuted, fontSize = 11.sp)
            }
        }

        Spacer(Modifier.height(12.dp))

        // Gregorian / Jalali toggle
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .clip(RoundedCornerShape(12.dp))
                .background(OverlayMedium)
                .padding(3.dp)
        ) {
            listOf(CalendarMode.GREGORIAN to "Gregorian", CalendarMode.JALALI to "شمسی").forEach { (m, label) ->
                Box(
                    modifier = Modifier
                        .weight(1f)
                        .clip(RoundedCornerShape(10.dp))
                        .background(if (mode == m) Color(0x4DC9A15C) else Color.Transparent)
                        .clickable { mode = m }
                        .padding(vertical = 8.dp),
                    contentAlignment = Alignment.Center
                ) {
                    Text(
                        label,
                        color = if (mode == m) AmberText else AmberMuted,
                        fontSize = 12.sp,
                        fontWeight = if (mode == m) FontWeight.Bold else FontWeight.Normal
                    )
                }
            }
        }

        Spacer(Modifier.height(12.dp))

        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            IconButton(onClick = {
                when (mode) {
                    CalendarMode.GREGORIAN -> if (gMonth == 1) { gMonth = 12; gYear -= 1 } else gMonth -= 1
                    CalendarMode.JALALI -> if (jMonth == 1) { jMonth = 12; jYear -= 1 } else jMonth -= 1
                }
            }) {
                Icon(
                    if (isRtl) Icons.Filled.ChevronRight else Icons.Filled.ChevronLeft,
                    contentDescription = stringResource(R.string.prev_month),
                    tint = BrassLight
                )
            }
            Text(monthLabel, color = AmberText, fontWeight = FontWeight.Bold, fontSize = 15.sp)
            IconButton(onClick = {
                when (mode) {
                    CalendarMode.GREGORIAN -> if (gMonth == 12) { gMonth = 1; gYear += 1 } else gMonth += 1
                    CalendarMode.JALALI -> if (jMonth == 12) { jMonth = 1; jYear += 1 } else jMonth += 1
                }
            }) {
                Icon(
                    if (isRtl) Icons.Filled.ChevronLeft else Icons.Filled.ChevronRight,
                    contentDescription = stringResource(R.string.next_month),
                    tint = BrassLight
                )
            }
        }

        if (data?.isOffline == true) {
            Spacer(Modifier.height(4.dp))
            Text(
                stringResource(R.string.monthly_offline_note),
                color = Color(0xFFF0C9C9),
                fontSize = 11.sp
            )
        }

        Spacer(Modifier.height(12.dp))

        if (loading) {
            Box(modifier = Modifier.fillMaxWidth().padding(top = 40.dp), contentAlignment = Alignment.Center) {
                CircularProgressIndicator(color = Brass)
            }
        } else {
            val days = data?.days.orEmpty()
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .clip(RoundedCornerShape(16.dp))
                    .background(CardSurface)
            ) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .background(Color(0x1AC9A15C))
                        .padding(horizontal = 10.dp, vertical = 10.dp)
                ) {
                    Text(stringResource(R.string.col_day), color = AmberMuted, fontSize = 11.sp, fontWeight = FontWeight.Bold, modifier = Modifier.width(32.dp))
                    TABLE_COLUMNS.forEach { key ->
                        Text(
                            columnLabels[key] ?: key,
                            color = AmberMuted,
                            fontSize = 11.sp,
                            fontWeight = FontWeight.Bold,
                            modifier = Modifier.weight(1f),
                            textAlign = androidx.compose.ui.text.style.TextAlign.Center
                        )
                    }
                }
                LazyColumn(modifier = Modifier.heightIn(max = 480.dp)) {
                    items(days) { dayEntry ->
                        val isToday = dayEntry.day == todayDay
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .background(if (isToday) Color(0x26C9A15C) else Color.Transparent)
                                .padding(horizontal = 10.dp, vertical = 8.dp)
                        ) {
                            Text(
                                "${dayEntry.day}",
                                color = if (isToday) BrassLight else AmberText,
                                fontWeight = if (isToday) FontWeight.Bold else FontWeight.Normal,
                                fontSize = 12.sp,
                                modifier = Modifier.width(32.dp)
                            )
                            TABLE_COLUMNS.forEach { key ->
                                Text(
                                    dayEntry.timings[key] ?: "--:--",
                                    color = if (isToday) AmberText else AmberText.copy(alpha = 0.85f),
                                    fontSize = 12.sp,
                                    modifier = Modifier.weight(1f),
                                    textAlign = androidx.compose.ui.text.style.TextAlign.Center
                                )
                            }
                        }
                    }
                }
            }
        }
    }
}

/**
 * Builds a Jalali month's worth of prayer times by converting each Jalali day to its
 * Gregorian date and pulling it from whichever Gregorian month(s) that spans (usually one,
 * sometimes two near a month boundary) — at most two underlying fetches either way.
 */
private suspend fun fetchJalaliMonth(
    repo: MonthlyPrayerTimesRepository,
    lat: Double,
    lon: Double,
    jy: Int,
    jm: Int,
    method: com.qibla.prayertimes.data.PrayerCalculationMethod
): MonthPrayerTimes {
    val dayCount = JalaliCalendar.daysInMonth(jy, jm)
    val cache = mutableMapOf<Pair<Int, Int>, MonthPrayerTimes>()
    var anyOffline = false
    val days = (1..dayCount).map { jd ->
        val (gy, gm, gd) = JalaliCalendar.toGregorian(jy, jm, jd)
        val monthData = cache.getOrPut(gy to gm) { repo.fetchMonth(lat, lon, gy, gm, method) }
        if (monthData.isOffline) anyOffline = true
        val timings = monthData.days.firstOrNull { it.day == gd }?.timings ?: emptyMap()
        DayPrayerTimes(jd, timings)
    }
    return MonthPrayerTimes(jy, jm, days, anyOffline)
}

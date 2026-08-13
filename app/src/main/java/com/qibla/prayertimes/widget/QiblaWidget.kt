package com.qibla.prayertimes.widget

import android.content.Context
import android.os.Build
import android.os.SystemClock
import android.widget.RemoteViews
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.glance.Image
import androidx.glance.GlanceId
import androidx.glance.GlanceModifier
import androidx.glance.action.actionStartActivity
import androidx.glance.action.clickable
import androidx.glance.appwidget.AndroidRemoteViews
import androidx.glance.appwidget.GlanceAppWidget
import androidx.glance.appwidget.cornerRadius
import androidx.glance.appwidget.provideContent
import androidx.glance.background
import androidx.glance.layout.*
import androidx.glance.text.*
import androidx.glance.unit.ColorProvider
import com.qibla.prayertimes.MainActivity
import com.qibla.prayertimes.R
import com.qibla.prayertimes.data.WidgetDataStore
import com.qibla.prayertimes.data.WidgetSnapshot
import com.qibla.prayertimes.data.prayerLabels
import com.qibla.prayertimes.util.LocalePrefs
import java.text.SimpleDateFormat
import java.util.*

private val bgColor = ColorProvider(Color(0xFFF3ECDD))
private val cellBorderColor = ColorProvider(Color(0xFFD9C8A0))
private val cellFillColor = ColorProvider(Color(0xFFFBF6EA))
private val goldText = ColorProvider(Color(0xFF8A6A2E))
private val faintGoldText = ColorProvider(Color(0xFFAD8F55))

private val widgetPrayerKeys = listOf("Fajr", "Sunrise", "Dhuhr", "Sunset", "Maghrib", "Midnight")
private val cellWidth = 70.dp
private val cellHeight = 59.dp

private val WEEKDAYS_FA = arrayOf("یکشنبه", "دوشنبه", "سه‌شنبه", "چهارشنبه", "پنجشنبه", "جمعه", "شنبه")
private val WEEKDAYS_AR = arrayOf("الأحد", "الاثنين", "الثلاثاء", "الأربعاء", "الخميس", "الجمعة", "السبت")

class QiblaWidget : GlanceAppWidget() {
    override suspend fun provideGlance(context: Context, id: GlanceId) {
        val localizedContext = LocalePrefs.wrap(context)
        val snapshot = WidgetDataStore(context).load()
        provideContent {
            WidgetContent(localizedContext, snapshot)
        }
    }
}

@Composable
private fun WidgetContent(langContext: Context, snapshot: WidgetSnapshot?) {

    val config = langContext.resources.configuration
    val language =
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.N)
            config.locales[0].language
        else
            config.locale.language

    val labels = prayerLabels(langContext)

    Column(
        modifier = GlanceModifier
            .fillMaxSize()
            .background(bgColor)
            .cornerRadius(20.dp)
            .padding(12.dp)
            .clickable(actionStartActivity<MainActivity>()),
        horizontalAlignment = Alignment.Horizontal.CenterHorizontally
    ) {
        if (snapshot != null) {

            val countdown = nextPrayerCountdown(snapshot.timings)
            val weekdayName = weekdayName(language)
            val gregorianText = formatGregorian(langContext, snapshot.gregorianDateKey)
            val jalaliWithWeekday = listOf(weekdayName, snapshot.jalaliText).filter { it.isNotBlank() }.joinToString(" ")

            val clockBlock: @Composable () -> Unit = {
                AndroidRemoteViews(RemoteViews(langContext.packageName, R.layout.widget_clock))
            }

            val countdownLabelBlock: @Composable () -> Unit = {
                if (countdown != null) {
                    Text(
                        text = langContext.getString(
                            R.string.widget_countdown_label,
                            labels[countdown.first] ?: countdown.first
                        ),
                        style = TextStyle(
                            color = goldText,
                            fontSize = 16.sp,
                            fontWeight = FontWeight.Bold,
                            textAlign = TextAlign.Center
                        ),
                        maxLines = 1
                    )
                }
            }

            val jalaliBlock: @Composable () -> Unit = {
                Text(
                    text = jalaliWithWeekday,
                    style = TextStyle(
                        color = goldText,
                        fontSize = 16.sp,
                        fontWeight = FontWeight.Bold,
                        textAlign = TextAlign.Center
                    ),
                    maxLines = 1
                )
            }

            // ⭐ نسخهٔ سازگار: کلیک روی تایمر → برنامه باز می‌شود → ویجت رفرش می‌شود
            val timerBlock: @Composable () -> Unit = {
                if (countdown != null) {
                    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.N) {
                        val nowElapsed = SystemClock.elapsedRealtime()
                        val nowWall = System.currentTimeMillis()
                        val base = nowElapsed + (countdown.second - nowWall)
                        val rv = RemoteViews(langContext.packageName, R.layout.widget_countdown)
                        rv.setChronometer(R.id.widget_countdown_view, base, null, true)
                        AndroidRemoteViews(rv)
                    } else {
                        Text(
                            text = staticDuration(countdown.second),
                            style = TextStyle(
                                color = goldText,
                                fontSize = 16.sp,
                                fontWeight = FontWeight.Bold,
                                textAlign = TextAlign.Center
                            ),
                            modifier = GlanceModifier.clickable(
                                actionStartActivity<MainActivity>()   // ← تنها روش سازگار
                            )
                        )
                    }
                }
            }

            Row(
                modifier = GlanceModifier.fillMaxWidth(),
                horizontalAlignment = Alignment.Horizontal.CenterHorizontally,
                verticalAlignment = Alignment.Vertical.CenterVertically
            ) {
                Box(
                    modifier = GlanceModifier.defaultWeight(),
                    contentAlignment = Alignment.CenterStart
                ) {
                    clockBlock()
                }

                Box(
                    modifier = GlanceModifier.defaultWeight(),
                    contentAlignment = Alignment.CenterEnd
                ) {
                    Row(
                        verticalAlignment = Alignment.Vertical.CenterVertically
                    ) {
                        Text(
                            text = snapshot.cityName,
                            style = TextStyle(
                                color = goldText,
                                fontSize = 16.sp,
                                fontWeight = FontWeight.Bold,
                                textAlign = TextAlign.End
                            ),
                            maxLines = 1
                        )
                        Spacer(modifier = GlanceModifier.width(4.dp))
                        Image(
                            provider = androidx.glance.ImageProvider(R.drawable.ic_location_pin),
                            contentDescription = null,
                            modifier = GlanceModifier.width(12.dp).height(12.dp)
                        )
                    }
                }
            }

            Spacer(modifier = GlanceModifier.height(1.dp))

            Box(
                modifier = GlanceModifier.fillMaxWidth(),
                contentAlignment = Alignment.Center
            ) {
                jalaliBlock()
            }

            Spacer(modifier = GlanceModifier.height(1.dp))

            Row(
                modifier = GlanceModifier.fillMaxWidth(),
                horizontalAlignment = Alignment.Horizontal.CenterHorizontally
            ) {

                Text(
                    text = snapshot.hijriText,
                    style = TextStyle(
                        color = faintGoldText,
                        fontSize = 16.sp
                    ),
                    modifier = GlanceModifier.padding(4.dp)
                )

                Text(
                    text = "-",
                    style = TextStyle(
                        color = faintGoldText,
                        fontSize = 16.sp
                    ),
                    modifier = GlanceModifier.padding(horizontal = 4.dp)
                )

                Text(
                    text = gregorianText,
                    style = TextStyle(
                        color = faintGoldText,
                        fontSize = 16.sp
                    ),
                    modifier = GlanceModifier.padding(4.dp)
                )
            }

            Spacer(modifier = GlanceModifier.height(1.dp))

            Box(
                modifier = GlanceModifier.fillMaxWidth(),
                contentAlignment = Alignment.Center
            ) {
                Row(
                    verticalAlignment = Alignment.Vertical.CenterVertically
                ) {
                    timerBlock()
                    Spacer(modifier = GlanceModifier.width(6.dp))
                    countdownLabelBlock()
                }
            }

            Spacer(modifier = GlanceModifier.height(1.dp))

            Row(
                modifier = GlanceModifier.fillMaxWidth(),
                horizontalAlignment = Alignment.Horizontal.CenterHorizontally
            ) {
                widgetPrayerKeys.forEachIndexed { index, key ->
                    if (index > 0) Spacer(modifier = GlanceModifier.width(4.dp))
                    PrayerCell(label = labels[key] ?: key, time = snapshot.timings[key] ?: "--:--")
                }
            }

        } else {
            Text(
                text = langContext.getString(R.string.widget_updating),
                style = TextStyle(
                    color = goldText,
                    fontSize = 15.sp,
                    textAlign = TextAlign.Center
                )
            )
            Spacer(modifier = GlanceModifier.height(2.dp))
            Text(
                text = langContext.getString(R.string.widget_open_app_hint),
                style = TextStyle(
                    color = faintGoldText,
                    fontSize = 11.sp,
                    textAlign = TextAlign.Center
                )
            )
        }
    }
}

@Composable
private fun PrayerCell(label: String, time: String) {
    Column(
        modifier = GlanceModifier
            .width(cellWidth)
            .background(cellBorderColor)
            .cornerRadius(10.dp)
            .padding(0.8.dp)
    ) {
        Column(
            modifier = GlanceModifier
                .fillMaxWidth()
                .height(cellHeight)
                .background(cellFillColor)
                .cornerRadius(8.dp)
                .padding(horizontal = 2.dp, vertical = 4.dp),
            horizontalAlignment = Alignment.Horizontal.CenterHorizontally
        ) {
            Text(
                text = label,
                style = TextStyle(
                    color = goldText,
                    fontSize = 11.sp,
                    fontWeight = FontWeight.Bold,
                    textAlign = TextAlign.Center
                )
            )
            Spacer(modifier = GlanceModifier.height(0.dp))
            Text(
                text = time,
                style = TextStyle(
                    color = goldText,
                    fontSize = 15.sp,
                    fontWeight = FontWeight.Bold,
                    textAlign = TextAlign.Center
                )
            )
        }
    }
}

private fun weekdayName(language: String): String {
    val dow = Calendar.getInstance().get(Calendar.DAY_OF_WEEK)
    return when (language) {
        "fa" -> WEEKDAYS_FA[dow - 1]
        "ar" -> WEEKDAYS_AR[dow - 1]
        else -> SimpleDateFormat("EEEE", Locale.ENGLISH).format(Calendar.getInstance().time)
    }
}

private fun formatGregorian(context: Context, dateKey: String): String {
    return try {
        val parts = dateKey.split("-").map { it.toInt() }
        val cal = Calendar.getInstance().apply { set(parts[0], parts[1] - 1, parts[2]) }

        val config = context.resources.configuration
        val locale =
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.N)
                config.locales[0]
            else
                config.locale

        SimpleDateFormat("dd MMMM yyyy", locale).format(cal.time)
    } catch (e: Exception) {
        dateKey
    }
}
/*private fun formatGregorianSafe(dateKey: String): String {
    return try {
        val parts = dateKey.split("-").map { it.toInt() }
        val cal = Calendar.getInstance().apply {
            set(parts[0], parts[1] - 1, parts[2])
        }

        val day = parts[2]
        val year = parts[0]

        val monthFa = when (cal.get(Calendar.MONTH)) {
            0 -> "ژانویه"
            1 -> "فوریه"
            2 -> "مارس"
            3 -> "آوریل"
            4 -> "مه"
            5 -> "ژوئن"
            6 -> "ژوئیه"
            7 -> "اوت"
            8 -> "سپتامبر"
            9 -> "اکتبر"
            10 -> "نوامبر"
            11 -> "دسامبر"
            else -> ""
        }

        "$day $monthFa $year"

    } catch (e: Exception) {
        dateKey
    }
}*/

private fun staticDuration(targetMillis: Long): String {
    val diff = (targetMillis - System.currentTimeMillis()).coerceAtLeast(0) / 1000
    val h = diff / 3600
    val m = (diff % 3600) / 60
    val s = diff % 60
    return "%02d:%02d:%02d".format(h, m, s)
}

private fun nextPrayerCountdown(timings: Map<String, String>): Pair<String, Long>? {
    val order = listOf("Fajr", "Dhuhr", "Asr", "Maghrib", "Isha")
    val now = Calendar.getInstance()
    val sdf = SimpleDateFormat("HH:mm", Locale.US)

    fun toCalendar(hhmm: String): Calendar? {
        val parsed = try { sdf.parse(hhmm) } catch (e: Exception) { null } ?: return null
        val parsedCal = Calendar.getInstance().apply { time = parsed }
        return Calendar.getInstance().apply {
            set(Calendar.HOUR_OF_DAY, parsedCal.get(Calendar.HOUR_OF_DAY))
            set(Calendar.MINUTE, parsedCal.get(Calendar.MINUTE))
            set(Calendar.SECOND, 0)
            set(Calendar.MILLISECOND, 0)
        }
    }

    var bestKey: String? = null
    var bestCal: Calendar? = null
    for (key in order) {
        val timeStr = timings[key] ?: continue
        val cal = toCalendar(timeStr) ?: continue
        if (cal.after(now) && (bestCal == null || cal.before(bestCal))) {
            bestKey = key
            bestCal = cal
        }
    }
    if (bestKey == null) {
        val fajrStr = timings["Fajr"] ?: return null
        val cal = toCalendar(fajrStr) ?: return null
        cal.add(Calendar.DAY_OF_YEAR, 1)
        bestKey = "Fajr"
        bestCal = cal
    }
    val target = bestCal ?: return null
    return bestKey to target.timeInMillis
}

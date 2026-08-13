package com.qibla.prayertimes.widget

import android.content.Context
import android.os.Build
import android.widget.RemoteViews
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
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
import androidx.glance.Image
import androidx.glance.ImageProvider
import com.qibla.prayertimes.MainActivity
import com.qibla.prayertimes.R
import com.qibla.prayertimes.data.WidgetDataStore
import com.qibla.prayertimes.data.WidgetSnapshot
import com.qibla.prayertimes.data.prayerLabels
import com.qibla.prayertimes.util.LocalePrefs
import java.text.SimpleDateFormat
import java.util.Calendar
import java.util.Locale

private val bgColor = ColorProvider(Color(0xFFF3ECDD))
private val goldText = ColorProvider(Color(0xFF8A6A2E))
private val cellFillColor = ColorProvider(Color(0xFFFBF6EA))
private val cellBorderColor = ColorProvider(Color(0xFFD9C8A0))

private val cellWidth = 90.dp
private val cellHeight = 50.dp

class LightQiblaWidget : GlanceAppWidget() {
    override suspend fun provideGlance(context: Context, id: GlanceId) {

        val localizedContext = LocalePrefs.wrap(context)
        val snapshot = WidgetDataStore(context).load()

        provideContent {
            LightWidgetContent(localizedContext, snapshot)
        }
    }
}

@Composable
private fun LightWidgetContent(langContext: Context, snapshot: WidgetSnapshot?) {

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

        if (snapshot == null) {
            Text(
                text = labels["Updating"] ?: "Updating...",
                style = TextStyle(
                    color = goldText,
                    fontSize = 16.sp,
                    fontWeight = FontWeight.Bold,
                    textAlign = TextAlign.Center
                )
            )
        } else {

            // ⭐ ردیف اول: ساعت سمت چپ + نام شهر سمت راست
            Row(
                modifier = GlanceModifier.fillMaxWidth(),
                verticalAlignment = Alignment.Vertical.CenterVertically,
                horizontalAlignment = Alignment.Horizontal.CenterHorizontally
            ) {

                // ساعت
                Box(
                    modifier = GlanceModifier.defaultWeight(),
                    contentAlignment = Alignment.CenterStart
                ) {
                    AndroidRemoteViews(
                        RemoteViews(langContext.packageName, R.layout.widget_clock)
                    )
                }

                // نام شهر + آیکون مکان
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
                            provider = ImageProvider(R.drawable.ic_location_pin),
                            contentDescription = null,
                            modifier = GlanceModifier.width(12.dp).height(12.dp)
                        )
                    }
                }
            }

            Spacer(modifier = GlanceModifier.height(1.dp))

            // ⭐ تاریخ + نام روز
            val config = langContext.resources.configuration
            val language =
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.N)
                    config.locales[0].language
                else
                    config.locale.language

            val weekday = weekdayName(language)
            val jalaliFull = "$weekday ${snapshot.jalaliText}"

            Text(
                text = jalaliFull,
                style = TextStyle(
                    color = goldText,
                    fontSize = 18.sp,
                    fontWeight = FontWeight.Bold,
                    textAlign = TextAlign.Center
                ),
                maxLines = 1
            )

            Spacer(modifier = GlanceModifier.height(2.dp))

            // ⭐ ردیف اول اوقات شرعی (سه‌تایی)
            Row(
                modifier = GlanceModifier.fillMaxWidth(),
                horizontalAlignment = Alignment.Horizontal.CenterHorizontally
            ) {
                LightCell(labels["Fajr"] ?: "Fajr", snapshot.timings["Fajr"] ?: "--:--")
                Spacer(modifier = GlanceModifier.width(4.dp))
                LightCell(labels["Sunrise"] ?: "Sunrise", snapshot.timings["Sunrise"] ?: "--:--")
                Spacer(modifier = GlanceModifier.width(4.dp))
                LightCell(labels["Dhuhr"] ?: "Dhuhr", snapshot.timings["Dhuhr"] ?: "--:--")
            }

            Spacer(modifier = GlanceModifier.height(4.dp))

            // ⭐ ردیف دوم اوقات شرعی (سه‌تایی)
            Row(
                modifier = GlanceModifier.fillMaxWidth(),
                horizontalAlignment = Alignment.Horizontal.CenterHorizontally
            ) {
                LightCell(labels["Sunset"] ?: "Sunset", snapshot.timings["Sunset"] ?: "--:--")
                Spacer(modifier = GlanceModifier.width(4.dp))
                LightCell(labels["Maghrib"] ?: "Maghrib", snapshot.timings["Maghrib"] ?: "--:--")
                Spacer(modifier = GlanceModifier.width(4.dp))
                LightCell(labels["Midnight"] ?: "Midnight", snapshot.timings["Midnight"] ?: "--:--")
            }
        }
    }
}

@Composable
private fun LightCell(label: String, time: String) {
    Column(
        modifier = GlanceModifier
            .width(cellWidth)
            .background(cellBorderColor)
            .cornerRadius(10.dp)
            .padding(1.dp)
    ) {
        Column(
            modifier = GlanceModifier
                .fillMaxWidth()
                .height(cellHeight)
                .background(cellFillColor)
                .cornerRadius(8.dp)
                .padding(4.dp),
            horizontalAlignment = Alignment.Horizontal.CenterHorizontally
        ) {
            Text(
                text = label,
                style = TextStyle(
                    color = goldText,
                    fontSize = 11.sp,
                    fontWeight = FontWeight.Bold
                )
            )
            Spacer(modifier = GlanceModifier.height(0.dp))
            Text(
                text = time,
                style = TextStyle(
                    color = goldText,
                    fontSize = 15.sp,
                    fontWeight = FontWeight.Bold
                )
            )
        }
    }
}

// ⭐ تابع نام روز (بیرون از Composable)
private fun weekdayName(language: String): String {
    val dow = Calendar.getInstance().get(Calendar.DAY_OF_WEEK)
    return when (language) {
        "fa" -> arrayOf("یکشنبه","دوشنبه","سه‌شنبه","چهارشنبه","پنجشنبه","جمعه","شنبه")[dow - 1]
        "ar" -> arrayOf("الأحد","الاثنين","الثلاثاء","الأربعاء","الخميس","الجمعة","السبت")[dow - 1]
        else -> SimpleDateFormat("EEEE", Locale.ENGLISH).format(Calendar.getInstance().time)
    }
}

package com.qibla.prayertimes.util

import android.content.Context
import java.util.Calendar

data class HijriApprox(val year: Int, val month: Int, val day: Int) {
    companion object {
        // Shared by Persian and Arabic — both languages use the same Hijri month names.
        private val MONTH_NAMES_AR = listOf(
            "محرم", "صفر", "ربیع‌الاول", "ربیع‌الثانی", "جمادی‌الاول", "جمادی‌الثانی",
            "رجب", "شعبان", "رمضان", "شوال", "ذی‌القعده", "ذی‌الحجه"
        )
        private val MONTH_NAMES_EN = listOf(
            "Muharram", "Safar", "Rabi al-Awwal", "Rabi al-Thani", "Jumada al-Awwal", "Jumada al-Thani",
            "Rajab", "Shaban", "Ramadan", "Shawwal", "Dhu al-Qidah", "Dhu al-Hijjah"
        )
    }

    val monthNameAr: String get() = MONTH_NAMES_AR.getOrElse(month - 1) { "" }
    val monthNameEn: String get() = MONTH_NAMES_EN.getOrElse(month - 1) { "" }

    /** Month name in whichever language is currently active (see [LocalePrefs.currentLanguage]). */
    fun monthName(context: Context): String =
        if (LocalePrefs.currentLanguage(context) == "en") monthNameEn else monthNameAr
}

/**
 * Gregorian-to-Hijri conversion using the standard tabular (civil) Islamic calendar algorithm
 * — a fixed 30-year/11-leap-year arithmetic cycle, with no network call. Accurate to within
 * about a day of the officially announced date (verified against the well-known epoch
 * 30 Jul 2022 = 1 Muharram 1444 AH).
 *
 * Because the moon-sighting-based date actually announced by local authorities can differ from
 * this arithmetic result by a day or two — and that gap varies by country — every lookup also
 * applies the user's manual [HijriCorrectionPrefs] offset, so the displayed date can be tuned
 * to match what's officially observed where the user lives.
 */
object HijriCalendar {

    /** Today's Hijri date, shifted by [offsetDays] (typically the user's stored correction). */
    fun today(offsetDays: Int = 0): HijriApprox {
        val cal = Calendar.getInstance()
        if (offsetDays != 0) cal.add(Calendar.DAY_OF_MONTH, offsetDays)
        return fromGregorian(
            cal.get(Calendar.YEAR),
            cal.get(Calendar.MONTH) + 1,
            cal.get(Calendar.DAY_OF_MONTH)
        )
    }

    /** Today's Hijri date, applying the correction the user has stored in settings. Used as
     *  the offline fallback's source of truth (see [PrayerCalculationMethod] / repository) and
     *  as a quick local estimate before the actual (online or offline) result has loaded. */
    fun todayCorrected(context: Context): HijriApprox = today(HijriCorrectionPrefs.get(context))

    /**
     * Shifts an already-known Islamic date by [offsetDays] days. Used to apply the user's
     * manual correction directly on top of the *online* source's Hijri date (see
     * PrayerTimesRepository) — the Aladhan API's own `adjustment` query parameter turned out to
     * be unreliable in practice, so this app applies the shift itself instead, on whichever
     * date (online or offline) it would otherwise show. A correction of "+1" always means "one
     * day later than the unshifted result," online or offline alike.
     */
    fun shift(year: Int, month: Int, day: Int, offsetDays: Int): HijriApprox {
        if (offsetDays == 0) return HijriApprox(year, month, day)
        val jdn = islamicToJdn(year, month, day) + offsetDays
        val (y, m, d) = jdnToIslamic(jdn)
        return HijriApprox(y, m, d)
    }

    fun fromGregorian(gy: Int, gm: Int, gd: Int): HijriApprox {
        val jdn = gregorianToJdn(gy, gm, gd)
        val (y, m, d) = jdnToIslamic(jdn)
        return HijriApprox(y, m, d)
    }

    private fun gregorianToJdn(y: Int, m: Int, d: Int): Long {
        val a = (14 - m) / 12
        val y2 = y + 4800 - a
        val m2 = m + 12 * a - 3
        return (d + (153L * m2 + 2) / 5 + 365L * y2 + y2 / 4 - y2 / 100 + y2 / 400 - 32045).toLong()
    }

    /**
     * The standard tabular-Islamic-calendar-to-JDN formula (epoch 1948440), the exact inverse
     * of [jdnToIslamic] below — verified to round-trip against it using this file's own
     * reference point (30 Jul 2022 Gregorian = 1 Muharram 1444 AH = JDN 2459791).
     */
    private fun islamicToJdn(year: Int, month: Int, day: Int): Long {
        val monthTerm = kotlin.math.ceil(29.5 * (month - 1)).toLong()
        val yearTerm = (year - 1).toLong() * 354L
        val leapTerm = Math.floorDiv(3L + 11L * year, 30L)
        return day + monthTerm + yearTerm + leapTerm + 1948440L - 1L
    }

    private fun jdnToIslamic(jdn0: Long): Triple<Int, Int, Int> {
        var jdn = jdn0 - 1948440 + 10632
        val n = (jdn - 1) / 10631
        jdn = jdn - 10631 * n + 354
        val j = ((10985 - jdn) / 5316) * ((50 * jdn) / 17719) + (jdn / 5670) * ((43 * jdn) / 15238)
        jdn = jdn - ((30 - j) / 15) * ((17719 * j) / 50) - (j / 16) * ((15238 * j) / 43) + 29
        val month = (24 * jdn) / 709
        val day = jdn - (709 * month) / 24
        val year = 30 * n + j - 30
        return Triple(year.toInt(), month.toInt(), day.toInt())
    }
}

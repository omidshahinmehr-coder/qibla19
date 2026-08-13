package com.qibla.prayertimes.util

import java.util.Calendar

data class JalaliDate(val year: Int, val month: Int, val day: Int) {
    companion object {
        private val MONTH_NAMES = listOf(
            "فروردین", "اردیبهشت", "خرداد", "تیر", "مرداد", "شهریور",
            "مهر", "آبان", "آذر", "دی", "بهمن", "اسفند"
        )
    }

    val monthName: String get() = MONTH_NAMES.getOrElse(month - 1) { "" }

    override fun toString(): String = "$day $monthName $year"
}

/**
 * Converts Gregorian dates to the Jalali (Solar Hijri / Persian) calendar.
 *
 * Ported from the jalaali-js reference algorithm (Julian Day Number based, using the
 * historically accurate 33-year leap-year break table), so it stays correct across
 * centuries rather than relying on a fixed cycle approximation. No external calendar
 * library or network call is needed. Verified against known reference dates such as
 * 2024-03-20 -> 1403-01-01 (Nowruz) and 1979-02-11 -> 1357-11-22 (22 Bahman).
 */
object JalaliCalendar {

    private val BREAKS = intArrayOf(
        -61, 9, 38, 199, 426, 686, 756, 818, 1111, 1181, 1210,
        1635, 2060, 2097, 2192, 2262, 2324, 2394, 2456, 3178
    )

    fun today(): JalaliDate {
        val cal = Calendar.getInstance()
        return fromGregorian(
            cal.get(Calendar.YEAR),
            cal.get(Calendar.MONTH) + 1,
            cal.get(Calendar.DAY_OF_MONTH)
        )
    }

    fun fromGregorian(gy: Int, gm: Int, gd: Int): JalaliDate {
        val (jy, jm, jd) = d2j(g2d(gy, gm, gd))
        return JalaliDate(jy, jm, jd)
    }

    /** Converts a Jalali date back to Gregorian (year, month, day) — the inverse of [fromGregorian]. */
    fun toGregorian(jy: Int, jm: Int, jd: Int): Triple<Int, Int, Int> = d2g(j2d(jy, jm, jd))

    /** Number of days in the given Jalali month (handles the leap-year 29/30-day Esfand). */
    fun daysInMonth(jy: Int, jm: Int): Int = when {
        jm <= 6 -> 31
        jm <= 11 -> 30
        else -> if (jalCal(jy).leap == 1) 30 else 29
    }

    // Truncating integer division (matches the JS `~~(a / b)` semantics used by the reference algorithm).
    private fun divi(a: Int, b: Int): Int {
        val q = a.toDouble() / b.toDouble()
        return if (q >= 0) q.toInt() else -((-q).toInt())
    }

    private fun mod(a: Int, b: Int): Int = a - divi(a, b) * b

    private fun g2d(gy: Int, gm: Int, gd: Int): Int {
        var d = divi((gy + divi(gm - 8, 6) + 100100) * 1461, 4) +
            divi(153 * mod(gm + 9, 12) + 2, 5) + gd - 34840408
        d -= divi(divi(gy + 100100 + divi(gm - 8, 6), 100) * 3, 4) - 752
        return d
    }

    private fun d2g(jdn: Int): Triple<Int, Int, Int> {
        var j = 4 * jdn + 139361631
        j += divi(divi(4 * jdn + 183187720, 146097) * 3, 4) * 4 - 3908
        val i = divi(mod(j, 1461), 4) * 5 + 308
        val gd = divi(mod(i, 153), 5) + 1
        val gm = mod(divi(i, 153), 12) + 1
        val gy = divi(j, 1461) - 100100 + divi(8 - gm, 6)
        return Triple(gy, gm, gd)
    }

    private data class JalCal(val leap: Int, val gy: Int, val march: Int)

    private fun jalCal(jy: Int): JalCal {
        val bl = BREAKS.size
        val gy = jy + 621
        var leapJ = -14
        var jp = BREAKS[0]
        var jump = 0
        var i = 1
        while (i < bl) {
            val jm = BREAKS[i]
            jump = jm - jp
            if (jy < jm) break
            leapJ += divi(jump, 33) * 8 + divi(mod(jump, 33), 4)
            jp = jm
            i += 1
        }
        var n = jy - jp
        leapJ += divi(n, 33) * 8 + divi(mod(n, 33) + 3, 4)
        if (mod(jump, 33) == 4 && jump - n == 4) leapJ += 1
        val leapG = divi(gy, 4) - divi((divi(gy, 100) + 1) * 3, 4) - 150
        val march = 20 + leapJ - leapG
        if (jump - n < 6) n = n - jump + divi(jump + 4, 33) * 33
        var leap = mod(mod(n + 1, 33) - 1, 4)
        if (leap == -1) leap = 4
        return JalCal(leap, gy, march)
    }

    private fun j2d(jy: Int, jm: Int, jd: Int): Int {
        val r = jalCal(jy)
        return g2d(r.gy, 3, r.march) + (jm - 1) * 31 - divi(jm, 7) * (jm - 7) + jd - 1
    }

    private fun d2j(jdn: Int): Triple<Int, Int, Int> {
        val gy0 = d2g(jdn).first
        var jy = gy0 - 621
        var r = jalCal(jy)
        val jdn1f = g2d(r.gy, 3, r.march)
        var k = jdn - jdn1f
        if (k >= 0) {
            if (k <= 185) {
                val jm = 1 + divi(k, 31)
                val jd = mod(k, 31) + 1
                return Triple(jy, jm, jd)
            } else {
                k -= 186
            }
        } else {
            jy -= 1
            k += 179
            r = jalCal(jy)
            if (r.leap == 1) k += 1
        }
        val jm = 7 + divi(k, 30)
        val jd = mod(k, 30) + 1
        return Triple(jy, jm, jd)
    }
}

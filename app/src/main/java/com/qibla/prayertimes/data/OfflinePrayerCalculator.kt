package com.qibla.prayertimes.data

import java.util.Calendar
import java.util.TimeZone
import kotlin.math.*

/**
 * Computes today's prayer times locally from the sun's position, with no network call.
 *
 * This is the standard astronomical method used by widely-deployed open prayer-time
 * calculators: solar declination and the equation of time are derived from low-precision
 * solar coordinate formulas, then each prayer's clock time is found from the hour angle at
 * which the sun reaches the required angle below the horizon. The angles themselves come from
 * the chosen [PrayerCalculationMethod], so offline and online results agree closely for the
 * same location, date and method.
 *
 * The device's current timezone offset is used, since a [com.qibla.prayertimes.model.City]
 * only stores latitude/longitude. This matches the real UTC offset whenever the phone's
 * system timezone corresponds to where the user actually is, which is the common case.
 */
object OfflinePrayerCalculator {

    private const val RISE_SET_ANGLE = 0.833 // atmospheric refraction + solar radius
    private const val IMSAK_OFFSET_MINUTES = 10.0

    fun computeToday(
        lat: Double,
        lon: Double,
        method: PrayerCalculationMethod = PrayerCalculationMethod.DEFAULT
    ): Map<String, String> {
        val cal = Calendar.getInstance()
        return computeFor(lat, lon, cal.get(Calendar.YEAR), cal.get(Calendar.MONTH) + 1, cal.get(Calendar.DAY_OF_MONTH), method)
    }

    /** Same computation as [computeToday] but for an arbitrary date — used for the monthly table. */
    fun computeFor(
        lat: Double,
        lon: Double,
        year: Int,
        month: Int,
        day: Int,
        method: PrayerCalculationMethod = PrayerCalculationMethod.DEFAULT
    ): Map<String, String> {
        val dateCal = java.util.GregorianCalendar(year, month - 1, day, 12, 0)
        val tzOffsetHours = TimeZone.getDefault().getOffset(dateCal.timeInMillis) / 3_600_000.0

        val jDate = julianDate(year, month, day) - lon / (15.0 * 24.0)

        // Two refinement passes starting from typical seed hours is enough for sub-minute accuracy.
        var seed = mapOf(
            "fajr" to 5.0, "sunrise" to 6.0, "dhuhr" to 12.0, "asr" to 13.0,
            "sunset" to 18.0, "maghrib" to 18.0, "isha" to 18.0
        )
        repeat(2) { seed = computePass(jDate, seed, lat, method) }

        val offset = tzOffsetHours - lon / 15.0
        val fajr = fixHour(seed.getValue("fajr") + offset)
        val sunrise = fixHour(seed.getValue("sunrise") + offset)
        val dhuhr = fixHour(seed.getValue("dhuhr") + offset)
        val asr = fixHour(seed.getValue("asr") + offset)
        val sunset = fixHour(seed.getValue("sunset") + offset)
        val maghrib = fixHour(seed.getValue("maghrib") + offset)
        val isha = fixHour(seed.getValue("isha") + offset)
        val imsak = fixHour(fajr - IMSAK_OFFSET_MINUTES / 60.0)

        // Jafari midnight: midpoint of the night between sunset and next day's fajr.
        val nightLength = fixHour(fajr - sunset)
        val midnight = fixHour(sunset + nightLength / 2.0)

        return mapOf(
            "Imsak" to hm(imsak),
            "Fajr" to hm(fajr),
            "Sunrise" to hm(sunrise),
            "Dhuhr" to hm(dhuhr),
            "Asr" to hm(asr),
            "Sunset" to hm(sunset),
            "Maghrib" to hm(maghrib),
            "Isha" to hm(isha),
            "Midnight" to hm(midnight)
        )
    }

    private fun computePass(jDate: Double, seed: Map<String, Double>, lat: Double, method: PrayerCalculationMethod): Map<String, Double> {
        val fajr = sunAngleTime(jDate, method.fajrAngle, seed.getValue("fajr") / 24.0, lat, ccw = true)
        val sunrise = sunAngleTime(jDate, RISE_SET_ANGLE, seed.getValue("sunrise") / 24.0, lat, ccw = true)
        val dhuhr = midDay(jDate, seed.getValue("dhuhr") / 24.0)
        val asr = asrTime(jDate, seed.getValue("asr") / 24.0, lat)
        val sunset = sunAngleTime(jDate, RISE_SET_ANGLE, seed.getValue("sunset") / 24.0, lat, ccw = false)
        val maghrib = sunAngleTime(jDate, method.maghribAngle, seed.getValue("maghrib") / 24.0, lat, ccw = false)
        val isha = sunAngleTime(jDate, method.ishaAngle, seed.getValue("isha") / 24.0, lat, ccw = false)
        return mapOf(
            "fajr" to fajr, "sunrise" to sunrise, "dhuhr" to dhuhr, "asr" to asr,
            "sunset" to sunset, "maghrib" to maghrib, "isha" to isha
        )
    }

    private fun julianDate(year: Int, month: Int, day: Int): Double {
        var y = year
        var m = month
        if (m <= 2) {
            y -= 1
            m += 12
        }
        val a = floor(y / 100.0)
        val b = 2 - a + floor(a / 4.0)
        return floor(365.25 * (y + 4716)) + floor(30.6001 * (m + 1)) + day + b - 1524.5
    }

    /** Returns (declination, equationOfTimeHours) for the given Julian date. */
    private fun sunPosition(jd: Double): Pair<Double, Double> {
        val d = jd - 2451545.0
        val g = fixAngle(357.529 + 0.98560028 * d)
        val q = fixAngle(280.459 + 0.98564736 * d)
        val l = fixAngle(q + 1.915 * sinDeg(g) + 0.020 * sinDeg(2 * g))
        val e = 23.439 - 0.00000036 * d
        var ra = Math.toDegrees(atan2(cosDeg(e) * sinDeg(l), cosDeg(l))) / 15.0
        ra = fixHour(ra)
        val eqt = q / 15.0 - ra
        val decl = Math.toDegrees(asin(sinDeg(e) * sinDeg(l)))
        return decl to eqt
    }

    private fun midDay(jDate: Double, time: Double): Double {
        val eqt = sunPosition(jDate + time).second
        return fixHour(12.0 - eqt)
    }

    private fun sunAngleTime(jDate: Double, angle: Double, time: Double, lat: Double, ccw: Boolean): Double {
        val decl = sunPosition(jDate + time).first
        val noon = midDay(jDate, time)
        val arg = (-sinDeg(angle) - sinDeg(decl) * sinDeg(lat)) / (cosDeg(decl) * cosDeg(lat))
        val t = (1.0 / 15.0) * Math.toDegrees(acos(arg.coerceIn(-1.0, 1.0)))
        return if (ccw) noon - t else noon + t
    }

    private fun asrTime(jDate: Double, time: Double, lat: Double): Double {
        val decl = sunPosition(jDate + time).first
        val angle = -Math.toDegrees(atan(1.0 / (1.0 + tanDeg(abs(lat - decl)))))
        return sunAngleTime(jDate, angle, time, lat, ccw = false)
    }

    private fun sinDeg(deg: Double) = sin(Math.toRadians(deg))
    private fun cosDeg(deg: Double) = cos(Math.toRadians(deg))
    private fun tanDeg(deg: Double) = tan(Math.toRadians(deg))

    private fun fixAngle(a: Double): Double {
        var v = a - 360.0 * floor(a / 360.0)
        if (v < 0) v += 360.0
        return v
    }

    private fun fixHour(h: Double): Double {
        var v = h - 24.0 * floor(h / 24.0)
        if (v < 0) v += 24.0
        return v
    }

    private fun hm(hours: Double): String {
        var h = floor(hours).toInt()
        var m = ((hours - h) * 60.0).roundToInt()
        if (m == 60) {
            m = 0
            h = (h + 1) % 24
        }
        return "%02d:%02d".format(h, m)
    }
}

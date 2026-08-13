package com.qibla.prayertimes.data

import kotlin.math.*

object QiblaMath {
    private const val KAABA_LAT = 21.4225
    private const val KAABA_LON = 39.8262

    /** Bearing in degrees (0-360) from true north, clockwise, toward the Kaaba. */
    fun bearing(lat: Double, lon: Double): Double {
        val phi1 = Math.toRadians(lat)
        val phi2 = Math.toRadians(KAABA_LAT)
        val dLambda = Math.toRadians(KAABA_LON - lon)
        val y = sin(dLambda) * cos(phi2)
        val x = cos(phi1) * sin(phi2) - sin(phi1) * cos(phi2) * cos(dLambda)
        val theta = atan2(y, x)
        return (Math.toDegrees(theta) + 360) % 360
    }

    /** Great-circle distance to the Kaaba in kilometers. */
    fun distanceKm(lat: Double, lon: Double): Int {
        val r = 6371.0
        val phi1 = Math.toRadians(lat)
        val phi2 = Math.toRadians(KAABA_LAT)
        val dPhi = Math.toRadians(KAABA_LAT - lat)
        val dLambda = Math.toRadians(KAABA_LON - lon)
        val a = sin(dPhi / 2).pow(2) + cos(phi1) * cos(phi2) * sin(dLambda / 2).pow(2)
        return (r * 2 * atan2(sqrt(a), sqrt(1 - a))).roundToInt()
    }
}

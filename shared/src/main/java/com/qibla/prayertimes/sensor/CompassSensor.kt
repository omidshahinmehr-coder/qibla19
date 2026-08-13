package com.qibla.prayertimes.sensor

import android.content.Context
import android.hardware.Sensor
import android.hardware.SensorEvent
import android.hardware.SensorEventListener
import android.hardware.SensorManager
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableFloatStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.platform.LocalContext

/**
 * Reads the device's magnetic heading (0° = true-ish north, clockwise) using the rotation
 * vector sensor when available, falling back to accelerometer + magnetometer fusion on
 * older/cheaper devices that lack it. A short low-pass filter smooths out jitter so the
 * needle doesn't visibly shake.
 *
 * Returns null when no orientation sensor is present on the device at all — callers should
 * fall back to a static, north-up compass in that case.
 */
class CompassSensor(context: Context) : SensorEventListener {
    private val sensorManager = context.getSystemService(Context.SENSOR_SERVICE) as SensorManager
    private val rotationSensor: Sensor? = sensorManager.getDefaultSensor(Sensor.TYPE_ROTATION_VECTOR)
    private val accelerometer: Sensor? = sensorManager.getDefaultSensor(Sensor.TYPE_ACCELEROMETER)
    private val magnetometer: Sensor? = sensorManager.getDefaultSensor(Sensor.TYPE_MAGNETIC_FIELD)

    val hasOrientationSensor: Boolean = rotationSensor != null || (accelerometer != null && magnetometer != null)

    private var onHeadingChanged: ((Float) -> Unit)? = null
    private var smoothedHeading: Float? = null

    private val rotationMatrix = FloatArray(9)
    private val orientationAngles = FloatArray(3)
    private var lastAccel: FloatArray? = null
    private var lastMag: FloatArray? = null

    fun start(listener: (Float) -> Unit) {
        onHeadingChanged = listener
        if (rotationSensor != null) {
            sensorManager.registerListener(this, rotationSensor, SensorManager.SENSOR_DELAY_UI)
        } else if (accelerometer != null && magnetometer != null) {
            sensorManager.registerListener(this, accelerometer, SensorManager.SENSOR_DELAY_UI)
            sensorManager.registerListener(this, magnetometer, SensorManager.SENSOR_DELAY_UI)
        }
    }

    fun stop() {
        sensorManager.unregisterListener(this)
        onHeadingChanged = null
        smoothedHeading = null
    }

    override fun onSensorChanged(event: SensorEvent) {
        val rawHeading: Float = when (event.sensor.type) {
            Sensor.TYPE_ROTATION_VECTOR -> {
                SensorManager.getRotationMatrixFromVector(rotationMatrix, event.values)
                SensorManager.getOrientation(rotationMatrix, orientationAngles)
                Math.toDegrees(orientationAngles[0].toDouble()).toFloat()
            }
            Sensor.TYPE_ACCELEROMETER -> {
                lastAccel = event.values.clone()
                computeFusedHeading() ?: return
            }
            Sensor.TYPE_MAGNETIC_FIELD -> {
                lastMag = event.values.clone()
                computeFusedHeading() ?: return
            }
            else -> return
        }

        val normalized = (rawHeading + 360f) % 360f
        val previous = smoothedHeading
        val next = if (previous == null) {
            normalized
        } else {
            // Shortest-path low-pass filter so the 0/360 wraparound doesn't cause a visible snap.
            var delta = normalized - previous
            if (delta > 180f) delta -= 360f
            if (delta < -180f) delta += 360f
            (previous + delta * 0.15f + 360f) % 360f
        }
        smoothedHeading = next
        onHeadingChanged?.invoke(next)
    }

    private fun computeFusedHeading(): Float? {
        val accel = lastAccel ?: return null
        val mag = lastMag ?: return null
        val success = SensorManager.getRotationMatrix(rotationMatrix, null, accel, mag)
        if (!success) return null
        SensorManager.getOrientation(rotationMatrix, orientationAngles)
        return Math.toDegrees(orientationAngles[0].toDouble()).toFloat()
    }

    override fun onAccuracyChanged(sensor: Sensor?, accuracy: Int) { /* not needed */ }
}

/**
 * Composable helper: returns the live device heading in degrees, or null if the device has
 * no usable orientation sensor. Automatically starts/stops with the composition lifecycle.
 */
@Composable
fun rememberDeviceHeading(): Float? {
    val context = LocalContext.current
    var heading by remember { mutableFloatStateOf(Float.NaN) }
    var available by remember { mutableFloatStateOf(1f) }

    DisposableEffect(Unit) {
        val sensor = CompassSensor(context)
        if (sensor.hasOrientationSensor) {
            sensor.start { heading = it }
        } else {
            available = 0f
        }
        onDispose { sensor.stop() }
    }

    return if (available == 0f || heading.isNaN()) null else heading
}

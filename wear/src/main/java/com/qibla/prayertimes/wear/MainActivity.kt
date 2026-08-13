package com.qibla.prayertimes.wear

import android.Manifest
import android.content.pm.PackageManager
import android.location.Location
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.core.content.ContextCompat
import androidx.wear.compose.material.Chip
import androidx.wear.compose.material.ChipDefaults
import androidx.wear.compose.material.CircularProgressIndicator
import androidx.wear.compose.material.MaterialTheme
import androidx.wear.compose.material.Text
import com.qibla.prayertimes.data.QiblaMath
import com.qibla.prayertimes.sensor.rememberDeviceHeading
import kotlinx.coroutines.launch

private sealed class WatchState {
    object PermissionNeeded : WatchState()
    object Locating : WatchState()
    object NoLocation : WatchState()
    data class Ready(val lat: Double, val lon: Double) : WatchState()
}

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContent {
            WatchQiblaApp()
        }
    }
}

@Composable
private fun WatchQiblaApp() {
    val context = LocalContext.current
    val scope = rememberCoroutineScope()

    var state by remember {
        mutableStateOf<WatchState>(
            if (hasLocationPermission(context)) WatchState.Locating else WatchState.PermissionNeeded
        )
    }

    val permissionLauncher = rememberLocationPermissionLauncher { granted ->
        state = if (granted) WatchState.Locating else WatchState.PermissionNeeded
    }

    fun fetchLocation() {
        scope.launch {
            val location: Location? = WatchLocationHelper(context).getCurrentLocation()
            state = if (location != null) {
                WatchState.Ready(location.latitude, location.longitude)
            } else {
                WatchState.NoLocation
            }
        }
    }

    LaunchedEffect(state is WatchState.Locating) {
        if (state is WatchState.Locating) fetchLocation()
    }

    MaterialTheme {
        Box(
            modifier = Modifier
                .fillMaxSize()
                .background(WatchNightDeep),
            contentAlignment = Alignment.Center
        ) {
            when (val s = state) {
                is WatchState.PermissionNeeded -> PermissionScreen {
                    permissionLauncher.launch(
                        arrayOf(Manifest.permission.ACCESS_FINE_LOCATION, Manifest.permission.ACCESS_COARSE_LOCATION)
                    )
                }
                is WatchState.Locating -> LocatingScreen()
                is WatchState.NoLocation -> NoLocationScreen(onRetry = { state = WatchState.Locating })
                is WatchState.Ready -> QiblaDialScreen(lat = s.lat, lon = s.lon)
            }
        }
    }
}

private fun hasLocationPermission(context: android.content.Context): Boolean =
    ContextCompat.checkSelfPermission(context, Manifest.permission.ACCESS_FINE_LOCATION) == PackageManager.PERMISSION_GRANTED ||
        ContextCompat.checkSelfPermission(context, Manifest.permission.ACCESS_COARSE_LOCATION) == PackageManager.PERMISSION_GRANTED

@Composable
private fun rememberLocationPermissionLauncher(onResult: (Boolean) -> Unit) =
    androidx.activity.compose.rememberLauncherForActivityResult(
        ActivityResultContracts.RequestMultiplePermissions()
    ) { results ->
        onResult(results.values.any { it })
    }

@Composable
private fun PermissionScreen(onRequest: () -> Unit) {
    Column(
        modifier = Modifier.padding(16.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Text(
            text = stringResource(R.string.wear_permission_needed),
            color = WatchAmberText,
            fontSize = 13.sp,
            textAlign = TextAlign.Center
        )
        Spacer(Modifier.height(12.dp))
        Chip(
            onClick = onRequest,
            label = { Text(stringResource(R.string.wear_grant_permission)) },
            colors = ChipDefaults.chipColors(backgroundColor = WatchBrass)
        )
    }
}

@Composable
private fun LocatingScreen() {
    Column(horizontalAlignment = Alignment.CenterHorizontally) {
        CircularProgressIndicator(indicatorColor = WatchBrassLight)
        Spacer(Modifier.height(10.dp))
        Text(stringResource(R.string.wear_finding_location), color = WatchAmberMuted, fontSize = 12.sp)
    }
}

@Composable
private fun NoLocationScreen(onRetry: () -> Unit) {
    Column(
        modifier = Modifier.padding(16.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Text(
            text = stringResource(R.string.wear_no_location),
            color = WatchAmberText,
            fontSize = 13.sp,
            textAlign = TextAlign.Center
        )
        Spacer(Modifier.height(12.dp))
        Chip(
            onClick = onRetry,
            label = { Text(stringResource(R.string.wear_retry)) },
            colors = ChipDefaults.chipColors(backgroundColor = WatchBrass)
        )
    }
}

/**
 * Same bearing/alignment math as the phone app's QiblaScreen: the needle is drawn relative to
 * the live device heading (so it always points at the Kaaba regardless of which way the watch
 * is facing), the tick ring counter-rotates to stay true-north-referenced, and "aligned" is
 * within 6° either side — identical thresholds to the phone.
 */
@Composable
private fun QiblaDialScreen(lat: Double, lon: Double) {
    val bearing = remember(lat, lon) { QiblaMath.bearing(lat, lon).toFloat() }
    val distanceKm = remember(lat, lon) { QiblaMath.distanceKm(lat, lon) }
    val deviceHeading = rememberDeviceHeading()

    val needleAngle = if (deviceHeading != null) (bearing - deviceHeading + 360f) % 360f else bearing
    val dialRotation = if (deviceHeading != null) (360f - deviceHeading) % 360f else 0f
    val isAligned = deviceHeading != null && minOf(needleAngle, 360f - needleAngle) < 6f

    Column(horizontalAlignment = Alignment.CenterHorizontally, verticalArrangement = Arrangement.Center) {
        if (deviceHeading == null) {
            Text(
                text = stringResource(R.string.wear_no_compass),
                color = WatchAmberMuted,
                fontSize = 10.sp,
                textAlign = TextAlign.Center,
                modifier = Modifier.padding(bottom = 4.dp)
            )
        }
        WatchCompassDial(
            bearingDegrees = needleAngle,
            dialRotationDegrees = dialRotation,
            isAligned = isAligned,
            dialSize = 170.dp
        )
        Spacer(Modifier.height(6.dp))
        Text("${"%.0f".format(bearing)}°", color = WatchAmberText, fontSize = 15.sp)
        Text(
            text = stringResource(R.string.wear_distance_km, distanceKm),
            color = WatchAmberMuted,
            fontSize = 10.sp
        )
    }
}

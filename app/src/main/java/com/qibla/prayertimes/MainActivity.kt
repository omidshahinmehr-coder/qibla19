package com.qibla.prayertimes

import android.Manifest
import android.content.Context
import android.os.Build
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.Surface
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.ui.Modifier
import com.qibla.prayertimes.ui.QiblaNavHost
import com.qibla.prayertimes.ui.theme.NightMid
import com.qibla.prayertimes.ui.theme.QiblaAppTheme
import com.qibla.prayertimes.ui.theme.ThemeState
import com.qibla.prayertimes.util.LocalePrefs

class MainActivity : ComponentActivity() {

    override fun attachBaseContext(newBase: Context) {
        super.attachBaseContext(LocalePrefs.wrap(newBase))
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        ThemeState.initFrom(this)
        setContent {
            QiblaAppTheme {
                Surface(modifier = Modifier.fillMaxSize(), color = NightMid) {
                    val permissionLauncher = rememberLauncherForActivityResult(
                        ActivityResultContracts.RequestMultiplePermissions()
                    ) { /* result handled implicitly; relevant buttons re-check permission at call time */ }

                    LaunchedEffect(Unit) {
                        val permissions = mutableListOf(
                            Manifest.permission.ACCESS_FINE_LOCATION,
                            Manifest.permission.ACCESS_COARSE_LOCATION
                        )
                        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
                            permissions.add(Manifest.permission.POST_NOTIFICATIONS)
                        }
                        permissionLauncher.launch(permissions.toTypedArray())
                    }

                    QiblaNavHost()
                }
            }
        }
    }
}

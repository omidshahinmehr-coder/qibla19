package com.qibla.prayertimes

import android.annotation.SuppressLint
import android.app.Activity
import android.content.Intent
import android.os.Bundle
import android.webkit.JavascriptInterface
import android.webkit.WebView
import android.webkit.WebViewClient
import androidx.activity.ComponentActivity
import androidx.lifecycle.lifecycleScope
import com.qibla.prayertimes.data.LocationHelper
import kotlinx.coroutines.launch

/**
 * Lets the user manually choose a location by panning/tapping/dragging a pin on a real,
 * interactive map, instead of only being able to search or enter coordinates manually.
 * Started with an Activity Result contract; returns RESULT_OK with EXTRA_PICKED_LAT/
 * EXTRA_PICKED_LNG when the user confirms.
 *
 * This is a plain Android Activity with a classic XML layout — deliberately not a Compose
 * screen. The WebView had repeated, unreproducible "blank white screen" issues when embedded
 * in Compose via AndroidView interop; this mirrors a separately proven-working implementation
 * that uses a real Activity instead, to rule out the interop layer as the cause.
 */
class MapPickerActivity : ComponentActivity() {

    companion object {
        const val EXTRA_INITIAL_LAT = "initial_lat"
        const val EXTRA_INITIAL_LNG = "initial_lng"
        const val EXTRA_PICKED_LAT = "picked_lat"
        const val EXTRA_PICKED_LNG = "picked_lng"
    }

    private lateinit var webView: WebView

    @SuppressLint("SetJavaScriptEnabled")
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_map_picker)

        webView = findViewById(R.id.pickerWebView)
        webView.settings.javaScriptEnabled = true
        webView.settings.domStorageEnabled = true
        webView.addJavascriptInterface(JsBridge(), "AndroidBridge")

        val hasInitialCoords = intent.hasExtra(EXTRA_INITIAL_LAT) && intent.hasExtra(EXTRA_INITIAL_LNG)
        val initialLat = intent.getDoubleExtra(EXTRA_INITIAL_LAT, Double.NaN)
        val initialLng = intent.getDoubleExtra(EXTRA_INITIAL_LNG, Double.NaN)

        webView.webViewClient = object : WebViewClient() {
            override fun onPageFinished(view: WebView, url: String?) {
                super.onPageFinished(view, url)
                if (hasInitialCoords) {
                    view.evaluateJavascript("setInitialLocation($initialLat, $initialLng)", null)
                } else {
                    // No coordinates passed in — try the phone's current location as a
                    // friendlier starting point than the page's hardcoded default; if that
                    // fails, the default stands and the user just pans to find their spot.
                    lifecycleScope.launch {
                        val city = LocationHelper(this@MapPickerActivity).getCurrentCity()
                        if (city != null) {
                            view.evaluateJavascript("setInitialLocation(${city.lat}, ${city.lon})", null)
                        }
                    }
                }
            }
        }

        webView.loadUrl("file:///android_asset/map_picker.html")
    }

    private inner class JsBridge {
        @JavascriptInterface
        fun onLocationPicked(lat: Double, lng: Double) {
            runOnUiThread {
                val result = Intent().apply {
                    putExtra(EXTRA_PICKED_LAT, lat)
                    putExtra(EXTRA_PICKED_LNG, lng)
                }
                setResult(Activity.RESULT_OK, result)
                finish()
            }
        }
    }
}

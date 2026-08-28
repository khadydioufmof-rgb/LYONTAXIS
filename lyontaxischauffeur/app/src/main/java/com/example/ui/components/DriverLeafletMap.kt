package com.example.ui.components

import android.annotation.SuppressLint
import android.webkit.WebView
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.viewinterop.AndroidView
import com.example.model.DriverStatus
import com.example.model.RideRequest

@SuppressLint("SetJavaScriptEnabled")
@Composable
fun DriverLeafletMap(
    status: DriverStatus,
    activeRide: RideRequest?,
    modifier: Modifier = Modifier
) {
    AndroidView(
        modifier = modifier,
        factory = { context ->
            WebView(context).apply {
                settings.javaScriptEnabled = true
                settings.domStorageEnabled = true
                setBackgroundColor(0xFF0B0F17.toInt())
                loadDataWithBaseURL(
                    "https://lyontaxis.local/",
                    leafletDocument(status, activeRide),
                    "text/html",
                    "UTF-8",
                    null
                )
            }
        },
        update = { webView ->
            webView.loadDataWithBaseURL(
                "https://lyontaxis.local/",
                leafletDocument(status, activeRide),
                "text/html",
                "UTF-8",
                null
            )
        }
    )
}

private fun leafletDocument(status: DriverStatus, activeRide: RideRequest?): String {
    val rideMarker = if (activeRide != null) {
        "L.marker([45.7601, 4.8357]).addTo(map).bindPopup('Prise en charge').openPopup();"
    } else {
        ""
    }
    return """
        <!doctype html>
        <html><head><meta name="viewport" content="width=device-width, initial-scale=1.0">
        <link rel="stylesheet" href="https://unpkg.com/leaflet@1.9.4/dist/leaflet.css">
        <style>html,body,#map{height:100%;margin:0;background:#0b0f17} .status{position:absolute;z-index:1000;top:14px;left:14px;padding:8px 12px;border-radius:12px;background:#10141d;color:#ffb300;font:700 12px Arial;box-shadow:0 4px 16px #0006}</style>
        </head><body><div id="map"></div><div class="status">${status.name.replace('_', ' ')}</div>
        <script src="https://unpkg.com/leaflet@1.9.4/dist/leaflet.js"></script>
        <script>const map=L.map('map').setView([45.764,4.8357],13);L.tileLayer('https://{s}.tile.openstreetmap.org/{z}/{x}/{y}.png',{attribution:'&copy; OpenStreetMap'}).addTo(map);L.marker([45.764,4.8357]).addTo(map).bindPopup('Votre position');$rideMarker</script>
        </body></html>
    """.trimIndent()
}

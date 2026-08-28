package com.example.ui.components

import android.annotation.SuppressLint
import android.webkit.JavascriptInterface
import android.webkit.WebView
import android.webkit.WebViewClient
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.viewinterop.AndroidView
import com.example.model.LocationPoint
import java.util.Locale

@SuppressLint("SetJavaScriptEnabled")
@Composable
fun LeafletMap(
  pickup: LocationPoint?,
  dropoff: LocationPoint?,
  modifier: Modifier = Modifier,
  interactive: Boolean = true,
  onMapTap: ((latitude: Double, longitude: Double) -> Unit)? = null
) {
  val context = LocalContext.current
  val bridge = remember(onMapTap) { MapBridge(onMapTap) }
  val webView = remember { WebView(context) }

  AndroidView(
    factory = {
      webView.apply {
        settings.javaScriptEnabled = true
        settings.domStorageEnabled = true
        settings.allowFileAccess = true
        isVerticalScrollBarEnabled = false
        isHorizontalScrollBarEnabled = false
        addJavascriptInterface(bridge, "AndroidMap")
        webViewClient = object : WebViewClient() {
          override fun onPageFinished(view: WebView, url: String?) {
            updateMap(view, pickup, dropoff, interactive)
          }
        }
        loadUrl("file:///android_asset/leaflet_map.html")
      }
    },
    update = { view -> updateMap(view, pickup, dropoff, interactive) },
    modifier = modifier
  )

  DisposableEffect(webView) {
    onDispose {
      webView.removeJavascriptInterface("AndroidMap")
      webView.destroy()
    }
  }
}

private fun updateMap(view: WebView, pickup: LocationPoint?, dropoff: LocationPoint?, interactive: Boolean) {
  val pickupJson = pickup?.let { "{lat:${it.latitude},lng:${it.longitude},title:${it.title.toJsString()}}" } ?: "null"
  val dropoffJson = dropoff?.let { "{lat:${it.latitude},lng:${it.longitude},title:${it.title.toJsString()}}" } ?: "null"
  view.evaluateJavascript(
    "window.renderRide($pickupJson,$dropoffJson,${interactive.toString().lowercase(Locale.US)});",
    null
  )
}

private fun String.toJsString(): String = "'${replace("\\", "\\\\").replace("'", "\\'").replace("\n", " ")}'"

private class MapBridge(private val onMapTap: ((Double, Double) -> Unit)?) {
  @JavascriptInterface
  fun onMapTap(latitude: Double, longitude: Double) {
    onMapTap?.invoke(latitude, longitude)
  }
}

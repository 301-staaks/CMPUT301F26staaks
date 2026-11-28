package com.example.staaks_campsite_app

import android.content.Context
import android.content.pm.PackageManager
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Card
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import com.google.android.gms.maps.CameraUpdateFactory
import com.google.android.gms.maps.model.CameraPosition
import com.google.maps.android.compose.GoogleMap
import com.google.maps.android.compose.Marker
import com.google.maps.android.compose.rememberCameraPositionState
import com.google.android.gms.maps.model.LatLng
import com.google.maps.android.compose.rememberMarkerState

fun isMapsApiKeyConfigured(context: Context): Boolean {
    return try {
        val appInfo = context.packageManager.getApplicationInfo(context.packageName, PackageManager.GET_META_DATA)
        val apiKey = appInfo.metaData?.getString("com.google.android.geo.API_KEY")
        !apiKey.isNullOrBlank() && apiKey != "\${MAPS_API_KEY}" && !apiKey.startsWith("YOUR_")
    } catch (e: Exception) {
        false
    }
}

@Composable
fun PinMap(position: LatLng, title: String?, snippet: String? = null, modifier: Modifier = Modifier) {
    val context = LocalContext.current
    val apiKeyConfigured = remember { isMapsApiKeyConfigured(context) }

    if (!apiKeyConfigured) {
        Card(modifier = modifier) {
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(16.dp),
                contentAlignment = Alignment.Center
            ) {
                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                    if (!title.isNullOrBlank()) {
                        Text(text = title)
                    }
                    if (!snippet.isNullOrBlank()) {
                        Text(text = snippet)
                    }
                    Text(text = "Lat: ${position.latitude}, Lng: ${position.longitude}")
                    Text(
                        text = "Google Maps API Key not configured.\nSet MAPS_API_KEY in local.properties",
                        color = Color.Red,
                        modifier = Modifier.padding(top = 8.dp)
                    )
                }
            }
        }
        return
    }

    val camera = rememberCameraPositionState {
        this.position = CameraPosition.fromLatLngZoom(position, 15f)
    }
    LaunchedEffect(position) {
        camera.animate(CameraUpdateFactory.newLatLngZoom(position, 15f))
    }

    val markerState = rememberMarkerState(position = position)
    LaunchedEffect(position) { markerState.position = position }

    Text("lat = ${position.latitude}, lng = ${position.longitude}")

    GoogleMap(modifier = modifier, cameraPositionState = camera) {
        Marker(
            state = markerState,
            title = title,
            snippet = snippet,
        )
    }
}
package com.example.staaks_campsite_app

import android.content.Context
import android.location.Address
import android.location.Geocoder
import android.os.Build
import com.google.android.gms.maps.model.LatLng
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.suspendCancellableCoroutine
import kotlinx.coroutines.withContext
import java.util.Locale
import kotlin.coroutines.resume

fun parseCoordinates(input: String): LatLng? {
    val parts = input.split(",").map { it.trim().toDoubleOrNull() }
    if (parts.size != 2 || parts.any { it == null }) return null
    val lat = parts[0]!!; val lng = parts[1]!!
    return if (lat in -90.0..90.0 && lng in -180.0..180.0) LatLng(lat, lng) else null
}


suspend fun geocode(context: Context, query: String): LatLng? {
    if (!Geocoder.isPresent()) return null
    return withContext(Dispatchers.IO) {
        runCatching {
            val geocoder = Geocoder(context, Locale.getDefault())
            if (Build.VERSION.SDK_INT >= 33) {
                suspendCancellableCoroutine { cont ->
                    try {
                        geocoder.getFromLocationName(query, 1, object : Geocoder.GeocodeListener {
                            override fun onGeocode(results: MutableList<Address>) {
                                cont.resume(results.firstOrNull()?.let { LatLng(it.latitude, it.longitude) })
                            }
                            override fun onError(errorMessage: String?) {
                                cont.resume(null)
                            }
                        })
                    } catch (e: Exception) {
                        cont.resume(null)
                    }
                }
            } else {
                @Suppress("DEPRECATION")
                geocoder.getFromLocationName(query, 1)?.firstOrNull()
                    ?.let { LatLng(it.latitude, it.longitude) }
            }
        }.getOrNull()
    }
}
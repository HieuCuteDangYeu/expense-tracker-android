package com.example.expensetracker.util

import android.annotation.SuppressLint
import android.content.Context
import android.location.Geocoder
import android.location.Location
import android.os.Build
import com.google.android.gms.location.LocationServices
import com.google.android.gms.location.Priority
import com.google.android.gms.tasks.CancellationTokenSource
import kotlinx.coroutines.tasks.await
import java.util.Locale

class LocationHelper(private val context: Context) {
    private val fusedLocationClient = LocationServices.getFusedLocationProviderClient(context)

    @SuppressLint("MissingPermission")
    suspend fun getCurrentLocationAddress(): String? {
        return try {
            val location: Location? = fusedLocationClient.getCurrentLocation(
                Priority.PRIORITY_HIGH_ACCURACY,
                CancellationTokenSource().token
            ).await()

            if (location != null) {
                getAddressFromCoordinates(location.latitude, location.longitude)
            } else {
                null
            }
        } catch (e: Exception) {
            e.printStackTrace()
            null
        }
    }

    private fun getAddressFromCoordinates(lat: Double, lng: Double): String {
        val geocoder = Geocoder(context, Locale.getDefault())
        return try {
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
                // For API 33+, geocode is asynchronous natively, but since we are in a suspend function
                // and the blocking getFromLocation is deprecated, we will try the legacy blocking call
                // first as it still works synchronously for most cases, wrapped in try/catch.
                val addresses = geocoder.getFromLocation(lat, lng, 1)
                if (!addresses.isNullOrEmpty()) {
                    val address = addresses[0]
                    val city = address.locality ?: address.subAdminArea ?: ""
                    val street = address.thoroughfare ?: address.featureName ?: ""
                    if (city.isNotBlank() || street.isNotBlank()) {
                        listOf(street, city).filter { it.isNotBlank() }.joinToString(", ")
                    } else {
                        buildLatLongString(lat, lng)
                    }
                } else {
                    buildLatLongString(lat, lng)
                }
            } else {
                @Suppress("DEPRECATION")
                val addresses = geocoder.getFromLocation(lat, lng, 1)
                if (!addresses.isNullOrEmpty()) {
                    val address = addresses[0]
                    val city = address.locality ?: address.subAdminArea ?: ""
                    val street = address.thoroughfare ?: address.featureName ?: ""
                    if (city.isNotBlank() || street.isNotBlank()) {
                        listOf(street, city).filter { it.isNotBlank() }.joinToString(", ")
                    } else {
                        buildLatLongString(lat, lng)
                    }
                } else {
                    buildLatLongString(lat, lng)
                }
            }
        } catch (e: Exception) {
            e.printStackTrace()
            buildLatLongString(lat, lng)
        }
    }

    private fun buildLatLongString(lat: Double, lng: Double): String {
        return String.format(Locale.US, "%.4f, %.4f", lat, lng)
    }
}

package com.example.expensetracker.data.network

import android.content.Context
import android.net.ConnectivityManager
import android.net.Network
import android.net.NetworkCapabilities
import android.net.NetworkRequest
import kotlinx.coroutines.channels.awaitClose
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.callbackFlow

enum class NetworkStatus {
    Available_WiFi, Available_Cellular, Lost
}

class NetworkConnectivityObserver(context: Context) {
    private val connectivityManager =
        context.getSystemService(Context.CONNECTIVITY_SERVICE) as ConnectivityManager

    val networkStatus: Flow<NetworkStatus> = callbackFlow {
        val callback = object : ConnectivityManager.NetworkCallback() {
            override fun onAvailable(network: Network) {
                trySend(getCurrentNetworkStatus())
            }

            override fun onLost(network: Network) {
                trySend(NetworkStatus.Lost)
            }

            override fun onCapabilitiesChanged(
                network: Network, networkCapabilities: NetworkCapabilities
            ) {
                trySend(getCurrentNetworkStatus())
            }
        }

        val request =
            NetworkRequest.Builder().addCapability(NetworkCapabilities.NET_CAPABILITY_INTERNET)
                .build()

        connectivityManager.registerNetworkCallback(request, callback)

        trySend(getCurrentNetworkStatus())

        awaitClose {
            connectivityManager.unregisterNetworkCallback(callback)
        }
    }

    private fun getCurrentNetworkStatus(): NetworkStatus {
        val network = connectivityManager.activeNetwork ?: return NetworkStatus.Lost
        val capabilities =
            connectivityManager.getNetworkCapabilities(network) ?: return NetworkStatus.Lost

        return when {
            capabilities.hasTransport(NetworkCapabilities.TRANSPORT_WIFI) -> NetworkStatus.Available_WiFi
            capabilities.hasTransport(NetworkCapabilities.TRANSPORT_CELLULAR) -> NetworkStatus.Available_Cellular
            else -> NetworkStatus.Available_Cellular
        }
    }
}
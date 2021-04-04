package com.acel.streamlivetool.value

import android.content.Context
import android.net.ConnectivityManager
import android.net.Network
import android.net.NetworkCapabilities
import android.net.NetworkRequest
import androidx.lifecycle.MutableLiveData
import com.acel.streamlivetool.base.MyApplication

object WifiManager {
    val isNetworkAvailable = MutableLiveData(false)
    val isWifiConnected = MutableLiveData(false)
    private val connectivityManager = MyApplication.application.getSystemService(Context.CONNECTIVITY_SERVICE) as ConnectivityManager
    private val networkRequest = NetworkRequest.Builder().build()

    private val networkCallback = object : ConnectivityManager.NetworkCallback() {
        override fun onAvailable(network: Network) {
            super.onAvailable(network)
            isNetworkAvailable.postValue(true)
            isWifiConnected.postValue(isWifiNetwork(network))
        }

        override fun onLost(network: Network) {
            super.onLost(network)
            isNetworkAvailable.postValue(false)
            //如果是wifi断开
            if (isWifiNetwork(network))
                isWifiConnected.postValue(false)
        }
    }

    fun startListen() {
        connectivityManager.registerNetworkCallback(networkRequest, networkCallback)
    }

    fun stopListen() {
        connectivityManager.unregisterNetworkCallback(networkCallback)
    }

    @Suppress("DEPRECATION")
    private fun isWifiNetwork(network: Network): Boolean {
        return if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.M) {
            val capabilities = connectivityManager.getNetworkCapabilities(network)
            capabilities != null && capabilities.hasTransport(NetworkCapabilities.TRANSPORT_WIFI)
        } else {
            connectivityManager.activeNetworkInfo?.type == ConnectivityManager.TYPE_WIFI
        }
    }
}
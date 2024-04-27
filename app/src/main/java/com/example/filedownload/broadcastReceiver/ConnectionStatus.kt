package com.example.harrypotter.broadcastReceiver

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.net.ConnectivityManager
import android.net.NetworkCapabilities
import android.os.Build

@Suppress("DEPRECATION")
class ConnectionStatus : BroadcastReceiver() {
    override fun onReceive(context: Context, p1: Intent?) {
        connectivityReceiverListener?.onNetworkConnectionChanged(isConnectedOrConnecting(context))
    }

    fun isConnectedOrConnecting(context: Context) : Boolean{
        val connectivityManager = context.getSystemService(Context.CONNECTIVITY_SERVICE) as ConnectivityManager
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            val activeNetwork = connectivityManager.activeNetwork ?: return false
            val networkCapabilities = connectivityManager.getNetworkCapabilities(activeNetwork) ?: return false
            return when {
                networkCapabilities.hasTransport(NetworkCapabilities.TRANSPORT_WIFI) -> true
                networkCapabilities.hasTransport(NetworkCapabilities.TRANSPORT_CELLULAR) -> true
                networkCapabilities.hasTransport(NetworkCapabilities.TRANSPORT_ETHERNET) -> true
                else -> false
            }
        } else {
            return connectivityManager.activeNetworkInfo?.isConnected ?: false
        }

    }

    interface ConnectivityReceiverListener{
        fun onNetworkConnectionChanged(isConnect : Boolean)
    }

    companion object {
        var connectivityReceiverListener : ConnectivityReceiverListener? = null
    }
}
package com.example.filedownload.ui

import android.Manifest
import android.app.DownloadManager
import android.content.Intent
import android.content.IntentFilter
import android.content.pm.PackageManager
import android.net.ConnectivityManager
import android.net.Uri
import android.os.Build
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.provider.Settings
import android.widget.Button
import com.example.filedownload.R
import com.example.filedownload.broadcastReceiver.DownloadBroadcastReceiver
import com.example.filedownload.databinding.ActivityMainBinding
import com.example.harrypotter.broadcastReceiver.ConnectionStatus
import com.fondesa.kpermissions.allDenied
import com.fondesa.kpermissions.allGranted
import com.fondesa.kpermissions.allPermanentlyDenied
import com.fondesa.kpermissions.anyDenied
import com.fondesa.kpermissions.anyPermanentlyDenied
import com.fondesa.kpermissions.extension.permissionsBuilder
import com.fondesa.kpermissions.extension.send
import com.google.android.material.dialog.MaterialAlertDialogBuilder
import com.google.android.material.snackbar.Snackbar

class MainActivity : AppCompatActivity(), ConnectionStatus.ConnectivityReceiverListener {
    private lateinit var binding: ActivityMainBinding
    private val connectionStatus = ConnectionStatus()
    private var snackbar: Snackbar? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        binding = ActivityMainBinding.inflate(layoutInflater)
        setContentView(binding.root)
    }

    override fun onPostResume() {
        super.onPostResume()
        registerReceiver(connectionStatus, IntentFilter(ConnectivityManager.CONNECTIVITY_ACTION))
        ConnectionStatus.connectivityReceiverListener = this
    }

    override fun onDestroy() {
        super.onDestroy()
        unregisterReceiver(connectionStatus)
    }

    override fun onNetworkConnectionChanged(isConnect: Boolean) {
        if (!isConnect) {
            snackbar = Snackbar.make(
                findViewById(R.id.mainCL),
                getString(R.string.no_internet_connection),
                Snackbar.LENGTH_INDEFINITE
            )
                .setAction(getString(R.string.turn_wifi_on)) {
                    startActivity(Intent(Settings.ACTION_WIFI_SETTINGS))
                }
            snackbar?.show()
        } else {
            snackbar?.dismiss()
        }
    }

}
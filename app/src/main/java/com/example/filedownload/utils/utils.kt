package com.example.filedownload.utils

import android.content.Context
import android.content.DialogInterface
import android.content.Intent
import android.net.Uri
import android.provider.Settings
import android.view.LayoutInflater
import android.widget.Button
import android.widget.Toast
import androidx.core.content.ContextCompat.startActivity
import com.example.filedownload.R
import com.google.android.material.dialog.MaterialAlertDialogBuilder

var downloadUrl = "https://shotstack-assets.s3-ap-southeast-2.amazonaws.com/footage/earth.mp4"

fun Context.openSettingsDialog() {
    try {
        val dialog = MaterialAlertDialogBuilder(this).create()
        val view = LayoutInflater.from(this).inflate(R.layout.dialog_enable_video_permissions, null)
        view?.apply {
            findViewById<Button>(R.id.dialog_btn_cancel).setOnClickListener {
                dialog.dismiss()
            }
            findViewById<Button>(R.id.dialog_btn_go_to_settings).setOnClickListener {
                val intent = createAppSettingsIntent(this.context)
                startActivity(intent)
                dialog.dismiss()
            }
        }
        dialog.setView(view)
        dialog.show()
    } catch (e: Exception) { }
}

private fun createAppSettingsIntent(context: Context) = Intent().apply {
    action = Settings.ACTION_APPLICATION_DETAILS_SETTINGS
    data = Uri.fromParts("package", context.packageName, null)
}
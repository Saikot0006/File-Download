package com.example.filedownload.broadcastReceiver

import android.app.DownloadManager
import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.util.Log

class DownloadBroadcastReceiver : BroadcastReceiver() {

    interface DownloadListener {
        fun onDownloadComplete(downloadId: Long)
    }

    var downloadListener: DownloadListener? = null

    override fun onReceive(p0: Context?, p1: Intent?) {
        if(p1?.action == "android.intent.action.DOWNLOAD_COMPLETE"){
            val downloadId = p1.getLongExtra(DownloadManager.EXTRA_DOWNLOAD_ID,-1L)
            Log.d("downloadCompleteID", "setUpObserver: $downloadId")
            if(downloadId!=-1L){
                //Toast.makeText(p0, "Download Completed.", Toast.LENGTH_SHORT).show()
                downloadListener?.onDownloadComplete(downloadId)
            }
        }
    }
}
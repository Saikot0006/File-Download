package com.example.filedownload.ui

import android.Manifest
import android.app.DownloadManager
import android.app.NotificationChannel
import android.app.NotificationManager
import android.content.Context
import android.content.pm.PackageManager
import android.database.Cursor
import android.net.Uri
import android.os.Build
import android.os.Environment
import android.util.Log
import androidx.annotation.RequiresApi
import androidx.core.app.ActivityCompat
import androidx.core.app.NotificationCompat
import androidx.core.app.NotificationManagerCompat
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import com.example.filedownload.R
import com.example.filedownload.repository.DownloadRepository
import java.io.File

class DownloadViewModel : ViewModel(){
    private lateinit var repository: DownloadRepository

    private val _isDownloadFile: MutableLiveData<String?> = MutableLiveData<String?>()
    val isDownloadFile: LiveData<String?> = _isDownloadFile

    private val _downloadId : MutableLiveData<Long> = MutableLiveData<Long>()
    val downloadId : LiveData<Long> = _downloadId

    private val _downloadStatus: MutableLiveData<String?> = MutableLiveData<String?>()
    val downloadStatus: LiveData<String?> = _downloadStatus

    private val _progressStatus: MutableLiveData<Int> = MutableLiveData<Int>()
    val progressStatus: LiveData<Int> = _progressStatus

    init {
        repository = DownloadRepository()
    }

    fun downloadFile(context: Context) {
        try {
            val fileName = repository.getUrl().split("/").last()
            _isDownloadFile.value = "Downloading $fileName"

            // Create a download request
            val request = DownloadManager.Request(Uri.parse(repository.getUrl()))
                .setTitle(fileName)
                .setDescription("Downloading")
                .setAllowedNetworkTypes(
                    DownloadManager.Request.NETWORK_WIFI or DownloadManager.Request.NETWORK_MOBILE
                )
                .setNotificationVisibility(DownloadManager.Request.VISIBILITY_VISIBLE_NOTIFY_COMPLETED)
                .setDestinationInExternalPublicDir(
                    Environment.DIRECTORY_DOWNLOADS,
                    "CMED_Health/${System.currentTimeMillis()}_$fileName" // Provide the full path including the file name
                )
                .setAllowedOverMetered(true)
                .setAllowedOverRoaming(true)

            val downloadManager = context.getSystemService(Context.DOWNLOAD_SERVICE) as DownloadManager
            val downloadId = downloadManager.enqueue(request)
            _downloadId.value = downloadId

        } catch (e: Exception) {
            e.printStackTrace()
            _downloadId.value = 1L
            _isDownloadFile.value = "Download fail"
        }
    }

    fun queryDownloadProgress(downloadManager: DownloadManager, downloadId : Long) {
        try {
            val cursor = downloadManager.query(DownloadManager.Query().setFilterById(downloadId))
            Log.d("downloadCompleteID", "setUpObserver: downloadCompleteID")

            if (cursor != null && cursor.moveToFirst()) {
                val status = cursor.getInt(cursor.getColumnIndex(DownloadManager.COLUMN_STATUS) ?:0)

                Log.d("downloadCompleteID", "setUpObserver: downloadCompleteID $status")
                when (status) {
                    DownloadManager.STATUS_FAILED -> {
                        _downloadStatus.value = "Download Failed"
                    }
                    DownloadManager.STATUS_PAUSED -> {
                        _downloadStatus.value = "Download Paused"
                    }
                    DownloadManager.STATUS_PENDING -> {
                        _downloadStatus.value = "Download Pending"
                        _progressStatus.value = 0
                    }
                    DownloadManager.STATUS_RUNNING -> {
                        try {
                            val totalSize = cursor.getLong(cursor.getColumnIndex(DownloadManager.COLUMN_TOTAL_SIZE_BYTES) ?: 0)
                            val downloaded = cursor.getLong(cursor.getColumnIndex(DownloadManager.COLUMN_BYTES_DOWNLOADED_SO_FAR) ?: 0)

                            if (totalSize > 0) {
                                val progress = (downloaded * 100L / totalSize).toInt()
                                _progressStatus.value = progress
                            }
                        }catch (e:Exception){
                            Log.d("downloadCompleteID", "Completed STATUS_RUNNING${e.localizedMessage}")
                        }
                    }
                    DownloadManager.STATUS_SUCCESSFUL -> {
                        try {
                            Log.d("downloadCompleteID", "Completed")
                            _downloadStatus.value =  "Download Completed"
                            _progressStatus.value = 100
                        }catch (e:Exception){
                            Log.d("downloadCompleteID", "Completed ${e.localizedMessage}")
                        }

                    }
                }
            } else {
                _downloadStatus.value = "No data available for the download"
            }
            cursor?.close()
        } catch (e: Exception) {
            e.printStackTrace()
        }
    }

}
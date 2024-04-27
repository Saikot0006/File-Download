package com.example.filedownload.ui

import android.Manifest
import android.app.DownloadManager
import android.content.Context
import android.content.IntentFilter
import android.content.pm.PackageManager
import android.os.Build
import android.os.Bundle
import android.os.Environment
import android.util.Log
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.annotation.RequiresApi
import androidx.core.content.ContextCompat
import androidx.fragment.app.viewModels
import com.example.filedownload.R
import com.example.filedownload.broadcastReceiver.DownloadBroadcastReceiver
import com.example.filedownload.databinding.FragmentDownloadBinding
import com.example.filedownload.utils.openSettingsDialog
import com.fondesa.kpermissions.allDenied
import com.fondesa.kpermissions.allGranted
import com.fondesa.kpermissions.allPermanentlyDenied
import com.fondesa.kpermissions.anyDenied
import com.fondesa.kpermissions.anyPermanentlyDenied
import com.fondesa.kpermissions.extension.permissionsBuilder
import com.fondesa.kpermissions.extension.send
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext

class DownloadFragment : Fragment(), DownloadBroadcastReceiver.DownloadListener {

    private val downloadViewModel : DownloadViewModel by viewModels()
    private lateinit var binding: FragmentDownloadBinding
    val REQUEST_CODE = 200
    private var permissionGranted = false
    private val downloadBroadcastReceiver = DownloadBroadcastReceiver()
    private var id: Long = -1
    private var downloadManager: DownloadManager? = null


    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        // Inflate the layout for this fragment
        binding = FragmentDownloadBinding.inflate(layoutInflater,container,false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        initView()
        askForPermission()
        onClick()
        setUpObserver()
    }

    private fun initView() {
        downloadManager = context?.getSystemService(Context.DOWNLOAD_SERVICE) as DownloadManager
    }

    private fun onClick() {
        binding.downloadBtn.setOnClickListener {
            if(askForPermission()){
                if(Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU){
                    context?.registerReceiver(downloadBroadcastReceiver, IntentFilter(DownloadManager.ACTION_DOWNLOAD_COMPLETE), Context.RECEIVER_NOT_EXPORTED)
                }else{
                    context?.registerReceiver(downloadBroadcastReceiver, IntentFilter(DownloadManager.ACTION_DOWNLOAD_COMPLETE))
                }
                downloadViewModel.downloadFile(requireContext())
                binding.progressbarCL.visibility = View.VISIBLE
                binding.progressBar.progress = 0
                binding.progressBarValueTV.text = "0%"
            }
        }
    }

    private fun progressStatus(downloadId:Long) {
        try {
            val cursor = downloadManager?.query(DownloadManager.Query().setFilterById(downloadId))
            if(cursor != null && cursor.moveToFirst()){
                val totalSize = cursor.getLong(cursor.getColumnIndex(DownloadManager.COLUMN_TOTAL_SIZE_BYTES) ?: 0)
                val downloaded = cursor.getLong(cursor.getColumnIndex(DownloadManager.COLUMN_BYTES_DOWNLOADED_SO_FAR) ?: 0)

                Log.d("progress", "progressStatus: $totalSize $downloaded")

                if (totalSize > 0) {
                    val progress = (downloaded * 100L / totalSize).toInt()
                    binding.progressBar.progress = progress
                    binding.progressBarValueTV.text = progress.toString()
                    Log.d("progress", "progressStatus: ${progress.toString()}")
                }
            }

        }catch (e:Exception){
            Log.d("downloadCompleteID", "Completed STATUS_RUNNING${e.localizedMessage}")
        }
    }

    private fun setUpObserver() {
        downloadViewModel.apply {
            isDownloadFile.observe(viewLifecycleOwner){
                if(!it.isNullOrEmpty()){
                    Toast.makeText(requireContext(), "$it", Toast.LENGTH_SHORT).show()
                }
            }

            downloadStatus.observe(viewLifecycleOwner){
                Toast.makeText(context, "$it", Toast.LENGTH_SHORT).show()
            }

            downloadId.observe(viewLifecycleOwner){
                id = it
            }

            progressStatus.observe(viewLifecycleOwner){
                binding.progressBar.progress = it
                binding.progressBarValueTV.text = it.toString()+"%"
            }
        }
    }

    private fun askForPermission(): Boolean {

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            permissionsBuilder(
                Manifest.permission.READ_MEDIA_IMAGES,
                Manifest.permission.READ_MEDIA_AUDIO,
                Manifest.permission.READ_MEDIA_VIDEO,
                Manifest.permission.POST_NOTIFICATIONS
            ).build()
                .send { result ->
                    when {
                        result.allGranted() -> {
                            permissionGranted = true
                        }

                        result.allDenied() || result.anyDenied() -> {
                            context?.openSettingsDialog()
                        }

                        result.allPermanentlyDenied() || result.anyPermanentlyDenied() -> {
                            context?.openSettingsDialog()
                        }
                    }
                }
            return permissionGranted

        } else if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
            permissionsBuilder(
                Manifest.permission.READ_EXTERNAL_STORAGE,
                Manifest.permission.WRITE_EXTERNAL_STORAGE
            ).build()
                .send { result ->
                    when {
                        result.allGranted() -> {
                            permissionGranted = true
                        }

                        result.allDenied() || result.anyDenied() -> {
                            context?.openSettingsDialog()
                        }

                        result.allPermanentlyDenied() || result.anyPermanentlyDenied() -> {
                            context?.openSettingsDialog()
                        }
                    }
                }
            return permissionGranted

        } else {
            permissionsBuilder(
                Manifest.permission.WRITE_EXTERNAL_STORAGE,
                Manifest.permission.READ_EXTERNAL_STORAGE
            ).build()
                .send { result ->
                    when {
                        result.allGranted() -> {
                            permissionGranted = true
                        }

                        result.allDenied() || result.anyDenied() -> {
                            context?.openSettingsDialog()
                        }

                        result.allPermanentlyDenied() || result.anyPermanentlyDenied() -> {
                            context?.openSettingsDialog()
                        }
                    }
                }
            return permissionGranted
        }


    }

    override fun onRequestPermissionsResult(
        requestCode: Int,
        permissions: Array<out String>,
        grantResults: IntArray
    ) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults)
        if (requestCode == REQUEST_CODE) {
            permissionGranted = grantResults[0] == PackageManager.PERMISSION_GRANTED
        }
    }

    override fun onResume() {
        super.onResume()
        downloadBroadcastReceiver.downloadListener = this
    }

    override fun onDestroyView() {
        super.onDestroyView()
        context?.unregisterReceiver(downloadBroadcastReceiver)
    }

    override fun onDownloadComplete(downloadId: Long) {
        if(downloadId==id){

            downloadManager?.let { downloadViewModel.queryDownloadProgress(it,downloadId) }
            context?.unregisterReceiver(downloadBroadcastReceiver)
        }
    }

}
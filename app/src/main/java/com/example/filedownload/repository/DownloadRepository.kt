package com.example.filedownload.repository

import com.example.filedownload.utils.downloadUrl
import java.lang.Exception

class DownloadRepository {

    fun getUrl():String{
        if(downloadUrl.isEmpty()){
            throw Exception("Url is empty")
        }else{
            return downloadUrl.trim()
        }
    }

}
package com.bq.comicviewer

import android.app.Application
import android.preference.PreferenceManager
import com.bq.androidx.tool.ImgListDowloadExecutor
import com.bq.androidx.tool.commonExecutor

class App : Application() {

    companion object {
        var maxImgListDownloadThread = 5

        var diskLruCacheDir = System.getProperty("java.io.tmpdir")!! + "/disk-lru-cache"

        var diskCacheMaxSize = 1024 * 1024 * 200L // byte

        lateinit var context: Application
    }

    override fun onCreate() {
        super.onCreate()
        context = this
        val sharedPreferencee = PreferenceManager.getDefaultSharedPreferences(this)
        maxImgListDownloadThread = sharedPreferencee.getInt("maxImgListDownloadThread", 5)
        diskLruCacheDir = sharedPreferencee.getString("diskLruCacheDir",
                System.getProperty("java.io.tmpdir")!! + "/disk-lru-cache")!!
    }

    override fun onTerminate() {
        try {
            try {
                commonExecutor.shutdown()
            } finally {
                ImgListDowloadExecutor.executorService.shutdown()
            }
        } finally {
            super.onTerminate()
        }

    }
}
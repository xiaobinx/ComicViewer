package com.bq.comicviewer

import android.app.Application
import android.preference.PreferenceManager
import com.bq.androidx.tool.commonExecutor
import com.bq.androidx.tool.executorService
import com.bq.kotlinx.cache.DiskCache


class App : Application() {
    companion object {
        /**
         * 全局可用的context
         */
        private lateinit var bContext: Application
        val context get() = bContext // 避免手贱改bContext

        /**
         * 图片列表下载最大线程数
         */
        var maxImgListDownloadThread = 5

        /**
         * 硬盘缓存相关
         */
        var diskCacheDir = System.getProperty("java.io.tmpdir")!! + "/disk-lru-cache"
        var diskCacheVersion = 1
        var diskCacheMaxSize = 1024 * 1024 * 200L // byte
        lateinit var diskCache: DiskCache
    }

    override fun onCreate() {
        super.onCreate()
        bContext = this
        val sharedPreferencee = PreferenceManager.getDefaultSharedPreferences(this)
        maxImgListDownloadThread = sharedPreferencee.getInt("maxImgListDownloadThread", 5)
        diskCacheDir = sharedPreferencee.getString(
            "diskLruCacheDir",
            System.getProperty("java.io.tmpdir")!! + "/disk-lru-cache"
        )!!

        diskCache = DiskCache(diskCacheDir, diskCacheVersion, diskCacheMaxSize)
    }

    override fun onTerminate() {
        try {
            try {
                commonExecutor.shutdown()
            } finally {
                executorService.shutdown()
            }
        } finally {
            super.onTerminate()
        }

    }
}
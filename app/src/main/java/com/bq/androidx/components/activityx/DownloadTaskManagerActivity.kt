package com.bq.androidx.components.activityx

import android.annotation.SuppressLint
import androidx.appcompat.app.AppCompatActivity
import com.bq.androidx.http.imglistloader.BitmapCacheLoader
import com.bq.androidx.http.imglistloader.SimpleBitmapListLoader
import java.util.*

@SuppressLint("Registered")
abstract class DownloadTaskManagerActivity : AppCompatActivity() {

    abstract val imgBitmapListLoader: SimpleBitmapListLoader

    /**
     * 只在主线程中操作downloadTasks 避免线程安全问题
     */
    private var canceledTasks = LinkedList<BitmapCacheLoader>()

    /**
     * 重新执行被取消的任务
     */
    override fun onResume() {
        super.onResume()
        if (canceledTasks.size < 1) return
        val it = canceledTasks.iterator()
        while (it.hasNext()) {
            val task = it.next()
            if (task.future?.isCancelled == true) {
                task.load()
            }
        }
        canceledTasks.clear()
    }

    /**
     * 添加未下载完成的任务
     */
    override fun onPause() {
        try {
            imgBitmapListLoader.cancelTaskAndAddUndoneTo(canceledTasks)
        } finally {
            super.onPause()
        }
    }

    /**
     * 清空所有下载任务
     */
    override fun finish() {
        try {
            canceledTasks.clear()
        } finally {
            super.finish()
        }
    }
}
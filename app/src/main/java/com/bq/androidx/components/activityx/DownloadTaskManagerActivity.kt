package com.bq.androidx.components.activityx

import android.annotation.SuppressLint
import androidx.appcompat.app.AppCompatActivity
import com.bq.androidx.http.Task
import com.bq.androidx.http.BitmapListLoader
import java.util.*

@SuppressLint("Registered")
abstract class DownloadTaskManagerActivity : AppCompatActivity() {

    abstract val bitmapListLoader: BitmapListLoader

    /**
     * 重新执行被取消的任务
     */
    override fun onResume() {
        super.onResume()
        bitmapListLoader.onResume()
    }

    /**
     * 添加未下载完成的任务
     */
    override fun onPause() {
        try {
            bitmapListLoader.onPause()
        } finally {
            super.onPause()
        }
    }

    /**
     * 清空所有下载任务
     */
    override fun finish() {
        try {
            bitmapListLoader.finish()
        } finally {
            super.finish()
        }
    }
}
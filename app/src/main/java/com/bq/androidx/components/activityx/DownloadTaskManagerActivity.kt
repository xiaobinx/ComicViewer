package com.bq.androidx.components.activityx

import android.annotation.SuppressLint
import android.app.Activity
import com.bq.androidx.tool.DownloadTask
import com.bq.androidx.tool.ImgListDowloadExecutor
import java.util.*

@SuppressLint("Registered")
open class DownloadTaskManagerActivity : Activity() {

    /**
     * 只在主线程中操作downloadTasks 避免线程安全问题
     */
    private var downloadTasks = LinkedList<DownloadTask>()

    private var clearTime = System.currentTimeMillis()

    fun addDownloadTask(task: DownloadTask) {
        downloadTasks.add(task)
        clear()
    }

    fun clear() {
        val now = System.currentTimeMillis()
        if (now - clearTime < 500) return
        val iterator = downloadTasks.iterator()
        while (iterator.hasNext()) {
            val task = iterator.next()
            if (task.isDone()) {
                iterator.remove()
            }
        }
        clearTime = now
    }

    /**
     * 重新执行被取消的任务
     */
    override fun onResume() {
        super.onResume()
        if (downloadTasks.size < 1) return
        val newList = LinkedList<DownloadTask>()
        val it = downloadTasks.iterator()
        while (it.hasNext()) {
            val task = it.next()
            if (task.isCancelled()) {
                ImgListDowloadExecutor.submit(task.url, task.action)?.let {
                    newList.add(it)
                }
            }
        }
        downloadTasks = newList
    }

    /**
     * 移除已经完成的任务
     */
    override fun onPause() {
        try {
            val it = downloadTasks.iterator()
            while (it.hasNext()) {
                val task = it.next()
                if (task.isDone()) {
                    it.remove()
                } else {
                    task.cancel(false)
                }
            }
        } finally {
            super.onPause()
        }
    }

    /**
     * 清空所有下载任务
     */
    override fun finish() {
        try {
            downloadTasks.forEach { it.cancel(false) }
            downloadTasks.clear()
        } finally {
            super.finish()
        }
    }
}
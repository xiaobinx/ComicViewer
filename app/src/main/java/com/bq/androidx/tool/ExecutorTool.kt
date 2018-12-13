package com.bq.androidx.tool

import com.bq.comicviewer.App
import java.util.*
import java.util.concurrent.*

/**
 * 通用任务处理线程池，多数耗时操作都能使用
 */
val commonExecutor by lazy {
    ThreadPoolExecutor(
            0,
            5,
            10L,
            TimeUnit.SECONDS,
            LinkedBlockingQueue(),
            ThreadFactory {
                Thread(it, "imgListDowloadExecutor-thread").apply { this.isDaemon = false }
            }
    )
}

object ImgListDowloadExecutor {
    /* 用来保存正在下载的任务 */
    private val map = HashMap<String, DownloadTask>()

    val executorService by lazy {
        ThreadPoolExecutor(
                0,
                App.maxImgListDownloadThread,
                60L,
                TimeUnit.SECONDS,
                LinkedBlockingQueue(),
                ThreadFactory {
                    Thread(it, "imgListDowloadExecutor-thread").apply { this.isDaemon = false }
                }
        )
    }

    @Synchronized
    fun submit(url: String, action: () -> Unit): DownloadTask? {
        var task: DownloadTask? = null
        if (!map.containsKey(url)) {// 这里放弃的重复的下载任务
            task = DownloadTask(url, action)
            task.future = executorService.submit(task)
            map[url] = task
        }
        return task
    }

    @Synchronized
    fun removeTask(url: String) {
        map.remove(url)
    }

}

/**
 * 一个正在执行的下载任务
 */
class DownloadTask(val url: String, val action: () -> Unit, var future: Future<*>? = null) : Runnable {

    fun isDone(): Boolean = future?.isDone ?: false

    fun isCancelled(): Boolean = future?.isCancelled ?: false

    fun cancel(mayInterruptIfRunning: Boolean): Boolean {
        try {
            return future?.cancel(mayInterruptIfRunning) ?: false
        } finally {
            ImgListDowloadExecutor.removeTask(url)
        }
    }

    override fun run() {
        try {
            action()
        } finally {
            ImgListDowloadExecutor.removeTask(url)
        }
    }
}
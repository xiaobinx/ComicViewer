package com.bq.androidx.tool

import com.bq.comicviewer.App
import java.util.concurrent.LinkedBlockingQueue
import java.util.concurrent.ThreadFactory
import java.util.concurrent.ThreadPoolExecutor
import java.util.concurrent.TimeUnit

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
            Thread(it, "commonExecutor-thread").apply { this.isDaemon = false }
        }
    )
}

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

package com.bq.androidx.http

import android.graphics.Bitmap
import android.util.Log
import com.bq.androidx.tool.MainHandler
import com.bq.androidx.tool.bitmap.decodeStream
import com.bq.androidx.tool.commonExecutor
import com.bq.androidx.tool.executorService
import com.bq.comicviewer.App
import com.bq.kotlinx.md5
import com.bq.kotlinx.readBytesThenClose
import com.bq.kotlinx.toHexString
import java.lang.ref.SoftReference
import java.util.*
import java.util.concurrent.Future

enum class Status {

    /**
     * 正在准备
     */
    PREPARING,
    /**
     * 正在加载
     */
    LOADING,
    /**
     * 正在下载
     */
    DOWN_LOADING,
    /**
     * 加载完成
     */
    DONE,
    /**
     * 加载发生错误
     */
    ERROR,
    /**
     * 下载被取消
     */
    CANCELED
}

/**
 * 异步下载文件并可设置使用内存及硬盘两种缓存，分别使用imgListDowloadExecutor, imgLoadExecutor下载和加载的图片，用于下载长列表中的图片
 *
 * @param pixelW 从图片文件加载到bitmap时使用的压缩参数，两个压缩参数默认为0，当且仅当两个参数都大于0的时候才会压缩加载图片
 * @param pixelH 从图片文件加载到bitmap时使用的压缩参数，两个压缩参数默认为0，当且仅当两个参数都大于0的时候才会压缩加载图片
 * @param useMeCache 是否使用内存缓存
 * @param useDiskCache 是否使用硬盘缓存
 * @return 返回正在执行或将要执行任务的Future
 */
@Suppress("UNNECESSARY_NOT_NULL_ASSERTION")
class Task(
    private val url: String,
    private val pixelW: Int = 0,
    private val pixelH: Int = 0,
    private val useMeCache: Boolean = true,
    private val useDiskCache: Boolean = true,
    action: (Bitmap) -> Unit
) {
    val tag = javaClass.name!!

    private val executor by lazy { HttpExecutor(url) }

    private val urlMd5 by lazy { url.md5().toHexString() }

    private val bmkey by lazy { "$pixelW-$pixelW-$urlMd5" }

    private var future: Future<*>? = null

    var status = Status.PREPARING
        private set

    var action: ((Bitmap) -> Unit) = action
        @Synchronized get
        @Synchronized set(value) {
            field = value
            if (status == Status.DONE || status == Status.ERROR || status == Status.CANCELED) load()
        } // end set

    init {
        if (!(useMeCache || useDiskCache)) {
            throw Exception("必须有中一个缓存选项为true，否则不应该用这个类加载图片")
        }
    }

    /**
     * 若下载任务还没开始就取消
     */
    fun cancel() {
        if (future?.cancel(false) == true) {
            status = Status.CANCELED
        }
    }

    /**
     * 下载任务是否被取消
     */
    val isCancelled: Boolean get() = future?.isCancelled ?: false

    fun load() {
        status = Status.LOADING
        // 1.内存缓存中取
        if (useMeCache) {
            App.bimapCache.get(bmkey)?.let {
                status = Status.DONE
                MainHandler.runOnUiThread { action(it) }
                return
            }
        }

        // 2.硬盘缓存中取
        if (useDiskCache) {
            App.diskCache.get(urlMd5)?.let {
                commonExecutor.submit {
                    try {
                        val bm = decodeStream(it.inputStream.readBytesThenClose(), pixelW, pixelH)
                        if (useMeCache) App.bimapCache.put(bmkey, bm)
                        status = Status.DONE
                        MainHandler.runOnUiThread { action(bm) }
                    } catch (e: Throwable) {
                        status = Status.ERROR
                        Log.e(tag, "加载Bitmap snapshot: $it, url: $url, ${e.message}", e)
                    } // end try catch
                } // end commonExecutor.submit
                return  // load return
            } // end DiskCache.get(dcKey) let
        } // end if (readDiskCache)

        // 3. 下载
        download()
    }// end fun load

    fun prepareDownload() {
        status = Status.LOADING
        if (!useDiskCache) throw RuntimeException("必须启用硬盘缓存")
        if (isCached()) return // 硬盘缓存已经完成下载

        download()
    }

    fun isCached() = App.diskCache.contains(urlMd5)

    private fun download() {
        future = executorService.submit {
            try {
                status = Status.DOWN_LOADING
                val bytes = executor.get()?.body()?.byteStream()?.let {
                    val bytes = it.readBytesThenClose()
                    if (bytes.size < 0) throw Exception("获取请求体发生错误")
                    if (useDiskCache) App.diskCache.put(urlMd5, bytes)
                    bytes
                } // end byteStream let
                if (null == bytes) {
                    throw Exception("获取请求体发生错误")
                } else {
                    val bm = decodeStream(bytes, pixelW, pixelH)
                    if (useMeCache) App.bimapCache.put(bmkey, bm)
                    status = Status.DONE
                    MainHandler.runOnUiThread { action(bm) }
                }// end else
            } catch (e: Throwable) {
                status = Status.ERROR
                Log.e(tag, "加载Bitmap发生错误url: $url, ${e.message}", e)
            }
        }
    }

}

/**
 * 管理一类图片列表的下载
 */
class BitmapListLoader(
    private val pixelW: Int = 0,
    private val pixelH: Int = 0,
    private val useMeCache: Boolean = true,
    private val useDiskCache: Boolean = true
) {

    private val container = HashMap<String, SoftReference<Task>>()

    private var canceledTasks = LinkedList<Task>()


    /**
     * 加载图片
     */
    @Synchronized
    fun load(url: String, action: (Bitmap) -> Unit) {
        var task = container[url]?.get()
        if (null == task) {
            task = Task(url, pixelW, pixelH, useMeCache, useDiskCache, action)
            container[url] = SoftReference(task)
            task.load()
        } else {
            task.action = action
        }
    }


    /**
     * 预加载图片
     */
    @Synchronized
    fun load(url: String) {
        var task = container[url]?.get()
        if (null == task) {
            task = Task(url, pixelW, pixelH, useMeCache, useDiskCache) {}
            container[url] = SoftReference(task)
            task.prepareDownload()
        }
    }

    /**
     * 暂停下载,取消所有未完成的任务，并添加到列表list中
     */
    fun onPause() {
        container.entries.forEach {
            val loader = it.value.get()
            if (null != loader && !loader.isCancelled) {
                loader.cancel()
                if (loader.isCancelled) {
                    canceledTasks.add(loader)
                }
            } // end ifElse
        }// end entries.forEach
    }

    /**
     * 清空所有下载任务
     */
    fun finish() {
        container.values.forEach {
            it.get()?.let { task ->
                task.cancel()
            }
        }
    }

    /**
     * 重新执行被取消的任务
     */
    fun onResume() {
        if (canceledTasks.size < 1) return
        val it = canceledTasks.iterator()
        while (it.hasNext()) {
            val task = it.next()
            if (task.isCancelled) {
                task.load()
            }
        }
        canceledTasks.clear()
    }

    @Synchronized
    fun cancel(coverUrl: String) {
        container[coverUrl]?.get()?.cancel()
    }

    fun isDoing(url: String): Boolean {
        val task = container[url]?.get()
        return task?.status == Status.DOWN_LOADING || task?.isCached() ?: false
    }
}
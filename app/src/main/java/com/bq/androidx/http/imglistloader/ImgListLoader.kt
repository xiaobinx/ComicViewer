package com.bq.androidx.http.imglistloader

import android.graphics.Bitmap
import android.util.Log
import com.bq.androidx.http.HttpExecutor
import com.bq.androidx.tool.bimapCache
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

/**
 * 正在准备
 */
const val PREPARING = 2
/**
 * 正在加载
 */
const val LOADING = 1
/**
 * 加载完成
 */
const val DONE = 0
/**
 * 加载发生错误
 */
const val ERROR = -1

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
class BitmapCacheLoader(
    val url: String,
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

    var future: Future<*>? = null
        private set

    var status = PREPARING
        private set

    var action: ((Bitmap) -> Unit) = action
        @Synchronized get
        @Synchronized set(value) {
            field = value
            when (status) {
                DONE, ERROR -> load()
                LOADING -> {
                    if (null == future || future?.isCancelled == true) {
                        load()
                    }
                }
            }
        }

    init {
        if (!(useMeCache || useDiskCache)) {
            throw Exception("必须有中一个缓存选项为true，否则不应该用这个类加载图片")
        }
        load()
    }

    fun load() {
        status = LOADING
        // 1.内存缓存中取
        if (useMeCache) {
            bimapCache.get(bmkey)?.let {
                try {
                    status = DONE
                    action(it)
                } catch (e: Throwable) {
                    Log.e(tag, "执行action发生错误: $it, url: $url, ${e.message}", e)
                } // end try catch
                return
            }
        }

        // 2.硬盘缓存中取
        if (useDiskCache) {
            App.diskCache.get(urlMd5)?.let {
                commonExecutor.submit {
                    try {
                        val bm = decodeStream(it.inputStream.readBytesThenClose(), pixelW, pixelH)
                        if (useMeCache) bimapCache.put(bmkey, bm)
                        status = DONE
                        action(bm)
                    } catch (e: Throwable) {
                        status = ERROR
                        Log.e(tag, "加载Bitmap snapshot: $it, url: $url, ${e.message}", e)
                    } // end try catch
                } // end commonExecutor.submit
                return  // load return
            } // end DiskCache.get(dcKey) let
        } // end if (readDiskCache)

        // 3. 下载
        future = executorService.submit {
            try {
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
                    if (useMeCache) bimapCache.put(bmkey, bm)
                    status = DONE
                    action(bm)
                }// end else
            } catch (e: Throwable) {
                status = ERROR
                Log.e(tag, "加载Bitmap发生错误url: $url, ${e.message}", e)
            } // end try catch
        }// end download runnable executorService.submit
    }// end fun load

}

/**
 * 管理一类图片列表的下载
 */
class SimpleBitmapListLoader(
    private val pixelW: Int = 0,
    private val pixelH: Int = 0,
    private val useMeCache: Boolean = true,
    private val useDiskCache: Boolean = true
) {
    private val container = HashMap<String, SoftReference<BitmapCacheLoader>>()

    @Synchronized
    fun load(url: String, action: (Bitmap) -> Unit) {
        var loader = container[url]?.get()
        if (null == loader) {
            loader = BitmapCacheLoader(url, pixelW, pixelH, useMeCache, useDiskCache, action)
            container[url] = SoftReference(loader)
        } else {
            loader.action = action
        }
    }

    val tag = javaClass.name

    /**
     * 取消所有未完成的任务，并添加到列表list中
     */
    @Synchronized
    fun cancelTaskAndAddUndoneTo(list: LinkedList<BitmapCacheLoader>) {
        container.entries.forEach {
            val loader = it.value.get()
            val future = loader?.future
            if (null != loader && null != future && !future.isDone) {
                future.cancel(false)
                if (future.isCancelled) {
                    list.add(loader)
                }
            } // end ifElse
        }// end entries.forEach
    }// end cancelTaskAndAddUndoneTo
}
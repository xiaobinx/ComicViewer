package com.bq.androidx.http

import android.graphics.Bitmap
import android.util.Log
import com.bq.androidx.tool.DownloadTask
import com.bq.androidx.tool.ImgListDowloadExecutor
import com.bq.androidx.tool.bimapCache
import com.bq.androidx.tool.bitmap.decodeStream
import com.bq.androidx.tool.commonExecutor
import com.bq.comicviewer.App.Companion.diskCache
import com.bq.kotlinx.md5
import com.bq.kotlinx.readBytesThenClose
import com.bq.kotlinx.toHexString
import okhttp3.Call
import okhttp3.OkHttpClient
import okhttp3.Request
import okhttp3.Response
import java.util.concurrent.TimeUnit

private val t_client = OkHttpClient.Builder()
    .readTimeout(30, TimeUnit.SECONDS)
    .build()

/**
 * http工具，下载文件时缓存目录使用httpCacheDir
 *
 * @param url url
 * @param client 默认使用全局的_client，可以传入一个新的自定义
 */
class HttpExecutor(val url: String, private val client: OkHttpClient = t_client) {

    private val tag = javaClass.name

    private lateinit var onSuccess: (CallbackContext) -> Any
    private var onError: ((CallbackContext) -> Any)? = null
    private var doFinally: ((Any) -> Unit)? = null

    /**
     * doFinally的参数为onSuccess或者onError的返回值
     */
    fun doFinally(doFinally: (Any) -> Unit): HttpExecutor {
        this.doFinally = doFinally
        return this
    }

    fun error(onError: (CallbackContext) -> Any): HttpExecutor {
        this.onError = onError
        return this
    }

    fun success(onSuccess: (CallbackContext) -> Any): HttpExecutor {
        this.onSuccess = onSuccess
        return this
    }


    fun asyExec(onSuccess: (CallbackContext) -> Any): Call {
        success(onSuccess)
        return asyExec()
    }

    fun asyExec(): Call {
        if (!::onSuccess.isInitialized) {
            throw UninitializedPropertyAccessException("必须使用onSuccess方法设置成功后的回调函数")
        }
        val request = prepareCommonRequest()
        val call = client.newCall(request)
        call.enqueue(
            SimpleCallback(
                onSuccess,
                onError ?: {
                    Log.e(tag, "请求发生错误: ${it.e?.message}，url->${it.call!!.request().url()}", it.e)
                    "end"
                },
                doFinally ?: { }
            )
        )
        return call
    }

    private fun prepareCommonRequest(): Request {
        return Request.Builder().url(url).build()
    }

    /**
     * 同步get
     */
    fun get(): Response? {
        return try {
            val request = prepareCommonRequest()
            val response = client.newCall(request).execute()
            if (response.isSuccessful)
                response
            else {
                Log.e(tag, "请求发生错误url->$url，状态码->${response.code()}")
                null
            }
        } catch (e: Throwable) {
            Log.e(tag, "请求发生错误url->$url，异常信息->${e.message}", e)
            null
        }
    }

    /**
     * 异步get文本
     *
     * @param success 请求成功时的回调函数
     */
    fun asyGetText(success: (String) -> Unit): HttpExecutor {
        asyExec {
            success(it.response?.body()?.string() ?: throw Exception("请求成功，但请求体转成字符串的时候发生错误"))
        }
        return this
    }

    /**
     * 异步下载文件并同时使用内存及硬盘两种缓存，分别使用imgListDowloadExecutor, imgLoadExecutor下载和加载的图片，用于下载长列表中的图片，异步的返回bitmap
     * 这个方法会尝试避免重复下载，后添加的重复下载任务会被直接放弃，回调函数不会被执行
     * 执行这个方法 onSuccess onError doFinally都不会生效
     * onSuccess onError doFinally
     *
     * @param pixelW 从图片文件加载到bitmap时使用的压缩参数，两个压缩参数默认为0，当且仅当两个参数都大于0的时候才会压缩加载图片
     * @param pixelH 从图片文件加载到bitmap时使用的压缩参数，两个压缩参数默认为0，当且仅当两个参数都大于0的时候才会压缩加载图片
     * @param useMeCache 是否使用内存缓存
     * @param useDiskCache 是否使用硬盘缓存
     * @param success 请求成功时的回调函数，失败的时候不会抛出异常，也不会执行success方法，只会在控制台中打印错误的日志
     * @return 返回正在执行或将要执行任务的Future
     */
    fun asyLoadImgWithCache(
        pixelW: Int = 0,
        pixelH: Int = 0,
        useMeCache: Boolean = true,
        useDiskCache: Boolean = true,
        success: (Bitmap) -> Unit
    ): DownloadTask? {
        val urlMd5 = url.md5().toHexString() // 用作硬盘缓存 和 内存缓存的key
        // 1.内存缓存中取
        val bmkey = "$pixelW-$pixelW-$urlMd5" // 内存缓存 把缩放参数加上
        if (useMeCache) {
            val bm = bimapCache.get(bmkey)
            if (null != bm) {
                try {
                    success(bm)
                } catch (e: Throwable) {
                    Log.e(tag, "执行success 发生错误 url: $url", e)
                }
                return null
            }
        }

        // 2.硬盘缓存中取
        if (useDiskCache) {
            diskCache.get(urlMd5)?.let {
                commonExecutor.submit {
                    try {
                        val b = decodeStream(it.inputStream.readBytesThenClose(), pixelW, pixelH)
                        if (useMeCache) bimapCache.put(bmkey, b)
                        success(b)
                    } catch (e: Throwable) {
                        Log.e(tag, "加载Bitmap snapshot: $it, url: $url, ${e.message}", e)
                    } // end try catch
                } // end commonExecutor.submit
                return null // asyListImgLoad return
            } // end DiskCache.get(dcKey) let
        } // end if (readDiskCache)

        // 3. 下载
        return ImgListDowloadExecutor.submit(url) {
            try {
                val bytes = get()?.body()?.byteStream()?.let {
                    val bytes = it.readBytesThenClose()
                    if (bytes.size < 0) throw Exception("获取请求体发生错误")
                    if (useDiskCache) diskCache.put(urlMd5, bytes)
                    bytes
                } // end byteStream let
                if (null == bytes) {
                    throw Exception("获取请求体发生错误")
                } else {
                    val b = decodeStream(bytes, pixelW, pixelH)
                    if (useMeCache) bimapCache.put(bmkey, b)
                    success(b)
                }// end else
            } catch (e: Throwable) {
                Log.e(tag, "加载Bitmap发生错误url: $url, ${e.message}", e)
            } // end try catch
        }// end runnable
    }
}
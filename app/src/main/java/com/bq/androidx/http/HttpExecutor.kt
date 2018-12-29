package com.bq.androidx.http

import android.util.Log
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

}
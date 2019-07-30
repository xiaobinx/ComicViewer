package com.bq.androidx.http

import android.util.Log
import com.bq.androidx.tool.MainHandler
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
    val requestBuilder: Request.Builder = Request.Builder().url(url)

    var fOnSuccess: ((Call, Response) -> Unit)? = null
    var fOnError: ((Call, Exception) -> Unit)? = null
    var fDoFinally: (() -> Unit)? = null

    fun onSuccess(action: (Call, Response) -> Unit): HttpExecutor {
        fOnSuccess = action
        return this
    }

    fun success(action: (String) -> Unit): HttpExecutor {
        fOnSuccess = { _, response ->
            action(response.body?.string() ?: throw Exception("请求成功，但请求体转成字符串的时候发生错误"))
        }
        return this
    }

    fun onError(action: (Call, Exception) -> Unit): HttpExecutor {
        fOnError = action
        return this
    }

    fun onFinally(action: () -> Unit): HttpExecutor {
        fDoFinally = action
        return this
    }

    fun onFinallyInUiThread(action: () -> Unit): HttpExecutor {
        fDoFinally = {
            MainHandler.post(action)
        }
        return this
    }

    private fun prepareCommonRequest(): Request {
        return requestBuilder.build()
    }

    fun asyExec(): Call {
        fOnSuccess ?: throw NullPointerException("必须使用onSuccess方法设置成功后的回调函数")
        val request = prepareCommonRequest()
        val call = client.newCall(request)
        call.enqueue(
            SimpleCallback(
                fOnSuccess ?: throw Exception("没有设置OnSeccess函数"),
                fOnError ?: { c, e ->
                    Log.e(tag, "请求发生错误: ${e.message}，url->${c.request().url}", e)
                },
                fDoFinally ?: { }
            )
        )
        return call
    }

    /**
     * 同步get
     */
    fun get(): Response? {
        val request = prepareCommonRequest()
        val response = client.newCall(request).execute()
        if (!response.isSuccessful)
            throw Exception("请求发生错误url->$url，状态码->${response.code}")
        return response
    }

    /**
     * 异步get文本
     *
     * @param success 请求成功时的回调函数
     */
    fun asyGetText(action: (String) -> Unit): HttpExecutor {
        success(action)
        asyExec ()
        return this
    }

}
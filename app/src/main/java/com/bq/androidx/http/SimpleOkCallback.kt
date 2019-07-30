package com.bq.androidx.http

import android.util.Log
import okhttp3.Call
import okhttp3.Callback
import okhttp3.Response
import java.io.IOException

//
//interface OnSuccess {
//    fun success(call: Call, response: Response)
//}
//
//interface onError {
//    fun error(call: Call, e: Exception)
//}
//
//interface onError {
//    fun error(call: Call, e: Exception)
//}

class SimpleCallback(
    val onSuccess: (Call, Response) -> Unit,
    val onError: (Call, Exception) -> Unit,
    val doFinally: () -> Unit

) : Callback {

    override fun onFailure(call: Call, e: IOException) {
        try {
            onError(call, e)
        } catch (e1: Throwable) {
            Log.e("com.bq.androidx.http", "onError方法执行发生错误: ${e1.message}\nurl->${call.request().url}", e1)
        }
        try {
            doFinally()
        } catch (e2: Throwable) {
            Log.e("com.bq.androidx.http", "doFinally方法执行发生错误: ${e2.message}\nurl->${call.request().url}", e2)
        }
    }

    override fun onResponse(call: Call, response: Response) {
        val code = response.code
        if (200 == code || 206 == code) {
            try {
                onSuccess(call, response)
            } catch (e: Throwable) {
                Log.e("com.bq.androidx.http", "onSuccess方法执行发生错误: ${e.message}\nurl->${call.request().url}", e)
            }
        } else {
            try {
                onError(call, HttpCodeException(code, "HTTP请求发生错误返回状态码: $code"))
            } catch (e: Throwable) {
                Log.e("com.bq.androidx.http", "onError方法执行发生错误: ${e.message}\nurl->${call.request().url}", e)
            }
        }
        try {
            doFinally()
        } catch (e: Throwable) {
            Log.e("com.bq.androidx.http", "doFinally方法执行发生错误: ${e.message}\nurl->${call.request().url}", e)
        }
    }
}

class HttpCodeException(var code: Int, message: String) : Exception(message)

class CallbackContext(val call: Call? = null, val response: Response? = null, val e: Throwable? = null)
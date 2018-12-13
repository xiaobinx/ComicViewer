package com.bq.androidx.http

import android.util.Log
import okhttp3.Call
import okhttp3.Callback
import okhttp3.Response
import java.io.IOException

//class SimpleOkCallback(val succ: (Call, Response) -> Unit) : Callback {
//
//    private val tag = javaClass.name
//
//    override fun onFailure(call: Call, e: IOException) {
//        Log.e(tag, "请求发生错误: ${e.message}，url->${call.request().url()}", e)
//    }
//
//    override fun onResponse(call: Call, response: Response) {
//        val code = response.code()
//        if (200 == code || 206 == code) {
//            succ(call, response)
//        } else {
//            Log.e(tag, "请求发生错误,状态码: ${response.code()}，url->${call.request().url()}")
//        }
//    }
//
//}


class SimpleCallback(
        val onSuccess: (CallbackContext) -> Any,
        val onError: (CallbackContext) -> Any,
        val doFinally: (Any) -> Unit

) : Callback {

    override fun onFailure(call: Call, e: IOException) {
        val r: Any = try {
            onError(CallbackContext(call, null, e))
        } catch (e1: Throwable) {
            Log.e("com.bq.androidx.http", "onError方法执行发生错误: ${e1.message}\nurl->${call.request().url()}", e1)
            e1
        }
        try {
            doFinally(r)
        } catch (e2: Throwable) {
            Log.e("com.bq.androidx.http", "doFinally方法执行发生错误: ${e2.message}\nurl->${call.request().url()}", e2)
        }
    }

    override fun onResponse(call: Call, response: Response) {
        val cc = CallbackContext(call, response, null)
        val code = response.code()
        val a: Any = if (200 == code || 206 == code) {
            try {
                onSuccess(cc)
            } catch (e: Throwable) {
                Log.e("com.bq.androidx.http", "onSuccess方法执行发生错误: ${e.message}\nurl->${call.request().url()}", e)
                e
            }
        } else {
            try {
                onError(cc)
            } catch (e: Throwable) {
                Log.e("com.bq.androidx.http", "onError方法执行发生错误: ${e.message}\nurl->${call.request().url()}", e)
                e
            }
        }
        try {
            doFinally(a)
        } catch (e: Throwable) {
            Log.e("com.bq.androidx.http", "doFinally方法执行发生错误: ${e.message}\nurl->${call.request().url()}", e)
        }
    }
}

class CallbackContext(val call: Call? = null, val response: Response? = null, val e: Throwable? = null)
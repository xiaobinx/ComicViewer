package com.bq.androidx.tool

import android.os.Handler
import android.os.Looper
import com.bq.androidx.tool.MainHandler.post

object MainHandler : Handler(Looper.getMainLooper()) {

    fun runOnUiThread(action: () -> Unit) {
        if (Looper.myLooper() == Looper.getMainLooper()) {
            action()
        } else {
            post(action)
        }
    }
}
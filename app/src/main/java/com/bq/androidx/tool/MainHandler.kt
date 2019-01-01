package com.bq.androidx.tool

import android.os.Handler
import android.os.Looper

object MainHandler : Handler(Looper.getMainLooper()) {
    fun runOnUiThread(action: Runnable) {
        if (Looper.myLooper() == Looper.getMainLooper()) {
            action.run()
        } else {
            post(action)
        }
    }
}
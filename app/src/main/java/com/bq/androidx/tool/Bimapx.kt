package com.bq.androidx.tool

import android.graphics.Bitmap
import android.util.Log
import android.util.LruCache

class BimapCache {

    private val cache = object : LruCache<String, Bitmap>((Runtime.getRuntime().maxMemory() / 8).toInt()) {
        override fun sizeOf(key: String, value: Bitmap): Int {
            return value.byteCount
        }
    }

    fun put(key: String, bitmap: Bitmap): Bitmap? {
//        Log.d(
//            this.javaClass.name, "method PUT: $this,cache size: ${cache.size()}/${cache.maxSize()}" +
//                    ",put bitmap size: ${bitmap.byteCount}."
//        )
        return cache.put(key, bitmap)
    }

    fun get(key: String): Bitmap? {
        val bitmap = cache.get(key)
//        if (null == bitmap) {
//            Log.d(this.javaClass.name, "method GET: $this,cache size: ${cache.size()}/${cache.maxSize()}.");
//        } else {
//            Log.d(
//                this.javaClass.name, "method GET: $this,cache size: ${cache.size()}/${cache.maxSize()}," +
//                        "get bitmap size: ${bitmap.byteCount}."
//            )
//        }
        return bitmap
    }

    override fun toString(): String {
        return cache.toString()
    }
}
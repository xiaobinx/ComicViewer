package com.bq.androidx

import java.io.File

val httpCacheDir: String
    get() {
        println("执行~cacheDir")
        val path = System.getProperty("java.io.tmpdir") + "/http"
        val dir = File(path)
        if (!dir.exists()) {
            println("创建~cacheDir")
            dir.mkdirs()
        }
        return path
    }
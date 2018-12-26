package com.bq.kotlinx.cache

import com.bq.kotlinx.copyToThenClose
import com.jakewharton.disklrucache.DiskLruCache
import java.io.ByteArrayInputStream
import java.io.Closeable
import java.io.File
import java.io.InputStream

class DiskCache(
    private val cacheDir: String,
    private val appVersion: Int,
    private val diskCacheMaxSize: Long
) {

    init {
        val dir = File(cacheDir)
        if (!dir.exists()) {
            dir.mkdirs()
        }
    }

    private val cache by lazy {
        DiskLruCache.open(
            File(cacheDir),
            appVersion, 1,
            diskCacheMaxSize
        )
    }

    fun contains(key: String): Boolean {
        return cache.contains(key)
    }

    fun get(key: String): Snapshot? {
        return cache.get(key)?.let { SnapshotImpl(it) }
    }

//    fun put(key: String, inputStream: InputStream) {
//        val editor = cache.edit(key)
//        inputStream.copyToThenClose(editor.newOutputStream(0))
//        editor.commit()
//        cache.flush()
//    }

    fun put(key: String, bytes: ByteArray) {
        val editor = cache.edit(key)
        ByteArrayInputStream(bytes).copyToThenClose(editor.newOutputStream(0))
        editor.commit()
        cache.flush()
    }

}

interface Snapshot : Closeable {
    val inputStream: InputStream
    val length: Long
    override fun close() {
        inputStream.close()
    }
}

class SnapshotImpl(snapshot: DiskLruCache.Snapshot) : Snapshot {
    override val inputStream: InputStream = snapshot.getInputStream(0)
    override val length: Long = snapshot.getLength(0)
    override fun toString(): String {
        return "SnapshotImpl( length=$length, inputStream=$inputStream)"
    }
}



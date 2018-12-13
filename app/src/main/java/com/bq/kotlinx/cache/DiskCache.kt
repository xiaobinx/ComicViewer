package me.xiaobinx.kotlinx.cache

import com.bq.kotlinx.copyToThenClose
import com.bq.comicviewer.App
import com.jakewharton.disklrucache.DiskLruCache
import java.io.ByteArrayInputStream
import java.io.Closeable
import java.io.File
import java.io.InputStream


private const val appVersion = 1

val diskLruCacheDir: File
    get() {
        val path = App.diskLruCacheDir
        val dir = File(path)
        if (!dir.exists()) {
            dir.mkdirs()
        }
        println(path)
        return dir
    }

object DiskCache {

    private val cache by lazy {
        DiskLruCache.open(
                diskLruCacheDir,
                appVersion, 1,
                App.diskCacheMaxSize
        )
    }

    fun contains(key:String):Boolean {
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



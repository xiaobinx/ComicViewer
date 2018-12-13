package com.bq.kotlinx

import java.io.InputStream
import java.io.OutputStream

fun InputStream.copyToThenClose(out: OutputStream, bufferSize: Int = DEFAULT_BUFFER_SIZE): Long {
    return this.use {
        out.use {
            var bytesCopied: Long = 0
            val b = ByteArray(bufferSize)
            var len = -1
            while (true) {
                len = read(b)
                if (len < 0) break
                bytesCopied += len
                out.write(b, 0, len)
            }
            bytesCopied
        }
    }
}

// @Suppress("NOTHING_TO_INLINE")
inline fun InputStream.readBytesThenClose(): ByteArray {
    this.use {
        return readBytes()
    }
}
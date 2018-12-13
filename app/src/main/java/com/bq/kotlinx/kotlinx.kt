package com.bq.kotlinx

import java.security.MessageDigest

fun String.md5(): ByteArray = MessageDigest.getInstance("MD5").digest(toByteArray())

fun ByteArray.toHexString(): String {
    if (isEmpty()) {
        return ""
    }

    val hexChar = charArrayOf('0', '1', '2', '3', '4', '5', '6', '7', '8', '9', 'a', 'b', 'c', 'd', 'e', 'f')
    val sb = StringBuilder(size * 2);
    for (i in 0 until size) {
        val high = (get(i).toInt() and 0xf0) shr 4
        val low = get(i).toInt() and 0x0f
        sb.append(hexChar[high]).append(hexChar[low])
    }
    return sb.toString()
}
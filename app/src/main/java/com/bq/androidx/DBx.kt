package com.bq.androidx

import android.database.Cursor
import android.database.sqlite.SQLiteDatabase
import android.util.Log

inline fun Cursor.eachRow(action: Cursor.() -> Unit) {
    use {
        while (it.moveToNext()) {
            action()
        }
    }
}


@Suppress("NOTHING_TO_INLINE")
inline fun Cursor.getString(name: String): String {
    return getString(getColumnIndex(name))
}

@Suppress("NOTHING_TO_INLINE")
inline fun Cursor.getLong(name: String): Long {
    return getLong(getColumnIndex(name))
}

@Suppress("NOTHING_TO_INLINE")
inline fun Cursor.getInt(name: String): Int {
    return getInt(getColumnIndex(name))
}

fun Cursor.println() {
    val tag = "com.bq.androidx.Cursor"
    eachRow {
        val sb = StringBuilder()
        columnNames.forEach { name ->
            sb.append(name).append("=").append(getString(getColumnIndex(name))).append("; ")
        }
        Log.d(tag, sb.toString())
    }
}
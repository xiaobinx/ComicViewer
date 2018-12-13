package com.bq.androidx

import android.app.Activity
import android.os.Environment
import android.util.Log

// Activity.-------------------------------------------------------------------------------------
val Activity.screenWidth get() = this.windowManager.defaultDisplay.width
val Activity.screenHeight get() = this.windowManager.defaultDisplay.height

fun Activity.printAllDirIKnow() {
    val tag = "com.bq.androidx"
    Log.i(tag, "System.getProperty(\"java.io.tmpdir\"): ${System.getProperty("java.io.tmpdir")}")
    Log.i(tag, "this.cacheDi}: $cacheDir")
    Log.i(tag, "this.codeCacheDir: $codeCacheDir")
    Log.i(tag, "this.externalCacheDir: $externalCacheDir")
    Log.i(tag, "this.filesDir}: $filesDir")
    Log.i(tag, "this.noBackupFilesDir: $noBackupFilesDir")
    Log.i(tag, "this.obbDir: $obbDir")
    Log.i(tag, "this.getExternalFilesDir-\"\": ${this.getExternalFilesDir("")}")
    Log.i(tag, "Environment.getDataDirectory(): ${Environment.getDataDirectory()}")
    Log.i(tag, "Environment.getDownloadCacheDirectory(): ${Environment.getDownloadCacheDirectory()}")
    Log.i(tag, "Environment.getExternalStorageDirectory(): ${Environment.getExternalStorageDirectory()}")
    Log.i(tag, "Environment.getExternalStoragePublicDirectory(DIRECTORY_MUSIC): ${Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_MUSIC)}")
    Log.i(tag, "Environment.getExternalStoragePublicDirectory(DIRECTORY_RINGTONES): ${Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_RINGTONES)}")
    Log.i(tag, "Environment.getExternalStoragePublicDirectory(DIRECTORY_NOTIFICATIONS): ${Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_NOTIFICATIONS)}")
    Log.i(tag, "Environment.getExternalStoragePublicDirectory(DIRECTORY_DCIM): ${Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_DCIM)}")
    Log.i(tag, "Environment.getExternalStoragePublicDirectory(DIRECTORY_PODCASTS): ${Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_PODCASTS)}")
    Log.i(tag, "Environment.getExternalStoragePublicDirectory(DIRECTORY_ALARMS): ${Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_ALARMS)}")
    Log.i(tag, "Environment.getExternalStoragePublicDirectory(DIRECTORY_PICTURES): ${Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_PICTURES)}")
    Log.i(tag, "Environment.getExternalStoragePublicDirectory(DIRECTORY_DOWNLOADS): ${Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_DOWNLOADS)}")
    Log.i(tag, "Environment.getExternalStoragePublicDirectory(DIRECTORY_DOCUMENTS): ${Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_DOCUMENTS)}")

}
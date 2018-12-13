package com.bq.androidx.tool.bitmap

import android.graphics.Bitmap
import android.graphics.BitmapFactory
import java.io.ByteArrayInputStream

/**
 * Created by xiaob on 2018/3/15.
 */


fun decodeStream(bytes: ByteArray, pixelW: Int, pixelH: Int): Bitmap {
    val input = ByteArrayInputStream(bytes)
    return if (pixelW <= 0 || pixelH <= 0) {
        BitmapFactory.decodeStream(input)
    } else {
        val options = BitmapFactory.Options()
        options.inJustDecodeBounds = true
        options.inPreferredConfig = Bitmap.Config.RGB_565
        BitmapFactory.decodeStream(input, null, options)
        val imageWidth = options.outWidth
        val imageHeight = options.outHeight
        options.inSampleSize = getSampleSize(imageWidth, imageHeight, pixelW, pixelH)
        options.inJustDecodeBounds = false
        input.reset()
        BitmapFactory.decodeStream(input, null, options)!!
    }

}

fun getSampleSize(imageWidth: Int, imageHeight: Int, pixelW: Int, pixelH: Int): Int {
    var sampleSize = 1
    if (imageWidth >= imageHeight && imageWidth > pixelW) {
        sampleSize = imageWidth / pixelW
    } else if (imageWidth < imageHeight && imageHeight > pixelH) {
        sampleSize = imageHeight / pixelH
    }
    if (sampleSize < 0) {
        sampleSize = 1
    }
    return sampleSize
}
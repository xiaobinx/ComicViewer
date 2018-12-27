package com.bq.androidx

import android.view.View

@Suppress("NOTHING_TO_INLINE")
inline fun View.toggleVisibility() {
    visibility = if (visibility == View.GONE) View.VISIBLE else View.GONE
}
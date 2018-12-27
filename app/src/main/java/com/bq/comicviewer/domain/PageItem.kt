package com.bq.comicviewer.domain


class PageItem {
    var rowOfPage: Int = 10

    var onLoading: Boolean = false

    var headp: Int = 1

    var tailp: Int = 1

    var maxp: Int = -1

    var rowCount: Int = -1
        set(value) {
            field = value
            val tp: Float = field.toFloat() / rowOfPage
            maxp = tp.toInt()
            maxp = if (tp > maxp) maxp + 1 else maxp
        }

    fun hasPrePage(): Boolean {
        return headp > 1
    }

    fun prePage(): Int {
        return headp - 1
    }

    fun hasNextPage(): Boolean {
        return tailp < maxp
    }

    fun nextPage(): Int {
        return tailp + 1
    }

    @Synchronized
    fun reset() {
        headp = 1
        tailp = 1
        maxp = -1
    }
}
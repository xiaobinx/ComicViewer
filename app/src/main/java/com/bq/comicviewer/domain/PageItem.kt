package com.bq.comicviewer.domain


class PageItem {
    /**
     * 每页的项目数量
     */
    var rowOfPage: Int = 10

    /**
     * 是否正在加载标志
     */
    var onLoading: Boolean = false

    /**
     * 列表头列
     */
    var headp: Int = 1

    /**
     * 列表尾页
     */
    var tailp: Int = 1

    /**
     * 最大页数
     */
    var maxp: Int = -1

    /**
     * 总项目数量
     */
    var rowCount: Int = -1
        set(value) {
            field = value
            if (rowOfPage < 1) return
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
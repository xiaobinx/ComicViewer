package com.bq.comicviewer.domain

/**
 * 用来表达一个可滑动列表的页信息
 */
class PageItem(
    /** 每页的项目数量 */
    var rowOfPage: Int = 10,
    /** 列表头列 */
    var headp: Int = 1,
    /** 列表尾页 */
    var tailp: Int = 1,
    /** 最大页数 */
    var maxp: Int = -1
) {
    /**
     * 第一个可见项目的索引（以headp页第一项为0）
     */
    var firstVisibleItem: Int = 0

    /**
     * headp页第一个项目的数据实际索引
     */
    val firstItemIndex: Int get() = (headp - 1) * rowOfPage

    /**
     * 第一个可见项目的数据实际索引，以传入数据的（数据库或时其他存储）第一项为0
     */
    val firstVisibleItemIndex: Int get() = firstItemIndex + firstVisibleItem

    /**
     *  第一个项目所在的页码
     */
    val firstVisibleItemPage: Int get() = firstVisibleItemIndex / rowOfPage + 1

    /**
     * 最后一个可见项目的索引（以headp页第一项为0）
     */
    var lastVisibleItem: Int = 0
    /**
     * headp页最后一个项目的数据实际索引
     */
    val lastItemIndex: Int get() = (headp - 1) * rowOfPage

    /**
     * 最后一个可见项目的数据实际索引，以传入数据的（数据库或时其他存储）第一项为0
     */
    val lastVisibleItemIndex: Int get() = lastItemIndex + lastVisibleItem

    /**
     *  最后一个项目所在的页码
     */
    val lastVisibleItemPage: Int get() = lastVisibleItemIndex / rowOfPage + 1

    /**
     * 当前中间项所在页码
     */
    val page: Int
        get() {
            /**
             * 中间项的索引
             */
            val medianItemIndex: Int = ((lastVisibleItemIndex.toFloat() + firstVisibleItemIndex.toFloat()) / 2).toInt()
            return medianItemIndex / rowOfPage + 1
        }


    /**
     * 是否正在加载标志
     */
    var onLoading: Boolean = false

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
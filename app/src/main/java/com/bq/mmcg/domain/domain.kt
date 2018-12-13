package com.bq.mmcg.domain

import java.io.Serializable

data class Comic(val url: String,
                 val coverUrl: String,
                 val title: String,
                 val type: String, // comic: 漫画, doujin: 同人志, cg: cg
                 var id: Long = -1,
                 /** 历史记录，观看到哪一页 */
                 var i: Int = 0,
                 var imgs: ArrayList<String> = ArrayList(0)
                 ) : Serializable

data class ComicPage(var pageNum: Int, var comics: ArrayList<Comic>)
package com.bq.mmcg.parser

import com.bq.mmcg.domain.ComicPage

interface IComicPageParser {
    fun parseComicPage(html: String): ComicPage

    fun parseComic(html: String): ArrayList<String>
}
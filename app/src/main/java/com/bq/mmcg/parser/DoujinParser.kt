package com.bq.mmcg.parser

import com.bq.mmcg.domain.Comic
import com.bq.mmcg.domain.ComicPage
import java.util.regex.Pattern

class DoujinParser : IComicPageParser {

    override fun parseComicPage(html: String): ComicPage {
        val pageNum = parsePageNum(html)
        val comics = parseComics(html)
        return ComicPage(pageNum, comics)
    }

    override fun parseComic(html: String): ArrayList<String> {
        return parseImgs(html)
    }

    private val patternPageNum1 = Pattern.compile(">(\\d+)</a></span><span class='ukbd'>")
    private val patternPageNum3 = Pattern.compile("<a\\s+href='\\S+?'>(\\d+)</a>")
    private fun parsePageNum(html: String): Int {
        var m = patternPageNum1.matcher(html)
        var pageNum = -1
        if (m.find()) {
            val pageStr = m.group(1)
            pageNum = Integer.valueOf(pageStr)
        } else {
            m = patternPageNum3.matcher(html)
            while (m.find()) {
                val page = Integer.valueOf(m.group(1))
                if (page > pageNum) {
                    pageNum = page
                }
            }
        }

        return pageNum
    }

    private var patternList = Pattern.compile(
            "<a class=\"aRF\" target=\"_blank\" href=\"(.+?)\" ><img class=\"aRF_Scg\" border=\"0\" src=\"(.+?)\" width=\"200\" height=\"286\" alt=\"(.+?)\" onerror=\"this.onerror=null\";   \"this.src='.+?'\"  link=\"(.+?)\" ></img></a>")

    private fun parseComics(html: String): ArrayList<Comic> {
        val m = patternList.matcher(html)

        val list = ArrayList<Comic>()

        while (m.find()) {
            val url = m.group(1)
            val coverUrl = m.group(2)
            val title = m.group(3)

            val comic = Comic(url, coverUrl, title, "doujin")
            list.add(comic)
        }

        return list
    }

    private val patternComicUrl = Pattern.compile("Large_cgurl\\[\\d+\\]\\s*=\\s*\"(.+?)\"")
    private fun parseImgs(html: String): ArrayList<String> {
        val list = ArrayList<String>()
        val m = patternComicUrl.matcher(html)
        while (m.find()) {
            val url = m.group(1)
            list.add(url)
        }
        return list
    }

//    private val patternComicTitle = Pattern.compile("<H1>(.+?)</H1>")
//    private fun parseTitle(html: String): String? {
//        var title: String? = null
//        val m = patternComicTitle.matcher(html)
//        if (m.find()) {
//            title = m.group(1)
//        }
//        return title
//    }

}
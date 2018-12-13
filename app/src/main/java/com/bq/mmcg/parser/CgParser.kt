package com.bq.mmcg.parser

import com.bq.mmcg.domain.Comic
import java.util.regex.Pattern

class CgParser {
    protected var pattern = Pattern.compile(
            "'<a class=\"aRF\" target=\"_blank\" href=\"(.+?)\">'\\+'<img class=\"aRF_Scg\" border=\"0\" src=\"(.+?)\" '\\+' width=\"200\" height=\"286\" alt=\"(.+?)\" '")

    private val patternCgTitle = Pattern.compile("<H1>(.+?)</H1>")

    private val patternCgUrls = Pattern.compile("Large_cgurl\\[\\d+\\]\\s*=\\s*\"(.+?)\"")

    fun parseCgList(html: String): List<Comic> {
        val list = ArrayList<Comic>()

        val m = pattern.matcher(html)

        while (m.find()) {

            val url = m.group(1)

            val coverUrl = m.group(2)

            val title = m.group(3)

            val cg = Comic(url, coverUrl, title, "cg")
            list.add(cg)
        }

        return list
    }

    fun parseCgTitle(html: String): String? {
        var title: String? = null
        val m = patternCgTitle.matcher(html)
        if (m.find()) {
            title = m.group(1)
        }
        return title
    }

    fun parseImgUrls(html: String): List<String> {
        val list = ArrayList<String>()
        val m = patternCgUrls.matcher(html)
        while (m.find()) {
            val url = m.group(1)
            list.add(url)
        }
        return list
    }

}
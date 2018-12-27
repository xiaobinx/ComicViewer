package com.bq.comicviewer.activity

import android.os.Bundle
import android.util.Log
import androidx.recyclerview.widget.DefaultItemAnimator
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.bq.androidx.components.activityx.DownloadTaskManagerActivity
import com.bq.androidx.http.HttpExecutor
import com.bq.comicviewer.R
import com.bq.comicviewer.URL_PATTERN_COMIC_LIST
import com.bq.comicviewer.URL_PATTERN_DOUJIN_LIST
import com.bq.comicviewer.adaprt.ComicePageAdaprt
import com.bq.comicviewer.domain.PageItem
import com.bq.mmcg.domain.Comic
import com.bq.mmcg.domain.ComicPage
import com.bq.mmcg.parser.ComicParser
import com.bq.mmcg.parser.DoujinParser
import com.bq.mmcg.parser.IComicPageParser
import kotlinx.android.synthetic.main.activity_item_rlist_comic.*
import java.util.*

/**
 * Created by xiaob on 2018/3/13.
 */
class ComicListActivity : DownloadTaskManagerActivity() {

    private val tag = javaClass.name

    val comics = ArrayList<Comic>(100)

    private val comicePageAdaprt = ComicePageAdaprt(comics, this)

    private lateinit var partternUrl: String

    private lateinit var comicPageParser: IComicPageParser

    private val pageItem = PageItem()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_item_rlist_comic)

        val strId = intent.getIntExtra("type", R.string.str_main_title_comic)

        if (R.string.str_main_title_doujin == strId) {
            partternUrl = URL_PATTERN_DOUJIN_LIST
            comicPageParser = DoujinParser()
        } else {
            partternUrl = URL_PATTERN_COMIC_LIST
            comicPageParser = ComicParser()
        }

        title = getString(strId)

        rv_page.apply {
            layoutManager = LinearLayoutManager(this@ComicListActivity)
            adapter = comicePageAdaprt
            itemAnimator = DefaultItemAnimator()

            addOnScrollListener(object : RecyclerView.OnScrollListener() {
                var lastVisibleItem: Int = 0
                override fun onScrollStateChanged(recyclerView: RecyclerView, newState: Int) {
                    if (newState == RecyclerView.SCROLL_STATE_IDLE && lastVisibleItem + 1 == comicePageAdaprt.itemCount) {
                        loadNextTailp()
                    }
                }

                override fun onScrolled(recyclerView: RecyclerView, dx: Int, dy: Int) {
                    val layoutManager = recyclerView.layoutManager as LinearLayoutManager
                    //最后一个可见的ITEM
                    lastVisibleItem = layoutManager.findLastVisibleItemPosition()
                }
            })
        }

        sr.apply {
            setColorSchemeResources(
                android.R.color.holo_green_light,
                android.R.color.holo_orange_light,
                android.R.color.holo_red_light
            )
            setOnRefreshListener {
                loadNextHeadp()
            }
        }

        refresh()
    }

    private fun loadNextHeadp() {
        if (!pageItem.hasPrePage()) {
            refresh()
            return
        }

        val p = pageItem.prePage()
        loadPage(p) {
            comics.addAll(0, it.comics)
            pageItem.headp = p
        }
    }

    private fun loadNextTailp() {
        if (!pageItem.hasNextPage()) return

        val p = pageItem.nextPage()
        loadPage(p) {
            comics.addAll(it.comics)
            pageItem.tailp = p
        }
    }

    private fun refresh() {
        pageItem.reset()
        loadPage(1) { comicPage ->
            comics.clear()
            comics.addAll(comicPage.comics)
        }
    }

    private fun loadPage(p: Int, action: (ComicPage) -> Unit) {
        if (pageItem.onLoading) return
        sr.isRefreshing = true
        pageItem.onLoading = true
        val pageUrl = partternUrl.replace("@{page}", p.toString())
        HttpExecutor(pageUrl).doFinally {
            runOnUiThread {
                sr.isRefreshing = false
                pageItem.onLoading = false
            }
        }.asyGetText {
            val comicPage = comicPageParser.parseComicPage(it)
            Log.d(tag, "共有列表项：${comicPage.comics.size}")
            action(comicPage)

            pageItem.maxp = comicPage.pageNum
            runOnUiThread {
                comicePageAdaprt.notifyDataSetChanged()
            }
        }
    }
}
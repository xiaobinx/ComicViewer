package com.bq.comicviewer.activity

import android.os.Bundle
import android.util.Log
import androidx.recyclerview.widget.DefaultItemAnimator
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.bq.androidx.components.activityx.DownloadTaskManagerActivity
import com.bq.androidx.http.BitmapListLoader
import com.bq.androidx.tool.commonExecutor
import com.bq.comicviewer.R
import com.bq.comicviewer.adaprt.ComicePageAdaprt
import com.bq.comicviewer.domain.PageItem
import com.bq.comicviewer.sqlhelper.ComicSqlHelper
import com.bq.mmcg.domain.Comic
import kotlinx.android.synthetic.main.activity_item_rlist_comic.*

class ComicHistoryActivity : DownloadTaskManagerActivity() {

    private val tag = javaClass.name

    private val comics = ArrayList<Comic>()

    override val bitmapListLoader = BitmapListLoader(90, 120)

    private val comicePageAdaprt = ComicePageAdaprt(comics, bitmapListLoader, this)

    private val pageItem = PageItem()

    private val comicSqlHelper by lazy { ComicSqlHelper(this) }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_item_rlist_comic)
        title = "浏览历史"
        recyclerView.apply {
            layoutManager = LinearLayoutManager(this@ComicHistoryActivity)
            adapter = comicePageAdaprt
            itemAnimator = DefaultItemAnimator()

            addOnScrollListener(object : RecyclerView.OnScrollListener() {
                var lastVisibleItem: Int = 0
                override fun onScrollStateChanged(recyclerView: RecyclerView, newState: Int) {
                    Log.d(this@ComicHistoryActivity.tag, "newState: $newState")
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

        swipeRefreshLayout.apply {
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
            comics.addAll(0, it)
            pageItem.headp = p
        }
    }

    private fun loadNextTailp() {
        if (!pageItem.hasNextPage()) return

        val p = pageItem.nextPage()
        loadPage(p) {
            comics.addAll(it)
            pageItem.tailp = p
        }
    }

    private fun refresh() {
        pageItem.reset()
        loadPage(1) {
            comics.clear()
            comics.addAll(it)
        }
    }

    private fun loadPage(p: Int, action: (ArrayList<Comic>) -> Unit) {
        if (pageItem.onLoading) return
        swipeRefreshLayout.isRefreshing = true
        pageItem.onLoading = true
        commonExecutor.execute {
            action(comicSqlHelper.queryHistory(p, pageItem))
            runOnUiThread {
                comicePageAdaprt.notifyDataSetChanged()
                swipeRefreshLayout.isRefreshing = false
                pageItem.onLoading = false
            }
        }
    }

    override fun finish() {
        try {
            comicSqlHelper.close()
        } finally {
            super.finish()
        }
    }
}
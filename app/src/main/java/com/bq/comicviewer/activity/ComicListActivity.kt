package com.bq.comicviewer.activity

import android.annotation.SuppressLint
import android.os.Bundle
import android.util.Log
import android.view.Menu
import android.view.MenuInflater
import android.view.MenuItem
import androidx.recyclerview.widget.DefaultItemAnimator
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.bq.androidx.components.activityx.DownloadTaskManagerActivity
import com.bq.androidx.http.HttpExecutor
import com.bq.androidx.http.imglistloader.SimpleBitmapListLoader
import com.bq.androidx.toast
import com.bq.comicviewer.R
import com.bq.comicviewer.URL_PATTERN_COMIC_LIST
import com.bq.comicviewer.URL_PATTERN_DOUJIN_LIST
import com.bq.comicviewer.adaprt.ComicePageAdaprt
import com.bq.comicviewer.components.PagePickerDialog
import com.bq.comicviewer.domain.PageItem
import com.bq.mmcg.domain.Comic
import com.bq.mmcg.domain.ComicPage
import com.bq.mmcg.parser.ComicParser
import com.bq.mmcg.parser.DoujinParser
import com.bq.mmcg.parser.IComicPageParser
import kotlinx.android.synthetic.main.activity_item_rlist_comic.*
import java.util.*

@SuppressLint("InflateParams")
class ComicListActivity : DownloadTaskManagerActivity() {

    private val tag = javaClass.name

    private val comics = ArrayList<Comic>(100)

    override val imgBitmapListLoader = SimpleBitmapListLoader(90, 120)

    private val comicePageAdaprt = ComicePageAdaprt(comics, imgBitmapListLoader, this)

    private lateinit var partternUrl: String

    private lateinit var comicPageParser: IComicPageParser

    private val pageItem = PageItem(100)

    private lateinit var pagePickerDialog: PagePickerDialog

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

        recyclerView.apply {
            layoutManager = LinearLayoutManager(this@ComicListActivity)
            adapter = comicePageAdaprt
            itemAnimator = DefaultItemAnimator()

            addOnScrollListener(object : RecyclerView.OnScrollListener() {
                override fun onScrollStateChanged(recyclerView: RecyclerView, newState: Int) {
                    if (newState == RecyclerView.SCROLL_STATE_IDLE && pageItem.lastVisibleItem + 1 == comicePageAdaprt.itemCount) {
                        loadNextTailp()
                    }
                }

                override fun onScrolled(recyclerView: RecyclerView, dx: Int, dy: Int) {
                    val layoutManager = recyclerView.layoutManager as LinearLayoutManager
                    //最后一个可见的ITEM
                    pageItem.firstVisibleItem = layoutManager.findFirstVisibleItemPosition()
                    pageItem.lastVisibleItem = layoutManager.findLastVisibleItemPosition()
                }
            })// end addOnScrollListener
        } // end recyclerView setting

        swipeRefreshLayout.apply {
            setColorSchemeResources(
                android.R.color.holo_green_light,
                android.R.color.holo_orange_light,
                android.R.color.holo_red_light
            )
            setOnRefreshListener {
                loadNextHeadp()
            }
        } // end swipeRefreshLayout setting

        pagePickerDialog = PagePickerDialog(this) { d, p -> onJumpPageActionDone(d, p) }

        refresh()
    }

    /**
     * 加载头页上一页
     */
    private fun loadNextHeadp() {
        if (!pageItem.hasPrePage()) {
            refresh()
            return
        }

        swipeRefreshLayout.isRefreshing = true
        val p = pageItem.prePage()
        loadPage(p) {
            comics.addAll(0, it.comics)
            pageItem.headp = p
            runOnUiThread {
                comicePageAdaprt.notifyItemRangeInserted(0, it.comics.size)
            }
        }
    }

    /**
     * 加载尾页的下一页
     */
    private fun loadNextTailp() {
        if (!pageItem.hasNextPage()) return

        val p = pageItem.nextPage()
        loadPage(p) {
            comics.addAll(it.comics)
            pageItem.tailp = p
            runOnUiThread {
                comicePageAdaprt.notifyItemRangeInserted(comics.size - 1, it.comics.size)
            }
        }
    }

    private fun refresh() {
        swipeRefreshLayout.isRefreshing = true
        loadPage(1) { comicPage ->
            comics.clear()
            comics.addAll(comicPage.comics)
            pageItem.reset()
            runOnUiThread {
                comicePageAdaprt.notifyDataSetChanged()
                recyclerView.scrollToPosition(0)
            }
        }
    }

    /**
     * 加载指定页的项目，自定义如何添加到当前列表comics
     */
    private fun loadPage(p: Int, action: (ComicPage) -> Unit) {
        if (pageItem.onLoading) return
        pageItem.onLoading = true
        val pageUrl = partternUrl.replace("@{page}", p.toString())
        HttpExecutor(pageUrl).onFinallyInUiThread {
            if (swipeRefreshLayout.isRefreshing) {
                swipeRefreshLayout.isRefreshing = false
            }
            pageItem.onLoading = false
        }.asyGetText {
            val comicPage = comicPageParser.parseComicPage(it)
            Log.d(tag, "共有列表项：${comicPage.comics.size}")
            action(comicPage)
            pageItem.maxp = comicPage.pageNum
        }
    }

    /**
     * 跳页动作
     */
    private fun onJumpPageActionDone(pagePickerDialog: PagePickerDialog, p: Int): Boolean {
        Log.d(tag, "jump: $p")
        return when {
            p < 1 -> {
                pagePickerDialog.setEditText(1)
                true
            }
            p > pageItem.maxp -> {
                pagePickerDialog.setEditText(pageItem.maxp)
                true
            }
            else -> {
                swipeRefreshLayout.isRefreshing = true
                loadPage(p) {
                    comics.clear()
                    comics.addAll(it.comics)
                    pageItem.headp = p
                    pageItem.tailp = p
                    runOnUiThread {
                        comicePageAdaprt.notifyDataSetChanged()
                        recyclerView.scrollToPosition(0)
                    }
                }
                false
            }
        }
    }

    @SuppressLint("SetTextI18n")
    private fun onJumpPage() {
        if(pageItem.maxp < 1) {
            toast("还未加载完成。")
        }else {
            pagePickerDialog.maxp = pageItem.maxp
            pagePickerDialog.page = pageItem.page
            pagePickerDialog.show()
        }
    }


    override fun onCreateOptionsMenu(menu: Menu): Boolean {
        MenuInflater(this).inflate(R.menu.menu_comic_list, menu)
        return super.onCreateOptionsMenu(menu)
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        when (item.itemId) {
            R.id.refreshPage -> refresh()
            R.id.jumpPage -> onJumpPage()
        }
        return super.onOptionsItemSelected(item)
    }
}
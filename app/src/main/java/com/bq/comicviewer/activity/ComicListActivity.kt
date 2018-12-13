package com.bq.comicviewer.activity

import android.content.Intent
import android.os.Bundle
import android.util.Log
import android.view.View
import android.view.ViewGroup
import androidx.recyclerview.widget.DefaultItemAnimator
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.bq.androidx.components.activityx.DownloadTaskManagerActivity
import com.bq.androidx.http.HttpExecutor
import com.bq.mmcg.domain.Comic
import com.bq.mmcg.domain.ComicPage
import com.bq.mmcg.parser.ComicParser
import com.bq.mmcg.parser.DoujinParser
import com.bq.mmcg.parser.IComicPageParser
import com.bq.comicviewer.R
import com.bq.comicviewer.URL_PATTERN_COMIC_LIST
import com.bq.comicviewer.URL_PATTERN_DOUJIN_LIST
import kotlinx.android.synthetic.main.activity_item_rlist_comic.*
import kotlinx.android.synthetic.main.rlist_item_comic.view.*
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
                    Log.d(this@ComicListActivity.tag, "newState: $newState")
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

class ComicePageAdaprt(private val comics: ArrayList<Comic>, private val activity: DownloadTaskManagerActivity)
    : RecyclerView.Adapter<ComicePageAdaprt.RvHolder>(), View.OnClickListener {

    override fun onClick(v: View) {
        val comic = v.iv.tag as Comic
        val intent = Intent(activity, ComicPageViewerActivity::class.java)
        intent.putExtra("comic", comic)
        activity.startActivity(intent)
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): RvHolder {
        val view = activity.layoutInflater.inflate(R.layout.rlist_item_comic, parent, false)
        view.setOnClickListener(this)
        return RvHolder(view)
    }

    override fun getItemCount(): Int {
        return comics.size
    }

    override fun onBindViewHolder(holder: RvHolder, position: Int) {
        val iv = holder.iv
        iv.setImageBitmap(null)
        val comic = comics[position]
        holder.tv.text = comic.title
        iv.tag = comic
        val task = HttpExecutor(comic.coverUrl).asyListImgLoad(90, 120) {
            activity.runOnUiThread {
                if (iv.tag === comic) {
                    iv.setImageBitmap(it)
                }
            } // end activity.runOnUiThread
        }// end  HttpExecutor.asyListImgLoad
        println("生成任务$task")
        if (null != task) activity.downloadTasks.add(task)
    } // end fun onBindViewHolder

    class RvHolder(view: View) : RecyclerView.ViewHolder(view) {
        val iv = view.iv!!
        val tv = view.tv!!
    }
}

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
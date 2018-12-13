package com.bq.comicviewer.activity

import android.app.Activity
import android.os.Bundle
import android.util.Log
import android.view.View
import android.view.ViewGroup
import android.view.Window
import android.widget.ImageView
import androidx.viewpager.widget.PagerAdapter
import com.bq.androidx.http.HttpExecutor
import com.bq.androidx.tool.commonExecutor
import com.bq.mmcg.domain.Comic
import com.bq.mmcg.parser.ComicParser
import com.bq.comicviewer.R
import com.bq.comicviewer.sqlhelper.ComicSqlHelper
import kotlinx.android.synthetic.main.activity_comic_page_viewer.*
import kotlinx.android.synthetic.main.vpitem_comic_page.view.*
import java.util.*

/**
 * Created by xiaob on 2018/3/14.
 */
class ComicPageViewerActivity : Activity() {

    private val tag = javaClass.toString()

    lateinit var comic: Comic

    private val parser = ComicParser()

    var imgs = ArrayList<String>()

    val comicSqlHelper by lazy { ComicSqlHelper(this) }

    lateinit var pvAdapter: ComicPageViewerAdapter

    @Suppress("RECEIVER_NULLABILITY_MISMATCH_BASED_ON_JAVA_ANNOTATIONS")
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        requestWindowFeature(Window.FEATURE_NO_TITLE)
        setContentView(R.layout.activity_comic_page_viewer)

        comic = intent.extras["comic"] as Comic
        pvAdapter = ComicPageViewerAdapter(this)
        vp.adapter = pvAdapter
        loadData()
    }

    private fun loadData() {
        comicSqlHelper.queryAndFillComic(comic)
        if (comic.id > 0 && comic.imgs.size > 0) {
            initUi()
        } else {
            HttpExecutor(comic.url).asyGetText {
                val list = parser.parseComic(it)
                Log.d(tag, "count: ${list.size}, url: ${comic.url}")
                comic.imgs = list
                comicSqlHelper.saveComic(comic)
                initUi()
            }// end HttpExecutor asyGetText
        } // end comic.id > 0 else
    }

    private fun initUi() {
        runOnUiThread {
            imgs.addAll(comic.imgs)
            pvAdapter.notifyDataSetChanged()
            vp.setCurrentItem(comic.i, false)
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

class ComicPageViewerAdapter(private val activity: ComicPageViewerActivity) : PagerAdapter() {

    // private val tag = javaClass.name

    private val viewPage = activity.vp

    private val comic = activity.comic

    private val imgs = activity.imgs

    private val holders = LinkedList<ViewHolder>()

    override fun instantiateItem(container: ViewGroup, position: Int): Any {
        val holder = if (0 == holders.size) {
            ViewHolder(activity.layoutInflater.inflate(R.layout.vpitem_comic_page, container, false))
        } else {
            holders.removeFirst()
        }
        val (_, iv, pb) = holder
        val imgUrl = imgs[position]
        iv.tag = imgUrl
        pb.visibility = View.VISIBLE
        HttpExecutor(imgUrl).asyListImgLoad(useMeCache = false) {
            activity.runOnUiThread {
                if (iv.tag == imgUrl) {
                    pb.visibility = View.GONE
                    iv.setImageBitmap(it)
                }
            }
        }
        container.addView(holder.view)

        val currentItemIndex = viewPage.currentItem
        if (currentItemIndex > 0 && currentItemIndex != comic.i) {
            commonExecutor.execute {
                activity.comicSqlHelper.updateHistory(comic.id, currentItemIndex)
                comic.i = currentItemIndex
            }
        }
        return holder
    }

    override fun destroyItem(container: ViewGroup, position: Int, o: Any) {
        val holder = o as ViewHolder
        holder.iv.setImageBitmap(null)
        container.removeView(holder.view)
        holders.add(holder)
    }

    override fun isViewFromObject(view: View, o: Any): Boolean {
        return view == (o as ViewHolder).view
    }

    override fun getCount(): Int {
        return imgs.size
    }

    @Suppress("PLUGIN_WARNING")
    class ViewHolder(val view: View) {
        val iv = view.iv!!
        private val pb = view.pb!!

        operator fun component1(): View = view
        operator fun component2(): ImageView = iv
        operator fun component3(): View = pb
    }

}
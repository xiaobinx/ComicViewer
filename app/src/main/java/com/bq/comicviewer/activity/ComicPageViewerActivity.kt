package com.bq.comicviewer.activity

import android.app.Activity
import android.os.Bundle
import android.util.Log
import android.view.View
import android.view.ViewGroup
import android.view.Window
import android.widget.ImageView
import android.widget.SeekBar
import androidx.viewpager.widget.PagerAdapter
import androidx.viewpager.widget.ViewPager
import com.bq.androidx.http.HttpExecutor
import com.bq.comicviewer.R
import com.bq.comicviewer.sqlhelper.ComicSqlHelper
import com.bq.mmcg.domain.Comic
import com.bq.mmcg.parser.ComicParser
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

        vp.apply {
            vp.adapter = pvAdapter

            addOnPageChangeListener(object : ViewPager.OnPageChangeListener {
                override fun onPageScrollStateChanged(state: Int) {
                    if (ViewPager.SCROLL_STATE_IDLE == state && sb.progress != vp.currentItem) {
                        updateImgProgress()
                    }
                }

                override fun onPageScrolled(position: Int, positionOffset: Float, positionOffsetPixels: Int) {}
                override fun onPageSelected(position: Int) {}
            })
        }
        sb.setOnSeekBarChangeListener(object : SeekBar.OnSeekBarChangeListener {
            override fun onProgressChanged(seekBar: SeekBar, progress: Int, fromUser: Boolean) {
                seekBar.progress
                if (fromUser && vp.currentItem != progress) vp.currentItem = progress
            }

            override fun onStartTrackingTouch(seekBar: SeekBar) {}
            override fun onStopTrackingTouch(seekBar: SeekBar) {}
        })
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
        imgs.addAll(comic.imgs)
        sb.apply {
            max = imgs.size - 1
            progress = comic.i
        }
        runOnUiThread {
            pvAdapter.notifyDataSetChanged()
            vp.setCurrentItem(comic.i, false)
        }
    }

    fun updateImgProgress() {
        sb.progress = vp.currentItem
    }

    override fun finish() {
        try {
            // 更新观看历史记录
            comicSqlHelper.updateHistory(comic.id, vp.currentItem)
            comicSqlHelper.close()
        } finally {
            super.finish()
        }
    }

    fun toggleToolBar() {
        if (sb.visibility == View.GONE) {
            sb.visibility = View.VISIBLE
        } else {
            sb.visibility = View.GONE
        }
    }
}

class ComicPageViewerAdapter(private val activity: ComicPageViewerActivity) : PagerAdapter() {

    private val tag = javaClass.name

    private val imgs = activity.imgs

    private val holders = LinkedList<ViewHolder>()

    override fun instantiateItem(container: ViewGroup, position: Int): Any {
        // 从缓存中获取或创建一个holder
        val holder = if (0 == holders.size) {
            val newView = activity.layoutInflater.inflate(R.layout.vpitem_comic_page, container, false)
            ViewHolder(newView).apply {
                iv.setOnClickListener { activity.toggleToolBar() }
            }
        } else {
            holders.removeFirst()
        }
        val (_, iv, pb) = holder
        val imgUrl = imgs[position]
        iv.tag = imgUrl
        pb.visibility = View.VISIBLE
        loadBm(imgUrl, iv, pb)
        container.addView(holder.view)
        return holder
    }

    override fun destroyItem(container: ViewGroup, position: Int, o: Any) {
        val holder = o as ViewHolder
        holder.iv.apply {
            setImageBitmap(null)
            tag = null
        }
        container.removeView(holder.view)
        holders.add(holder)
    }

    override fun isViewFromObject(view: View, o: Any): Boolean {
        return view == (o as ViewHolder).view
    }

    override fun getCount(): Int {
        return imgs.size
    }

    lateinit var currentHolder: ViewHolder // 先设置位延迟初始化 看看 有问题再说 表示当前显示 的holder
    override fun setPrimaryItem(container: ViewGroup, position: Int, `object`: Any) {
        currentHolder = `object` as ViewHolder
    }

    fun getPrimaryItem(): ViewHolder {
        return currentHolder
    }

    fun loadBm(imgUrl: String, iv: ImageView, pb: View) {
        HttpExecutor(imgUrl).asyLoadImgWithCache(useMeCache = false) {
            activity.runOnUiThread {
                if (iv.tag == imgUrl) {
                    pb.visibility = View.GONE
                    iv.setImageBitmap(it)
                }
            }
        }
    }

    @Suppress("PLUGIN_WARNING")
    class ViewHolder(val view: View) {
        val iv = view.iv!! as ImageView
        private val pb = view.pb!!

        operator fun component1(): View = view
        operator fun component2(): ImageView = iv
        operator fun component3(): View = pb
    }

}
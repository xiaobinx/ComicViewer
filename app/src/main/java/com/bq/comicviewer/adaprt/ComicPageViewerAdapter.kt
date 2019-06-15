package com.bq.comicviewer.adaprt

import android.annotation.SuppressLint
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.ProgressBar
import androidx.viewpager.widget.PagerAdapter
import com.bq.androidx.screenWidth
import com.bq.comicviewer.App
import com.bq.comicviewer.R
import com.bq.comicviewer.activity.ComicPageViewerActivity
import kotlinx.android.synthetic.main.vpitem_comic_page.view.*
import java.util.*

/**
 * 仅在ComicPageViewerActivity中使用
 */
class ComicPageViewerAdapter(
    private val activity: ComicPageViewerActivity
) : PagerAdapter() {

    // private val tag = javaClass.name
    private val bitmapListLoader = activity.bitmapListLoader

    private val imgs = activity.imgs

    private val holders = LinkedList<ViewHolder>()

    override fun instantiateItem(container: ViewGroup, position: Int): Any {
        // 从缓存中获取或创建一个holder
        val holder = if (0 == holders.size) {
            val newView = activity.layoutInflater.inflate(R.layout.vpitem_comic_page, container, false)
            ViewHolder(newView)
        } else {
            holders.removeFirst()
        }
        val (_, iv, progressBar) = holder
        val imgUrl = imgs[position]
        iv.tag = imgUrl
        progressBar.visibility = View.VISIBLE
        loadBm(imgUrl, iv, progressBar)
        container.addView(holder.view)
        return holder
    }

    override fun destroyItem(container: ViewGroup, position: Int, o: Any) {
        val holder = o as ViewHolder
        holder.imageView.apply {
            setImageBitmap(null)
            (tag as? String)?.let {
                bitmapListLoader.cancel(it)
            }
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

    private fun loadBm(imgUrl: String, iv: ImageView, progressBar: ProgressBar) {
        bitmapListLoader.load(imgUrl) {
            if (iv.tag == imgUrl) {
                progressBar.visibility = View.GONE
                iv.setImageBitmap(it)
            }
        }
    }

    private val lt: Int by lazy { activity.screenWidth / 3 }// 三等分屏幕
    private val gt: Int by lazy { activity.screenWidth * 2 / 3 } // 三等分屏幕

    @SuppressLint("ClickableViewAccessibility")
    inner class ViewHolder(val view: View) {

        val imageView = view.photoView!!
        private val progressBar = view.progressBar!!

        init {
            imageView.setOnViewTapListener { _, x, _ ->
                click(x)
            }
        }

        /**
         * 翻页相关逻辑
         */
        private fun click(x: Float) {
            if (x < lt) {
                activity.prePage()
            } else if (x >= lt && x <= gt) {
                activity.toggleToolBar()
            } else if (x > gt) {
                activity.nextPage()
            }
        }

        operator fun component1() = view
        operator fun component2() = imageView
        operator fun component3() = progressBar
    }

}
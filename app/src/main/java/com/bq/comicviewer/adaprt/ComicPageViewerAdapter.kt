package com.bq.comicviewer.adaprt

import android.annotation.SuppressLint
import android.view.MotionEvent
import android.view.View
import android.view.ViewConfiguration
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.ProgressBar
import androidx.viewpager.widget.PagerAdapter
import com.bq.androidx.screenWidth
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
    private val bitmapListLoader = activity.imgBitmapListLoader

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


    /**
     * 翻页相关逻辑
     */
    private val lt: Int by lazy { activity.screenWidth / 3 }// 三等分屏幕
    private val gt: Int by lazy { activity.screenWidth * 2 / 3 } // 三等分屏幕
    private val mTouchSlop by lazy { ViewConfiguration.get(activity).scaledTouchSlop }
    private val onImgTouchListener: View.OnTouchListener by lazy {
        object : View.OnTouchListener {
            var clickX = 0f
            var clickY = 0f
            @SuppressLint("ClickableViewAccessibility")
            override fun onTouch(v: View?, ev: MotionEvent): Boolean {
                when (ev.action) {
                    MotionEvent.ACTION_DOWN -> {
                        clickX = ev.x
                        clickY = ev.y
                    }
                    MotionEvent.ACTION_UP -> {
                        val xDiff = Math.abs(ev.x - clickX)
                        val yDiff = Math.abs(ev.y - clickY)
                        if (xDiff < mTouchSlop && xDiff >= yDiff) click(ev.x)
                    }
                }
                return true
            }

            private fun click(x: Float) {
                if (x < lt) {
                    activity.prePage()
                } else if (x >= lt && x <= gt) {
                    activity.toggleToolBar()
                } else if (x > gt) {
                    activity.nextPage()
                }
            }
        }
    }

    @SuppressLint("ClickableViewAccessibility")
    inner class ViewHolder(val view: View) {

        val imageView = view.photoView!!
        private val progressBar = view.progressBar!!

        init {
            imageView.setOnTouchListener(onImgTouchListener)
        }

        operator fun component1() = view
        operator fun component2() = imageView
        operator fun component3() = progressBar
    }

}
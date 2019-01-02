package com.bq.comicviewer.adaprt

import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.ProgressBar
import androidx.viewpager.widget.PagerAdapter
import com.bq.androidx.http.imglistloader.SimpleBitmapListLoader
import com.bq.comicviewer.R
import com.bq.comicviewer.activity.ComicPageViewerActivity
import kotlinx.android.synthetic.main.vpitem_comic_page.view.*
import java.util.*

/**
 * 仅在ComicPageViewerActivity中使用
 */
class ComicPageViewerAdapter(
    private val bitmapListLoader: SimpleBitmapListLoader,
    private val activity: ComicPageViewerActivity
) : PagerAdapter() {

    // private val tag = javaClass.name

    private val imgs = activity.imgs

    private val holders = LinkedList<ViewHolder>()

    override fun instantiateItem(container: ViewGroup, position: Int): Any {
        // 从缓存中获取或创建一个holder
        val holder = if (0 == holders.size) {
            val newView = activity.layoutInflater.inflate(R.layout.vpitem_comic_page, container, false)
            ViewHolder(newView).apply {
                imageView.setOnClickListener { activity.toggleToolBar() }
            }
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

    class ViewHolder(val view: View) {
        val imageView = view.photoView!!
        private val progressBar = view.progressBar!!

        operator fun component1() = view
        operator fun component2() = imageView
        operator fun component3() = progressBar
    }

}
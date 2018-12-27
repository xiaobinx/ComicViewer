package com.bq.comicviewer.adaprt

import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import androidx.viewpager.widget.PagerAdapter
import com.bq.androidx.http.HttpExecutor
import com.bq.comicviewer.R
import com.bq.comicviewer.activity.ComicPageViewerActivity
import kotlinx.android.synthetic.main.vpitem_comic_page.view.*
import java.util.*

/**
 * 仅在ComicPageViewerActivity中使用
 */
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
        val task = HttpExecutor(imgUrl).asyLoadImgWithCache(useMeCache = false) {
            activity.runOnUiThread {
                if (iv.tag == imgUrl) {
                    pb.visibility = View.GONE
                    iv.setImageBitmap(it)
                }
            }
        }
        if (task != null) activity.addDownloadTask(task)
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
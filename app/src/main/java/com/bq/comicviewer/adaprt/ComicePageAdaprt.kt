package com.bq.comicviewer.adaprt

import android.content.Intent
import android.view.View
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.bq.androidx.components.activityx.DownloadTaskManagerActivity
import com.bq.androidx.http.HttpExecutor
import com.bq.comicviewer.R
import com.bq.comicviewer.activity.ComicPageViewerActivity
import com.bq.mmcg.domain.Comic
import kotlinx.android.synthetic.main.rlist_item_comic.view.*
import java.util.*

class ComicePageAdaprt(private val comics: ArrayList<Comic>, private val activity: DownloadTaskManagerActivity) :
    RecyclerView.Adapter<ComicePageAdaprt.RvHolder>(), View.OnClickListener {

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
        val task = HttpExecutor(comic.coverUrl).asyLoadImgWithCache(90, 120) {
            activity.runOnUiThread {
                if (iv.tag === comic) {
                    iv.setImageBitmap(it)
                }
            } // end activity.runOnUiThread
        }// end  HttpExecutor.asyListImgLoad
        if (null != task) activity.addDownloadTask(task)
    } // end fun onBindViewHolder

    class RvHolder(view: View) : RecyclerView.ViewHolder(view) {
        val iv = view.iv!!
        val tv = view.tv!!
    }
}
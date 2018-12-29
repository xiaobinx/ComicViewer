package com.bq.comicviewer.adaprt

import android.app.Activity
import android.content.Intent
import android.view.View
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.bq.androidx.http.imglistloader.SimpleBitmapListLoader
import com.bq.comicviewer.R
import com.bq.comicviewer.activity.ComicPageViewerActivity
import com.bq.mmcg.domain.Comic
import kotlinx.android.synthetic.main.rlist_item_comic.view.*
import java.util.*

class ComicePageAdaprt(
    private val comics: ArrayList<Comic>,
    val bitmapListLoader: SimpleBitmapListLoader,
    private val activity: Activity
) :
    RecyclerView.Adapter<ComicePageAdaprt.RvHolder>(), View.OnClickListener {

    override fun onClick(v: View) {
        val comic = v.imageView.tag as Comic
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
        val iv = holder.imageView
        iv.setImageBitmap(null)
        val comic = comics[position]
        holder.textView.text = comic.title
        iv.tag = comic
        bitmapListLoader.load(comic.coverUrl) {
            activity.runOnUiThread {
                if (iv.tag === comic) {
                    iv.setImageBitmap(it)
                }
            } // end activity.runOnUiThread
        }// end addLoadTask
    } // end fun onBindViewHolder

    class RvHolder(view: View) : RecyclerView.ViewHolder(view) {
        val imageView = view.imageView!!
        val textView = view.textView!!
    }
}
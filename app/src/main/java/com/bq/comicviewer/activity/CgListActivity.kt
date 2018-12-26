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
import com.bq.mmcg.parser.CgParser
import com.bq.comicviewer.R
import com.bq.comicviewer.URL_CG_LIST
import kotlinx.android.synthetic.main.activity_item_rlist_comic.*
import kotlinx.android.synthetic.main.rlist_item_comic.view.*
import java.util.*

/**
 * Created by xiaob on 2018/3/13.
 */
class CgListActivity : DownloadTaskManagerActivity() {

    private val tag = javaClass.toString()

    val comics = ArrayList<Comic>(100)

    private val comicePageAdaprt = CgPageAdaprt(this)

    private var cgParser = CgParser()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_item_rlist_comic)

        title = getString(R.string.str_main_title_cg)

        rv_page.apply {
            layoutManager = LinearLayoutManager(this@CgListActivity)
            adapter = comicePageAdaprt
            itemAnimator = DefaultItemAnimator()
        }

        sr.setOnRefreshListener {
            loadPage()
        }

        loadPage()
    }

    private fun loadPage() {
        sr.isRefreshing = true
        HttpExecutor(URL_CG_LIST).asyGetText {
            val list = cgParser.parseCgList(it)
            Log.d(tag, "共有列表项：${list.size}")
            comics.apply {
                clear()
                addAll(list)
            }
            runOnUiThread {
                sr.isRefreshing = false
                comicePageAdaprt.notifyDataSetChanged()
            }
        }
    }
}

private class CgPageAdaprt(private val activity: CgListActivity) : RecyclerView.Adapter<CgPageAdaprt.RvHolder>(), View.OnClickListener {

    private val comics = activity.comics

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
        HttpExecutor(comic.coverUrl).asyLoadImgWithCache(90, 120) {
            activity.runOnUiThread {
                if (iv.tag === comic) {
                    iv.setImageBitmap(it)
                }
            }
        }?.let {
            activity.addDownloadTask(it)
        }
    }

    private class RvHolder(view: View) : RecyclerView.ViewHolder(view) {
        val iv = view.iv!!
        val tv = view.tv!!
    }
}

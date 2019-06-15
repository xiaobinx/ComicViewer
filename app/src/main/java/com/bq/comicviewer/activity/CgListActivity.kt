package com.bq.comicviewer.activity

import android.os.Bundle
import android.util.Log
import androidx.recyclerview.widget.DefaultItemAnimator
import androidx.recyclerview.widget.LinearLayoutManager
import com.bq.androidx.components.activityx.DownloadTaskManagerActivity
import com.bq.androidx.http.HttpExecutor
import com.bq.androidx.http.BitmapListLoader
import com.bq.comicviewer.R
import com.bq.comicviewer.URL_CG_LIST
import com.bq.comicviewer.adaprt.ComicePageAdaprt
import com.bq.mmcg.domain.Comic
import com.bq.mmcg.parser.CgParser
import kotlinx.android.synthetic.main.activity_item_rlist_comic.*
import java.util.*

/**
 * Created by xiaob on 2018/3/13.
 */
class CgListActivity : DownloadTaskManagerActivity() {

    private val tag = javaClass.toString()

    private val comics = ArrayList<Comic>(25)

    override val bitmapListLoader = BitmapListLoader(90, 120)

    private val comicePageAdaprt = ComicePageAdaprt(comics, bitmapListLoader, this)

    private var cgParser = CgParser()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_item_rlist_comic)

        title = getString(R.string.str_main_title_cg)

        recyclerView.apply {
            layoutManager = LinearLayoutManager(this@CgListActivity)
            adapter = comicePageAdaprt
            itemAnimator = DefaultItemAnimator()
        }

        swipeRefreshLayout.setOnRefreshListener {
            loadPage()
        }

        loadPage()
    }

    private fun loadPage() {
        swipeRefreshLayout.isRefreshing = true
        HttpExecutor(URL_CG_LIST).asyGetText {
            val list = cgParser.parseCgList(it)
            Log.d(tag, "共有列表项：${list.size}")
            comics.apply {
                clear()
                addAll(list)
            }
            runOnUiThread {
                swipeRefreshLayout.isRefreshing = false
                comicePageAdaprt.notifyDataSetChanged()
            }
        }
    }
}
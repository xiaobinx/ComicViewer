package com.bq.comicviewer.activity

import android.annotation.SuppressLint
import android.os.Bundle
import android.util.Log
import android.widget.SeekBar
import androidx.viewpager.widget.ViewPager
import com.bq.androidx.components.activityx.DownloadTaskManagerActivity
import com.bq.androidx.http.HttpExecutor
import com.bq.androidx.http.imglistloader.SimpleBitmapListLoader
import com.bq.androidx.toggleVisibility
import com.bq.comicviewer.R
import com.bq.comicviewer.adaprt.ComicPageViewerAdapter
import com.bq.comicviewer.sqlhelper.ComicSqlHelper
import com.bq.mmcg.domain.Comic
import com.bq.mmcg.parser.ComicParser
import kotlinx.android.synthetic.main.activity_comic_page_viewer.*
import java.util.*

class ComicPageViewerActivity : DownloadTaskManagerActivity() {

    private val tag = javaClass.toString()

    lateinit var comic: Comic

    private val parser = ComicParser()

    var imgs = ArrayList<String>()

    val comicSqlHelper by lazy { ComicSqlHelper(this) }

    override val imgBitmapListLoader = SimpleBitmapListLoader(useMeCache = false)

    lateinit var pvAdapter: ComicPageViewerAdapter

    @Suppress("RECEIVER_NULLABILITY_MISMATCH_BASED_ON_JAVA_ANNOTATIONS")
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        // requestWindowFeature(Window.FEATURE_NO_TITLE)
        supportActionBar?.hide()
        setContentView(R.layout.activity_comic_page_viewer)

        comic = intent.extras["comic"] as Comic
        pvAdapter = ComicPageViewerAdapter(this)

        viewPager.apply {
            adapter = pvAdapter

            addOnPageChangeListener(object : ViewPager.OnPageChangeListener {
                override fun onPageScrollStateChanged(state: Int) {
                    if (sb.progress != currentItem) {
                        updateImgProgress()
                        setText()
                    }
                }

                override fun onPageScrolled(position: Int, positionOffset: Float, positionOffsetPixels: Int) {}
                override fun onPageSelected(position: Int) {}
            })
        }
        sb.setOnSeekBarChangeListener(object : SeekBar.OnSeekBarChangeListener {
            override fun onProgressChanged(seekBar: SeekBar, progress: Int, fromUser: Boolean) {
                if (fromUser && viewPager.currentItem != progress) {
                    viewPager.currentItem = progress
                    setText()
                }
            }

            override fun onStartTrackingTouch(seekBar: SeekBar) {}
            override fun onStopTrackingTouch(seekBar: SeekBar) {}
        })
        loadData()
    }

    @SuppressLint("SetTextI18n")
    fun setText() {
        textViewLabel.text = "${viewPager.currentItem + 1}/${comic.imgs.size}"
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
            viewPager.setCurrentItem(comic.i, false)
            setText()
        }
    }

    fun updateImgProgress() {
        sb.progress = viewPager.currentItem
    }

    override fun finish() {
        try {
            // 更新观看历史记录
            comicSqlHelper.updateHistory(comic.id, viewPager.currentItem)
            comicSqlHelper.close()
        } finally {
            super.finish()
        }
    }

    fun toggleToolBar() {
        llToolBar.toggleVisibility()
    }
}
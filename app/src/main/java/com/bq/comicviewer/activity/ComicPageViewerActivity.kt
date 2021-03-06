package com.bq.comicviewer.activity

import android.annotation.SuppressLint
import android.os.Bundle
import android.util.Log
import android.widget.SeekBar
import androidx.appcompat.app.AppCompatActivity
import androidx.viewpager.widget.ViewPager
import com.bq.androidx.http.BitmapListLoader
import com.bq.androidx.http.HttpExecutor
import com.bq.androidx.toggleVisibility
import com.bq.comicviewer.R
import com.bq.comicviewer.adaprt.ComicPageViewerAdapter
import com.bq.comicviewer.sqlhelper.ComicSqlHelper
import com.bq.mmcg.domain.Comic
import com.bq.mmcg.parser.ComicParser
import kotlinx.android.synthetic.main.activity_comic_page_viewer.*
import java.util.*

class ComicPageViewerActivity : AppCompatActivity() {

    private val tag = javaClass.toString()

    lateinit var comic: Comic

    private val parser = ComicParser()

    var imgs = ArrayList<String>()

    private val comicSqlHelper by lazy { ComicSqlHelper(this) }

    val bitmapListLoader = BitmapListLoader(useMeCache = false)

    private val pvAdapter: ComicPageViewerAdapter = ComicPageViewerAdapter(this)

    @Suppress("RECEIVER_NULLABILITY_MISMATCH_BASED_ON_JAVA_ANNOTATIONS")
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        supportActionBar?.hide()
        setContentView(R.layout.activity_comic_page_viewer)

        comic = intent.extras["comic"] as Comic

        viewPager.apply {
            adapter = pvAdapter

            addOnPageChangeListener(object : ViewPager.OnPageChangeListener {
                override fun onPageScrollStateChanged(state: Int) {
                    if (ViewPager.SCROLL_STATE_SETTLING == state) {
                        if (sb.progress != currentItem) {
                            updateImgProgress()
                            setText()
                        }
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
                // prepareDownload(comic)
                initUi()
            }// end HttpExecutor asyGetText
        } // end comic.id > 0 else
    }

    /**
     * 从历史纪录开始预下载任务
     */
    private fun prepareDownload(comic: Comic) {
        for (i in comic.i until comic.imgs.size) {
            bitmapListLoader.load(comic.imgs[i])
        }
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
            bitmapListLoader.finish()
        } finally {
            super.finish()
        }
    }

    fun toggleToolBar() {
        llToolBar.toggleVisibility()
    }

    fun nextPage() {
        val currentItem = viewPager.currentItem
        if (currentItem < imgs.size - 1) {
            viewPager.currentItem = currentItem + 1
        }
    }

    fun prePage() {
        val currentItem = viewPager.currentItem
        if (currentItem > 0) {
            viewPager.currentItem = currentItem - 1
        }
    }
}
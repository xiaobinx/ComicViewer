package com.bq.comicviewer.activity

import android.content.Intent
import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import com.bq.comicviewer.R
import kotlinx.android.synthetic.main.activity_main.*

class MainActivity : AppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        cardCg.setOnClickListener {
            val intent = Intent(this, CgListActivity::class.java)
            startActivity(intent)
        }

        cardComic.setOnClickListener {
            openPage(R.string.str_main_title_comic)
        }
        cardDoujin.setOnClickListener {
            openPage(R.string.str_main_title_doujin)
        }
        cardHistory.setOnClickListener {
            val intent = Intent(this, ComicHistoryActivity::class.java)
            startActivity(intent)
        }

    }

    private fun openPage(strId: Int) {
        val intent = Intent(this, ComicListActivity::class.java)
        intent.putExtra("type", strId)
        startActivity(intent)
    }

}

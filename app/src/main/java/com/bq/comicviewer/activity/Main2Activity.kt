package com.bq.comicviewer.activity

import android.content.Intent
import android.os.Bundle
import android.view.Menu
import android.view.MenuInflater
import android.view.MenuItem
import androidx.appcompat.app.AppCompatActivity
import com.bq.androidx.printAllDirIKnow
import com.bq.comicviewer.R
import kotlinx.android.synthetic.main.activity_main2.*

class Main2Activity : AppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main2)

        printAllDirIKnow()

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
    }

    private fun openPage(strId: Int) {
        val intent = Intent(this, ComicListActivity::class.java)
        intent.putExtra("type", strId)
        startActivity(intent)
    }


    override fun onCreateOptionsMenu(menu: Menu?): Boolean {
        MenuInflater(this).inflate(R.menu.menu_mian2, menu)
        return super.onCreateOptionsMenu(menu)
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        if (R.id.history == item.itemId) {
            val intent = Intent(this, ComicHistoryActivity::class.java)
            startActivity(intent)
        }
        return super.onOptionsItemSelected(item)
    }
}

package com.bq.comicviewer.activity

import android.app.Activity
import android.content.Intent
import android.os.Bundle
import com.bq.comicviewer.R
import kotlinx.android.synthetic.main.activity_main.*

class MainActivity : Activity() {

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

//    override fun onRequestPermissionsResult(requestCode: Int, permissions: Array<String>, grantResults: IntArray) {
//        if (0 == requestCode
//                && permissions.size > 0
//                && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
//            Toast.makeText(this, "成功获取WRITE_EXTERNAL_STORAGE权限", Toast.LENGTH_LONG).show()
//        } else {
//            Toast.makeText(this, "获取WRITE_EXTERNAL_STORAGE权限失败", Toast.LENGTH_LONG).show()
//        }
//    }

}

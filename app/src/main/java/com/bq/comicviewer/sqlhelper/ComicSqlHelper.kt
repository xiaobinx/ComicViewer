package com.bq.comicviewer.sqlhelper

import android.annotation.SuppressLint
import android.content.ContentValues
import android.content.Context
import android.database.sqlite.SQLiteDatabase
import android.database.sqlite.SQLiteOpenHelper
import com.bq.androidx.eachRow
import com.bq.androidx.println
import com.bq.androidx.transaction
import com.bq.mmcg.domain.Comic
import com.bq.comicviewer.domain.PageItem


const val version = 1

class ComicSqlHelper(context: Context) : SQLiteOpenHelper(context, "comic", null, version) {

    override fun onCreate(db: SQLiteDatabase) {
        db.transaction {
            db.execSQL("""
            CREATE TABLE IF NOT EXISTS comic (
                _id INTEGER PRIMARY KEY AUTOINCREMENT,
                url NVARCHAR(300) NOT NULL,
                cover_url NVARCHAR(300) NOT NULL,
                title NVARCHAR(300) NOT NULL,
                type NVACHAR(20) NOT NULL
            );
            """.trimIndent())
            db.execSQL("""
            CREATE UNIQUE INDEX i_comic_url ON comic(url);;
            """.trimIndent())
            db.execSQL("""
            CREATE TABLE IF NOT EXISTS comic_imgs (
                cid INTEGER NOT NULL,
                i INTEGER NOT NULL,
                img_url NVARCHAR(300) NOT NULL,
                PRIMARY KEY(cid,i),
                FOREIGN KEY (cid) REFERENCES comic(cid)
            );;
            """.trimIndent())
            db.execSQL("""
            CREATE TABLE IF NOT EXISTS history (
                cid INTEGER PRIMARY KEY,
                i INTEGER NOT NULL,
                read_date INTEGER NOT NULL,
                FOREIGN KEY (cid) REFERENCES comic(cid)
            )
            """.trimIndent())

        }
    }

    override fun onUpgrade(db: SQLiteDatabase, oldVersion: Int, newVersion: Int) {
    }

    /**
     * <插入> 一个comic，并 <插入> 或 <更新> 历史纪录
     */
    fun saveComic(comic: Comic, i: Int = 0) {
        writableDatabase.transaction {
            var id = rawQuery("SELECT _id FROM comic WHERE url = ?", arrayOf(comic.url)).use {
                if (it.moveToFirst()) it.getLong(0) else -1
            }
            if (id < 0) {
                id = insert("comic", null, ContentValues().apply {
                    put("url", comic.url)
                    put("cover_url", comic.coverUrl)
                    put("title", comic.title)
                    put("type", comic.type)
                })
                comic.imgs.forEachIndexed { i, url ->
                    execSQL("INSERT INTO comic_imgs(cid, i, img_url) VALUES(?, ?, ?)", arrayOf(id, i, url))
                }
            } // end if id < 0
            // updateHistory(this, id, i)
            comic.id = id
        } // end transaction
    }

    /**
     * 以ID <插入> 或 <更新> 一个历史纪录
     */
    private fun updateHistory(db: SQLiteDatabase, id: Long, i: Int = 0) {
        val date = System.currentTimeMillis()
        val eff = db.update("history",
                ContentValues().apply { put("read_date", date);put("i", i) },
                "cid = ?", arrayOf(id.toString()))
        if (1 > eff) {
            db.execSQL("INSERT INTO history(cid, i, read_date) VALUES(?, ?, ?)", arrayOf(id, i, date))
        }
        // db.rawQuery("SELECT * FROM history INNER JOIN comic ON cid = _id", null).use { it.println() }
    }

    /**
     * 以ID <插入> 或 <更新> 一个历史纪录
     */
    fun updateHistory(id: Long, i: Int) = writableDatabase.use {
        updateHistory(it, id, if (i < 1) 0 else i)
    }

    /**
     * 以传入的comic中的url填充comic中的id, imgs, 历史记录
     */
    @SuppressLint("Recycle")
    fun queryAndFillComic(comic: Comic) {
        readableDatabase.transaction {
            // 1.尝试查询ID 不存在就直接返回
            if (comic.id < 0) {
                rawQuery("SELECT _id FROM comic WHERE url = ? ", arrayOf(comic.url)).use {
                    if (it.moveToFirst()) {
                        comic.id = it.getLong(0)
                    } else {
                        return //表示第一次访问这个漫画，直接返回 queryFillComic
                    }
                }// end rawQuery id
            } // end if comic.id < 0

            // 2.尝试查询图片列表
            val imgs = ArrayList<String>()
            // eachRow中已经关闭
            rawQuery("SELECT img_url FROM comic_imgs WHERE cid = ? ORDER BY i ASC", arrayOf(comic.id.toString()))
                    .eachRow { imgs.add(getString(0)) }
            if (imgs.size > 0) comic.imgs = imgs
            // 3.尝试查询历史纪录
            rawQuery("SELECT i FROM history WHERE cid = ? ", arrayOf(comic.id.toString())).use {
                if (it.moveToFirst()) {
                    comic.i = it.getInt(0)
                }
            }// end rawQuery id
        }// end transaction
    }

    @SuppressLint("Recycle")
    fun queryHistory(p: Int, pageItem: PageItem): ArrayList<Comic> {
        val db = readableDatabase
        val start = (p - 1) * pageItem.rowOfPage
        val list = ArrayList<Comic>()
        db.rawQuery("SELECT _id, url, cover_url, title, type, i FROM comic c LEFT JOIN history h ON c._id = h.cid ORDER BY read_date DESC LIMIT ?, ?"
                , arrayOf(start.toString(), pageItem.rowOfPage.toString())).eachRow {
            list.add(Comic(
                    getString(getColumnIndex("url")),
                    getString(getColumnIndex("cover_url")),
                    getString(getColumnIndex("title")),
                    getString(getColumnIndex("type")),
                    getLong(getColumnIndex("_id")),
                    getInt(getColumnIndex("i"))
            ))
        }

        db.rawQuery("SELECT count(1) FROM history", null).use {
            if (it.moveToFirst()) pageItem.rowCount = it.getInt(0)
        }

        db.rawQuery("SELECT _id, url, cover_url, title, type, i FROM comic c LEFT JOIN history h ON c._id = h.cid ORDER BY read_date DESC", null)
                .println()
        return list
    }

}
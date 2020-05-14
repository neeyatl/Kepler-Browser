package com.aurumtechie.keplerbrowser

import android.content.ContentValues
import android.content.Context
import android.database.sqlite.SQLiteDatabase
import android.database.sqlite.SQLiteOpenHelper
import java.lang.System.currentTimeMillis

class KeplerDatabaseHelper(context: Context) :
    SQLiteOpenHelper(context, DB_NAME, null, DB_VERSION) {

    companion object {
        const val DB_NAME = "Kepler Browser"
        const val DB_VERSION = 2

        enum class WebPageListItems(val table: String) {
            HISTORY("History"), BOOKMARKS("Bookmarks");
        }

        fun SQLiteDatabase.insertWebPage(
            webPageListItem: WebPageListItems, title: String, url: String,
            timeInMillis: Long = currentTimeMillis()
        ): Long {
            val webPageDataValues = ContentValues().apply {
                put("title", title)
                put("url", url)
                put("timeInMillis", timeInMillis)
            }
            return insert(webPageListItem.table, null, webPageDataValues)
        }

        fun SQLiteDatabase.removeWebPage(
            webPageListItem: WebPageListItems, title: String, url: String
        ): Int = delete(webPageListItem.table, "title = ? AND url = ?", arrayOf(title, url))
    }

    override fun onCreate(db: SQLiteDatabase?) {
        updateDatabase(db!!, 0, DB_VERSION)
    }

    override fun onUpgrade(db: SQLiteDatabase?, oldVersion: Int, newVersion: Int) {
        updateDatabase(db!!, oldVersion, newVersion)
    }

    private fun updateDatabase(db: SQLiteDatabase, oldVersion: Int, newVersion: Int) {
        if (oldVersion < 1) {
            db.execSQL("CREATE TABLE Bookmarks (_id INTEGER PRIMARY KEY AUTOINCREMENT, title TEXT, url TEXT, timeInMillis INTEGER);")
            db.execSQL("CREATE TABLE History (_id INTEGER PRIMARY KEY AUTOINCREMENT, title TEXT, url TEXT, timeInMillis INTEGER);")
        }

        if (oldVersion == 1) db.execSQL("DROP TABLE SavedPages")

        if (oldVersion < 2) {
            db.insertWebPage(WebPageListItems.BOOKMARKS, "Example", "https://www.example.com/")
            db.insertWebPage(WebPageListItems.BOOKMARKS, "Google", "https://google.com/")
        }
    }

}

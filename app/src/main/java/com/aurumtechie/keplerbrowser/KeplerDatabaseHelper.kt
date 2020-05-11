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
        const val DB_VERSION = 1

        const val HISTORY = "History"
        const val BOOKMARKS = "Bookmarks"
        const val SAVED_PAGES = "SavedPages"

        fun SQLiteDatabase.insertWebPage(
            table: String, title: String, url: String,
            timeInMillis: Long = currentTimeMillis()
        ): Long {
            val webPageDataValues = ContentValues()
            webPageDataValues.put("title", title)
            webPageDataValues.put("url", url)
            webPageDataValues.put("timeInMillis", timeInMillis)
            return insert(table, null, webPageDataValues)
        }

        fun SQLiteDatabase.removeWebPage(table: String, title: String, url: String): Int =
            delete(table, "title = ? AND url = ?", arrayOf(title, url))
    }

    override fun onCreate(db: SQLiteDatabase?) {
        db?.execSQL("CREATE TABLE Bookmarks (_id INTEGER PRIMARY KEY AUTOINCREMENT, title TEXT, url TEXT, timeInMillis INTEGER);")
        db?.execSQL("CREATE TABLE SavedPages (_id INTEGER PRIMARY KEY AUTOINCREMENT, title TEXT, url TEXT, timeInMillis INTEGER);")
        db?.execSQL("CREATE TABLE History (_id INTEGER PRIMARY KEY AUTOINCREMENT, title TEXT, url TEXT, timeInMillis INTEGER);")
    }

    override fun onUpgrade(db: SQLiteDatabase?, oldVersion: Int, newVersion: Int) {

    }

}

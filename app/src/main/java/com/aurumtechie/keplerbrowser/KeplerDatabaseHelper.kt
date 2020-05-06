package com.aurumtechie.keplerbrowser

import android.content.ContentValues
import android.content.Context
import android.database.sqlite.SQLiteDatabase
import android.database.sqlite.SQLiteOpenHelper

class KeplerDatabaseHelper(context: Context) :
    SQLiteOpenHelper(context, DB_NAME, null, DB_VERSION) {

    companion object {
        const val DB_NAME = "Kepler Browser"
        const val DB_VERSION = 1

        const val HISTORY = "History"
        const val BOOKMARKS = "Bookmarks"
        const val SAVED_PAGES = "SavedPages"

        fun SQLiteDatabase.insertWebpage(
            table: String,
            title: String,
            url: String/*, imageBits: ByteArray*/,
            timeInMillis: Long = System.currentTimeMillis() // TODO: Replace with Kotlin equivalent of this
        ): Long {
            val webPageDataValues = ContentValues()
            webPageDataValues.put("title", title)
            webPageDataValues.put("url", url)
//        webPageDataValues.put("imageBits", imageBits)
            if (table == HISTORY) webPageDataValues.put("timeInMillis", timeInMillis)

            return this.insert(table, null, webPageDataValues)
        }

    }

    override fun onCreate(db: SQLiteDatabase?) {
        // TODO: ADD support to save image bits if possible
        db?.execSQL("CREATE TABLE Bookmarks (_id INTEGER PRIMARY KEY AUTOINCREMENT, title TEXT, url TEXT);")
        db?.execSQL("CREATE TABLE SavedPages (_id INTEGER PRIMARY KEY AUTOINCREMENT, title TEXT, url TEXT);")
        db?.execSQL("CREATE TABLE History (_id INTEGER PRIMARY KEY AUTOINCREMENT, title TEXT, url TEXT, timeInMillis INTEGER);") // TODO: Replace with LONG if possible
    }

    override fun onUpgrade(db: SQLiteDatabase?, oldVersion: Int, newVersion: Int) {

    }

}

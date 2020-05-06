package com.aurumtechie.keplerbrowser

import android.database.sqlite.SQLiteException
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ProgressBar
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.cursoradapter.widget.SimpleCursorAdapter
import androidx.fragment.app.ListFragment
import com.aurumtechie.keplerbrowser.KeplerDatabaseHelper.Companion.HISTORY
import kotlinx.android.synthetic.main.activity_web_pages_list.*

class WebPagesListActivity : AppCompatActivity() {

    companion object {
        const val EXTRA_STRING = "WebPagesListActivity"
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_web_pages_list)
        setSupportActionBar(toolbar)
        supportActionBar?.setDisplayHomeAsUpEnabled(true)

        val text = intent?.getStringExtra(EXTRA_STRING)
        supportActionBar?.title = text
        textView.text = text
    }
}

// TODO: USE fragments with SimpleCursorAdapter to create and display lists of bookmarks, history, and saved pages.
class WebPagesListFragment : ListFragment() {

    var table: String = HISTORY

    companion object {
        fun getInstance(table: String = HISTORY) =
            WebPagesListFragment().apply { this.table = table }
    }

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
//        TODO("USE Coroutines to do the database tasks in the background")
        val progressBar = ProgressBar(context).apply { visibility = ProgressBar.VISIBLE }

        val cursor = try {
            context?.let { KeplerDatabaseHelper(it) }?.readableDatabase?.query(
                table,
                arrayOf("_id", "title", "url"),
                null,
                null,
                null,
                null,
                null
            )
        } catch (e: SQLiteException) {
            Toast.makeText(context, "Database Unavailable: ${e.message}", Toast.LENGTH_SHORT).show()
            e.printStackTrace()
            null
        }

        progressBar.visibility = ProgressBar.GONE

        cursor?.let {
            listAdapter = SimpleCursorAdapter(
                context, R.layout.web_page_list_item, it, arrayOf("title", "url"),
                intArrayOf(R.id.title, R.id.url), 0
            )
        }
        return super.onCreateView(inflater, container, savedInstanceState)
    }
}
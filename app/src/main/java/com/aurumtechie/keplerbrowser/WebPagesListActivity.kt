package com.aurumtechie.keplerbrowser

import android.annotation.SuppressLint
import android.database.sqlite.SQLiteException
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ProgressBar
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.cursoradapter.widget.SimpleCursorAdapter
import androidx.fragment.app.ListFragment
import kotlinx.android.synthetic.main.activity_web_pages_list.*
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

class WebPagesListActivity : AppCompatActivity() {

    companion object {
        const val EXTRA_STRING = "WebPagesListActivity"
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_web_pages_list)
        setSupportActionBar(toolbar)
        supportActionBar?.setDisplayHomeAsUpEnabled(true)

        val selectTitle = intent?.extras?.get(EXTRA_STRING) as String
        supportActionBar?.title = selectTitle

        if (selectTitle != getString(R.string.saved_pages))
            supportFragmentManager.beginTransaction()
                .replace(R.id.listFragmentContainer, WebPagesListFragment.getInstance(selectTitle))
                .commit()
        else
            listFragmentContainer.addView(TextView(this).apply {
                text = getString(R.string.saved_pages)
            })
    }

    override fun onBackPressed() {
        super.onBackPressed() // pop fragment back-stack
        super.onBackPressed() // exit the activity
    }
}

class WebPagesListFragment : ListFragment() {

    var table: String = KeplerDatabaseHelper.Companion.WebPageListItems.HISTORY.table

    companion object {
        fun getInstance(table: String) = WebPagesListFragment().apply { this.table = table }
    }

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {

        val progressBar = ProgressBar(context).apply { visibility = ProgressBar.VISIBLE }

        CoroutineScope(Dispatchers.Default).launch {
            @SuppressLint("Recycle")
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
                Toast.makeText(context, "Database Unavailable: ${e.message}", Toast.LENGTH_SHORT)
                    .show()
                e.printStackTrace()
                null
            }

            withContext(Dispatchers.Main) {
                progressBar.visibility = ProgressBar.GONE

                cursor?.let {
                    listAdapter = SimpleCursorAdapter(
                        context, R.layout.web_page_list_item, it, arrayOf("title", "url"),
                        intArrayOf(R.id.title, R.id.url), 0
                    )
                }
            }
        }
        return super.onCreateView(inflater, container, savedInstanceState)
    }
}
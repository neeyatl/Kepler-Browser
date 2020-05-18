package com.aurumtechie.keplerbrowser

import android.annotation.SuppressLint
import android.database.Cursor
import android.database.sqlite.SQLiteException
import android.os.Bundle
import android.view.*
import android.widget.ProgressBar
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import androidx.cursoradapter.widget.SimpleCursorAdapter
import androidx.fragment.app.ListFragment
import androidx.preference.PreferenceManager
import com.google.android.material.snackbar.Snackbar
import kotlinx.android.synthetic.main.activity_web_pages_list.*
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

class WebPagesListActivity : AppCompatActivity() {

    private val selectTitle: String by lazy {
        intent?.getStringExtra(EXTRA_STRING)?.toString() ?: "Error"
    }

    companion object {
        const val EXTRA_STRING = "WebPagesListActivity"
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_web_pages_list)
        setSupportActionBar(toolbar)
        supportActionBar?.setDisplayHomeAsUpEnabled(true)

        supportActionBar?.title = selectTitle

        if (selectTitle == KeplerDatabaseHelper.Companion.WebPageListItems.HISTORY.table &&
            PreferenceManager.getDefaultSharedPreferences(this)
                .getBoolean(getString(R.string.private_mode), false)
        ) Snackbar.make(
            listFragmentContainer,
            getString(R.string.private_mode_is_on),
            Snackbar.LENGTH_LONG
        ).show()

        if (selectTitle != getString(R.string.saved_pages))
            supportFragmentManager.beginTransaction()
                .replace(
                    R.id.listFragmentContainer,
                    WebPagesListFragment.getInstance(selectTitle)
                ).commit()
        else listFragmentContainer.addView(TextView(this).apply {
            text = getString(R.string.saved_pages)
            textSize = 26F
        })
    }

    override fun onCreateOptionsMenu(menu: Menu?): Boolean {
        if (selectTitle == KeplerDatabaseHelper.Companion.WebPageListItems.HISTORY.table)
            menuInflater.inflate(R.menu.history, menu)
        return super.onCreateOptionsMenu(menu)
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean = when (item.itemId) {
        R.id.clear_history -> {
            AlertDialog.Builder(this).setMessage(getString(R.string.are_you_sure))
                .setPositiveButton(getString(R.string.delete)) { d, _ ->
                    CoroutineScope(Dispatchers.Default).launch {
                        try {
                            KeplerDatabaseHelper(this@WebPagesListActivity).writableDatabase.delete(
                                KeplerDatabaseHelper.Companion.WebPageListItems.HISTORY.table,
                                null, null
                            )
                            withContext(Dispatchers.Main) {
                                Toast.makeText(
                                    this@WebPagesListActivity,
                                    "Successfully Deleted!",
                                    Toast.LENGTH_SHORT
                                ).show()
                            }
                        } catch (e: SQLiteException) {
                            withContext(Dispatchers.Main) {
                                Toast.makeText(
                                    this@WebPagesListActivity,
                                    "Database Unavailable: Couldn't delete",
                                    Toast.LENGTH_SHORT
                                ).show()
                            }
                            e.printStackTrace()
                        }
                    }
                    // Update the screen after deleting the history
                    (supportFragmentManager
                        .findFragmentById(R.id.listFragmentContainer) as WebPagesListFragment)
                        .changeCursor()
                    d.dismiss()
                }.setNegativeButton(getString(android.R.string.cancel)) { d, _ -> d.dismiss() }
                .show()
            true
        }
        else -> super.onOptionsItemSelected(item)
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
            val cursor = getNewCursor()

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

    // To make fetching a new cursor easier when history is deleted and the screen needs an update
    @SuppressLint("Recycle")
    private suspend fun getNewCursor(): Cursor? = try {
        context?.let { KeplerDatabaseHelper(it) }?.readableDatabase?.query(
            table,
            arrayOf("_id", "title", "url"),
            null, null,
            null, null,
            "timeInMillis DESC" // Sorting based on time and showing the most recent results
        )
    } catch (e: SQLiteException) {
        withContext(Dispatchers.Main) {
            Toast.makeText(
                context,
                "Database Unavailable",
                Toast.LENGTH_SHORT
            ).show()
        }
        e.printStackTrace()
        null
    }

    // Changes the existing cursor with a new one
    fun changeCursor() {
        CoroutineScope(Dispatchers.Main).launch {
            (this@WebPagesListFragment.listAdapter as SimpleCursorAdapter).changeCursor(getNewCursor())
        }
    }
}
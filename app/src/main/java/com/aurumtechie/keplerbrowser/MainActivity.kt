package com.aurumtechie.keplerbrowser

import android.content.Context
import android.content.Intent
import android.os.Bundle
import android.view.Menu
import android.view.MenuItem
import android.view.View
import android.view.animation.AlphaAnimation
import android.view.inputmethod.EditorInfo
import android.view.inputmethod.InputMethodManager
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.preference.PreferenceManager
import com.aurumtechie.keplerbrowser.KeplerDatabaseHelper.Companion.BOOKMARKS
import com.aurumtechie.keplerbrowser.KeplerDatabaseHelper.Companion.HISTORY
import com.aurumtechie.keplerbrowser.KeplerDatabaseHelper.Companion.SAVED_PAGES
import com.aurumtechie.keplerbrowser.WebPagesListActivity.Companion.EXTRA_STRING
import com.google.android.material.snackbar.Snackbar
import kotlinx.android.synthetic.main.activity_main.*
import kotlinx.android.synthetic.main.fragment_web_view_tab.*

class MainActivity : AppCompatActivity(),
    AllOpenTabsRecyclerViewAdapter.Companion.OnTabClickListener {

    private val settingsPreference by lazy { PreferenceManager.getDefaultSharedPreferences(this) }

    private val buttonClick = AlphaAnimation(1F, 0.2F).apply { duration = 100 }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        // TODO: ADD support for startup tabs using settings preference
        setSupportActionBar(searchToolbar)
        searchEditText.setOnEditorActionListener { v, actionId, _ ->
            if (actionId == EditorInfo.IME_ACTION_DONE) {
                onSearchClicked(v)
                true
            } else false
        }

        if (savedInstanceState == null)
            supportFragmentManager.beginTransaction()
                .replace(R.id.tabContainer, WebViewTabFragment())
                .addToBackStack(null)
                .commit()
    }

    private fun onSearchClicked(view: View) {
        view.startAnimation(buttonClick)
        try {
            if (!ConnectivityHelper.isConnectedToNetwork(this@MainActivity)) {
                (getSystemService(Context.INPUT_METHOD_SERVICE) as InputMethodManager)
                    .hideSoftInputFromWindow(searchEditText.windowToken, 0)
                Snackbar.make(view, R.string.check_connection, Snackbar.LENGTH_LONG).show()
            } else {
                (getSystemService(Context.INPUT_METHOD_SERVICE) as InputMethodManager)
                    .hideSoftInputFromWindow(searchEditText.windowToken, 0)

                val userInput = searchEditText.text.toString()
                if (!userInput.contains("."))
                    webView.loadUrl(
                        "${settingsPreference.getString(
                            resources.getString(R.string.preferred_search_engine),
                            resources.getString(R.string.preferred_search_engine_def_value)
                        )}search?q=${userInput.replace(
                            " ",
                            "+"
                        )}"
                    )
                else
                    webView.loadUrl("https://www.$userInput")
            }
        } catch (e: Exception) {
            Toast.makeText(this@MainActivity, R.string.check_connection, Toast.LENGTH_SHORT)
                .show()
            e.printStackTrace()
        }
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        //TODO: Perform search on activity passed intent
        data?.getStringExtra("search")
            ?.let { url ->
                supportFragmentManager.beginTransaction()
                    .replace(R.id.tabContainer, WebViewTabFragment.getInstance(url))
                    .addToBackStack(null).commit()
            }
    }

    fun onPreviousClicked(view: View) {
        view.startAnimation(buttonClick)
        if (webView.canGoBack()) webView.goBack()
    }

    fun onTabsClicked(view: View) {
        view.startAnimation(buttonClick)
        val openTabs: MutableList<WebViewTabFragment> = mutableListOf()
        supportFragmentManager.fragments.forEach { if (it is WebViewTabFragment) openTabs.add(it) }

        supportFragmentManager.beginTransaction().replace(
            R.id.tabContainer,
            OpenTabsFragment(openTabs.toList())
        ).commit()
    }

    fun onNextClicked(view: View) {
        view.startAnimation(buttonClick)
        if (webView.canGoForward()) webView.goForward()
    }

    override fun onTabClick(view: View, position: Int) {
        supportFragmentManager.beginTransaction()
            .replace(R.id.tabContainer, supportFragmentManager.fragments[position])
            .addToBackStack(null)
            .commit()
    }

    override fun onCreateOptionsMenu(menu: Menu?): Boolean {
        menuInflater.inflate(R.menu.options_menu, menu)
        return super.onCreateOptionsMenu(menu)
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean = when (item.itemId) {
        R.id.bookmarks -> {
            startActivity(Intent(this, WebPagesListActivity::class.java).apply {
                putExtra(
                    EXTRA_STRING,
                    BOOKMARKS
                )
            })
            true
        }
        R.id.saved_pages -> {
            startActivity(Intent(this, WebPagesListActivity::class.java).apply {
                putExtra(
                    EXTRA_STRING,
                    SAVED_PAGES
                )
            })
            true
        }
        R.id.history -> {
            startActivity(Intent(this, WebPagesListActivity::class.java).apply {
                putExtra(
                    EXTRA_STRING,
                    HISTORY
                )
            })
            true
        }
        R.id.settings_option -> {
            startActivity(Intent(this, SettingsActivity::class.java))
            true
        }
        R.id.about -> {
            startActivity(Intent(this, WebPagesListActivity::class.java).apply {
                putExtra(
                    EXTRA_STRING,
                    getString(R.string.about)
                )
            })
            true
        }
        else -> super.onOptionsItemSelected(item)
    }

}

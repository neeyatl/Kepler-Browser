package com.aurumtechie.keplerbrowser

import android.content.Context
import android.content.Intent
import android.content.SharedPreferences
import android.graphics.Color
import android.os.Bundle
import android.view.View
import android.view.animation.AlphaAnimation
import android.view.inputmethod.EditorInfo
import android.view.inputmethod.InputMethodManager
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.appcompat.app.AppCompatDelegate
import androidx.preference.PreferenceManager
import com.google.android.material.snackbar.Snackbar
import kotlinx.android.synthetic.main.activity_main.*


class MainActivity : AppCompatActivity(), SharedPreferences.OnSharedPreferenceChangeListener {

    private val settingsPreference by lazy { PreferenceManager.getDefaultSharedPreferences(this) }

    private val buttonClick = AlphaAnimation(1F, 0.2F).apply { duration = 100 }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        settingsPreference.let { pref ->
            if (pref.getBoolean(resources.getString(R.string.dark_theme), false))
                AppCompatDelegate.setDefaultNightMode(AppCompatDelegate.MODE_NIGHT_YES)
            else AppCompatDelegate.setDefaultNightMode(AppCompatDelegate.MODE_NIGHT_NO)
        }
        setContentView(R.layout.activity_main)

        setSupportActionBar(searchToolbar)
        searchEditText.setOnEditorActionListener { _, actionId, _ ->
            if (actionId == EditorInfo.IME_ACTION_DONE) {
                searchButton.performClick()
                true
            } else false
        }

        if (savedInstanceState != null)
            webView.restoreState(savedInstanceState)
        else {
            setUpWebView()
            loadHomePage()
        }

        settingsPreference.registerOnSharedPreferenceChangeListener(this)
    }

    private fun loadHomePage() = webView.loadUrl(
        settingsPreference.getString(
            resources.getString(R.string.preferred_search_engine),
            resources.getString(R.string.preferred_search_engine_def_value)
        )
    )

    private fun setUpWebView() {
        webView.settings.javaScriptEnabled =
            settingsPreference.getBoolean("javascript_enabled", false)
        webView.settings.useWideViewPort = true
        webView.settings.loadWithOverviewMode = true
        webView.settings.setSupportZoom(true)
        webView.settings.setSupportMultipleWindows(true)
        webView.scrollBarStyle = View.SCROLLBARS_INSIDE_OVERLAY
        webView.setBackgroundColor(Color.WHITE)
        // TODO: ADD support for startup tabs using settings preference

        webView.webChromeClient = KeplerWebChromeClient(progressBar)
        webView.webViewClient = KeplerWebViewClient()
    }

    fun onSearchClicked(view: View) {
        view.startAnimation(buttonClick)
        try {
            if (!ConnectivityHelper.isConnectedToNetwork(this@MainActivity)) {
                Snackbar.make(view, R.string.check_connection, Snackbar.LENGTH_SHORT).show()
            } else {
                val inputMethodManager: InputMethodManager =
                    getSystemService(Context.INPUT_METHOD_SERVICE) as InputMethodManager
                inputMethodManager.hideSoftInputFromWindow(searchEditText.windowToken, 0)

                val userInput = searchEditText.text.toString()
                if (!userInput.contains("."))
                    webView.loadUrl(
                        "https://www.google.com/search?q=${userInput.replace(
                            " ",
                            "+"
                        )}"
                    )
                else
                    webView.loadUrl("https://www.$userInput")
                searchEditText.setText("")
            }
        } catch (e: Exception) {
            Toast.makeText(this@MainActivity, R.string.check_connection, Toast.LENGTH_SHORT)
                .show()
            e.printStackTrace()
        }
    }

    fun onPreviousClicked(view: View) {
        view.startAnimation(buttonClick)
        if (webView.canGoBack()) webView.goBack()
    }

    fun onHomeClicked(view: View) {
        view.startAnimation(buttonClick)
        loadHomePage()
    }

    fun onSettingsClicked(view: View) {
        view.startAnimation(buttonClick)
        startActivity(Intent(this, SettingsActivity::class.java))
    }

    fun onNextClicked(view: View) {
        view.startAnimation(buttonClick)
        if (webView.canGoForward()) webView.goForward()
    }

    override fun onDestroy() {
        super.onDestroy()
        settingsPreference.unregisterOnSharedPreferenceChangeListener(this)
    }

    override fun onSharedPreferenceChanged(
        sharedPreferences: SharedPreferences?,
        key: String?
    ) {
        key?.let {
            when (it) {
                resources.getString(R.string.dark_theme) -> sharedPreferences?.let { pref ->
                    if (pref.getBoolean(resources.getString(R.string.dark_theme), false))
                        AppCompatDelegate.setDefaultNightMode(AppCompatDelegate.MODE_NIGHT_YES)
                    else AppCompatDelegate.setDefaultNightMode(AppCompatDelegate.MODE_NIGHT_NO)
                }
                else -> {
                }
            }
        }
    }
}

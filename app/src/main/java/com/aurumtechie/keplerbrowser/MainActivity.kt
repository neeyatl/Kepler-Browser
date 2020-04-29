package com.aurumtechie.keplerbrowser

import android.content.Context
import android.graphics.Color
import android.os.Bundle
import android.view.View
import android.view.animation.AlphaAnimation
import android.view.inputmethod.EditorInfo
import android.view.inputmethod.InputMethodManager
import android.webkit.WebChromeClient
import android.webkit.WebView
import android.widget.ProgressBar
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import com.google.android.material.snackbar.Snackbar
import kotlinx.android.synthetic.main.activity_main.*


class MainActivity : AppCompatActivity() {

    private val buttonClick = AlphaAnimation(1F, 0.2F).apply { duration = 100 }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
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
    }

    private fun loadHomePage() = webView.loadUrl("https://www.google.com")

    private fun setUpWebView() {
//        webView.settings.javaScriptEnabled = true
        webView.settings.useWideViewPort = true
        webView.settings.loadWithOverviewMode = true
        webView.settings.setSupportZoom(true)
//        webView.settings.setSupportMultipleWindows(true)
        webView.scrollBarStyle = View.SCROLLBARS_INSIDE_OVERLAY
        webView.setBackgroundColor(Color.WHITE)

        webView.webChromeClient = object : WebChromeClient() {
            override fun onProgressChanged(view: WebView?, newProgress: Int) {
                super.onProgressChanged(view, newProgress)

                progressBar.progress = newProgress
                if (newProgress < 100 && progressBar.visibility == ProgressBar.GONE)
                    progressBar.visibility = ProgressBar.VISIBLE

                if (newProgress == 100)
                    progressBar.visibility = ProgressBar.GONE
                else
                    progressBar.visibility = ProgressBar.VISIBLE
            }
        }

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
            Toast.makeText(this@MainActivity, R.string.check_connection, Toast.LENGTH_SHORT).show()
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
        Toast.makeText(view.context, "To be implemented", Toast.LENGTH_SHORT).show()
    }

    fun onNextClicked(view: View) {
        view.startAnimation(buttonClick)
        if (webView.canGoForward()) webView.goForward()
    }

}

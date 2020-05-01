package com.aurumtechie.keplerbrowser

import android.os.Message
import android.webkit.CookieManager
import android.webkit.WebChromeClient
import android.webkit.WebView
import android.webkit.WebViewClient
import android.widget.ProgressBar

class KeplerWebViewClient : WebViewClient() {
    override fun shouldOverrideUrlLoading(view: WebView?, url: String?): Boolean {
        view?.loadUrl(url)
        CookieManager.getInstance().setAcceptCookie(true)
        return true
    }
}

class KeplerWebChromeClient(private val progressBar: ProgressBar) : WebChromeClient() {
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

    //  TODO: Create a new window and open the link there
    override fun onCreateWindow(
        view: WebView?,
        isDialog: Boolean,
        isUserGesture: Boolean,
        resultMsg: Message?
    ): Boolean {
        if (!isUserGesture) return false
        return super.onCreateWindow(view, isDialog, isUserGesture, resultMsg)
    }
}
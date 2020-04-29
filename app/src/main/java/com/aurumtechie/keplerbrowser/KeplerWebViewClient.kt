package com.aurumtechie.keplerbrowser

import android.webkit.CookieManager
import android.webkit.WebView
import android.webkit.WebViewClient

class KeplerWebViewClient : WebViewClient() {
    override fun shouldOverrideUrlLoading(view: WebView?, url: String?): Boolean {
        view?.loadUrl(url)
        CookieManager.getInstance().setAcceptCookie(true)
        return true
    }
}
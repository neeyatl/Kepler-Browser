package com.aurumtechie.keplerbrowser

import android.content.SharedPreferences
import android.graphics.Color
import android.os.Build
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.webkit.WebView
import android.widget.ProgressBar
import androidx.fragment.app.Fragment
import androidx.preference.PreferenceManager
import kotlinx.android.synthetic.main.activity_main.*
import kotlinx.android.synthetic.main.fragment_web_view_tab.*

class WebViewTabFragment : Fragment() {

    var url: String? = null

    private val settingsPreference: SharedPreferences by lazy {
        PreferenceManager.getDefaultSharedPreferences(context)
    }

    companion object {
        fun getInstance(url: String) = WebViewTabFragment().apply { this.url = url }
    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        val nestedScrollingWebView: WebView =
            inflater.inflate(R.layout.fragment_web_view_tab, container, false) as WebView
        if (savedInstanceState != null)
            nestedScrollingWebView.restoreState(savedInstanceState)
        else
            setUpWebView(nestedScrollingWebView)
        return nestedScrollingWebView
    }

    override fun onStart() {
        super.onStart()
        if (url != null) webView.loadUrl(url)
        else loadHomePage(webView)
    }

    override fun onResume() {
        super.onResume()
        if (activity?.progressBar?.visibility == ProgressBar.VISIBLE)
            activity?.progressBar?.visibility = ProgressBar.GONE
    }

    private fun loadHomePage(webView: WebView) = webView.loadUrl(
        settingsPreference.getString(
            resources.getString(R.string.preferred_search_engine),
            resources.getString(R.string.preferred_search_engine_def_value)
        )
    )

    private fun setUpWebView(webView: WebView) {
        webView.webChromeClient = activity?.progressBar?.let { KeplerWebChromeClient(it) }
        webView.webViewClient = KeplerWebViewClient

        webView.settings.javaScriptEnabled =
            settingsPreference.getBoolean("javascript_enabled", false)
        webView.settings.useWideViewPort = true
        webView.settings.loadWithOverviewMode = true
        webView.settings.setSupportZoom(true)
        webView.settings.setSupportMultipleWindows(true)
        webView.scrollBarStyle = View.SCROLLBARS_INSIDE_OVERLAY
        webView.setBackgroundColor(
            if (settingsPreference.getBoolean(resources.getString(R.string.dark_theme), false))
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M)
                    resources.getColor(R.color.very_light_grey, resources.newTheme())
                else
                    resources.getColor(R.color.very_light_grey)
            else Color.WHITE
        )
    }

}

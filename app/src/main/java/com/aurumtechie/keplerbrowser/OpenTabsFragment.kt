package com.aurumtechie.keplerbrowser

import android.content.Context
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.webkit.WebView
import androidx.core.view.isNotEmpty
import androidx.fragment.app.Fragment
import androidx.recyclerview.widget.GridLayoutManager
import androidx.recyclerview.widget.RecyclerView
import kotlinx.android.synthetic.main.tabs_item_web_view_layout.view.*

class OpenTabsFragment(private val openTabs: List<WebViewTabFragment>) : Fragment() {

    constructor() : this(listOf())

    private lateinit var onTabClickListener: AllOpenTabsRecyclerViewAdapter.Companion.OnTabClickListener

    override fun onAttach(context: Context) {
        super.onAttach(context)
        onTabClickListener = context as AllOpenTabsRecyclerViewAdapter.Companion.OnTabClickListener
    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        val tabs: RecyclerView =
            inflater.inflate(R.layout.fragment_open_tabs, container, false) as RecyclerView
        tabs.adapter = AllOpenTabsRecyclerViewAdapter(onTabClickListener, openTabs)
        tabs.layoutManager = GridLayoutManager(context, 2)
        return view
    }

}

class AllOpenTabsRecyclerViewAdapter(
    private val onTabClickListener: OnTabClickListener,
    private val openTabs: List<WebViewTabFragment>
) :
    RecyclerView.Adapter<AllOpenTabsRecyclerViewAdapter.WebViewTabHolder>() {

    companion object {
        interface OnTabClickListener {
            fun onTabClick(view: View, position: Int)
        }
    }

    class WebViewTabHolder(itemView: View) :
        RecyclerView.ViewHolder(itemView)

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): WebViewTabHolder =
        WebViewTabHolder(
            LayoutInflater.from(parent.context)
                .inflate(R.layout.tabs_item_web_view_layout, parent, false) as WebView
        )

    override fun getItemCount(): Int = openTabs.size

    override fun onBindViewHolder(holder: WebViewTabHolder, position: Int) {
        val webViewTabFragment = openTabs[position]
        val webView = webViewTabFragment.view as WebView
        // Add the WebView to the FrameLayout for displaying
        val webViewItemContainer = holder.itemView.webViewItem
        if (webViewItemContainer.isNotEmpty()) webViewItemContainer.removeAllViews()
        webViewItemContainer.addView(webView)

        // Display Web Page title and url
        holder.itemView.title.text = webView.title
        holder.itemView.url.text = webView.url

        // Handle click events
        holder.itemView.setOnClickListener { onTabClickListener.onTabClick(it, position) }
    }
}

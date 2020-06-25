@file:Suppress("BlockingMethodInNonBlockingContext")

package com.aurumtechie.keplerbrowser

import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.database.sqlite.SQLiteException
import android.net.Uri
import android.os.Bundle
import android.os.Handler
import android.provider.Settings
import android.view.Menu
import android.view.MenuItem
import android.view.View
import android.view.animation.AlphaAnimation
import android.view.inputmethod.EditorInfo
import android.view.inputmethod.InputMethodManager
import android.widget.Toast
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import androidx.appcompat.widget.Toolbar
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import androidx.core.net.toUri
import androidx.fragment.app.FragmentTransaction
import androidx.preference.PreferenceManager
import com.aurumtechie.keplerbrowser.ConnectivityHelper.WEB_URL_REGEX
import com.aurumtechie.keplerbrowser.KeplerDatabaseHelper.Companion.insertWebPage
import com.aurumtechie.keplerbrowser.KeplerDatabaseHelper.Companion.removeWebPage
import com.aurumtechie.keplerbrowser.WebPagesListActivity.Companion.EXTRA_STRING
import com.google.android.material.snackbar.Snackbar
import kotlinx.android.synthetic.main.activity_main.*
import kotlinx.android.synthetic.main.fragment_web_view_tab.*
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import java.io.File
import java.io.FileWriter
import java.net.URL

class MainActivity : AppCompatActivity(),
    AllOpenTabsRecyclerViewAdapter.Companion.OnTabClickListener {

    companion object {
        private const val REQUEST_CODE = 4579
    }

    private val settingsPreference by lazy { PreferenceManager.getDefaultSharedPreferences(this) }

    private val buttonClick = AlphaAnimation(0.2F, 1F).apply { duration = 100 }

    private var exitOnDoubleBackPressed = false

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        SettingsActivity.changeThemeForPreference(
            settingsPreference,
            getString(R.string.dark_theme),
            resources.getStringArray(R.array.dark_mode_values)
        )

        setContentView(R.layout.activity_main)

        setSupportActionBar(defaultToolbar)

        searchEditText.setOnEditorActionListener { v, actionId, _ ->
            if (actionId == EditorInfo.IME_ACTION_DONE) {
                onSearchStarted(v)
                true
            } else false
        }

        if (savedInstanceState == null)
            supportFragmentManager.beginTransaction()
                .replace(R.id.tabContainer, WebViewTabFragment())
                .addToBackStack(null)
                .commit()
        // If saved instance state is not null, the saved fragments will automatically be used
    }

    override fun onBackPressed() {
        val currentFragment = supportFragmentManager.findFragmentById(R.id.tabContainer)
        if (currentFragment != null) // Current fragment cannot be null. At least one fragment will be present at all times.
            if (currentFragment is WebViewTabFragment) {
                // if webView.canGoBack then goBack else exit app
                if (currentFragment.webView.canGoBack()) currentFragment.webView.goBack()
                else exitOnDoubleBackPressed(webView)
            } else // if current fragment isn't WebViewTabFragment which means it's OpenTabsFragment, in which case you exit the app.
                exitOnDoubleBackPressed(tabContainer)
        else super.onBackPressed()
    }

    override fun onStart() {
        super.onStart()
        if (supportFragmentManager.backStackEntryCount == 0) startNewTab()
    }

    override fun onSaveInstanceState(outState: Bundle) {
        super.onSaveInstanceState(outState)
        supportFragmentManager.findFragmentById(R.id.tabContainer)?.let {
            supportFragmentManager.saveFragmentInstanceState(it)
        }
    }

    private fun exitOnDoubleBackPressed(view: View) {
        if (exitOnDoubleBackPressed) // Exit the application
            startActivity(Intent(Intent.ACTION_MAIN).apply {
                addCategory(Intent.CATEGORY_HOME)
                flags = Intent.FLAG_ACTIVITY_NEW_TASK
            }).also { finish() }

        exitOnDoubleBackPressed = true
        Snackbar.make(
            view,
            R.string.press_back_again_to_close_app,
            Snackbar.LENGTH_SHORT
        ).show()
        // If the user doesn't press back within the next 2 seconds, change back.
        Handler().postDelayed({ exitOnDoubleBackPressed = false }, 2 * 1000)
    }

    fun onSearchButtonClicked(view: View) {
        view.startAnimation(buttonClick)
        defaultToolbar.visibility = Toolbar.GONE
        searchToolbar.visibility = Toolbar.VISIBLE
        setSupportActionBar(searchToolbar)
    }

    private fun switchActionBar() {
        searchToolbar.visibility = Toolbar.GONE
        defaultToolbar.visibility = Toolbar.VISIBLE
        setSupportActionBar(defaultToolbar)
    }

    private fun onSearchStarted(view: View) {
        view.startAnimation(buttonClick)
        try {
            if (!ConnectivityHelper.isConnectedToNetwork(view.context)) {
                (getSystemService(Context.INPUT_METHOD_SERVICE) as InputMethodManager)
                    .hideSoftInputFromWindow(searchEditText.windowToken, 0)
                Snackbar.make(view, R.string.check_connection, Snackbar.LENGTH_LONG).show()
            } else {
                (getSystemService(Context.INPUT_METHOD_SERVICE) as InputMethodManager)
                    .hideSoftInputFromWindow(searchEditText.windowToken, 0)

                val userInput = searchEditText.text.toString()
                if (userInput.matches(WEB_URL_REGEX)) {
                    webView.loadUrl(
                        // If a protocol is not mentioned, default it to https
                        if (userInput.matches(ConnectivityHelper.URL_PROTOCOL_CHECK_REGEX))
                            userInput else "https://$userInput"
                    )
                } else webView.loadUrl(
                    "${settingsPreference.getString(
                        resources.getString(R.string.preferred_search_engine),
                        resources.getString(R.string.preferred_search_engine_def_value)
                    )}search?q=${userInput.replace(
                        " ",
                        "+"
                    )}"
                )
            }
        } catch (e: Exception) {
            e.printStackTrace()
            Snackbar.make(view, R.string.check_connection, Snackbar.LENGTH_SHORT)
                .show()
        }
    }

    fun onPreviousClicked(view: View) {
        view.startAnimation(buttonClick)
        val webView =
            (supportFragmentManager.findFragmentById(R.id.tabContainer) as WebViewTabFragment).webView
        if (webView.canGoBack()) webView.goBack()
    }

    fun onTabsClicked(view: View) {
        view.startAnimation(buttonClick)
        val openTabs: MutableList<WebViewTabFragment> = mutableListOf()
        supportFragmentManager.fragments.forEach { if (it is WebViewTabFragment) openTabs.add(it) }

        supportFragmentManager.beginTransaction().replace(
            R.id.tabContainer,
            OpenTabsFragment(openTabs.toList())
        ).apply { setTransition(FragmentTransaction.TRANSIT_FRAGMENT_OPEN) }.commit()
    }

    fun onNextClicked(view: View) {
        view.startAnimation(buttonClick)
        val webView =
            (supportFragmentManager.findFragmentById(R.id.tabContainer) as WebViewTabFragment).webView
        if (webView.canGoForward()) webView.goForward()
    }

    override fun onTabClick(view: View, position: Int) {
        supportFragmentManager.popBackStack() // Remove the openTabsFragment before replacing the top of the stack with the selected fragment

        supportFragmentManager.beginTransaction()
            .replace(R.id.tabContainer, supportFragmentManager.fragments[position])
            .apply { setTransition(FragmentTransaction.TRANSIT_FRAGMENT_FADE) }
            .addToBackStack(null).commit()
    }

    override fun onCreateOptionsMenu(menu: Menu?): Boolean {
        menuInflater.inflate(R.menu.options_menu, menu)
        return super.onCreateOptionsMenu(menu)
    }

    private fun startNewTab() {
        supportFragmentManager.beginTransaction()
            .replace(R.id.tabContainer, WebViewTabFragment())
            .apply { setTransition(FragmentTransaction.TRANSIT_FRAGMENT_OPEN) }
            .addToBackStack(null).commit()
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean = when (item.itemId) {
        R.id.new_tab -> {
            startNewTab()
            true
        }
        R.id.bookmarks -> {
            startActivity(Intent(this, WebPagesListActivity::class.java).apply {
                putExtra(
                    EXTRA_STRING,
                    KeplerDatabaseHelper.Companion.WebPageListItems.BOOKMARKS.table
                )
            })
            true
        }
        R.id.saved_pages -> {
            startActivity(Intent(this, WebPagesListActivity::class.java).apply {
                putExtra(
                    EXTRA_STRING,
                    getString(R.string.saved_pages)
                )
            })
            true
        }
        R.id.history -> {
            startActivity(Intent(this, WebPagesListActivity::class.java).apply {
                putExtra(
                    EXTRA_STRING,
                    KeplerDatabaseHelper.Companion.WebPageListItems.HISTORY.table
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

    fun onAddToBookmarksClicked(view: View) {
        view.startAnimation(buttonClick)
        val webView =
            (supportFragmentManager.findFragmentById(R.id.tabContainer) as WebViewTabFragment).webView
        val title = webView.title
        val url = webView.url
        CoroutineScope(Dispatchers.Default).launch {
            try {
                val db = KeplerDatabaseHelper(this@MainActivity).writableDatabase
                val result: Long = db?.insertWebPage(
                    KeplerDatabaseHelper.Companion.WebPageListItems.BOOKMARKS,
                    title, url
                )!! // SQLiteDatabase.insert() is a non nullable function but is implemented in Java and hence the Long? type.
                withContext(Dispatchers.Main) {
                    if (result != -1L)
                        Snackbar.make(view, "Successfully Added!", Snackbar.LENGTH_LONG)
                            .setAction("UNDO") {
                                if (db.removeWebPage(
                                        KeplerDatabaseHelper.Companion.WebPageListItems.BOOKMARKS,
                                        webView.title,
                                        webView.url
                                    ) == 1
                                )
                                    Toast.makeText(this@MainActivity, "Undone!", Toast.LENGTH_SHORT)
                                        .show()
                            }.show()
                }
            } catch (e: SQLiteException) {
                Toast.makeText(
                    this@MainActivity,
                    "Database Unavailable: ${e.message}",
                    Toast.LENGTH_SHORT
                ).show()
                e.printStackTrace()
            }
        }
    }

    override fun onRequestPermissionsResult(
        requestCode: Int,
        permissions: Array<out String>,
        grantResults: IntArray
    ) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults)
        when (requestCode) {
            REQUEST_CODE -> {
                if (grantResults.isNotEmpty() && grantResults[0] != PackageManager.PERMISSION_GRANTED)
                    if (ActivityCompat.shouldShowRequestPermissionRationale(
                            this,
                            android.Manifest.permission.WRITE_EXTERNAL_STORAGE
                        )
                    ) // If permission was denied once before but the user wasn't informed why the permission is necessary, do so.
                        AlertDialog.Builder(this)
                            .setMessage(R.string.external_storage_permission_rationale)
                            .setPositiveButton(android.R.string.ok) { dialog, _ ->
                                dialog.dismiss()
                                requestExternalStoragePermission()
                            }.show()
                    else /* If user has chosen to not be shown permission requests any longer,
                     inform the user about it's importance and redirect her/him to device settings
                     so that permissions can be given */
                        requestPermissionAndOpenSettings()
            }
        }
    }

    fun onSavePageClicked(view: View) {
        view.startAnimation(buttonClick)

        if (ContextCompat.checkSelfPermission(
                this,
                android.Manifest.permission.WRITE_EXTERNAL_STORAGE
            ) != PackageManager.PERMISSION_GRANTED
        ) requestExternalStoragePermission()

        val webView =
            (supportFragmentManager.findFragmentById(R.id.tabContainer) as WebViewTabFragment).webView
        val fileName = "/storage/emulated/0/Download/${webView.title}.html"
        val urlString = webView.url.toString()

        Toast.makeText(view.context, R.string.downloading_file, Toast.LENGTH_SHORT).show()

        CoroutineScope(Dispatchers.IO).launch {
            try {
                // Create a URL object
                val url = URL(urlString)
                // Create a buffered reader object using the url object
                val reader = url.openStream().bufferedReader()

                // Enter filename in which you want to download
                val downloadFile = File(fileName).also { it.createNewFile() }
                // Create a buffered writer object for the file
                val writer = FileWriter(downloadFile).buffered()

                // read and write each line from the stream till the end
                var line: String
                while (reader.readLine().also { line = it?.toString() ?: "" } != null)
                    writer.write(line)

                // Close all open streams
                reader.close()
                writer.close()

                // Update UI for download is successful
                withContext(Dispatchers.Main) {
                    // Show a message for when the app is successfully downloaded
                    // Also provide an action to view the downloaded file
                    Snackbar.make(
                        view,
                        R.string.file_downloaded_successfully,
                        Snackbar.LENGTH_LONG
                    ).setAction(R.string.view) {
                        it.context.startActivity(
                            Intent.createChooser(
                                Intent(Intent.ACTION_VIEW).apply {
                                    setDataAndType(
                                        downloadFile.toUri(),
                                        "text/plain"
                                    )
                                },
                                view.context.getString(R.string.open_using)
                            )
                        )
                    }.show()
                }

            } catch (e: Exception) {
                // Update UI for download has failed
                withContext(Dispatchers.Main) {
                    Snackbar.make(view, R.string.download_failed, Snackbar.LENGTH_SHORT).show()

                    // File is downloaded incompletely in case of a download fail. Delete this file.
                    val incompleteFile = File(fileName)
                    if (incompleteFile.exists()) incompleteFile.delete()

                    e.printStackTrace()
                }
            }
        }
    }

    private fun requestExternalStoragePermission() = ActivityCompat.requestPermissions(
        this,
        arrayOf(
            android.Manifest.permission.READ_EXTERNAL_STORAGE,
            android.Manifest.permission.WRITE_EXTERNAL_STORAGE
        ),
        REQUEST_CODE
    )

    private fun requestPermissionAndOpenSettings() = AlertDialog.Builder(this)
        .setMessage(R.string.permission_request)
        .setPositiveButton(R.string.show_settings) { dialog, _ ->
            dialog.dismiss()
            // Open application settings to enable the user to toggle the permission settings
            startActivity(Intent(Settings.ACTION_APPLICATION_DETAILS_SETTINGS).apply {
                data = Uri.fromParts("package", packageName, null)
            })
        }.show()

    fun onCancelSearchClicked(view: View) {
        view.startAnimation(buttonClick)
        searchEditText.setText("")
        (getSystemService(Context.INPUT_METHOD_SERVICE) as InputMethodManager)
            .apply { if (isActive) hideSoftInputFromWindow(searchEditText.windowToken, 0) }
        switchActionBar()
    }

}

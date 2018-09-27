package cafe.adriel.nomanswallpaper.view.main

import android.content.Intent
import android.graphics.Color
import android.net.Uri
import android.os.Bundle
import android.view.MenuItem
import androidx.appcompat.app.ActionBarDrawerToggle
import androidx.appcompat.app.AlertDialog
import androidx.core.view.GravityCompat
import androidx.fragment.app.FragmentManager
import androidx.fragment.app.FragmentPagerAdapter
import androidx.lifecycle.Observer
import cafe.adriel.androidcoroutinescopes.appcompat.CoroutineScopedActivity
import cafe.adriel.nomanswallpaper.App
import cafe.adriel.nomanswallpaper.BuildConfig
import cafe.adriel.nomanswallpaper.R
import cafe.adriel.nomanswallpaper.util.Analytics
import cafe.adriel.nomanswallpaper.util.loadImage
import cafe.adriel.nomanswallpaper.util.open
import cafe.adriel.nomanswallpaper.util.share
import cafe.adriel.nomanswallpaper.view.main.dialog.AboutDialog
import cafe.adriel.nomanswallpaper.view.main.dialog.DonateDialog
import cafe.adriel.nomanswallpaper.view.main.settings.SettingsFragment
import cafe.adriel.nomanswallpaper.view.main.wallpaperlist.WallpaperListFragment
import com.google.android.material.navigation.NavigationView
import com.google.android.material.snackbar.Snackbar
import com.google.firebase.firestore.FirebaseFirestore
import com.kobakei.ratethisapp.RateThisApp
import kotlinx.android.synthetic.main.activity_main.*
import kotlinx.android.synthetic.main.drawer_header.view.*
import org.koin.androidx.viewmodel.ext.android.viewModel

class MainActivity : CoroutineScopedActivity(),
    NavigationView.OnNavigationItemSelectedListener, DonateDialog.OnDonateListener {

    private val viewModel by viewModel<MainViewModel>()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)
        setSupportActionBar(vToolbar)

        if (BuildConfig.RELEASE) {
            RateThisApp.onCreate(this)
            RateThisApp.showRateDialogIfNeeded(this)
        }

        FirebaseFirestore.getInstance().enableNetwork()
    }

    override fun onPostCreate(savedInstanceState: Bundle?) {
        super.onPostCreate(savedInstanceState)
        val drawerToggle = ActionBarDrawerToggle(this, vDrawer, vToolbar, R.string.open_menu, R.string.close_menu)
        vDrawerNav.setNavigationItemSelectedListener(this)
        vDrawer.addDrawerListener(drawerToggle)
        drawerToggle.drawerArrowDrawable.color = Color.WHITE
        drawerToggle.syncState()

        vDrawerNav.menu.getItem(0).isChecked = true
        vDrawerNav.menu.performIdentifierAction(R.id.nav_wallpapers, 0)
        vDrawerNav.getHeaderView(0)
            .vDrawerHeaderLogo
            .loadImage(R.drawable.drawer_header_logo)

        val adapter = SectionsPagerAdapter(supportFragmentManager)
        vContent.adapter = adapter
        vContent.offscreenPageLimit = adapter.count

        with(viewModel) {
            appUpdateAvailable.observe(this@MainActivity, Observer { newVersion ->
                if (newVersion)
                    showUpdateAppDialog()
            })
            purchaseCompleted.observe(this@MainActivity, Observer { success ->
                if (success)
                    Snackbar.make(vRoot, R.string.thanks_for_support, Snackbar.LENGTH_LONG).show()
            })
            billingSupported.observe(this@MainActivity, Observer { supported ->
                vDrawerNav.menu
                    .findItem(R.id.nav_donate)
                    .isVisible = supported
            })
        }
    }

    override fun onBackPressed() {
        if (vDrawer.isDrawerOpen(GravityCompat.START)) {
            vDrawer.closeDrawer(GravityCompat.START)
        } else {
            super.onBackPressed()
        }
    }

    override fun onResume() {
        super.onResume()
        when(vContent?.currentItem){
            0 -> goToWallpapers()
            1 -> goToSettings()
        }
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        if (!viewModel.verifyDonation(requestCode, resultCode, data)) {
            super.onActivityResult(requestCode, resultCode, data)
        }
    }

    override fun onNavigationItemSelected(item: MenuItem): Boolean {
        when (item.itemId) {
            R.id.nav_wallpapers -> goToWallpapers()
            R.id.nav_settings -> goToSettings()
            R.id.nav_about -> AboutDialog.show(this@MainActivity)
            R.id.nav_donate -> DonateDialog.show(this@MainActivity)
            R.id.nav_share -> shareApp()
            R.id.nav_rate -> rateApp()
        }
        vDrawer.postDelayed({
            vDrawer.closeDrawer(GravityCompat.START)
        }, 100)
        return true
    }

    override fun onDonate(sku: String) {
        viewModel.donate(this, sku)
    }

    private fun showUpdateAppDialog() {
        AlertDialog.Builder(this)
            .setTitle(R.string.update_available)
            .setMessage(R.string.new_version_available_update_now)
            .setNegativeButton(R.string.no, null)
            .setPositiveButton(R.string.yes) { _, _ ->
                showAppInPlayStore()
            }
            .show()
    }

    private fun shareApp() {
        "${getString(R.string.you_should_try)}\n${App.PLAY_STORE_URL}".share(this)
        Analytics.logShareApp()
    }

    private fun rateApp() {
        showAppInPlayStore()
        Analytics.logRateApp()
    }

    private fun goToWallpapers() {
        vToolbar.title = getString(R.string.wallpapers)
        vContent.currentItem = 0
    }

    private fun goToSettings() {
        vToolbar.title = getString(R.string.settings)
        vContent.currentItem = 1
    }

    private fun showAppInPlayStore() {
        try {
            Uri.parse(App.MARKET_URL).open(this)
            Analytics.logOpenUrl(App.MARKET_URL)
        } catch (e: Exception) {
            Uri.parse(App.PLAY_STORE_URL).open(this)
            Analytics.logOpenUrl(App.PLAY_STORE_URL)
        }
    }

    inner class SectionsPagerAdapter(fm: FragmentManager) : FragmentPagerAdapter(fm) {

        private val sections by lazy { listOf(
            WallpaperListFragment(),
            SettingsFragment())
        }

        override fun getItem(position: Int) = sections[position]

        override fun getCount() = sections.size

    }

}
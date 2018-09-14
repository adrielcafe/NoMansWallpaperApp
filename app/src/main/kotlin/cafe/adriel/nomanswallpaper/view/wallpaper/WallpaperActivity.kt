package cafe.adriel.nomanswallpaper.view.wallpaper

import android.Manifest
import android.app.Activity
import android.content.Intent
import android.graphics.Color
import android.graphics.drawable.ColorDrawable
import android.graphics.drawable.Drawable
import android.os.Bundle
import android.view.View
import android.view.animation.AnimationUtils
import androidx.appcompat.app.AppCompatActivity
import androidx.core.app.ActivityOptionsCompat
import androidx.lifecycle.Observer
import cafe.adriel.nomanswallpaper.R
import cafe.adriel.nomanswallpaper.model.Wallpaper
import cafe.adriel.nomanswallpaper.util.*
import com.bumptech.glide.load.DataSource
import com.bumptech.glide.load.engine.GlideException
import com.bumptech.glide.request.RequestListener
import com.bumptech.glide.request.target.Target
import com.google.android.material.floatingactionbutton.FloatingActionButton
import com.google.android.material.snackbar.Snackbar
import com.hlab.fabrevealmenu.helper.AnimationHelper
import com.hlab.fabrevealmenu.listeners.OnFABMenuSelectedListener
import com.markodevcic.peko.Peko
import com.markodevcic.peko.rationale.SnackBarRationale
import kotlinx.android.synthetic.main.activity_wallpaper.*
import kotlinx.coroutines.experimental.android.UI
import kotlinx.coroutines.experimental.delay
import kotlinx.coroutines.experimental.launch
import org.koin.androidx.viewmodel.ext.android.viewModel

class WallpaperActivity : AppCompatActivity(), OnFABMenuSelectedListener {

    companion object {
        private const val EXTRA_WALLPAPER = "wallpaper"

        fun start(activity: Activity, wallpaper: Wallpaper, transitionView: View){
            val intent = Intent(activity, WallpaperActivity::class.java).apply {
                putExtra(EXTRA_WALLPAPER, wallpaper)
            }
            val options = ActivityOptionsCompat
                .makeSceneTransitionAnimation(activity, transitionView, transitionView.transitionName)
            activity.startActivity(intent, options.toBundle())
        }
    }

    private val viewModel by viewModel<WallpaperViewModel>()
    private val wallpaper by lazy {
        intent.getParcelableExtra(EXTRA_WALLPAPER) as Wallpaper
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_wallpaper)

        val mainColor = ColorDrawable(Color.parseColor(wallpaper.mainColorHex))
        vClose.setOnClickListener { exit() }
        vWallpaper.background = mainColor
        vAuthor.text = if(wallpaper.author.isNotBlank()) wallpaper.author else "?"
        vAuthor.compoundDrawablesRelative[0]?.run {
            setTint(Color.BLACK)
            bounds.inset(2.px, 2.px)
        }
        with(vOptionsMenu) {
            bindAnchorView(vShowOptions)
            setOnFABMenuSelectedListener(this@WallpaperActivity)
            getItemById(R.id.opt_set_wallpaper).iconDrawable.setTint(Color.WHITE)
            getItemById(R.id.opt_set_as).iconDrawable.setTint(Color.WHITE)
            getItemById(R.id.opt_download).iconDrawable.setTint(Color.WHITE)
            getItemById(R.id.opt_share).iconDrawable.setTint(Color.WHITE)
        }

        supportPostponeEnterTransition()
        GlideApp.with(applicationContext)
            .load(wallpaper.url)
            .placeholder(mainColor)
            .dontTransform()
            .dontAnimate()
            .listener(object : RequestListener<Drawable> {
                override fun onLoadFailed(e: GlideException?, model: Any?, target: Target<Drawable>?, isFirstResource: Boolean): Boolean {
                    supportStartPostponedEnterTransition()
                    return false
                }
                override fun onResourceReady(resource: Drawable?, model: Any?, target: Target<Drawable>?, dataSource: DataSource?, isFirstResource: Boolean): Boolean {
                    supportStartPostponedEnterTransition()
                    return false
                }
            })
            .into(vWallpaper)

        viewModel.getWallpaperUpdated().observe(this, Observer { onWallpaperUpdated(it) })
        viewModel.getWallpaperDownloaded().observe(this, Observer { onWallpaperDownloaded(it) })
    }

    override fun onPostCreate(savedInstanceState: Bundle?) {
        super.onPostCreate(savedInstanceState)
        vShowOptions.postDelayed({
            vClose.visibility = View.VISIBLE
            vAuthor.visibility = View.VISIBLE
            vClose.startAnimation(AnimationUtils.loadAnimation(this@WallpaperActivity, R.anim.fade_in))
            vAuthor.startAnimation(AnimationUtils.loadAnimation(this@WallpaperActivity, R.anim.slide_in))
            vShowOptions.show()
        }, 500)
    }

    override fun onBackPressed() {
        if(vShowOptions.isOrWillBeHidden) {
            super.onBackPressed()
        } else {
            exit(true)
        }
    }

    override fun onMenuItemSelected(view: View?, id: Int) {
        launch(UI) {
            delay(AnimationHelper.REVEAL_DURATION)
            when (id) {
                R.id.opt_set_wallpaper ->
                    if(isConnected()) viewModel.setWallpaper(wallpaper, true)
                R.id.opt_set_as ->
                    if(isConnected()) viewModel.setWallpaper(wallpaper, false)
                R.id.opt_download ->
                    if(isConnected()) downloadWallpaper(wallpaper)
                R.id.opt_share -> shareWallpaper(wallpaper)
            }
        }
    }

    private fun onWallpaperUpdated(success: Boolean){
        Snackbar.make(vRoot,
            if(success) R.string.wallpaper_set else R.string.something_went_wrong,
            Snackbar.LENGTH_LONG).show()
    }

    private fun onWallpaperDownloaded(wallpaperUri: String){
        val message =
            if(wallpaperUri.isNotBlank()) R.string.wallpaper_downloaded
            else R.string.something_went_wrong
        Snackbar.make(vRoot, message, Snackbar.LENGTH_LONG).run {
            if(wallpaperUri.isNotBlank()) {
                setAction(R.string.open) {
                    viewModel.showWallpaperInGallery(wallpaperUri)
                }
            }
            show()
        }
    }

    private fun downloadWallpaper(wallpaper: Wallpaper){
        launch(UI) {
            val rationaleSnackBar =
                Snackbar.make(vRoot, R.string.permissions_needed, Snackbar.LENGTH_LONG)
            val rationale = SnackBarRationale(rationaleSnackBar, getString(R.string.allow))
            val result = Peko.requestPermissionsAsync(this@WallpaperActivity,
                Manifest.permission.WRITE_EXTERNAL_STORAGE, rationale = rationale).await()
            if (Manifest.permission.WRITE_EXTERNAL_STORAGE in result.grantedPermissions) {
                viewModel.downloadWallpaper(wallpaper)
            }
        }
    }

    private fun shareWallpaper(wallpaper: Wallpaper){
        wallpaper.url.share(this)
        Analytics.logShareWallpaper(wallpaper)
    }

    private fun exit(backPressed: Boolean = false){
        vClose.startAnimation(AnimationUtils.loadAnimation(this@WallpaperActivity, R.anim.fade_out))
        vAuthor.startAnimation(AnimationUtils.loadAnimation(this@WallpaperActivity, R.anim.slide_out))
        vShowOptions.hide(object : FloatingActionButton.OnVisibilityChangedListener() {
            override fun onHidden(fab: FloatingActionButton?) {
                vClose.visibility = View.INVISIBLE
                vAuthor.visibility = View.INVISIBLE
                if(backPressed) onBackPressed() else finishAfterTransition()
            }
        })
    }

}
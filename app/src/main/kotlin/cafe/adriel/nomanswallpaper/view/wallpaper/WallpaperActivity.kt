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
import androidx.core.app.ActivityOptionsCompat
import androidx.lifecycle.Observer
import cafe.adriel.androidcoroutinescopes.appcompat.CoroutineScopedActivity
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
import com.tinsuke.icekick.extension.freezeInstanceState
import com.tinsuke.icekick.extension.parcelLateState
import com.tinsuke.icekick.extension.unfreezeInstanceState
import kotlinx.android.synthetic.main.activity_wallpaper.*
import kotlinx.coroutines.experimental.delay
import kotlinx.coroutines.experimental.launch
import org.koin.androidx.viewmodel.ext.android.viewModel

class WallpaperActivity : CoroutineScopedActivity(), OnFABMenuSelectedListener {

    companion object {
        private const val EXTRA_WALLPAPER = "wallpaper"

        fun start(activity: Activity, wallpaper: Wallpaper, transitionView: View) {
            val intent = Intent(activity, WallpaperActivity::class.java).apply {
                putExtra(EXTRA_WALLPAPER, wallpaper)
            }
            val options = ActivityOptionsCompat
                .makeSceneTransitionAnimation(activity, transitionView, transitionView.transitionName)
            activity.startActivity(intent, options.toBundle())
        }
    }

    private val viewModel by viewModel<WallpaperViewModel>()
    private var wallpaper: Wallpaper by parcelLateState()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        unfreezeInstanceState(savedInstanceState)

        setContentView(R.layout.activity_wallpaper)

        if(savedInstanceState == null) {
            intent?.run {
                if (hasExtra(EXTRA_WALLPAPER)) {
                    wallpaper = getParcelableExtra(EXTRA_WALLPAPER)
                }
            }
        }

        vClose.setOnClickListener { exit() }
        if (wallpaper.author.isNotBlank()) {
            vAuthor.visibility = View.INVISIBLE
            vAuthor.text = wallpaper.author
            vAuthor.compoundDrawablesRelative[0]?.run {
                setTint(Color.BLACK)
                bounds.inset(2.px, 2.px)
            }
        } else {
            vAuthor.visibility = View.GONE
        }
        with(vOptionsMenu) {
            bindAnchorView(vShowOptions)
            setOnFABMenuSelectedListener(this@WallpaperActivity)
            getItemById(R.id.opt_set_wallpaper).iconDrawable.setTint(Color.WHITE)
            getItemById(R.id.opt_set_as).iconDrawable.setTint(Color.WHITE)
            getItemById(R.id.opt_download).iconDrawable.setTint(Color.WHITE)
            getItemById(R.id.opt_share).iconDrawable.setTint(Color.WHITE)
            getItemById(R.id.opt_copy_url).iconDrawable.setTint(Color.WHITE)
        }

        val imageUrl = if(wallpaper.thumbUrl.isNotBlank()) wallpaper.thumbUrl else wallpaper.url
        val imageColor = try {
            Color.parseColor(wallpaper.colorHex)
        } catch (e: Exception){
            Color.BLACK
        }
        val imageColorDrawable = ColorDrawable(imageColor)
        vWallpaper.background = imageColorDrawable

        supportPostponeEnterTransition()
        GlideApp.with(applicationContext)
            .load(imageUrl)
            .placeholder(imageColorDrawable)
            .dontTransform()
            .dontAnimate()
            .listener(object : RequestListener<Drawable> {
                override fun onLoadFailed(e: GlideException?, model: Any?,
                                          target: Target<Drawable>?,
                                          isFirstResource: Boolean): Boolean {
                    supportStartPostponedEnterTransition()
                    return false
                }

                override fun onResourceReady(resource: Drawable?, model: Any?,
                                             target: Target<Drawable>?, dataSource: DataSource?,
                                             isFirstResource: Boolean): Boolean {
                    supportStartPostponedEnterTransition()
                    return false
                }
            })
            .into(vWallpaper)

        viewModel.wallpaperUpdated.observe(this, Observer { onWallpaperUpdated(it) })
        viewModel.wallpaperDownloaded.observe(this, Observer { onWallpaperDownloaded(it) })
    }

    override fun onPostCreate(savedInstanceState: Bundle?) {
        super.onPostCreate(savedInstanceState)
        vShowOptions.postDelayed({
            if(vAuthor.visibility != View.GONE) {
                vAuthor.visibility = View.VISIBLE
                vAuthor.startAnimation(
                    AnimationUtils.loadAnimation(this@WallpaperActivity, R.anim.slide_in))
            }
            vClose.visibility = View.VISIBLE
            vClose.startAnimation(
                AnimationUtils.loadAnimation(this@WallpaperActivity, R.anim.fade_in))
            vShowOptions.show()
        }, 500)
    }

    override fun onSaveInstanceState(outState: Bundle) {
        super.onSaveInstanceState(outState)
        freezeInstanceState(outState)
    }

    override fun onBackPressed() {
        if (vShowOptions.isOrWillBeHidden) {
            super.onBackPressed()
        } else {
            exit(true)
        }
    }

    override fun onMenuItemSelected(view: View?, id: Int) {
        launch {
            val loadingSnackBar = Snackbar.make(vRoot,
                R.string.downloading_wallpaper, Snackbar.LENGTH_LONG)
            delay(AnimationHelper.REVEAL_DURATION * 2)
            when (id) {
                R.id.opt_set_wallpaper -> if (isConnected()) {
                    loadingSnackBar.show()
                    viewModel.setWallpaper(wallpaper, true)
                }
                R.id.opt_set_as -> if (isConnected()) {
                    loadingSnackBar.show()
                    viewModel.setWallpaper(wallpaper, false)
                }
                R.id.opt_download -> if (isConnected()) {
                    loadingSnackBar.show()
                    downloadWallpaper(wallpaper)
                }
                R.id.opt_share -> if (isConnected()) {
                    loadingSnackBar.show()
                    viewModel.shareWallpaper(wallpaper)
                }
                R.id.opt_copy_url -> {
                    loadingSnackBar.dismiss()
                    copyWallpaperUrl(wallpaper)
                }
            }
        }
    }

    private fun onWallpaperUpdated(success: Boolean) {
        Snackbar.make(
            vRoot,
            if (success) R.string.wallpaper_set else R.string.something_went_wrong,
            Snackbar.LENGTH_LONG
        ).show()
    }

    private fun onWallpaperDownloaded(wallpaperUri: String) {
        val message = if (wallpaperUri.isNotBlank())
            R.string.saved_in_gallery
        else
            R.string.something_went_wrong
        Snackbar.make(vRoot, message, Snackbar.LENGTH_LONG).run {
            if (message == R.string.saved_in_gallery) {
                setAction(R.string.open) { viewModel.showWallpaperInGallery(wallpaperUri) }
            }
            show()
        }
    }

    private fun downloadWallpaper(wallpaper: Wallpaper) {
        launch {
            val rationaleSnackBar =
                Snackbar.make(vRoot, R.string.permissions_needed, Snackbar.LENGTH_LONG)
            val rationale = SnackBarRationale(rationaleSnackBar, getString(R.string.allow))
            val result = Peko.requestPermissionsAsync(
                this@WallpaperActivity,
                Manifest.permission.WRITE_EXTERNAL_STORAGE, rationale = rationale
            ).await()
            if (Manifest.permission.WRITE_EXTERNAL_STORAGE in result.grantedPermissions) {
                viewModel.downloadWallpaper(wallpaper)
            }
        }
    }

    private fun copyWallpaperUrl(wallpaper: Wallpaper) {
        val message =
            if (wallpaper.url.copyToClipboard(this)) R.string.url_copied_clipboard
            else R.string.something_went_wrong
        Snackbar.make(vRoot, message, Snackbar.LENGTH_LONG).show()
        Analytics.logCopyWallpaperUrl(wallpaper)
    }

    private fun exit(backPressed: Boolean = false) {
        vClose.startAnimation(
            AnimationUtils.loadAnimation(this@WallpaperActivity, R.anim.fade_out))
        vAuthor.startAnimation(
            AnimationUtils.loadAnimation(this@WallpaperActivity, R.anim.slide_out))
        vShowOptions.hide(object : FloatingActionButton.OnVisibilityChangedListener() {
            override fun onHidden(fab: FloatingActionButton?) {
                vClose.visibility = View.INVISIBLE
                vAuthor.visibility = View.INVISIBLE
                if (backPressed) onBackPressed() else finishAfterTransition()
            }
        })
    }

}
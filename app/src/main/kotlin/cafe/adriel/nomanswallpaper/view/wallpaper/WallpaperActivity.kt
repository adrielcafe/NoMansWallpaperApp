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
import androidx.core.content.res.ResourcesCompat
import androidx.lifecycle.Observer
import cafe.adriel.androidcoroutinescopes.appcompat.CoroutineScopedActivity
import cafe.adriel.nomanswallpaper.R
import cafe.adriel.nomanswallpaper.model.Wallpaper
import cafe.adriel.nomanswallpaper.util.*
import com.bumptech.glide.load.DataSource
import com.bumptech.glide.load.engine.GlideException
import com.bumptech.glide.request.RequestListener
import com.bumptech.glide.request.target.Target
import com.crashlytics.android.Crashlytics
import com.google.android.material.floatingactionbutton.FloatingActionButton
import com.google.android.material.snackbar.Snackbar
import com.hlab.fabrevealmenu.helper.AnimationHelper
import com.hlab.fabrevealmenu.listeners.OnFABMenuSelectedListener
import com.hlab.fabrevealmenu.listeners.OnMenuStateChangedListener
import com.markodevcic.peko.Peko
import com.markodevcic.peko.PermissionRequestResult
import com.markodevcic.peko.rationale.SnackBarRationale
import com.tinsuke.icekick.extension.freezeInstanceState
import com.tinsuke.icekick.extension.parcelState
import com.tinsuke.icekick.extension.unfreezeInstanceState
import kotlinx.android.synthetic.main.activity_wallpaper.*
import kotlinx.coroutines.experimental.delay
import kotlinx.coroutines.experimental.launch
import org.koin.androidx.viewmodel.ext.android.viewModel

class WallpaperActivity : CoroutineScopedActivity(), OnFABMenuSelectedListener, OnMenuStateChangedListener {

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
    private var wallpaper: Wallpaper? by parcelState()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        unfreezeInstanceState(savedInstanceState)
        setContentView(R.layout.activity_wallpaper)

        intent?.run {
            if (hasExtra(EXTRA_WALLPAPER)) {
                wallpaper = getParcelableExtra(EXTRA_WALLPAPER)
            }
        }

        if(wallpaper == null){
            finish()
            return
        }

        wallpaper?.let {
            if (it.author.isNotBlank()) {
                vAuthor.visibility = View.INVISIBLE
                vAuthor.text = it.author
                vAuthor.compoundDrawablesRelative[0]?.run {
                    setTint(Color.BLACK)
                    bounds.inset(2.px, 2.px)
                }
            } else {
                vAuthor.visibility = View.GONE
            }

            val imageUrl = if(it.thumbUrl.isNotBlank() && !Settings.isHighQualityThumb(this))
                it.thumbUrl
            else
                it.url
            val imageColor = try {
                Color.parseColor(it.colorHex)
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
        }

        vClose.setOnClickListener { exit() }
        with(vOptionsMenu) {
            bindAnchorView(vShowOptions)
            setOnMenuStateChangedListener(this@WallpaperActivity)
            setOnFABMenuSelectedListener(this@WallpaperActivity)
            getItemById(R.id.opt_set_wallpaper).iconDrawable.setTint(Color.WHITE)
            getItemById(R.id.opt_set_as).iconDrawable.setTint(Color.WHITE)
            getItemById(R.id.opt_download).iconDrawable.setTint(Color.WHITE)
            getItemById(R.id.opt_share).iconDrawable.setTint(Color.WHITE)
            getItemById(R.id.opt_copy_url).iconDrawable.setTint(Color.WHITE)
        }

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
            delay((AnimationHelper.REVEAL_DURATION * 2).toLong())
            wallpaper?.also {
                when (id) {
                    R.id.opt_set_wallpaper -> if (isConnected()) {
                        loadingSnackBar.show()
                        viewModel.setWallpaper(it, true)
                    }
                    R.id.opt_set_as -> if (isConnected()) {
                        loadingSnackBar.show()
                        viewModel.setWallpaper(it, false)
                    }
                    R.id.opt_favorite -> {
                        loadingSnackBar.dismiss()
                        toggleFavorite(it)
                    }
                    R.id.opt_download -> if (isConnected()) {
                        loadingSnackBar.dismiss()
                        downloadWallpaper(it)
                    }
                    R.id.opt_share -> if (isConnected()) {
                        loadingSnackBar.show()
                        viewModel.shareWallpaper(it)
                    }
                    R.id.opt_copy_url -> {
                        loadingSnackBar.dismiss()
                        copyWallpaperUrl(it)
                    }
                }
            }
        }
    }

    override fun onExpand() {
        wallpaper?.let {
            viewModel.isFavorite(it).observeOnce(this, Observer { isFavorite ->
                val menuItem = vOptionsMenu.getItemById(R.id.opt_favorite)
                if(isFavorite){
                    menuItem.title = getString(R.string.remove_favorites)
                    menuItem.iconDrawable = ResourcesCompat.getDrawable(
                        resources, R.drawable.ic_favorite, null)?.apply {
                            setTint(Color.WHITE)
                        }
                } else {
                    menuItem.title = getString(R.string.add_favorites)
                    menuItem.iconDrawable = ResourcesCompat.getDrawable(
                        resources, R.drawable.ic_favorite_outline, null)?.apply {
                            setTint(Color.WHITE)
                        }
                }
                menuItem.iconDrawable.alpha = 255
            })
        }
    }

    override fun onCollapse() {

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

    private fun toggleFavorite(wallpaper: Wallpaper){
        viewModel.toggleFavorite(wallpaper).observeOnce(this, Observer { isFavorite ->
            val message = if(isFavorite) R.string.added_favorites else R.string.removed_favorites
            Snackbar.make(vRoot, message, Snackbar.LENGTH_SHORT).show()
            postEvent(FavoriteWallpaperEvent(wallpaper), true)
        })
    }

    private fun downloadWallpaper(wallpaper: Wallpaper) {
        launch {
            val permissionResult = try {
                val rationaleSnackBar =
                    Snackbar.make(vRoot, R.string.permissions_needed, Snackbar.LENGTH_LONG)
                val rationale = SnackBarRationale(rationaleSnackBar, getString(R.string.allow))
                Peko.requestPermissionsAsync(
                    this@WallpaperActivity,
                    Manifest.permission.WRITE_EXTERNAL_STORAGE, rationale = rationale
                ).await()
            } catch (e: Exception){
                Crashlytics.logException(e)
                e.printStackTrace()
                PermissionRequestResult(emptyList(), emptyList())
            }
            if (Manifest.permission.WRITE_EXTERNAL_STORAGE in permissionResult.grantedPermissions) {
                Snackbar.make(vRoot, R.string.downloading_wallpaper, Snackbar.LENGTH_LONG).show()
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
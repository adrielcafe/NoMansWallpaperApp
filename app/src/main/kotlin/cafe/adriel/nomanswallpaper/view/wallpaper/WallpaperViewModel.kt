package cafe.adriel.nomanswallpaper.view.wallpaper

import android.app.Application
import android.app.WallpaperManager
import android.content.Context
import android.content.Intent
import android.net.Uri
import android.provider.MediaStore
import androidx.core.content.FileProvider
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import cafe.adriel.nomanswallpaper.App
import cafe.adriel.nomanswallpaper.BuildConfig
import cafe.adriel.nomanswallpaper.R
import cafe.adriel.nomanswallpaper.model.Wallpaper
import cafe.adriel.nomanswallpaper.util.Analytics
import com.bumptech.glide.Glide
import com.crashlytics.android.Crashlytics
import kotlinx.coroutines.experimental.IO
import kotlinx.coroutines.experimental.android.UI
import kotlinx.coroutines.experimental.launch
import kotlinx.coroutines.experimental.withContext
import java.io.File

class WallpaperViewModel(app: Application) : AndroidViewModel(app){

    companion object {
        private const val PROVIDER_AUTHORITY = "${BuildConfig.APPLICATION_ID}.provider"
        private const val DEFAULT_FILE_NAME = "wallpaper.jpg"
    }

    private val wallpaperUpdated = MutableLiveData<Boolean>()
    private val wallpaperDownloaded = MutableLiveData<String>()

    fun getWallpaperUpdated(): LiveData<Boolean> = wallpaperUpdated

    fun getWallpaperDownloaded(): LiveData<String> = wallpaperDownloaded

    fun setWallpaper(wallpaper: Wallpaper, quick: Boolean = false){
        launch(UI) {
            try {
                val wallpaperFile = getWallpaperFile(wallpaper)
                if(quick) {
                    setWallpaper(wallpaperFile)
                    wallpaperUpdated.value = true
                } else {
                    showWallpaperOptions(wallpaperFile)
                }
                Analytics.logSetWallpaper(wallpaper)
            } catch (e: Exception){
                Crashlytics.logException(e)
                e.printStackTrace()
                wallpaperUpdated.value = false
            }
        }
    }

    fun downloadWallpaper(wallpaper: Wallpaper){
        launch(UI) {
            try {
                val wallpaperUri = saveWallpaperInGallery(wallpaper)
                wallpaperDownloaded.value = wallpaperUri
                Analytics.logDownloadWallpaper(wallpaper)
            } catch (e: Exception){
                Crashlytics.logException(e)
                e.printStackTrace()
                wallpaperDownloaded.value = ""
            }
        }
    }

    fun showWallpaperInGallery(wallpaperUri: String){
        Intent(Intent.ACTION_VIEW).run {
            setDataAndType(Uri.parse(wallpaperUri), "image/*")
            getApplication<App>().startActivity(this)
        }
    }

    private fun showWallpaperOptions(wallpaperFile: File){
        val context = getApplication<App>() as Context
        val uri = FileProvider.getUriForFile(context, PROVIDER_AUTHORITY, wallpaperFile)
        val intent = Intent(Intent.ACTION_ATTACH_DATA).apply {
            setDataAndType(uri, "image/*")
            addCategory(Intent.CATEGORY_DEFAULT)
            addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION)
            putExtra("mimeType", "image/*")
        }
        context.startActivity(Intent.createChooser(intent, context.getString(R.string.set_as)))
    }

    private suspend fun setWallpaper(wallpaperFile: File) = withContext(IO){
        WallpaperManager.getInstance(getApplication<App>())
            .setStream(wallpaperFile.inputStream())
    }

    private suspend fun saveWallpaperInGallery(wallpaper: Wallpaper) = withContext(IO){
        val wallpaperFile = getWallpaperFile(wallpaper)
        val wallpaperUri = MediaStore.Images.Media.insertImage(
            getApplication<App>().contentResolver,
            wallpaperFile.absolutePath, wallpaperFile.name, "By ${wallpaper.author}"
        )
        val mediaScanIntent = Intent(Intent.ACTION_MEDIA_SCANNER_SCAN_FILE)
        mediaScanIntent.data = Uri.parse(wallpaperUri)
        getApplication<App>().sendBroadcast(mediaScanIntent)

        wallpaperUri
    }

    private suspend fun getWallpaperFile(wallpaper: Wallpaper) = withContext(IO) {
        val file = Glide.with(getApplication<App>())
            .downloadOnly()
            .load(wallpaper.url)
            .submit()
            .get()
        File(file.parent, DEFAULT_FILE_NAME).apply {
            file.renameTo(this)
        }
    }
}
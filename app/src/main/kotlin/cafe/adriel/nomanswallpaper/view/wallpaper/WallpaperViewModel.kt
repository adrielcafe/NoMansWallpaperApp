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
import androidx.lifecycle.viewModelScope
import cafe.adriel.nomanswallpaper.App
import cafe.adriel.nomanswallpaper.BuildConfig
import cafe.adriel.nomanswallpaper.R
import cafe.adriel.nomanswallpaper.model.Wallpaper
import cafe.adriel.nomanswallpaper.repository.WallpaperRepository
import cafe.adriel.nomanswallpaper.util.Analytics
import com.bumptech.glide.Glide
import com.google.firebase.crashlytics.FirebaseCrashlytics
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import java.io.File

class WallpaperViewModel(app: Application, private val wallpaperRepo: WallpaperRepository) : AndroidViewModel(app) {

    companion object {
        private const val PROVIDER_AUTHORITY = "${BuildConfig.APPLICATION_ID}.provider"
        private const val MIME_TYPE_IMAGE = "image/*"
        private const val DEFAULT_FILE_NAME = "wallpaper.jpg"
    }

    private val _wallpaperUpdated = MutableLiveData<Boolean>()
    private val _wallpaperDownloaded = MutableLiveData<String>()

    val wallpaperUpdated: LiveData<Boolean> get() = _wallpaperUpdated
    val wallpaperDownloaded: LiveData<String> get() = _wallpaperDownloaded

    fun isFavorite(wallpaper: Wallpaper): LiveData<Boolean> {
        val favorite = MutableLiveData<Boolean>()
        viewModelScope.launch {
            favorite.value = wallpaperRepo.isFavorite(wallpaper)
        }
        return favorite
    }

    fun toggleFavorite(wallpaper: Wallpaper): LiveData<Boolean> {
        val favorite = MutableLiveData<Boolean>()
        viewModelScope.launch {
            if(wallpaperRepo.isFavorite(wallpaper)){
                wallpaperRepo.removeFavorite(wallpaper)
                favorite.value = false
            } else {
                wallpaperRepo.addFavorite(wallpaper)
                favorite.value = true
                Analytics.logFavoriteWallpaper(wallpaper)
            }
        }
        return favorite
    }

    fun setWallpaper(wallpaper: Wallpaper, quick: Boolean = false) {
        viewModelScope.launch {
            try {
                val wallpaperFile = getWallpaperFile(wallpaper)
                if (quick) {
                    setWallpaper(wallpaperFile)
                    _wallpaperUpdated.value = true
                } else {
                    showSetWallpaperOptions(wallpaperFile)
                }
                Analytics.logSetWallpaper(wallpaper)
            } catch (e: Exception) {
                FirebaseCrashlytics.getInstance().recordException(e)
                e.printStackTrace()
                _wallpaperUpdated.value = false
            }
        }
    }

    fun shareWallpaper(wallpaper: Wallpaper) {
        viewModelScope.launch {
            try {
                val context = getApplication<App>() as Context
                val wallpaperFile = getWallpaperFile(wallpaper)
                val uri = FileProvider.getUriForFile(context, PROVIDER_AUTHORITY, wallpaperFile)
                val intent = Intent(Intent.ACTION_SEND).apply {
                    addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
                    putExtra(Intent.EXTRA_STREAM, uri)
                    type = MIME_TYPE_IMAGE
                }
                context.startActivity(
                    Intent.createChooser(intent, context.getString(R.string.share_with)).addFlags(Intent.FLAG_ACTIVITY_NEW_TASK))
                Analytics.logShareWallpaper(wallpaper)
            } catch (e: Exception) {
                FirebaseCrashlytics.getInstance().recordException(e)
                e.printStackTrace()
            }
        }
    }

    fun downloadWallpaper(wallpaper: Wallpaper) {
        viewModelScope.launch {
            try {
                val wallpaperUri = saveWallpaperInGallery(wallpaper)
                _wallpaperDownloaded.value = wallpaperUri
                Analytics.logDownloadWallpaper(wallpaper)
            } catch (e: Exception) {
                FirebaseCrashlytics.getInstance().recordException(e)
                e.printStackTrace()
                _wallpaperDownloaded.value = ""
            }
        }
    }

    fun showWallpaperInGallery(wallpaperUri: String) {
        Intent(Intent.ACTION_VIEW).run {
            try {
                addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
                setDataAndType(Uri.parse(wallpaperUri), MIME_TYPE_IMAGE)
                getApplication<App>().startActivity(this)
            } catch (e: Exception){
                FirebaseCrashlytics.getInstance().recordException(e)
                e.printStackTrace()
            }
        }
    }

    private fun showSetWallpaperOptions(wallpaperFile: File) {
        val context = getApplication<App>() as Context
        val uri = FileProvider.getUriForFile(context, PROVIDER_AUTHORITY, wallpaperFile)
        val intent = Intent(Intent.ACTION_ATTACH_DATA).apply {
            setDataAndType(uri, MIME_TYPE_IMAGE)
            addCategory(Intent.CATEGORY_DEFAULT)
            addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION)
            addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
            putExtra("mimeType", MIME_TYPE_IMAGE)
        }
        context.startActivity(
            Intent.createChooser(intent, context.getString(R.string.set_as)).addFlags(Intent.FLAG_ACTIVITY_NEW_TASK))
    }

    private suspend fun setWallpaper(wallpaperFile: File) = withContext(Dispatchers.IO) {
        WallpaperManager.getInstance(getApplication<App>())
            .setStream(wallpaperFile.inputStream())
    }

    private suspend fun saveWallpaperInGallery(wallpaper: Wallpaper): String = withContext(Dispatchers.IO) {
        val context = getApplication<App>() as Context
        val wallpaperFile = getWallpaperFile(wallpaper)
        val wallpaperUri = MediaStore.Images.Media.insertImage(
            context.contentResolver,
            wallpaperFile.absolutePath, wallpaperFile.name, "By ${wallpaper.author}"
        )
        val intent = Intent(Intent.ACTION_MEDIA_SCANNER_SCAN_FILE).apply {
            data = Uri.parse(wallpaperUri)
        }
        context.sendBroadcast(intent)

        wallpaperUri
    }

    private suspend fun getWallpaperFile(wallpaper: Wallpaper): File = withContext(Dispatchers.IO) {
        val file = Glide.with(getApplication<App>())
            .downloadOnly()
            .load(wallpaper.url)
            .submit()
            .get()
        val renamedFile = File(file.parent, DEFAULT_FILE_NAME)
        file.renameTo(renamedFile)
        renamedFile
    }
}
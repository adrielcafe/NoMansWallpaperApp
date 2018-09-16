package cafe.adriel.nomanswallpaper.view.main.wallpaperlist

import android.app.Application
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import cafe.adriel.androidcoroutinescopes.viewmodel.CoroutineScopedAndroidViewModel
import cafe.adriel.nomanswallpaper.model.Wallpaper
import cafe.adriel.nomanswallpaper.repository.WallpaperRepository
import kotlinx.coroutines.experimental.launch

class WallpaperListViewModel(app: Application, private val wallpaperRepo: WallpaperRepository) :
    CoroutineScopedAndroidViewModel(app) {

    private val _wallpapers = MutableLiveData<List<Wallpaper>>()

    val wallpapers: LiveData<List<Wallpaper>> get() = _wallpapers

    fun loadWallpapers() {
        launch {
            _wallpapers.value = wallpaperRepo.getWallpapers()
        }
    }

}
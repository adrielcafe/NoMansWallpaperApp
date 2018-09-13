package cafe.adriel.nomanswallpaper.view.main.wallpaperlist

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import cafe.adriel.nomanswallpaper.model.Wallpaper
import cafe.adriel.nomanswallpaper.repository.WallpaperRepository
import kotlinx.coroutines.experimental.android.UI
import kotlinx.coroutines.experimental.launch

class WallpaperListViewModel(app: Application, private val wallpaperRepo: WallpaperRepository) : AndroidViewModel(app){

    private val wallpapers = MutableLiveData<List<Wallpaper>>()

    fun getWallpapers(): LiveData<List<Wallpaper>> = wallpapers

    fun loadWallpapers(){
        launch(UI) {
            wallpapers.value = wallpaperRepo.getWallpapers()
        }
    }

}
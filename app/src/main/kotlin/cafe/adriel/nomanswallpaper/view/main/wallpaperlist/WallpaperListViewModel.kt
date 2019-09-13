package cafe.adriel.nomanswallpaper.view.main.wallpaperlist

import android.app.Application
import androidx.lifecycle.*
import cafe.adriel.nomanswallpaper.model.Wallpaper
import cafe.adriel.nomanswallpaper.repository.WallpaperRepository
import com.crashlytics.android.Crashlytics
import kotlinx.coroutines.launch

class WallpaperListViewModel(app: Application, private val wallpaperRepo: WallpaperRepository) :
    AndroidViewModel(app) {

    enum class SortBy { RANDOM, NEWEST, OLDEST }

    private val _sortBy = MutableLiveData<SortBy>()
    private val _wallpapers = Transformations.switchMap(_sortBy) { sortBy ->
        val wallpapers = MutableLiveData<List<Wallpaper>>()
        viewModelScope.launch {
            try {
                wallpapers.value = wallpaperRepo.getWallpapers().run {
                    when(sortBy){
                        SortBy.NEWEST -> reversed()
                        SortBy.OLDEST-> this
                        else -> shuffled()
                    }
                }
            } catch (e: Exception){
                Crashlytics.logException(e)
                e.printStackTrace()
                wallpapers.value = emptyList()
            }
        }
        wallpapers
    }

    private var favorites = emptySet<String>()

    val wallpapers: LiveData<List<Wallpaper>> get() = _wallpapers

    init {
        loadWallpapers(SortBy.RANDOM)
    }

    fun isFavorite(wallpaper: Wallpaper) = wallpaper.id in favorites

    fun loadWallpapers(sortBy: SortBy = _sortBy.value ?: SortBy.RANDOM) {
        _sortBy.value = sortBy
    }

    suspend fun loadFavorites(): Set<String> {
        favorites = wallpaperRepo.getFavorites()
        return favorites
    }

}
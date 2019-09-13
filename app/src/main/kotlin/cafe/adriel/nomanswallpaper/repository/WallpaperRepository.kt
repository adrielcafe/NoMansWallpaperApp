package cafe.adriel.nomanswallpaper.repository

import cafe.adriel.nomanswallpaper.model.Wallpaper
import cafe.adriel.nomanswallpaper.util.mmkv
import com.crashlytics.android.Crashlytics
import com.google.android.gms.tasks.Tasks
import com.google.firebase.firestore.DocumentSnapshot
import com.google.firebase.firestore.FirebaseFirestore
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import java.util.*

class WallpaperRepository {

    companion object {
        // Firestore
        private const val COLLECTION_WALLPAPERS = "wallpapers"
        private const val QUERY_ORDER_BY = "createdAt"
        private const val QUERY_LIMIT = 1000L

        // MMKV
        private const val KEY_FAVORITES = "favorites"
    }

    // https://firestore.googleapis.com/v1beta1/projects/nmw-app/databases/(default)/documents/wallpapers
    suspend fun getWallpapers(): List<Wallpaper> = withContext(Dispatchers.IO) {
        val collectionQuery = FirebaseFirestore.getInstance()
            .collection(COLLECTION_WALLPAPERS)
            .orderBy(QUERY_ORDER_BY)
            .limit(QUERY_LIMIT)
            .get()
        with(collectionQuery) {
            Tasks.await(this)
            if(isSuccessful){
                val items = result?.documents?.also {
                        if(it.isNotEmpty()){
                            FirebaseFirestore.getInstance().disableNetwork()
                        }
                    }
                    ?.map(::toWallpaper)
                items ?: emptyList()
            } else {
                Crashlytics.logException(exception)
                exception?.printStackTrace()
                emptyList()
            }
        }
    }

    private fun toWallpaper(document: DocumentSnapshot) =
        Wallpaper(
            document.id,
            document.getString("url") ?: "",
            document.getString("colorHex") ?: "",
            document.getString("author") ?: "",
            document.getDate("createdAt") ?: Date()
        )

    suspend fun addFavorite(wallpaper: Wallpaper) = withContext(Dispatchers.IO) {
        getFavorites().toMutableSet().run {
            add(wallpaper.id)
            setFavorites(this)
        }
    }

    suspend fun removeFavorite(wallpaper: Wallpaper) = withContext(Dispatchers.IO) {
        getFavorites().toMutableSet().run {
            remove(wallpaper.id)
            setFavorites(this)
        }
    }

    suspend fun isFavorite(wallpaper: Wallpaper): Boolean = withContext(Dispatchers.IO) {
        wallpaper.id in getFavorites()
    }

    suspend fun getFavorites(): Set<String> = withContext(Dispatchers.IO) {
        mmkv.decodeStringSet(KEY_FAVORITES, emptySet())
    }

    private suspend fun setFavorites(favorites: Set<String>) = withContext(Dispatchers.IO) {
        mmkv.encode(KEY_FAVORITES, favorites)
    }

}

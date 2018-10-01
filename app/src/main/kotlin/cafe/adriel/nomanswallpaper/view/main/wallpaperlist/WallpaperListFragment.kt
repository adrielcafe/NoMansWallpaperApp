package cafe.adriel.nomanswallpaper.view.main.wallpaperlist

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.lifecycle.Observer
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import cafe.adriel.androidcoroutinescopes.appcompat.CoroutineScopedFragment
import cafe.adriel.nomanswallpaper.R
import cafe.adriel.nomanswallpaper.model.Wallpaper
import cafe.adriel.nomanswallpaper.util.*
import cafe.adriel.nomanswallpaper.view.custom.WallpaperImageView
import cafe.adriel.nomanswallpaper.view.wallpaper.WallpaperActivity
import cafe.adriel.nomanswallpaper.view.wallpaper.WallpaperViewModel
import com.bumptech.glide.Glide
import com.bumptech.glide.ListPreloader.PreloadModelProvider
import com.bumptech.glide.integration.recyclerview.RecyclerViewPreloader
import com.bumptech.glide.util.FixedPreloadSizeProvider
import com.google.android.material.snackbar.Snackbar
import com.kennyc.view.MultiStateView
import com.mikepenz.fastadapter.FastAdapter
import com.mikepenz.fastadapter.commons.adapters.FastItemAdapter
import com.mikepenz.fastadapter.listeners.ClickEventHook
import kotlinx.android.synthetic.main.fragment_wallpaper_list.*
import kotlinx.android.synthetic.main.fragment_wallpaper_list.view.*
import kotlinx.android.synthetic.main.item_wallpaper.view.*
import kotlinx.android.synthetic.main.state_empty.view.*
import kotlinx.coroutines.experimental.Dispatchers
import kotlinx.coroutines.experimental.launch
import kotlinx.coroutines.experimental.withContext
import org.greenrobot.eventbus.EventBus
import org.greenrobot.eventbus.Subscribe
import org.koin.androidx.viewmodel.ext.android.viewModel
import java.util.Collections.singletonList

class WallpaperListFragment : CoroutineScopedFragment() {

    companion object {
        private const val FILTER_ALL = ""
        private const val FILTER_FAVORITES = "favorites"
    }

    private val singleViewModel by viewModel<WallpaperViewModel>()
    private val listViewModel by viewModel<WallpaperListViewModel>()
    private lateinit var adapter: FastItemAdapter<WallpaperAdapterItem>

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        return inflater.inflate(R.layout.fragment_wallpaper_list, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        if (!::adapter.isInitialized) {
            adapter = FastItemAdapter()
            adapter.apply {
                setHasStableIds(true)
                withUseIdDistributor(true)
                withOnClickListener { view, _, item, _ ->
                    view?.run {
                        onListItemClicked(this, item)
                    }
                    true
                }
                withEventHook(object : ClickEventHook<WallpaperAdapterItem>() {
                    override fun onBindMany(viewHolder: RecyclerView.ViewHolder) =
                        viewHolder.itemView.run { listOf(vFavorite, vSet) }

                    override fun onClick(view: View?, position: Int, fastAdapter: FastAdapter<WallpaperAdapterItem>?, item: WallpaperAdapterItem?) {
                        if (view != null && item != null) {
                            onListItemClicked(view, item)
                        }
                    }
                })
                itemFilter.withFilterPredicate { item, constraint ->
                    if(constraint == FILTER_FAVORITES)
                        listViewModel.isFavorite(item.wallpaper)
                    else
                        true
                }
            }
        }

        val imageModelProvider = WallpaperPreloadModelProvider()
        val imageSizeProvider = FixedPreloadSizeProvider<String>(
            WallpaperImageView.IMAGE_WIDTH, WallpaperImageView.IMAGE_HEIGHT)
        val imagePreloader = RecyclerViewPreloader(
            Glide.with(this), imageModelProvider, imageSizeProvider, 10)

        with(view) {
            vRefresh.isRefreshing = true
            vRefresh.setOnRefreshListener { listViewModel.loadWallpapers() }

            vWallpaperList.addOnScrollListener(imagePreloader)
            vWallpaperList.addOnScrollListener(object : RecyclerView.OnScrollListener() {
                override fun onScrolled(recyclerView: RecyclerView, dx: Int, dy: Int) {
                    val manager = recyclerView.layoutManager as LinearLayoutManager
                    vRefresh.isEnabled = manager.findFirstCompletelyVisibleItemPosition() == 0
                }
            })
            vWallpaperList.adapter = adapter
        }

        singleViewModel.wallpaperUpdated.observe(this, Observer { onWallpaperUpdated(it) })
        listViewModel.wallpapers.observe(this, Observer { showWallpapers(it) })
    }

    override fun onStart() {
        super.onStart()
        EventBus.getDefault().register(this)
    }

    override fun onStop() {
        super.onStop()
        EventBus.getDefault().unregister(this)
    }

    private fun onListItemClicked(view: View, item: WallpaperAdapterItem) {
        when (view.id) {
            R.id.vItemRoot -> if(!item.isLoading(view)) showWallpaper(view.vWallpaper, item.wallpaper)
            R.id.vFavorite -> singleViewModel.toggleFavorite(item.wallpaper)
                .observeOnce(this, Observer {
                    launch {
                        listViewModel.loadFavorites()
                        item.updateFavorite(view)
                    }
                })
            R.id.vSet -> activity?.run {
                if (!item.isLoading(view.parent.parent as View) && isConnected()) {
                    Snackbar.make(findViewById(R.id.vRoot),
                        R.string.downloading_wallpaper, Snackbar.LENGTH_LONG).show()
                    singleViewModel.setWallpaper(item.wallpaper, false)
                }
            }
        }
    }

    private fun onWallpaperUpdated(success: Boolean) {
        activity?.run {
            Snackbar.make(
                findViewById(R.id.vRoot),
                if (success) R.string.wallpaper_set else R.string.something_went_wrong,
                Snackbar.LENGTH_SHORT
            ).show()
        }
    }

    private fun showWallpapers(wallpapers: List<Wallpaper>) {
        launch {
            listViewModel.loadFavorites()
            if (wallpapers.isNotEmpty()) {
                val adapterItems = withContext(Dispatchers.Default) {
                    wallpapers.map { wallpaper ->
                        WallpaperAdapterItem(wallpaper) {
                            listViewModel.isFavorite(wallpaper)
                        }
                    }
                }
                adapter.itemFilter.clear()
                adapter.itemFilter.add(adapterItems)
            }
            vRefresh.isRefreshing = false
            updateState()
        }
    }

    private fun showWallpaper(view: View, wallpaper: Wallpaper) {
        activity?.run {
            WallpaperActivity.start(this, wallpaper, view)
        }
    }

    private fun updateState(){
        vState?.viewState = if(adapter.adapterItems.isEmpty()) {
            val emptyView = vState?.getView(MultiStateView.VIEW_STATE_EMPTY) as ViewGroup
            emptyView.vStateIcon.loadImage(R.drawable.drawer_header_logo, null)
            MultiStateView.VIEW_STATE_EMPTY
        } else {
            MultiStateView.VIEW_STATE_CONTENT
        }
    }

    fun updateFilter(onlyFavorites: Boolean){
        launch {
            listViewModel.loadFavorites()
            if(isAdded) {
                vWallpaperList?.post {
                    adapter.itemFilter.filter(if (onlyFavorites) FILTER_FAVORITES else FILTER_ALL) {
                        updateState()
                    }
                }
            }
        }
    }

    fun sortWallpapers(sortBy: WallpaperListViewModel.SortBy){
        listViewModel.loadWallpapers(sortBy)
    }

    private fun getAdapterItem(wallpaper: Wallpaper): WallpaperAdapterItem? =
        adapter.adapterItems.firstOrNull { it.wallpaper.id == wallpaper.id }

    @Subscribe(sticky = true)
    fun onEvent(event: FavoriteWallpaperEvent){
        EventBus.getDefault().removeStickyEvent(event)
        launch {
            listViewModel.loadFavorites()
            getAdapterItem(event.wallpaper)?.let { item ->
                vWallpaperList?.findViewHolderForItemId(item.identifier)?.apply {
                    item.updateFavorite(itemView)
                }
            }
        }
    }

    private inner class WallpaperPreloadModelProvider : PreloadModelProvider<String> {
        override fun getPreloadItems(position: Int): List<String> {
            val imageUrl = context?.run {
                val wallpaper = adapter.getAdapterItem(position).wallpaper
                if (wallpaper.thumbUrl.isNotBlank() && !Settings.isHighQualityThumb(this))
                    wallpaper.thumbUrl
                else
                    wallpaper.url
            }.orEmpty()
            return if(imageUrl.isNotEmpty()) singletonList(imageUrl) else emptyList()
        }

        override fun getPreloadRequestBuilder(url: String) =
            GlideApp.with(this@WallpaperListFragment)
                .load(url)
                .override(WallpaperImageView.IMAGE_WIDTH, WallpaperImageView.IMAGE_HEIGHT)
    }

}
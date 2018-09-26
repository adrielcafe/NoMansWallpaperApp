package cafe.adriel.nomanswallpaper.view.main.settings

import android.annotation.SuppressLint
import android.os.Bundle
import android.view.View
import android.view.ViewGroup
import androidx.core.view.forEach
import androidx.preference.*
import androidx.recyclerview.widget.RecyclerView
import cafe.adriel.nomanswallpaper.R
import cafe.adriel.nomanswallpaper.util.Settings

class SettingsFragment : PreferenceFragmentCompat() {

    override fun onCreatePreferences(savedInstanceState: Bundle?, rootKey: String?) {
        setPreferencesFromResource(R.xml.preferences, rootKey)
    }

    override fun onCreateAdapter(preferenceScreen: PreferenceScreen?): RecyclerView.Adapter<*> {
        return object : PreferenceGroupAdapter(preferenceScreen){
            @SuppressLint("RestrictedApi")
            override fun onBindViewHolder(holder: PreferenceViewHolder, position: Int, payloads: MutableList<Any>) {
                super.onBindViewHolder(holder, position, payloads)
                // Hide icon to remove start padding
                val preference = getItem(position)
                if (preference is PreferenceCategory) {
                    removeStartPadding(holder.itemView)
                } else {
                    holder.itemView
                        .findViewById<View?>(R.id.icon_frame)
                        ?.visibility = if (preference.icon == null) View.GONE else View.VISIBLE
                }
            }
        }
    }

    override fun onResume() {
        super.onResume()
        preferenceManager.sharedPreferences.registerOnSharedPreferenceChangeListener(Settings)
    }

    override fun onPause() {
        super.onPause()
        preferenceManager.sharedPreferences.unregisterOnSharedPreferenceChangeListener(Settings)
    }

    private fun removeStartPadding(view: View) {
        if (view is ViewGroup){
            view.forEach {
                removeStartPadding(it)
                it.setPaddingRelative(0, view.paddingTop, view.paddingEnd, view.paddingBottom)
            }
        }
    }

}
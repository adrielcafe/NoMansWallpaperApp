package cafe.adriel.nomanswallpaper.view.custom

import android.content.Context
import android.util.AttributeSet
import android.widget.RelativeLayout
import androidx.appcompat.widget.AppCompatTextView
import cafe.adriel.nomanswallpaper.App
import cafe.adriel.nomanswallpaper.R
import cafe.adriel.nomanswallpaper.util.colorFrom
import cafe.adriel.nomanswallpaper.util.inflate
import kotlinx.android.synthetic.main.view_donate.view.*

class DonateView(context: Context, attrs: AttributeSet? = null) : RelativeLayout(context, attrs) {

    var selectedProductSku = App.PRODUCT_SKU_COFFEE_1
        private set

    init {
        inflate(R.layout.view_donate)?.run {
            updateTextColor(v1Coffee, true)
            vSlider.setOnDiscreteSliderChangeListener { selectedPosition ->
                updateTextColor(v1Coffee, false)
                updateTextColor(v3Coffee, false)
                updateTextColor(v5Coffee, false)
                when (selectedPosition) {
                    0 -> {
                        selectedProductSku = App.PRODUCT_SKU_COFFEE_1
                        updateTextColor(v1Coffee, true)
                    }
                    1 -> {
                        selectedProductSku = App.PRODUCT_SKU_COFFEE_3
                        updateTextColor(v3Coffee, true)
                    }
                    2 -> {
                        selectedProductSku = App.PRODUCT_SKU_COFFEE_5
                        updateTextColor(v5Coffee, true)
                    }
                }
            }
        }
    }

    private fun updateTextColor(view: AppCompatTextView, selected: Boolean) {
        view.setTextColor(
            context.colorFrom(
                if (selected) R.color.colorAccent else R.color.grey_400
            )
        )
    }

}
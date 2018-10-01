package cafe.adriel.nomanswallpaper.view.main

import android.app.Activity
import android.app.Application
import android.content.Intent
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import cafe.adriel.androidcoroutinescopes.viewmodel.CoroutineScopedAndroidViewModel
import cafe.adriel.nomanswallpaper.BuildConfig
import cafe.adriel.nomanswallpaper.util.Analytics
import cafe.adriel.nomanswallpaper.util.RemoteConfig
import com.crashlytics.android.Crashlytics
import com.github.stephenvinouze.core.managers.KinAppManager
import com.github.stephenvinouze.core.models.KinAppProductType
import com.github.stephenvinouze.core.models.KinAppPurchase
import com.github.stephenvinouze.core.models.KinAppPurchaseResult
import kotlinx.coroutines.experimental.launch

class MainViewModel(app: Application) : CoroutineScopedAndroidViewModel(app),
    KinAppManager.KinAppListener {

    private val billingManager by lazy {
        KinAppManager(getApplication(), "")
    }
    private val _appUpdateAvailable = MutableLiveData<Boolean>()
    private val _purchaseCompleted = MutableLiveData<Boolean>()
    private val _billingSupported = MutableLiveData<Boolean>()

    val appUpdateAvailable: LiveData<Boolean> get() = _appUpdateAvailable
    val purchaseCompleted: LiveData<Boolean> get() = _purchaseCompleted
    val billingSupported: LiveData<Boolean> get() = _billingSupported

    init {
        billingManager.bind(this)
        launch {
            RemoteConfig.load()
            _appUpdateAvailable.value = BuildConfig.VERSION_CODE < RemoteConfig.getMinVersion()
        }
    }

    override fun onCleared() {
        super.onCleared()
        billingManager.unbind()
    }

    override fun onBillingReady() {
        launch {
            try {
                _billingSupported.value = billingManager.isBillingSupported(KinAppProductType.INAPP)
                billingManager.restorePurchases(KinAppProductType.INAPP)?.forEach {
                    billingManager.consumePurchase(it)
                }
            } catch (e: Exception) {
                Crashlytics.logException(e)
                e.printStackTrace()
                _billingSupported.value = false
            }
        }
    }

    override fun onPurchaseFinished(purchaseResult: KinAppPurchaseResult, purchase: KinAppPurchase?) {
        if (purchaseResult == KinAppPurchaseResult.SUCCESS && purchase != null) {
            launch {
                billingManager.consumePurchase(purchase)
                _purchaseCompleted.value = true
            }
        } else {
            _purchaseCompleted.value = false
        }
    }

    fun verifyDonation(requestCode: Int, resultCode: Int, data: Intent?) =
        try {
            billingManager.verifyPurchase(requestCode, resultCode, data)
        } catch (e: Exception) {
            Crashlytics.logException(e)
            e.printStackTrace()
            false
        }

    fun donate(activity: Activity, sku: String) {
        if (BuildConfig.RELEASE) {
            if (sku.isNotBlank()) {
                billingManager.purchase(activity, sku, KinAppProductType.INAPP)
                Analytics.logDonate(sku)
            }
        } else {
            billingManager.purchase(
                activity,
                KinAppManager.TEST_PURCHASE_SUCCESS,
                KinAppProductType.INAPP
            )
        }
    }

}
package za.co.etaxi.app

import android.app.Application
import org.koin.android.ext.koin.androidContext
import org.koin.dsl.module
import za.co.etaxi.app.core.di.initKoin
import za.co.etaxi.app.core.network.TokenManager

// Android Application entry point. Registers the Context so Koin can
// construct TokenManager (which needs Context for
// EncryptedSharedPreferences). Also calls initKoin which loads the
// network + repository modules.
//
// IMPORTANT: Make sure AndroidManifest.xml references this class:
//   <application android:name=".EtaxiApp" ... >
class EtaxiApp : Application() {
    override fun onCreate() {
        super.onCreate()
        initKoin {
            androidContext(this@EtaxiApp)
            modules(
                module {
                    single { TokenManager(get()) }
                }
            )
        }
    }
}

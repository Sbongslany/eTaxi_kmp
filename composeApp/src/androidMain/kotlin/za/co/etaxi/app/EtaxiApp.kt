package za.co.etaxi.app

import android.app.Application
import org.koin.android.ext.koin.androidContext
import org.koin.dsl.module
import za.co.etaxi.app.core.di.initKoin
import za.co.etaxi.app.core.location.AndroidLocationStreamer
import za.co.etaxi.app.core.location.LocationStreamer
import za.co.etaxi.app.core.network.TokenManager

// Android Application entry point. Registers Context-dependent
// dependencies (TokenManager, LocationStreamer) and bootstraps Koin.
//
// AndroidManifest.xml must reference this class:
//   <application android:name=".EtaxiApp" ... >
class EtaxiApp : Application() {
    override fun onCreate() {
        super.onCreate()
        initKoin {
            androidContext(this@EtaxiApp)
            modules(
                module {
                    single { TokenManager(get()) }
                    single<LocationStreamer> { AndroidLocationStreamer(get()) }
                }
            )
        }
    }
}

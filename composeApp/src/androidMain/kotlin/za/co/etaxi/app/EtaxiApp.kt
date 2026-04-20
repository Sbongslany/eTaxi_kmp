package za.co.etaxi.app

import android.app.Application
import org.koin.android.ext.koin.androidContext
import org.koin.android.ext.koin.androidLogger
import org.koin.core.logger.Level
import za.co.etaxi.app.core.di.initKoin

class EtaxiApp : Application() {
    override fun onCreate() {
        super.onCreate()
        initKoin {
            androidLogger(Level.INFO)
            androidContext(this@EtaxiApp)
        }
    }
}

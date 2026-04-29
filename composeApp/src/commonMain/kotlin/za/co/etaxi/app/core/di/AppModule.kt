package za.co.etaxi.app.core.di

import io.ktor.client.HttpClient
import org.koin.core.context.startKoin
import org.koin.core.module.Module
import org.koin.dsl.KoinAppDeclaration
import org.koin.dsl.module
import za.co.etaxi.app.core.network.EtaxiApi
import za.co.etaxi.app.core.network.TokenManager
import za.co.etaxi.app.core.ws.WsClient
import za.co.etaxi.app.data.mock.MockDriverRepository
import za.co.etaxi.app.data.mock.MockNotificationRepository
import za.co.etaxi.app.data.mock.MockPaymentRepository
import za.co.etaxi.app.data.mock.MockRiderRepository
import za.co.etaxi.app.data.mock.MockSessionRepository
import za.co.etaxi.app.data.remote.RemoteAuthApi
import za.co.etaxi.app.data.remote.RemoteDriverApi
import za.co.etaxi.app.data.remote.RemoteDriverRepository
import za.co.etaxi.app.data.remote.RemoteRiderRepository
import za.co.etaxi.app.data.remote.RemoteSessionRepository
import za.co.etaxi.app.data.remote.RemoteTripApi
import za.co.etaxi.app.domain.repository.DriverRepository
import za.co.etaxi.app.domain.repository.NotificationRepository
import za.co.etaxi.app.domain.repository.PaymentRepository
import za.co.etaxi.app.domain.repository.RiderRepository
import za.co.etaxi.app.domain.repository.SessionRepository

// Master kill-switch for the remote API layer.
const val USE_REMOTE_API: Boolean = true

val networkModule: Module = module {
    single<HttpClient> { EtaxiApi.create() }
    single { RemoteAuthApi(get(), get()) }
    single { RemoteDriverApi(get(), get()) }
    single { RemoteTripApi(get(), get()) }
    single { WsClient(tokenManager = get()) }
    // Default to a no-op LocationStreamer. The Android entry point
    // (EtaxiApp) overrides this with AndroidLocationStreamer that uses
    // FusedLocationProviderClient. iOS keeps the no-op until that
    // platform is wired up.
    single<za.co.etaxi.app.core.location.LocationStreamer> {
        za.co.etaxi.app.core.location.NoopLocationStreamer()
    }
}

val repositoryModule: Module = module {
    if (USE_REMOTE_API) {
        single<SessionRepository> {
            RemoteSessionRepository(authApi = get(), tokens = get())
        }
        single<DriverRepository> {
            RemoteDriverRepository(
                api = get(),
                tripApi = get(),
                ws = get(),
                locationStreamer = get(),
            )
        }
        single<RiderRepository> {
            RemoteRiderRepository(tripApi = get(), ws = get())
        }
    } else {
        single<SessionRepository> { MockSessionRepository() }
        single<DriverRepository>  { MockDriverRepository()  }
        single<RiderRepository>   { MockRiderRepository()   }
    }
    single<PaymentRepository> { MockPaymentRepository() }
    single<NotificationRepository> { MockNotificationRepository() }
}

fun initKoin(appDeclaration: KoinAppDeclaration = {}) {
    startKoin {
        appDeclaration()
        modules(networkModule, repositoryModule)
    }
}

package za.co.etaxi.app.core.di

import io.ktor.client.HttpClient
import org.koin.core.context.startKoin
import org.koin.core.module.Module
import org.koin.dsl.KoinAppDeclaration
import org.koin.dsl.module
import za.co.etaxi.app.core.network.EtaxiApi
import za.co.etaxi.app.core.network.TokenManager
import za.co.etaxi.app.data.mock.MockDriverRepository
import za.co.etaxi.app.data.mock.MockNotificationRepository
import za.co.etaxi.app.data.mock.MockPaymentRepository
import za.co.etaxi.app.data.mock.MockRiderRepository
import za.co.etaxi.app.data.mock.MockSessionRepository
import za.co.etaxi.app.data.remote.RemoteAuthApi
import za.co.etaxi.app.data.remote.RemoteDriverApi
import za.co.etaxi.app.data.remote.RemoteDriverRepository
import za.co.etaxi.app.data.remote.RemoteSessionRepository
import za.co.etaxi.app.domain.repository.DriverRepository
import za.co.etaxi.app.domain.repository.NotificationRepository
import za.co.etaxi.app.domain.repository.PaymentRepository
import za.co.etaxi.app.domain.repository.RiderRepository
import za.co.etaxi.app.domain.repository.SessionRepository

// Master kill-switch for the remote API layer.
// true  -> SessionRepository + DriverRepository resolve to the real
//          backend at http://192.168.0.181:5001/api/v1
// false -> Fall back to Mock repositories. Useful when the dev backend
//          is unreachable.
const val USE_REMOTE_API: Boolean = true

// Network infrastructure bindings. TokenManager is NOT bound here
// because it needs a platform-specific constructor argument (Android
// Context). TokenManager gets registered in the platform entry point.
val networkModule: Module = module {
    single<HttpClient> { EtaxiApi.create() }
    single { RemoteAuthApi(get(), get()) }
    single { RemoteDriverApi(get(), get()) }
}

// Repository bindings. USE_REMOTE_API decides which ones win.
val repositoryModule: Module = module {
    if (USE_REMOTE_API) {
        single<SessionRepository> {
            RemoteSessionRepository(authApi = get(), tokens = get())
        }
        single<DriverRepository> {
            RemoteDriverRepository(api = get())
        }
    } else {
        single<SessionRepository> { MockSessionRepository() }
        single<DriverRepository>  { MockDriverRepository()  }
    }
    // These two stay on mocks until real endpoints ship
    single<RiderRepository>   { MockRiderRepository()   }
    single<PaymentRepository> { MockPaymentRepository() }
    single<NotificationRepository> { MockNotificationRepository() }
}

fun initKoin(appDeclaration: KoinAppDeclaration = {}) {
    startKoin {
        appDeclaration()
        modules(networkModule, repositoryModule)
    }
}

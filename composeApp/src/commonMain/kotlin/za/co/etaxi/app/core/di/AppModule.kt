package za.co.etaxi.app.core.di

import org.koin.core.context.startKoin
import org.koin.core.module.Module
import org.koin.dsl.KoinAppDeclaration
import org.koin.dsl.module
import za.co.etaxi.app.data.mock.MockDriverRepository
import za.co.etaxi.app.data.mock.MockNotificationRepository
import za.co.etaxi.app.data.mock.MockPaymentRepository
import za.co.etaxi.app.data.mock.MockRiderRepository
import za.co.etaxi.app.data.mock.MockSessionRepository
import za.co.etaxi.app.domain.repository.DriverRepository
import za.co.etaxi.app.domain.repository.NotificationRepository
import za.co.etaxi.app.domain.repository.PaymentRepository
import za.co.etaxi.app.domain.repository.RiderRepository
import za.co.etaxi.app.domain.repository.SessionRepository

val appModule: Module = module {
    single<SessionRepository> { MockSessionRepository() }
    single<RiderRepository>   { MockRiderRepository()   }
    single<DriverRepository>  { MockDriverRepository()  }
    single<PaymentRepository> { MockPaymentRepository() }
    single<NotificationRepository> { MockNotificationRepository() }
}

fun initKoin(appDeclaration: KoinAppDeclaration = {}) {
    startKoin {
        appDeclaration()
        modules(appModule)
    }
}

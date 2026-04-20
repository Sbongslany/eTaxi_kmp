package za.co.etaxi.app.data.mock

import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import za.co.etaxi.app.domain.repository.NotificationRepository

class MockNotificationRepository : NotificationRepository {
    private val _state = MutableStateFlow(MockData.notifications)
    override fun all() = _state.asStateFlow()

    override suspend fun markRead(id: String) {
        _state.value = _state.value.map { if (it.id == id) it.copy(read = true) else it }
    }

    override suspend fun markAllRead() {
        _state.value = _state.value.map { it.copy(read = true) }
    }
}

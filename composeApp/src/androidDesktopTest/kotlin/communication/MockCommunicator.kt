package communication

import kotlinx.coroutines.delay

class MockCommunicator : BaseCommunicator() {
    private var webSocketConnected = false
    override suspend fun connectWithWebsocket() {
        delay(500L)
        webSocketConnected = true
    }

    override fun sendBytes(bytes: ByteArray) {
        if (!webSocketConnected) throw WebSocketNotConnectedException()
    }

    override suspend fun closeWebSocket() {
        delay(100L)
        webSocketConnected = false
    }
}
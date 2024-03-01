package communication

import kotlinx.coroutines.delay

class MockCommunicator : BaseCommunicator() {
    private var webSocketConnected = false
    var pleaseThrowAnExceptionOnConnection = false
    var lastBytesSent: ByteArray? = null
    override suspend fun connectWithWebsocket() {
        delay(500L)
        if (pleaseThrowAnExceptionOnConnection) throw Exception()
        webSocketConnected = true
    }

    override fun sendBytes(bytes: ByteArray) {
        if (!webSocketConnected) throw WebSocketNotConnectedException()
        lastBytesSent = bytes
    }

    override suspend fun closeWebSocket() {
        delay(100L)
        webSocketConnected = false
    }
}
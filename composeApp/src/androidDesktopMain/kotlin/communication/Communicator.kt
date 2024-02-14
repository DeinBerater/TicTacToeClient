package communication

import io.ktor.client.HttpClient
import io.ktor.client.engine.cio.CIO
import io.ktor.client.plugins.websocket.DefaultClientWebSocketSession
import io.ktor.client.plugins.websocket.WebSockets
import io.ktor.client.plugins.websocket.webSocket
import io.ktor.websocket.Frame
import io.ktor.websocket.FrameType

class Communicator : BaseCommunicator() {
    private lateinit var websocketSession: DefaultClientWebSocketSession
    override suspend fun connectWithWebsocket() {
        val client = HttpClient(CIO) {
            install(WebSockets)
        }
        client.webSocket("http://localhost/") {
            websocketSession = this

            for (frame in incoming) {
                if (frame !is Frame.Binary) {
                    println("Frame is not binary!!!")
                    continue
                }
                bytesIncoming.send(ByteDeconstructor(frame.data))
            }
        }
    }

    override suspend fun sendBytes(bytes: ByteArray) {
        websocketSession.send(
            Frame.byType(
                false, FrameType.BINARY, bytes,
                rsv1 = false,
                rsv2 = false,
                rsv3 = false
            )
        )
    }

}

actual fun createCommunicator(): BaseCommunicator = Communicator()
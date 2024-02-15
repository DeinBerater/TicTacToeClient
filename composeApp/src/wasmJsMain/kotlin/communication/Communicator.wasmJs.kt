package communication

import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.await
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import org.khronos.webgl.ArrayBuffer
import org.khronos.webgl.Int8Array
import org.khronos.webgl.get
import org.w3c.dom.ARRAYBUFFER
import org.w3c.dom.BinaryType
import org.w3c.dom.WebSocket
import kotlin.js.Promise

class Communicator : BaseCommunicator() {
    private lateinit var webSocket: WebSocket

    private fun onClosePromise(): Promise<Nothing?> {
        return Promise { resolved, _ ->
            webSocket.onclose = {
                resolved(null)
            }
        }
    }

    private fun onMessagePromise(): Promise<JsAny?> {
        return Promise { resolved, _ ->
            webSocket.onmessage = {
                resolved(it.data)
            }
        }
    }

    /** Connects this communicator with a websocket.
     * This should directly be done after the communicator is initialized, else it is useless.
     * Throws an exception if something didn't work with that.
     * */
    override suspend fun connectWithWebsocket() {

        println("Trying to connect to WebSocket...")
        webSocket = WebSocket("ws://192.168.178.121:80")

        webSocket.binaryType = BinaryType.ARRAYBUFFER
    }

    override suspend fun initializeEventListeners(scope: CoroutineScope) {

        scope.launch {
            // Timeout after 4 seconds.
            delay(4000L)
            if (webSocket.readyState.toInt() == 0) {
                webSocket.close()
            }
        }

        scope.launch {
            while (webSocket.readyState.toInt() <= 1) {
                val data = onMessagePromise().await<JsAny?>()

                data as? ArrayBuffer ?: throw InvalidPacketException("Data is not binary.")

                // Convert data to ByteArray
                val dataArray = Int8Array(data)
                val byteArray = ByteArray(dataArray.length)
                for (i in 0 until dataArray.length) {
                    byteArray[i] = dataArray[i]
                }

                // Send byte array
                bytesIncoming.send(byteArray)
            }
        }


        scope.launch {
            onClosePromise().await<Nothing?>()
            println("WebSocket closed (cannot connect?), disconnected.")
            // Websocket closed, sending null.
            bytesIncoming.send(null)
        }
    }


    /** Sends bytes to the websocket in a new coroutine.
     * If for any reason the bytes cannot be sent, the websocket is closed and a message is printed into the console.
     * @throws WebSocketNotConnectedException if the websocket is not connected.
     * */
    override fun sendBytes(bytes: ByteArray) {
        // ReadyStates: 0 (Connecting), 1 (Open), 2 (Closing), 3 (Closed)
        if (!this@Communicator::webSocket.isInitialized || webSocket.readyState.toInt() != 1) throw WebSocketNotConnectedException()

        TODO("Not yet implemented")
    }

}

actual fun createCommunicator(): BaseCommunicator = Communicator()
package communication

import io.ktor.client.engine.HttpClientEngineConfig
import io.ktor.client.engine.HttpClientEngineFactory
import io.ktor.client.engine.cio.CIO
import kotlinx.coroutines.launch
import kotlinx.coroutines.runBlocking

actual fun getCommunicatorEngine(): HttpClientEngineFactory<HttpClientEngineConfig> = CIO

actual fun doAsynchronously(block: suspend () -> Unit) {
    runBlocking { launch { block() } }
}

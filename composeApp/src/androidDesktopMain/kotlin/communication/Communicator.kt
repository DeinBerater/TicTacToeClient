package communication

import io.ktor.client.engine.HttpClientEngineConfig
import io.ktor.client.engine.HttpClientEngineFactory
import io.ktor.client.engine.cio.CIO
import kotlinx.coroutines.DelicateCoroutinesApi
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.launch

actual fun getCommunicatorEngine(): HttpClientEngineFactory<HttpClientEngineConfig> = CIO

@OptIn(DelicateCoroutinesApi::class)
actual fun doAsynchronously(block: suspend () -> Unit) {
    GlobalScope.launch { block() }
}

package de.deinberater.tictactoe.garmincommunication

import com.garmin.android.connectiq.ConnectIQ
import com.garmin.android.connectiq.IQApp
import com.garmin.android.connectiq.IQDevice
import de.deinberater.nigglgarminmobile.devicecommunication.exceptions.DataTransmissionException
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Job
import kotlinx.coroutines.TimeoutCancellationException
import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.launch
import kotlinx.coroutines.suspendCancellableCoroutine
import kotlinx.coroutines.withTimeout
import kotlin.coroutines.resume
import kotlin.coroutines.resumeWithException


class IQAppCommunicator(
    private val transmittingInstance: ConnectIQ, private val iqApp: IQApp,
    private val device: IQDevice,
    private val sendingQueueScope: CoroutineScope
) {
    private val queue = Channel<Any>()
    private var onAppReceive: ((garminData: Any) -> Unit)? = null
    var sendingJob: Job? = null
        private set

    init {
        continueQueue() // Also starts the queue
    }

    fun setOnAppReceive(block: (garminData: Any) -> Unit) {
        onAppReceive = block
    }

    /** Called when the garmin device sent data.
     * */
    fun dataReceived(garminData: Any) {
        onAppReceive?.let { it(garminData) }
    }

    private suspend fun runQueue() {
        for (itemToSend in queue) {
            try {
                println("Now trying to transmit $itemToSend.")
                transmit(itemToSend)
            } catch (e: DataTransmissionException) {
                println("Could not transmit $itemToSend: ${e.message}")
            } catch (e: TimeoutCancellationException) {
                println("Timeout reached trying to transmit.")
            }
        }
    }

    fun transmitData(data: Any) {
        sendingQueueScope.launch {
            queue.send(data)
        }
    }

    fun getDeviceId() = device.deviceIdentifier

    fun stopQueue() {
        if (sendingJob == null || sendingJob?.isActive != true) return // Queue is not initialized or not active

        println("Transmission queue paused.")
        sendingJob?.cancel()
    }

    fun continueQueue() {
        if (sendingJob?.isActive == true) return // Queue is already active

        println("Transmission queue continuing...")
        sendingJob = sendingQueueScope.launch {
            runQueue()
        }
    }

    private suspend fun transmit(data: Any) {
        withTimeout(5000L) {
            suspendCancellableCoroutine { continuation ->
                transmittingInstance.sendMessage(device, iqApp, data) { _, _, status ->
                    if (status == ConnectIQ.IQMessageStatus.SUCCESS) continuation.resume(Unit)
                    else continuation.resumeWithException(DataTransmissionException("Transmission status is $status."))
                }
            }
        }
    }
}
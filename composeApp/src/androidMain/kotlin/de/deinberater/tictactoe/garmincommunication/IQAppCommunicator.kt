package de.deinberater.tictactoe.garmincommunication

import android.util.Log
import com.garmin.android.connectiq.ConnectIQ
import com.garmin.android.connectiq.IQApp
import com.garmin.android.connectiq.IQDevice
import de.deinberater.nigglgarminmobile.devicecommunication.Queue
import de.deinberater.nigglgarminmobile.devicecommunication.QueueState
import de.deinberater.nigglgarminmobile.devicecommunication.exceptions.DataTransmissionException
import kotlinx.coroutines.channels.Channel
import kotlin.coroutines.resume
import kotlin.coroutines.resumeWithException
import kotlin.coroutines.suspendCoroutine

class IQAppCommunicator(
    val appReceiveChannel: Channel<Any>, private val transmittingInstance: ConnectIQ,
    private val iqApp: IQApp,
    private val device: IQDevice
) {
    private val queue = Queue<MutableList<Any>>()
    private var queueState = QueueState.READY
        set(value) {
            Log.d(
                "IQConnection",
                "Setting queue state from $field to $value for app with id ${iqApp.applicationId} and device ${device.deviceIdentifier}."
            )
            field = value
        }

    fun transmitData(data: MutableList<Any>) {
        // ToDo: Add to queue (Flow?)
    }


    fun getDeviceId() = device.deviceIdentifier

    fun stopQueue() {
        queueState = QueueState.IDLE
    }

    fun continueQueue() {
        TODO()
    }

    private suspend fun transmit(data: MutableList<Any>) = suspendCoroutine { continuation ->
        transmittingInstance.sendMessage(device, iqApp, data) { _, _, status ->
            if (status == ConnectIQ.IQMessageStatus.SUCCESS) continuation.resume(Unit)
            else continuation.resumeWithException(DataTransmissionException("Transmission status is $status."))
        }
    }
}
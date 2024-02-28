package de.deinberater.tictactoe.garmincommunication

import android.util.Log
import com.garmin.android.connectiq.ConnectIQ
import com.garmin.android.connectiq.IQApp
import com.garmin.android.connectiq.IQDevice
import de.deinberater.nigglgarminmobile.devicecommunication.Queue
import de.deinberater.nigglgarminmobile.devicecommunication.QueueState
import kotlinx.coroutines.channels.Channel

class IQAppCommunicator(
    val appReceiveChannel: Channel<MutableList<Any>>, private val transmittingInstance: ConnectIQ,
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
}
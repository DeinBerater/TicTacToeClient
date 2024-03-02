package de.deinberater.tictactoe.garmincommunication

import android.content.Context
import android.util.Log
import com.garmin.android.connectiq.ConnectIQ
import com.garmin.android.connectiq.IQApp
import com.garmin.android.connectiq.IQDevice
import com.garmin.android.connectiq.exception.InvalidStateException
import de.deinberater.tictactoe.garmincommunication.exceptions.IQInitializeException
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.coroutineScope
import kotlinx.coroutines.launch
import kotlinx.coroutines.suspendCancellableCoroutine
import kotlin.coroutines.resume
import kotlin.coroutines.resumeWithException

class IQCommunicator(private val context: Context) {
    private val connectType = ConnectIQ.IQConnectType.WIRELESS // TETHERED

    val iqAppCommunicators = mutableListOf<IQAppCommunicator>()
    private lateinit var connectIQInstance: ConnectIQ

    /** Initializes the communicator and gets the apps.
     * Continues if the garmin sdk is initialized, with at least one device with the app installed.
     * @throws IQInitializeException if the connect IQ instance could not be created or
     * no device with the app installed can be found.
     * */
    @Throws(IQInitializeException::class)
    suspend fun initializeCommunicator(
        applicationId: String,
        scope: CoroutineScope,
        onSdkShutDown: () -> Unit
    ) {
        this@IQCommunicator.connectIQInstance = ConnectIQ.getInstance(context, connectType)
        initializeConnectIQ(onSdkShutDown)

        val connectedDevices = connectIQInstance.connectedDevices
        if (connectedDevices == null || connectedDevices.size == 0) throw IQInitializeException("No devices known, communicator not initialized!")

        // Opens a coroutine scope, where for every connected device the app should be received.
        // Every device should launch another scope, and after every device has finished, the code continues.
        coroutineScope {
            connectedDevices.forEach {
                launch {
                    val app = try {
                        receiveIQApp(
                            it,
                            applicationId
                        )
                    } catch (exception: IQInitializeException) {
                        // Don't add this device, as the app is apparently not installed.
                        return@launch
                    }

                    val thisCommunicator = IQAppCommunicator(connectIQInstance, app, it, scope)

                    iqAppCommunicators.add(
                        thisCommunicator
                    )
                    registerDeviceForEvents(it)

                    // Register to receive messages from the application
                    connectIQInstance.registerForAppEvents(it, app) { _, _, messageData, status ->
                        if (status != ConnectIQ.IQMessageStatus.SUCCESS) {
                            Log.d(
                                "IQConnection",
                                "No success in receiving a message. Status: ${status.name}"
                            )
                            return@registerForAppEvents
                        }

                        thisCommunicator.dataReceived(messageData)
                    }
                }
            }
        }


        // If no device with the app can be found, don't continue.
        if (iqAppCommunicators.size == 0) throw IQInitializeException("No devices connected with the app installed.")


        // The connect iq app is installed on at least one device, so the communicator is initialized now.
        Log.d("IQConnection", "Sdk is ready now. ${iqAppCommunicators.size} devices initialized.")
    }


    private suspend fun initializeConnectIQ(onSdkShutDownParam: () -> Unit) =
        suspendCancellableCoroutine { continuation ->
            connectIQInstance.initialize(context, false, object : ConnectIQ.ConnectIQListener {

                // Called when the SDK has been successfully initialized
                override fun onSdkReady() {
                    println("Garmin SDK ready.")
                    continuation.resume(Unit)
                }

                // Called when initialization fails.
                override fun onInitializeError(status: ConnectIQ.IQSdkErrorStatus) {
                    // A failure has occurred during initialization. Inspect
                    // the IQSdkErrorStatus value for more information regarding
                    // the failure.
                    closeCommunicator()
                    continuation.resumeWithException(IQInitializeException("Can't initialize! Status: $status"))
                }

                // Called when the SDK has been shut down
                override fun onSdkShutDown() {
                    onSdkShutDownParam()
                }
            })

        }

    private suspend fun receiveIQApp(device: IQDevice, applicationId: String) =
        suspendCancellableCoroutine { continuation ->

            val iQApplicationInfoListener = object : ConnectIQ.IQApplicationInfoListener {
                override fun onApplicationInfoReceived(iqApp: IQApp) {
                    continuation.resume(iqApp)
                }

                override fun onApplicationNotInstalled(applicationId: String?) {
                    continuation.resumeWithException(IQInitializeException("Corresponding IQ application not installed!"))
                }

            }

            connectIQInstance.getApplicationInfo(applicationId, device, iQApplicationInfoListener)
        }

    private fun registerDeviceForEvents(deviceToRegister: IQDevice) {
        connectIQInstance.registerForDeviceEvents(deviceToRegister) { device, status ->

            // Get the device, and if it cannot be found for any reason, return.
            val deviceAppCommunicator = iqAppCommunicators.find {
                it.getDeviceId() == device.deviceIdentifier
            } ?: return@registerForDeviceEvents

            Log.d(
                "IQConnection", "IQ device connection status changed to ${status.name}."
            )

            if (status == IQDevice.IQDeviceStatus.CONNECTED) deviceAppCommunicator.continueQueue() else deviceAppCommunicator.stopQueue()
        }
    }

    fun closeCommunicator() {
        try {
            iqAppCommunicators.forEach {
                it.sendingJob?.cancel() // Cancel all sending jobs / active queues to avoid unnecessary memory usage.
            }
            connectIQInstance.shutdown(context)
        } catch (illegalStateException: InvalidStateException) {
            // In this case, the sdk is usually shut down already, thus it can be ignored.
        }
    }

}
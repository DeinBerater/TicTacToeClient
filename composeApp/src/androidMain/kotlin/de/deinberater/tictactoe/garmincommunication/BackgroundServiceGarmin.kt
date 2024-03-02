package de.deinberater.tictactoe.garmincommunication

import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent
import android.app.Service
import android.content.Intent
import android.os.Build
import android.os.IBinder
import android.util.Log
import androidx.core.app.NotificationCompat
import de.deinberater.tictactoe.R
import de.deinberater.tictactoe.garmincommunication.exceptions.IQInitializeException
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.launch


class BackgroundServiceGarmin : Service() {
    private val job = SupervisorJob()
    private val scope = CoroutineScope(Dispatchers.Unconfined + job)

    private val iqCommunicator = IQCommunicator(this@BackgroundServiceGarmin)
    private var games = mutableListOf<GarminGame>()

    companion object {
        private const val NOTIFICATION_ID = 1
        private const val NOTIFICATION_CHANNEL_ID = "Notifications_Garmin_Background"
        private const val ACTION_STOP_SERVICE = "STOP_GARMIN_SERVICE"
    }

    // Needed for interface
    override fun onBind(p0: Intent?): IBinder? {
        return null
    }

    override fun onStartCommand(intent: Intent?, flags: Int, startId: Int): Int {
        if (ACTION_STOP_SERVICE == intent?.action) {
            println("Cancelling Garmin service after button press...")
            stopSelf()
        }

        // The service only works after android 8.
        if (Build.VERSION.SDK_INT < Build.VERSION_CODES.O) {
            stopSelf()
            return super.onStartCommand(intent, flags, startId)
        }

        val name = "Garmin Background Process"
        val descriptionText =
            "Notifications showing the app is ready for TicTacToe to be played on garmin devices."
        val importance = NotificationManager.IMPORTANCE_DEFAULT
        val mChannel = NotificationChannel(NOTIFICATION_CHANNEL_ID, name, importance)
        mChannel.description = descriptionText

        val notificationManager = getSystemService(NOTIFICATION_SERVICE) as NotificationManager
        notificationManager.createNotificationChannel(mChannel)


        val applicationId =
            "" // ToDo: Create application and insert id here


        scope.launch {
            try {
                Log.d("IQConnection", "Trying to initialize Connect IQ app...")
                iqCommunicator.initializeCommunicator(applicationId, scope) {
                    stopSelf()
                }

                val deviceCommunicators = iqCommunicator.iqAppCommunicators

                deviceCommunicators.forEach {
                    // Create a garmin game, the rest is handled there.
                    games += GarminGame(it, scope)
                }

            } catch (exception: IQInitializeException) {
                println("IQ initialization failed: ${exception.message}")
                stopSelf()
                return@launch
            }

            println("Showing notification...")
            showNotification()
        }

        return super.onStartCommand(intent, flags, startId)
    }

    private fun showNotification() {

        val stopSelf = Intent(this, BackgroundServiceGarmin::class.java)
        stopSelf.setAction(ACTION_STOP_SERVICE)
        val pStopSelf = PendingIntent.getService(this, 0, stopSelf, PendingIntent.FLAG_IMMUTABLE)

        try {
            startForeground(
                NOTIFICATION_ID, NotificationCompat.Builder(
                    this,
                    NOTIFICATION_CHANNEL_ID
                )
                    .setOngoing(true)
                    .setSmallIcon(R.drawable.ic_launcher_foreground)
                    .setContentTitle("TicTacToe service active.")
                    .setContentText("You can now play TicTacToe from your Garmin device.")
                    .setPriority(NotificationCompat.PRIORITY_DEFAULT)
                    .addAction(R.drawable.ic_launcher_foreground, "Stop", pStopSelf)
                    .build()
            )
        } catch (e: Exception) {
            println("Cannot start service: ${e.message}")
        }
    }

    override fun onDestroy() {
        println("Garmin Service onDestroy")

        // Shutdown the ConnectIQ instance to ensure it unregisters the receiver.
        iqCommunicator.closeCommunicator()

        games.forEach {
            scope.launch {
                it.cancelGame()
            }
        }

        job.cancel()
        super.onDestroy()
    }
}
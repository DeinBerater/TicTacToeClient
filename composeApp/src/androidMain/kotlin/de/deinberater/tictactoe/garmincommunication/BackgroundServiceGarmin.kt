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
import de.deinberater.tictactoe.MainActivity
import de.deinberater.tictactoe.R
import de.deinberater.tictactoe.garmincommunication.exceptions.IQInitializeException
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.launch

class BackgroundServiceGarmin : Service() {
    private val job = SupervisorJob()
    private val scope = CoroutineScope(Dispatchers.Unconfined + job)

    companion object {
        private const val NOTIFICATION_ID = 1
        private const val NOTIFICATION_CHANNEL_ID = "Notifications_Garmin_Background"
    }

    // Needed for interface
    override fun onBind(p0: Intent?): IBinder? {
        return null
    }

    override fun onStartCommand(intent: Intent?, flags: Int, startId: Int): Int {

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


        val applicationId = "" // ToDo: Create application and insert id here


        scope.launch {
            val iqCommunicator = IQCommunicator(this@BackgroundServiceGarmin)
            try {
                Log.d("IQConnection", "Trying to initialize Connect IQ app...")
                iqCommunicator.initializeCommunicator(applicationId) {
                    closeService()
                }

                val deviceCommunicators = iqCommunicator.iqAppCommunicators

            } catch (exception: IQInitializeException) {
                closeService()
            }

        }


        showNotification()
        return super.onStartCommand(intent, flags, startId)
    }

    private fun closeService() {
        Log.i(
            getString(R.string.app_name),
            "Closing Garmin service..."
        )
        stopSelf()
    }

    private fun showNotification() {

        val notificationIntent = Intent(this, MainActivity::class.java)
        val pendingIntent = PendingIntent.getActivity(
            this, 0,
            notificationIntent, PendingIntent.FLAG_IMMUTABLE
        )
        startForeground(
            NOTIFICATION_ID, NotificationCompat.Builder(
                this,
                NOTIFICATION_CHANNEL_ID
            )
                .setOngoing(true)
                .setSmallIcon(R.drawable.ic_launcher_foreground)
                .setContentTitle("TicTacToe service active.")
                .setContentText("You can now play TicTacToe from your Garmin device.")
                .setContentIntent(pendingIntent)
                .build()
        )

        // ToDo: Button to stop service
    }

    override fun onDestroy() {
        super.onDestroy()
        job.cancel()
    }
}
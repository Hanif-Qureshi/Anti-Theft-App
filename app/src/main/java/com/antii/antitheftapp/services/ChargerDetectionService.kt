package com.antii.antitheftapp.services


import android.app.Notification
import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.Service
import android.content.*
import android.media.MediaPlayer
import android.os.Build
import android.os.IBinder
import androidx.core.app.NotificationCompat
import com.antii.antitheftapp.R
import com.antii.antitheftapp.SharedPrefUtil

class ChargerDetectionService : Service() {

    private var mediaPlayer: MediaPlayer? = null
    private lateinit var powerReceiver: BroadcastReceiver
    private lateinit var unlockReceiver: BroadcastReceiver

    override fun onCreate() {
        super.onCreate()

        startForeground(2, createNotification())

        // Receiver for unplug detection
        powerReceiver = object : BroadcastReceiver() {
            override fun onReceive(context: Context?, intent: Intent?) {
                if (intent?.action == Intent.ACTION_POWER_DISCONNECTED) {
                    startSiren()
                }
            }
        }
        registerReceiver(powerReceiver, IntentFilter(Intent.ACTION_POWER_DISCONNECTED))

        // Receiver for screen unlock
        unlockReceiver = object : BroadcastReceiver() {
            override fun onReceive(context: Context?, intent: Intent?) {
                if (isSirenActive()) {
                    stopSiren()
                }
            }
        }
        registerReceiver(unlockReceiver, IntentFilter(Intent.ACTION_USER_PRESENT))
    }

    private fun startSiren() {
        if (mediaPlayer?.isPlaying != true) {
            mediaPlayer = MediaPlayer.create(this, R.raw.sirern)
            mediaPlayer?.isLooping = true
            mediaPlayer?.start()
        }
    }

    private fun stopSiren() {
        SharedPrefUtil.removeActiveService(this, "charger")
        mediaPlayer?.stop()
        mediaPlayer?.release()
        mediaPlayer = null
        stopSelf()
    }

    private fun createNotification(): Notification {
        val channelId = "charger_detection_channel"
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val channel = NotificationChannel(
                channelId,
                "Charger Detection",
                NotificationManager.IMPORTANCE_DEFAULT
            )
            getSystemService(NotificationManager::class.java).createNotificationChannel(channel)
        }

        return NotificationCompat.Builder(this, channelId)
            .setContentTitle("Charger Detection Active")
            .setContentText("Monitoring charger removal...")
            .setSmallIcon(R.drawable.ic_launcher_foreground)
            .build()
    }

    override fun onDestroy() {
        super.onDestroy()
        unregisterReceiver(powerReceiver)
        unregisterReceiver(unlockReceiver)
        stopSiren()
    }

    private fun isSirenActive(): Boolean {
        return mediaPlayer?.isPlaying == true
    }

    override fun onBind(intent: Intent?): IBinder? = null
}

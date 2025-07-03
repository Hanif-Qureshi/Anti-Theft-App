package com.antii.antitheftapp.services


import android.app.Notification
import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.Service
import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.content.IntentFilter
import android.hardware.Sensor
import android.hardware.SensorEvent
import android.hardware.SensorEventListener
import android.hardware.SensorManager
import android.media.MediaPlayer
import android.os.Build
import android.os.IBinder
import androidx.core.app.NotificationCompat
import com.antii.antitheftapp.R
import com.antii.antitheftapp.SharedPrefUtil

class PocketDetectionService : Service(), SensorEventListener {

    private lateinit var sensorManager: SensorManager
    private var proximitySensor: Sensor? = null
    private var mediaPlayer: MediaPlayer? = null
    private lateinit var unlockReceiver: BroadcastReceiver

    override fun onCreate() {
        super.onCreate()

        // Start foreground service with notification
        startForeground(1, createNotification())

        // Register proximity sensor
        sensorManager = getSystemService(SENSOR_SERVICE) as SensorManager
        proximitySensor = sensorManager.getDefaultSensor(Sensor.TYPE_PROXIMITY)
        proximitySensor?.let {
            sensorManager.registerListener(this, it, SensorManager.SENSOR_DELAY_NORMAL)
        }

        // Listen for screen unlock to stop siren
        unlockReceiver = object : BroadcastReceiver() {
            override fun onReceive(context: Context?, intent: Intent?) {
                if (isSirenActive()) {
                    stopSiren()
                }
            }
        }
        registerReceiver(unlockReceiver, IntentFilter(Intent.ACTION_USER_PRESENT))
    }

    override fun onSensorChanged(event: SensorEvent?) {
        val distance = event?.values?.get(0) ?: return
        val isFar = proximitySensor?.maximumRange?.let { distance >= it } ?: false

        if (isFar && mediaPlayer?.isPlaying != true) {
            startSiren()
        }
    }

    override fun onAccuracyChanged(sensor: Sensor?, accuracy: Int) {}

    private fun startSiren() {
        mediaPlayer = MediaPlayer.create(this, R.raw.sirern)
        mediaPlayer?.isLooping = true
        mediaPlayer?.start()
    }

    private fun stopSiren() {
        SharedPrefUtil.removeActiveService(this, "pocket")
        mediaPlayer?.stop()
        mediaPlayer?.release()
        mediaPlayer = null
        stopSelf()
    }

    private fun createNotification(): Notification {
        val channelId = "pocket_detection_channel"
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val channel = NotificationChannel(
                channelId,
                "Pocket Detection",
                NotificationManager.IMPORTANCE_LOW
            )
            getSystemService(NotificationManager::class.java).createNotificationChannel(channel)
        }

        return NotificationCompat.Builder(this, channelId)
            .setContentTitle("Pocket Detection Active")
            .setContentText("Monitoring proximity sensor...")
            .setSmallIcon(R.drawable.ic_launcher_foreground)
            .build()
    }

    override fun onDestroy() {
        super.onDestroy()
        sensorManager.unregisterListener(this)
        unregisterReceiver(unlockReceiver)
        stopSiren()
    }

    override fun onBind(intent: Intent?): IBinder? = null

    private fun isSirenActive(): Boolean {
        return mediaPlayer?.isPlaying == true
    }
}

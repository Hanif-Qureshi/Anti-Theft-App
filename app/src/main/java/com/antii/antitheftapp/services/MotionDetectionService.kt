package com.antii.antitheftapp.services

import com.antii.antitheftapp.R
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
import com.antii.antitheftapp.SharedPrefUtil
import kotlin.math.sqrt

class MotionDetectionService : Service(), SensorEventListener {

    private lateinit var sensorManager: SensorManager
    private var accelerometer: Sensor? = null
    private var mediaPlayer: MediaPlayer? = null
    private lateinit var unlockReceiver: BroadcastReceiver

    override fun onCreate() {
        super.onCreate()

        startForeground(3, createNotification())

        sensorManager = getSystemService(SENSOR_SERVICE) as SensorManager
        accelerometer = sensorManager.getDefaultSensor(Sensor.TYPE_ACCELEROMETER)
        accelerometer?.let {
            sensorManager.registerListener(this, it, SensorManager.SENSOR_DELAY_NORMAL)
        }

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
        val x = event?.values?.get(0) ?: 0f
        val y = event?.values?.get(1) ?: 0f
        val z = event?.values?.get(2) ?: 0f

        val acceleration = sqrt(x * x + y * y + z * z)
        if (acceleration > 15) { // threshold for motion
            startSiren()
        }
    }

    override fun onAccuracyChanged(sensor: Sensor?, accuracy: Int) {}

    private fun startSiren() {
        if (mediaPlayer?.isPlaying != true) {
            mediaPlayer = MediaPlayer.create(this, R.raw.sirern)
            mediaPlayer?.isLooping = true
            mediaPlayer?.start()
        }
    }

    private fun stopSiren() {
        SharedPrefUtil.removeActiveService(this, "motion")

        mediaPlayer?.stop()
        mediaPlayer?.release()
        mediaPlayer = null
        stopSelf()
    }

    private fun createNotification(): Notification {
        val channelId = "motion_detection_channel"
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val channel = NotificationChannel(
                channelId,
                "Motion Detection",
                NotificationManager.IMPORTANCE_LOW
            )
            getSystemService(NotificationManager::class.java).createNotificationChannel(channel)
        }

        return NotificationCompat.Builder(this, channelId)
            .setContentTitle("Motion Detection Active")
            .setContentText("Monitoring device movement...")
            .setSmallIcon(R.drawable.ic_launcher_foreground)
            .build()
    }

    override fun onDestroy() {
        super.onDestroy()
        sensorManager.unregisterListener(this)
        unregisterReceiver(unlockReceiver)
        stopSiren()
    }

    private fun isSirenActive(): Boolean {
        return mediaPlayer?.isPlaying == true
    }

    override fun onBind(intent: Intent?): IBinder? = null
}

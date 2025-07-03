package com.antii.antitheftapp

import android.content.Intent
import android.os.Build
import android.os.Bundle
import android.widget.Button
import android.widget.LinearLayout
import android.widget.TextView
import android.widget.Toast
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.core.app.ActivityCompat
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import com.antii.antitheftapp.services.ChargerDetectionService
import com.antii.antitheftapp.services.MotionDetectionService
import com.antii.antitheftapp.services.PocketDetectionService
import com.google.android.material.button.MaterialButton
import androidx.core.content.edit
import kotlinx.coroutines.delay

class MainActivity : AppCompatActivity() {



    lateinit var pocketBtn: Button
    lateinit var chargerBtn: Button
    lateinit var motionBtn: Button

    lateinit var pocketStatus: TextView
    lateinit var chargerStatus: TextView
    lateinit var motionStatus: TextView


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContentView(R.layout.activity_main)



        pocketBtn = findViewById(R.id.btnPocket)
        chargerBtn = findViewById(R.id.btnCharger)
        motionBtn = findViewById(R.id.btnMotion)

        pocketStatus = findViewById(R.id.statusPocket)
        chargerStatus = findViewById(R.id.statusCharger)
        motionStatus = findViewById(R.id.statusMotion)



        pocketBtn.setOnClickListener {
            startService(Intent(this, PocketDetectionService::class.java))
            SharedPrefUtil.addActiveService(this, "pocket")
            updateStatus()
        }

        chargerBtn.setOnClickListener {
            startService(Intent(this, ChargerDetectionService::class.java))
            SharedPrefUtil.addActiveService(this, "charger")
            updateStatus()
        }

        motionBtn.setOnClickListener {
            startService(Intent(this, MotionDetectionService::class.java))
            SharedPrefUtil.addActiveService(this, "motion")
            updateStatus()
        }







        ViewCompat.setOnApplyWindowInsetsListener(findViewById<LinearLayout>(R.id.mainParent)) { view, insets ->
            val systemBarsInsets = insets.getInsets(WindowInsetsCompat.Type.systemBars() or WindowInsetsCompat.Type.displayCutout())
            view.setPadding(0, systemBarsInsets.top, 0, systemBarsInsets.bottom)
            insets
        }



    }

    override fun onResume() {
        super.onResume()
        updateStatus()
    }

private fun updateStatus() {
    val activeServices = SharedPrefUtil.getActiveServices(this)
    pocketStatus.text = if ("pocket" in activeServices) "Status: Active" else "Status: Inactive"
    chargerStatus.text = if ("charger" in activeServices) "Status: Active" else "Status: Inactive"
    motionStatus.text = if ("motion" in activeServices) "Status: Active" else "Status: Inactive"
}
}
package com.example.birds_view

import android.content.Intent
import android.os.Bundle
import android.os.Handler
import android.os.Looper
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity


class MainActivity : AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContentView(R.layout.activity_main)

        //Add a delay to simulate and the navigate to the login screen
        Handler(Looper.getMainLooper()).postDelayed({
            //Navigate to the login screen
            startActivity(Intent(this, LoginActivity::class.java))
        }, 2000)
    }
}
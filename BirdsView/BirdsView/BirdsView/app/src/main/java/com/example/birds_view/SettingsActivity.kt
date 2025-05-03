package com.example.birds_view

import android.content.Intent
import android.os.Bundle
import android.widget.Button
import android.widget.EditText
import android.widget.RadioGroup
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import com.google.android.material.bottomnavigation.BottomNavigationView
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore


class SettingsActivity : AppCompatActivity() {

    private lateinit var radioGroup: RadioGroup
    private lateinit var editMaxDistance: EditText
    private lateinit var btnSaveSettings: Button
    private lateinit var btnLogOut: Button
    private lateinit var bottomNavigationView: BottomNavigationView
    private lateinit var auth: FirebaseAuth
    private lateinit var db: FirebaseFirestore

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_settings)

        initializeViews()
        setupFirebase()
        loadUserSettings()
        setupBottomNavigation()
        setupButtonListeners()
    }

    private fun initializeViews() {
        radioGroup = findViewById(R.id.radioGroup)
        editMaxDistance = findViewById(R.id.editMaxDistance)
        btnSaveSettings = findViewById(R.id.btnSaveSettings)
        btnLogOut = findViewById(R.id.btnLogOut)
        bottomNavigationView = findViewById(R.id.bottomNavigation)
    }

    private fun setupFirebase() {
        auth = FirebaseAuth.getInstance()
        db = FirebaseFirestore.getInstance()
    }

    private fun loadUserSettings() {
        val user = auth.currentUser
        user?.let { currentUser ->
            db.collection("users")
                .document(currentUser.uid)
                .get()
                .addOnSuccessListener { document ->
                    val settings = document.get("settings") as? Map<String, Any>

                    settings?.let {
                        val maxDistance = it["maxDistance"] as? Double
                        editMaxDistance.setText(maxDistance?.toString() ?: "")

                        val unit = it["unit"] as? String
                        when (unit) {
                            "km" -> radioGroup.check(R.id.radioMetric)
                            "mi" -> radioGroup.check(R.id.radioImperial)
                        }
                    }
                }
        }
    }

    private fun setupBottomNavigation() {
        bottomNavigationView.selectedItemId = R.id.settings
        bottomNavigationView.setOnItemSelectedListener { menuItem ->
            when (menuItem.itemId) {
                R.id.home -> {
                    startActivity(Intent(this, MapActivity::class.java))
                    finish()
                    true
                }
                R.id.observations -> {
                    startActivity(Intent(this, ObservationsActivity::class.java))
                    finish()
                    true
                }
                R.id.leaderboard -> {
                    startActivity(Intent(this, LeaderboardActivity::class.java))
                    finish()
                    true
                }
                R.id.settings -> true
                else -> false
            }
        }
    }

    private fun setupButtonListeners() {
        btnSaveSettings.setOnClickListener {
            saveSettings()
        }

        btnLogOut.setOnClickListener {
            auth.signOut()
            startActivity(Intent(this, LoginActivity::class.java))
            finish()
        }
    }

    private fun saveSettings() {
        val selectedRadioButtonId = radioGroup.checkedRadioButtonId
        val maxDistance = editMaxDistance.text.toString().toDoubleOrNull()

        if (maxDistance == null || maxDistance < 0) {
            Toast.makeText(this, "Please enter a valid distance", Toast.LENGTH_SHORT).show()
            return
        }

        val unit = when (selectedRadioButtonId) {
            R.id.radioMetric -> "km"
            R.id.radioImperial -> "mi"
            else -> "km"
        }

        val user = auth.currentUser
        user?.let {
            val settings = hashMapOf(
                "maxDistance" to maxDistance,
                "unit" to unit
            )

            db.collection("users")
                .document(user.uid)
                .update("settings", settings)
                .addOnSuccessListener {
                    Toast.makeText(this, "Settings saved successfully", Toast.LENGTH_SHORT).show()
                }
                .addOnFailureListener { e ->
                    Toast.makeText(this, "Failed to save settings: ${e.message}", Toast.LENGTH_SHORT).show()
                }
        }
    }

    override fun onBackPressed() {
        super.onBackPressed()
        startActivity(Intent(this, MapActivity::class.java))
        finish()
    }
}

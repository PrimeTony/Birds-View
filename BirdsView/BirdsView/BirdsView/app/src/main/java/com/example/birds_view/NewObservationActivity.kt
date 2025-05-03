package com.example.birds_view

import android.Manifest
import android.content.Intent
import android.content.pm.PackageManager
import android.location.Location
import android.os.Bundle
import android.widget.Button
import android.widget.EditText
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import com.example.birds_view.models.Observation
import com.google.android.gms.location.FusedLocationProviderClient
import com.google.android.gms.location.LocationServices
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

class NewObservationActivity : AppCompatActivity() {

    private lateinit var fusedLocationClient: FusedLocationProviderClient
    private var currentLocation: Location? = null

    private lateinit var TextSpecies: EditText
    private lateinit var TextLocation: EditText
    private lateinit var TextNotes: EditText
    private lateinit var ViewDate: TextView
    private lateinit var Time: TextView
    private lateinit var buttonSaveObservation: Button

    private lateinit var auth: FirebaseAuth
    private lateinit var db: FirebaseFirestore

    companion object {
        private const val LOCATION_PERMISSION_REQUEST_CODE = 1
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_new_observation)

        // Initialize views
        TextSpecies = findViewById(R.id.editTextSpecies)
        TextLocation = findViewById(R.id.editTextLocation)
        TextNotes = findViewById(R.id.editTextNotes)
        ViewDate = findViewById(R.id.editTextDate)
        Time = findViewById(R.id.editTextTime)
        buttonSaveObservation = findViewById(R.id.buttonSave)

        // Initialize Firebase
        auth = FirebaseAuth.getInstance()
        db = FirebaseFirestore.getInstance()

        // Initialize FusedLocationProviderClient
        fusedLocationClient = LocationServices.getFusedLocationProviderClient(this)

        // Check and request location permission
        if (checkLocationPermission()) {
            fetchLastLocation()
        }

        // Display the current date and time
        displayCurrentDateTime()

        // Save observation to Firestore
        buttonSaveObservation.setOnClickListener {
            saveObservationToFirestore()
        }

    }

    // Display current date and time
    private fun displayCurrentDateTime() {
        val currentDate = SimpleDateFormat("dd/MM/yyyy", Locale.getDefault()).format(Date())
        val currentTime = SimpleDateFormat("HH:mm", Locale.getDefault()).format(Date())
        ViewDate.text = currentDate
        Time.text = currentTime
    }

    // Check location permission
    private fun checkLocationPermission(): Boolean {
        if (ContextCompat.checkSelfPermission(
                this,
                Manifest.permission.ACCESS_FINE_LOCATION
            ) != PackageManager.PERMISSION_GRANTED
        ) {
            ActivityCompat.requestPermissions(
                this,
                arrayOf(Manifest.permission.ACCESS_FINE_LOCATION),
                LOCATION_PERMISSION_REQUEST_CODE
            )
            return false
        }
        return true
    }

    // Fetch last known location
    private fun fetchLastLocation() {
        if (ActivityCompat.checkSelfPermission(
                this,
                Manifest.permission.ACCESS_FINE_LOCATION
            ) != PackageManager.PERMISSION_GRANTED && ActivityCompat.checkSelfPermission(
                this,
                Manifest.permission.ACCESS_COARSE_LOCATION
            ) != PackageManager.PERMISSION_GRANTED
        ) {
            ActivityCompat.requestPermissions(
                this,
                arrayOf(Manifest.permission.ACCESS_FINE_LOCATION),
                LOCATION_PERMISSION_REQUEST_CODE
            )
            return
        }
        fusedLocationClient.lastLocation.addOnSuccessListener { location: Location? ->
            location?.let {
                currentLocation = it
                val latitude = it.latitude
                val longitude = it.longitude
                TextLocation.setText("$latitude, $longitude")
            }
        }
    }

    // Save observation to Firestore
    private fun saveObservationToFirestore() {
        val species = TextSpecies.text.toString()
        val date = ViewDate.text.toString()
        val time = Time.text.toString()
        val location = TextLocation.text.toString()
        val notes = TextNotes.text.toString()

        if (species.isNotEmpty() && location.isNotEmpty()) {
            val observation = Observation(species, date, time, location, notes)

            // Get the current user's UID from Firebase Authentication
            val user = auth.currentUser
            val userUid = user?.uid

            if (userUid != null) {
                // Reference to the user's document in the "users" collection
                val userDocRef = db.collection("users").document(userUid)

                // Reference to the "observations" subcollection inside the user's document
                val observationsCollectionRef = userDocRef.collection("observations")

                // Add the observation
                observationsCollectionRef.add(observation)
                    .addOnSuccessListener { documentReference ->
                        Toast.makeText(
                            this,
                            "Observation added with ID: ${documentReference.id}",
                            Toast.LENGTH_SHORT
                        ).show()

                        // After successfully saving, navigate to SavedObservationsActivity
                        val intent = Intent(this, ObservationsActivity::class.java)
                        startActivity(intent)
                    }
                    .addOnFailureListener { e ->
                        Toast.makeText(this, "Error adding observation: $e", Toast.LENGTH_SHORT)
                            .show()
                    }
            } else {
                Toast.makeText(this, "User not authenticated.", Toast.LENGTH_SHORT).show()
            }
        } else {
            Toast.makeText(this, "Species and Location fields are required.", Toast.LENGTH_SHORT)
                .show()
        }
    }
}
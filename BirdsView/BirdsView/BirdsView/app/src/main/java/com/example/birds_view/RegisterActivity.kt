package com.example.birds_view

import android.content.Intent
import android.os.Bundle
import android.widget.Button
import android.widget.EditText
import android.widget.TextView
import android.widget.Toast
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore


class RegisterActivity : AppCompatActivity() {

    private lateinit var Username: EditText
    private lateinit var Email: EditText
    private lateinit var Password: EditText
    private lateinit var Login: TextView
    private lateinit var buttonRegister: Button

    //firebase auth and firestore
    private lateinit var auth: FirebaseAuth
    private lateinit var db: FirebaseFirestore

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContentView(R.layout.activity_register)

        //initialize views
        Username = findViewById(R.id.usernameEditText)
        Email = findViewById(R.id.emailEditText)
        Password = findViewById(R.id.passwordEditText)
        Login = findViewById(R.id.loginLinkTextView)
        buttonRegister = findViewById(R.id.registerButton)

        //initialize firebase auth and firestore
        auth = FirebaseAuth.getInstance()
        db = FirebaseFirestore.getInstance()

        //set on click listener for button register
        buttonRegister.setOnClickListener {

            val username = Username.text.toString()
            val email = Email.text.toString()
            val password = Password.text.toString()

            // Ensure that email and password are not empty
            if (username.isEmpty() || email.isEmpty() || password.isEmpty()) {
                // Handle empty fields, display an error message, etc.
                Toast.makeText(this, "Email and password must not be empty", Toast.LENGTH_SHORT).show()
            } else {
                // Create a new user with Firebase Authentication
                auth.createUserWithEmailAndPassword(email, password)
                    .addOnCompleteListener { task ->
                        if (task.isSuccessful) {
                            // Registration successful
                            val user = auth.currentUser

                            // Save user data to Firestore
                            user?.let {
                                val userData = HashMap<String, Any>()
                                userData["username"] = username
                                userData["email"] = email
                                userData["password"] = password

                                db.collection("users")
                                    .document(user.uid)
                                    .set(userData)
                                    .addOnSuccessListener {
                                        // User data saved to Firestore
                                        Toast.makeText(this, "Registration successful", Toast.LENGTH_SHORT).show()

                                    }
                                    .addOnFailureListener { e ->
                                        // Handle Firestore write error
                                        Toast.makeText(this, "Registration failed", Toast.LENGTH_SHORT).show()
                                    }
                            }

                            // Redirect to the next activity or perform any other action
                            startActivity(Intent(this, LoginActivity::class.java))
                        } else {
                            // Registration failed, handle the error
                            val exception = task.exception
                            // Display an error message or log the exception
                            Toast.makeText(this, "Registration failed", Toast.LENGTH_SHORT).show()
                        }
                    }
            }
        }

        //set on click listener for text view login
        Login.setOnClickListener {
            startActivity(Intent(this, LoginActivity::class.java))
        }
    }
}
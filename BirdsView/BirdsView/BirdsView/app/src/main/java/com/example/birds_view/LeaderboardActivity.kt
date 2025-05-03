package com.example.birds_view

import android.content.Intent
import android.os.Bundle
import android.view.View
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.recyclerview.widget.DividerItemDecoration
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.example.birds_view.adapter.LeaderboardAdapter
import com.example.birds_view.models.LeaderboardItem
import com.example.birds_view.models.Observation
import com.example.birds_view.models.User
import com.google.android.material.bottomnavigation.BottomNavigationView
import com.google.android.material.progressindicator.CircularProgressIndicator
import com.google.firebase.firestore.FirebaseFirestore

class LeaderboardActivity : AppCompatActivity() {
    private lateinit var db: FirebaseFirestore
    private lateinit var leaderboardAdapter: LeaderboardAdapter
    private lateinit var recyclerView: RecyclerView
    private lateinit var progressBar: CircularProgressIndicator
    private lateinit var bottomNavigation: BottomNavigationView

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_leaderboard)
        initializeViews()
        setupRecyclerView()
        setupBottomNavigation()
        loadLeaderboardData()
    }

    private fun initializeViews() {
        db = FirebaseFirestore.getInstance()
        recyclerView = findViewById(R.id.leaderboardRecyclerView)
        progressBar = findViewById(R.id.progressBar)
        bottomNavigation = findViewById(R.id.bottomNavigation)
    }

    private fun setupRecyclerView() {
        leaderboardAdapter = LeaderboardAdapter()
        recyclerView.apply {
            layoutManager = LinearLayoutManager(this@LeaderboardActivity)
            adapter = leaderboardAdapter
            addItemDecoration(DividerItemDecoration(context, DividerItemDecoration.VERTICAL))
        }
    }

    private fun loadLeaderboardData() {
        progressBar.visibility = View.VISIBLE

        db.collection("users").get().addOnSuccessListener { userDocuments ->
            val users = mutableMapOf<String, User>()
            val observationCounts = mutableMapOf<String, Int>()
            var completedQueries = 0

            userDocuments.forEach { doc ->
                val user = doc.toObject(User::class.java)
                users[doc.id] = user

                db.collection("users")
                    .document(doc.id)
                    .collection("observations")
                    .get()
                    .addOnSuccessListener { observations ->
                        observationCounts[doc.id] = observations.size()
                        completedQueries++

                        if (completedQueries == users.size) {
                            val leaderboardEntries = users.map { (id, user) ->
                                Pair(user, observationCounts[id] ?: 0)
                            }.sortedByDescending { it.second }

                            leaderboardAdapter.submitList(leaderboardEntries)
                            progressBar.visibility = View.GONE
                        }
                    }
            }
        }
    }


    private fun setupBottomNavigation() {
        bottomNavigation.selectedItemId = R.id.leaderboard
        bottomNavigation.setOnItemSelectedListener { item ->
            when (item.itemId) {
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
                R.id.leaderboard -> true
                R.id.settings -> {
                    startActivity(Intent(this, SettingsActivity::class.java))
                    finish()
                    true
                }
                else -> false
            }
        }
    }

    override fun onBackPressed() {
        super.onBackPressed()
        startActivity(Intent(this, MainActivity::class.java))
        finish()
    }
}

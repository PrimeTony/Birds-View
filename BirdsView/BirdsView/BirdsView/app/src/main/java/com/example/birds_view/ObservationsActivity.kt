package com.example.birds_view

import android.content.Intent
import android.graphics.Canvas
import android.graphics.drawable.ColorDrawable
import android.os.Bundle
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.core.content.ContextCompat
import androidx.recyclerview.widget.ItemTouchHelper
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.example.birds_view.adapter.ObservationAdapter
import com.example.birds_view.models.Observation
import com.google.android.material.bottomnavigation.BottomNavigationView
import com.google.android.material.floatingactionbutton.ExtendedFloatingActionButton
import com.google.android.material.floatingactionbutton.FloatingActionButton
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.Query


class ObservationsActivity : AppCompatActivity() {
    private lateinit var db: FirebaseFirestore
    private lateinit var auth: FirebaseAuth
    private lateinit var recyclerView: RecyclerView
    private lateinit var adapter: ObservationAdapter
    private lateinit var bottomNavigation: BottomNavigationView
    private lateinit var fabAddObservation: ExtendedFloatingActionButton

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_observations)

        initializeViews()
        setupFirebase()
        setupRecyclerView()
        setupBottomNavigation()
        loadObservations()
        setupFab()
    }

    private fun initializeViews() {
        recyclerView = findViewById(R.id.recyclerViewObservations)
        bottomNavigation = findViewById(R.id.bottomNavigation)
        fabAddObservation = findViewById(R.id.fabAddObservation)
    }

    private fun setupFirebase() {
        db = FirebaseFirestore.getInstance()
        auth = FirebaseAuth.getInstance()
    }

    private fun setupRecyclerView() {
        adapter = ObservationAdapter()
        recyclerView.apply {
            layoutManager = LinearLayoutManager(this@ObservationsActivity)
            adapter = this@ObservationsActivity.adapter
        }

        val swipeHandler = object : ItemTouchHelper.SimpleCallback(0, ItemTouchHelper.RIGHT) {
            override fun onMove(recyclerView: RecyclerView, viewHolder: RecyclerView.ViewHolder, target: RecyclerView.ViewHolder): Boolean {
                return false
            }

            override fun onSwiped(viewHolder: RecyclerView.ViewHolder, direction: Int) {
                val position = viewHolder.adapterPosition
                val observation = adapter.currentList[position]
                // Implement sharing logic here
                adapter.notifyItemChanged(position)
                Toast.makeText(this@ObservationsActivity, "Sharing ${observation.species}", Toast.LENGTH_SHORT).show()
                // Handle sharing
                shareObservation(observation)
                adapter.notifyItemChanged(position)

            }

            override fun onChildDraw(
                c: Canvas,
                recyclerView: RecyclerView,
                viewHolder: RecyclerView.ViewHolder,
                dX: Float,
                dY: Float,
                actionState: Int,
                isCurrentlyActive: Boolean
            ) {
                val itemView = viewHolder.itemView
                val icon = ContextCompat.getDrawable(this@ObservationsActivity, R.drawable.ic_share)
                val background = ColorDrawable(ContextCompat.getColor(this@ObservationsActivity, R.color.material_dynamic_primary50))

                val iconMargin = (itemView.height - icon!!.intrinsicHeight) / 2
                val iconTop = itemView.top + (itemView.height - icon.intrinsicHeight) / 2
                val iconBottom = iconTop + icon.intrinsicHeight

                if (dX > 0) {
                    val iconLeft = itemView.left + iconMargin
                    val iconRight = itemView.left + iconMargin + icon.intrinsicWidth
                    icon.setBounds(iconLeft, iconTop, iconRight, iconBottom)

                    background.setBounds(itemView.left, itemView.top, itemView.left + dX.toInt(), itemView.bottom)
                } else {
                    background.setBounds(0, 0, 0, 0)
                }

                background.draw(c)
                icon.draw(c)

                super.onChildDraw(c, recyclerView, viewHolder, dX, dY, actionState, isCurrentlyActive)
            }
        }

        val itemTouchHelper = ItemTouchHelper(swipeHandler)
        itemTouchHelper.attachToRecyclerView(recyclerView)
    }

    private fun shareObservation(observation: Observation) {
        val shareIntent = Intent().apply {
            action = Intent.ACTION_SEND
            type = "text/plain"
            putExtra(Intent.EXTRA_TEXT, "I spotted a ${observation.species} on ${observation.date}!")
        }
        startActivity(Intent.createChooser(shareIntent, "Share via"))
    }

    private fun setupBottomNavigation() {
        bottomNavigation.selectedItemId = R.id.observations
        bottomNavigation.setOnItemSelectedListener { item ->
            when (item.itemId) {
                R.id.home -> {
                    startActivity(Intent(this, MapActivity::class.java))
                    finish()
                    true
                }
                R.id.observations -> true
                R.id.leaderboard -> {
                    startActivity(Intent(this, LeaderboardActivity::class.java))
                    finish()
                    true
                }
                R.id.settings -> {
                    startActivity(Intent(this, SettingsActivity::class.java))
                    finish()
                    true
                }
                else -> false
            }
        }
    }

    private fun setupFab() {
        fabAddObservation.setOnClickListener {
            startActivity(Intent(this, NewObservationActivity::class.java))
        }
    }

    private fun loadObservations() {
        val currentUser = auth.currentUser
        currentUser?.let { user ->
            db.collection("users")
                .document(user.uid)
                .collection("observations")
                .orderBy("date", Query.Direction.DESCENDING)
                .addSnapshotListener { snapshots, error ->
                    if (error != null) {
                        Toast.makeText(this, "Error loading observations", Toast.LENGTH_SHORT).show()
                        return@addSnapshotListener
                    }

                    snapshots?.let { documents ->
                        val observations = documents.mapNotNull { doc ->
                            doc.toObject(Observation::class.java)
                        }
                        adapter.submitList(observations)
                    }
                }
        }
    }

    inner class SwipeToShareCallback : ItemTouchHelper.SimpleCallback(0, ItemTouchHelper.LEFT) {
        override fun onMove(recyclerView: RecyclerView, viewHolder: RecyclerView.ViewHolder, target: RecyclerView.ViewHolder) = false

        override fun onSwiped(viewHolder: RecyclerView.ViewHolder, direction: Int) {
            val position = viewHolder.adapterPosition
            val observation = adapter.currentList[position]

            val shareIntent = Intent().apply {
                action = Intent.ACTION_SEND
                type = "text/plain"
                putExtra(Intent.EXTRA_TEXT, "I spotted a ${observation.species} on ${observation.date}!")
            }
            startActivity(Intent.createChooser(shareIntent, "Share via"))

            // Reset the card position
            adapter.notifyItemChanged(position)
        }

        override fun onChildDraw(
            c: Canvas,
            recyclerView: RecyclerView,
            viewHolder: RecyclerView.ViewHolder,
            dX: Float,
            dY: Float,
            actionState: Int,
            isCurrentlyActive: Boolean
        ) {
            val itemView = viewHolder.itemView
            val shareIcon = ContextCompat.getDrawable(this@ObservationsActivity, R.drawable.ic_share)
            val background = ColorDrawable(getColor(R.color.material_dynamic_primary50))

            background.setBounds(
                itemView.right + dX.toInt(),
                itemView.top,
                itemView.right,
                itemView.bottom
            )
            background.draw(c)

            shareIcon?.let {
                val iconMargin = (itemView.height - it.intrinsicHeight) / 2
                val iconTop = itemView.top + iconMargin
                val iconBottom = iconTop + it.intrinsicHeight
                val iconRight = itemView.right - iconMargin
                val iconLeft = iconRight - it.intrinsicWidth
                it.setBounds(iconLeft, iconTop, iconRight, iconBottom)
                it.draw(c)
            }

            super.onChildDraw(c, recyclerView, viewHolder, dX, dY, actionState, isCurrentlyActive)
        }
    }
}

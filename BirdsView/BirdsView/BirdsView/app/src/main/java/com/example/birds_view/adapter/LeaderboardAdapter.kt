package com.example.birds_view.adapter

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import com.example.birds_view.R
import com.example.birds_view.models.User
import com.example.birds_view.models.Observation

class LeaderboardAdapter : ListAdapter<Pair<User, Int>, LeaderboardAdapter.ViewHolder>(DiffCallback()) {

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val view = LayoutInflater.from(parent.context)
            .inflate(R.layout.item_leaderboard, parent, false)
        return ViewHolder(view)
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        holder.bind(getItem(position), position + 1)
    }

    class ViewHolder(view: View) : RecyclerView.ViewHolder(view) {
        private val rankTextView: TextView = view.findViewById(R.id.rankTextView)
        private val userNameTextView: TextView = view.findViewById(R.id.userNameTextView)
        private val observationCountTextView: TextView = view.findViewById(R.id.observationCountTextView)

        fun bind(item: Pair<User, Int>, position: Int) {
            val (user, observationCount) = item
            rankTextView.text = position.toString()
            userNameTextView.text = user.username
            observationCountTextView.text = "$observationCount observations"
        }
    }

    class DiffCallback : DiffUtil.ItemCallback<Pair<User, Int>>() {
        override fun areItemsTheSame(oldItem: Pair<User, Int>, newItem: Pair<User, Int>) =
            oldItem.first.username == newItem.first.username

        override fun areContentsTheSame(oldItem: Pair<User, Int>, newItem: Pair<User, Int>) =
            oldItem == newItem
    }
}

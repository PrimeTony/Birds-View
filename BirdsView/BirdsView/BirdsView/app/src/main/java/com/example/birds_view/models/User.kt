package com.example.birds_view.models

class User (
    val email: String = "",
    val username: String = "",
    var password: String = "",
    var isMetric: Boolean = true,
    var maxDistance: Double = 0.0
){
    override fun toString(): String {
        return "User(email='$email', username='$username', password='$password', isMetric=$isMetric, maxDistance=$maxDistance)"
    }
}
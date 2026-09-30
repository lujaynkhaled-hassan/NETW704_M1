package com.example.myapplication1

/**
 * User profile saved to Firebase Realtime Database under: users/<uid>
 * Firebase converts this object to JSON automatically, e.g.
 * { "uid": "...", "name": "...", "email": "...", "phone": "...", "role": "buyer" }
 *
 * Every field has a default value because Firebase needs an empty
 * constructor to turn JSON back into a User object when reading.
 */
data class User(
    val uid: String = "",
    val name: String = "",
    val email: String = "",
    val phone: String = "",
    val role: String = ""   // "buyer" or "seller"
)
package com.example.myapplication1

import android.content.Intent
import android.os.Bundle
import android.view.View
import android.widget.Button
import android.widget.EditText
import android.widget.ProgressBar
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.FirebaseDatabase

/**
 * Home / Profile screen.
 * - Reads the logged-in user's profile (JSON) from users/<uid>
 *   and converts it into a User object.
 * - Lets the user edit their name and phone and saves the changes back.
 */
class ProfileActivity : AppCompatActivity() {

    private val auth = FirebaseAuth.getInstance()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_profile)

        // If nobody is logged in, go back to the login screen
        val currentUser = auth.currentUser
        if (currentUser == null) {
            goToLogin()
            return
        }

        // Reference to this user's node: users/<uid>
        val userRef = FirebaseDatabase.getInstance().reference
            .child("users").child(currentUser.uid)

        val roleText = findViewById<TextView>(R.id.roleText)
        val emailText = findViewById<TextView>(R.id.emailText)
        val nameInput = findViewById<EditText>(R.id.nameInput)
        val phoneInput = findViewById<EditText>(R.id.phoneInput)
        val saveButton = findViewById<Button>(R.id.saveButton)
        val progressBar = findViewById<ProgressBar>(R.id.progressBar)

        // --- LOAD: read the JSON under users/<uid> and turn it into a User ---
        progressBar.visibility = View.VISIBLE
        userRef.get()
            .addOnSuccessListener { snapshot ->
                progressBar.visibility = View.GONE
                val user = snapshot.getValue(User::class.java)
                if (user != null) {
                    roleText.text = "Role: ${user.role.replaceFirstChar { it.uppercase() }}"
                    emailText.text = "Email: ${user.email}"
                    nameInput.setText(user.name)
                    phoneInput.setText(user.phone)
                }
            }
            .addOnFailureListener { e ->
                progressBar.visibility = View.GONE
                Toast.makeText(this, "Failed to load: ${e.message}", Toast.LENGTH_LONG).show()
            }

        // --- SAVE: update only the name and phone fields ---
        saveButton.setOnClickListener {
            val name = nameInput.text.toString().trim()
            val phone = phoneInput.text.toString().trim()

            if (name.isEmpty()) { nameInput.error = "Enter your name"; return@setOnClickListener }
            if (phone.length < 8) { phoneInput.error = "Enter a valid phone number"; return@setOnClickListener }

            progressBar.visibility = View.VISIBLE
            val updates = mapOf<String, Any>("name" to name, "phone" to phone)
            userRef.updateChildren(updates)
                .addOnSuccessListener {
                    progressBar.visibility = View.GONE
                    Toast.makeText(this, "Profile updated!", Toast.LENGTH_SHORT).show()
                }
                .addOnFailureListener { e ->
                    progressBar.visibility = View.GONE
                    Toast.makeText(this, "Update failed: ${e.message}", Toast.LENGTH_LONG).show()
                }
        }

        // --- LOG OUT ---
        findViewById<Button>(R.id.logoutButton).setOnClickListener {
            auth.signOut()
            goToLogin()
        }
    }

    private fun goToLogin() {
        startActivity(Intent(this, MainActivity::class.java))
        finish()
    }
}
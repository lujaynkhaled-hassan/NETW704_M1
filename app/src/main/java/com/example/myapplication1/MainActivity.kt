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
 * Login screen (the app's first screen).
 * Signs in with Firebase Email Authentication, then reads the user's
 * role from Realtime Database (users/<uid>/role) and redirects them.
 */
class MainActivity : AppCompatActivity() {

    private val auth = FirebaseAuth.getInstance()
    private val database = FirebaseDatabase.getInstance().reference

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        // Already logged in from a previous session? Skip the login screen.
        if (auth.currentUser != null) {
            openHome()
            return
        }

        setContentView(R.layout.activity_main)

        val emailInput = findViewById<EditText>(R.id.emailInput)
        val passwordInput = findViewById<EditText>(R.id.passwordInput)
        val loginButton = findViewById<Button>(R.id.loginButton)
        val progressBar = findViewById<ProgressBar>(R.id.progressBar)

        loginButton.setOnClickListener {
            val email = emailInput.text.toString().trim()
            val password = passwordInput.text.toString()

            if (email.isEmpty()) { emailInput.error = "Enter your email"; return@setOnClickListener }
            if (password.isEmpty()) { passwordInput.error = "Enter your password"; return@setOnClickListener }

            progressBar.visibility = View.VISIBLE
            loginButton.isEnabled = false

            auth.signInWithEmailAndPassword(email, password)
                .addOnSuccessListener { openHome() }
                .addOnFailureListener { e ->
                    progressBar.visibility = View.GONE
                    loginButton.isEnabled = true
                    Toast.makeText(this, "Login failed: ${e.message}", Toast.LENGTH_LONG).show()
                }
        }

        findViewById<TextView>(R.id.goToRegister).setOnClickListener {
            startActivity(Intent(this, RegisterActivity::class.java))
        }
    }

    /**
     * Reads the user's role from the database and opens their home screen.
     * For Milestone 1, buyers and sellers both go to ProfileActivity, which
     * shows the role. In later milestones each role gets its own app/screen.
     */
    private fun openHome() {
        val uid = auth.currentUser!!.uid
        database.child("users").child(uid).child("role").get()
            .addOnSuccessListener { snapshot ->
                val role = snapshot.getValue(String::class.java) ?: "buyer"
                Toast.makeText(this, "Welcome! Logged in as $role", Toast.LENGTH_SHORT).show()
                startActivity(Intent(this, ProfileActivity::class.java))
                finish() // remove login from the back stack
            }
            .addOnFailureListener { e ->
                Toast.makeText(this, "Could not load profile: ${e.message}", Toast.LENGTH_LONG).show()
                auth.signOut()
                recreate() // show the login form again
            }
    }
}
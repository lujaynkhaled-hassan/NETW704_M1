package com.example.myapplication1

import android.content.Intent
import android.os.Bundle
import android.util.Patterns
import android.view.View
import android.widget.Button
import android.widget.EditText
import android.widget.ProgressBar
import android.widget.RadioGroup
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.FirebaseDatabase

/**
 * Registration screen.
 * 1. Creates the account with Firebase Email Authentication.
 * 2. Immediately saves the profile (name, email, phone, role) to
 *    Realtime Database as JSON under users/<uid>.
 */
class RegisterActivity : AppCompatActivity() {

    private val auth = FirebaseAuth.getInstance()
    private val database = FirebaseDatabase.getInstance().reference

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_register)

        val nameInput = findViewById<EditText>(R.id.nameInput)
        val emailInput = findViewById<EditText>(R.id.emailInput)
        val phoneInput = findViewById<EditText>(R.id.phoneInput)
        val passwordInput = findViewById<EditText>(R.id.passwordInput)
        val roleGroup = findViewById<RadioGroup>(R.id.roleGroup)
        val registerButton = findViewById<Button>(R.id.registerButton)
        val progressBar = findViewById<ProgressBar>(R.id.progressBar)

        registerButton.setOnClickListener {
            val name = nameInput.text.toString().trim()
            val email = emailInput.text.toString().trim()
            val phone = phoneInput.text.toString().trim()
            val password = passwordInput.text.toString()
            val role = if (roleGroup.checkedRadioButtonId == R.id.sellerRadio) "seller" else "buyer"

            // --- Input validation ---
            if (name.isEmpty()) { nameInput.error = "Enter your name"; return@setOnClickListener }
            if (!Patterns.EMAIL_ADDRESS.matcher(email).matches()) { emailInput.error = "Enter a valid email"; return@setOnClickListener }
            if (phone.length < 8) { phoneInput.error = "Enter a valid phone number"; return@setOnClickListener }
            if (password.length < 6) { passwordInput.error = "At least 6 characters"; return@setOnClickListener }

            progressBar.visibility = View.VISIBLE
            registerButton.isEnabled = false

            // 1) Create the account in Firebase Authentication
            auth.createUserWithEmailAndPassword(email, password)
                .addOnSuccessListener { result ->
                    val uid = result.user!!.uid
                    val user = User(uid, name, email, phone, role)

                    // 2) Save the profile to Realtime Database under users/<uid>
                    database.child("users").child(uid).setValue(user)
                        .addOnSuccessListener {
                            Toast.makeText(this, "Account created!", Toast.LENGTH_SHORT).show()
                            startActivity(Intent(this, ProfileActivity::class.java))
                            finishAffinity() // close register + login so Back doesn't return to them
                        }
                        .addOnFailureListener { e ->
                            progressBar.visibility = View.GONE
                            registerButton.isEnabled = true
                            Toast.makeText(this, "Database error: ${e.message}", Toast.LENGTH_LONG).show()
                        }
                }
                .addOnFailureListener { e ->
                    progressBar.visibility = View.GONE
                    registerButton.isEnabled = true
                    Toast.makeText(this, "Sign up failed: ${e.message}", Toast.LENGTH_LONG).show()
                }
        }

        // Go back to the login screen
        findViewById<TextView>(R.id.goToLogin).setOnClickListener { finish() }
    }
}
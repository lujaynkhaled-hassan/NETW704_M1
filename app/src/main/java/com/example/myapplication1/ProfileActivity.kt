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
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.DatabaseReference
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.database.ValueEventListener

/**
 * Home / Profile screen.
 * - Listens to users/<uid> in real time: whenever the data changes in
 *   Firebase, the screen updates automatically.
 * - Lets the user edit name and phone; changes are written to Firebase immediately.
 */
class ProfileActivity : AppCompatActivity() {

    private val auth = FirebaseAuth.getInstance()
    private var userRef: DatabaseReference? = null
    private var profileListener: ValueEventListener? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_profile)

        // Only logged-in users may see this screen
        val currentUser = auth.currentUser
        if (currentUser == null) {
            goToLogin()
            return
        }

        val ref = FirebaseDatabase.getInstance().reference
            .child("users").child(currentUser.uid)
        userRef = ref

        val roleText = findViewById<TextView>(R.id.roleText)
        val emailText = findViewById<TextView>(R.id.emailText)
        val nameInput = findViewById<EditText>(R.id.nameInput)
        val phoneInput = findViewById<EditText>(R.id.phoneInput)
        val saveButton = findViewById<Button>(R.id.saveButton)
        val progressBar = findViewById<ProgressBar>(R.id.progressBar)

        // --- REAL-TIME READ: runs now, and again every time the data changes ---
        progressBar.visibility = View.VISIBLE
        val listener = object : ValueEventListener {
            override fun onDataChange(snapshot: DataSnapshot) {
                progressBar.visibility = View.GONE
                val user = snapshot.getValue(User::class.java) ?: return
                roleText.text = "Role: ${user.role.replaceFirstChar { it.uppercase() }}"
                emailText.text = "Email: ${user.email}"
                nameInput.setText(user.name)
                phoneInput.setText(user.phone)
            }

            override fun onCancelled(error: DatabaseError) {
                progressBar.visibility = View.GONE
                Toast.makeText(this@ProfileActivity, "Failed to load: ${error.message}", Toast.LENGTH_LONG).show()
            }
        }
        ref.addValueEventListener(listener)
        profileListener = listener

        // --- UPDATE: write the edited fields back to Firebase ---
        saveButton.setOnClickListener {
            val name = nameInput.text.toString().trim()
            val phone = phoneInput.text.toString().trim()

            if (name.isEmpty()) { nameInput.error = "Enter your name"; return@setOnClickListener }
            if (phone.length < 8) { phoneInput.error = "Enter a valid phone number"; return@setOnClickListener }

            progressBar.visibility = View.VISIBLE
            val updates = mapOf<String, Any>("name" to name, "phone" to phone)
            ref.updateChildren(updates)
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

    // Stop listening when the screen closes (avoids errors after logout)
    override fun onDestroy() {
        super.onDestroy()
        profileListener?.let { userRef?.removeEventListener(it) }
    }

    private fun goToLogin() {
        startActivity(Intent(this, MainActivity::class.java))
        finish()
    }
}
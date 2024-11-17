package com.itech.cdmrollhub

import android.content.Intent
import android.os.Bundle
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import com.google.firebase.Firebase
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.database.ValueEventListener
import com.google.firebase.database.database
import com.itech.cdmrollhub.databinding.ActivityForgotPasswordBinding

class ForgotPasswordActivity : AppCompatActivity() {

    private lateinit var binding: ActivityForgotPasswordBinding
    private lateinit var auth: FirebaseAuth
    private lateinit var database: FirebaseDatabase
    private val emailPattern = "[a-zA-Z0-9._-]+@pnm\\.edu\\.ph"

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityForgotPasswordBinding.inflate(layoutInflater)
        setContentView(binding.root)

        auth = FirebaseAuth.getInstance()
        database = Firebase.database

        binding.btnVerify.setOnClickListener {
            btnVerify()
        }

        binding.backBttn.setOnClickListener {
            handleBackBttn()
        }
    }

    private fun handleBackBttn() {
        val intent = Intent(this, LoginActivity::class.java)
        startActivity(intent)
    }

    private fun btnVerify() {
        val email = binding.email.text.toString()

        if (email.isEmpty()) {
            binding.email.error = "Enter email address"
            Toast.makeText(this, "Enter email address", Toast.LENGTH_SHORT).show()
        } else if (!email.matches(emailPattern.toRegex())) {
            binding.email.error = "Enter valid email address"
            Toast.makeText(this, "Enter valid email address", Toast.LENGTH_SHORT).show()
        } else {
            checkIfEmailExists(email)
        }
    }

    private fun checkIfEmailExists(email: String) {
        val usersRef = database.getReference("usersTbl")

        usersRef.orderByChild("email").equalTo(email).addListenerForSingleValueEvent(object :
            ValueEventListener {
            override fun onDataChange(snapshot: DataSnapshot) {
                if (snapshot.exists()) {
                    // Email exists in the database, send password reset email
                    sendPasswordResetEmail(email)
                } else {
                    // Email does not exist in the database
                    Toast.makeText(this@ForgotPasswordActivity, "Email is not registered in the system", Toast.LENGTH_SHORT).show()
                }
            }

            override fun onCancelled(error: DatabaseError) {
                Toast.makeText(this@ForgotPasswordActivity, "Failed to check email: ${error.message}", Toast.LENGTH_SHORT).show()
            }

        })
    }

    private fun sendPasswordResetEmail(email: String) {
        auth.sendPasswordResetEmail(email)
            .addOnSuccessListener {
                val intent = Intent(this, LoginActivity::class.java)
                startActivity(intent)
                Toast.makeText(this, "Password reset email sent", Toast.LENGTH_SHORT).show()
            }
            .addOnFailureListener {
                Toast.makeText(this, it.toString(), Toast.LENGTH_SHORT).show()
            }
    }
}
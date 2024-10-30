package com.itech.cdmrollhub

import android.content.Intent
import android.os.Bundle
import android.view.View
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import com.google.firebase.FirebaseApp
import com.google.firebase.Timestamp
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.FirebaseDatabase
import com.itech.cdmrollhub.databinding.ActivitySignupBinding

class SignupActivity : AppCompatActivity() {

    private lateinit var binding: ActivitySignupBinding
    private lateinit var auth: FirebaseAuth
    private lateinit var database: FirebaseDatabase

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivitySignupBinding.inflate(layoutInflater)
        setContentView(binding.root)

        FirebaseApp.initializeApp(this)

        auth = FirebaseAuth.getInstance()
        database = FirebaseDatabase.getInstance()

        binding.btnSignUp.setOnClickListener {
            submit()
        }

        binding.loginLink.setOnClickListener {
            handleLogIn()
        }
    }

    private fun submit() {
        binding.progressbar.visibility = View.VISIBLE
        val employee_id = binding.employeeID.text.toString().trim()
        val first_name = binding.firstName.text.toString().trim()
        val last_name = binding.lastName.text.toString().trim()
        val em = binding.email.text.toString().trim()
        val pass = binding.password.text.toString().trim()
        val c_pass = binding.confirmPassword.text.toString().trim()
        val emailPattern = "[a-zA-Z0-9._-]+@pnm\\.edu\\.ph"

        if(employee_id.isEmpty() || first_name.isEmpty() || last_name.isEmpty() || em.isEmpty() || pass.isEmpty() || c_pass.isEmpty()) {
            if(employee_id.isEmpty()) {
                binding.employeeID.error = "Employee ID is required!"
                binding.employeeID.requestFocus()
                binding.progressbar.visibility = View.GONE
                return
            }
            if(first_name.isEmpty()) {
                binding.firstName.error = "First Name is required!"
                binding.firstName.requestFocus()
                binding.progressbar.visibility = View.GONE
                return
            }
            if(last_name.isEmpty()) {
                binding.lastName.error = "Last Name is required!"
                binding.lastName.requestFocus()
                binding.progressbar.visibility = View.GONE
                return
            }
            if(em.isEmpty()) {
                binding.email.error = "Email is required!"
                binding.email.requestFocus()
                binding.progressbar.visibility = View.GONE
                return
            }
            if(pass.isEmpty()) {
                binding.password.error = "Password is required!"
                binding.password.requestFocus()
                binding.progressbar.visibility = View.GONE
                return
            }
            if(c_pass.isEmpty()) {
                binding.confirmPassword.error = "Confirm Password is required!"
                binding.confirmPassword.requestFocus()
                binding.progressbar.visibility = View.GONE
                return
            }
            Toast.makeText(this,"All fields are Required!", Toast.LENGTH_SHORT).show()
            binding.progressbar.visibility = View.GONE
            return
        } else if (!em.matches(emailPattern.toRegex())){
            binding.email.error = "Enter valid email address"
            binding.email.requestFocus()
            binding.progressbar.visibility = View.GONE
            return
        } else if (pass.length < 6){
            binding.password.error = "Enter your password more than 6 characters"
            binding.password.requestFocus()
            binding.progressbar.visibility = View.GONE
            return
        } else if (pass != c_pass){
            binding.confirmPassword.error = "Password not match"
            binding.confirmPassword.requestFocus()
            binding.progressbar.visibility = View.GONE
            return
        } else {
            createAccount(employee_id, first_name, last_name, em, pass)
        }
    }

    private fun createAccount(
        employee_id: String,
        first_name: String,
        last_name: String,
        em: String,
        pass: String
    ) {
        auth.createUserWithEmailAndPassword(em, pass)
            .addOnCompleteListener(this) { task ->
                if (task.isSuccessful) {
                    val user = auth.currentUser
                    val uid = user?.uid
                    val timestamp = Timestamp.now()
                    val profilePicture = "https://firebasestorage.googleapis.com/v0/b/cdm-payroll-system.appspot.com/o/profilePicture%2Fprofile_icon.png?alt=media&token=328ee3a4-46ad-4926-a0e3-4dd29a102223"

                    val userData = hashMapOf(
                        "employeeID" to employee_id,
                        "firstName" to first_name,
                        "lastName" to last_name,
                        "email" to em,
                        "password" to pass,
                        "profilePicture" to profilePicture,
                        "createdAt" to timestamp,
                    )

                    if (uid != null) {
                        database.reference.child("usersTbl").child(uid).setValue(userData)
                            .addOnCompleteListener { task ->
                                if (task.isSuccessful) {
                                    Toast.makeText(this, "Registered successfully", Toast.LENGTH_SHORT).show()
                                    binding.progressbar.visibility = View.GONE
                                    val intent = Intent(this, LoginActivity::class.java)
                                    startActivity(intent)
                                } else {
                                    binding.progressbar.visibility = View.GONE
                                    Toast.makeText(this, "Failed to register: ${task.exception?.message}", Toast.LENGTH_SHORT).show()
                                }
                            }
                    }

                } else {
                    binding.progressbar.visibility = View.GONE
                    Toast.makeText(this, "${task.exception?.message}", Toast.LENGTH_SHORT).show()
                }
            }
    }

    private fun handleLogIn() {
        val intent = Intent(this, LoginActivity::class.java)
        startActivity(intent)
    }
}
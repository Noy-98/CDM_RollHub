package com.itech.cdmrollhub.dashboard

import android.app.Activity
import android.content.Intent
import android.net.Uri
import android.os.Bundle
import android.provider.MediaStore
import android.util.Log
import android.util.Patterns
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import com.google.firebase.auth.EmailAuthProvider
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.storage.FirebaseStorage
import com.google.firebase.storage.StorageReference
import com.itech.cdmrollhub.R
import com.itech.cdmrollhub.databinding.FragmentProfileBinding
import com.squareup.picasso.Picasso
import java.util.UUID

class Profile : Fragment() {

    private var _binding: FragmentProfileBinding? = null
    private val binding get() = _binding!!
    private val firebaseAuth = FirebaseAuth.getInstance()
    private val storage = FirebaseStorage.getInstance()
    private val storageReference: StorageReference = storage.reference
    private val PICK_IMAGE_REQUEST = 1
    private var imageUri: Uri? = null

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        _binding = FragmentProfileBinding.inflate(inflater, container, false)

        // Set up click listeners
        displayUser()

        binding.profilePic.setOnClickListener {
            openGallery()
        }

        binding.btnSave.setOnClickListener {
            updateUserProfile()
        }

        return binding.root
    }

    private fun displayUser() {
        val currentUserId = firebaseAuth.currentUser?.uid

        if (currentUserId != null) {
            val userRef = FirebaseDatabase.getInstance().getReference("usersTbl").child(currentUserId)

            userRef.get().addOnSuccessListener { snapshot ->
                if (snapshot.exists()) {
                    val userMap = snapshot.value as? Map<String, Any?>
                    if (userMap != null) {
                        val employeeId = userMap["employeeID"] as? String
                        val firstName = userMap["firstName"] as? String
                        val lastName = userMap["lastName"] as? String
                        val email = userMap["email"] as? String
                        val profileImageUrl = snapshot.child("profilePicture").getValue(String::class.java)

                        binding.employeeID.setText(employeeId ?: "")
                        binding.firstName.setText(firstName ?: "")
                        binding.lastName.setText(lastName ?: "")
                        binding.email.setText(email ?: "")

                        if (!profileImageUrl.isNullOrEmpty()) {
                            Picasso.get().load(profileImageUrl).into(binding.profilePic)
                        } else {
                            binding.profilePic.setImageResource(R.drawable.camera_icon)
                        }
                    }
                } else {
                    Toast.makeText(requireContext(), "User data does not exist in the database", Toast.LENGTH_SHORT).show()
                }
            }.addOnFailureListener {
                Toast.makeText(requireContext(), "Failed to retrieve user data", Toast.LENGTH_SHORT).show()
            }
        } else {
            Log.e("displayUser", "User is not logged in")
        }
    }

    private fun openGallery() {
        val intent = Intent(Intent.ACTION_PICK, MediaStore.Images.Media.EXTERNAL_CONTENT_URI)
        startActivityForResult(intent, PICK_IMAGE_REQUEST)
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        if (requestCode == PICK_IMAGE_REQUEST && resultCode == Activity.RESULT_OK && data != null && data.data != null) {
            imageUri = data.data
            if (imageUri != null) {
                binding.progressbar.visibility = View.VISIBLE
                deleteOldImage()
            }
        }
    }

    private fun deleteOldImage() {
        val currentUser = firebaseAuth.currentUser
        if (currentUser != null) {
            val uid = currentUser.uid
            val usersReference = FirebaseDatabase.getInstance().getReference("usersTbl/$uid")

            usersReference.child("profilePicture").get().addOnSuccessListener { snapshot ->
                val profileImageUrl = snapshot.value as? String
                if (profileImageUrl != null) {
                    val oldAvatarRef = storage.getReferenceFromUrl(profileImageUrl)
                    oldAvatarRef.delete().addOnSuccessListener {
                        storeImage()
                    }.addOnFailureListener {
                        storeImage()
                    }
                } else {
                    storeImage()
                }
            }
        }
    }

    private fun storeImage() {
        val userId = firebaseAuth.currentUser?.uid ?: return
        val fileReference = storageReference.child("profilePicture/$userId/${UUID.randomUUID()}.jpg")

        fileReference.putFile(imageUri!!)
            .addOnSuccessListener {
                fileReference.downloadUrl.addOnSuccessListener { uri ->
                    val downloadUrl = uri.toString()
                    val userReference = FirebaseDatabase.getInstance().getReference("usersTbl/$userId")
                    userReference.child("profilePicture").setValue(downloadUrl)
                        .addOnSuccessListener {
                            binding.progressbar.visibility = View.GONE
                            Picasso.get().load(downloadUrl).into(binding.profilePic)
                            Toast.makeText(requireContext(), "Profile image updated successfully", Toast.LENGTH_SHORT).show()
                        }
                        .addOnFailureListener {
                            binding.progressbar.visibility = View.GONE
                            Toast.makeText(requireContext(), "Failed to update profile image", Toast.LENGTH_SHORT).show()
                        }
                }
            }
            .addOnFailureListener {
                binding.progressbar.visibility = View.GONE
                Toast.makeText(requireContext(), "Image upload failed", Toast.LENGTH_SHORT).show()
            }
    }

    private fun updateUserProfile() {
        val employeeId = binding.employeeID.text.toString().trim()
        val firstName = binding.firstName.text.toString().trim()
        val lastName = binding.lastName.text.toString().trim()
        val newPassword = binding.newPassword.text.toString()
        val confirmPassword = binding.confirmPassword.text.toString()
        val oldPassword = binding.oldPassword.text.toString() // Updated to match your XML

        // Validate input
        if (employeeId.isEmpty() || firstName.isEmpty() || lastName.isEmpty()) {
            Toast.makeText(requireContext(), "Please enter valid profile details.", Toast.LENGTH_SHORT).show()
            return
        }

        val currentUser = firebaseAuth.currentUser
        if (currentUser != null && currentUser.email != null) {
            val uid = currentUser.uid
            val userRef = FirebaseDatabase.getInstance().getReference("usersTbl/$uid")

            // Update password if provided
            if (oldPassword.isNotEmpty() || newPassword.isNotEmpty() || confirmPassword.isNotEmpty()) {
                if (newPassword.length < 6) {
                    Toast.makeText(requireContext(), "Password must be at least 6 characters", Toast.LENGTH_SHORT).show()
                    return
                } else if (newPassword != confirmPassword) {
                    Toast.makeText(requireContext(), "Passwords do not match", Toast.LENGTH_SHORT).show()
                    return
                } else {
                    val credential = EmailAuthProvider
                        .getCredential(currentUser.email!!, oldPassword)
                    currentUser.reauthenticate(credential)
                        .addOnCompleteListener { response ->
                            if (response.isSuccessful) {
                                currentUser.updatePassword(newPassword)
                                    .addOnCompleteListener { task ->
                                        if (task.isSuccessful) {
                                            binding.progressbar.visibility = View.GONE
                                            Toast.makeText(requireContext(), "Password is successfully updated.", Toast.LENGTH_SHORT).show()
                                            parentFragmentManager.popBackStack()
                                        } else {
                                            binding.progressbar.visibility = View.GONE
                                            Toast.makeText(requireContext(), "Something went wrong.", Toast.LENGTH_SHORT).show()
                                        }
                                    }
                            } else {
                                binding.progressbar.visibility = View.GONE
                                Toast.makeText(requireContext(), "Current password is incorrect", Toast.LENGTH_SHORT).show()
                            }
                        }
                }
            }

            // Update other profile information
            val updates = mapOf(
                "employeeID" to employeeId,
                "firstName" to firstName,
                "lastName" to lastName
            )

            userRef.updateChildren(updates).addOnSuccessListener {
                Toast.makeText(requireContext(), "Profile updated successfully", Toast.LENGTH_SHORT).show()
            }.addOnFailureListener {
                Toast.makeText(requireContext(), "Failed to update profile", Toast.LENGTH_SHORT).show()
            }
        }
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}

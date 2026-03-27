package com.example.flexrise

import android.net.Uri
import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.EditText
import android.widget.ImageView
import android.widget.Toast
import androidx.activity.result.PickVisualMediaRequest
import androidx.activity.result.contract.ActivityResultContracts
import androidx.appcompat.widget.AppCompatButton
import androidx.fragment.app.Fragment
import com.bumptech.glide.Glide
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.storage.FirebaseStorage

class EditProfileFragment : Fragment() {

    private val auth = FirebaseAuth.getInstance()
    private val database = FirebaseDatabase.getInstance()
    private val storage = FirebaseStorage.getInstance()

    private lateinit var ivProfile: ImageView
    private lateinit var etName: EditText
    private lateinit var etEmail: EditText
    private lateinit var etWeight: EditText
    private lateinit var etHeight: EditText
    
    private var selectedImageUri: Uri? = null

    private val pickMedia = registerForActivityResult(ActivityResultContracts.PickVisualMedia()) { uri ->
        if (uri != null) {
            selectedImageUri = uri
            Glide.with(this).load(uri).into(ivProfile)
        }
    }

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        val view = inflater.inflate(R.layout.fragment_edit_profile, container, false)

        ivProfile = view.findViewById(R.id.iv_edit_profile_photo)
        etName = view.findViewById(R.id.et_full_name)
        etEmail = view.findViewById(R.id.et_email)
        etWeight = view.findViewById(R.id.et_weight)
        etHeight = view.findViewById(R.id.et_height)
        val btnSave = view.findViewById<AppCompatButton>(R.id.btn_save)
        val tvChangePhoto = view.findViewById<View>(R.id.tv_change_photo)

        val uid = auth.currentUser?.uid
        if (uid != null) {
            database.reference.child("Users").child(uid).get().addOnSuccessListener { snapshot ->
                if (snapshot.exists()) {
                    etName.setText(snapshot.child("name").getValue(String::class.java))
                    etEmail.setText(snapshot.child("email").getValue(String::class.java))
                    etWeight.setText(snapshot.child("weight").getValue(String::class.java))
                    etHeight.setText(snapshot.child("height").getValue(String::class.java))
                    
                    val imageUrl = snapshot.child("profileImage").getValue(String::class.java)
                    if (imageUrl != null) {
                        Glide.with(this).load(imageUrl).placeholder(R.drawable.profile_avatar).into(ivProfile)
                    }
                }
            }
        }

        tvChangePhoto.setOnClickListener {
            pickMedia.launch(PickVisualMediaRequest(ActivityResultContracts.PickVisualMedia.ImageOnly))
        }

        btnSave.setOnClickListener {
            val name = etName.text.toString().trim()
            val email = etEmail.text.toString().trim()
            val weight = etWeight.text.toString().trim()
            val height = etHeight.text.toString().trim()

            if (name.isEmpty() || email.isEmpty()) {
                Toast.makeText(requireContext(), "Name and Email are required", Toast.LENGTH_SHORT).show()
                return@setOnClickListener
            }

            btnSave.isEnabled = false
            btnSave.text = "Saving..."

            if (selectedImageUri != null) {
                uploadImageAndSaveData(uid!!, name, email, weight, height)
            } else {
                saveDataToDatabase(uid!!, name, email, weight, height, null)
            }
        }

        return view
    }

    private fun uploadImageAndSaveData(uid: String, name: String, email: String, weight: String, height: String) {
        val storageRef = storage.reference.child("profile_images/$uid.jpg")
        
        storageRef.putFile(selectedImageUri!!)
            .addOnSuccessListener {
                storageRef.downloadUrl.addOnSuccessListener { downloadUri ->
                    saveDataToDatabase(uid, name, email, weight, height, downloadUri.toString())
                }
            }
            .addOnFailureListener { e ->
                val btnSave = view?.findViewById<AppCompatButton>(R.id.btn_save)
                btnSave?.isEnabled = true
                btnSave?.text = "Save"
                Log.e("EditProfile", "Upload failed", e)
                Toast.makeText(requireContext(), "Upload failed: ${e.message}", Toast.LENGTH_LONG).show()
            }
    }

    private fun saveDataToDatabase(uid: String, name: String, email: String, weight: String, height: String, imageUrl: String?) {
        val userMap = mutableMapOf<String, Any>(
            "name" to name,
            "email" to email,
            "weight" to weight,
            "height" to height
        )
        if (imageUrl != null) {
            userMap["profileImage"] = imageUrl
        }

        database.reference.child("Users").child(uid).updateChildren(userMap)
            .addOnSuccessListener {
                Toast.makeText(requireContext(), "Profile Updated!", Toast.LENGTH_SHORT).show()
                parentFragmentManager.popBackStack()
            }
            .addOnFailureListener { e ->
                val btnSave = view?.findViewById<AppCompatButton>(R.id.btn_save)
                btnSave?.isEnabled = true
                btnSave?.text = "Save"
                Toast.makeText(requireContext(), "Database update failed: ${e.message}", Toast.LENGTH_SHORT).show()
            }
    }
}
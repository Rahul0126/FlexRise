package com.example.flexrise

import android.os.Bundle
import android.text.Html
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.EditText
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.widget.AppCompatButton
import androidx.fragment.app.Fragment
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.FirebaseDatabase

class SignupFragment : Fragment() {

    private lateinit var auth: FirebaseAuth
    private lateinit var database: FirebaseDatabase

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        val view = inflater.inflate(R.layout.fragment_signup, container, false)
        
        auth = FirebaseAuth.getInstance()
        database = FirebaseDatabase.getInstance()

        val etFullName = view.findViewById<EditText>(R.id.et_full_name)
        val etEmail = view.findViewById<EditText>(R.id.et_email)
        val etPassword = view.findViewById<EditText>(R.id.et_password)
        val btnSignup = view.findViewById<AppCompatButton>(R.id.btn_signup)
        val tvAlreadyAccount = view.findViewById<TextView>(R.id.tv_already_account)

        tvAlreadyAccount.text = Html.fromHtml(getString(R.string.already_have_account), Html.FROM_HTML_MODE_COMPACT)

        // Set the listener for "Already have an account? Login" here
        tvAlreadyAccount.setOnClickListener {
            parentFragmentManager.beginTransaction()
                .replace(R.id.fragment_container, LoginFragment())
                .addToBackStack(null)
                .commit()
        }

        btnSignup.setOnClickListener {
            val name = etFullName.text.toString().trim()
            val email = etEmail.text.toString().trim()
            val password = etPassword.text.toString().trim()

            if (name.isEmpty() || email.isEmpty() || password.isEmpty()) {
                Toast.makeText(requireContext(), "Please fill all fields", Toast.LENGTH_SHORT).show()
                return@setOnClickListener
            }

            if (password.length < 6) {
                Toast.makeText(requireContext(), "Password should be at least 6 characters", Toast.LENGTH_SHORT).show()
                return@setOnClickListener
            }

            Log.d("SignupFragment", "Starting user creation...")
            auth.createUserWithEmailAndPassword(email, password)
                .addOnCompleteListener { task ->
                    if (task.isSuccessful) {
                        Log.d("SignupFragment", "Auth successful, writing to database...")
                        val userId = auth.currentUser?.uid
                        val userMap = mapOf(
                            "name" to name,
                            "email" to email,
                            "height" to "0",
                            "weight" to "0"
                        )
                        
                        userId?.let { uid ->
                            database.reference.child("Users").child(uid).setValue(userMap)
                                .addOnCompleteListener { dbTask ->
                                    if (dbTask.isSuccessful) {
                                        Log.d("SignupFragment", "Database write successful, navigating...")
                                        Toast.makeText(requireContext(), "Account Created Successfully!", Toast.LENGTH_SHORT).show()
                                        
                                        requireActivity().supportFragmentManager.beginTransaction()
                                            .replace(R.id.fragment_container, HomeFragment())
                                            .commitAllowingStateLoss()
                                    } else {
                                        val errorMsg = dbTask.exception?.message ?: "Unknown database error"
                                        Log.e("SignupFragment", "Database error: $errorMsg")
                                        Toast.makeText(requireContext(), "Database Error: $errorMsg", Toast.LENGTH_SHORT).show()
                                    }
                                }
                        }
                    } else {
                        val authError = task.exception?.message ?: "Unknown auth error"
                        Log.e("SignupFragment", "Auth error: $authError")
                        Toast.makeText(requireContext(), "Auth Error: $authError", Toast.LENGTH_LONG).show()
                    }
                }
        }
        
        return view
    }
}
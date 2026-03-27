package com.example.flexrise

import android.app.AlertDialog
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.EditText
import android.widget.ImageView
import android.widget.TextView
import android.widget.Toast
import androidx.fragment.app.Fragment
import com.google.firebase.auth.FirebaseAuth

class PrivacySecurityFragment : Fragment() {

    private val auth = FirebaseAuth.getInstance()

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        val view = inflater.inflate(R.layout.fragment_privacy_security, container, false)

        val btnBack = view.findViewById<ImageView>(R.id.btn_privacy_back)
        val tvChangePassword = view.findViewById<TextView>(R.id.tv_change_password)
        val tvPrivacyPolicy = view.findViewById<TextView>(R.id.tv_privacy_policy)
        val tvTermsService = view.findViewById<TextView>(R.id.tv_terms_service)

        btnBack?.setOnClickListener {
            parentFragmentManager.popBackStack()
        }

        tvChangePassword.setOnClickListener {
            showChangePasswordDialog()
        }

        tvPrivacyPolicy.setOnClickListener {
            showStaticContentDialog("Privacy Policy", getString(R.string.dummy_privacy_policy))
        }

        tvTermsService.setOnClickListener {
            showStaticContentDialog("Terms of Service", getString(R.string.dummy_terms_service))
        }

        return view
    }

    private fun showChangePasswordDialog() {
        val builder = AlertDialog.Builder(requireContext())
        val dialogView = layoutInflater.inflate(R.layout.dialog_change_password, null)
        builder.setView(dialogView)

        val etNewPassword = dialogView.findViewById<EditText>(R.id.et_new_password)
        val btnUpdate = dialogView.findViewById<Button>(R.id.btn_update_password)

        val dialog = builder.create()

        btnUpdate.setOnClickListener {
            val newPassword = etNewPassword.text.toString().trim()
            if (newPassword.length >= 6) {
                auth.currentUser?.updatePassword(newPassword)
                    ?.addOnCompleteListener { task ->
                        if (task.isSuccessful) {
                            Toast.makeText(context, "Password updated successfully!", Toast.LENGTH_SHORT).show()
                            dialog.dismiss()
                        } else {
                            Toast.makeText(context, "Error: ${task.exception?.message}", Toast.LENGTH_LONG).show()
                        }
                    }
            } else {
                Toast.makeText(context, "Password must be at least 6 characters", Toast.LENGTH_SHORT).show()
            }
        }

        dialog.show()
    }

    private fun showStaticContentDialog(title: String, content: String) {
        AlertDialog.Builder(requireContext())
            .setTitle(title)
            .setMessage(content)
            .setPositiveButton("Close", null)
            .show()
    }
}
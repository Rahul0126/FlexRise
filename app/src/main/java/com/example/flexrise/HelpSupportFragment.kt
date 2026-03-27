package com.example.flexrise

import android.content.Intent
import android.net.Uri
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AlertDialog
import androidx.fragment.app.Fragment

class HelpSupportFragment : Fragment() {

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        val view = inflater.inflate(R.layout.fragment_help_support, container, false)

        val btnBack = view.findViewById<ImageView>(R.id.btn_help_back)
        val tvFaqs = view.findViewById<TextView>(R.id.tv_faqs)
        val tvContactUs = view.findViewById<TextView>(R.id.tv_contact_us)
        val tvFeedback = view.findViewById<TextView>(R.id.tv_feedback)

        btnBack?.setOnClickListener {
            parentFragmentManager.popBackStack()
        }

        tvFaqs.setOnClickListener {
            showSimpleDialog("FAQs", "1. How to track steps? Just keep the app open!\n2. How to log meals? Go to Nutrition tab.\n3. Is my data safe? Yes, we use Firebase.")
        }

        tvContactUs.setOnClickListener {
            val intent = Intent(Intent.ACTION_SENDTO).apply {
                data = Uri.parse("mailto:support@flexrise.com")
                putExtra(Intent.EXTRA_SUBJECT, "Help Request - FlexRise")
            }
            try {
                startActivity(intent)
            } catch (e: Exception) {
                Toast.makeText(context, "No email app found", Toast.LENGTH_SHORT).show()
            }
        }

        tvFeedback.setOnClickListener {
            showSimpleDialog("Feedback", "Thank you for using FlexRise! Please email us at feedback@flexrise.com with your suggestions.")
        }

        return view
    }

    private fun showSimpleDialog(title: String, message: String) {
        AlertDialog.Builder(requireContext())
            .setTitle(title)
            .setMessage(message)
            .setPositiveButton("OK", null)
            .show()
    }
}
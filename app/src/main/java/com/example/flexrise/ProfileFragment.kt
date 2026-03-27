package com.example.flexrise

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.LinearLayout
import android.widget.TextView
import androidx.fragment.app.Fragment
import com.bumptech.glide.Glide
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.FirebaseDatabase

class ProfileFragment : Fragment() {

    private val auth = FirebaseAuth.getInstance()
    private val database = FirebaseDatabase.getInstance()

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        val view = inflater.inflate(R.layout.fragment_profile, container, false)

        val ivProfile = view.findViewById<ImageView>(R.id.iv_profile)
        val tvProfileName = view.findViewById<TextView>(R.id.tv_profile_name)
        val tvHeight = view.findViewById<TextView>(R.id.tv_height_display)
        val tvWeight = view.findViewById<TextView>(R.id.tv_weight_display)
        
        val editProfileItem = view.findViewById<TextView>(R.id.tv_edit_profile)
        val notificationsItem = view.findViewById<TextView>(R.id.tv_notifications)
        val logoutItem = view.findViewById<TextView>(R.id.tv_logout)

        // Fetch and update user data from Firebase in real-time
        val uid = auth.currentUser?.uid
        if (uid != null) {
            database.reference.child("Users").child(uid).addValueEventListener(object : com.google.firebase.database.ValueEventListener {
                override fun onDataChange(snapshot: com.google.firebase.database.DataSnapshot) {
                    if (snapshot.exists()) {
                        val name = snapshot.child("name").getValue(String::class.java)
                        val height = snapshot.child("height").getValue(String::class.java) ?: "0"
                        val weight = snapshot.child("weight").getValue(String::class.java) ?: "0"
                        val imageUrl = snapshot.child("profileImage").getValue(String::class.java)

                        tvProfileName.text = name ?: getString(R.string.profile_name)
                        tvHeight.text = "$height cm"
                        tvWeight.text = "$weight kg"

                        if (imageUrl != null && isAdded) {
                            Glide.with(this@ProfileFragment).load(imageUrl).into(ivProfile)
                        }
                    }
                }
                override fun onCancelled(error: com.google.firebase.database.DatabaseError) {}
            })
        }

        // Navigation to Edit Profile
        editProfileItem?.setOnClickListener {
            parentFragmentManager.beginTransaction()
                .replace(R.id.fragment_container, EditProfileFragment())
                .addToBackStack(null)
                .commit()
        }

        // Navigation to Notifications
        notificationsItem?.setOnClickListener {
            parentFragmentManager.beginTransaction()
                .replace(R.id.fragment_container, NotificationsFragment())
                .addToBackStack(null)
                .commit()
        }

        // Logout logic
        logoutItem?.setOnClickListener {
            auth.signOut()
            parentFragmentManager.beginTransaction()
                .replace(R.id.fragment_container, LoginFragment())
                .commit()
        }

        // Bottom Navigation logic
        view.findViewById<View>(R.id.nav_home).setOnClickListener {
            parentFragmentManager.beginTransaction().replace(R.id.fragment_container, HomeFragment()).commit()
        }
        view.findViewById<View>(R.id.nav_activity).setOnClickListener {
            parentFragmentManager.beginTransaction().replace(R.id.fragment_container, ActivityFragment()).commit()
        }
        view.findViewById<View>(R.id.nav_nutrition).setOnClickListener {
            parentFragmentManager.beginTransaction().replace(R.id.fragment_container, NutritionFragment()).commit()
        }

        return view
    }
}
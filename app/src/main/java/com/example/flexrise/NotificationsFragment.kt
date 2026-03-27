package com.example.flexrise

import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import androidx.appcompat.widget.SwitchCompat
import androidx.fragment.app.Fragment
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.FirebaseDatabase

class NotificationsFragment : Fragment() {

    private val auth = FirebaseAuth.getInstance()
    private val database = FirebaseDatabase.getInstance()
    private val uid = auth.currentUser?.uid

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        val view = inflater.inflate(R.layout.fragment_notifications, container, false)

        val btnBack = view.findViewById<ImageView>(R.id.btn_notifications_back)
        val switchDaily = view.findViewById<SwitchCompat>(R.id.switch_daily_workout)
        val switchWeekly = view.findViewById<SwitchCompat>(R.id.switch_weekly_goals)
        val switchMeal = view.findViewById<SwitchCompat>(R.id.switch_meal_reminders)
        val switchWater = view.findViewById<SwitchCompat>(R.id.switch_water_intake)
        val switchUpdate = view.findViewById<SwitchCompat>(R.id.switch_app_update)

        btnBack?.setOnClickListener {
            parentFragmentManager.popBackStack()
        }

        // Load existing settings from Firebase
        if (uid != null) {
            database.reference.child("Users").child(uid).child("settings").child("notifications")
                .get().addOnSuccessListener { snapshot ->
                    if (snapshot.exists()) {
                        switchDaily.isChecked = snapshot.child("daily_workout").getValue(Boolean::class.java) ?: true
                        switchWeekly.isChecked = snapshot.child("weekly_goals").getValue(Boolean::class.java) ?: true
                        switchMeal.isChecked = snapshot.child("meal_reminders").getValue(Boolean::class.java) ?: true
                        switchWater.isChecked = snapshot.child("water_intake").getValue(Boolean::class.java) ?: true
                        switchUpdate.isChecked = snapshot.child("app_update").getValue(Boolean::class.java) ?: true
                    }
                }
        }

        // Save settings when toggled
        val listener = { key: String, isChecked: Boolean ->
            uid?.let {
                database.reference.child("Users").child(it).child("settings").child("notifications")
                    .child(key).setValue(isChecked)
            }
        }

        switchDaily.setOnCheckedChangeListener { _, isChecked -> listener("daily_workout", isChecked) }
        switchWeekly.setOnCheckedChangeListener { _, isChecked -> listener("weekly_goals", isChecked) }
        switchMeal.setOnCheckedChangeListener { _, isChecked -> listener("meal_reminders", isChecked) }
        switchWater.setOnCheckedChangeListener { _, isChecked -> listener("water_intake", isChecked) }
        switchUpdate.setOnCheckedChangeListener { _, isChecked -> listener("app_update", isChecked) }

        return view
    }
}
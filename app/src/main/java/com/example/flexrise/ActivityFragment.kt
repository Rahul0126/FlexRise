package com.example.flexrise

import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.LinearLayout
import android.widget.ProgressBar
import android.widget.TextView
import androidx.core.content.ContextCompat
import androidx.fragment.app.Fragment
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.FirebaseDatabase
import java.text.SimpleDateFormat
import java.util.*

class ActivityFragment : Fragment() {

    private val auth = FirebaseAuth.getInstance()
    private val database = FirebaseDatabase.getInstance()
    private val sdf = SimpleDateFormat("yyyy-MM-dd", Locale.getDefault())
    
    private lateinit var tvSteps: TextView
    private lateinit var tvCalories: TextView
    private lateinit var tvDistance: TextView
    
    private val progressBars = mutableMapOf<String, ProgressBar>()
    private val calendarViews = mutableMapOf<String, TextView>()
    private val weekDateMap = mutableMapOf<String, String>() // Map "Mon" -> "2024-03-04"

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        val view = inflater.inflate(R.layout.fragment_activity, container, false)

        tvSteps = view.findViewById(R.id.tv_activity_steps)
        tvCalories = view.findViewById(R.id.tv_activity_calories)
        tvDistance = view.findViewById(R.id.tv_activity_distance)

        // Mapping ProgressBars
        progressBars["Mon"] = view.findViewById(R.id.progress_mon)
        progressBars["Tue"] = view.findViewById(R.id.progress_tue)
        progressBars["Wed"] = view.findViewById(R.id.progress_wed)
        progressBars["Thu"] = view.findViewById(R.id.progress_thu)
        progressBars["Fri"] = view.findViewById(R.id.progress_fri)
        progressBars["Sat"] = view.findViewById(R.id.progress_sat)
        progressBars["Sun"] = view.findViewById(R.id.progress_sun)

        // Mapping Calendar Header Views
        calendarViews["Mon"] = view.findViewById(R.id.cal_mon)
        calendarViews["Tue"] = view.findViewById(R.id.cal_tue)
        calendarViews["Wed"] = view.findViewById(R.id.cal_wed)
        calendarViews["Thu"] = view.findViewById(R.id.cal_thu)
        calendarViews["Fri"] = view.findViewById(R.id.cal_fri)
        calendarViews["Sat"] = view.findViewById(R.id.cal_sat)
        calendarViews["Sun"] = view.findViewById(R.id.cal_sun)

        loadWeeklyData()
        setupCalendarClickListeners()
        setupNavigation(view)

        return view
    }

    private fun loadWeeklyData() {
        val uid = auth.currentUser?.uid ?: return
        val calendar = Calendar.getInstance()
        
        // Ensure we calculate correctly for current week starting Monday
        calendar.firstDayOfWeek = Calendar.MONDAY
        calendar.set(Calendar.DAY_OF_WEEK, Calendar.MONDAY)
        
        val weekDays = listOf("Mon", "Tue", "Wed", "Thu", "Fri", "Sat", "Sun")
        val todayStr = sdf.format(Date())

        for (dayName in weekDays) {
            val dateStr = sdf.format(calendar.time)
            weekDateMap[dayName] = dateStr
            
            // Highlight today by default
            if (dateStr == todayStr) {
                highlightSelectedDay(dayName)
            }

            database.reference.child("ActivityLogs").child(uid).child(dateStr).child("steps")
                .get().addOnSuccessListener { snapshot ->
                    val steps = snapshot.getValue(Int::class.java) ?: 0
                    progressBars[dayName]?.progress = steps
                    
                    // Show today's data in stat cards on initial load
                    if (dateStr == todayStr) {
                        updateMainStats(steps)
                    }
                }
            
            calendar.add(Calendar.DAY_OF_YEAR, 1)
        }
    }

    private fun setupCalendarClickListeners() {
        for ((dayName, textView) in calendarViews) {
            textView.setOnClickListener {
                highlightSelectedDay(dayName)
                val selectedDate = weekDateMap[dayName]
                if (selectedDate != null) {
                    fetchDataForSelectedDay(selectedDate)
                }
            }
        }
    }

    private fun highlightSelectedDay(selectedDayName: String) {
        for ((dayName, textView) in calendarViews) {
            if (dayName == selectedDayName) {
                textView.setBackgroundResource(R.drawable.bg_selected_day)
                textView.setTextColor(ContextCompat.getColor(requireContext(), R.color.black))
            } else {
                textView.setBackgroundResource(R.drawable.bg_day_unselected)
                textView.setTextColor(ContextCompat.getColor(requireContext(), R.color.white))
            }
        }
    }

    private fun fetchDataForSelectedDay(date: String) {
        val uid = auth.currentUser?.uid ?: return
        database.reference.child("ActivityLogs").child(uid).child(date).child("steps")
            .get().addOnSuccessListener { snapshot ->
                val steps = snapshot.getValue(Int::class.java) ?: 0
                updateMainStats(steps)
            }
    }

    private fun updateMainStats(steps: Int) {
        tvSteps.text = steps.toString()
        val calories = (steps * 0.04).toInt()
        val distance = (steps * 0.0008)
        tvCalories.text = "$calories"
        tvDistance.text = String.format("%.1f km", distance)
    }

    private fun setupNavigation(view: View) {
        view.findViewById<View>(R.id.nav_home).setOnClickListener {
            parentFragmentManager.beginTransaction().replace(R.id.fragment_container, HomeFragment()).commit()
        }
        view.findViewById<View>(R.id.nav_activity).setOnClickListener { }
        view.findViewById<View>(R.id.nav_nutrition).setOnClickListener {
            parentFragmentManager.beginTransaction().replace(R.id.fragment_container, NutritionFragment()).commit()
        }
        view.findViewById<View>(R.id.nav_profile).setOnClickListener {
            parentFragmentManager.beginTransaction().replace(R.id.fragment_container, ProfileFragment()).commit()
        }
    }
}
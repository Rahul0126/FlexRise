package com.example.flexrise

import android.app.AlertDialog
import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.*
import androidx.core.content.ContextCompat
import androidx.fragment.app.Fragment
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.*
import java.text.SimpleDateFormat
import java.util.*

class NutritionFragment : Fragment() {

    private lateinit var tvIntakeDisplay: TextView
    private lateinit var pbIntake: ProgressBar
    private lateinit var calendarContainer: LinearLayout
    private lateinit var tvMonth: TextView

    // Meal Card Views
    private lateinit var tvBreakfastDesc: TextView
    private lateinit var tvBreakfastKcal: TextView
    private lateinit var tvLunchDesc: TextView
    private lateinit var tvLunchKcal: TextView
    private lateinit var tvDinnerDesc: TextView
    private lateinit var tvDinnerKcal: TextView
    private lateinit var tvSnacksDesc: TextView
    private lateinit var tvSnacksKcal: TextView

    private val auth = FirebaseAuth.getInstance()
    private val database = FirebaseDatabase.getInstance()
    private val sdf = SimpleDateFormat("yyyy-MM-dd", Locale.getDefault())
    private val todayDate = sdf.format(Date())
    private var selectedDate = todayDate

    private val TARGET_CALORIES = 2000
    private var dateValueListener: ValueEventListener? = null

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?
    ): View? {
        val view = inflater.inflate(R.layout.fragment_nutrition, container, false)

        // Initialize Views
        tvIntakeDisplay = view.findViewById(R.id.tv_intake_display)
        pbIntake = view.findViewById(R.id.pb_intake)
        calendarContainer = view.findViewById(R.id.nutrition_calendar_container)
        tvMonth = view.findViewById(R.id.tv_nutrition_month)

        tvBreakfastDesc = view.findViewById(R.id.tv_breakfast_desc)
        tvBreakfastKcal = view.findViewById(R.id.tv_breakfast_kcal)
        tvLunchDesc = view.findViewById(R.id.tv_lunch_desc)
        tvLunchKcal = view.findViewById(R.id.tv_lunch_kcal)
        tvDinnerDesc = view.findViewById(R.id.tv_dinner_desc)
        tvDinnerKcal = view.findViewById(R.id.tv_dinner_kcal)
        tvSnacksDesc = view.findViewById(R.id.tv_snacks_desc)
        tvSnacksKcal = view.findViewById(R.id.tv_snacks_kcal)

        setupCalendar()
        setupMealClickListeners(view)
        observeDataForDate(selectedDate)
        setupNavigation(view)

        return view
    }

    private fun setupCalendar() {
        calendarContainer.removeAllViews()
        val calendar = Calendar.getInstance()
        val monthYearSdf = SimpleDateFormat("MMMM yyyy", Locale.getDefault())
        tvMonth.text = monthYearSdf.format(calendar.time)

        val currentMonth = calendar.get(Calendar.MONTH)
        calendar.set(Calendar.DAY_OF_MONTH, 1)
        val dayNameSdf = SimpleDateFormat("EEE", Locale.getDefault())

        while (calendar.get(Calendar.MONTH) == currentMonth) {
            val dateStr = sdf.format(calendar.time)
            val dayName = dayNameSdf.format(calendar.time)
            val dayNum = calendar.get(Calendar.DAY_OF_MONTH).toString()

            val dayView = layoutInflater.inflate(R.layout.item_calendar_day, calendarContainer, false)
            val tvName = dayView.findViewById<TextView>(R.id.tv_day_name)
            val tvNumber = dayView.findViewById<TextView>(R.id.tv_day_number)
            val container = dayView.findViewById<LinearLayout>(R.id.ll_day_container)

            tvName.text = dayName
            tvNumber.text = dayNum

            if (dateStr == selectedDate) {
                container.setBackgroundResource(R.drawable.bg_selected_day)
                tvName.setTextColor(ContextCompat.getColor(requireContext(), R.color.black))
                tvNumber.setTextColor(ContextCompat.getColor(requireContext(), R.color.black))
            } else {
                container.background = null
                tvName.setTextColor(ContextCompat.getColor(requireContext(), R.color.text_secondary))
                tvNumber.setTextColor(ContextCompat.getColor(requireContext(), R.color.white))
            }

            dayView.setOnClickListener {
                selectedDate = dateStr
                setupCalendar()
                observeDataForDate(selectedDate)
            }

            calendarContainer.addView(dayView)
            calendar.add(Calendar.DAY_OF_MONTH, 1)
        }
    }

    private fun observeDataForDate(date: String) {
        val uid = auth.currentUser?.uid ?: return
        
        dateValueListener?.let {
            database.reference.child("ActivityLogs").child(uid).child(selectedDate).child("meals").removeEventListener(it)
        }

        dateValueListener = object : ValueEventListener {
            override fun onDataChange(snapshot: DataSnapshot) {
                updateMealsUI(snapshot)
            }

            override fun onCancelled(error: DatabaseError) {
                Log.e("NutritionFragment", "Error: ${error.message}")
            }
        }
        
        database.reference.child("ActivityLogs").child(uid).child(date).child("meals")
            .addValueEventListener(dateValueListener!!)
    }

    private fun updateMealsUI(snapshot: DataSnapshot) {
        var totalKcal = 0
        
        val meals = listOf("breakfast", "lunch", "dinner", "snacks")
        val descs = listOf(tvBreakfastDesc, tvLunchDesc, tvDinnerDesc, tvSnacksDesc)
        val kcals = listOf(tvBreakfastKcal, tvLunchKcal, tvDinnerKcal, tvSnacksKcal)

        for (i in meals.indices) {
            val mealSnap = snapshot.child(meals[i])
            if (mealSnap.exists()) {
                val name = mealSnap.child("name").getValue(String::class.java) ?: "Logged"
                val cal = mealSnap.child("calories").getValue(Int::class.java) ?: 0
                descs[i].text = name
                kcals[i].text = "$cal kcal"
                totalKcal += cal
            } else {
                descs[i].text = if (selectedDate == todayDate) "Tap to log" else "No data"
                kcals[i].text = "0 kcal"
            }
        }

        tvIntakeDisplay.text = "$totalKcal / $TARGET_CALORIES kcal"
        pbIntake.progress = totalKcal
    }

    private fun setupMealClickListeners(view: View) {
        view.findViewById<View>(R.id.card_breakfast).setOnClickListener { 
            if (selectedDate == todayDate) showLogMealDialog("breakfast") 
            else Toast.makeText(context, "History is read-only", Toast.LENGTH_SHORT).show()
        }
        view.findViewById<View>(R.id.card_lunch).setOnClickListener { 
            if (selectedDate == todayDate) showLogMealDialog("lunch")
            else Toast.makeText(context, "History is read-only", Toast.LENGTH_SHORT).show()
        }
        view.findViewById<View>(R.id.card_dinner).setOnClickListener { 
            if (selectedDate == todayDate) showLogMealDialog("dinner")
            else Toast.makeText(context, "History is read-only", Toast.LENGTH_SHORT).show()
        }
        view.findViewById<View>(R.id.card_snacks).setOnClickListener { 
            if (selectedDate == todayDate) showLogMealDialog("snacks")
            else Toast.makeText(context, "History is read-only", Toast.LENGTH_SHORT).show()
        }
    }

    private fun showLogMealDialog(mealType: String) {
        val builder = AlertDialog.Builder(requireContext())
        val dialogView = layoutInflater.inflate(R.layout.dialog_log_meal, null)
        builder.setView(dialogView)

        val etFoodName = dialogView.findViewById<EditText>(R.id.et_food_name)
        val etCalories = dialogView.findViewById<EditText>(R.id.et_food_calories)
        val btnSave = dialogView.findViewById<Button>(R.id.btn_save_meal)

        val dialog = builder.create()

        btnSave.setOnClickListener {
            val name = etFoodName.text.toString().trim()
            val calStr = etCalories.text.toString().trim()

            if (name.isNotEmpty() && calStr.isNotEmpty()) {
                val calories = calStr.toInt()
                saveMealToFirebase(mealType, name, calories)
                dialog.dismiss()
            } else {
                Toast.makeText(context, "Please fill all fields", Toast.LENGTH_SHORT).show()
            }
        }

        dialog.show()
    }

    private fun saveMealToFirebase(type: String, name: String, calories: Int) {
        val uid = auth.currentUser?.uid ?: return
        val mealData = mapOf(
            "name" to name,
            "calories" to calories
        )
        // Safety: even if called, only allow saving to todayDate
        database.reference.child("ActivityLogs").child(uid).child(todayDate).child("meals").child(type)
            .setValue(mealData)
    }

    private fun setupNavigation(view: View) {
        view.findViewById<View>(R.id.nav_home).setOnClickListener {
            parentFragmentManager.beginTransaction().replace(R.id.fragment_container, HomeFragment()).commit()
        }
        view.findViewById<View>(R.id.nav_activity).setOnClickListener {
            parentFragmentManager.beginTransaction().replace(R.id.fragment_container, ActivityFragment()).commit()
        }
        view.findViewById<View>(R.id.nav_profile).setOnClickListener {
            parentFragmentManager.beginTransaction().replace(R.id.fragment_container, ProfileFragment()).commit()
        }
    }
}
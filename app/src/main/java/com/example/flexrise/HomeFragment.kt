package com.example.flexrise

import android.Manifest
import android.content.Context
import android.content.pm.PackageManager
import android.hardware.Sensor
import android.hardware.SensorEvent
import android.hardware.SensorEventListener
import android.hardware.SensorManager
import android.os.Build
import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.LinearLayout
import android.widget.ProgressBar
import android.widget.TextView
import android.widget.Toast
import androidx.activity.result.contract.ActivityResultContracts
import androidx.core.content.ContextCompat
import androidx.fragment.app.Fragment
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.ValueEventListener
import java.text.SimpleDateFormat
import java.util.*

class HomeFragment : Fragment(), SensorEventListener {

    private var sensorManager: SensorManager? = null
    private var stepSensor: Sensor? = null
    private var isSensorPresent = false
    
    private lateinit var tvSteps: TextView
    private lateinit var progressBar: ProgressBar
    private lateinit var tvPercentage: TextView
    private lateinit var tvGoals: TextView
    private lateinit var calendarContainer: LinearLayout
    private lateinit var tvMonth: TextView
    
    private lateinit var tvTargetKcal: TextView
    private lateinit var tvBurnedKcal: TextView
    private lateinit var tvRemainingKcal: TextView
    
    private var savedSteps = 0
    private var baseSensorValue = -1f 
    private var isDataLoaded = false
    
    private val auth = FirebaseAuth.getInstance()
    private val database = FirebaseDatabase.getInstance()
    private val sdf = SimpleDateFormat("yyyy-MM-dd", Locale.getDefault())
    private val todayDate = sdf.format(Date())
    private var selectedDate = todayDate

    private val TARGET_CALORIES = 2000
    private val TARGET_STEPS = 10000

    private val requestPermissionLauncher = registerForActivityResult(
        ActivityResultContracts.RequestPermission()
    ) { isGranted: Boolean ->
        if (isGranted) {
            setupSensor()
        } else {
            Toast.makeText(requireContext(), "Permission denied for Activity Tracking", Toast.LENGTH_SHORT).show()
        }
    }

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        val view = inflater.inflate(R.layout.fragment_home, container, false)

        tvSteps = view.findViewById(R.id.tv_steps_value)
        progressBar = view.findViewById(R.id.progress_bar)
        tvPercentage = view.findViewById(R.id.tv_percentage)
        tvGoals = view.findViewById(R.id.tv_goals_value)
        calendarContainer = view.findViewById(R.id.calendar_container)
        tvMonth = view.findViewById(R.id.tv_month)
        
        tvTargetKcal = view.findViewById(R.id.tv_target_kcal)
        tvBurnedKcal = view.findViewById(R.id.tv_burned_kcal)
        tvRemainingKcal = view.findViewById(R.id.tv_remaining_kcal)

        updateUI(0, 0) // Initialize with 0
        setupCalendar()
        checkPermissionsAndSetup()
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

    private var dateValueListener: ValueEventListener? = null

    private fun observeDataForDate(date: String) {
        val uid = auth.currentUser?.uid ?: return
        
        dateValueListener?.let {
            database.reference.child("ActivityLogs").child(uid).child(selectedDate).removeEventListener(it)
        }

        if (date != todayDate) {
            isDataLoaded = true 
        } else {
            isDataLoaded = false 
        }

        dateValueListener = object : ValueEventListener {
            override fun onDataChange(snapshot: DataSnapshot) {
                val steps = snapshot.child("steps").getValue(Int::class.java) ?: 0
                val burnedCalories = snapshot.child("burned_calories").getValue(Int::class.java) ?: (steps * 0.04).toInt()
                
                if (date == todayDate) {
                    savedSteps = steps
                    isDataLoaded = true
                }
                
                updateUI(steps, burnedCalories)
            }

            override fun onCancelled(error: DatabaseError) {
                updateUI(0, 0)
            }
        }
        
        database.reference.child("ActivityLogs").child(uid).child(date)
            .addValueEventListener(dateValueListener!!)
    }

    private fun checkPermissionsAndSetup() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
            if (ContextCompat.checkSelfPermission(requireContext(), Manifest.permission.ACTIVITY_RECOGNITION) != PackageManager.PERMISSION_GRANTED) {
                requestPermissionLauncher.launch(Manifest.permission.ACTIVITY_RECOGNITION)
            } else {
                setupSensor()
            }
        } else {
            setupSensor()
        }
    }

    private fun setupSensor() {
        sensorManager = activity?.getSystemService(Context.SENSOR_SERVICE) as SensorManager
        stepSensor = sensorManager?.getDefaultSensor(Sensor.TYPE_STEP_COUNTER)
        if (stepSensor != null) {
            isSensorPresent = true
            sensorManager?.registerListener(this, stepSensor, SensorManager.SENSOR_DELAY_UI)
        }
    }

    private fun updateUI(steps: Int, burnedCalories: Int) {
        tvSteps.text = steps.toString()
        
        // Use the actual burned calories passed in
        val remaining = (TARGET_CALORIES - burnedCalories).coerceAtLeast(0)
        
        tvTargetKcal.text = "Target: $TARGET_CALORIES kcal"
        tvBurnedKcal.text = "Burned: $burnedCalories kcal"
        tvRemainingKcal.text = "Remaining: $remaining kcal"

        val percentage = if (TARGET_STEPS > 0) (steps.toFloat() / TARGET_STEPS.toFloat() * 100).toInt() else 0
        progressBar.progress = percentage
        tvPercentage.text = "$percentage%"
        tvGoals.text = "$percentage%"
    }

    override fun onSensorChanged(event: SensorEvent?) {
        if (event?.sensor?.type == Sensor.TYPE_STEP_COUNTER && isDataLoaded && selectedDate == todayDate) {
            val currentSensorValue = event.values[0]
            
            if (baseSensorValue == -1f) {
                baseSensorValue = currentSensorValue - savedSteps
            }
            
            if (currentSensorValue < baseSensorValue) {
                baseSensorValue = currentSensorValue
                savedSteps = 0 
            }

            val todaySteps = (currentSensorValue - baseSensorValue).toInt()
            
            if (todaySteps > savedSteps) {
                savedSteps = todaySteps
                val calories = (todaySteps * 0.04).toInt()
                saveActivityDataToFirebase(todaySteps, calories)
                updateUI(todaySteps, calories)
            }
        }
    }

    private fun saveActivityDataToFirebase(steps: Int, burnedCalories: Int) {
        val uid = auth.currentUser?.uid ?: return
        val data = mapOf(
            "steps" to steps,
            "burned_calories" to burnedCalories
        )
        database.reference.child("ActivityLogs").child(uid).child(todayDate).updateChildren(data)
    }

    override fun onAccuracyChanged(sensor: Sensor?, accuracy: Int) {}

    override fun onResume() {
        super.onResume()
        if (isSensorPresent) {
            sensorManager?.registerListener(this, stepSensor, SensorManager.SENSOR_DELAY_UI)
        }
    }

    override fun onPause() {
        super.onPause()
        sensorManager?.unregisterListener(this)
    }

    private fun setupNavigation(view: View) {
        view.findViewById<View>(R.id.challenge_card).setOnClickListener {
            parentFragmentManager.beginTransaction()
                .replace(R.id.fragment_container, WorkoutFragment())
                .addToBackStack(null)
                .commit()
        }
        view.findViewById<View>(R.id.nav_activity).setOnClickListener {
            parentFragmentManager.beginTransaction().replace(R.id.fragment_container, ActivityFragment()).commit()
        }
        view.findViewById<View>(R.id.nav_nutrition).setOnClickListener {
            parentFragmentManager.beginTransaction().replace(R.id.fragment_container, NutritionFragment()).commit()
        }
        view.findViewById<View>(R.id.nav_profile).setOnClickListener {
            parentFragmentManager.beginTransaction().replace(R.id.fragment_container, ProfileFragment()).commit()
        }
    }
}
package com.example.flexrise.controller

import android.os.Bundle
import android.os.CountDownTimer
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.ImageView
import android.widget.TextView
import androidx.fragment.app.Fragment
import com.example.flexrise.R
import java.util.Locale

class WorkoutFragment : Fragment() {

    private lateinit var tvDurationValue: TextView
    private lateinit var tvWorkoutCalories: TextView
    private lateinit var btnStartWorkout: Button

    private var countDownTimer: CountDownTimer? = null
    private var isWorkoutRunning = false
    private val totalTimeMs: Long = 45 * 60 * 1000 // 45 minutes
    private var timeRemainingMs: Long = totalTimeMs
    private val totalCalories = 350

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        val view = inflater.inflate(R.layout.fragment_workout, container, false)

        tvDurationValue = view.findViewById(R.id.tv_duration_value)
        tvWorkoutCalories = view.findViewById(R.id.tv_workout_calories)
        btnStartWorkout = view.findViewById(R.id.btn_start_workout)

        // Initialize UI with current state
        updateUI(timeRemainingMs)

        // Set up the back button
        val btnBack = view.findViewById<ImageView>(R.id.btn_back)
        btnBack.setOnClickListener {
            parentFragmentManager.popBackStack()
        }

        btnStartWorkout.setOnClickListener {
            if (isWorkoutRunning) {
                stopWorkout()
            } else {
                startWorkout()
            }
        }

        // Bottom Navigation logic
        view.findViewById<View>(R.id.nav_home)?.setOnClickListener {
            parentFragmentManager.beginTransaction().replace(
                R.id.fragment_container,
                HomeFragment()
            ).commit()
        }
        view.findViewById<View>(R.id.nav_activity)?.setOnClickListener {
            parentFragmentManager.beginTransaction().replace(
                R.id.fragment_container,
                ActivityFragment()
            ).commit()
        }
        view.findViewById<View>(R.id.nav_nutrition)?.setOnClickListener {
            parentFragmentManager.beginTransaction().replace(
                R.id.fragment_container,
                NutritionFragment()
            ).commit()
        }
        view.findViewById<View>(R.id.nav_profile)?.setOnClickListener {
            parentFragmentManager.beginTransaction().replace(
                R.id.fragment_container,
                ProfileFragment()
            ).commit()
        }

        return view
    }

    private fun startWorkout() {
        if (timeRemainingMs <= 0) {
            timeRemainingMs = totalTimeMs // Reset if it was finished
        }

        isWorkoutRunning = true
        btnStartWorkout.text = "Stop Workout"

        countDownTimer = object : CountDownTimer(timeRemainingMs, 1000) {
            override fun onTick(millisUntilFinished: Long) {
                timeRemainingMs = millisUntilFinished
                updateUI(timeRemainingMs)
            }

            override fun onFinish() {
                timeRemainingMs = 0
                stopWorkout()
                tvDurationValue.text = "00:00"
                tvWorkoutCalories.text = "$totalCalories kcal"
            }
        }.start()
    }

    private fun stopWorkout() {
        isWorkoutRunning = false
        btnStartWorkout.text = "Start Workout"
        countDownTimer?.cancel()
    }

    private fun updateUI(remainingMs: Long) {
        val secondsRemaining = remainingMs / 1000
        val minutes = secondsRemaining / 60
        val seconds = secondsRemaining % 60

        // Update Duration UI
        tvDurationValue.text = String.Companion.format(Locale.getDefault(), "%02d:%02d", minutes, seconds)

        // Update Calories UI (proportional to time elapsed)
        val timeElapsedMs = totalTimeMs - remainingMs
        val caloriesBurned = (timeElapsedMs.toFloat() / totalTimeMs.toFloat() * totalCalories).toInt()
        tvWorkoutCalories.text = "$caloriesBurned kcal"
    }

    override fun onDestroyView() {
        super.onDestroyView()
        countDownTimer?.cancel()
    }
}
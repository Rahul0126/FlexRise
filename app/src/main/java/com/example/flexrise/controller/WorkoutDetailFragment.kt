package com.example.flexrise.controller

import android.os.Bundle
import android.os.CountDownTimer
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.ProgressBar
import android.widget.TextView
import androidx.core.content.ContextCompat
import androidx.fragment.app.Fragment
import com.example.flexrise.R

class WorkoutDetailFragment : Fragment() {

    private lateinit var progressBar: ProgressBar
    private lateinit var timerText: TextView
    private lateinit var caloriesText: TextView
    private lateinit var startButton: Button

    private val totalWorkoutTime = 600000L // 10 minutes
    private val totalCalories = 80

    private var timer: CountDownTimer? = null
    private var timeLeft = totalWorkoutTime

    private var isWorkoutRunning = false

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {

        val view = inflater.inflate(R.layout.fragment_workout_detail, container, false)

        progressBar = view.findViewById(R.id.progress_circle)
        timerText = view.findViewById(R.id.tv_timer)
        caloriesText = view.findViewById(R.id.tv_calories)
        startButton = view.findViewById(R.id.btn_start_workout)

        updateTimerText()

        startButton.setOnClickListener {

            if (!isWorkoutRunning) {

                startWorkout()

                startButton.text = "Stop Workout"
                startButton.setBackgroundColor(
                    ContextCompat.getColor(requireContext(), android.R.color.holo_red_dark)
                )

                isWorkoutRunning = true

            } else {

                stopWorkout()

            }
        }

        return view
    }

    private fun startWorkout() {

        timer = object : CountDownTimer(timeLeft, 1000) {

            override fun onTick(millisUntilFinished: Long) {

                timeLeft = millisUntilFinished

                val elapsedTime = totalWorkoutTime - millisUntilFinished

                // Progress calculation
                val progress = (elapsedTime * 100 / totalWorkoutTime).toInt()
                progressBar.progress = progress

                // Calories calculation
                val caloriesBurned = (elapsedTime * totalCalories / totalWorkoutTime).toInt()
                caloriesText.text = "$caloriesBurned kcal"

                updateTimerText()
            }

            override fun onFinish() {

                progressBar.progress = 100
                timerText.text = "00:00"
                caloriesText.text = "$totalCalories kcal"

                startButton.text = "Start Workout"
                startButton.setBackgroundColor(
                    ContextCompat.getColor(requireContext(), R.color.accent_lime)
                )

                isWorkoutRunning = false
                timeLeft = totalWorkoutTime
            }

        }.start()
    }

    private fun stopWorkout() {

        timer?.cancel()

        progressBar.progress = 0
        timeLeft = totalWorkoutTime

        updateTimerText()
        caloriesText.text = "0 kcal"

        startButton.text = "Start Workout"
        startButton.setBackgroundColor(
            ContextCompat.getColor(requireContext(), R.color.accent_lime)
        )

        isWorkoutRunning = false
    }

    private fun updateTimerText() {

        val minutes = (timeLeft / 1000) / 60
        val seconds = (timeLeft / 1000) % 60

        val timeFormatted = String.format("%02d:%02d", minutes, seconds)
        timerText.text = timeFormatted
    }
}
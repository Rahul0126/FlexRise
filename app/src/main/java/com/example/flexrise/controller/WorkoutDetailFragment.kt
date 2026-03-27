package com.example.flexrise.controller

import android.os.Bundle
import android.os.CountDownTimer
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.ImageView
import android.widget.ProgressBar
import android.widget.TextView
import androidx.fragment.app.Fragment
import com.example.flexrise.R

class WorkoutDetailFragment : Fragment() {

    private lateinit var progressBar: ProgressBar
    private lateinit var timerText: TextView
    private lateinit var caloriesText: TextView

    private lateinit var backButton: ImageView
    private lateinit var startButton: Button
    private lateinit var pauseResumeButton: Button
    private lateinit var stopButton: Button

    private val totalWorkoutTime = 600000L
    private val totalCalories = 80

    private var timeLeft = totalWorkoutTime
    private var timer: CountDownTimer? = null
    private var isRunning = false

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {

        val view = inflater.inflate(R.layout.fragment_workout_detail, container, false)

        progressBar = view.findViewById(R.id.progress_circle)
        timerText = view.findViewById(R.id.tv_timer)
        caloriesText = view.findViewById(R.id.tv_calories)

        backButton = view.findViewById(R.id.btn_back)
        startButton = view.findViewById(R.id.btn_start)
        pauseResumeButton = view.findViewById(R.id.btn_pause_resume)
        stopButton = view.findViewById(R.id.btn_stop)

        updateTimerText()

        // BACK BUTTON FUNCTION
        backButton.setOnClickListener {
            parentFragmentManager.popBackStack()
        }

        startButton.setOnClickListener {
            startWorkout()

            startButton.visibility = View.GONE
            pauseResumeButton.visibility = View.VISIBLE
            stopButton.visibility = View.VISIBLE
        }

        pauseResumeButton.setOnClickListener {

            if (isRunning) {
                pauseWorkout()
                pauseResumeButton.text = "Resume"
            } else {
                resumeWorkout()
                pauseResumeButton.text = "Pause"
            }
        }

        stopButton.setOnClickListener {
            stopWorkout()
        }

        return view
    }

    private fun startWorkout() {

        timer = object : CountDownTimer(timeLeft, 1000) {

            override fun onTick(millisUntilFinished: Long) {

                timeLeft = millisUntilFinished

                val elapsed = totalWorkoutTime - millisUntilFinished

                val progress = (elapsed * 100 / totalWorkoutTime).toInt()
                progressBar.progress = progress

                val calories = (elapsed * totalCalories / totalWorkoutTime).toInt()
                caloriesText.text = "$calories kcal"

                updateTimerText()
            }

            override fun onFinish() {

                progressBar.progress = 100
                timerText.text = "00:00"
                caloriesText.text = "$totalCalories kcal"
            }

        }.start()

        isRunning = true
    }

    private fun pauseWorkout() {
        timer?.cancel()
        isRunning = false
    }

    private fun resumeWorkout() {
        startWorkout()
    }

    private fun stopWorkout() {

        timer?.cancel()

        timeLeft = totalWorkoutTime
        progressBar.progress = 0
        caloriesText.text = "0 kcal"

        updateTimerText()

        startButton.visibility = View.VISIBLE
        pauseResumeButton.visibility = View.GONE
        stopButton.visibility = View.GONE

        pauseResumeButton.text = "Pause"

        isRunning = false
    }

    private fun updateTimerText() {

        val minutes = (timeLeft / 1000) / 60
        val seconds = (timeLeft / 1000) % 60

        timerText.text = String.format("%02d:%02d", minutes, seconds)
    }
}
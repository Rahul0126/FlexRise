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
    private lateinit var btnPauseResume: Button
    private lateinit var btnStop: Button

    private var countDownTimer: CountDownTimer? = null
    private var isRunning = false

    private val totalTimeMs: Long = 45 * 60 * 1000
    private var timeRemainingMs: Long = totalTimeMs
    private val totalCalories = 350

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {

        val view = inflater.inflate(R.layout.fragment_workout, container, false)

        tvDurationValue = view.findViewById(R.id.tv_duration_value)
        tvWorkoutCalories = view.findViewById(R.id.tv_workout_calories)

        btnStartWorkout = view.findViewById(R.id.btn_start_workout)
        btnPauseResume = view.findViewById(R.id.btn_pause_resume)
        btnStop = view.findViewById(R.id.btn_stop)

        updateUI(timeRemainingMs)

        val btnBack = view.findViewById<ImageView>(R.id.btn_back)
        btnBack.setOnClickListener {
            parentFragmentManager.popBackStack()
        }

        btnStartWorkout.setOnClickListener {

            startWorkout()

            btnStartWorkout.visibility = View.GONE
            btnPauseResume.visibility = View.VISIBLE
            btnStop.visibility = View.VISIBLE
        }

        btnPauseResume.setOnClickListener {

            if (isRunning) {
                pauseWorkout()
                btnPauseResume.text = "Resume"
            } else {
                resumeWorkout()
                btnPauseResume.text = "Pause"
            }
        }

        btnStop.setOnClickListener {
            stopWorkout()
        }

        return view
    }

    private fun startWorkout() {

        countDownTimer = object : CountDownTimer(timeRemainingMs, 1000) {

            override fun onTick(millisUntilFinished: Long) {

                timeRemainingMs = millisUntilFinished
                updateUI(timeRemainingMs)
            }

            override fun onFinish() {

                timeRemainingMs = 0
                updateUI(timeRemainingMs)
                stopWorkout()
            }

        }.start()

        isRunning = true
    }

    private fun pauseWorkout() {
        countDownTimer?.cancel()
        isRunning = false
    }

    private fun resumeWorkout() {
        startWorkout()
    }

    private fun stopWorkout() {

        countDownTimer?.cancel()

        timeRemainingMs = totalTimeMs
        updateUI(timeRemainingMs)

        btnStartWorkout.visibility = View.VISIBLE
        btnPauseResume.visibility = View.GONE
        btnStop.visibility = View.GONE

        btnPauseResume.text = "Pause"

        isRunning = false
    }

    private fun updateUI(remainingMs: Long) {

        val secondsRemaining = remainingMs / 1000
        val minutes = secondsRemaining / 60
        val seconds = secondsRemaining % 60

        tvDurationValue.text =
            String.format(Locale.getDefault(), "%02d:%02d", minutes, seconds)

        val timeElapsedMs = totalTimeMs - remainingMs
        val caloriesBurned =
            (timeElapsedMs.toFloat() / totalTimeMs.toFloat() * totalCalories).toInt()

        tvWorkoutCalories.text = "$caloriesBurned kcal"
    }

    override fun onDestroyView() {
        super.onDestroyView()
        countDownTimer?.cancel()
    }
}
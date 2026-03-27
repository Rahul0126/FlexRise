package com.example.flexrise.controller

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.example.flexrise.R
import com.example.flexrise.model.Workout

class WorkoutAdapter(
    private val workouts: List<Workout>,
    private val onClick: (Workout) -> Unit
) : RecyclerView.Adapter<WorkoutAdapter.WorkoutViewHolder>() {

    class WorkoutViewHolder(view: View) : RecyclerView.ViewHolder(view) {
        val name: TextView = view.findViewById(R.id.tv_workout_name)
        val duration: TextView = view.findViewById(R.id.tv_workout_duration)
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): WorkoutViewHolder {

        val view = LayoutInflater.from(parent.context)
            .inflate(R.layout.item_workout, parent, false)

        return WorkoutViewHolder(view)
    }

    override fun onBindViewHolder(holder: WorkoutViewHolder, position: Int) {

        val workout = workouts[position]

        holder.name.text = workout.name
        holder.duration.text = "${workout.duration} min"

        holder.itemView.setOnClickListener {
            onClick(workout)
        }

    }

    override fun getItemCount(): Int = workouts.size
}
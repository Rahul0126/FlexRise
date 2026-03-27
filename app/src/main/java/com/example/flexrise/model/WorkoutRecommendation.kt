package com.example.flexrise.model

object WorkoutRecommendation {

    fun getDailyWorkouts(): List<Workout> {

        return listOf(
            Workout("Pushups", 10, 80, "Push your limits today!"),
            Workout("Squats", 12, 95, "Build powerful legs"),
            Workout("Jump Rope", 8, 70, "Boost your cardio"),
            Workout("Plank", 6, 50, "Strengthen your core")
        )

    }

}
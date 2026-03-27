package com.example.flexrise.controller

import android.os.Bundle
import android.os.Handler
import android.os.Looper
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import com.example.flexrise.R
import com.example.flexrise.controller.SignupFragment
import com.google.firebase.auth.FirebaseAuth

class SplashFragment : Fragment() {

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        return inflater.inflate(R.layout.fragment_splash, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        // 2 second delay then check login status
        Handler(Looper.getMainLooper()).postDelayed({
            if (isAdded) {
                val user = FirebaseAuth.getInstance().currentUser
                val destinationFragment = if (user != null) {
                    HomeFragment()
                } else {
                    SignupFragment()
                }

                parentFragmentManager.beginTransaction()
                    .replace(R.id.fragment_container, destinationFragment)
                    .commit()
            }
        }, 2000)
    }
}
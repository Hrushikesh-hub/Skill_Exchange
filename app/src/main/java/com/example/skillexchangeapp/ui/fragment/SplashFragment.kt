package com.example.skillexchangeapp.ui.fragment

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.lifecycle.lifecycleScope
import androidx.navigation.fragment.findNavController
import com.example.skillexchangeapp.R
import com.example.skillexchangeapp.SkillExchangeApplication
import com.example.skillexchangeapp.data.firebase.FirebaseSyncManager
import com.example.skillexchangeapp.utils.DemoDataSeeder
import com.example.skillexchangeapp.utils.SessionManager
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch

class SplashFragment : Fragment() {

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        return inflater.inflate(R.layout.fragment_splash, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        val app = requireActivity().application as SkillExchangeApplication
        val sessionManager = SessionManager(requireContext())

        lifecycleScope.launch {
            // Seed demo data on first launch
            DemoDataSeeder(app.repository, requireContext()).seedIfNeeded()

            // Start real-time Firestore → Room sync (two-device sync)
            FirebaseSyncManager.startSync(app.database)

            delay(2000)

            if (sessionManager.isLoggedIn()) {
                findNavController().navigate(R.id.action_splash_to_dashboard)
            } else {
                findNavController().navigate(R.id.action_splash_to_login)
            }
        }
    }
}


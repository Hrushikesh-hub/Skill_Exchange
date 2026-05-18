package com.example.skillexchangeapp.ui.fragment

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.lifecycleScope
import androidx.lifecycle.repeatOnLifecycle
import androidx.navigation.fragment.findNavController
import androidx.recyclerview.widget.LinearLayoutManager
import com.example.skillexchangeapp.R
import com.example.skillexchangeapp.SkillExchangeApplication
import com.example.skillexchangeapp.data.local.entity.Notification
import com.example.skillexchangeapp.databinding.FragmentNotificationsBinding
import com.example.skillexchangeapp.ui.adapter.NotificationAdapter
import com.example.skillexchangeapp.ui.viewmodel.MainViewModel
import com.example.skillexchangeapp.ui.viewmodel.ViewModelFactory
import com.example.skillexchangeapp.utils.SessionManager
import kotlinx.coroutines.launch

class NotificationFragment : Fragment() {

    private var _binding: FragmentNotificationsBinding? = null
    private val binding get() = _binding!!

    private val viewModel: MainViewModel by viewModels {
        ViewModelFactory((requireActivity().application as SkillExchangeApplication).repository)
    }

    private lateinit var adapter: NotificationAdapter

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View {
        _binding = FragmentNotificationsBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        binding.toolbar.setNavigationOnClickListener {
            requireActivity().onBackPressedDispatcher.onBackPressed()
        }

        val userId = SessionManager(requireContext()).getUserId()

        adapter = NotificationAdapter { notif -> handleTap(notif) }
        binding.rvNotifications.layoutManager = LinearLayoutManager(context)
        binding.rvNotifications.adapter = adapter

        binding.btnMarkAllRead.setOnClickListener {
            viewModel.markAllNotificationsRead(userId)
        }

        viewLifecycleOwner.lifecycleScope.launch {
            viewLifecycleOwner.repeatOnLifecycle(Lifecycle.State.STARTED) {
                viewModel.getNotifications(userId).collect { notifications ->
                    adapter.submitList(notifications)
                    if (notifications.isEmpty()) {
                        binding.emptyState.visibility = View.VISIBLE
                        binding.rvNotifications.visibility = View.GONE
                    } else {
                        binding.emptyState.visibility = View.GONE
                        binding.rvNotifications.visibility = View.VISIBLE
                    }
                }
            }
        }
    }

    private fun handleTap(notif: Notification) {
        viewModel.markNotificationRead(notif.id)
        when (notif.type) {
            "OFFER_RECEIVED" ->
                findNavController().navigate(R.id.action_dashboard_to_offerManagement)
            "OFFER_ACCEPTED", "OFFER_REJECTED",
            "SWAP_SCHEDULED", "SWAP_COMPLETED", "SWAP_CANCELLED" ->
                findNavController().navigate(R.id.action_dashboard_to_swapManagement)
            "REVIEW_RECEIVED" ->
                findNavController().navigate(R.id.action_dashboard_to_profile)
            else -> {} // stay
        }
    }

    override fun onDestroyView() { super.onDestroyView(); _binding = null }
}

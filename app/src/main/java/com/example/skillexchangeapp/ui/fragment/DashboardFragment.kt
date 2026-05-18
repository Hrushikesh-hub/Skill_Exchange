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
import com.example.skillexchangeapp.data.local.entity.User
import com.example.skillexchangeapp.databinding.FragmentDashboardBinding
import com.example.skillexchangeapp.ui.adapter.BarterSuggestionAdapter
import com.example.skillexchangeapp.ui.adapter.NeedAdapter
import com.example.skillexchangeapp.ui.viewmodel.MainViewModel
import com.example.skillexchangeapp.ui.viewmodel.ViewModelFactory
import com.example.skillexchangeapp.utils.SessionManager
import kotlinx.coroutines.launch

class DashboardFragment : Fragment() {

    private var _binding: FragmentDashboardBinding? = null
    private val binding get() = _binding!!

    private val viewModel: MainViewModel by viewModels {
        ViewModelFactory((requireActivity().application as SkillExchangeApplication).repository)
    }

    private lateinit var adapter: NeedAdapter
    private lateinit var barterAdapter: BarterSuggestionAdapter
    private var currentUser: User? = null

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View {
        _binding = FragmentDashboardBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        val userId = SessionManager(requireContext()).getUserId()

        // ── Adapters ──
        adapter = NeedAdapter(
            onItemClick = { need ->
                val bundle = Bundle().apply { putLong("postId", need.id) }
                findNavController().navigate(R.id.action_dashboard_to_offer, bundle)
            },
            currentUser = null
        )
        binding.rvRecentNeeds.layoutManager = LinearLayoutManager(context)
        binding.rvRecentNeeds.adapter = adapter

        barterAdapter = BarterSuggestionAdapter { suggestion ->
            val bundle = Bundle().apply { putLong("postId", suggestion.theyNeed.id) }
            findNavController().navigate(R.id.action_dashboard_to_offer, bundle)
        }
        binding.rvBarterSuggestions.layoutManager =
            LinearLayoutManager(context, LinearLayoutManager.HORIZONTAL, false)
        binding.rvBarterSuggestions.adapter = barterAdapter

        // ── User Stats ──
        viewLifecycleOwner.lifecycleScope.launch {
            viewLifecycleOwner.repeatOnLifecycle(Lifecycle.State.STARTED) {
                viewModel.getUser(userId).collect { user ->
                    user?.let {
                        currentUser = it
                        adapter.updateCurrentUser(it)
                        binding.tvWelcomeName.text = getString(R.string.hello_user, it.fullName.split(" ")[0])
                        binding.tvSkillPoints.text = "${it.skillPoints} SP"
                        binding.tvTrustScore.text = String.format("%.1f\u2605", it.trustScore)
                        binding.tvCompletedSwaps.text = "${it.completedSwaps}"
                    }
                }
            }
        }

        // ── Active Swaps Count ──
        viewLifecycleOwner.lifecycleScope.launch {
            viewLifecycleOwner.repeatOnLifecycle(Lifecycle.State.STARTED) {
                viewModel.getSwaps(userId).collect { swaps ->
                    val active = swaps.count { it.status in listOf("Ongoing", "Scheduled") }
                    binding.tvActiveSwaps.text = "$active"
                }
            }
        }

        // ── Pending Offers Banner (amber card) ──
        viewLifecycleOwner.lifecycleScope.launch {
            viewLifecycleOwner.repeatOnLifecycle(Lifecycle.State.STARTED) {
                viewModel.getPendingOfferCountForUser(userId).collect { count ->
                    if (count > 0) {
                        binding.cardPendingOffers.visibility = View.VISIBLE
                        binding.tvPendingCount.text =
                            "You have $count pending offer${if (count != 1) "s" else ""} waiting →"
                    } else {
                        binding.cardPendingOffers.visibility = View.GONE
                    }
                }
            }
        }

        // ── Notification Badge ──
        viewLifecycleOwner.lifecycleScope.launch {
            viewLifecycleOwner.repeatOnLifecycle(Lifecycle.State.STARTED) {
                viewModel.getUnreadNotificationCount(userId).collect { count ->
                    if (count > 0) {
                        binding.tvNotifBadge.visibility = View.VISIBLE
                        binding.tvNotifBadge.text = if (count > 9) "9+" else "$count"
                    } else {
                        binding.tvNotifBadge.visibility = View.GONE
                    }
                }
            }
        }

        // ── Community impact ──
        viewModel.getImpactSnapshot { impact ->
            activity?.runOnUiThread {
                binding.tvCommunityHours.text = "${impact.totalHours}h"
                binding.tvMoneySaved.text = "Rs ${impact.moneySavedEstimate}"
                binding.tvTopSkill.text = impact.topSkill
            }
        }

        // ── AI Barter Suggestions ──
        viewModel.getBarterSuggestions(userId) { suggestions ->
            activity?.runOnUiThread {
                if (suggestions.isNotEmpty()) {
                    binding.sectionBarterSuggestions.visibility = View.VISIBLE
                    binding.barterEmptyState.visibility = View.GONE
                    barterAdapter.submitList(suggestions)
                } else {
                    binding.sectionBarterSuggestions.visibility = View.VISIBLE
                    binding.barterEmptyState.visibility = View.VISIBLE
                }
            }
        }

        // ── Recent Needs (AI ranked) ──
        viewLifecycleOwner.lifecycleScope.launch {
            viewLifecycleOwner.repeatOnLifecycle(Lifecycle.State.STARTED) {
                viewModel.allOpenNeeds.collect { needs ->
                    viewModel.getRankedNeedsForUser(userId) { recommendations ->
                        activity?.runOnUiThread {
                            val ranked = recommendations.map { it.need }.take(5)
                            adapter.submitList(if (ranked.isNotEmpty()) ranked else needs.take(5))
                        }
                    }
                }
            }
        }

        // ── Navigation ──
        binding.btnPostNeed.setOnClickListener { findNavController().navigate(R.id.action_dashboard_to_postNeed) }
        binding.btnViewFeed.setOnClickListener { findNavController().navigate(R.id.action_dashboard_to_needFeed) }
        binding.btnSwapManagement.setOnClickListener { findNavController().navigate(R.id.action_dashboard_to_swapManagement) }
        binding.btnViewOffers.setOnClickListener { findNavController().navigate(R.id.action_dashboard_to_offerManagement) }
        binding.btnWallet.setOnClickListener { findNavController().navigate(R.id.action_dashboard_to_wallet) }
        binding.btnImpact.setOnClickListener { findNavController().navigate(R.id.action_dashboard_to_impact) }
        binding.btnAdmin.setOnClickListener { findNavController().navigate(R.id.action_dashboard_to_admin) }
        binding.cardImpactSummary.setOnClickListener { findNavController().navigate(R.id.action_dashboard_to_impact) }
        binding.cardPendingOffers.setOnClickListener { findNavController().navigate(R.id.action_dashboard_to_offerManagement) }
        binding.tvSeeAll.setOnClickListener { findNavController().navigate(R.id.action_dashboard_to_needFeed) }
        binding.fabProfile.setOnClickListener { findNavController().navigate(R.id.action_dashboard_to_profile) }
        binding.btnNotifications.setOnClickListener { findNavController().navigate(R.id.action_dashboard_to_notifications) }
    }

    override fun onDestroyView() { super.onDestroyView(); _binding = null }
}

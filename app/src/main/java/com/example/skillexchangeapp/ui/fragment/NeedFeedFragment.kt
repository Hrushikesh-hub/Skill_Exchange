package com.example.skillexchangeapp.ui.fragment

import android.os.Bundle
import android.text.Editable
import android.text.TextWatcher
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.lifecycle.lifecycleScope
import androidx.navigation.fragment.findNavController
import com.example.skillexchangeapp.R
import com.example.skillexchangeapp.SkillExchangeApplication
import com.example.skillexchangeapp.data.local.entity.NeedPost
import com.example.skillexchangeapp.data.local.entity.User
import com.example.skillexchangeapp.databinding.FragmentNeedFeedBinding
import com.example.skillexchangeapp.ui.adapter.NeedAdapter
import com.example.skillexchangeapp.ui.viewmodel.MainViewModel
import com.example.skillexchangeapp.ui.viewmodel.ViewModelFactory
import com.example.skillexchangeapp.utils.SessionManager
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.launch

class NeedFeedFragment : Fragment() {

    private var _binding: FragmentNeedFeedBinding? = null
    private val binding get() = _binding!!

    private val viewModel: MainViewModel by viewModels {
        ViewModelFactory((requireActivity().application as SkillExchangeApplication).repository)
    }

    private lateinit var adapter: NeedAdapter
    private var currentJob: Job? = null
    private var debounceJob: Job? = null
    private var currentFilter: String? = null
    private var currentUser: User? = null

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View {
        _binding = FragmentNeedFeedBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        binding.toolbar.setNavigationOnClickListener {
            requireActivity().onBackPressedDispatcher.onBackPressed()
        }

        val userId = SessionManager(requireContext()).getUserId()

        adapter = NeedAdapter(
            onItemClick = { need ->
                val bundle = Bundle().apply { putLong("postId", need.id) }
                findNavController().navigate(R.id.action_needFeed_to_offer, bundle)
            },
            currentUser = null
        )
        binding.rvNeeds.adapter = adapter

        // Load current user for match scores
        lifecycleScope.launch {
            val user = (requireActivity().application as SkillExchangeApplication)
                .repository.getUserByIdSync(userId)
            currentUser = user
            adapter.updateCurrentUser(user)
            loadNeeds(null, null) // re-render with user
        }

        loadNeeds(null, null)

        // ── Search with 300ms debounce ──
        binding.etSearch.addTextChangedListener(object : TextWatcher {
            override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {}
            override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {}
            override fun afterTextChanged(s: Editable?) {
                debounceJob?.cancel()
                debounceJob = lifecycleScope.launch {
                    delay(300)
                    val query = s?.toString()?.trim()
                    if (!query.isNullOrEmpty()) {
                        loadNeeds(query, null)
                    } else {
                        loadNeeds(null, currentFilter)
                    }
                }
            }
        })

        // ── Filter Chips ──
        binding.chipGroup.setOnCheckedStateChangeListener { _, checkedIds ->
            binding.etSearch.text?.clear()
            when {
                checkedIds.contains(R.id.chipAll) || checkedIds.isEmpty() -> {
                    currentFilter = null; loadNeeds(null, null)
                }
                checkedIds.contains(R.id.chipPlumbing) -> { currentFilter = "Plumbing"; loadNeeds(null, "Plumbing") }
                checkedIds.contains(R.id.chipCarpentry) -> { currentFilter = "Carpentry"; loadNeeds(null, "Carpentry") }
                checkedIds.contains(R.id.chipElectrical) -> { currentFilter = "Electrical"; loadNeeds(null, "Electrical") }
                checkedIds.contains(R.id.chipMasonry) -> { currentFilter = "Masonry"; loadNeeds(null, "Masonry") }
                checkedIds.contains(R.id.chipPainting) -> { currentFilter = "Painting"; loadNeeds(null, "Painting") }
                checkedIds.contains(R.id.chipWelding) -> { currentFilter = "Welding"; loadNeeds(null, "Welding") }
                checkedIds.contains(R.id.chipMechanic) -> { currentFilter = "Mechanic"; loadNeeds(null, "Mechanic") }
            }
        }
    }

    private fun loadNeeds(searchQuery: String?, skillFilter: String?) {
        currentJob?.cancel()
        currentJob = lifecycleScope.launch {
            val flow = when {
                !searchQuery.isNullOrBlank() -> viewModel.searchNeeds(searchQuery)
                !skillFilter.isNullOrBlank() -> viewModel.filterNeedsBySkill(skillFilter)
                else -> viewModel.allOpenNeeds
            }
            flow.collectLatest { needs ->
                // Sort by AI match score if user available
                val sorted = sortByScore(needs)
                adapter.submitList(sorted)
                val empty = sorted.isEmpty()
                binding.emptyState.visibility = if (empty) View.VISIBLE else View.GONE
                binding.rvNeeds.visibility = if (empty) View.GONE else View.VISIBLE
            }
        }
    }

    private fun sortByScore(needs: List<NeedPost>): List<NeedPost> {
        val user = currentUser ?: return needs
        return needs.sortedByDescending { need ->
            com.example.skillexchangeapp.ai.AiMatchEngine.rankNeedsForUser(user, listOf(need))
                .firstOrNull()?.score ?: 0
        }
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}

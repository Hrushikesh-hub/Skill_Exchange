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
import androidx.recyclerview.widget.LinearLayoutManager
import com.example.skillexchangeapp.SkillExchangeApplication
import com.example.skillexchangeapp.data.local.entity.SkillPointTransaction
import com.example.skillexchangeapp.databinding.FragmentWalletBinding
import com.example.skillexchangeapp.ui.adapter.TransactionAdapter
import com.example.skillexchangeapp.ui.viewmodel.MainViewModel
import com.example.skillexchangeapp.ui.viewmodel.ViewModelFactory
import com.example.skillexchangeapp.utils.SessionManager
import kotlinx.coroutines.launch

class WalletFragment : Fragment() {

    private var _binding: FragmentWalletBinding? = null
    private val binding get() = _binding!!

    private val viewModel: MainViewModel by viewModels {
        ViewModelFactory((requireActivity().application as SkillExchangeApplication).repository)
    }

    private lateinit var adapter: TransactionAdapter
    private var allTransactions: List<SkillPointTransaction> = emptyList()
    private var userId: Long = -1L

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View {
        _binding = FragmentWalletBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        binding.toolbar.setNavigationOnClickListener { requireActivity().onBackPressedDispatcher.onBackPressed() }

        userId = SessionManager(requireContext()).getUserId()

        adapter = TransactionAdapter()
        binding.rvTransactions.layoutManager = LinearLayoutManager(context)
        binding.rvTransactions.adapter = adapter

        // Filter chips
        binding.chipAll.setOnCheckedChangeListener { _, checked -> if (checked) renderTransactions("all") }
        binding.chipEarned.setOnCheckedChangeListener { _, checked -> if (checked) renderTransactions("earned") }
        binding.chipSpent.setOnCheckedChangeListener { _, checked -> if (checked) renderTransactions("spent") }

        viewLifecycleOwner.lifecycleScope.launch {
            viewLifecycleOwner.repeatOnLifecycle(Lifecycle.State.STARTED) {
                viewModel.walletBalance.collect { balance ->
                    binding.tvWalletBalance.text = "$balance SP"
                }
            }
        }

        viewLifecycleOwner.lifecycleScope.launch {
            viewLifecycleOwner.repeatOnLifecycle(Lifecycle.State.STARTED) {
                viewModel.earnedTotal.collect { earned ->
                    binding.tvEarnedTotal.text = "+$earned SP"
                }
            }
        }

        viewLifecycleOwner.lifecycleScope.launch {
            viewLifecycleOwner.repeatOnLifecycle(Lifecycle.State.STARTED) {
                viewModel.spentTotal.collect { spent ->
                    binding.tvSpentTotal.text = "-$spent SP"
                }
            }
        }

        viewLifecycleOwner.lifecycleScope.launch {
            viewLifecycleOwner.repeatOnLifecycle(Lifecycle.State.STARTED) {
                viewModel.transactionHistory.collect { txs ->
                    allTransactions = txs
                    renderTransactions(currentFilter())
                    if (txs.isEmpty()) {
                        binding.emptyState.visibility = View.VISIBLE
                        binding.rvTransactions.visibility = View.GONE
                    } else {
                        binding.emptyState.visibility = View.GONE
                        binding.rvTransactions.visibility = View.VISIBLE
                    }
                }
            }
        }
    }

    override fun onResume() {
        super.onResume()
        viewModel.loadWalletData(userId)
    }

    private fun currentFilter(): String = when {
        binding.chipEarned.isChecked -> "earned"
        binding.chipSpent.isChecked -> "spent"
        else -> "all"
    }

    private fun renderTransactions(filter: String) {
        val filtered = when (filter) {
            "earned" -> allTransactions.filter { it.amount > 0 }
            "spent" -> allTransactions.filter { it.amount < 0 }
            else -> allTransactions
        }
        adapter.submitList(filtered)
    }

    override fun onDestroyView() { super.onDestroyView(); _binding = null }
}

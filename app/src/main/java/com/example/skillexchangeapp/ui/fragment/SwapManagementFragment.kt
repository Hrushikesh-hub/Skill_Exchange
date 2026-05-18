package com.example.skillexchangeapp.ui.fragment

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.EditText
import android.widget.RatingBar
import android.widget.TextView
import android.widget.Toast
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.lifecycleScope
import androidx.lifecycle.repeatOnLifecycle
import androidx.navigation.fragment.findNavController
import androidx.recyclerview.widget.LinearLayoutManager
import com.example.skillexchangeapp.R
import com.example.skillexchangeapp.SkillExchangeApplication
import com.example.skillexchangeapp.data.local.entity.Review
import com.example.skillexchangeapp.data.local.entity.Swap
import com.example.skillexchangeapp.databinding.FragmentSwapManagementBinding
import com.example.skillexchangeapp.ui.adapter.SwapCardItem
import com.example.skillexchangeapp.ui.adapter.SwapManagementAdapter
import com.example.skillexchangeapp.ui.viewmodel.MainViewModel
import com.example.skillexchangeapp.ui.viewmodel.ViewModelFactory
import com.example.skillexchangeapp.utils.SessionManager
import com.google.android.material.bottomsheet.BottomSheetDialog
import com.google.android.material.button.MaterialButton
import com.google.android.material.dialog.MaterialAlertDialogBuilder
import com.google.android.material.snackbar.Snackbar
import com.google.android.material.textfield.TextInputEditText
import kotlinx.coroutines.launch

class SwapManagementFragment : Fragment() {

    private var _binding: FragmentSwapManagementBinding? = null
    private val binding get() = _binding!!

    private val viewModel: MainViewModel by viewModels {
        ViewModelFactory((requireActivity().application as SkillExchangeApplication).repository)
    }

    private lateinit var adapter: SwapManagementAdapter
    private var currentUserId: Long = -1L

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View {
        _binding = FragmentSwapManagementBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        binding.toolbar.setNavigationOnClickListener {
            requireActivity().onBackPressedDispatcher.onBackPressed()
        }

        currentUserId = SessionManager(requireContext()).getUserId()

        adapter = SwapManagementAdapter(
            onStartClick = { swap -> viewModel.startSwap(swap) },
            onCancelClick = { swap -> showCancelDialog(swap) },
            onProofClick = { swap -> showProofDialog(swap) },
            onCompleteClick = { swap -> showReviewDialog(swap) },
            onChatClick = { swap, partnerName ->
                val partnerId = if (swap.userAId == currentUserId) swap.userBId else swap.userAId
                val bundle = Bundle().apply {
                    putLong("partnerId", partnerId)
                    putString("partnerName", partnerName)
                }
                findNavController().navigate(R.id.action_swapManagement_to_chat, bundle)
            }
        )

        binding.rvSwaps.layoutManager = LinearLayoutManager(context)
        binding.rvSwaps.adapter = adapter

        viewLifecycleOwner.lifecycleScope.launch {
            viewLifecycleOwner.repeatOnLifecycle(Lifecycle.State.STARTED) {
                viewModel.getSwaps(currentUserId).collect { swaps ->
                    val sorted = swaps.sortedWith(compareBy {
                        when (it.status) {
                            "Ongoing" -> 0
                            "Proof Submitted" -> 1
                            "Scheduled" -> 2
                            "Completed" -> 3
                            else -> 4
                        }
                    })
                    val items = sorted.map { swap ->
                        val partnerId = if (swap.userAId == currentUserId) swap.userBId else swap.userAId
                        SwapCardItem(
                            swap = swap,
                            partnerName = "Loading...",
                            needTitle = "Loading...",
                            currentUserId = currentUserId
                        )
                    }
                    adapter.submitList(items)

                    // Resolve partner names and need titles async
                    sorted.forEachIndexed { idx, swap ->
                        viewModel.getSwapPartnerName(swap, currentUserId) { name ->
                            viewModel.getNeedForSwap(swap) { need ->
                                activity?.runOnUiThread {
                                    val current = adapter.currentList.toMutableList()
                                    if (idx < current.size) {
                                        current[idx] = current[idx].copy(
                                            partnerName = name,
                                            needTitle = need?.title ?: "Swap #${swap.id}"
                                        )
                                        adapter.submitList(current.toList())
                                    }
                                }
                            }
                        }
                    }

                    val activeCount = swaps.count { it.status in listOf("Ongoing", "Scheduled", "Proof Submitted") }
                    binding.tvSwapCount.text = "$activeCount active swap${if (activeCount != 1) "s" else ""}"

                    if (swaps.isEmpty()) {
                        binding.emptyState.visibility = View.VISIBLE
                        binding.rvSwaps.visibility = View.GONE
                    } else {
                        binding.emptyState.visibility = View.GONE
                        binding.rvSwaps.visibility = View.VISIBLE
                    }
                }
            }
        }
    }

    private fun showCancelDialog(swap: Swap) {
        val reasons = arrayOf(
            "Emergency came up",
            "Schedule conflict",
            "Found another solution",
            "Requirement changed",
            "Partner unresponsive",
            "Other"
        )
        var selectedReason = reasons[0]
        MaterialAlertDialogBuilder(requireContext())
            .setTitle("Cancel Swap")
            .setMessage("⚠️ Cancelling will reduce your trust score by 0.3 points.")
            .setSingleChoiceItems(reasons, 0) { _, which -> selectedReason = reasons[which] }
            .setPositiveButton("Confirm Cancel") { _, _ ->
                viewModel.cancelSwap(swap, currentUserId, selectedReason)
                Snackbar.make(binding.root, "⚠️ Trust score reduced", Snackbar.LENGTH_LONG).show()
            }
            .setNegativeButton("Keep Swap", null)
            .show()
    }

    private fun showProofDialog(swap: Swap) {
        val input = EditText(requireContext()).apply {
            hint = "Describe work done, materials used, before/after notes... (min 10 chars)"
            minLines = 3
        }
        MaterialAlertDialogBuilder(requireContext())
            .setTitle("Submit Work Proof")
            .setView(input)
            .setPositiveButton("Submit") { _, _ ->
                val note = input.text.toString().trim()
                if (note.length < 10) {
                    Toast.makeText(context, "Proof must be at least 10 characters", Toast.LENGTH_SHORT).show()
                    return@setPositiveButton
                }
                viewModel.submitProof(swap, currentUserId, note)
                Snackbar.make(binding.root, "Proof submitted successfully", Snackbar.LENGTH_SHORT).show()
            }
            .setNegativeButton("Cancel", null)
            .show()
    }

    private fun showReviewDialog(swap: Swap) {
        val dialog = BottomSheetDialog(requireContext())
        val dialogView = layoutInflater.inflate(R.layout.dialog_review, null)
        dialog.setContentView(dialogView)

        // Populate partner name in title
        viewModel.getSwapPartnerName(swap, currentUserId) { partnerName ->
            activity?.runOnUiThread {
                dialogView.findViewById<TextView>(R.id.tvReviewTitle)?.text =
                    "Rate your experience with $partnerName"
            }
        }

        dialogView.findViewById<TextView>(R.id.tvSPInfo)?.text =
            "You will earn ${swap.agreedHours} SP"

        val ratingBar = dialogView.findViewById<RatingBar>(R.id.ratingBar)
        val etComment = dialogView.findViewById<TextInputEditText>(R.id.etComment)

        dialogView.findViewById<MaterialButton>(R.id.btnSubmitReview)?.setOnClickListener {
            val rating = ratingBar.rating.toInt()
            if (rating == 0) {
                Toast.makeText(context, "Please select a rating", Toast.LENGTH_SHORT).show()
                return@setOnClickListener
            }
            val comment = etComment?.text.toString()
            val review = Review(swapId = swap.id, reviewerId = currentUserId, rating = rating, comment = comment)
            viewModel.completeSwapAndReview(swap, review, currentUserId)
            dialog.dismiss()
            Snackbar.make(binding.root, "🎉 +${swap.agreedHours} SP earned!", Snackbar.LENGTH_LONG).show()
        }

        dialog.show()
    }

    override fun onDestroyView() { super.onDestroyView(); _binding = null }
}

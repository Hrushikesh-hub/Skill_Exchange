package com.example.skillexchangeapp.ui.adapter

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import com.example.skillexchangeapp.data.local.entity.Swap
import com.example.skillexchangeapp.databinding.ItemSwapCardBinding
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

data class SwapCardItem(
    val swap: Swap,
    val partnerName: String,
    val needTitle: String,
    val currentUserId: Long
)

class SwapManagementAdapter(
    private val onStartClick: (Swap) -> Unit,
    private val onCancelClick: (Swap) -> Unit,
    private val onProofClick: (Swap) -> Unit,
    private val onCompleteClick: (Swap) -> Unit,
    private val onChatClick: (Swap, String) -> Unit
) : ListAdapter<SwapCardItem, SwapManagementAdapter.SwapViewHolder>(DIFF) {

    companion object {
        private val DIFF = object : DiffUtil.ItemCallback<SwapCardItem>() {
            override fun areItemsTheSame(a: SwapCardItem, b: SwapCardItem) = a.swap.id == b.swap.id
            override fun areContentsTheSame(a: SwapCardItem, b: SwapCardItem) = a == b
        }
    }

    inner class SwapViewHolder(private val binding: ItemSwapCardBinding) :
        RecyclerView.ViewHolder(binding.root) {

        fun bind(item: SwapCardItem) {
            val swap = item.swap

            binding.tvPartnerName.text = item.partnerName
            binding.tvNeedTitle.text = item.needTitle
            binding.tvAgreedHours.text = "⏱ ${swap.agreedHours} SP"

            // Scheduled date
            if (swap.scheduledDate != null) {
                val sdf = SimpleDateFormat("EEE dd MMM · HH:mm", Locale.getDefault())
                binding.tvScheduled.text = sdf.format(Date(swap.scheduledDate))
                binding.tvScheduled.visibility = View.VISIBLE
            } else {
                binding.tvScheduled.visibility = View.GONE
            }

            // Reset all visibility
            binding.btnStart.visibility = View.GONE
            binding.btnCancel.visibility = View.GONE
            binding.btnSubmitProof.visibility = View.GONE
            binding.btnComplete.visibility = View.GONE
            binding.tvCompletedBadge.visibility = View.GONE
            binding.tvCancelledBadge.visibility = View.GONE
            binding.tvCancellationReason.visibility = View.GONE
            binding.btnChat.visibility = View.GONE

            when (swap.status) {
                "Scheduled" -> {
                    binding.chipStatus.text = "Scheduled"
                    binding.chipStatus.chipBackgroundColor =
                        android.content.res.ColorStateList.valueOf(0xFF1976D2.toInt())
                    binding.btnStart.visibility = View.VISIBLE
                    binding.btnCancel.visibility = View.VISIBLE
                    binding.btnChat.visibility = View.VISIBLE
                }
                "Ongoing" -> {
                    binding.chipStatus.text = "Ongoing"
                    binding.chipStatus.chipBackgroundColor =
                        android.content.res.ColorStateList.valueOf(0xFFF57C00.toInt())
                    binding.btnSubmitProof.visibility = View.VISIBLE
                    binding.btnCancel.visibility = View.VISIBLE
                    binding.btnChat.visibility = View.VISIBLE
                }
                "Proof Submitted" -> {
                    binding.chipStatus.text = "Proof Submitted"
                    binding.chipStatus.chipBackgroundColor =
                        android.content.res.ColorStateList.valueOf(0xFF7B1FA2.toInt())
                    // Only requester (userAId) can complete
                    if (item.currentUserId == swap.userAId) {
                        binding.btnComplete.visibility = View.VISIBLE
                    }
                    binding.btnChat.visibility = View.VISIBLE
                }
                "Completed" -> {
                    binding.chipStatus.text = "Completed"
                    binding.chipStatus.chipBackgroundColor =
                        android.content.res.ColorStateList.valueOf(0xFF388E3C.toInt())
                    binding.tvCompletedBadge.visibility = View.VISIBLE
                    if (swap.completionDate != null) {
                        val sdf = SimpleDateFormat("dd MMM yyyy", Locale.getDefault())
                        binding.tvCompletedBadge.text = "✓ Completed · ${sdf.format(Date(swap.completionDate))}"
                    } else {
                        binding.tvCompletedBadge.text = "✓ Completed"
                    }
                }
                "Cancelled" -> {
                    binding.chipStatus.text = "Cancelled"
                    binding.chipStatus.chipBackgroundColor =
                        android.content.res.ColorStateList.valueOf(0xFFD32F2F.toInt())
                    binding.tvCancelledBadge.visibility = View.VISIBLE
                    if (!swap.cancellationReason.isNullOrBlank()) {
                        binding.tvCancellationReason.visibility = View.VISIBLE
                        binding.tvCancellationReason.text = "Reason: ${swap.cancellationReason}"
                    }
                }
            }

            binding.btnStart.setOnClickListener { onStartClick(swap) }
            binding.btnCancel.setOnClickListener { onCancelClick(swap) }
            binding.btnSubmitProof.setOnClickListener { onProofClick(swap) }
            binding.btnComplete.setOnClickListener { onCompleteClick(swap) }
            binding.btnChat.setOnClickListener { onChatClick(swap, item.partnerName) }
        }
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): SwapViewHolder {
        val binding = ItemSwapCardBinding.inflate(LayoutInflater.from(parent.context), parent, false)
        return SwapViewHolder(binding)
    }

    override fun onBindViewHolder(holder: SwapViewHolder, position: Int) {
        holder.bind(getItem(position))
    }
}

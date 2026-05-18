package com.example.skillexchangeapp.ui.adapter

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import com.example.skillexchangeapp.R
import com.example.skillexchangeapp.data.local.entity.Swap
import com.example.skillexchangeapp.databinding.ItemHistoryBinding

class HistoryAdapter : ListAdapter<Swap, HistoryAdapter.HistoryViewHolder>(SwapDiffCallback()) {

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): HistoryViewHolder {
        val binding = ItemHistoryBinding.inflate(LayoutInflater.from(parent.context), parent, false)
        return HistoryViewHolder(binding)
    }

    override fun onBindViewHolder(holder: HistoryViewHolder, position: Int) {
        holder.bind(getItem(position))
    }

    class HistoryViewHolder(private val binding: ItemHistoryBinding) :
        RecyclerView.ViewHolder(binding.root) {
        fun bind(swap: Swap) {
            val context = binding.root.context
            binding.tvSwapId.text = context.getString(R.string.swap_id_format, swap.id)
            binding.tvAgreedHours.text = context.getString(R.string.hours_format, swap.agreedHours)
            
            if (swap.status.equals("Completed", ignoreCase = true)) {
                binding.tvStatus.text = context.getString(R.string.completed)
                binding.tvStatus.setTextColor(context.getColor(R.color.primary))
            } else {
                binding.tvStatus.text = context.getString(R.string.ongoing)
                binding.tvStatus.setTextColor(context.getColor(R.color.secondary))
            }
        }
    }

    class SwapDiffCallback : DiffUtil.ItemCallback<Swap>() {
        override fun areItemsTheSame(oldItem: Swap, newItem: Swap): Boolean = oldItem.id == newItem.id
        override fun areContentsTheSame(oldItem: Swap, newItem: Swap): Boolean = oldItem == newItem
    }
}

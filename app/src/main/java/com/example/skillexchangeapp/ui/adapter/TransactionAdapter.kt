package com.example.skillexchangeapp.ui.adapter

import android.graphics.Color
import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import com.example.skillexchangeapp.data.local.entity.SkillPointTransaction
import com.example.skillexchangeapp.databinding.ItemTransactionBinding
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

class TransactionAdapter : ListAdapter<SkillPointTransaction, TransactionAdapter.TxViewHolder>(DIFF) {

    companion object {
        private val DIFF = object : DiffUtil.ItemCallback<SkillPointTransaction>() {
            override fun areItemsTheSame(a: SkillPointTransaction, b: SkillPointTransaction) = a.id == b.id
            override fun areContentsTheSame(a: SkillPointTransaction, b: SkillPointTransaction) = a == b
        }
    }

    inner class TxViewHolder(private val binding: ItemTransactionBinding) :
        RecyclerView.ViewHolder(binding.root) {
        fun bind(tx: SkillPointTransaction) {
            binding.tvDescription.text = tx.description
            val sdf = SimpleDateFormat("dd MMM yyyy · HH:mm", Locale.getDefault())
            binding.tvDate.text = sdf.format(Date(tx.timestamp))
            if (tx.amount >= 0) {
                binding.ivArrow.setImageResource(android.R.drawable.arrow_up_float)
                binding.ivArrow.setColorFilter(Color.parseColor("#388E3C"))
                binding.tvAmount.text = "+${tx.amount} SP"
                binding.tvAmount.setTextColor(Color.parseColor("#388E3C"))
            } else {
                binding.ivArrow.setImageResource(android.R.drawable.arrow_down_float)
                binding.ivArrow.setColorFilter(Color.parseColor("#D32F2F"))
                binding.tvAmount.text = "${tx.amount} SP"
                binding.tvAmount.setTextColor(Color.parseColor("#D32F2F"))
            }
        }
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): TxViewHolder {
        val binding = ItemTransactionBinding.inflate(LayoutInflater.from(parent.context), parent, false)
        return TxViewHolder(binding)
    }

    override fun onBindViewHolder(holder: TxViewHolder, position: Int) {
        holder.bind(getItem(position))
    }
}

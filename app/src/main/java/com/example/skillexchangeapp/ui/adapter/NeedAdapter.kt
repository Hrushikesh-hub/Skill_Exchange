package com.example.skillexchangeapp.ui.adapter

import android.content.res.ColorStateList
import android.graphics.Color
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import com.example.skillexchangeapp.ai.AiMatchEngine
import com.example.skillexchangeapp.data.local.entity.NeedPost
import com.example.skillexchangeapp.data.local.entity.User
import com.example.skillexchangeapp.databinding.ItemNeedBinding
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

class NeedAdapter(
    private val onItemClick: (NeedPost) -> Unit,
    private var currentUser: User? = null
) : ListAdapter<NeedPost, NeedAdapter.NeedViewHolder>(NeedDiffCallback()) {

    fun updateCurrentUser(user: User?) {
        currentUser = user
        notifyDataSetChanged()
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): NeedViewHolder {
        val binding = ItemNeedBinding.inflate(LayoutInflater.from(parent.context), parent, false)
        return NeedViewHolder(binding)
    }

    override fun onBindViewHolder(holder: NeedViewHolder, position: Int) {
        holder.bind(getItem(position))
    }

    inner class NeedViewHolder(private val binding: ItemNeedBinding) :
        RecyclerView.ViewHolder(binding.root) {

        fun bind(item: NeedPost) {
            binding.tvTitle.text = item.title
            binding.tvSkill.text = item.skillRequired
            binding.tvDescription.text = item.aiSummary.ifBlank { item.description }
            binding.tvOfferCount.text = if (item.offerCount > 0) "${item.offerCount} offer${if (item.offerCount != 1) "s" else ""}" else "No offers yet"
            binding.tvHours.text = "~${item.estimatedHours}h"

            val locationText = StringBuilder(item.location)
            if (item.deadline != null) {
                val fmt = SimpleDateFormat("dd MMM", Locale.getDefault())
                locationText.append(" · due ${fmt.format(Date(item.deadline))}")
            }
            binding.tvLocation.text = locationText

            // Urgency chip
            when (item.urgencyLevel.lowercase()) {
                "high" -> {
                    binding.chipUrgency.text = "🔴 High"
                    binding.chipUrgency.chipBackgroundColor = ColorStateList.valueOf(Color.parseColor("#FFEBEE"))
                    binding.chipUrgency.setTextColor(Color.parseColor("#D32F2F"))
                }
                "medium" -> {
                    binding.chipUrgency.text = "🟡 Medium"
                    binding.chipUrgency.chipBackgroundColor = ColorStateList.valueOf(Color.parseColor("#FFF8E1"))
                    binding.chipUrgency.setTextColor(Color.parseColor("#F57C00"))
                }
                else -> {
                    binding.chipUrgency.text = "🟢 Low"
                    binding.chipUrgency.chipBackgroundColor = ColorStateList.valueOf(Color.parseColor("#E8F5E9"))
                    binding.chipUrgency.setTextColor(Color.parseColor("#388E3C"))
                }
            }

            // AI Match score chip
            val user = currentUser
            if (user != null) {
                val score = AiMatchEngine.rankNeedsForUser(user, listOf(item)).firstOrNull()?.score ?: 0
                binding.chipMatchScore.visibility = View.VISIBLE
                binding.chipMatchScore.text = "$score% Match"
                val (bg, fg) = when {
                    score >= 70 -> Pair("#E8F5E9", "#388E3C")
                    score >= 40 -> Pair("#FFF8E1", "#F57C00")
                    else -> Pair("#F5F5F5", "#757575")
                }
                binding.chipMatchScore.chipBackgroundColor = ColorStateList.valueOf(Color.parseColor(bg))
                binding.chipMatchScore.setTextColor(Color.parseColor(fg))
            } else {
                binding.chipMatchScore.visibility = View.GONE
            }

            // Status badge + card tint for non-open posts
            if (item.status != "Open") {
                binding.tvStatus.visibility = View.VISIBLE
                binding.tvStatus.text = item.status
                binding.root.alpha = 0.55f
                binding.root.setOnClickListener(null)
            } else {
                binding.tvStatus.visibility = View.GONE
                binding.root.alpha = 1.0f
                binding.root.setOnClickListener { onItemClick(item) }
            }
        }
    }

    class NeedDiffCallback : DiffUtil.ItemCallback<NeedPost>() {
        override fun areItemsTheSame(oldItem: NeedPost, newItem: NeedPost) = oldItem.id == newItem.id
        override fun areContentsTheSame(oldItem: NeedPost, newItem: NeedPost) = oldItem == newItem
    }
}

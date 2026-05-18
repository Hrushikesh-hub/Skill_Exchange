package com.example.skillexchangeapp.ui.adapter

import android.content.res.ColorStateList
import android.graphics.Color
import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import com.example.skillexchangeapp.ai.BarterSuggestionEngine
import com.example.skillexchangeapp.databinding.ItemBarterSuggestionBinding

class BarterSuggestionAdapter(
    private val onClick: (BarterSuggestionEngine.BarterSuggestion) -> Unit
) : ListAdapter<BarterSuggestionEngine.BarterSuggestion, BarterSuggestionAdapter.VH>(DIFF) {

    companion object {
        private val DIFF = object : DiffUtil.ItemCallback<BarterSuggestionEngine.BarterSuggestion>() {
            override fun areItemsTheSame(a: BarterSuggestionEngine.BarterSuggestion, b: BarterSuggestionEngine.BarterSuggestion) =
                a.partner.id == b.partner.id && a.theyNeed.id == b.theyNeed.id
            override fun areContentsTheSame(a: BarterSuggestionEngine.BarterSuggestion, b: BarterSuggestionEngine.BarterSuggestion) = a == b
        }
    }

    inner class VH(private val binding: ItemBarterSuggestionBinding) :
        RecyclerView.ViewHolder(binding.root) {
        fun bind(s: BarterSuggestionEngine.BarterSuggestion) {
            binding.tvBarterTitle.text = "You offer ${s.youCanOffer}  ⇄  They need ${s.theyCanOfferSkill}"
            binding.tvBarterSubtitle.text = s.mutualBenefit
            binding.progressStrength.progress = s.strength
            binding.chipMutualMatch.text = "🔄 Mutual Match · ${s.strength}%"
            binding.chipMutualMatch.chipBackgroundColor =
                ColorStateList.valueOf(Color.parseColor("#E8EAF6"))
            binding.chipMutualMatch.setTextColor(Color.parseColor("#3949AB"))
            binding.root.setOnClickListener { onClick(s) }
        }
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): VH {
        val binding = ItemBarterSuggestionBinding.inflate(LayoutInflater.from(parent.context), parent, false)
        return VH(binding)
    }

    override fun onBindViewHolder(holder: VH, position: Int) {
        holder.bind(getItem(position))
    }
}

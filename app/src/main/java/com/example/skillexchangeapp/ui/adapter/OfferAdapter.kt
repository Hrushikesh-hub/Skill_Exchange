package com.example.skillexchangeapp.ui.adapter

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import com.example.skillexchangeapp.data.local.entity.Offer
import com.example.skillexchangeapp.data.local.entity.User
import com.example.skillexchangeapp.databinding.ItemOfferCardBinding

data class OfferWithDetails(
    val offer: Offer,
    val offerer: User,
    val needTitle: String
)

class OfferAdapter(
    private val onAccept: (OfferWithDetails) -> Unit,
    private val onReject: (OfferWithDetails) -> Unit
) : ListAdapter<OfferWithDetails, OfferAdapter.OfferViewHolder>(OfferDiffCallback()) {

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): OfferViewHolder {
        val binding = ItemOfferCardBinding.inflate(LayoutInflater.from(parent.context), parent, false)
        return OfferViewHolder(binding)
    }

    override fun onBindViewHolder(holder: OfferViewHolder, position: Int) {
        holder.bind(getItem(position))
    }

    inner class OfferViewHolder(private val binding: ItemOfferCardBinding) :
        RecyclerView.ViewHolder(binding.root) {
        fun bind(item: OfferWithDetails) {
            binding.tvNeedTitle.text = "For: ${item.needTitle}"
            binding.tvOffererName.text = item.offerer.fullName
            binding.tvOffererTrust.text =
                "${item.offerer.trustScore}★ | ${item.offerer.completedSwaps} swaps | ${item.offerer.verificationStatus} | R${item.offerer.reliabilityScore}"
            binding.tvOfferedSkill.text = "Offering: ${item.offer.offeredSkill}"
            binding.tvOfferedHours.text = "${item.offer.offeredHours}h"
            val schedule = listOfNotNull(
                item.offer.proposedTime,
                item.offer.matchScore.takeIf { it > 0 }?.let { "$it% match" }
            ).joinToString(" | ")
            binding.tvOfferMessage.text = if (schedule.isBlank()) item.offer.message else "${item.offer.message}\n$schedule"

            binding.tvOfferedHours.visibility = if (item.offer.offeredHours == 0) View.GONE else View.VISIBLE
            binding.btnAccept.setOnClickListener { onAccept(item) }
            binding.btnReject.setOnClickListener { onReject(item) }
        }
    }

    class OfferDiffCallback : DiffUtil.ItemCallback<OfferWithDetails>() {
        override fun areItemsTheSame(oldItem: OfferWithDetails, newItem: OfferWithDetails) = oldItem.offer.id == newItem.offer.id
        override fun areContentsTheSame(oldItem: OfferWithDetails, newItem: OfferWithDetails) = oldItem == newItem
    }
}

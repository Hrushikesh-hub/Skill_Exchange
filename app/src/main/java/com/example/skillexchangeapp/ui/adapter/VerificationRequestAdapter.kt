package com.example.skillexchangeapp.ui.adapter

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import com.example.skillexchangeapp.data.local.entity.VerificationRequest
import com.example.skillexchangeapp.databinding.ItemVerificationRequestBinding
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

data class VerificationRequestItem(
    val request: VerificationRequest,
    val userName: String
)

class VerificationRequestAdapter(
    private val onApprove: (VerificationRequest) -> Unit,
    private val onReject: (VerificationRequest) -> Unit
) : ListAdapter<VerificationRequestItem, VerificationRequestAdapter.VrViewHolder>(DIFF) {

    companion object {
        private val DIFF = object : DiffUtil.ItemCallback<VerificationRequestItem>() {
            override fun areItemsTheSame(a: VerificationRequestItem, b: VerificationRequestItem) = a.request.id == b.request.id
            override fun areContentsTheSame(a: VerificationRequestItem, b: VerificationRequestItem) = a == b
        }
    }

    inner class VrViewHolder(private val binding: ItemVerificationRequestBinding) :
        RecyclerView.ViewHolder(binding.root) {

        private var expanded = false

        fun bind(item: VerificationRequestItem) {
            val req = item.request
            binding.tvUserName.text = item.userName
            binding.tvSkill.text = req.skill
            binding.chipStatus.text = req.status
            val sdf = SimpleDateFormat("dd MMM yyyy", Locale.getDefault())
            binding.tvCreatedAt.text = sdf.format(Date(req.createdAt))

            // Collapsed proof text
            val proof = req.proofText
            if (proof.length > 80) {
                binding.tvProofText.text = proof.take(80) + "..."
                binding.btnShowMore.visibility = View.VISIBLE
                binding.btnShowMore.setOnClickListener {
                    expanded = !expanded
                    if (expanded) {
                        binding.tvProofText.text = proof
                        binding.btnShowMore.text = "Show less"
                    } else {
                        binding.tvProofText.text = proof.take(80) + "..."
                        binding.btnShowMore.text = "Show more"
                    }
                }
            } else {
                binding.tvProofText.text = proof
                binding.btnShowMore.visibility = View.GONE
            }

            if (req.status == "Pending") {
                binding.btnApprove.visibility = View.VISIBLE
                binding.btnReject.visibility = View.VISIBLE
            } else {
                binding.btnApprove.visibility = View.GONE
                binding.btnReject.visibility = View.GONE
            }

            binding.btnApprove.setOnClickListener { onApprove(req) }
            binding.btnReject.setOnClickListener { onReject(req) }
        }
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): VrViewHolder {
        val binding = ItemVerificationRequestBinding.inflate(LayoutInflater.from(parent.context), parent, false)
        return VrViewHolder(binding)
    }

    override fun onBindViewHolder(holder: VrViewHolder, position: Int) {
        holder.bind(getItem(position))
    }
}

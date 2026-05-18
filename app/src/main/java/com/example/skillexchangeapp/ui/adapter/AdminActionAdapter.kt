package com.example.skillexchangeapp.ui.adapter

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import com.example.skillexchangeapp.data.local.entity.AdminAction
import com.example.skillexchangeapp.databinding.ItemAdminActionBinding
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

class AdminActionAdapter : ListAdapter<AdminAction, AdminActionAdapter.ActionViewHolder>(DIFF) {

    companion object {
        private val DIFF = object : DiffUtil.ItemCallback<AdminAction>() {
            override fun areItemsTheSame(a: AdminAction, b: AdminAction) = a.id == b.id
            override fun areContentsTheSame(a: AdminAction, b: AdminAction) = a == b
        }
    }

    inner class ActionViewHolder(private val binding: ItemAdminActionBinding) :
        RecyclerView.ViewHolder(binding.root) {
        fun bind(action: AdminAction) {
            binding.tvActionType.text = action.actionType
            binding.tvTargetType.text = "${action.targetType} #${action.targetId}"
            binding.tvNotes.text = action.notes
            val sdf = SimpleDateFormat("dd MMM yyyy · HH:mm", Locale.getDefault())
            binding.tvTimestamp.text = sdf.format(Date(action.createdAt))
        }
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ActionViewHolder {
        val binding = ItemAdminActionBinding.inflate(LayoutInflater.from(parent.context), parent, false)
        return ActionViewHolder(binding)
    }

    override fun onBindViewHolder(holder: ActionViewHolder, position: Int) {
        holder.bind(getItem(position))
    }
}

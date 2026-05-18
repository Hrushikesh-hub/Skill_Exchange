package com.example.skillexchangeapp.ui.adapter

import android.content.res.ColorStateList
import android.graphics.Color
import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import com.example.skillexchangeapp.data.local.entity.Notification
import com.example.skillexchangeapp.databinding.ItemNotificationBinding

class NotificationAdapter(
    private val onTap: (Notification) -> Unit
) : ListAdapter<Notification, NotificationAdapter.NotifViewHolder>(NotifDiffCallback()) {

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): NotifViewHolder {
        val binding = ItemNotificationBinding.inflate(LayoutInflater.from(parent.context), parent, false)
        return NotifViewHolder(binding)
    }

    override fun onBindViewHolder(holder: NotifViewHolder, position: Int) {
        holder.bind(getItem(position))
    }

    inner class NotifViewHolder(private val binding: ItemNotificationBinding) :
        RecyclerView.ViewHolder(binding.root) {
        fun bind(notif: Notification) {
            binding.tvTitle.text = notif.title
            binding.tvMessage.text = notif.message
            binding.unreadDot.visibility = if (notif.isRead) android.view.View.INVISIBLE else android.view.View.VISIBLE

            // Unread = highlighted background
            if (!notif.isRead) {
                binding.root.setBackgroundColor(Color.parseColor("#EFF6FF"))
            } else {
                binding.root.setBackgroundColor(Color.TRANSPARENT)
            }

            // Relative timestamp
            val elapsed = System.currentTimeMillis() - notif.timestamp
            val minutes = elapsed / 60000
            binding.tvTime.text = when {
                minutes < 1    -> "Just now"
                minutes < 60   -> "${minutes}m ago"
                minutes < 1440 -> "${minutes / 60}h ago"
                minutes < 2880 -> "Yesterday"
                else           -> "${minutes / 1440} days ago"
            }

            binding.root.setOnClickListener { onTap(notif) }
        }
    }

    class NotifDiffCallback : DiffUtil.ItemCallback<Notification>() {
        override fun areItemsTheSame(oldItem: Notification, newItem: Notification) = oldItem.id == newItem.id
        override fun areContentsTheSame(oldItem: Notification, newItem: Notification) = oldItem == newItem
    }
}

package com.example.skillexchangeapp.ui.adapter

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import com.example.skillexchangeapp.data.local.entity.Message
import com.example.skillexchangeapp.databinding.ItemChatMessageBinding
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

class ChatAdapter(private val currentUserId: Long) :
    ListAdapter<Message, ChatAdapter.ChatViewHolder>(ChatDiffCallback()) {

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ChatViewHolder {
        val binding = ItemChatMessageBinding.inflate(LayoutInflater.from(parent.context), parent, false)
        return ChatViewHolder(binding)
    }

    override fun onBindViewHolder(holder: ChatViewHolder, position: Int) {
        holder.bind(getItem(position))
    }

    inner class ChatViewHolder(private val binding: ItemChatMessageBinding) :
        RecyclerView.ViewHolder(binding.root) {
        fun bind(message: Message) {
            val timeFormat = SimpleDateFormat("hh:mm a", Locale.getDefault())
            val time = timeFormat.format(Date(message.timestamp))

            if (message.senderId == currentUserId) {
                binding.layoutSent.visibility = View.VISIBLE
                binding.layoutReceived.visibility = View.GONE
                binding.tvSentMessage.text = message.content
                binding.tvSentTime.text = time
            } else {
                binding.layoutSent.visibility = View.GONE
                binding.layoutReceived.visibility = View.VISIBLE
                binding.tvReceivedMessage.text = message.content
                binding.tvReceivedTime.text = time
            }
        }
    }

    class ChatDiffCallback : DiffUtil.ItemCallback<Message>() {
        override fun areItemsTheSame(oldItem: Message, newItem: Message) = oldItem.id == newItem.id
        override fun areContentsTheSame(oldItem: Message, newItem: Message) = oldItem == newItem
    }
}

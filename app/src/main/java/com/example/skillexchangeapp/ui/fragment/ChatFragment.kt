package com.example.skillexchangeapp.ui.fragment

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.lifecycle.lifecycleScope
import com.example.skillexchangeapp.SkillExchangeApplication
import com.example.skillexchangeapp.data.local.entity.Message
import com.example.skillexchangeapp.databinding.FragmentChatBinding
import com.example.skillexchangeapp.ui.adapter.ChatAdapter
import com.example.skillexchangeapp.ui.viewmodel.MainViewModel
import com.example.skillexchangeapp.ui.viewmodel.ViewModelFactory
import com.example.skillexchangeapp.utils.SessionManager
import kotlinx.coroutines.launch

class ChatFragment : Fragment() {

    private var _binding: FragmentChatBinding? = null
    private val binding get() = _binding!!

    private val viewModel: MainViewModel by viewModels {
        ViewModelFactory((requireActivity().application as SkillExchangeApplication).repository)
    }

    private lateinit var adapter: ChatAdapter
    private var partnerId: Long = -1L

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View {
        _binding = FragmentChatBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        partnerId = arguments?.getLong("partnerId", -1L) ?: -1L
        val partnerName = arguments?.getString("partnerName", "Chat") ?: "Chat"
        val userId = SessionManager(requireContext()).getUserId()

        binding.toolbar.title = partnerName
        binding.toolbar.setNavigationOnClickListener {
            requireActivity().onBackPressedDispatcher.onBackPressed()
        }

        adapter = ChatAdapter(userId)
        binding.rvMessages.adapter = adapter

        binding.btnSend.setOnClickListener {
            val text = binding.etMessage.text.toString().trim()
            if (text.isNotEmpty() && partnerId > 0) {
                val message = Message(senderId = userId, receiverId = partnerId, content = text)
                viewModel.sendMessage(message)
                binding.etMessage.text?.clear()
            }
        }

        if (partnerId > 0) {
            lifecycleScope.launch {
                viewModel.getMessages(userId, partnerId).collect { messages ->
                    adapter.submitList(messages)
                    if (messages.isNotEmpty()) {
                        binding.rvMessages.scrollToPosition(messages.size - 1)
                    }
                }
            }
        }
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}

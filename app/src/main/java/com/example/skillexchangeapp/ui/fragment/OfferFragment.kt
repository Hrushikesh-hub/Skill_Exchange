package com.example.skillexchangeapp.ui.fragment

import android.app.DatePickerDialog
import android.os.Bundle
import android.text.Editable
import android.text.TextWatcher
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ScrollView
import android.widget.TextView
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.lifecycle.lifecycleScope
import androidx.navigation.fragment.findNavController
import com.example.skillexchangeapp.SkillExchangeApplication
import com.example.skillexchangeapp.data.local.entity.NeedPost
import com.example.skillexchangeapp.data.local.entity.Offer
import com.example.skillexchangeapp.databinding.FragmentOfferBinding
import com.example.skillexchangeapp.ui.viewmodel.MainViewModel
import com.example.skillexchangeapp.ui.viewmodel.ViewModelFactory
import com.example.skillexchangeapp.utils.SessionManager
import com.google.android.material.bottomsheet.BottomSheetDialog
import com.google.android.material.button.MaterialButton
import com.google.android.material.snackbar.Snackbar
import kotlinx.coroutines.launch
import java.text.SimpleDateFormat
import java.util.Calendar
import java.util.Locale

class OfferFragment : Fragment() {

    private var _binding: FragmentOfferBinding? = null
    private val binding get() = _binding!!
    private var needTitle = ""
    private var loadedNeed: NeedPost? = null
    private var proposedDate: Long? = null
    private var aiDraftUsed = false

    private val viewModel: MainViewModel by viewModels {
        ViewModelFactory((requireActivity().application as SkillExchangeApplication).repository)
    }

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View {
        _binding = FragmentOfferBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        binding.toolbar.setNavigationOnClickListener { findNavController().popBackStack() }

        val postId = arguments?.getLong("postId") ?: -1L
        val userId = SessionManager(requireContext()).getUserId()

        binding.etProposedDate.setOnClickListener {
            val calendar = Calendar.getInstance()
            DatePickerDialog(requireContext(), { _, year, month, day ->
                calendar.set(year, month, day)
                proposedDate = calendar.timeInMillis
                binding.etProposedDate.setText(
                    SimpleDateFormat("dd MMM yyyy", Locale.getDefault()).format(calendar.time)
                )
            }, calendar.get(Calendar.YEAR), calendar.get(Calendar.MONTH), calendar.get(Calendar.DAY_OF_MONTH)).apply {
                datePicker.minDate = System.currentTimeMillis()
            }.show()
        }

        // Live validation watcher for submit button
        val watcher = object : TextWatcher {
            override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {}
            override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {}
            override fun afterTextChanged(s: Editable?) { updateSubmitEnabled() }
        }
        binding.etSkillsOffered.addTextChangedListener(watcher)
        binding.etHoursOffered.addTextChangedListener(watcher)
        binding.etMessage.addTextChangedListener(watcher)
        binding.btnSubmit.isEnabled = false

        if (postId > 0) {
            lifecycleScope.launch {
                val app = requireActivity().application as SkillExchangeApplication
                val user = app.repository.getUserByIdSync(userId)
                val need = app.repository.getNeedById(postId)
                need?.let {
                    loadedNeed = it
                    needTitle = it.title
                    binding.tvNeedTitle.text = it.title
                    binding.tvNeedDescription.text = it.description
                    binding.tvNeedSkill.text = it.skillRequired
                    binding.tvNeedHours.text = "${it.estimatedHours}h · ${it.estimatedHours} SP"
                    binding.tvNeedOffers.text = "${it.offerCount} applied"
                    binding.etHoursOffered.setText(it.estimatedHours.toString())

                    // Own post check
                    if (it.userId == userId) {
                        binding.formContainer.visibility = View.GONE
                        binding.tvOwnPost.visibility = View.VISIBLE
                        return@let
                    }

                    // Non-open post check
                    if (it.status != "Open") {
                        binding.formContainer.visibility = View.GONE
                        binding.tvPostClosed.visibility = View.VISIBLE
                        binding.tvPostClosed.text = "This need is ${it.status} — offers no longer accepted."
                        return@let
                    }

                    // AI match explanation card
                    if (user != null) {
                        viewModel.generateOfferDraft(userId, it) { draft, explanation ->
                            activity?.runOnUiThread {
                                binding.tvMatchExplanation.text = explanation
                                binding.cardAiInsight.visibility = View.VISIBLE
                                if (binding.etMessage.text.isNullOrBlank()) {
                                    binding.etMessage.setText(draft)
                                }
                            }
                        }
                    }
                }
            }
        }

        // AI Draft BottomSheet
        binding.btnDraftOffer.setOnClickListener {
            val need = loadedNeed ?: return@setOnClickListener
            viewModel.generateOfferDraft(userId, need) { draft, _ ->
                activity?.runOnUiThread { showDraftBottomSheet(draft) }
            }
        }

        binding.btnSubmit.setOnClickListener {
            if (!validateAll()) return@setOnClickListener
            val message = binding.etMessage.text.toString().trim()
            val skillsOffered = binding.etSkillsOffered.text.toString().trim()
            val hoursOffered = binding.etHoursOffered.text.toString().toIntOrNull() ?: 0
            val proposedTime = binding.etProposedTime.text.toString().trim().let { if (it.isBlank()) null else it }
            val offer = Offer(
                needPostId = postId,
                offeredByUserId = userId,
                offeredSkill = skillsOffered,
                offeredHours = hoursOffered,
                message = message,
                proposedDate = proposedDate,
                proposedTime = proposedTime,
                aiDraftUsed = aiDraftUsed
            )
            viewModel.submitOffer(offer, needTitle)
            Snackbar.make(binding.root, "Offer submitted! ✓", Snackbar.LENGTH_SHORT).show()
            findNavController().popBackStack()
        }
    }

    private fun showDraftBottomSheet(draft: String) {
        val dialog = BottomSheetDialog(requireContext())
        val sheetView = layoutInflater.inflate(com.example.skillexchangeapp.R.layout.bottom_sheet_ai_draft, null)
        dialog.setContentView(sheetView)

        sheetView.findViewById<TextView>(com.example.skillexchangeapp.R.id.tvDraftContent)?.text = draft

        sheetView.findViewById<MaterialButton>(com.example.skillexchangeapp.R.id.btnUseDraft)?.setOnClickListener {
            binding.etMessage.setText(draft)
            binding.chipAiAssisted.visibility = View.VISIBLE
            aiDraftUsed = true
            dialog.dismiss()
        }
        sheetView.findViewById<MaterialButton>(com.example.skillexchangeapp.R.id.btnCloseDraft)?.setOnClickListener {
            dialog.dismiss()
        }
        dialog.show()
    }

    private fun updateSubmitEnabled() {
        val skill = binding.etSkillsOffered.text.toString().trim()
        val hours = binding.etHoursOffered.text.toString().toIntOrNull() ?: 0
        val msg = binding.etMessage.text.toString().trim()
        binding.btnSubmit.isEnabled = skill.isNotEmpty() && hours in 1..40 && msg.length >= 10
    }

    private fun validateAll(): Boolean {
        var valid = true
        val skill = binding.etSkillsOffered.text.toString().trim()
        if (skill.isEmpty()) { binding.tilSkillsOffered.error = "Enter your skill"; valid = false }
        else binding.tilSkillsOffered.error = null

        val hours = binding.etHoursOffered.text.toString().toIntOrNull() ?: 0
        if (hours !in 1..40) { binding.tilHoursOffered.error = "Hours must be 1–40"; valid = false }
        else binding.tilHoursOffered.error = null

        val msg = binding.etMessage.text.toString().trim()
        if (msg.length < 10) { binding.tilMessage.error = "Message must be at least 10 characters"; valid = false }
        else binding.tilMessage.error = null

        return valid
    }

    override fun onDestroyView() { super.onDestroyView(); _binding = null }
}

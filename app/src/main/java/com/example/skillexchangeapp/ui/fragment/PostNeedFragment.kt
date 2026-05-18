package com.example.skillexchangeapp.ui.fragment

import android.app.DatePickerDialog
import android.os.Bundle
import android.text.Editable
import android.text.TextWatcher
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ArrayAdapter
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.lifecycle.lifecycleScope
import androidx.navigation.fragment.findNavController
import com.example.skillexchangeapp.R
import com.example.skillexchangeapp.SkillExchangeApplication
import com.example.skillexchangeapp.data.local.entity.NeedPost
import com.example.skillexchangeapp.databinding.FragmentPostNeedBinding
import com.example.skillexchangeapp.ui.viewmodel.MainViewModel
import com.example.skillexchangeapp.ui.viewmodel.ViewModelFactory
import com.example.skillexchangeapp.utils.SessionManager
import com.google.android.material.snackbar.Snackbar
import kotlinx.coroutines.launch
import java.text.SimpleDateFormat
import java.util.Calendar
import java.util.Locale

class PostNeedFragment : Fragment() {

    private var _binding: FragmentPostNeedBinding? = null
    private val binding get() = _binding!!
    private var selectedDeadline: Long? = null

    private val viewModel: MainViewModel by viewModels {
        ViewModelFactory((requireActivity().application as SkillExchangeApplication).repository)
    }

    private val skills = listOf(
        "Plumbing", "Carpentry", "Electrical", "Masonry", "Painting",
        "Welding", "Mechanic", "Tailoring", "Cooking", "Teaching", "Farming", "Driving"
    )

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View {
        _binding = FragmentPostNeedBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        binding.toolbar.setNavigationOnClickListener { findNavController().popBackStack() }

        // Skill dropdown
        val skillAdapter = ArrayAdapter(requireContext(), android.R.layout.simple_dropdown_item_1line, skills)
        binding.actvSkill.setAdapter(skillAdapter)

        // Pre-fill location from user profile
        val userId = SessionManager(requireContext()).getUserId()
        lifecycleScope.launch {
            viewModel.getUser(userId).collect { user ->
                user?.let {
                    if (binding.etLocation.text.isNullOrBlank()) {
                        binding.etLocation.setText(it.village)
                    }
                }
            }
        }

        // Deadline picker
        binding.etDeadline.setOnClickListener {
            val cal = Calendar.getInstance()
            DatePickerDialog(requireContext(), { _, year, month, day ->
                cal.set(year, month, day)
                selectedDeadline = cal.timeInMillis
                val fmt = SimpleDateFormat("dd MMM yyyy", Locale.getDefault())
                binding.etDeadline.setText(fmt.format(cal.time))
            }, cal.get(Calendar.YEAR), cal.get(Calendar.MONTH), cal.get(Calendar.DAY_OF_MONTH)).apply {
                datePicker.minDate = System.currentTimeMillis()
            }.show()
        }

        // Submit button disabled until valid
        binding.btnPostNeed.isEnabled = false
        setupValidation()

        // AI Assist
        binding.btnAiAssist.setOnClickListener {
            val skill = binding.actvSkill.text.toString().trim()
            val location = binding.etLocation.text.toString().trim()
            val description = binding.etDescription.text.toString().trim()
            val hours = binding.etHours.text.toString().toIntOrNull() ?: 0
            val urgency = getUrgency()
            val assist = viewModel.generateNeedAssist(skill, location, description, hours, urgency)
            if (binding.etTitle.text.isNullOrBlank()) binding.etTitle.setText(assist.suggestedTitle)
            binding.etDescription.setText(assist.improvedDescription)
            binding.tvAiSuggestion.text = assist.fairnessNote
            binding.tvAiSuggestion.visibility = View.VISIBLE
        }

        binding.btnPostNeed.setOnClickListener {
            if (!validateAll()) return@setOnClickListener
            val post = NeedPost(
                userId = userId,
                title = binding.etTitle.text.toString().trim(),
                description = binding.etDescription.text.toString().trim(),
                skillRequired = binding.actvSkill.text.toString().trim(),
                estimatedHours = binding.etHours.text.toString().toInt(),
                urgencyLevel = getUrgency(),
                location = binding.etLocation.text.toString().trim(),
                deadline = selectedDeadline,
                fairnessNote = binding.tvAiSuggestion.text.toString()
            )
            viewModel.postNeed(post)
            Snackbar.make(binding.root, "Need posted! 🎉", Snackbar.LENGTH_SHORT).show()
            findNavController().popBackStack()
        }
    }

    private fun getUrgency(): String = when {
        binding.rbHigh.isChecked -> "High"
        binding.rbLow.isChecked -> "Low"
        else -> "Medium"
    }

    private fun isFormValid(): Boolean {
        val title = binding.etTitle.text.toString().trim()
        val desc = binding.etDescription.text.toString().trim()
        val skill = binding.actvSkill.text.toString().trim()
        val hoursStr = binding.etHours.text.toString().trim()
        val hours = hoursStr.toIntOrNull() ?: 0
        val location = binding.etLocation.text.toString().trim()
        val urgencySelected = binding.rbLow.isChecked || binding.rbMedium.isChecked || binding.rbHigh.isChecked
        return title.length in 3..100 && desc.length in 10..500 &&
                skill.isNotEmpty() && hours in 1..40 &&
                location.isNotEmpty() && urgencySelected
    }

    private fun validateAll(): Boolean {
        var valid = true
        val title = binding.etTitle.text.toString().trim()
        if (title.length < 3 || title.length > 100) {
            binding.tilTitle.error = "Title must be 3–100 characters"
            valid = false
        } else binding.tilTitle.error = null

        val desc = binding.etDescription.text.toString().trim()
        if (desc.length < 10 || desc.length > 500) {
            binding.tilDescription.error = "Description must be 10–500 characters"
            valid = false
        } else binding.tilDescription.error = null

        val skill = binding.actvSkill.text.toString().trim()
        if (skill.isEmpty()) {
            binding.tilSkill.error = "Select a skill"
            valid = false
        } else binding.tilSkill.error = null

        val hours = binding.etHours.text.toString().toIntOrNull() ?: 0
        if (hours < 1 || hours > 40) {
            binding.tilHours.error = "Enter hours between 1 and 40"
            valid = false
        } else binding.tilHours.error = null

        if (binding.etLocation.text.toString().trim().isEmpty()) {
            binding.tilLocation.error = "Location is required"
            valid = false
        } else binding.tilLocation.error = null

        return valid
    }

    private fun setupValidation() {
        val watcher = object : TextWatcher {
            override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {}
            override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {}
            override fun afterTextChanged(s: Editable?) {
                binding.btnPostNeed.isEnabled = isFormValid()
            }
        }
        binding.etTitle.addTextChangedListener(watcher)
        binding.etDescription.addTextChangedListener(watcher)
        binding.actvSkill.addTextChangedListener(watcher)
        binding.etHours.addTextChangedListener(watcher)
        binding.etLocation.addTextChangedListener(watcher)
        binding.rgUrgency.setOnCheckedChangeListener { _, _ -> binding.btnPostNeed.isEnabled = isFormValid() }
    }

    override fun onDestroyView() { super.onDestroyView(); _binding = null }
}

package com.example.skillexchangeapp.ui.fragment

import android.os.Bundle
import android.text.Editable
import android.text.InputType
import android.text.TextWatcher
import android.util.Patterns
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
import com.example.skillexchangeapp.data.local.entity.User
import com.example.skillexchangeapp.databinding.FragmentRegisterBinding
import com.example.skillexchangeapp.ui.viewmodel.AuthState
import com.example.skillexchangeapp.ui.viewmodel.AuthViewModel
import com.example.skillexchangeapp.ui.viewmodel.ViewModelFactory
import com.example.skillexchangeapp.utils.SecurityUtils
import com.example.skillexchangeapp.utils.SessionManager
import com.google.android.material.snackbar.Snackbar
import kotlinx.coroutines.launch

class RegisterFragment : Fragment() {

    private var _binding: FragmentRegisterBinding? = null
    private val binding get() = _binding!!
    private var passwordVisible = false

    private val viewModel: AuthViewModel by viewModels {
        ViewModelFactory((requireActivity().application as SkillExchangeApplication).repository)
    }

    private val skills = listOf(
        "Plumbing", "Carpentry", "Electrical", "Masonry", "Painting",
        "Welding", "Mechanic", "Tailoring", "Cooking", "Teaching", "Farming", "Driving"
    )

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View {
        _binding = FragmentRegisterBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        // Skill dropdown
        val skillAdapter = ArrayAdapter(requireContext(), android.R.layout.simple_dropdown_item_1line, skills)
        binding.actvPrimarySkill.setAdapter(skillAdapter)

        // Password visibility toggle
        binding.btnTogglePassword.setOnClickListener {
            passwordVisible = !passwordVisible
            val type = if (passwordVisible)
                InputType.TYPE_CLASS_TEXT or InputType.TYPE_TEXT_VARIATION_VISIBLE_PASSWORD
            else
                InputType.TYPE_CLASS_TEXT or InputType.TYPE_TEXT_VARIATION_PASSWORD
            binding.etPassword.inputType = type
            binding.etPassword.setSelection(binding.etPassword.text?.length ?: 0)
            binding.btnTogglePassword.text = if (passwordVisible) "Hide" else "Show"
        }

        binding.btnRegister.isEnabled = false
        setupValidation()

        binding.tvLogin.setOnClickListener { findNavController().popBackStack() }

        binding.btnRegister.setOnClickListener {
            if (!validateAll()) return@setOnClickListener
            val plainPassword = binding.etPassword.text.toString()
            val user = User(
                fullName = binding.etFullName.text.toString().trim(),
                email = binding.etEmail.text.toString().trim().lowercase(),
                phone = binding.etPhone.text.toString().trim(),
                village = binding.etVillage.text.toString().trim(),
                primarySkill = binding.actvPrimarySkill.text.toString().trim(),
                secondarySkills = "",
                experienceYears = 0,
                passwordHash = SecurityUtils.hashPassword(plainPassword)
            )
            binding.btnRegister.isEnabled = false
            viewModel.register(user, plainPassword)
        }

        observeViewModel()
    }

    private fun observeViewModel() {
        lifecycleScope.launch {
            viewModel.authState.collect { state ->
                when (state) {
                    is AuthState.Success -> {
                        SessionManager(requireContext()).saveSession(state.userId)
                        findNavController().navigate(R.id.action_register_to_dashboard)
                    }
                    is AuthState.Error -> {
                        binding.btnRegister.isEnabled = true
                        Snackbar.make(binding.root, state.message, Snackbar.LENGTH_LONG).show()
                    }
                    is AuthState.Loading -> {
                        binding.btnRegister.isEnabled = false
                    }
                    else -> {}
                }
            }
        }
    }

    private fun isFormValid(): Boolean {
        val name = binding.etFullName.text.toString().trim()
        val email = binding.etEmail.text.toString().trim()
        val phone = binding.etPhone.text.toString().trim()
        val pass = binding.etPassword.text.toString()
        val confirm = binding.etConfirmPassword.text.toString()
        val village = binding.etVillage.text.toString().trim()
        val skill = binding.actvPrimarySkill.text.toString().trim()
        val passHasLetter = pass.any { it.isLetter() }
        val passHasDigit = pass.any { it.isDigit() }
        return name.length in 2..50 &&
                Patterns.EMAIL_ADDRESS.matcher(email).matches() &&
                phone.length == 10 && phone.all { it.isDigit() } &&
                pass.length >= 8 && passHasLetter && passHasDigit &&
                pass == confirm &&
                village.isNotEmpty() &&
                skill.isNotEmpty()
    }

    private fun validateAll(): Boolean {
        var valid = true
        val name = binding.etFullName.text.toString().trim()
        if (name.length < 2 || name.length > 50) {
            binding.tilFullName.error = "2–50 characters required"
            valid = false
        } else binding.tilFullName.error = null

        val email = binding.etEmail.text.toString().trim()
        if (!Patterns.EMAIL_ADDRESS.matcher(email).matches()) {
            binding.tilEmail.error = "Enter a valid email"
            valid = false
        } else binding.tilEmail.error = null

        val phone = binding.etPhone.text.toString().trim()
        if (phone.length != 10 || !phone.all { it.isDigit() }) {
            binding.tilPhone.error = "Enter 10-digit phone number"
            valid = false
        } else binding.tilPhone.error = null

        val pass = binding.etPassword.text.toString()
        if (pass.length < 8 || !pass.any { it.isLetter() } || !pass.any { it.isDigit() }) {
            binding.tilPassword.error = "Min 8 chars with 1 letter and 1 digit"
            valid = false
        } else binding.tilPassword.error = null

        val confirm = binding.etConfirmPassword.text.toString()
        if (pass != confirm) {
            binding.tilConfirmPassword.error = "Passwords do not match"
            valid = false
        } else binding.tilConfirmPassword.error = null

        if (binding.etVillage.text.toString().trim().isEmpty()) {
            binding.tilVillage.error = "Village is required"
            valid = false
        } else binding.tilVillage.error = null

        if (binding.actvPrimarySkill.text.toString().trim().isEmpty()) {
            binding.tilPrimarySkill.error = "Select a skill"
            valid = false
        } else binding.tilPrimarySkill.error = null

        return valid
    }

    private fun setupValidation() {
        val watcher = object : TextWatcher {
            override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {}
            override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {}
            override fun afterTextChanged(s: Editable?) { binding.btnRegister.isEnabled = isFormValid() }
        }
        binding.etFullName.addTextChangedListener(watcher)
        binding.etEmail.addTextChangedListener(watcher)
        binding.etPhone.addTextChangedListener(watcher)
        binding.etPassword.addTextChangedListener(watcher)
        binding.etConfirmPassword.addTextChangedListener(watcher)
        binding.etVillage.addTextChangedListener(watcher)
        binding.actvPrimarySkill.addTextChangedListener(watcher)
    }

    override fun onDestroyView() { super.onDestroyView(); _binding = null }
}

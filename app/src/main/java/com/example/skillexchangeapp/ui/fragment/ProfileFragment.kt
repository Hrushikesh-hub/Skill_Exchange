package com.example.skillexchangeapp.ui.fragment
// ProfileFragment — with badge rendering via BadgeEngine

import android.graphics.Color
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.lifecycleScope
import androidx.lifecycle.repeatOnLifecycle
import androidx.navigation.fragment.findNavController
import com.example.skillexchangeapp.R
import com.example.skillexchangeapp.SkillExchangeApplication
import com.example.skillexchangeapp.ai.BadgeEngine
import com.example.skillexchangeapp.ai.TrustScoreEngine
import com.example.skillexchangeapp.data.local.entity.User
import com.example.skillexchangeapp.data.local.entity.VerificationRequest
import com.example.skillexchangeapp.databinding.FragmentProfileBinding
import com.example.skillexchangeapp.ui.viewmodel.MainViewModel
import com.example.skillexchangeapp.ui.viewmodel.ViewModelFactory
import com.example.skillexchangeapp.utils.SessionManager
import com.google.android.material.bottomsheet.BottomSheetDialog
import com.google.android.material.button.MaterialButton
import com.google.android.material.snackbar.Snackbar
import com.google.android.material.textfield.TextInputEditText
import kotlinx.coroutines.launch
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

class ProfileFragment : Fragment() {

    private var _binding: FragmentProfileBinding? = null
    private val binding get() = _binding!!

    private val viewModel: MainViewModel by viewModels {
        ViewModelFactory((requireActivity().application as SkillExchangeApplication).repository)
    }

    private var currentUser: User? = null
    private lateinit var sessionManager: SessionManager

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View {
        _binding = FragmentProfileBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        binding.toolbar.setNavigationOnClickListener {
            requireActivity().onBackPressedDispatcher.onBackPressed()
        }

        sessionManager = SessionManager(requireContext())
        val userId = sessionManager.getUserId()

        viewLifecycleOwner.lifecycleScope.launch {
            viewLifecycleOwner.repeatOnLifecycle(Lifecycle.State.STARTED) {
                viewModel.getUser(userId).collect { user ->
                    user?.let { bindUser(it) }
                }
            }
        }

        viewModel.getUserStats(userId) { _, _, reviewCount ->
            activity?.runOnUiThread {
                binding.tvStatReviews.text = "$reviewCount reviews"
            }
        }

        binding.btnUpdateProfile.setOnClickListener {
            currentUser?.let {
                val updated = it.copy(
                    fullName = binding.etFullName.text.toString().trim(),
                    phone = binding.etPhone.text.toString().trim(),
                    village = binding.etVillage.text.toString().trim(),
                    primarySkill = binding.etPrimarySkill.text.toString().trim(),
                    bio = binding.etBio.text.toString().trim(),
                    isAvailable = binding.switchAvailable.isChecked
                )
                viewModel.updateUser(updated)
                Toast.makeText(context, "Profile Updated ✅", Toast.LENGTH_SHORT).show()
            }
        }

        binding.btnWalletFromProfile.setOnClickListener { findNavController().navigate(R.id.action_profile_to_wallet) }
        binding.btnViewHistory.setOnClickListener { findNavController().navigate(R.id.action_profile_to_history) }
        binding.btnSettings.setOnClickListener { findNavController().navigate(R.id.action_profile_to_settings) }
        binding.btnLogout.setOnClickListener {
            sessionManager.logout()
            com.example.skillexchangeapp.data.firebase.FirebaseService.signOut()
            findNavController().navigate(R.id.action_profile_to_login)
        }
    }

    override fun onResume() {
        super.onResume()
        val userId = sessionManager.getUserId()
        viewModel.loadUserById(userId)
    }

    private fun bindUser(user: User) {
        currentUser = user

        binding.tvProfileName.text = user.fullName
        binding.tvProfileEmail.text = user.email

        val parts = user.fullName.split(" ")
        val initials = if (parts.size >= 2) "${parts[0][0]}${parts[1][0]}" else "${parts[0][0]}"
        binding.tvInitials.text = initials.uppercase()

        binding.tvStatPoints.text = "${user.skillPoints}"
        binding.tvStatRating.text = String.format("%.1f★", user.trustScore)
        binding.tvStatSwaps.text = "${user.completedSwaps}"

        // Trust label
        binding.tvTrustLabel.text = TrustScoreEngine.trustLabel(user)

        // Reliability progress bar
        binding.progressReliability.progress = user.reliabilityScore
        binding.tvReliabilityLabel.text = "Reliability: ${user.reliabilityScore}%"
        val color = when {
            user.reliabilityScore >= 80 -> Color.parseColor("#388E3C")
            user.reliabilityScore >= 50 -> Color.parseColor("#F57C00")
            else -> Color.parseColor("#D32F2F")
        }
        binding.progressReliability.progressTintList = android.content.res.ColorStateList.valueOf(color)

        // Member since
        val sdf = SimpleDateFormat("MMM yyyy", Locale.getDefault())
        binding.tvMemberSince.text = "Member since ${sdf.format(Date(user.createdAt))}"

        // Editable fields
        binding.etFullName.setText(user.fullName)
        binding.etPhone.setText(user.phone)
        binding.etVillage.setText(user.village)
        binding.etPrimarySkill.setText(user.primarySkill)
        binding.etBio.setText(user.bio)
        binding.switchAvailable.isChecked = user.isAvailable

        // Verification section
        updateVerificationSection(user)

        // Badges
        renderBadges(user)
    }

    private fun renderBadges(user: User) {
        val badges = BadgeEngine.getBadges(user)
        binding.layoutBadges.removeAllViews()
        for (badge in badges) {
            val chip = com.google.android.material.chip.Chip(requireContext()).apply {
                if (badge.earned) {
                    text = "${badge.emoji} ${badge.title}"
                    chipBackgroundColor = android.content.res.ColorStateList.valueOf(Color.parseColor("#E8F5E9"))
                    setTextColor(Color.parseColor("#388E3C"))
                } else {
                    text = "🔒 Locked"
                    chipBackgroundColor = android.content.res.ColorStateList.valueOf(Color.parseColor("#F5F5F5"))
                    setTextColor(Color.parseColor("#9E9E9E"))
                }
                isClickable = false
            }
            binding.layoutBadges.addView(chip)
        }
    }

    private fun updateVerificationSection(user: User) {
        binding.layoutUnverified.visibility = View.GONE
        binding.chipVerificationPending.visibility = View.GONE
        binding.chipVerificationVerified.visibility = View.GONE
        binding.layoutRejected.visibility = View.GONE

        when (user.verificationStatus) {
            "Unverified" -> {
                binding.layoutUnverified.visibility = View.VISIBLE
                binding.btnRequestVerification.setOnClickListener {
                    showVerificationBottomSheet(user)
                }
            }
            "Pending" -> {
                binding.chipVerificationPending.visibility = View.VISIBLE
            }
            "Verified" -> {
                binding.chipVerificationVerified.visibility = View.VISIBLE
            }
            "Rejected" -> {
                binding.layoutRejected.visibility = View.VISIBLE
                // Show rejection notes if available (fetched from latest VerificationRequest)
                viewLifecycleOwner.lifecycleScope.launch {
                    val repo = (requireActivity().application as SkillExchangeApplication).repository
                    repo.getVerificationRequestsForUser(user.id).collect { requests ->
                        val latest = requests.lastOrNull()
                        activity?.runOnUiThread {
                            binding.tvRejectionNotes.text = latest?.reviewerNotes ?: "No notes provided"
                        }
                    }
                }
                binding.btnReapply.setOnClickListener { showVerificationBottomSheet(user) }
            }
        }
    }

    private fun showVerificationBottomSheet(user: User) {
        val dialog = BottomSheetDialog(requireContext())
        val dialogView = layoutInflater.inflate(R.layout.bottom_sheet_verification, null)
        dialog.setContentView(dialogView)

        val etSkill = dialogView.findViewById<TextInputEditText>(R.id.etVerifSkill)
        val etProof = dialogView.findViewById<TextInputEditText>(R.id.etVerifProof)
        val btnSubmit = dialogView.findViewById<MaterialButton>(R.id.btnSubmitVerification)

        etSkill?.setText(user.primarySkill)

        btnSubmit?.setOnClickListener {
            val skill = etSkill?.text.toString().trim()
            val proof = etProof?.text.toString().trim()
            if (skill.isEmpty()) {
                Toast.makeText(context, "Skill is required", Toast.LENGTH_SHORT).show()
                return@setOnClickListener
            }
            if (proof.length < 50) {
                Toast.makeText(context, "Proof must be at least 50 characters", Toast.LENGTH_SHORT).show()
                return@setOnClickListener
            }
            viewModel.requestVerification(user.id, skill, proof)
            dialog.dismiss()
            Snackbar.make(binding.root, "Verification request submitted ✅", Snackbar.LENGTH_LONG).show()
        }

        dialog.show()
    }

    override fun onDestroyView() { super.onDestroyView(); _binding = null }
}

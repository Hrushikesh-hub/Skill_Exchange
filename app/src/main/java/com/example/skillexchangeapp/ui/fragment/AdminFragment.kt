package com.example.skillexchangeapp.ui.fragment

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.EditText
import android.widget.Toast
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.lifecycleScope
import androidx.lifecycle.repeatOnLifecycle
import androidx.navigation.fragment.findNavController
import androidx.recyclerview.widget.LinearLayoutManager
import com.example.skillexchangeapp.SkillExchangeApplication
import com.example.skillexchangeapp.data.local.entity.VerificationRequest
import com.example.skillexchangeapp.databinding.FragmentAdminBinding
import com.example.skillexchangeapp.ui.adapter.AdminActionAdapter
import com.example.skillexchangeapp.ui.adapter.ReportAdapter
import com.example.skillexchangeapp.ui.adapter.VerificationRequestAdapter
import com.example.skillexchangeapp.ui.adapter.VerificationRequestItem
import com.example.skillexchangeapp.ui.viewmodel.MainViewModel
import com.example.skillexchangeapp.ui.viewmodel.ViewModelFactory
import com.example.skillexchangeapp.utils.SessionManager
import com.google.android.material.dialog.MaterialAlertDialogBuilder
import com.google.android.material.tabs.TabLayout
import kotlinx.coroutines.launch

class AdminFragment : Fragment() {

    private var _binding: FragmentAdminBinding? = null
    private val binding get() = _binding!!

    private val viewModel: MainViewModel by viewModels {
        ViewModelFactory((requireActivity().application as SkillExchangeApplication).repository)
    }

    private lateinit var verifAdapter: VerificationRequestAdapter
    private lateinit var reportAdapter: ReportAdapter
    private lateinit var actionAdapter: AdminActionAdapter
    private var adminId: Long = -1L
    private var allVerifItems: List<VerificationRequestItem> = emptyList()

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View {
        _binding = FragmentAdminBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        binding.toolbar.setNavigationOnClickListener { requireActivity().onBackPressedDispatcher.onBackPressed() }

        adminId = SessionManager(requireContext()).getUserId()

        // Guard: non-admin access denied
        viewLifecycleOwner.lifecycleScope.launch {
            val user = (requireActivity().application as SkillExchangeApplication).repository.getUserByIdSync(adminId)
            if (user == null || !user.isAdmin) {
                Toast.makeText(context, "Access Denied", Toast.LENGTH_SHORT).show()
                findNavController().popBackStack()
                return@launch
            }
        }

        setupAdapters()
        setupTabs()
        observeData()
    }

    private fun setupAdapters() {
        verifAdapter = VerificationRequestAdapter(
            onApprove = { req -> showApproveConfirm(req) },
            onReject = { req -> showRejectDialog(req) }
        )
        reportAdapter = ReportAdapter { report ->
            val input = EditText(requireContext()).apply { hint = "Resolution notes..." }
            MaterialAlertDialogBuilder(requireContext())
                .setTitle("Resolve Report #${report.id}")
                .setView(input)
                .setPositiveButton("Resolve") { _, _ ->
                    val notes = input.text.toString().ifBlank { "Resolved by admin" }
                    viewModel.resolveReport(report, adminId, notes)
                    Toast.makeText(context, "Report resolved", Toast.LENGTH_SHORT).show()
                }
                .setNegativeButton("Cancel", null)
                .show()
        }
        actionAdapter = AdminActionAdapter()

        binding.rvVerifications.layoutManager = LinearLayoutManager(context)
        binding.rvVerifications.adapter = verifAdapter
        binding.rvReports.layoutManager = LinearLayoutManager(context)
        binding.rvReports.adapter = reportAdapter
        binding.rvAdminLog.layoutManager = LinearLayoutManager(context)
        binding.rvAdminLog.adapter = actionAdapter
    }

    private fun setupTabs() {
        showTab(0)
        binding.tabLayout.addOnTabSelectedListener(object : TabLayout.OnTabSelectedListener {
            override fun onTabSelected(tab: TabLayout.Tab?) { showTab(tab?.position ?: 0) }
            override fun onTabUnselected(tab: TabLayout.Tab?) {}
            override fun onTabReselected(tab: TabLayout.Tab?) {}
        })

        // Filter chips for verifications
        binding.chipVerifAll.setOnCheckedChangeListener { _, checked -> if (checked) filterVerif("all") }
        binding.chipVerifPending.setOnCheckedChangeListener { _, checked -> if (checked) filterVerif("Pending") }
        binding.chipVerifApproved.setOnCheckedChangeListener { _, checked -> if (checked) filterVerif("Approved") }
        binding.chipVerifRejected.setOnCheckedChangeListener { _, checked -> if (checked) filterVerif("Rejected") }
    }

    private fun showTab(index: Int) {
        binding.layoutVerifications.visibility = if (index == 0) View.VISIBLE else View.GONE
        binding.rvReports.visibility = if (index == 1) View.VISIBLE else View.GONE
        binding.rvAdminLog.visibility = if (index == 2) View.VISIBLE else View.GONE
    }

    private fun filterVerif(status: String) {
        val filtered = if (status == "all") allVerifItems
        else allVerifItems.filter { it.request.status == status }
        verifAdapter.submitList(filtered)
    }

    private fun observeData() {
        viewLifecycleOwner.lifecycleScope.launch {
            viewLifecycleOwner.repeatOnLifecycle(Lifecycle.State.STARTED) {
                viewModel.getAllVerificationRequests().collect { requests ->
                    val items = requests.map { req ->
                        val user = (requireActivity().application as SkillExchangeApplication)
                            .repository.getUserByIdSync(req.userId)
                        VerificationRequestItem(req, user?.fullName ?: "User #${req.userId}")
                    }
                    allVerifItems = items
                    filterVerif(currentVerifFilter())
                }
            }
        }

        viewLifecycleOwner.lifecycleScope.launch {
            viewLifecycleOwner.repeatOnLifecycle(Lifecycle.State.STARTED) {
                viewModel.getAllReports().collect { reports ->
                    reportAdapter.submitList(reports)
                }
            }
        }

        viewLifecycleOwner.lifecycleScope.launch {
            viewLifecycleOwner.repeatOnLifecycle(Lifecycle.State.STARTED) {
                viewModel.getRecentAdminActions().collect { actions ->
                    actionAdapter.submitList(actions)
                }
            }
        }
    }

    private fun currentVerifFilter(): String = when {
        binding.chipVerifPending.isChecked -> "Pending"
        binding.chipVerifApproved.isChecked -> "Approved"
        binding.chipVerifRejected.isChecked -> "Rejected"
        else -> "all"
    }

    private fun showApproveConfirm(req: VerificationRequest) {
        MaterialAlertDialogBuilder(requireContext())
            .setTitle("Approve Verification")
            .setMessage("Approve ${req.skill} skill for User #${req.userId}?")
            .setPositiveButton("Approve") { _, _ ->
                viewModel.reviewVerificationRequest(req, adminId, true, "Approved by coordinator after proof review")
                Toast.makeText(context, "Worker verified ✅", Toast.LENGTH_SHORT).show()
            }
            .setNegativeButton("Cancel", null)
            .show()
    }

    private fun showRejectDialog(req: VerificationRequest) {
        val input = EditText(requireContext()).apply { hint = "Reason for rejection..." }
        MaterialAlertDialogBuilder(requireContext())
            .setTitle("Reject Verification")
            .setView(input)
            .setPositiveButton("Reject") { _, _ ->
                val notes = input.text.toString().ifBlank { "Proof is incomplete" }
                viewModel.reviewVerificationRequest(req, adminId, false, notes)
                Toast.makeText(context, "Verification rejected", Toast.LENGTH_SHORT).show()
            }
            .setNegativeButton("Cancel", null)
            .show()
    }

    override fun onDestroyView() { super.onDestroyView(); _binding = null }
}

package com.example.skillexchangeapp.ui.fragment

import android.app.DatePickerDialog
import android.app.TimePickerDialog
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.lifecycle.lifecycleScope
import androidx.recyclerview.widget.LinearLayoutManager
import com.example.skillexchangeapp.SkillExchangeApplication
import com.example.skillexchangeapp.databinding.FragmentOfferManagementBinding
import com.example.skillexchangeapp.ui.adapter.OfferAdapter
import com.example.skillexchangeapp.ui.adapter.OfferWithDetails
import com.example.skillexchangeapp.ui.viewmodel.MainViewModel
import com.example.skillexchangeapp.ui.viewmodel.ViewModelFactory
import com.example.skillexchangeapp.utils.SessionManager
import com.google.android.material.dialog.MaterialAlertDialogBuilder
import kotlinx.coroutines.launch
import java.text.SimpleDateFormat
import java.util.*

class OfferManagementFragment : Fragment() {

    private var _binding: FragmentOfferManagementBinding? = null
    private val binding get() = _binding!!

    private val viewModel: MainViewModel by viewModels {
        ViewModelFactory((requireActivity().application as SkillExchangeApplication).repository)
    }

    private lateinit var adapter: OfferAdapter

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View {
        _binding = FragmentOfferManagementBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        binding.toolbar.setNavigationOnClickListener {
            requireActivity().onBackPressedDispatcher.onBackPressed()
        }

        adapter = OfferAdapter(
            onAccept = { offerWithDetails -> showScheduleDialog(offerWithDetails) },
            onReject = { offerWithDetails ->
                MaterialAlertDialogBuilder(requireContext())
                    .setTitle("Reject Offer")
                    .setMessage("Reject offer from ${offerWithDetails.offerer.fullName}?")
                    .setPositiveButton("Reject") { _, _ ->
                        viewModel.rejectOffer(offerWithDetails.offer)
                        Toast.makeText(context, "Offer rejected", Toast.LENGTH_SHORT).show()
                    }
                    .setNegativeButton("Cancel", null)
                    .show()
            }
        )

        binding.rvOffers.layoutManager = LinearLayoutManager(context)
        binding.rvOffers.adapter = adapter

        val userId = SessionManager(requireContext()).getUserId()
        lifecycleScope.launch {
            viewModel.getPendingOffersWithDetails(userId).collect { offers ->
                adapter.submitList(offers)
                binding.tvOfferSummary.text = "${offers.size} pending offer${if (offers.size != 1) "s" else ""} to review"
                if (offers.isEmpty()) {
                    binding.emptyState.visibility = View.VISIBLE
                    binding.rvOffers.visibility = View.GONE
                } else {
                    binding.emptyState.visibility = View.GONE
                    binding.rvOffers.visibility = View.VISIBLE
                }
            }
        }
    }

    private fun showScheduleDialog(offerWithDetails: OfferWithDetails) {
        val options = arrayOf("Accept & Schedule Date/Time", "Accept Now (Start Immediately)")
        MaterialAlertDialogBuilder(requireContext())
            .setTitle("Accept Offer from ${offerWithDetails.offerer.fullName}")
            .setItems(options) { _, which ->
                when (which) {
                    0 -> pickScheduleDateTime(offerWithDetails)
                    1 -> {
                        viewModel.acceptOffer(offerWithDetails.offer, null, null) {
                            activity?.runOnUiThread {
                                Toast.makeText(context, "Offer accepted! Swap started \uD83C\uDF89", Toast.LENGTH_SHORT).show()
                            }
                        }
                    }
                }
            }
            .setNegativeButton("Cancel", null)
            .show()
    }

    private fun pickScheduleDateTime(offerWithDetails: OfferWithDetails) {
        val calendar = Calendar.getInstance()
        DatePickerDialog(requireContext(), { _, year, month, day ->
            calendar.set(year, month, day)
            TimePickerDialog(requireContext(), { _, hour, minute ->
                calendar.set(Calendar.HOUR_OF_DAY, hour)
                calendar.set(Calendar.MINUTE, minute)
                val fmt = SimpleDateFormat("hh:mm a", Locale.getDefault())
                val timeStr = fmt.format(calendar.time)
                viewModel.acceptOffer(offerWithDetails.offer, calendar.timeInMillis, timeStr) {
                    activity?.runOnUiThread {
                        Toast.makeText(context, "Accepted & Scheduled! \uD83D\uDCC5", Toast.LENGTH_SHORT).show()
                    }
                }
            }, calendar.get(Calendar.HOUR_OF_DAY), calendar.get(Calendar.MINUTE), false).show()
        }, calendar.get(Calendar.YEAR), calendar.get(Calendar.MONTH), calendar.get(Calendar.DAY_OF_MONTH)).apply {
            datePicker.minDate = System.currentTimeMillis()
        }.show()
    }

    override fun onDestroyView() { super.onDestroyView(); _binding = null }
}

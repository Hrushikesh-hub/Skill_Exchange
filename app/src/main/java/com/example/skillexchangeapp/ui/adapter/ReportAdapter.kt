package com.example.skillexchangeapp.ui.adapter

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import com.example.skillexchangeapp.data.local.entity.Report
import com.example.skillexchangeapp.databinding.ItemReportBinding

class ReportAdapter(
    private val onResolve: (Report) -> Unit
) : ListAdapter<Report, ReportAdapter.ReportViewHolder>(DIFF) {

    companion object {
        private val DIFF = object : DiffUtil.ItemCallback<Report>() {
            override fun areItemsTheSame(a: Report, b: Report) = a.id == b.id
            override fun areContentsTheSame(a: Report, b: Report) = a == b
        }
    }

    inner class ReportViewHolder(private val binding: ItemReportBinding) :
        RecyclerView.ViewHolder(binding.root) {
        fun bind(report: Report) {
            binding.tvReporterLabel.text = "Reporter ID: ${report.reporterId}"
            binding.tvReportedLabel.text = "Reported ID: ${report.reportedUserId ?: "N/A"}"
            binding.tvReason.text = "Reason: ${report.reason}"
            binding.tvDetails.text = report.details
            binding.chipStatus.text = report.status
            if (report.status == "Open") {
                binding.btnResolve.isEnabled = true
                binding.btnResolve.setOnClickListener { onResolve(report) }
            } else {
                binding.btnResolve.isEnabled = false
            }
        }
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ReportViewHolder {
        val binding = ItemReportBinding.inflate(LayoutInflater.from(parent.context), parent, false)
        return ReportViewHolder(binding)
    }

    override fun onBindViewHolder(holder: ReportViewHolder, position: Int) {
        holder.bind(getItem(position))
    }
}

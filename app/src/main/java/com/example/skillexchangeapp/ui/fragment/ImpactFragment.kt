package com.example.skillexchangeapp.ui.fragment

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import com.example.skillexchangeapp.SkillExchangeApplication
import com.example.skillexchangeapp.databinding.FragmentImpactBinding
import com.example.skillexchangeapp.ui.viewmodel.MainViewModel
import com.example.skillexchangeapp.ui.viewmodel.ViewModelFactory
import com.example.skillexchangeapp.utils.DemoScenarioSeeder

class ImpactFragment : Fragment() {
    private var _binding: FragmentImpactBinding? = null
    private val binding get() = _binding!!

    private val viewModel: MainViewModel by viewModels {
        ViewModelFactory((requireActivity().application as SkillExchangeApplication).repository)
    }

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View {
        _binding = FragmentImpactBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        binding.toolbar.setNavigationOnClickListener { requireActivity().onBackPressedDispatcher.onBackPressed() }
        binding.tvDemoStory.text = DemoScenarioSeeder.storySteps().mapIndexed { index, step -> "${index + 1}. $step" }.joinToString("\n\n")

        viewModel.getImpactSnapshot { impact ->
            activity?.runOnUiThread {
                binding.tvTotalHours.text = "${impact.totalHours}h\nHours exchanged"
                binding.tvMoneySaved.text = "Rs ${impact.moneySavedEstimate}\nValue saved"
                binding.tvActiveWorkers.text = "${impact.activeWorkers}\nActive workers"
                binding.tvCompletedSwaps.text = "${impact.completedSwaps}\nCompleted swaps"
                binding.tvTopSkill.text = "${impact.topSkill}\nTop skill"
                binding.tvAverageTrust.text = "${"%.1f".format(impact.averageTrust)}\nAvg trust"
                binding.tvOpenNeeds.text = "Open needs: ${impact.openNeeds}"
            }
        }
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}

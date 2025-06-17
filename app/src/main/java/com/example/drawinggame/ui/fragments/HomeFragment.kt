package com.example.drawinggame.ui.fragments

import android.content.Intent
import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.navigation.fragment.findNavController
import com.example.drawinggame.R
import com.example.drawinggame.databinding.FragmentHomeBinding
import com.example.drawinggame.ui.activities.DrawingActivity
import com.google.android.material.dialog.MaterialAlertDialogBuilder

class HomeFragment : BaseFragment<FragmentHomeBinding>() {
    
    override fun getViewBinding(inflater: LayoutInflater, container: ViewGroup?): FragmentHomeBinding {
        return FragmentHomeBinding.inflate(inflater, container, false)
    }
    
    override fun setupUI() {
        setupClickListeners()
        loadDailyPrompt()
    }
    
    override fun setupObservers() {
        // TODO: Observe daily prompt data from ViewModel
    }
    
    private fun setupClickListeners() {
        binding.startDrawingButton.setOnClickListener {
            findNavController().navigate(R.id.action_home_to_drawing)
        }
        
        binding.galleryButton.setOnClickListener {
            findNavController().navigate(R.id.action_home_to_gallery)
        }
        
        binding.profileButton.setOnClickListener {
            findNavController().navigate(R.id.action_home_to_profile)
        }
        
        // Temporary: Native demo button
        binding.nativeDemoButton.setOnClickListener {
            // Keep original functionality for gradual migration
            val intent = Intent(requireContext(), DrawingActivity::class.java)
            startActivity(intent)
        }
    }
    
    private fun loadDailyPrompt() {
        // TODO: Load from ViewModel in Phase 9
        // No prompt text in new design
    }
    
    private fun showFeatureComingSoon(feature: String) {
        MaterialAlertDialogBuilder(requireContext())
            .setTitle("Coming Soon")
            .setMessage("$feature feature will be available in a future update!")
            .setPositiveButton("OK", null)
            .show()
    }
}
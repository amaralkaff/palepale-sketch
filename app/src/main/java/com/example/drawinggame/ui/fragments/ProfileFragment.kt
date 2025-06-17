package com.example.drawinggame.ui.fragments

import android.view.LayoutInflater
import android.view.ViewGroup
import com.example.drawinggame.databinding.FragmentProfileBinding

class ProfileFragment : BaseFragment<FragmentProfileBinding>() {
    
    override fun getViewBinding(inflater: LayoutInflater, container: ViewGroup?): FragmentProfileBinding {
        return FragmentProfileBinding.inflate(inflater, container, false)
    }
    
    override fun setupUI() {
        // TODO: Phase 8 - Profile implementation
    }
    
    override fun setupObservers() {
        // TODO: Phase 8 - Profile observers
    }
}
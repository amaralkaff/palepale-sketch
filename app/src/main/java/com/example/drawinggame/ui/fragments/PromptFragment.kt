package com.example.drawinggame.ui.fragments

import android.view.LayoutInflater
import android.view.ViewGroup
import com.example.drawinggame.databinding.FragmentPromptBinding

class PromptFragment : BaseFragment<FragmentPromptBinding>() {
    
    override fun getViewBinding(inflater: LayoutInflater, container: ViewGroup?): FragmentPromptBinding {
        return FragmentPromptBinding.inflate(inflater, container, false)
    }
    
    override fun setupUI() {
        // TODO: Phase 9 - Prompt implementation
    }
    
    override fun setupObservers() {
        // TODO: Phase 9 - Prompt observers
    }
}
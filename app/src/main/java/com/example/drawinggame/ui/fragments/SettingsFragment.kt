package com.example.drawinggame.ui.fragments

import android.view.LayoutInflater
import android.view.ViewGroup
import com.example.drawinggame.databinding.FragmentSettingsBinding

class SettingsFragment : BaseFragment<FragmentSettingsBinding>() {
    
    override fun getViewBinding(inflater: LayoutInflater, container: ViewGroup?): FragmentSettingsBinding {
        return FragmentSettingsBinding.inflate(inflater, container, false)
    }
    
    override fun setupUI() {
        // TODO: Future implementation
    }
    
    override fun setupObservers() {
        // TODO: Future implementation
    }
}
package com.example.drawinggame.ui.fragments

import android.view.LayoutInflater
import android.view.ViewGroup
import com.example.drawinggame.databinding.FragmentDrawingBinding

class DrawingFragment : BaseFragment<FragmentDrawingBinding>() {
    
    override fun getViewBinding(inflater: LayoutInflater, container: ViewGroup?): FragmentDrawingBinding {
        return FragmentDrawingBinding.inflate(inflater, container, false)
    }
    
    override fun setupUI() {
        // TODO: Phase 4 - Drawing implementation
    }
    
    override fun setupObservers() {
        // TODO: Phase 4 - Drawing observers
    }
}
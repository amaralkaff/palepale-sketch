package com.example.drawinggame.ui.fragments

import android.view.LayoutInflater
import android.view.ViewGroup
import com.example.drawinggame.databinding.FragmentDrawingDetailBinding

class DrawingDetailFragment : BaseFragment<FragmentDrawingDetailBinding>() {
    
    override fun getViewBinding(inflater: LayoutInflater, container: ViewGroup?): FragmentDrawingDetailBinding {
        return FragmentDrawingDetailBinding.inflate(inflater, container, false)
    }
    
    override fun setupUI() {
        // TODO: Phase 8 - Drawing detail implementation
    }
    
    override fun setupObservers() {
        // TODO: Phase 8 - Drawing detail observers
    }
}
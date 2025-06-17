package com.example.drawinggame.ui.fragments

import android.view.LayoutInflater
import android.view.ViewGroup
import com.example.drawinggame.databinding.FragmentGalleryBinding

class GalleryFragment : BaseFragment<FragmentGalleryBinding>() {
    
    override fun getViewBinding(inflater: LayoutInflater, container: ViewGroup?): FragmentGalleryBinding {
        return FragmentGalleryBinding.inflate(inflater, container, false)
    }
    
    override fun setupUI() {
        // TODO: Phase 8 - Gallery implementation
    }
    
    override fun setupObservers() {
        // TODO: Phase 8 - Gallery observers
    }
}
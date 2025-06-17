package com.example.drawinggame.ui.activities

import android.os.Bundle
import android.widget.Button
import androidx.appcompat.app.AppCompatActivity

class DrawingActivity : AppCompatActivity() {
    
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        
        // Use the existing simple placeholder layout
        setContentView(com.example.drawinggame.R.layout.activity_drawing_placeholder)
        
        // Add back button functionality
        findViewById<Button>(com.example.drawinggame.R.id.backButton)?.setOnClickListener {
            finish()
        }
    }
}
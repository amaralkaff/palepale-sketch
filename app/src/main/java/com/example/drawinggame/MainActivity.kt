package com.example.drawinggame

import android.content.Intent
import android.os.Bundle
import android.widget.Button
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import com.example.drawinggame.ui.activities.DrawingActivity

class MainActivity : AppCompatActivity() {
    
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        
        // Use simple layout for now to test basic functionality
        setContentView(R.layout.activity_main_simple)
        
        // Simple button setup
        findViewById<Button>(R.id.startDrawingButton).setOnClickListener {
            val intent = Intent(this, DrawingActivity::class.java)
            startActivity(intent)
        }
        
        findViewById<Button>(R.id.galleryButton).setOnClickListener {
            Toast.makeText(this, "Gallery coming soon!", Toast.LENGTH_SHORT).show()
        }
        
        findViewById<Button>(R.id.profileButton).setOnClickListener {
            Toast.makeText(this, "Profile coming soon!", Toast.LENGTH_SHORT).show()
        }
    }
}
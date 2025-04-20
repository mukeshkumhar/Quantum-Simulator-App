package com.example.quantumsimulator.Activity

import android.content.Intent
import android.os.Bundle
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import com.example.quantumsimulator.R
import com.example.quantumsimulator.databinding.ActivityMainBinding

class MainActivity : AppCompatActivity() {
    private lateinit var binding: ActivityMainBinding
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityMainBinding.inflate(layoutInflater)
        setContentView(binding.root)
        enableEdgeToEdge()
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main)) { v, insets ->
            val systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars())
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom)
            insets
        }

        val codeButton = binding.codeEditButton
        codeButton.setOnClickListener {
            val intent = Intent(this, QuantumEditorActivity::class.java)
            startActivity(intent)
        }

        val circuitButton = binding.circuitButton
        circuitButton.setOnClickListener {
            val intent = Intent(this, QuantumCircuitActivity::class.java)
            startActivity(intent)
        }
    }
}
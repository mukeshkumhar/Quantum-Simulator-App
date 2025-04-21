package com.example.quantumsimulator.Activity

import android.graphics.BitmapFactory
import android.os.Bundle
import android.util.Base64
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import androidx.lifecycle.lifecycleScope
import com.example.quantumsimulator.ApiManager.RetrofitClient
import com.example.quantumsimulator.DataModels.CodeRequest
import com.example.quantumsimulator.DataModels.CodeResponse
import com.example.quantumsimulator.R
import com.example.quantumsimulator.databinding.ActivityQuantumEditorBinding
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import retrofit2.Response

class QuantumEditorActivity : AppCompatActivity() {
    private lateinit var binding: ActivityQuantumEditorBinding

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityQuantumEditorBinding.inflate(layoutInflater)
        setContentView(binding.root)
        enableEdgeToEdge()
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main)) { v, insets ->
            val systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars())
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom)
            insets
        }

        binding.backBtn.setOnClickListener {
            val intent = android.content.Intent(this, MainActivity::class.java)
            startActivity(intent)
        }

        binding.runCodeBtn.setOnClickListener {
            RunCode()
        }


    }

    private fun RunCode() {

        val code = binding.editCodeText.text.toString()

        lifecycleScope.launch {
            try {
                if (code.isNotEmpty()) {
                    val formattedCode = code
                        .replace("\\", "\\\\")
                        .replace("\t", "\\t")
                    val request = CodeRequest(formattedCode)
                    println(request)
                    val response: Response<CodeResponse> = withContext(Dispatchers.IO) {
                        RetrofitClient.instance.runCode(request)
                    }
                    withContext(Dispatchers.Main) {
                        if (response.isSuccessful && response.body() != null) {
                            val result = response.body()
                            binding.outputText.text = result?.output
                            val base64Image = result?.circuit_image
                            val decodedBytes = Base64.decode(base64Image, Base64.DEFAULT)
                            val bitmap =
                                BitmapFactory.decodeByteArray(decodedBytes, 0, decodedBytes.size)
                            binding.circuitImage.setImageBitmap(bitmap)

                        } else {
                            binding.outputText.text = "Something went wrong"
                        }
                    }

                } else {
                    binding.outputText.text = "Please enter some code"
                }
            } catch (e: Exception) {
                print("Error in RunCode: ${e.message}")
            }
        }

    }

}
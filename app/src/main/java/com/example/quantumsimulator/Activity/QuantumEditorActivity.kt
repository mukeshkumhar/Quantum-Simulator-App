package com.example.quantumsimulator.Activity

import android.annotation.SuppressLint
import android.graphics.BitmapFactory
import android.graphics.Color
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
import io.github.rosemoe.sora.langs.textmate.TextMateColorScheme
import io.github.rosemoe.sora.langs.textmate.TextMateLanguage
import io.github.rosemoe.sora.langs.textmate.registry.ThemeRegistry
import io.github.rosemoe.sora.widget.schemes.EditorColorScheme
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import retrofit2.Response
import kotlin.time.TimeSource


class QuantumEditorActivity : AppCompatActivity() {
    private lateinit var binding: ActivityQuantumEditorBinding

    @SuppressLint("SetJavaScriptEnabled")
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

        binding.runBtn.setOnClickListener {
            RunCode1()
        }

//        val webView = binding.codeEditorWebView

        val editor = binding.editor
        editor.requestFocus()
        editor.showSoftInput()  // This opens the keyboard

//        val tmLanguage = TextMateLanguage.create("source.python", true)
//        editor.setEditorLanguage(tmLanguage)
//        editor.setText("""print("Hello World1!")""".trimIndent())
        // Optional: Enable auto-indent, line numbers, etc.

        val schemee = editor.colorScheme // Or createDracula(), etc.
        schemee.setColor(EditorColorScheme.TEXT_NORMAL, Color.WHITE)
        schemee.setColor(EditorColorScheme.WHOLE_BACKGROUND, Color.parseColor("#2C322E")) // Background
        schemee.setColor(EditorColorScheme.LINE_NUMBER, Color.GRAY) // Numbers
        schemee.setColor(EditorColorScheme.LINE_BLOCK_LABEL, Color.BLUE) // Line highlight
        schemee.setColor(EditorColorScheme.LINE_NUMBER_BACKGROUND, Color.parseColor("#2C322E")) // Background for numbers)

        editor.isLineNumberEnabled = true
        editor.isWordwrap = true
        editor.isHighlightCurrentLine = true


//        webView.settings.javaScriptEnabled = true
//        webView.settings.allowFileAccess = true
//        webView.settings.domStorageEnabled = true
//        webView.settings.allowUniversalAccessFromFileURLs = true
//        webView.settings.allowFileAccessFromFileURLs = true

//
//
//        webView.webChromeClient = WebChromeClient()
//        webView.webViewClient = WebViewClient()
//
//        webView.loadUrl("file:///android_asset/editor/index.html")

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

    private fun RunCode1() {

        val code = binding.editor.text.toString()  // 🔄 changed from EditText to CodeEditor

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
                            binding.outputText1.text = result?.output
                            val base64Image = result?.circuit_image
                            val decodedBytes = Base64.decode(base64Image, Base64.DEFAULT)
                            val bitmap =
                                BitmapFactory.decodeByteArray(decodedBytes, 0, decodedBytes.size)
                            binding.circuitImage.setImageBitmap(bitmap)

                        } else {
                            binding.outputText1.text = "Something went wrong"
                        }
                    }

                } else {
                    binding.outputText1.text = "Please enter some code"
                }
            } catch (e: Exception) {
                print("Error in RunCode: ${e.message}")
            }
        }

    }


}
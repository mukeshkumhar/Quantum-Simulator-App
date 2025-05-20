package com.example.quantumsimulator.Activity

import android.annotation.SuppressLint
import android.graphics.BitmapFactory
import android.graphics.Color
import android.graphics.Typeface
import android.os.Bundle
import android.util.Base64
import android.view.View
import android.widget.ImageView
import android.widget.Toast
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import androidx.lifecycle.lifecycleScope
import androidx.recyclerview.widget.GridLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.example.quantumsimulator.Adapter.UnitaryMatrixAdapter
import com.example.quantumsimulator.ApiManager.RetrofitClient
import com.example.quantumsimulator.DataModels.CodeRequest
import com.example.quantumsimulator.DataModels.CodeResponse
import com.example.quantumsimulator.R
import com.example.quantumsimulator.databinding.ActivityQuantumEditorBinding
import com.github.mikephil.charting.charts.BarChart
import com.github.mikephil.charting.components.XAxis
import com.github.mikephil.charting.data.BarData
import com.github.mikephil.charting.data.BarDataSet
import com.github.mikephil.charting.data.BarEntry
import com.github.mikephil.charting.formatter.IndexAxisValueFormatter
import com.github.mikephil.charting.utils.ColorTemplate
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

    private lateinit var MeasurementbarChart: BarChart
    private lateinit var statevectorChart: BarChart

    private var unitaryReal: List<List<Double>> = listOf()
    private var unitaryImag: List<List<Double>> = listOf()

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
            binding.editProgressBar.visibility = View.VISIBLE
            RunCode1()

        }

        MeasurementbarChart = binding.measurementBarChart
        statevectorChart = binding.statevectorBarChart


        val editor = binding.editor
        editor.requestFocus()
        editor.showSoftInput()  // This opens the keyboard
        editor.typefaceText = Typeface.MONOSPACE

//        val tmLanguage = TextMateLanguage.create("source.python", true)
//        editor.setEditorLanguage(tmLanguage)
//        editor.setText("""print("Hello World1!")""".trimIndent())
//         Optional: Enable auto-indent, line numbers, etc.

        val schemee = editor.colorScheme // Or createDracula(), etc.
        schemee.setColor(EditorColorScheme.TEXT_NORMAL, Color.WHITE)
        schemee.setColor(EditorColorScheme.WHOLE_BACKGROUND, Color.parseColor("#2C322E")) // Background
        schemee.setColor(EditorColorScheme.LINE_NUMBER, Color.GRAY) // Numbers
        schemee.setColor(EditorColorScheme.LINE_BLOCK_LABEL, Color.BLUE) // Line highlight
        schemee.setColor(EditorColorScheme.LINE_NUMBER_BACKGROUND, Color.parseColor("#2C322E")) // Background for numbers)

        editor.isLineNumberEnabled = true
        editor.isWordwrap = true
        editor.isHighlightCurrentLine = true

//        // Load the grammar (e.g., Python or JavaScript grammar)
//        val grammar = assets.open("textmate/Python.tmLanguage.json").bufferedReader().use { it.readText() }
//
//        // Load the Molokai theme
//        val theme = assets.open("textmate/monokai-color-theme.json").bufferedReader().use { it.readText() }

//        // Create and apply the highlighter
//        val highlighter = TextMateHighlighter()
//        highlighter.setGrammar(grammar)
//        highlighter.setTheme(theme)




        val unitaryRecycler = binding.unitaryMatrixRecycler

        binding.unitaryBtn.setOnClickListener {
            if (unitaryRecycler.visibility == View.VISIBLE){
                unitaryRecycler.visibility = View.GONE
                binding.unitaryBtn.text = "Show Unitary Matrix"
            } else {

                unitaryRecycler.visibility = View.VISIBLE
                binding.unitaryBtn.text = "Hide Unitary Matrix"
            }
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
                    println("Request From Code Editor: $request")
                    println(request)
                    val response: Response<CodeResponse> = withContext(Dispatchers.IO) {
                        RetrofitClient.instance.runCode(request)
                    }
                    withContext(Dispatchers.Main) {
                        if (response.isSuccessful && response.body() != null) {
                            val result = response.body()
                            println("Result From Code Editor API: $result")

                            if (result?.output == null){
                                binding.outputText.text = result?.error
                                println(result?.error)
                            } else {
                                binding.outputText.text = result?.output
                            }

                            val circuitBase64 = result?.circuit_image ?: ""
                            val imageView = binding.circuitImage

                            showCircuitDiagram(circuitBase64, imageView)

                            showMeasurementChart(result?.measurement_counts ?: emptyMap())
                            showStatevectorChart(result?.statevector ?: emptyList())

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
                            println("Result From Code Editor API: $result")

                            binding.outputText1.text = result?.output
                            binding.errorText.text = "Error: " + result?.error

                            binding.editProgressBar.visibility = View.GONE

                            val circuitBase64 = result?.circuit_image ?: ""
                            val imageView = binding.circuitImage

                            showCircuitDiagram(circuitBase64, imageView)

                            showMeasurementChart(result?.measurement_counts ?: emptyMap())
                            showStatevectorChart(result?.statevector ?: emptyList())

                            unitaryReal = result?.unitary_matrix_real!!
                            unitaryImag = result?.unitary_matrix_imag!!

                            unitaryMatrixView()

                        } else {
                            binding.outputText1.text = "Something went wrong"
                            binding.editProgressBar.visibility = View.GONE
                        }
                    }

                } else {
                    binding.outputText1.text = "Please enter some code"
                    binding.editProgressBar.visibility = View.GONE
                }
            } catch (e: Exception) {
                print("Error in RunCode: ${e.message}")
                binding.editProgressBar.visibility = View.GONE
            }
        }

    }
// Show Circuit Diagram
    fun showCircuitDiagram(base64Image: String, imageView: ImageView) {
        try {
            val decodedBytes = Base64.decode(base64Image, Base64.DEFAULT)
            val bitmap = BitmapFactory.decodeByteArray(decodedBytes, 0, decodedBytes.size)
            imageView.setImageBitmap(bitmap)
        } catch (e: Exception) {
            e.printStackTrace()
        }
    }

    //    Measurement BarChart
    private fun showMeasurementChart(measurementCounts: Map<String, Int>) {
        val entries = ArrayList<BarEntry>()
        val labels = ArrayList<String>()

        var index = 0
        for ((state, count) in measurementCounts) {
            entries.add(BarEntry(index.toFloat(), count.toFloat()))
            labels.add("|$state⟩")
            index++
        }

        val dataSet = BarDataSet(entries, "Measurement Counts")
        dataSet.color = Color.CYAN
        dataSet.valueTextColor = Color.WHITE
        dataSet.valueTextSize = 14f

        val barData = BarData(dataSet)
        MeasurementbarChart.data = barData

        // Customize X-Axis
        val xAxis = MeasurementbarChart.xAxis
        xAxis.valueFormatter = IndexAxisValueFormatter(labels)
        xAxis.position = XAxis.XAxisPosition.BOTTOM
        xAxis.textColor = Color.WHITE
        xAxis.textSize = 12f
        xAxis.granularity = 1f
        xAxis.setDrawGridLines(false)

        // Customize Y-Axis
        MeasurementbarChart.axisLeft.textColor = Color.WHITE
        MeasurementbarChart.axisRight.isEnabled = false // Hide right Y-Axis

        // Customize Bar Chart
        MeasurementbarChart.setFitBars(true)
        MeasurementbarChart.description.isEnabled = false
        MeasurementbarChart.legend.textColor = Color.WHITE
        MeasurementbarChart.animateY(1000)
    }

//    Statevector BarChart
    private fun showStatevectorChart(statevector: List<Double>){
        print(statevector)
        val entries = ArrayList<BarEntry>()
        val labels = ArrayList<String>()

    for (i in statevector.indices) {
        entries.add(BarEntry(i.toFloat(), statevector[i].toFloat()))
        labels.add("|$i⟩") // Quantum state label (like |0⟩, |1⟩)
    }

    val dataSet = BarDataSet(entries, "Statevector Amplitudes")
    dataSet.colors = ColorTemplate.COLORFUL_COLORS.toList()
    dataSet.valueTextColor = Color.WHITE
    dataSet.valueTextSize = 14f

        val barData = BarData(dataSet)
        statevectorChart.data = barData

        val xAxis = statevectorChart.xAxis
        xAxis.valueFormatter = IndexAxisValueFormatter(labels)
        xAxis.position = XAxis.XAxisPosition.BOTTOM
        xAxis.textColor = Color.WHITE
        xAxis.textSize = 12f
        xAxis.granularity = 1f

        statevectorChart.axisLeft.textColor = Color.WHITE
        statevectorChart.axisRight.isEnabled = false

        statevectorChart.setFitBars(true)
        statevectorChart.description.isEnabled = false
        statevectorChart.legend.textColor = Color.WHITE
        statevectorChart.animateY(1000)

    }

    //    Unitary Matrix
    private fun unitaryMatrixView() {
        if (unitaryReal.isNotEmpty()) {
            val recyclerView: RecyclerView = findViewById(R.id.unitaryMatrixRecycler)
            recyclerView.layoutManager = GridLayoutManager(this, unitaryReal[0].size)
            recyclerView.adapter = UnitaryMatrixAdapter(unitaryReal)
        } else {
            Toast.makeText(this, "Matrix data is empty", Toast.LENGTH_SHORT).show()
            print("Matrix data is empty")
        }
    }

}
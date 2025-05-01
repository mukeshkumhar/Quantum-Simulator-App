package com.example.quantumsimulator.Activity

import android.R.attr.orientation
import android.graphics.BitmapFactory
import android.graphics.Color
import android.os.Bundle
import android.os.Handler
import android.os.Looper
import android.util.Base64
import android.view.View
import android.widget.ImageView
import android.widget.LinearLayout
import android.widget.TextView
import android.widget.Toast
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import androidx.lifecycle.lifecycleScope
import androidx.recyclerview.widget.GridLayoutManager
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.example.quantumsimulator.Adapter.GateAdapter
import com.example.quantumsimulator.Adapter.UnitaryMatrixAdapter
import com.example.quantumsimulator.ApiManager.RetrofitClient
import com.example.quantumsimulator.DataModels.GateAction
import com.example.quantumsimulator.DataModels.QuantumGates
import com.example.quantumsimulator.DataModels.QuantumRequest
import com.example.quantumsimulator.DataModels.QuantumResponse
import com.example.quantumsimulator.DataModels.StateVectorEntry
import com.example.quantumsimulator.DataModels.quantumGates
import com.example.quantumsimulator.R
import com.example.quantumsimulator.databinding.ActivityQuantumCircuitBinding
import com.github.mikephil.charting.charts.BarChart
import com.github.mikephil.charting.components.XAxis
import com.github.mikephil.charting.data.BarData
import com.github.mikephil.charting.data.BarDataSet
import com.github.mikephil.charting.data.BarEntry
import com.github.mikephil.charting.formatter.IndexAxisValueFormatter
import com.github.mikephil.charting.formatter.ValueFormatter
import com.github.mikephil.charting.utils.ColorTemplate
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import retrofit2.Response
import java.util.Stack

class QuantumCircuitActivity : AppCompatActivity() {
    private lateinit var binding: ActivityQuantumCircuitBinding

    private lateinit var gateAdapter: GateAdapter
    private lateinit var circuitCanvas: LinearLayout
    private val circuitSteps = mutableListOf<Triple<TextView, QuantumGates, Int>>()
    private val undoStack = Stack<Triple<TextView, QuantumGates, Int>>()
    private val undoButton by lazy { binding.undoButton }
    private lateinit var qubitRows: Array<LinearLayout>
    private val noOfQubit = mutableListOf<LinearLayout>()
    private val appliedGates = mutableListOf<GateAction>()

    private var progressDialog: AlertDialog? = null
    private var handler: Handler? = null
    private var runnable: Runnable? = null

    private val messages = listOf("Loading...","Quantum is simulating...", "Please wait...")
    private var currentMessageIndex = 0
    private var currentCharIndex = 0
    private var isDeleting = false

    private var resultTypingHandler: Handler? = null
    private var resultTypingRunnable: Runnable? = null

    private lateinit var MeasurementbarChart: BarChart
    private lateinit var statevectorChart: BarChart

    private var unitaryReal: List<List<Double>> = listOf()
    private var unitaryImag: List<List<Double>> = listOf()

    private var densityReal: List<List<Double>> = listOf()
    private var densityImag: List<List<Double>> = listOf()


    private val qubitCount = 3 // Change this to support more qubits


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityQuantumCircuitBinding.inflate(layoutInflater)
        setContentView(binding.root)
        enableEdgeToEdge()
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main)) { v, insets ->
            val systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars())
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom)
            insets
        }
//        undo Button click listener
        undoButton.setOnClickListener {
            undoDelete()
        }

        binding.backBtn.setOnClickListener {
            val intent = android.content.Intent(this, MainActivity::class.java)
            startActivity(intent)
        }

        val qubitContainer = binding.gateName
        qubitName(qubitContainer,qubitCount)

        MeasurementbarChart = binding.measurementBarChart
        statevectorChart = binding.statevectorBarChart




        val rvGates = binding.rvGates
        circuitCanvas = binding.circuitCanvas

        // Create rows for multiple qubits
        qubitRows = Array(qubitCount) { LinearLayout(this) }
        qubitRows.forEach { row ->
            row.orientation = LinearLayout.HORIZONTAL
            circuitCanvas.addView(row) // Add rows to circuit canvas
        }

        // Initialize the gate adapter
        gateAdapter = GateAdapter(quantumGates) { selectedGate ->
            selectQubitAndAddGate(selectedGate)
        }
        rvGates.layoutManager = LinearLayoutManager(this, LinearLayoutManager.HORIZONTAL, false)
        rvGates.adapter = gateAdapter

//        Call API When run button is clicked
        binding.btnRunCircuit.setOnClickListener {
            showLoadingDialog()
            callQuantumApi()
        }

        val unitaryRecycler = binding.unitaryMatrixRecycler
        val densityRecycler = binding.densityMatrixRecycler

        binding.unitaryBtn.setOnClickListener {
            if (unitaryRecycler.visibility == View.VISIBLE){
                unitaryRecycler.visibility = View.GONE
                binding.unitaryBtn.text = "Show Unitary Matrix"
            } else {

                unitaryRecycler.visibility = View.VISIBLE
                binding.unitaryBtn.text = "Hide Unitary Matrix"
            }
        }

        binding.densityBtn.setOnClickListener {
            if (densityRecycler.visibility == View.VISIBLE){
                densityRecycler.visibility = View.GONE
                binding.densityBtn.text = "Show Density Matrix"
            } else {
                densityRecycler.visibility = View.VISIBLE
                binding.densityBtn.text = "Hide Density Matrix"
            }
        }




    }

//    Creating Qubits Lines

    private fun selectQubitAndAddGate(gate: QuantumGates) {
        val qubitOptions = Array(qubitCount) { "Qubit ${it + 1}" }

        AlertDialog.Builder(this)
            .setTitle("Select Qubit")
            .setItems(qubitOptions) { _, index ->
                addGateToCircuit(gate, index)
            }
            .show()
    }

    private fun qubitName(container: LinearLayout, numberOfQubits: Int){

        noOfQubit.clear() // Clear existing rows if any

        for (i in 0 until numberOfQubits) {
            val rowLayout = LinearLayout(this).apply {
                orientation = LinearLayout.HORIZONTAL
                layoutParams = LinearLayout.LayoutParams(
                    LinearLayout.LayoutParams.MATCH_PARENT,
                    LinearLayout.LayoutParams.WRAP_CONTENT
                ).apply {
                    setMargins(15, 30, 0, 0) // Margin bottom 10
                }
            }

            val label = TextView(this).apply {
                text = "Q${i + 1}->"
                textSize = 16f
                setPadding(10, 10, 20, 10)
            }

            rowLayout.addView(label)
            container.addView(rowLayout)
            noOfQubit.add(rowLayout) // Save for later gate additions
        }
    }

//    Add gates to the circuit
    private fun addGateToCircuit(gate: QuantumGates,qubitIndex: Int) {
    // Create a new TextView to represent the gate
    val gateView = TextView(this)
    gateView.text = gate.symbol
    gateView.textSize = 18f
    gateView.setPadding(10, 10, 10, 10)
    gateView.setBackgroundResource(R.drawable.gate_bg)

    // Set layout params with bottom margin
    val layoutParams = LinearLayout.LayoutParams(
        LinearLayout.LayoutParams.WRAP_CONTENT,
        LinearLayout.LayoutParams.WRAP_CONTENT
    )
    layoutParams.setMargins(15, 0, 0, 15) // Left, Top, Right, Bottom (in px)
    gateView.layoutParams = layoutParams

    // Long press to delete the gate
    gateView.setOnLongClickListener {
        showDeleteConfirmationDialog(gateView, gate, qubitIndex)
        true
    }

    // Ensure `qubitRows` is properly initialized
    if (qubitIndex in qubitRows.indices) {
        qubitRows[qubitIndex].addView(gateView)  // Add gate to the correct qubit row
        circuitSteps.add(Triple(gateView, gate, qubitIndex)) // Store in the list
        appliedGates.add(GateAction(qubitIndex, gate.name))
    } else {
        Toast.makeText(this, "Invalid Qubit Index", Toast.LENGTH_SHORT).show()
    }
    }

    private fun callQuantumApi() {
        if (appliedGates.isEmpty()) {
            Toast.makeText(this, "No gates applied", Toast.LENGTH_SHORT).show()
            return
        }
        // Transform the list into a required format (group by qubit index)
//        val gatesMap = appliedGates.groupBy { it.qubitIndex }
//            .mapValues { entry -> entry.value.map { it.gateName } }

        val gateSymbolMap = quantumGates.associateBy({ it.name }, { it.symbol })


        val gatesMap = appliedGates.groupBy { it.qubitIndex }
            .mapValues { entry -> entry.value.map { gateSymbolMap[it.gateName] ?: it.gateName } }

        val request = QuantumRequest(qubitCount, gatesMap)
        print("Request: $request")

        lifecycleScope.launch {
            try {
                val response: Response<QuantumResponse> = withContext(Dispatchers.IO) {
                    RetrofitClient.instance.simulate(request)
                }

                withContext(Dispatchers.Main) {
                    if (response.isSuccessful  && response.body() != null){
                        val result = response.body()
                        hideLoadingDialog()
                        println("Result From API: $result")
                        animateResultTyping(result?.circuit_diagram.toString())

                        val circuitBase64 = result?.circuit_diagram_image ?: ""
                        val imageView = binding.circuitImage

                        showCircuitDiagram(circuitBase64, imageView)

                        showProbabilityBarChart(result?.probability_distribution as Map<String, Float>?)
                        showBarChart(result?.measurement_counts ?: emptyMap())
                        showStatevectorChart(result?.statevector ?: emptyList())
//                        binding.tvResult.text = result.toString()
                        unitaryReal = result?.unitary_matrix?.real!!
                        unitaryImag = result?.unitary_matrix?.imag!!

                        densityReal = result?.density_matrix?.real!!
                        densityImag = result?.density_matrix?.imag!!

                        // Setup RecyclerView here
                        setupRecyclerView()
                        densityRecyclerView()



                    }
                }
            } catch (e: Exception) {
                hideLoadingDialog()
                withContext(Dispatchers.Main) {
                    Toast.makeText(this@QuantumCircuitActivity, "Error: ${e.message}", Toast.LENGTH_LONG).show()
                }
                print("Error while calling API: ${e.message}")
            }
        }

    }
//    Show delete confirmation dialog
    private fun showDeleteConfirmationDialog(view: TextView, gate: QuantumGates,qubitIndex: Int) {
    AlertDialog.Builder(this)
        .setTitle("Delete Gate")
        .setMessage("Do you want to remove this ${gate.name} gate?")
        .setPositiveButton("Yes") { _, _ ->
            // Store gate in undo stack before deleting
            undoStack.push(Triple(view, gate, qubitIndex))

            // Remove from UI and data list
            qubitRows[qubitIndex].removeView(view)
            circuitSteps.removeIf { it.first == view }

            // Show undo button
            undoButton.visibility = View.VISIBLE

            Toast.makeText(this, "${gate.name} removed", Toast.LENGTH_SHORT).show()
        }
        .setNegativeButton("No", null)
        .show()
    }

    private fun undoDelete() {
        if (undoStack.isNotEmpty()) {
            val (view, gate, qubitIndex) = undoStack.pop()

            // Restore gate to UI and data list
            qubitRows[qubitIndex].addView(view)
            circuitSteps.add(Triple(view, gate, qubitIndex))

            // Hide undo button if no more undo actions left
            if (undoStack.isEmpty()) {
                undoButton.visibility = View.GONE
            }

            Toast.makeText(this, "${gate.name} restored to Qubit ${qubitIndex + 1}", Toast.LENGTH_SHORT).show()
        }
    }

    fun showCircuitDiagram(base64Image: String, imageView: ImageView) {
        try {
            val decodedBytes = Base64.decode(base64Image, Base64.DEFAULT)
            val bitmap = BitmapFactory.decodeByteArray(decodedBytes, 0, decodedBytes.size)
            imageView.setImageBitmap(bitmap)
        } catch (e: Exception) {
            e.printStackTrace()
        }
    }

//    probabilities chart

    private fun showProbabilityBarChart(probabilities: Map<String, Float>?) {
        val entries = ArrayList<BarEntry>()
        val labels = ArrayList<String>()

        var index = 0f
        if (probabilities != null) {
            for ((state, prob) in probabilities) {
                entries.add(BarEntry(index, prob * 100)) // Convert to percentage
                labels.add("|$state⟩")
                index++
            }
        }

        val dataSet = BarDataSet(entries, "Probability %")
        dataSet.setColors(ColorTemplate.MATERIAL_COLORS, 255)
        dataSet.valueTextColor = Color.WHITE
        dataSet.valueTextSize = 14f

        // ✨ Custom formatter to show percentage sign
        dataSet.valueFormatter = object : ValueFormatter() {
            override fun getFormattedValue(value: Float): String {
                return String.format("%.2f%%", value)
            }
        }

        val barData = BarData(dataSet)

        val chart = binding.probabilityChart
        chart.data = barData
        chart.description.isEnabled = false
        chart.setFitBars(true)
        chart.animateY(1000)

        val xAxis = chart.xAxis
        xAxis.valueFormatter = IndexAxisValueFormatter(labels)
        xAxis.granularity = 1f
        xAxis.position = XAxis.XAxisPosition.BOTTOM
        xAxis.textSize = 12f
        xAxis.textColor = Color.WHITE

        chart.axisLeft.axisMinimum = 0f
        chart.axisLeft.textColor = Color.WHITE
        chart.axisRight.isEnabled = false

        // Customize Chart
        chart.setFitBars(true)
        chart.description.isEnabled = false
        chart.legend.textColor = Color.WHITE
        chart.animateY(1000)

        chart.invalidate() // Refresh the chart
    }


//    Statevector BarChart

    private fun showStatevectorChart(stateVectorItems: List<StateVectorEntry>) {
        val entries = ArrayList<BarEntry>()
        val labels = ArrayList<String>()

        for ((index, item) in stateVectorItems.withIndex()) {

            val probability = (item.amplitude * item.amplitude).toFloat()  // Compute |α|²
            entries.add(BarEntry(index.toFloat(), probability))
            labels.add("${item.basis}\n")  // Label with state + probability
        }

        val dataSet = BarDataSet(entries, "Quantum State Probabilities")
        dataSet.setColors(Color.RED, Color.GREEN, Color.BLUE, Color.YELLOW, Color.CYAN)  // Unique colors
        dataSet.valueTextColor = Color.WHITE
        dataSet.valueTextSize = 12f

        val barData = BarData(dataSet)
        statevectorChart.data = barData

        // Customize X-Axis
        val xAxis = statevectorChart.xAxis
        xAxis.valueFormatter = IndexAxisValueFormatter(labels)
        xAxis.position = XAxis.XAxisPosition.BOTTOM
        xAxis.textColor = Color.WHITE
        xAxis.textSize = 12f
        xAxis.granularity = 1f
        xAxis.setDrawGridLines(false)

        // Customize Y-Axis
        statevectorChart.axisLeft.textColor = Color.WHITE
        statevectorChart.axisRight.isEnabled = false

        // Enable touch interactions
        statevectorChart.setTouchEnabled(true)
        statevectorChart.setPinchZoom(true)

        // Customize Chart
        statevectorChart.setFitBars(true)
        statevectorChart.description.isEnabled = false
        statevectorChart.legend.textColor = Color.WHITE
        statevectorChart.animateY(1000)
    }


    //    Measurement BarChart
private fun showBarChart(measurementCounts: Map<String, Int>) {
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

//    Unitary Matrix
    private fun setupRecyclerView() {
        if (unitaryReal.isNotEmpty()) {
            val recyclerView: RecyclerView = findViewById(R.id.unitaryMatrixRecycler)
            recyclerView.layoutManager = GridLayoutManager(this, unitaryReal[0].size)
            recyclerView.adapter = UnitaryMatrixAdapter(unitaryReal)
        } else {
            Toast.makeText(this, "Matrix data is empty", Toast.LENGTH_SHORT).show()
            print("Matrix data is empty")
        }
    }

//    Density Matrix
private fun densityRecyclerView() {
    if (densityReal.isNotEmpty()) {
        val recyclerView: RecyclerView = findViewById(R.id.densityMatrixRecycler)
        recyclerView.layoutManager = GridLayoutManager(this, densityReal[0].size)
        recyclerView.adapter = UnitaryMatrixAdapter(densityReal)
    } else {
        Toast.makeText(this, "Matrix data is empty", Toast.LENGTH_SHORT).show()
        print("Matrix data is empty")
    }
}



    private fun showLoadingDialog() {
        val builder = AlertDialog.Builder(this)
        val dialogView = layoutInflater.inflate(R.layout.custom_progress_dialog, null)
        val typingText = dialogView.findViewById<TextView>(R.id.typingText)
        builder.setView(dialogView)
        builder.setCancelable(false)
        progressDialog = builder.create()
        progressDialog?.show()

        handler = Handler(Looper.getMainLooper())
        runnable = object : Runnable {
            override fun run() {
                val fullText = messages[currentMessageIndex]

                if (!isDeleting) {
                    if (currentCharIndex <= fullText.length) {
                        typingText.text = fullText.substring(0, currentCharIndex)
                        currentCharIndex++
                        handler?.postDelayed(this, 100)
                    } else {
                        isDeleting = true
                        handler?.postDelayed(this, 1000)
                    }
                } else {
                    if (currentCharIndex > 0) {
                        if (currentCharIndex <= fullText.length){
                            typingText.text = fullText.substring(0, currentCharIndex)
                            currentCharIndex--
                            handler?.postDelayed(this, 10)
                        } else{
                            currentCharIndex--
                            handler?.postDelayed(this, 10)
                        }
                    } else {
                        isDeleting = false
                        currentMessageIndex = (currentMessageIndex + 1) % messages.size
                        handler?.postDelayed(this, 500)
                    }
                }
            }
        }

        handler?.post(runnable!!)
    }

    private fun hideLoadingDialog() {
        handler?.removeCallbacks(runnable!!)
        progressDialog?.dismiss()
    }

    private fun animateResultTyping(text: String) {
        resultTypingHandler = Handler(Looper.getMainLooper())
        var index = 0

        resultTypingRunnable = object : Runnable {
            override fun run() {
                if (index <= text.length) {
                    binding.tvResult.text = text.substring(0, index)
                    index++
                    resultTypingHandler?.postDelayed(this, 1)
                }
            }
        }

        resultTypingHandler?.post(resultTypingRunnable!!)
    }



}
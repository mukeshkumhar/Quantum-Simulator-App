package com.example.quantumsimulator.Activity

import android.graphics.Color
import android.os.Bundle
import android.view.View
import android.widget.LinearLayout
import android.widget.TextView
import android.widget.Toast
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import androidx.lifecycle.lifecycleScope
import androidx.recyclerview.widget.LinearLayoutManager
import com.example.quantumsimulator.Adapter.GateAdapter
import com.example.quantumsimulator.ApiManager.RetrofitClient
import com.example.quantumsimulator.DataModels.GateAction
import com.example.quantumsimulator.DataModels.QuantumGates
import com.example.quantumsimulator.DataModels.QuantumRequest
import com.example.quantumsimulator.DataModels.QuantumResponse
import com.example.quantumsimulator.DataModels.quantumGates
import com.example.quantumsimulator.R
import com.example.quantumsimulator.databinding.ActivityQuantumCircuitBinding
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
    private val appliedGates = mutableListOf<GateAction>()

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
            callQuantumApi()
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

//    Add gates to the circuit
    private fun addGateToCircuit(gate: QuantumGates,qubitIndex: Int) {
    // Create a new TextView to represent the gate
    val gateView = TextView(this)
    gateView.text = gate.symbol
    gateView.textSize = 18f
    gateView.setPadding(10, 10, 10, 10)
    gateView.setBackgroundResource(R.drawable.gate_bg)

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
                        println("Result From API: $result")
                        binding.tvResult.text = result.toString()
                    }
                }
            } catch (e: Exception) {
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

}
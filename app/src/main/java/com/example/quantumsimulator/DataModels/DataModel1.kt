package com.example.quantumsimulator.DataModels

import com.google.gson.annotations.SerializedName

data class QuantumConcept(
    val title: String,
    val description: String,
    val iconResId: Int
)

data class QuantumRequest(
    val num_qubits: Int,
    val gates: Map<Int, List<String>> // List of gates applied to each qubit
)

data class GateAction(
    val qubitIndex: Int,  // 0-based index for qubit
    val gateName: String       // Gate name or symbol
)

data class QuantumResponse(
    val measurement_counts: Map<String, Int>,
    val statevector: List<StateVectorEntry>,
    val statevector_real: List<Double>,
    val statevector_imag: List<Double>,
    val unitary_matrix: UnitaryMatrix,
    val density_matrix: DensityMatrix,
    val probability_distribution: Map<String, Double>,
    val circuit_diagram: String,
    val circuit_diagram_image: String
)

data class StateVectorEntry(
    @SerializedName("basis")
    val basis: String,
    @SerializedName("amplitude")
    val amplitude: Double
)

data class UnitaryMatrix(
    @SerializedName("real")
    val real: List<List<Double>>,
    @SerializedName("imag")
    val imag: List<List<Double>>
)

data class DensityMatrix(
    @SerializedName("real")
    val real: List<List<Double>>,
    @SerializedName("imag")
    val imag: List<List<Double>>
)
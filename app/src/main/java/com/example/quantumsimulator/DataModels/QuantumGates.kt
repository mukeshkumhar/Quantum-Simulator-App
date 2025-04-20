package com.example.quantumsimulator.DataModels

data class QuantumGates(val name: String, val symbol: String)


val quantumGates = listOf(
    QuantumGates("X Gate", "X"),
    QuantumGates("Y Gate", "Y"),
    QuantumGates("Z Gate", "Z"),
    QuantumGates("H Gate", "H"),
    QuantumGates("CNOT Gate", "CX"),
)
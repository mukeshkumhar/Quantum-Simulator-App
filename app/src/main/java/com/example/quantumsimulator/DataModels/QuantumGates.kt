package com.example.quantumsimulator.DataModels

data class QuantumGates(val name: String, val symbol: String)


val quantumGates = listOf(
    QuantumGates("X Gate", "X"),
    QuantumGates("Y Gate", "Y"),
    QuantumGates("Z Gate", "Z"),
    QuantumGates("Hadamard Gate", "H"),
    QuantumGates("Phase Gate (S)", "S"),
    QuantumGates("T Gate (π/8)", "T"),
    QuantumGates("Identity Gate", "I"),

    // Inverse Gates
    QuantumGates("S† Gate (S-dagger)", "S†"),
    QuantumGates("T† Gate (T-dagger)", "T†"),

    // Parameterized Gates
    QuantumGates("RX(θ)", "RX(1.57)"),
    QuantumGates("RY(θ)", "RY(1.57)"),
    QuantumGates("RZ(θ)", "RZ(1.57)"),

    QuantumGates("U1(λ)", "U1(3.14)"),
    QuantumGates("U2(ϕ,λ)", "U2(1.57,3.14)"),
    QuantumGates("U3(θ,ϕ,λ)", "U3(1.57,1.57,3.14)"),

    // Controlled Gates
    QuantumGates("CX (CNOT) Gate", "CX"),
    QuantumGates("CY Gate", "CY"),
    QuantumGates("CZ Gate", "CZ"),
    QuantumGates("CH Gate", "CH"),

    QuantumGates("CRX(θ,control)", "CRX(1.57,0)"),
    QuantumGates("CRY(θ,control)", "CRY(1.57,0)"),
    QuantumGates("CRZ(θ,control)", "CRZ(1.57,0)"),

    QuantumGates("CCX (Toffoli) Gate", "CCX"),
    QuantumGates("SWAP Gate", "SWAP"),
    QuantumGates("CSWAP Gate", "CSWAP"),

    // Swap & Measurement
    QuantumGates("SWAP Gate", "SWAP"),
    QuantumGates("Measure", "M")
)
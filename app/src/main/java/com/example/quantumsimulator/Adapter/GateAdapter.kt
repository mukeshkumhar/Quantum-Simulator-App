package com.example.quantumsimulator.Adapter

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.example.quantumsimulator.DataModels.QuantumGates
import com.example.quantumsimulator.R

class GateAdapter(
    private val gates: List<QuantumGates>,
    private val onGateClick: (QuantumGates) -> Unit
) : RecyclerView.Adapter<GateAdapter.GateViewHolder>() {

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): GateViewHolder {
        val view = LayoutInflater.from(parent.context).inflate(R.layout.item_gate, parent, false)
        return GateViewHolder(view)
    }
    override fun onBindViewHolder(holder: GateViewHolder, position: Int) {
        val gate = gates[position]
        holder.tvGateName.text = gate.name
        holder.itemView.setOnClickListener { onGateClick(gate) }
    }

    override fun getItemCount() = gates.size

    class GateViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        val tvGateName: TextView = itemView.findViewById(R.id.tvGateName)
    }
}
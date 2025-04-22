package com.example.quantumsimulator.Adapter

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.example.quantumsimulator.R

class UnitaryMatrixAdapter (private val matrixData: List<List<Double>>) : RecyclerView.Adapter<UnitaryMatrixAdapter.MatrixViewHolder>()  {
    class MatrixViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        val textView: TextView = itemView.findViewById(R.id.matrixCell)
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): MatrixViewHolder {
        val view = LayoutInflater.from(parent.context).inflate(R.layout.matrix_cell, parent, false)
        return MatrixViewHolder(view)
    }

    override fun onBindViewHolder(holder: MatrixViewHolder, position: Int) {
        val row = position / matrixData[0].size
        val col = position % matrixData[0].size
        holder.textView.text = matrixData[row][col].toString()
    }

    override fun getItemCount(): Int {
        return matrixData.size * matrixData[0].size
    }
}
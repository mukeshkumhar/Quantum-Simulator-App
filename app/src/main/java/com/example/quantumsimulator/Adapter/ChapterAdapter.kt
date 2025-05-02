package com.example.quantumsimulator.Adapter

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.LinearLayout
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import com.example.quantumsimulator.DataModels.QuantumChapter
import com.example.quantumsimulator.R

class ChapterAdapter (private val chapters: List<QuantumChapter>) :
    RecyclerView.Adapter<ChapterAdapter.ChapterViewHolder>() {

    private var expandedPosition = -1


    inner class ChapterViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        val title: TextView = itemView.findViewById(R.id.chapterTitle)
        val discription: TextView = itemView.findViewById(R.id.chapterDiscription)
        val image: ImageView = itemView.findViewById(R.id.chapterImage)
        val expandImage: ImageView = itemView.findViewById(R.id.expand_image)
        val expandText: TextView = itemView.findViewById(R.id.expand_text)
        val expandableLayout: LinearLayout = itemView.findViewById(R.id.expandable_layout)
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ChapterViewHolder {
        val view = LayoutInflater.from(parent.context).inflate(R.layout.chapter_item, parent, false)
        return ChapterViewHolder(view)
    }

    override fun onBindViewHolder(holder: ChapterViewHolder, position: Int) {
        val chapter = chapters[position]
        holder.title.text = chapter.title
        holder.discription.text = chapter.discription
////        holder.image.setImageResource(chapter.imageUrl)
//        // Load image from URL using Glide
//                Glide.with(holder.itemView.context)
//                    .load(chapter.imageUrl)
////                    .placeholder(R.drawable.placeholder) // optional placeholder
////                    .error(R.drawable.error) // optional error image
//                    .into(holder.image)

        holder.expandText.text = chapter.explanation

        Glide.with(holder.itemView.context)
            .load(chapter.imageUrl)
            .into(holder.expandImage)

        val isExpanded = position == expandedPosition

        val positionClick = holder.adapterPosition

        holder.expandableLayout.visibility = if (isExpanded) View.VISIBLE else View.GONE

        holder.itemView.setOnClickListener {
            if (expandedPosition == position) {
                expandedPosition = -1
            } else {
                expandedPosition = positionClick
            }
            notifyDataSetChanged()
        }
    }

    override fun getItemCount(): Int = chapters.size
}
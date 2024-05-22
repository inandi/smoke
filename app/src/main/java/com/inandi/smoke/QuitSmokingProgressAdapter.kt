package com.inandi.smoke

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import android.widget.ImageView
import com.bumptech.glide.Glide

class QuitSmokingProgressAdapter(private val progressData: Array<Array<String>>) :
    RecyclerView.Adapter<QuitSmokingProgressAdapter.ProgressViewHolder>() {

    inner class ProgressViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        val milestoneTextView: TextView = itemView.findViewById(R.id.milestoneTextView)
        val awardTextView: TextView = itemView.findViewById(R.id.awardTextView)
        val descriptionTextView: TextView = itemView.findViewById(R.id.descriptionTextView)
        val imageView: ImageView = itemView.findViewById(R.id.imageView)
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ProgressViewHolder {
        val itemView = LayoutInflater.from(parent.context)
            .inflate(R.layout.item_progress, parent, false)
        return ProgressViewHolder(itemView)
    }

    override fun onBindViewHolder(holder: ProgressViewHolder, position: Int) {
        val milestone = progressData[position]
        holder.milestoneTextView.text = milestone[1]
        holder.awardTextView.text = milestone[2]
        holder.descriptionTextView.text = milestone[3]

        // Set text color to white
        holder.milestoneTextView.setTextColor(holder.itemView.resources.getColor(android.R.color.white))
        holder.awardTextView.setTextColor(holder.itemView.resources.getColor(android.R.color.white))
        holder.descriptionTextView.setTextColor(holder.itemView.resources.getColor(android.R.color.white))


        // Load the image (assuming you have a method to do this, e.g., using Glide or Picasso)
        val imagePath = milestone[4]
        // Example using Glide
        Glide.with(holder.itemView.context).load(imagePath).into(holder.imageView)
    }

    override fun getItemCount(): Int {
        return progressData.size
    }
}

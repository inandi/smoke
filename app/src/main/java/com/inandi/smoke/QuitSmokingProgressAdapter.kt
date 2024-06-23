/**
 * QuitSmokingProgressAdapter is responsible to load the RecyclerView
 *
 * @author Gobinda Nandi
 * @version 0.2
 * @since 2024-04-01
 * @copyright Copyright (c) 2024
 * @license This code is licensed under the MIT License.
 * See the LICENSE file for details.
 */

package com.inandi.smoke

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import android.widget.ImageView
import com.bumptech.glide.Glide
import org.json.JSONObject
import java.text.SimpleDateFormat
import java.util.Locale

/**
 * RecyclerView Adapter for displaying progress data in quitting smoking.
 *
 * @param progressData Array of progress data where each element is an array of strings
 * representing a milestone. The expected structure of each inner array is:
 * [id, milestone, award, description, imagePath].
 */
class QuitSmokingProgressAdapter(
    private val progressData: Array<Array<String>>,
    private val jsonObjectFormData: JSONObject,
) :
    RecyclerView.Adapter<QuitSmokingProgressAdapter.ProgressViewHolder>() {

    /**
     * ViewHolder class for holding the views for each item in the RecyclerView.
     *
     * @param itemView The view of the individual list item.
     */
    inner class ProgressViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        val milestoneTextView: TextView = itemView.findViewById(R.id.milestoneTextView)
        val awardTextView: TextView = itemView.findViewById(R.id.awardTextView)
        val descriptionTextView: TextView = itemView.findViewById(R.id.descriptionTextView)
        val awardAchievedOn: TextView = itemView.findViewById(R.id.awardAchievedOn)
        val imageView: ImageView = itemView.findViewById(R.id.imageView)
    }

    /**
     * Called when RecyclerView needs a new [ProgressViewHolder] of the given type to represent
     * an item.
     *
     * @param parent The ViewGroup into which the new View will be added after it is bound to an adapter position.
     * @param viewType The view type of the new View.
     * @return A new ProgressViewHolder that holds a View of the given view type.
     */
    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ProgressViewHolder {
        val itemView = LayoutInflater.from(parent.context)
            .inflate(R.layout.item_progress, parent, false)
        return ProgressViewHolder(itemView)
    }

    /**
     * Called by RecyclerView to display the data at the specified position.
     * This method should update the contents of the [ProgressViewHolder.itemView] to reflect the item at the given position.
     *
     * @param holder The ViewHolder which should be updated to represent the contents of the item at the given position in the data set.
     * @param position The position of the item within the adapter's data set.
     */
    override fun onBindViewHolder(holder: ProgressViewHolder, position: Int) {
        val setGetData = SetGetData()
        val milestone = progressData[position]
        holder.milestoneTextView.text = milestone[1]
        holder.awardTextView.text = milestone[2]
        holder.descriptionTextView.text = milestone[3]

        val awardDetailString = setGetData.getNextAwardDetailFromStatusKeyOfJsonObject(
            jsonObjectFormData,
            "award_achieved_timeline",
            milestone[0]
        )

        // Set text color to white
        holder.milestoneTextView.setTextColor(holder.itemView.resources.getColor(android.R.color.white))
        holder.awardTextView.setTextColor(holder.itemView.resources.getColor(android.R.color.white))
        holder.descriptionTextView.setTextColor(holder.itemView.resources.getColor(android.R.color.white))

        if (awardDetailString == null) {
            holder.awardAchievedOn.text = ""
        } else {
            val jsonObjectAwardDetail = JSONObject(awardDetailString)

            // Extract values from jsonObjectAwardDetail
            val score = jsonObjectAwardDetail.optString("score", "")
            val dateTimeString = jsonObjectAwardDetail.optString("datetime", "")
            holder.awardAchievedOn.text = ""

            if (dateTimeString.isNotEmpty() && score.isNotEmpty()) {
                // Parse dateTimeString into a Date object
                val dateTimeFormatter = SimpleDateFormat("yyyy-MM-dd HH:mm:ss", Locale.getDefault())
                val dateTime = dateTimeFormatter.parse(dateTimeString)

                // Format the date to a nicer format
                val dateFormatter = SimpleDateFormat("MMMM dd, yyyy", Locale.getDefault())
                val formattedDate = dateFormatter.format(dateTime)
                holder.awardAchievedOn.text =
                    "You attained this accomplishment on $formattedDate, scoring $score%."

                // Set text color to darker grey if score archived
                holder.milestoneTextView.setTextColor(holder.itemView.resources.getColor(android.R.color.darker_gray))
                holder.awardTextView.setTextColor(holder.itemView.resources.getColor(android.R.color.darker_gray))
                holder.descriptionTextView.setTextColor(holder.itemView.resources.getColor(android.R.color.darker_gray))
            }
        }

        // Load the image (assuming you have a method to do this, e.g., using Glide or Picasso)
        val imagePath = milestone[4]
        // Example using Glide
        Glide.with(holder.itemView.context).load(imagePath).into(holder.imageView)
    }

    /**
     * Returns the total number of items in the data set held by the adapter.
     *
     * @return The total number of items in this adapter.
     */
    override fun getItemCount(): Int {
        return progressData.size
    }
}

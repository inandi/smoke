/**
 * BadgeActivity is responsible for displaying the badges representing milestones in the
 * quit smoking progress.
 *
 * This activity sets up the layout, initializes the RecyclerView to display badges, and
 * provides navigation to the "About" and "Home" activities via buttons.
 *
 * This file is part of Quit Smoking Android.
 *
 * Author: Gobinda Nandi
 * Created: 2024
 *
 * Copyright (c) 2024 Gobinda Nandi
 * This software is released under the MIT License.
 * See the LICENSE file for details.
 */

package com.inandi.smoke

import android.os.Bundle
import androidx.activity.ComponentActivity
import android.widget.ImageButton
import android.content.Intent
import androidx.recyclerview.widget.RecyclerView
import androidx.recyclerview.widget.LinearLayoutManager
import org.json.JSONObject

class BadgeActivity : ComponentActivity() {

    private lateinit var dataSet: DataSet

    /**
     * Called when the activity is starting. This is where most initialization should go.
     *
     * This method sets the content view to the activity's layout, initializes the badge view,
     * and sets up click listeners for the "About" and "Home" buttons to navigate to their
     * respective activities.
     *
     * @param savedInstanceState If the activity is being re-initialized after previously being
     *                           shut down then this Bundle contains the data it most recently
     *                           supplied in `onSaveInstanceState`. Note: Otherwise it is null.
     */
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_badge)

        loadBadgeView()

        val aboutButton = findViewById<ImageButton>(R.id.aboutButton)
        aboutButton.setOnClickListener {
            startActivity(Intent(this@BadgeActivity, AboutActivity::class.java))
        }

        val homeButton = findViewById<ImageButton>(R.id.homeButton)
        homeButton.setOnClickListener {
            startActivity(Intent(this@BadgeActivity, MainActivity::class.java))
        }
    }

    /**
     * Initializes and sets up the badge view by loading data into the RecyclerView.
     *
     * This method creates an instance of the `DataSet` class, retrieves the quitSmokingProgress()
     * data and sets up the RecyclerView with a linear layout manager and an adapter populated
     * with the retrieved data.
     *
     * The RecyclerView is used to display a list of badges, each representing a milestone in the
     * quit smoking progress.
     */
    private fun loadBadgeView() {
        val dataSet = DataSet()
        val recyclerView: RecyclerView = findViewById(R.id.recyclerView)
        recyclerView.layoutManager = LinearLayoutManager(this)

        // Fetch data from quitSmokingProgress function
        val progressData = dataSet.quitSmokingProgress()

        // Create and set adapter
        val adapter = QuitSmokingProgressAdapter(progressData)
        recyclerView.adapter = adapter
    }

}
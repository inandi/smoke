/**
 * AboutActivity is responsible for displaying the information of
 * quit smoking app.
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
import android.widget.TextView
import android.view.View
import android.widget.ImageButton
import android.content.Intent
import android.net.Uri

/**
 * AboutActivity is a ComponentActivity that displays information about the project,
 * including the project version. It provides navigation to the home and badge activities
 * and a function to open a "Buy Me a Coffee" link.
 */
class AboutActivity : ComponentActivity() {

    /**
     * Called when the activity is starting. This is where most initialization should go.
     *
     * This method sets the content view to the activity's layout, displays the project version,
     * and sets up click listeners for the "Home" and "Badge" buttons to navigate to their
     * respective activities.
     *
     * @param savedInstanceState If the activity is being re-initialized after previously being
     *                           shut down then this Bundle contains the data it most recently
     *                           supplied in `onSaveInstanceState`. Note: Otherwise it is null.
     */
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_about)

        // Display the project version
        val versionTextView: TextView = findViewById(R.id.project_version)
        versionTextView.text = MainActivity.PROJECT_VERSION

        // Set up click listener for "Home" button
        val homeButton = findViewById<ImageButton>(R.id.homeButton)
        homeButton.setOnClickListener {
            startActivity(Intent(this@AboutActivity, MainActivity::class.java))
        }

        // Set up click listener for "Badge" button
        val badgeButton = findViewById<ImageButton>(R.id.badgeButton)
        badgeButton.setOnClickListener {
            startActivity(Intent(this@AboutActivity, BadgeActivity::class.java))
        }
    }

    /**
     * Opens the Buy Me a Coffee link in a web browser.
     *
     * @param view The view that triggers the function.
     */
    fun openCoffeeLink(view: View) {
        val url = "https://www.buymeacoffee.com/iGobinda"
        val intent = Intent(Intent.ACTION_VIEW, Uri.parse(url))
        startActivity(intent)
    }
}

/**
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

class AboutActivity : ComponentActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_about)

        val versionTextView: TextView = findViewById(R.id.project_version)
        versionTextView.text = MainActivity.PROJECT_VERSION

        val homeButton = findViewById<ImageButton>(R.id.homeButton)
        homeButton.setOnClickListener {
            startActivity(Intent(this@AboutActivity, MainActivity::class.java))
        }

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

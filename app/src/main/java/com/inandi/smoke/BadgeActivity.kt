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
import android.widget.ImageButton
import android.content.Intent
import androidx.recyclerview.widget.RecyclerView
import androidx.recyclerview.widget.LinearLayoutManager
import org.json.JSONObject

class BadgeActivity : ComponentActivity() {

    private lateinit var dataSet: DataSet

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_badge)
        val dataSet = DataSet()
        val recyclerView: RecyclerView = findViewById(R.id.recyclerView)

        recyclerView.layoutManager = LinearLayoutManager(this)

        // Fetch data from quitSmokingProgress function
        val progressData = dataSet.quitSmokingProgress()

        // Create and set adapter
        val adapter = QuitSmokingProgressAdapter(progressData)
        recyclerView.adapter = adapter

        val aboutButton = findViewById<ImageButton>(R.id.aboutButton)
        aboutButton.setOnClickListener {
            startActivity(Intent(this@BadgeActivity, AboutActivity::class.java))
        }

        val homeButton = findViewById<ImageButton>(R.id.homeButton)
        homeButton.setOnClickListener {
            startActivity(Intent(this@BadgeActivity, MainActivity::class.java))
        }

    }

    fun getSmokingProgressById(id: String): JSONObject? {
        val progressArray = dataSet.quitSmokingProgress()
        for (item in progressArray) {
            if (item[0] == id) {
                val obj = JSONObject()
                obj.put("number", item[0])
                obj.put("timeFrame", item[1])
                obj.put("animal", item[2])
                obj.put("description", item[3])
                obj.put("imageUrl", item[4])
                obj.put("hourDuration", item[5])
                return obj
            }
        }
        return null // Return null if ID is not found
    }


}
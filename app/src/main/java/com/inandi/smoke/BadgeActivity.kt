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

class BadgeActivity : ComponentActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_badge)
        val recyclerView: RecyclerView = findViewById(R.id.recyclerView)

        recyclerView.layoutManager = LinearLayoutManager(this)

        // Fetch data from quitSmokingProgress function
        val progressData = quitSmokingProgress()

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

    /**
     * Provides an array containing quit smoking progress data.
     * Each element of the array represents a milestone in the quitting process,
     * containing information such as duration, icon, and description.
     *
     * @return An array of arrays, where each inner array represents a milestone.
     *         The inner array structure: [id, title, award, description, imagePath]
     *         - id: An int representing id of each milestone.
     *         - title: A string representing the title or name of the milestone.
     *         - award: A string representing the icon/award associated with the milestone.
     *         - description: A string providing details about the milestone.
     *         - imagePath: A string providing details location of image.
     */
    private fun quitSmokingProgress(): Array<Array<String>> {
        return arrayOf(
            arrayOf(
                "1",
                "After 1 Day",
                "Bee",
                "Within minutes of quitting, your heart rate and blood pressure drop. Carbon monoxide levels in your blood decrease, allowing oxygen levels to return to normal.",
                "file:///android_asset/media/award/bee.jpeg"),

            arrayOf(
                "2",
                "After 7 Days",
                "Rabbit",
                "Nicotine is completely out of your system. Your sense of taste and smell start to improve.",
                "file:///android_asset/media/award/rabbit.jpg"),

            arrayOf(
                "3",
                "After 1 Month",
                "Wolf",
                "Your lung function begins to improve. You may notice a decrease in coughing and shortness of breath. Energy levels increase.",
                "file:///android_asset/media/award/wolf.jpg"),

            arrayOf(
                "4",
                "After 3 Months",
                "Lion",
                "Circulation improves, leading to better blood flow to your extremities. Lung function continues to improve, making physical activity easier. Breathing becomes noticeably easier.",
                "file:///android_asset/media/award/lion.jpg"),

            arrayOf(
                "5",
                "After 6 Months",
                "Tiger",
                "Coughing, sinus congestion, fatigue, and shortness of breath decrease even more. Lung function can increase by up to 10%. Risk of infections decreases.",
                "file:///android_asset/media/award/tiger.jpeg"),

            arrayOf(
                "6",
                "After 1 Year",
                "Eagle",
                "Risk of heart disease is halved compared to a smoker's risk. Risk of heart attack drops significantly. Cilia in the lungs begin to regain normal function, reducing the risk of infection.",
                "file:///android_asset/media/award/eagle.jpg"),

            arrayOf(
                "7",
                "After 2 Years",
                "Bear",
                "Risk of stroke decreases significantly. Lung function continues to improve, reducing the risk of chronic lung diseases.",
                "file:///android_asset/media/award/bear.jpg"),

            arrayOf(
                "8",
                "After 3 Years",
                "Leopard",
                "Risk of heart disease drops to that of a nonsmoker. Lung function improves even further.",
                "file:///android_asset/media/award/leopard.jpg"),

            arrayOf(
                "9",
                "After 4 Years",
                "Gorilla",
                "Risk of cancers, such as mouth, throat, esophagus, and bladder, decreases significantly. Coughing and shortness of breath continue to decrease.",
                "file:///android_asset/media/award/gorilla.jpg"),

            arrayOf(
                "10",
                "After 5 Years",
                "Elephant",
                "Risk of stroke decreases to that of a nonsmoker. Risk of cancers, such as cervix, colon, and rectum, decreases.",
                "file:///android_asset/media/award/elephant.jpg"),

            arrayOf(
                "11",
                "After 10 Years",
                "Rhino",
                "Risk of dying from lung cancer drops by about half compared to a smoker. Risk of cancers, such as kidney, pancreas, and esophagus, decreases significantly.",
                "file:///android_asset/media/award/rhino.jpg"),

            arrayOf(
                "12",
                "After 15 Years",
                "Dragon",
                "Risk of heart disease drops to that of a nonsmoker. Overall risk of death becomes nearly the same as that of someone who has never smoked.",
                "file:///android_asset/media/award/dragon.jpg")
        )
    }

}
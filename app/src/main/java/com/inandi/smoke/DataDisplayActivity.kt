package com.inandi.smoke

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.tooling.preview.Preview
import com.inandi.smoke.ui.theme.SmokeTheme
import android.widget.TextView
import android.widget.Button
import android.view.View
import android.widget.ImageButton
import android.widget.EditText
import android.widget.Spinner
import androidx.appcompat.app.AppCompatActivity
import android.content.Context
import android.content.SharedPreferences
import android.content.Intent
import com.google.gson.Gson
import java.io.FileOutputStream
import android.widget.Toast
import java.io.FileInputStream
import java.io.InputStreamReader
import java.io.BufferedReader
import java.util.TimeZone

class DataDisplayActivity : ComponentActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_data_display)

        val aboutButton = findViewById<ImageButton>(R.id.aboutButton)
        aboutButton.setOnClickListener {
            startActivity(Intent(this@DataDisplayActivity, AboutActivity::class.java))
        }

        val badgeButton = findViewById<ImageButton>(R.id.badgeButton)
        badgeButton.setOnClickListener {
            startActivity(Intent(this@DataDisplayActivity, BadgeActivity::class.java))
        }

        // Read the JSON data from the file
        val savedData = readDataFromFile()
        // Convert JSON data to FormData object
        val formData = Gson().fromJson(savedData, MainActivity.FormData::class.java)

        // Display the data in TextViews or any other UI components
//        val countryTextView = findViewById<TextView>(R.id.countryTextView)
//        val startYearTextView = findViewById<TextView>(R.id.startYearTextView)
//        val smokesPerDayTextView = findViewById<TextView>(R.id.smokesPerDayTextView)
//        val cigarettePriceTextView = findViewById<TextView>(R.id.cigarettePriceTextView)
//        val createdOnTextView = findViewById<TextView>(R.id.createdOnTextView)
//
//        countryTextView.text = "Country: ${formData.country}"
//        startYearTextView.text = "Start Year: ${formData.startYear}"
//        smokesPerDayTextView.text = "Smokes per Day: ${formData.smokesPerDay}"
//        cigarettePriceTextView.text = "Cigarette Price: ${formData.cigarettePrice}"
//        createdOnTextView.text = "Created On: ${formData.created_on}"

    }

    private fun readDataFromFile(): String {
        val filename = "formData.json"
        val fileInputStream: FileInputStream = openFileInput(filename)
        val inputStreamReader = InputStreamReader(fileInputStream)
        val bufferedReader = BufferedReader(inputStreamReader)
        val stringBuilder = StringBuilder()
        var line: String?
        while (bufferedReader.readLine().also { line = it } != null) {
            stringBuilder.append(line)
        }
        return stringBuilder.toString()
    }
}
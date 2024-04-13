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
import java.text.SimpleDateFormat
import java.util.Date

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

class MainActivity : ComponentActivity() {

    data class FormData(
        val country: String,
        val startYear: Int,
        val smokesPerDay: Int,
        val cigarettePrice: Double,
        val created_on: String
    )

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        val formDataFile = getFileStreamPath("formData.json")
        if (formDataFile != null && formDataFile.exists()) {
            navigateToDataDisplayScreen()
        } else {
            setContentView(R.layout.activity_main) // Ensure this matches your layout file's name.

            val aboutButton = findViewById<ImageButton>(R.id.aboutButton)
            aboutButton.setOnClickListener {
                startActivity(Intent(this@MainActivity, AboutActivity::class.java))
            }

            val badgeButton = findViewById<ImageButton>(R.id.badgeButton)
            badgeButton.setOnClickListener {
                startActivity(Intent(this@MainActivity, BadgeActivity::class.java))
            }

            val buttonSubmit = findViewById<Button>(R.id.buttonSubmit)
            buttonSubmit.setOnClickListener {
                val country = findViewById<Spinner>(R.id.spinnerCountry).selectedItem.toString()
                val startYear = findViewById<EditText>(R.id.editTextStartYear).text.toString().toInt()
                val smokesPerDay = findViewById<EditText>(R.id.editTextSmokesPerDay).text.toString().toInt()
                val cigarettePrice = findViewById<EditText>(R.id.editTextCigarettePrice).text.toString().toDouble()
                val formData = FormData(country, startYear, smokesPerDay, cigarettePrice, getCurrentTimestamp())
                val jsonData = Gson().toJson(formData)
                saveDataToFile(jsonData)

//                // Verify the saved JSON data
//                val savedData = readDataFromFile()
//                val parsedObject = Gson().fromJson(savedData, FormData::class.java)
//                println(parsedObject)
            }
        }
    }

    private fun navigateToDataDisplayScreen() {
        startActivity(Intent(this@MainActivity, DataDisplayActivity::class.java))
    }

    private fun saveDataToFile(data: String) {
        val filename = "formData.json"
        val fileOutputStream: FileOutputStream
        try {
            fileOutputStream = openFileOutput(filename, Context.MODE_PRIVATE)
            fileOutputStream.write(data.toByteArray())
            fileOutputStream.close()
            Toast.makeText(this, "Data saved", Toast.LENGTH_SHORT).show()
        } catch (e: Exception) {
            e.printStackTrace()
            Toast.makeText(this, "Failed to save data", Toast.LENGTH_SHORT).show()
        }
    }

//    private fun readDataFromFile(): String {
//        val filename = "formData.json"
//        val fileInputStream: FileInputStream = openFileInput(filename)
//        val inputStreamReader = InputStreamReader(fileInputStream)
//        val bufferedReader = BufferedReader(inputStreamReader)
//        val stringBuilder = StringBuilder()
//        var line: String?
//        while (bufferedReader.readLine().also { line = it } != null) {
//            stringBuilder.append(line)
//        }
//        return stringBuilder.toString()
//    }

    private fun getCurrentTimestamp(): String {
        val dateFormat = SimpleDateFormat("yyyy-MM-dd HH:mm:ss")
        dateFormat.timeZone = TimeZone.getTimeZone("GMT") // Set timezone to GMT
        val currentTimeStamp = Date()
        return dateFormat.format(currentTimeStamp)
    }
}

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
import android.util.Log
import org.json.JSONObject
import java.io.IOException
import java.io.FileNotFoundException
import java.nio.charset.StandardCharsets

class DataDisplayActivity : ComponentActivity() {

    private lateinit var cigarettePriceTextView: TextView

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_data_display)
        // Call the function to update the TextView with cig count

        // Initialize cigarettePriceTextView
        cigarettePriceTextView = findViewById(R.id.cigarettePrice)

        println("---start.....")
        updateTextViewWithJsonValue("cigarettePrice")
        println("---end.....")

        val aboutButton = findViewById<ImageButton>(R.id.aboutButton)
        aboutButton.setOnClickListener {
            startActivity(Intent(this@DataDisplayActivity, AboutActivity::class.java))
        }

        val badgeButton = findViewById<ImageButton>(R.id.badgeButton)
        badgeButton.setOnClickListener {
            startActivity(Intent(this@DataDisplayActivity, BadgeActivity::class.java))
        }
    }

    private fun updateTextViewWithJsonValue(elementName: String) {
        val formData = readDataFromFile()
        Log.d("JSON", "formData: $formData")
        val jsonObject = createJsonObjectFromFormData(formData)
        Log.d("JSON", "jsonObject: $jsonObject")
        val jsonValue = jsonObject.optString(elementName)
        Log.d("JSON", "$elementName: $jsonValue")
        cigarettePriceTextView.text = jsonValue
    }

    private fun createJsonObjectFromFormData(formData: String): JSONObject {
        val jsonObject = JSONObject()
        val pattern = Regex("\"([^\":]+)\":([^\",]+)")
        pattern.findAll(formData).forEach { matchResult ->
            val (key, value) = matchResult.destructured
            jsonObject.put(key.trim(), value.trim())
        }
        return jsonObject
    }

    private fun readDataFromFile(): String {
        val fileInputStream: FileInputStream = openFileInput(MainActivity.FORM_DATA_FILENAME)
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
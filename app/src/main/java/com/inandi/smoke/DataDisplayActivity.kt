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
import java.io.File
import java.text.SimpleDateFormat
import java.util.*
import kotlin.math.ceil

class DataDisplayActivity : ComponentActivity() {

    private lateinit var textViewDisplayCount: TextView
    private lateinit var textViewDisplayMoney: TextView
    private lateinit var textViewDisplayDay: TextView

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_data_display)
        // Call the function to update the TextView with cig count

//        deleteFormDataFile()

        // Initialize cigarettePriceTextView
        textViewDisplayCount = findViewById(R.id.displayCount)
        textViewDisplayMoney = findViewById(R.id.displayMoney)
        textViewDisplayDay = findViewById(R.id.displayDay)

        println("---start.....")
        updateTextViewWithJsonValue()
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

    private fun updateTextViewWithJsonValue() {
        val formData = readDataFromFile()
        val jsonObject = createJsonObjectFromFormData(formData)
        Log.d("JSON", "jsonObject: $jsonObject")

        // award
        val awardName = "Tiger"
        val dummyData = "NULL"

        val varOriginalObject = jsonObject.optJSONObject("original")
        val varCigarettePrice = varOriginalObject.optDouble("cigarettePrice")
        val varCountryObject = varOriginalObject.optJSONObject("country")
        val varCountryName = varCountryObject?.optString("country_name")
        val varCurrencyName = varCountryObject?.optString("currency_name")
        val varCountrySymbol = varCountryObject?.optString("currency_symbol")
        val varCreatedOn = varOriginalObject.optString("created_on")
        val varSmokesPerDay = varOriginalObject.optInt("smokesPerDay")
        val varStartYear = varOriginalObject.optString("startYear")
        val perHourSpent = perHourSpentMoney(varSmokesPerDay, varCigarettePrice)
        val perHourSmoked = perHourSmokedCigarette(varSmokesPerDay)
        val pastDays = getDateDiff(varCreatedOn)

        textViewDisplayCount.text=getString(R.string.displayCountMsgTemplate, formatNumberWithCommas(perHourSmoked), dummyData, awardName)
        textViewDisplayMoney.text = getString(R.string.displayMoneyMsgTemplate, varCountrySymbol, formatNumberWithCommas(perHourSpent),  varCountrySymbol, dummyData, awardName)
        textViewDisplayDay.text=getString(R.string.displayDayMsgTemplate, pastDays, dummyData, awardName)

    }

    private fun perHourSpentMoney(smokesPerDay: Int, cigarettePrice: Double): Double {
        val cigarettesPerHour = smokesPerDay.toDouble() / 24
        return ceil(cigarettesPerHour * cigarettePrice * 100) / 100 // Round up to two decimal places
    }

    private fun perHourSmokedCigarette(smokesPerDay: Int): Double {
        return ceil(smokesPerDay.toDouble() / 24 * 100) / 100 // Round up to two decimal places
    }

    private fun createJsonObjectFromFormData(formData: String): JSONObject {
        return JSONObject(formData)
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

    private fun getDateDiff(dateString: String): String {
        // Parse the provided date string
        val sdf = SimpleDateFormat("yyyy-MM-dd HH:mm:ss", Locale.getDefault())
        sdf.timeZone = TimeZone.getTimeZone("UTC")
        val date = sdf.parse(dateString)

        // Get the current time in UTC
        val currentTime = Calendar.getInstance(TimeZone.getTimeZone("UTC")).time

        // Calculate the difference in milliseconds
        val diffInMillis = currentTime.time - date.time

        // Convert milliseconds to days, hours, and minutes
        val days = (diffInMillis / (1000 * 60 * 60 * 24)).toInt()
        val hours = ((diffInMillis / (1000 * 60 * 60)) % 24).toInt()
        val minutes = ((diffInMillis / (1000 * 60)) % 60).toInt()

        // Construct the result string
        val result = StringBuilder()
        if (days > 0) {
            result.append("${formatNumberWithCommas(days)} days ")
        }
        if (hours > 0) {
            result.append("${formatNumberWithCommas(hours)} hour(s) ")
        }
        if (minutes > 0 && days.toInt() == 0 && hours.toInt() == 0) {
            result.append("${formatNumberWithCommas(minutes)} minute(s)")
        }
        return result.toString().trim()
    }

    private fun deleteFormDataFile() {
        val file = File(filesDir, MainActivity.FORM_DATA_FILENAME)
        if (file.exists()) {
            val deleted = file.delete()
            if (deleted) {
                Log.d("FileDeleted", "formData.json deleted successfully")
            } else {
                Log.e("FileDeleted", "Failed to delete formData.json")
            }
        } else {
            Log.d("FileDeleted", "formData.json does not exist")
        }
    }

    private fun formatNumberWithCommas(number: Number): String {
        val numberString = number.toString()
        val parts = numberString.split(".")
        val wholePart = parts[0]
        val decimalPart = if (parts.size > 1) "." + parts[1] else ""
        val pattern = Regex("\\B(?=(\\d{3})+(?!\\d))")
        return wholePart.replace(pattern, ",") + decimalPart
    }

}
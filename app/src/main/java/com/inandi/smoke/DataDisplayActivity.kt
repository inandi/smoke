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

    /**
     * Initializes the activity, sets the content view, and updates the TextView elements with
     * JSON values.
     *
     * This method is called when the activity is first created. It sets the content view to
     * `activity_data_display`,
     * initializes the TextView elements, calls a function to update them with data from a
     * JSON object, and sets up
     * click listeners for the about and badge buttons.
     *
     * @param savedInstanceState If the activity is being re-initialized after previously being
     * shut down, this contains the data it most recently supplied in `onSaveInstanceState`.
     */
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_data_display)
        // Call the function to update the TextView with cig count

//        deleteFormDataFile()

        // Initialize TextView elements for displaying cigarette data
        // Initialize cigarettePriceTextView
        textViewDisplayCount = findViewById(R.id.displayCount)
        textViewDisplayMoney = findViewById(R.id.displayMoney)
        textViewDisplayDay = findViewById(R.id.displayDay)

        println("---start.....")
        // Update the TextView elements with data from the JSON object
        updateTextViewWithJsonValue()
        println("---end.....")

        // Set up the about button and its click listener
        val aboutButton = findViewById<ImageButton>(R.id.aboutButton)
        aboutButton.setOnClickListener {
            startActivity(Intent(this@DataDisplayActivity, AboutActivity::class.java))
        }

        // Set up the badge button and its click listener
        val badgeButton = findViewById<ImageButton>(R.id.badgeButton)
        badgeButton.setOnClickListener {
            startActivity(Intent(this@DataDisplayActivity, BadgeActivity::class.java))
        }
    }

    /**
     * Updates TextView elements with values extracted from a JSON object.
     *
     * This function reads form data from a file, parses it into a `JSONObject`, extracts
     * relevant values, calculates additional metrics, and updates several TextView elements
     * with the formatted information.
     *
     * The TextView elements are updated with:
     * - The average number of cigarettes smoked per hour.
     * - The average amount of money spent on cigarettes per hour.
     * - The number of days since a specified date.
     */
    private fun updateTextViewWithJsonValue() {
        val formData = readDataFromFile()
        val jsonObject = createJsonObjectFromFormData(formData)
        Log.d("JSON", "jsonObject: $jsonObject")

        // award
        val awardName = "Tiger"
        val dummyData = "NULL"

        // Extract values from the JSON object
        val varOriginalObject = jsonObject.optJSONObject("original")
        val varCigarettePrice = varOriginalObject.optDouble("cigarettePrice")
        val varCountryObject = varOriginalObject.optJSONObject("country")
        val varCountryName = varCountryObject?.optString("country_name")
        val varCurrencyName = varCountryObject?.optString("currency_name")
        val varCountrySymbol = varCountryObject?.optString("currency_symbol")
        val varCreatedOn = varOriginalObject.optString("created_on")
        val varSmokesPerDay = varOriginalObject.optInt("smokesPerDay")
        val varStartYear = varOriginalObject.optString("startYear")

        // Calculate per hour metrics and past days
        val perHourSpent = perHourSpentMoney(varSmokesPerDay, varCigarettePrice)
        val perHourSmoked = perHourSmokedCigarette(varSmokesPerDay)
        val pastDays = getDateDiff(varCreatedOn)

        // Update TextView elements with the calculated and formatted information
        textViewDisplayCount.text=getString(R.string.displayCountMsgTemplate, formatNumberWithCommas(perHourSmoked), dummyData, awardName)
        textViewDisplayMoney.text = getString(R.string.displayMoneyMsgTemplate, varCountrySymbol, formatNumberWithCommas(perHourSpent),  varCountrySymbol, dummyData, awardName)
        textViewDisplayDay.text=getString(R.string.displayDayMsgTemplate, pastDays, dummyData, awardName)
    }

    /**
     * Calculates the amount of money spent on cigarettes per hour.
     *
     * This function takes the number of cigarettes smoked per day and the price of a single cigarette,
     * then calculates the average amount of money spent on cigarettes per hour. The result is rounded
     * up to two decimal places.
     *
     * @param smokesPerDay The number of cigarettes smoked per day.
     * @param cigarettePrice The price of a single cigarette.
     * @return A `Double` representing the amount of money spent on cigarettes per hour,
     * rounded up to two decimal places.
     */
    private fun perHourSpentMoney(smokesPerDay: Int, cigarettePrice: Double): Double {
        val cigarettesPerHour = smokesPerDay.toDouble() / 24
        return ceil(cigarettesPerHour * cigarettePrice * 100) / 100 // Round up to two decimal places
    }

    /**
     * Calculates the average number of cigarettes smoked per hour.
     *
     * This function takes the total number of cigarettes smoked per day and calculates the average
     * number of cigarettes smoked per hour. The result is rounded up to two decimal places.
     *
     * @param smokesPerDay The number of cigarettes smoked per day.
     * @return A `Double` representing the average number of cigarettes smoked per hour, rounded up to two decimal places.
     */
    private fun perHourSmokedCigarette(smokesPerDay: Int): Double {
        return ceil(smokesPerDay.toDouble() / 24 * 100) / 100 // Round up to two decimal places
    }

    /**
     * Creates a JSON object from the provided form data string.
     *
     * This function takes a string containing form data in JSON format and parses it
     * into a `JSONObject`.
     *
     * @param formData A string containing the form data in JSON format.
     * @return A `JSONObject` representing the form data.
     * @throws JSONException If the form data string cannot be parsed into a valid JSON object.
     */
    private fun createJsonObjectFromFormData(formData: String): JSONObject {
        return JSONObject(formData)
    }

    /**
     * Reads data from the form data file and returns it as a string.
     *
     * This function opens the file named `formData.json` from the application's internal
     * storage, reads its contents line by line, and returns the entire content as a single string.
     *
     * @return A `String` containing the contents of the form data file.
     * @throws IOException If an I/O error occurs during reading the file.
     */
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

    /**
     * Calculates the difference between the provided date string and the current UTC time.
     *
     * This function takes a date string in the format "yyyy-MM-dd HH:mm:ss", parses it to
     * a date object, and then calculates the difference between this date and the current
     * time in UTC. The result is returned as a string representing the difference in days,
     * hours, and minutes.
     *
     * For example:
     * - Input: "2024-05-17 12:00:00" -> Output: "1 day(s) 5 hour(s) 30 minute(s)" (assuming
     * the current time is 2024-05-18 17:30:00 UTC)
     *
     * @param dateString The date string to be compared, in the format "yyyy-MM-dd HH:mm:ss".
     * @return A `String` representing the difference in days, hours, and minutes.
     */
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
            result.append("${formatNumberWithCommas(days)} day(s) ")
        }
        if (hours > 0) {
            result.append("${formatNumberWithCommas(hours)} hour(s) ")
        }
        if (minutes > 0) {
            result.append("${formatNumberWithCommas(minutes)} minute(s)")
        }
        return result.toString().trim()
    }

    /**
     * Deletes the form data file (formData.json) if it exists.
     *
     * This function checks if a file named `formData.json` exists in the application's
     * files directory. If the file exists, it attempts to delete it and logs the result.
     *
     * - If the file is successfully deleted, it logs a success message.
     * - If the file fails to delete, it logs an error message.
     * - If the file does not exist, it logs a message indicating so.
     */
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

    /**
     * Formats a given number by adding commas as thousands separators.
     *
     * This function takes a `Number` as input and returns a `String` representation
     * of the number with commas inserted at every thousandth place.
     *
     * For example:
     * - Input: 1234567 -> Output: "1,234,567"
     * - Input: 1234567.89 -> Output: "1,234,567.89"
     *
     * @param number The number to be formatted. This can be any subclass of `Number`.
     * @return A `String` representation of the number with commas as thousands separators.
     */
    private fun formatNumberWithCommas(number: Number): String {
        val numberString = number.toString()
        val parts = numberString.split(".")
        val wholePart = parts[0]
        val decimalPart = if (parts.size > 1) "." + parts[1] else ""
        val pattern = Regex("\\B(?=(\\d{3})+(?!\\d))")
        return wholePart.replace(pattern, ",") + decimalPart
    }

}
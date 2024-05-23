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
import java.text.SimpleDateFormat
import java.util.Date
import android.widget.Button
import android.view.View
import android.widget.ImageButton
import android.widget.EditText
import android.widget.Spinner
import android.content.Context
import android.content.Intent
import android.os.Build
import java.io.FileOutputStream
import android.widget.Toast
import java.util.TimeZone
import android.widget.AdapterView
import android.widget.ArrayAdapter
import androidx.annotation.RequiresApi
import org.json.JSONObject
import java.time.LocalDateTime
import java.time.format.DateTimeFormatter
import java.time.temporal.ChronoUnit
import java.util.Calendar

class MainActivity : ComponentActivity() {

    companion object {
        const val FORM_DATA_FILENAME = "formData.json"
        const val PROJECT_VERSION = "v0.1"
    }

    private lateinit var spinnerCountry: Spinner
    private lateinit var dataSet: DataSet
    private lateinit var setGetData: SetGetData

    /**
     * Initializes the MainActivity and handles navigation to the DataDisplayActivity
     * if the form data file exists.
     *
     * This function performs the following steps:
     * 1. Checks if the form data file exists. If it does, navigates to DataDisplayActivity.
     * 2. If the file doesn't exist, sets the content view to activity_main and initializes
     * the country spinner with data.
     * 3. Sets up listeners for the spinner and buttons on the activity.
     * 4. Collects user input from text fields and saves the data as a JSON object to a file when
     * the submit button is clicked.
     *
     * @param savedInstanceState The saved instance state bundle.
     */
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        val dataSet = DataSet()
        val setGetData = SetGetData()

        val countryObject = JSONObject()

        // Check if the form data file exists
        val formDataFile = getFileStreamPath(FORM_DATA_FILENAME)

        if (formDataFile != null && formDataFile.exists()) {
            // If the file exists, navigate to the data display screen
            navigateToDataDisplayScreen()
        } else {

            // If the file does not exist, set the content view to activity_main
            setContentView(R.layout.activity_main) // Ensure this matches your layout file's name.

//            setGetData.prepareForm()

            // Initialize the country spinner
            spinnerCountry = findViewById(R.id.spinnerCountry)
            // Call the function to initialize the array
            val countries = dataSet.initializeCountriesArray()

            // Adapter for the spinner
            val adapter = ArrayAdapter<String>(
                this,
                android.R.layout.simple_spinner_item,
                countries.map { "${it[0]} (${it[2]})" } // Combine country name with currency symbol
            )

            adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item)

            spinnerCountry.adapter = adapter

            // Set up the spinner item selected listener
            spinnerCountry.onItemSelectedListener = object : AdapterView.OnItemSelectedListener {
                override fun onItemSelected(parent: AdapterView<*>, view: View?, position: Int, id: Long) {
                    val selectedCountry = countries[position]
                    val selectedCountryName = selectedCountry[0]
                    val selectedCurrencyName = selectedCountry[1]
                    val selectedCurrencySymbol = selectedCountry[2]
                    countryObject.put("country_name", selectedCountryName)
                    countryObject.put("currency_name", selectedCurrencyName)
                    countryObject.put("currency_symbol", selectedCurrencySymbol)
                }
                override fun onNothingSelected(parent: AdapterView<*>) {
                    // Do nothing if nothing is selected
                }
            }

            // Set up listeners for the button
            val aboutButton = findViewById<ImageButton>(R.id.aboutButton)
            aboutButton.setOnClickListener {
                startActivity(Intent(this@MainActivity, AboutActivity::class.java))
            }

            // Set up listeners for the button
            val badgeButton = findViewById<ImageButton>(R.id.badgeButton)
            badgeButton.setOnClickListener {
                startActivity(Intent(this@MainActivity, BadgeActivity::class.java))
            }

            /**
             * Sets up an OnClickListener for the submit button to handle user input and save it to a file.
             * Retrieves user input from EditText fields, creates a JSON object, and saves it to a file.
             */
            val buttonSubmit = findViewById<Button>(R.id.buttonSubmit)
            buttonSubmit.setOnClickListener {
                // Retrieve user input from EditText fields
                val startYear = findViewById<EditText>(R.id.editTextStartYear).text.toString().toInt()
                val smokesPerDay = findViewById<EditText>(R.id.editTextSmokesPerDay).text.toString().toInt()
                val cigarettePrice = findViewById<EditText>(R.id.editTextCigarettePrice).text.toString().toDouble()

                // Create a JSON object to hold the user input
                val jsonObject = JSONObject()
                jsonObject.put("country", countryObject) // Include country details
                jsonObject.put("cigarettePrice", cigarettePrice) // Price per cigarette
                jsonObject.put("smokesPerDay", smokesPerDay) // Number of cigarettes smoked per day
                jsonObject.put("startYear", startYear) // Year the user started smoking
                val getCurrentTimestamp = getCurrentTimestamp()
                jsonObject.put("created_on", getCurrentTimestamp) // Timestamp of when the data is created

                // Wrap the JSON object in another JSON object
                val originalObject = JSONObject()
                originalObject.put("original", jsonObject)

                // Create a JSON object to hold the update status
                val updateJsonObject = JSONObject()

                // Create an instance of BadgeActivity
                val badgeActivity = BadgeActivity()

                val firstAwardDetail = badgeActivity.getSmokingProgressById("1")
                updateJsonObject.put("next_award_detail", firstAwardDetail) // Include country details


                val hourDurationStr = firstAwardDetail?.get("hourDuration") as? String
                val hourDuration = hourDurationStr?.toIntOrNull() ?: 0 // Convert to Int or use 0 if invalid
                val minutesToAdd = hourDuration * 60L


                val nextAwardDatetime = addMinutesToDateTime(getCurrentTimestamp, minutesToAdd)
                updateJsonObject.put("next_award_datetime", nextAwardDatetime) // Include country details

                originalObject.put("status", updateJsonObject)


                // Save the data to a file
                saveDataToFile(originalObject,this)
            }
        }
    }

    private fun addMinutesToDateTime(dateTime: String, minutesToAdd: Long): String {
        val formatter = SimpleDateFormat("yyyy-MM-dd HH:mm:ss")
        val date = formatter.parse(dateTime)
        val calendar = Calendar.getInstance()
        calendar.time = date
        calendar.add(Calendar.MINUTE, minutesToAdd.toInt())
        return formatter.format(calendar.time)
    }

    fun getStatusValueFromJsonObject(jsonObject: JSONObject, detailKey: String, valueKey: String): String? {
        val statusObject = jsonObject.getJSONObject("status")
        val nextAwardDetail = statusObject.optString(detailKey)
        // @todo fix it
        val nextAwardJson = JSONObject(nextAwardDetail)
        return nextAwardJson.optString(valueKey)
    }


    /**
     * Navigates to the DataDisplayActivity screen.
     *
     * This function creates an intent to start the `DataDisplayActivity` and then
     * starts the activity, transitioning the user from the current activity to
     * the DataDisplayActivity.
     */
    private fun navigateToDataDisplayScreen() {
        startActivity(Intent(this@MainActivity, DataDisplayActivity::class.java))
    }

    /**
     * Saves a JSON object to a file in the application's internal storage.
     *
     * This function takes a `JSONObject` and a `Context`, converts the JSON object to a string,
     * and writes it to a file named `FORM_DATA_FILENAME` in the application's internal storage.
     * If the data is saved successfully, a toast message indicating success is shown.
     * If an error occurs, a toast message indicating failure is shown and the exception is printed.
     *
     * @param jsonObject The `JSONObject` to be saved to the file.
     * @param context The `Context` used to open the file output stream.
     */
    private fun saveDataToFile(jsonObject: JSONObject, context: Context) {
        val fileOutputStream: FileOutputStream
        try {
            // Open the file output stream in private mode
            fileOutputStream = context.openFileOutput(FORM_DATA_FILENAME, Context.MODE_PRIVATE)
            // Write the JSON object as a string to the file
            fileOutputStream.write(jsonObject.toString().toByteArray())
            // Close the file output stream
            fileOutputStream.close()
            // Show a toast message indicating success
            Toast.makeText(context, "Data saved", Toast.LENGTH_SHORT).show()
        } catch (e: Exception) {
            // Print the stack trace of the exception
            e.printStackTrace()
            // Show a toast message indicating failure
            Toast.makeText(context, "Failed to save data", Toast.LENGTH_SHORT).show()
        }
    }

    /**
     * Retrieves the current timestamp in the format "yyyy-MM-dd HH:mm:ss".
     *
     * This function gets the current date and time, formats it as a string in the specified format,
     * and sets the timezone to GMT.
     *
     * @return A `String` representing the current timestamp in "yyyy-MM-dd HH:mm:ss" format.
     */
    private fun getCurrentTimestamp(): String {
        val dateFormat = SimpleDateFormat("yyyy-MM-dd HH:mm:ss")
        dateFormat.timeZone = TimeZone.getTimeZone("GMT") // Set timezone to GMT
        val currentTimeStamp = Date()
        return dateFormat.format(currentTimeStamp)
    }

}

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
import android.view.ViewGroup
import android.widget.TextView

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
        dataSet = DataSet()
        setGetData = SetGetData()
        val countryObject = JSONObject()

        // Check if the form data file exists
        val formDataFile = getFileStreamPath(FORM_DATA_FILENAME)

        if (formDataFile != null && formDataFile.exists()) {
            // If the file exists, navigate to the data display screen
            navigateToDataDisplayScreen()
        } else {
            // If the file does not exist, set the content view to activity_main
            setContentView(R.layout.activity_main)

            // Initialize the country spinner
            initializeCountrySpinner(countryObject)

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

            // Set up listener for the submit button
            setupSubmitButtonListener(countryObject)
        }
    }

    /**
     * Sets up an OnClickListener for the submit button to handle user input and save it to a file.
     * Retrieves user input from EditText fields, creates a JSON object, and saves it to a file.
     */
    private fun setupSubmitButtonListener(countryObject: JSONObject) {
        val buttonSubmit = findViewById<Button>(R.id.buttonSubmit)
        buttonSubmit.setOnClickListener {
            // Check if countryObject is null or empty
            if (countryObject.length() == 0) {
                val context = this
                val toast = Toast.makeText(context, "Please provide country details", Toast.LENGTH_SHORT)
                toast.show()
                return@setOnClickListener // Exit the click listener if countryObject is empty
            }

            // Retrieve user input from EditText fields
            val startYearText = findViewById<EditText>(R.id.editTextStartYear).text.toString()
            val smokesPerDayText = findViewById<EditText>(R.id.editTextSmokesPerDay).text.toString()
            val cigarettePriceText = findViewById<EditText>(R.id.editTextCigarettePrice).text.toString()

            // Check for empty fields and show toast message
            if (startYearText.isEmpty() || smokesPerDayText.isEmpty() || cigarettePriceText.isEmpty()) {
                val context = this
                val toast = Toast.makeText(context, "Please fill in all fields", Toast.LENGTH_SHORT)
                toast.show()
                return@setOnClickListener // Exit the click listener if any field is empty
            }

            // Convert input to valid data types (assuming these are integers and a double)
            val startYear = startYearText.toIntOrNull() ?: 0
            val smokesPerDay = smokesPerDayText.toIntOrNull() ?: 0
            val cigarettePrice = cigarettePriceText.toDoubleOrNull() ?: 0.0

            // create JSON to save in file
            val jsonObjectForForm = JSONObject()
            val getCurrentTimestamp = setGetData.getCurrentTimestamp()

            // Create a JSON object to hold the user input, in "original" key
            val jsonObjectOriginal = JSONObject()
            jsonObjectOriginal.put("country", countryObject) // Include country details
            jsonObjectOriginal.put("cigarettePrice", cigarettePrice) // Price per cigarette
            jsonObjectOriginal.put("smokesPerDay", smokesPerDay) // Number of cigarettes smoked per day
            jsonObjectOriginal.put("startYear", startYear) // Year the user started smoking
            jsonObjectOriginal.put("created_on", getCurrentTimestamp) // Timestamp of when the data is created
            // store in main JSON
            jsonObjectForForm.put("original", jsonObjectOriginal)

            // Create a JSON object to hold the update status, in "status" key
            val jsonObjectStatus = JSONObject()
            val firstAwardDetail = setGetData.getSmokingProgressById("1")
            jsonObjectStatus.put("next_award_detail", firstAwardDetail)

            // get the time to achieve the award in minutes
            val hourDurationStr = firstAwardDetail?.get("hourDuration") as? String
            val hourDuration = hourDurationStr?.toIntOrNull() ?: 0 // Convert to Int or use 0 if invalid
            val minutesToAdd = hourDuration * 60L

            // add minutes to the current timestamp
            val nextAwardDatetime = setGetData.addMinutesToDateTime(getCurrentTimestamp, minutesToAdd)
            jsonObjectStatus.put("next_award_datetime", nextAwardDatetime) // Include country details

            // store in main JSON
            jsonObjectForForm.put("status", jsonObjectStatus)

            // Save the data to a file
            saveDataToFile(jsonObjectForForm, this)
        }
    }

    private fun initializeCountrySpinner(countryObject: JSONObject) {
        spinnerCountry = findViewById(R.id.spinnerCountry)

        // Initialize the country list
        val countries = dataSet.initializeCountriesArray()
        val countryNames = countries.map { "${it[0]} (${it[2]})" }.toMutableList()

        // Add the placeholder to the beginning of the list
        countryNames.add(0, getString(R.string.placeholder_select_country))

        // Create a custom ArrayAdapter
        val adapter = object : ArrayAdapter<String>(this, android.R.layout.simple_spinner_item, countryNames) {
            override fun getView(position: Int, convertView: View?, parent: ViewGroup): View {
                val view = super.getView(position, convertView, parent) as TextView
                if (position == 0) {
                    view.setTextColor(resources.getColor(android.R.color.darker_gray))
                } else {
                    view.setTextColor(resources.getColor(android.R.color.white))
                }
                return view
            }

            override fun getDropDownView(position: Int, convertView: View?, parent: ViewGroup): View {
                val view = super.getDropDownView(position, convertView, parent) as TextView
                if (position == 0) {
                    view.setTextColor(resources.getColor(android.R.color.darker_gray))
                } else {
                    view.setTextColor(resources.getColor(android.R.color.black))
                }
                return view
            }
        }

        adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item)
        spinnerCountry.adapter = adapter
        spinnerCountry.setSelection(0, false) // Ensure the placeholder is shown initially

        spinnerCountry.onItemSelectedListener = object : AdapterView.OnItemSelectedListener {
            override fun onItemSelected(parent: AdapterView<*>, view: View?, position: Int, id: Long) {
                if (position > 0) { // Ignore the placeholder selection
                    val selectedCountry = countries[position - 1]
                    val selectedCountryName = selectedCountry[0]
                    val selectedCurrencyName = selectedCountry[1]
                    val selectedCurrencySymbol = selectedCountry[2]
                    countryObject.put("country_name", selectedCountryName)
                    countryObject.put("currency_name", selectedCurrencyName)
                    countryObject.put("currency_symbol", selectedCurrencySymbol)
                } else {
                    // Placeholder selected
                    countryObject.remove("country_name")
                    countryObject.remove("currency_name")
                    countryObject.remove("currency_symbol")
                }
            }

            override fun onNothingSelected(parent: AdapterView<*>) {
                // Do nothing if nothing is selected
            }
        }
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
            navigateToDataDisplayScreen()
        } catch (e: Exception) {
            // Print the stack trace of the exception
            e.printStackTrace()
            // Show a toast message indicating failure
            Toast.makeText(context, "Failed to save data", Toast.LENGTH_SHORT).show()
        }
    }

}

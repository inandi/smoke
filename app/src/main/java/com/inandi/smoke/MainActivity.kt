/**
 * This file contains the main activity of the smoking habits tracking application.
 *
 * It initializes UI components, handles user input, and manages data storage and navigation.
 * The activity allows users to select a country, input smoking habits, and save the data
 * to a JSON file in the internal storage. It also provides navigation to other activities
 * for additional features.
 *
 * @author Gobinda Nandi
 * @version 0.2
 * @since 2024-04-01
 * @copyright Copyright (c) 2024
 * @license This code is licensed under the MIT License.
 * See the LICENSE file for details.
 */

package com.inandi.smoke

import android.os.Bundle
import androidx.activity.ComponentActivity
import android.widget.Button
import android.view.View
import android.widget.ImageButton
import android.widget.EditText
import android.widget.Spinner
import android.content.Context
import android.content.Intent
import java.io.FileOutputStream
import android.widget.Toast
import android.widget.AdapterView
import android.widget.ArrayAdapter
import org.json.JSONObject
import android.view.ViewGroup
import android.widget.TextView

class MainActivity : ComponentActivity() {

    companion object {
        const val FORM_DATA_FILENAME = "quit_smoking_progress.json"
        const val PROJECT_VERSION = "v0.3"
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
     * @since 0.1
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
     * Retrieves user input from EditText fields, validates and converts them to appropriate data types,
     * constructs a JSON object with the collected data, and saves it to a file. Also calculates and
     * includes additional data such as total money spent on cigarettes and total cigarettes smoked.
     *
     * @param countryObject The JSONObject containing details about the selected country, including
     *                      country name, currency name, and currency symbol.
     * @since 0.1
     */
    private fun setupSubmitButtonListener(countryObject: JSONObject) {
        // Find the submit button by its ID
        val buttonSubmit = findViewById<Button>(R.id.buttonSubmit)

        // Set an OnClickListener to handle button clicks
        buttonSubmit.setOnClickListener {
            // Check if countryObject is null or empty
            if (countryObject.length() == 0) {
                val context = this
                val toast =
                    Toast.makeText(context, "Please provide country details", Toast.LENGTH_SHORT)
                toast.show()
                return@setOnClickListener // Exit the click listener if countryObject is empty
            }

            // Retrieve user input from EditText fields
            val startYearText = findViewById<EditText>(R.id.editTextStartYear).text.toString()
            val smokesPerDayText = findViewById<EditText>(R.id.editTextSmokesPerDay).text.toString()
            val cigarettePriceText =
                findViewById<EditText>(R.id.editTextCigarettePrice).text.toString()

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
            val getCurrentTimestamp = setGetData.getCurrentDateTime()

            // Create a JSON object to hold the user input, in "original" key
            val jsonObjectOriginal = JSONObject()
            jsonObjectOriginal.put("country", countryObject) // Include country details
            jsonObjectOriginal.put("cigarettePrice", cigarettePrice) // Price per cigarette
            jsonObjectOriginal.put(
                "smokesPerDay",
                smokesPerDay
            ) // Number of cigarettes smoked per day
            jsonObjectOriginal.put("startYear", startYear) // Year the user started smoking
            jsonObjectOriginal.put(
                "created_on",
                getCurrentTimestamp
            ) // Timestamp of when the data is created

            val dataDisplayActivity = DataDisplayActivity()

            // Calculate money spent per minute and total money spent on cigarettes
            val perMinuteSpent =
                dataDisplayActivity.perMinuteSpentMoney(smokesPerDay, cigarettePrice)
            val perMinuteSmoked = dataDisplayActivity.perMinuteSmokedCigarette(smokesPerDay)

            // total money spent till user starts using app from start year
            val calculateTotalMoneySpentVar = dataDisplayActivity.calculateTotalSpent(
                perMinuteSpent,
                startYear,
                getCurrentTimestamp
            )
            // total cig smoked till user starts using app from start year
            val calculateTotalSmokedVar = dataDisplayActivity.calculateTotalSpent(
                perMinuteSmoked,
                startYear,
                getCurrentTimestamp
            )

            jsonObjectOriginal.put(
                "total_money_spent",
                setGetData.formatNumberWithCommas(calculateTotalMoneySpentVar)
            )
            jsonObjectOriginal.put(
                "total_smoked",
                setGetData.formatNumberWithCommas(calculateTotalSmokedVar)
            )

            // store in main JSON
            jsonObjectForForm.put("original", jsonObjectOriginal)

            // Create a JSON object to hold the update status, in "status" key
            val jsonObjectStatus = JSONObject()

            // Retrieve and include next award details from setGetData
            val firstAwardDetail = setGetData.getSmokingProgressById("1")
            jsonObjectStatus.put("next_award_detail", firstAwardDetail)

            // Calculate the time to achieve the award in minutes
            val hourDurationStr = firstAwardDetail?.get("hourDuration") as? String
            val hourDuration =
                hourDurationStr?.toDouble() ?: 0.0 // Convert to Int or use 0 if invalid
            val minutesToAdd = hourDuration * 60L

            // Calculate the datetime for achieving the next award and include it in the status JSON
            // add minutes to the current timestamp
            val nextAwardDatetime =
                setGetData.addMinutesToDateTime(getCurrentTimestamp, minutesToAdd)

            // To store in "status" key
            jsonObjectStatus.put(
                "next_award_datetime",
                nextAwardDatetime
            )

            // store in main JSON
            jsonObjectForForm.put("status", jsonObjectStatus)

            // Save the data to a file
            saveDataToFile(jsonObjectForForm, this)
            navigateToDataDisplayScreen()
        }
    }

    /**
     * Initializes a Spinner with a list of countries and handles country selection events.
     *
     * This function sets up a Spinner to display a list of countries along with their currency symbols,
     * including a placeholder entry for initial selection. When a country is selected, it updates a
     * provided JSONObject with the selected country's name, currency name, and currency symbol.
     *
     * @param countryObject The JSONObject to store the selected country's details (country name,
     *                      currency name, currency symbol).
     * @since 0.1
     */
    private fun initializeCountrySpinner(countryObject: JSONObject) {
        // Find the Spinner view by its ID
        spinnerCountry = findViewById(R.id.spinnerCountry)

        // Initialize the country list
        val countries = dataSet.initializeCountriesArray()
        val countryNames = countries.map { "${it[0]} (${it[2]})" }.toMutableList()

        // Add the placeholder to the beginning of the list
        countryNames.add(0, getString(R.string.placeholder_select_country))

        // Create a custom ArrayAdapter
        val adapter = object :
            ArrayAdapter<String>(this, android.R.layout.simple_spinner_item, countryNames) {

            override fun getView(position: Int, convertView: View?, parent: ViewGroup): View {
                val view = super.getView(position, convertView, parent) as TextView
                // Customize text color for the selected item
                if (position == 0) {
                    view.setTextColor(resources.getColor(android.R.color.darker_gray))
                } else {
                    view.setTextColor(resources.getColor(android.R.color.white))
                }
                return view
            }

            override fun getDropDownView(
                position: Int,
                convertView: View?,
                parent: ViewGroup,
            ): View {
                val view = super.getDropDownView(position, convertView, parent) as TextView
                // Customize text color for dropdown items
                if (position == 0) {
                    view.setTextColor(resources.getColor(android.R.color.darker_gray))
                } else {
                    view.setTextColor(resources.getColor(android.R.color.black))
                }
                return view
            }
        }

        // Set the dropdown layout style
        adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item)

        // Set the adapter to the Spinner
        spinnerCountry.adapter = adapter

        // Set initial selection to the placeholder item
        spinnerCountry.setSelection(0, false) // Ensure the placeholder is shown initially

        // Handle item selection events
        spinnerCountry.onItemSelectedListener = object : AdapterView.OnItemSelectedListener {
            override fun onItemSelected(
                parent: AdapterView<*>,
                view: View?,
                position: Int,
                id: Long,
            ) {
                if (position > 0) { // Ignore the placeholder selection
                    val selectedCountry = countries[position - 1]
                    val selectedCountryName = selectedCountry[0]
                    val selectedCurrencyName = selectedCountry[1]
                    val selectedCurrencySymbol = selectedCountry[2]

                    // Update the JSONObject with selected country details
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
     * @since 0.1
     */
    private fun navigateToDataDisplayScreen() {
        startActivity(Intent(this@MainActivity, DataDisplayActivity::class.java))
    }

    /**
     * Saves a JSON object to a file in the application's internal storage.
     *
     * This function takes a `JSONObject` and a `Context`, converts the JSON object to a string,
     * and writes it to a file named `FORM_DATA_FILENAME` in the application's internal storage.
     * If an error occurs, a toast message indicating failure is shown and the exception is printed.
     *
     * @param jsonObject The `JSONObject` to be saved to the file.
     * @param context The `Context` used to open the file output stream.
     * @since 0.2
     */
    fun saveDataToFile(jsonObject: JSONObject, context: Context) {
        val fileOutputStream: FileOutputStream
        try {
            // Open the file output stream in private mode
            fileOutputStream = context.openFileOutput(FORM_DATA_FILENAME, Context.MODE_PRIVATE)
            // Write the JSON object as a string to the file
            fileOutputStream.write(jsonObject.toString().toByteArray())
            // Close the file output stream
            fileOutputStream.close()
        } catch (e: Exception) {
            // Print the stack trace of the exception
            e.printStackTrace()
            // Show a toast message indicating failure
            Toast.makeText(context, "Failed to save data", Toast.LENGTH_SHORT).show()
        }
    }
}

/**
 * BadgeActivity is responsible for displaying the badges representing milestones in the
 * quit smoking progress.
 *
 * This activity sets up the layout, initializes the RecyclerView to display badges, and
 * provides navigation to the "About" and "Home" activities via buttons.
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
import android.widget.ImageButton
import android.content.Intent
import androidx.recyclerview.widget.RecyclerView
import androidx.recyclerview.widget.LinearLayoutManager
import org.json.JSONObject
import java.io.BufferedReader
import java.io.FileInputStream
import java.io.FileNotFoundException
import java.io.IOException
import java.io.InputStreamReader

/**
 * BadgeActivity is a ComponentActivity that displays a list of badges representing milestones
 * in a user's quit smoking progress. This activity initializes the view, sets up RecyclerView
 * with data, and handles navigation to other activities via buttons.
 * @since 0.1
 */
class BadgeActivity : ComponentActivity() {

    // Dataset to store quit smoking progress data
    private lateinit var dataSet: DataSet

    /**
     * Called when the activity is starting. This is where most initialization should go.
     *
     * This method sets the content view to the activity's layout, initializes the badge view,
     * and sets up click listeners for the "About" and "Home" buttons to navigate to their
     * respective activities.
     *
     * @param savedInstanceState If the activity is being re-initialized after previously being
     *                           shut down then this Bundle contains the data it most recently
     *                           supplied in `onSaveInstanceState`. Note: Otherwise it is null.
     * @since 0.1
     */
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_badge)

        loadBadgeView()

        // Set up click listener for "About" button
        val aboutButton = findViewById<ImageButton>(R.id.aboutButton)
        aboutButton.setOnClickListener {
            startActivity(Intent(this@BadgeActivity, AboutActivity::class.java))
        }

        // Set up click listener for "Home" button
        val homeButton = findViewById<ImageButton>(R.id.homeButton)
        homeButton.setOnClickListener {
            startActivity(Intent(this@BadgeActivity, MainActivity::class.java))
        }
    }

    /**
     * Initializes and sets up the badge view by loading data into the RecyclerView.
     *
     * This method creates an instance of the `DataSet` class, retrieves the quitSmokingProgress()
     * data and sets up the RecyclerView with a linear layout manager and an adapter populated
     * with the retrieved data.
     *
     * The RecyclerView is used to display a list of badges, each representing a milestone in the
     * quit smoking progress.
     * @since 0.1
     */
    private fun loadBadgeView() {
        val dataSet = DataSet()
//        val setGetData = SetGetData()

        // Read form data from file and create JSONObject
        val formData = readDataFromFile()
        var jsonObjectFormData = JSONObject()

        if (!formData.isNullOrEmpty()) {
            jsonObjectFormData = createJsonObjectFromFormData(formData)
        }

        val recyclerView: RecyclerView = findViewById(R.id.recyclerView)
        recyclerView.layoutManager = LinearLayoutManager(this)

        // Fetch data from quitSmokingProgress function
        val progressData = dataSet.quitSmokingProgress()

        // Create and set adapter
        val adapter = QuitSmokingProgressAdapter(progressData, jsonObjectFormData)
        recyclerView.adapter = adapter
    }

    /**
     * Creates a JSON object from the provided form data string.
     *
     * This function takes a string containing form data in JSON format and parses it into a
     * `JSONObject`.
     *
     * @param formData A string containing the form data in JSON format.
     * @return A `JSONObject` representing the form data.
     * @since 0.1
     */
    private fun createJsonObjectFromFormData(formData: String): JSONObject {
        return JSONObject(formData)
    }

    /**
     * Reads data from the form data file and returns it as a string.
     *
     * This function opens the file named `formData.json` from the application's internal storage,
     * reads its contents line by line, and returns the entire content as a single string.
     *
     * @return A `String` containing the contents of the form data file.
     * @throws IOException If an I/O error occurs during reading the file.
     * @since 0.2
     */
    private fun readDataFromFile(): String? {
        return try {
            val fileInputStream: FileInputStream = openFileInput(MainActivity.FORM_DATA_FILENAME)
            val inputStreamReader = InputStreamReader(fileInputStream)
            val bufferedReader = BufferedReader(inputStreamReader)
            val stringBuilder = StringBuilder()
            var line: String?
            while (bufferedReader.readLine().also { line = it } != null) {
                stringBuilder.append(line)
            }
            stringBuilder.toString()
        } catch (e: FileNotFoundException) {
            null
        } catch (e: IOException) {
            // Log the exception if needed
            e.printStackTrace()
            null
        }
    }
}
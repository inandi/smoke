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
//import androidx.appcompat.app.AppCompatActivity
import android.widget.ImageButton

import android.widget.EditText
import android.widget.Spinner
import androidx.appcompat.app.AppCompatActivity

import android.content.Context
import android.content.SharedPreferences


class MainActivity : ComponentActivity() {

    private lateinit var spinnerCountry: Spinner
    private lateinit var editTextStartYear: EditText
    private lateinit var editTextSmokesPerDay: EditText
    private lateinit var editTextCigarettePrice: EditText
    private lateinit var buttonSubmit: Button

    // SharedPreferences
    private lateinit var sharedPreferences: SharedPreferences

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main) // Ensure this matches your layout file's name.
        val topLabel = findViewById<TextView>(R.id.topLabel)
        topLabel.text = "Everyday you are wining!!!"



        val aboutButton = findViewById<ImageButton>(R.id.aboutButton)
        aboutButton.setOnClickListener {
            // Handle Home button click
            // For example:
            // startActivity(Intent(this, HomeActivity::class.java))
        }

        // You can also handle clicks programmatically
        val homeButton = findViewById<ImageButton>(R.id.homeButton)
        homeButton.setOnClickListener {
            // Handle Home button click
            // For example:
            // startActivity(Intent(this, HomeActivity::class.java))
        }

        val badgeButton = findViewById<ImageButton>(R.id.badgeButton)
        badgeButton.setOnClickListener {
            // Handle Home button click
            // For example:
            // startActivity(Intent(this, HomeActivity::class.java))
        }


        spinnerCountry = findViewById(R.id.spinnerCountry)
        editTextStartYear = findViewById(R.id.editTextStartYear)
        editTextSmokesPerDay = findViewById(R.id.editTextSmokesPerDay)
        editTextCigarettePrice = findViewById(R.id.editTextCigarettePrice)
        buttonSubmit = findViewById(R.id.buttonSubmit)

        // Initialize SharedPreferences
        sharedPreferences = getSharedPreferences("FormData", Context.MODE_PRIVATE)

        // Load previously saved data, if available
        loadFormData()

        buttonSubmit.setOnClickListener {
            // Handle form submission here
            val country = spinnerCountry.selectedItem.toString()
            val startYear = editTextStartYear.text.toString().toIntOrNull() ?: 0
            val smokesPerDay = editTextSmokesPerDay.text.toString().toIntOrNull() ?: 0
            val cigarettePrice = editTextCigarettePrice.text.toString().toFloatOrNull() ?: 0f

            // Process the form data (e.g., send it to a server, save to a database, etc.)


            // Save form data to SharedPreferences
            saveFormData(country, startYear, smokesPerDay, cigarettePrice)
        }



//        setContent {
//            SmokeTheme {
//                // A surface container using the 'background' color from the theme
//                Surface(
//                    modifier = Modifier.fillMaxSize(),
//                    color = MaterialTheme.colorScheme.background
//                ) {
//                    Greeting("Android")
//                }
//            }
//        }
    }

    private fun saveFormData(country: String, startYear: Int, smokesPerDay: Int, cigarettePrice: Float) {
        val editor = sharedPreferences.edit()
        editor.putString("country", country)
        editor.putInt("startYear", startYear)
        editor.putInt("smokesPerDay", smokesPerDay)
        editor.putFloat("cigarettePrice", cigarettePrice)
        editor.apply()
    }

    private fun loadFormData() {
        val savedCountry = sharedPreferences.getString("country", "")
        val savedStartYear = sharedPreferences.getInt("startYear", 0)
        val savedSmokesPerDay = sharedPreferences.getInt("smokesPerDay", 0)
        val savedCigarettePrice = sharedPreferences.getFloat("cigarettePrice", 0f)

        // Set the values to the views
        spinnerCountry.setSelection(getIndex(spinnerCountry, savedCountry))
        editTextStartYear.setText(savedStartYear.toString())
        editTextSmokesPerDay.setText(savedSmokesPerDay.toString())
        editTextCigarettePrice.setText(savedCigarettePrice.toString())
    }

    private fun getIndex(spinner: Spinner, value: String?): Int {
        for (i in 0 until spinner.count) {
            if (spinner.getItemAtPosition(i).toString() == value) {
                return i
            }
        }
        return 0 // Default to the first item if not found
    }

}

// Handle button clicks defined in XML
fun onHomeButtonClick(view: View) {
    // Handle Home button click
}

fun onAboutButtonClick(view: View) {
    // Handle About button click
}

fun onBadgeButtonClick(view: View) {
    // Handle Badge button click
}

@Composable
fun Greeting(name: String, modifier: Modifier = Modifier) {
    Text(
        text = "Hello $name!",
        modifier = modifier
    )
}

@Preview(showBackground = true)
@Composable
fun GreetingPreview() {
    SmokeTheme {
        Greeting("Android")
    }
}
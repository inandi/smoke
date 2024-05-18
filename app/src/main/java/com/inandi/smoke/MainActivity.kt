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

import android.widget.AdapterView
import android.widget.ArrayAdapter
import org.json.JSONObject

class MainActivity : ComponentActivity() {

    companion object {
        const val FORM_DATA_FILENAME = "formData.json"
    }

    private lateinit var spinnerCountry: Spinner

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
        val countryObject = JSONObject()

        // Check if the form data file exists
        val formDataFile = getFileStreamPath(FORM_DATA_FILENAME)

        if (formDataFile != null && formDataFile.exists()) {
            // If the file exists, navigate to the data display screen
            navigateToDataDisplayScreen()
        } else {
            // If the file does not exist, set the content view to activity_main
            setContentView(R.layout.activity_main) // Ensure this matches your layout file's name.

            // Initialize the country spinner
            spinnerCountry = findViewById(R.id.spinnerCountry)
            // Call the function to initialize the array
            val countries = initializeCountriesArray()

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
                jsonObject.put("created_on", getCurrentTimestamp()) // Timestamp of when the data is created

                // Wrap the JSON object in another JSON object
                val originalObject = JSONObject()
                originalObject.put("original", jsonObject)

                // Save the data to a file
                saveDataToFile(originalObject,this)
            }
        }
    }

    /**
     * Initializes the array of countries with their respective currencies and symbols.
     *
     * @return An array of arrays containing country name, currency name, and currency symbol.
     */
    private fun initializeCountriesArray(): Array<Array<String>> {
        return arrayOf(
            arrayOf("Afghanistan", "Afghan Afghani", "؋"),
            arrayOf("Albania", "Albanian Lek", "L"),
            arrayOf("Algeria", "Algerian Dinar", "د.ج"),
            arrayOf("Andorra", "Euro", "€"),
            arrayOf("Angola", "Angolan Kwanza", "Kz"),
            arrayOf("Antigua and Barbuda", "Eastern Caribbean Dollar", "$"),
            arrayOf("Argentina", "Argentine Peso", "$"),
            arrayOf("Armenia", "Armenian Dram", "֏"),
            arrayOf("Australia", "Australian Dollar", "$"),
            arrayOf("Austria", "Euro", "€"),
            arrayOf("Azerbaijan", "Azerbaijani Manat", "₼"),
            arrayOf("Bahamas", "Bahamian Dollar", "$"),
            arrayOf("Bahrain", "Bahraini Dinar", "ب.د"),
            arrayOf("Bangladesh", "Bangladeshi Taka", "৳"),
            arrayOf("Barbados", "Barbadian Dollar", "$"),
            arrayOf("Belarus", "Belarusian Ruble", "Br"),
            arrayOf("Belgium", "Euro", "€"),
            arrayOf("Belize", "Belize Dollar", "$"),
            arrayOf("Benin", "West African CFA franc", "Fr"),
            arrayOf("Bhutan", "Bhutanese Ngultrum", "Nu."),
            arrayOf("Bolivia", "Bolivian Boliviano", "Bs."),
            arrayOf("Bosnia and Herzegovina", "Bosnia and Herzegovina Convertible Mark", "KM"),
            arrayOf("Botswana", "Botswana Pula", "P"),
            arrayOf("Brazil", "Brazilian Real", "R$"),
            arrayOf("Brunei", "Brunei Dollar", "$"),
            arrayOf("Bulgaria", "Bulgarian Lev", "лв"),
            arrayOf("Burkina Faso", "West African CFA franc", "Fr"),
            arrayOf("Burundi", "Burundian Franc", "Fr"),
            arrayOf("Cabo Verde", "Cape Verdean Escudo", "$"),
            arrayOf("Cambodia", "Cambodian Riel", "៛"),
            arrayOf("Cameroon", "Central African CFA franc", "Fr"),
            arrayOf("Canada", "Canadian Dollar", "$"),
            arrayOf("Central African Republic", "Central African CFA franc", "Fr"),
            arrayOf("Chad", "Central African CFA franc", "Fr"),
            arrayOf("Chile", "Chilean Peso", "$"),
            arrayOf("China", "Chinese Yuan", "¥"),
            arrayOf("Colombia", "Colombian Peso", "$"),
            arrayOf("Comoros", "Comorian Franc", "Fr"),
            arrayOf("Congo (Brazzaville)", "Central African CFA franc", "Fr"),
            arrayOf("Congo (Kinshasa)", "Congolese Franc", "FC"),
            arrayOf("Costa Rica", "Costa Rican Colon", "₡"),
            arrayOf("Croatia", "Croatian Kuna", "kn"),
            arrayOf("Cuba", "Cuban Peso", "$"),
            arrayOf("Cyprus", "Euro", "€"),
            arrayOf("Czech Republic", "Czech Koruna", "Kč"),
            arrayOf("Denmark", "Danish Krone", "kr"),
            arrayOf("Djibouti", "Djiboutian Franc", "Fr"),
            arrayOf("Dominica", "Eastern Caribbean Dollar", "$"),
            arrayOf("Dominican Republic", "Dominican Peso", "$"),
            arrayOf("Ecuador", "United States Dollar", "$"),
            arrayOf("Egypt", "Egyptian Pound", "E£"),
            arrayOf("El Salvador", "United States Dollar", "$"),
            arrayOf("Equatorial Guinea", "Central African CFA franc", "Fr"),
            arrayOf("Eritrea", "Eritrean Nakfa", "Nfk"),
            arrayOf("Estonia", "Euro", "€"),
            arrayOf("Eswatini", "Swazi Lilangeni", "E"),
            arrayOf("Ethiopia", "Ethiopian Birr", "Br"),
            arrayOf("Fiji", "Fijian Dollar", "$"),
            arrayOf("Finland", "Euro", "€"),
            arrayOf("France", "Euro", "€"),
            arrayOf("Gabon", "Central African CFA franc", "Fr"),
            arrayOf("Gambia", "Gambian Dalasi", "D"),
            arrayOf("Georgia", "Georgian Lari", "ლ"),
            arrayOf("Germany", "Euro", "€"),
            arrayOf("Ghana", "Ghanaian Cedi", "₵"),
            arrayOf("Greece", "Euro", "€"),
            arrayOf("Grenada", "Eastern Caribbean Dollar", "$"),
            arrayOf("Guatemala", "Guatemalan Quetzal", "Q"),
            arrayOf("Guinea", "Guinean Franc", "Fr"),
            arrayOf("Guinea-Bissau", "West African CFA franc", "Fr"),
            arrayOf("Guyana", "Guyanese Dollar", "$"),
            arrayOf("Haiti", "Haitian Gourde", "G"),
            arrayOf("Honduras", "Honduran Lempira", "L"),
            arrayOf("Hungary", "Hungarian Forint", "Ft"),
            arrayOf("Iceland", "Icelandic Krona", "kr"),
            arrayOf("India", "Indian Rupee", "₹"),
            arrayOf("Indonesia", "Indonesian Rupiah", "Rp"),
            arrayOf("Iran", "Iranian Rial", "﷼"),
            arrayOf("Iraq", "Iraqi Dinar", "ع.د"),
            arrayOf("Ireland", "Euro", "€"),
            arrayOf("Israel", "Israeli Shekel", "₪"),
            arrayOf("Italy", "Euro", "€"),
            arrayOf("Ivory Coast", "West African CFA franc", "Fr"),
            arrayOf("Jamaica", "Jamaican Dollar", "J$"),
            arrayOf("Japan", "Japanese Yen", "¥"),
            arrayOf("Jordan", "Jordanian Dinar", "د.ا"),
            arrayOf("Kazakhstan", "Kazakhstani Tenge", "₸"),
            arrayOf("Kenya", "Kenyan Shilling", "Sh"),
            arrayOf("Kiribati", "Australian Dollar", "$"),
            arrayOf("Kuwait", "Kuwaiti Dinar", "د.ك"),
            arrayOf("Kyrgyzstan", "Kyrgyzstani Som", "сом"),
            arrayOf("Laos", "Lao Kip", "₭"),
            arrayOf("Latvia", "Euro", "€"),
            arrayOf("Lebanon", "Lebanese Pound", "ل.ل"),
            arrayOf("Lesotho", "Lesotho Loti", "L"),
            arrayOf("Liberia", "Liberian Dollar", "$"),
            arrayOf("Libya", "Libyan Dinar", "ل.د"),
            arrayOf("Liechtenstein", "Swiss Franc", "CHF"),
            arrayOf("Lithuania", "Euro", "€"),
            arrayOf("Luxembourg", "Euro", "€"),
            arrayOf("Madagascar", "Malagasy Ariary", "Ar"),
            arrayOf("Malawi", "Malawian Kwacha", "MK"),
            arrayOf("Malaysia", "Malaysian Ringgit", "RM"),
            arrayOf("Maldives", "Maldivian Rufiyaa", "ރ."),
            arrayOf("Mali", "West African CFA franc", "Fr"),
            arrayOf("Malta", "Euro", "€"),
            arrayOf("Marshall Islands", "United States Dollar", "$"),
            arrayOf("Mauritania", "Mauritanian Ouguiya", "UM"),
            arrayOf("Mauritius", "Mauritian Rupee", "₨"),
            arrayOf("Mexico", "Mexican Peso", "$"),
            arrayOf("Micronesia", "United States Dollar", "$"),
            arrayOf("Moldova", "Moldovan Leu", "L"),
            arrayOf("Monaco", "Euro", "€"),
            arrayOf("Mongolia", "Mongolian Tugrik", "₮"),
            arrayOf("Montenegro", "Euro", "€"),
            arrayOf("Morocco", "Moroccan Dirham", "د.م."),
            arrayOf("Mozambique", "Mozambican Metical", "MT"),
            arrayOf("Myanmar", "Myanmar Kyat", "K"),
            arrayOf("Namibia", "Namibian Dollar", "$"),
            arrayOf("Nauru", "Australian Dollar", "$"),
            arrayOf("Nepal", "Nepalese Rupee", "₨"),
            arrayOf("Netherlands", "Euro", "€"),
            arrayOf("New Zealand", "New Zealand Dollar", "$"),
            arrayOf("Nicaragua", "Nicaraguan Cordoba", "C$"),
            arrayOf("Niger", "West African CFA franc", "Fr"),
            arrayOf("Nigeria", "Nigerian Naira", "₦"),
            arrayOf("North Korea", "North Korean Won", "₩"),
            arrayOf("North Macedonia", "Macedonian Denar", "ден"),
            arrayOf("Norway", "Norwegian Krone", "kr"),
            arrayOf("Oman", "Omani Rial", "ر.ع."),
            arrayOf("Pakistan", "Pakistani Rupee", "₨"),
            arrayOf("Palau", "United States Dollar", "$"),
            arrayOf("Palestine", "Israeli Shekel", "₪"),
            arrayOf("Panama", "Panamanian Balboa", "B/."),
            arrayOf("Papua New Guinea", "Papua New Guinean Kina", "K"),
            arrayOf("Paraguay", "Paraguayan Guarani", "₲"),
            arrayOf("Peru", "Peruvian Sol", "S/"),
            arrayOf("Philippines", "Philippine Peso", "₱"),
            arrayOf("Poland", "Polish Zloty", "zł"),
            arrayOf("Portugal", "Euro", "€"),
            arrayOf("Qatar", "Qatari Riyal", "ر.ق"),
            arrayOf("Romania", "Romanian Leu", "lei"),
            arrayOf("Russia", "Russian Ruble", "₽"),
            arrayOf("Rwanda", "Rwandan Franc", "Fr"),
            arrayOf("Saint Kitts and Nevis", "Eastern Caribbean Dollar", "$"),
            arrayOf("Saint Lucia", "Eastern Caribbean Dollar", "$"),
            arrayOf("Saint Vincent and the Grenadines", "Eastern Caribbean Dollar", "$"),
            arrayOf("Samoa", "Samoan Tala", "T"),
            arrayOf("San Marino", "Euro", "€"),
            arrayOf("Sao Tome and Principe", "São Tomé and Príncipe Dobra", "Db"),
            arrayOf("Saudi Arabia", "Saudi Riyal", "ر.س"),
            arrayOf("Senegal", "West African CFA franc", "Fr"),
            arrayOf("Serbia", "Serbian Dinar", "дин."),
            arrayOf("Seychelles", "Seychellois Rupee", "₨"),
            arrayOf("Sierra Leone", "Sierra Leonean Leone", "Le"),
            arrayOf("Singapore", "Singapore Dollar", "$"),
            arrayOf("Slovakia", "Euro", "€"),
            arrayOf("Slovenia", "Euro", "€"),
            arrayOf("Solomon Islands", "Solomon Islands Dollar", "$"),
            arrayOf("Somalia", "Somali Shilling", "Sh"),
            arrayOf("South Africa", "South African Rand", "R"),
            arrayOf("South Korea", "South Korean Won", "₩"),
            arrayOf("South Sudan", "South Sudanese Pound", "£"),
            arrayOf("Spain", "Euro", "€"),
            arrayOf("Sri Lanka", "Sri Lankan Rupee", "₨"),
            arrayOf("Sudan", "Sudanese Pound", "£"),
            arrayOf("Suriname", "Surinamese Dollar", "$"),
            arrayOf("Sweden", "Swedish Krona", "kr"),
            arrayOf("Switzerland", "Swiss Franc", "CHF"),
            arrayOf("Syria", "Syrian Pound", "£"),
            arrayOf("Taiwan", "New Taiwan Dollar", "NT$"),
            arrayOf("Tajikistan", "Tajikistani Somoni", "ЅМ"),
            arrayOf("Tanzania", "Tanzanian Shilling", "Sh"),
            arrayOf("Thailand", "Thai Baht", "฿"),
            arrayOf("Timor-Leste", "United States Dollar", "$"),
            arrayOf("Togo", "West African CFA franc", "Fr"),
            arrayOf("Tonga", "Tongan Pa'anga", "T$"),
            arrayOf("Trinidad and Tobago", "Trinidad and Tobago Dollar", "TT$"),
            arrayOf("Tunisia", "Tunisian Dinar", "د.ت"),
            arrayOf("Turkey", "Turkish Lira", "₺"),
            arrayOf("Turkmenistan", "Turkmenistan Manat", "T"),
            arrayOf("Tuvalu", "Australian Dollar", "$"),
            arrayOf("Uganda", "Ugandan Shilling", "Sh"),
            arrayOf("Ukraine", "Ukrainian Hryvnia", "₴"),
            arrayOf("United Arab Emirates", "United Arab Emirates Dirham", "د.إ"),
            arrayOf("United Kingdom", "Pound Sterling", "£"),
            arrayOf("United States", "United States Dollar", "$"),
            arrayOf("Uruguay", "Uruguayan Peso", "$"),
            arrayOf("Uzbekistan", "Uzbekistani Som", "so'm"),
            arrayOf("Vanuatu", "Vanuatu Vatu", "Vt"),
            arrayOf("Vatican City", "Euro", "€"),
            arrayOf("Venezuela", "Venezuelan Bolivar", "Bs."),
            arrayOf("Vietnam", "Vietnamese Dong", "₫"),
            arrayOf("Yemen", "Yemeni Rial", "﷼"),
            arrayOf("Zambia", "Zambian Kwacha", "ZK"),
            arrayOf("Zimbabwe", "Zimbabwean Dollar", "$")
        )
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

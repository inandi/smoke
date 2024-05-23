/**
 * This file is part of Quit Smoking Android.
 *
 * Author: Gobinda Nandi Created: 2024
 *
 * Copyright (c) 2024 Gobinda Nandi This software is released under the MIT License. See the LICENSE
 * file for details.
 */
package com.inandi.smoke

import android.content.Intent
import android.os.Build
import android.os.Bundle
import android.util.Log
import android.view.WindowMetrics
import android.widget.ImageButton
import android.widget.TextView
import androidx.activity.ComponentActivity
import com.google.android.gms.ads.AdRequest
import com.google.android.gms.ads.AdSize
import com.google.android.gms.ads.AdView
import com.google.android.gms.ads.MobileAds
import com.google.android.gms.ads.RequestConfiguration
import com.inandi.smoke.databinding.ActivityDataDisplayBinding
import java.io.BufferedReader
import java.io.File
import java.io.FileInputStream
import java.io.IOException
import java.io.InputStreamReader
import java.text.SimpleDateFormat
import java.util.*
import java.util.concurrent.atomic.AtomicBoolean
import kotlin.math.ceil
import org.json.JSONObject

private const val TAG = "DataDisplayActivity"

class DataDisplayActivity : ComponentActivity() {

    private lateinit var textViewDisplayCount: TextView
    private lateinit var textViewDisplayMoney: TextView
    private lateinit var textViewDisplayDay: TextView

    private val isMobileAdsInitializeCalled = AtomicBoolean(false)
    private val initialLayoutComplete = AtomicBoolean(false)
    private lateinit var binding: ActivityDataDisplayBinding
    private lateinit var adView: AdView
    private lateinit var googleMobileAdsConsentManager: GoogleMobileAdsConsentManager

    // Get the ad size with screen width.
    private val adSize: AdSize
        get() {
            val displayMetrics = resources.displayMetrics
            val adWidthPixels =
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.R) {
                    val windowMetrics: WindowMetrics = this.windowManager.currentWindowMetrics
                    windowMetrics.bounds.width()
                } else {
                    displayMetrics.widthPixels
                }
            val density = displayMetrics.density
            val adWidth = (adWidthPixels / density).toInt()
            return AdSize.getCurrentOrientationAnchoredAdaptiveBannerAdSize(this, adWidth)
        }

    /**
     * Initializes the activity, sets the content view, and updates the TextView elements with JSON
     * values.
     *
     * This method is called when the activity is first created. It sets the content view to
     * `activity_data_display`, initializes the TextView elements, calls a function to update them
     * with data from a JSON object, and sets up click listeners for the about and badge buttons.
     *
     * @param savedInstanceState If the activity is being re-initialized after previously being shut
     *   down, this contains the data it most recently supplied in `onSaveInstanceState`.
     */
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_data_display)

        loadGoogleAds()

        // Call the function to update the TextView with cig count
//                deleteFormDataFile()

        // Initialize TextView elements for displaying data
        initializeDataDisplayTextView()

        println("---start.....")
        // Update the TextView elements with data from the JSON object
        loadData()
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

    private fun loadGoogleAds() {

        binding = ActivityDataDisplayBinding.inflate(layoutInflater)
        setContentView(binding.root)

        adView = AdView(this)
        binding.adViewContainer.addView(adView)

        // Log the Mobile Ads SDK version.
        Log.d(TAG, "Google Mobile Ads SDK Version: " + MobileAds.getVersion())

        googleMobileAdsConsentManager =
            GoogleMobileAdsConsentManager.getInstance(applicationContext)
        googleMobileAdsConsentManager.gatherConsent(this) { error ->
            if (error != null) {
                // Consent not obtained in current session.
                Log.d(TAG, "${error.errorCode}: ${error.message}")
            }

            if (googleMobileAdsConsentManager.canRequestAds) {
                initializeMobileAdsSdk()
            }

            if (googleMobileAdsConsentManager.isPrivacyOptionsRequired) {
                // Regenerate the options menu to include a privacy setting.
                invalidateOptionsMenu()
            }
        }

        // This sample attempts to load ads using consent obtained in the previous session.
        if (googleMobileAdsConsentManager.canRequestAds) {
            initializeMobileAdsSdk()
        }

        // Since we're loading the banner based on the adContainerView size, we need to wait until
        // this
        // view is laid out before we can get the width.
        binding.adViewContainer.viewTreeObserver.addOnGlobalLayoutListener {
            if (
                !initialLayoutComplete.getAndSet(true) &&
                googleMobileAdsConsentManager.canRequestAds
            ) {
                loadBanner()
            }
        }

        // Set your test devices. Check your logcat output for the hashed device ID to
        // get test ads on a physical device. e.g.
        // "Use RequestConfiguration.Builder().setTestDeviceIds(Arrays.asList("ABCDEF012345"))
        // to get test ads on this device."
        MobileAds.setRequestConfiguration(
            RequestConfiguration.Builder().setTestDeviceIds(listOf("ABCDEF012345")).build()
        )
    }

    private fun initializeDataDisplayTextView() {
        textViewDisplayCount = findViewById(R.id.displayCount)
        textViewDisplayMoney = findViewById(R.id.displayMoney)
        textViewDisplayDay = findViewById(R.id.displayDay)
    }

    /** Called when leaving the activity. */
    public override fun onPause() {
        adView.pause()
        super.onPause()
    }

    /** Called when returning to the activity. */
    public override fun onResume() {
        super.onResume()
        adView.resume()
    }

    /** Called before the activity is destroyed. */
    public override fun onDestroy() {
        adView.destroy()
        super.onDestroy()
    }

    //    override fun onCreateOptionsMenu(menu: Menu?): Boolean {
    //        menuInflater.inflate(R.menu.action_menu, menu)
    //        val moreMenu = menu?.findItem(R.id.action_more)
    //        moreMenu?.isVisible = googleMobileAdsConsentManager.isPrivacyOptionsRequired
    //        return super.onCreateOptionsMenu(menu)
    //    }

    //    override fun onOptionsItemSelected(item: MenuItem): Boolean {
    //        val menuItemView = findViewById<View>(item.itemId)
    //        PopupMenu(this, menuItemView).apply {
    //            menuInflater.inflate(R.menu.popup_menu, menu)
    //            show()
    //            setOnMenuItemClickListener { popupMenuItem ->
    //                when (popupMenuItem.itemId) {
    //                    R.id.privacy_settings -> {
    //                        // Handle changes to user consent.
    //
    // googleMobileAdsConsentManager.showPrivacyOptionsForm(this@MainActivity) { formError ->
    //                            if (formError != null) {
    //                                Toast.makeText(this@MainActivity, formError.message,
    // Toast.LENGTH_SHORT).show()
    //                            }
    //                        }
    //                        true
    //                    }
    //                    else -> false
    //                }
    //            }
    //            return super.onOptionsItemSelected(item)
    //        }
    //    }

    private fun loadBanner() {

        // This is an ad unit ID for a test ad. Replace with your own banner ad unit ID.
        //
        adView.adUnitId = "ca-app-pub-3940256099942544/9214589741"
        adView.setAdSize(adSize)

        // Create an ad request.
        val adRequest = AdRequest.Builder().build()

        // Start loading the ad in the background.
        adView.loadAd(adRequest)
    }

    private fun initializeMobileAdsSdk() {
        if (isMobileAdsInitializeCalled.getAndSet(true)) {
            return
        }

        // Initialize the Mobile Ads SDK.
        MobileAds.initialize(this) {}

        // Load an ad.
        if (initialLayoutComplete.get()) {
            loadBanner()
        }
    }

    /**
     * Updates TextView elements with values extracted from a JSON object.
     *
     * This function reads form data from a file, parses it into a `JSONObject`, extracts relevant
     * values, calculates additional metrics, and updates several TextView elements with the
     * formatted information.
     *
     * The TextView elements are updated with:
     * - The average number of cigarettes smoked per minute.
     * - The average amount of money spent on cigarettes per minute.
     * - The number of days since a specified date.
     */
    private fun loadData() {
        // Create an instance of BadgeActivity
        val badgeActivity = BadgeActivity()
        val mainActivity = MainActivity()

        val formData = readDataFromFile()
        val jsonObjectFormData = createJsonObjectFromFormData(formData)

        Log.d("JSON", "jsonObjectFormData: $jsonObjectFormData")

        val upComingAwardName = mainActivity.getStatusValueFromJsonObject(jsonObjectFormData, "next_award_detail", "animal")
        val upcomingAwardDurationInHour=mainActivity.getStatusValueFromJsonObject(jsonObjectFormData, "next_award_detail", "hourDuration")

        val dummyData = "NULL"
        var finalCountrySymbol: String? = null

        // Extract values from the JSON object
        val varOriginalObject = jsonObjectFormData.optJSONObject("original")
        val varCigarettePrice = varOriginalObject.optDouble("cigarettePrice")
        val varCountryObject = varOriginalObject.optJSONObject("country")
        val varCountryName = varCountryObject?.optString("country_name")
        val varCurrencyName = varCountryObject?.optString("currency_name")
        val varCountrySymbol = varCountryObject?.optString("currency_symbol")

        val varStatusObject = jsonObjectFormData.optJSONObject("status")
        val varNextAwardDatetime = varStatusObject.optString("next_award_datetime")


        finalCountrySymbol =
            if (isRtlLanguage(varCountrySymbol)) {
                "($varCurrencyName) "
            } else {
                varCountrySymbol
            }

        val varCreatedOn = varOriginalObject.optString("created_on")
        val varSmokesPerDay = varOriginalObject.optInt("smokesPerDay")
        val varStartYear = varOriginalObject.optString("startYear")

        // Calculate per minute metrics and past days
        val perMinuteSpent = perMinuteSpentMoney(varSmokesPerDay, varCigarettePrice)
        val perMinuteSmoked = perMinuteSmokedCigarette(varSmokesPerDay)
        val displayData = getDisplayData(varCreatedOn, perMinuteSpent, perMinuteSmoked)

        val totalCigarettesSmoked: Number? =
            displayData
                .find { it.containsKey("totalCigarettesSmoked") }
                ?.get("totalCigarettesSmoked") as? Number

        val totalMoneySpent: Number? =
            displayData.find { it.containsKey("totalMoneySpent") }?.get("totalMoneySpent")
                    as? Number

        //        val days = displayData.find { it.containsKey("days") }?.get("days")
        //        val hours = displayData.find { it.containsKey("hours") }?.get("hours")
        //        val minutes = displayData.find { it.containsKey("minutes") }?.get("minutes")
        val timeCompletedString = displayData.find { it.containsKey("dayString") }?.get("dayString")

        // Update TextView elements with the calculated and formatted information
        textViewDisplayCount.text =
            getString(
                R.string.displayCountMsgTemplate,
                totalCigarettesSmoked,
                dummyData,
                upComingAwardName
            )

        textViewDisplayMoney.text =
            getString(
                R.string.displayMoneyMsgTemplate,
                finalCountrySymbol,
                totalMoneySpent,
                finalCountrySymbol,
                totalMoneySpent,
                upComingAwardName
            )
        val timeRestString = getDateDiff(varNextAwardDatetime)
        textViewDisplayDay.text =
            getString(R.string.displayDayMsgTemplate, timeCompletedString, timeRestString, upComingAwardName)
    }

    private fun isRtlLanguage(text: String?): Boolean {
        return text?.let {
            val firstChar = it.getOrNull(0)
            firstChar != null &&
                    (Character.getDirectionality(firstChar) == Character.DIRECTIONALITY_RIGHT_TO_LEFT ||
                            Character.getDirectionality(firstChar) ==
                            Character.DIRECTIONALITY_RIGHT_TO_LEFT_ARABIC)
        } ?: false
    }

    private fun getDisplayData(
        fromDate: String,
        perMinuteSpent: Double,
        perMinuteSmoked: Double
    ): Array<Map<String, Any>> {

        // Date format in UTC
        val dateFormat = SimpleDateFormat("yyyy-MM-dd HH:mm:ss", Locale.getDefault())
        dateFormat.timeZone = TimeZone.getTimeZone("UTC")
        val startDate: Date =
            dateFormat.parse(fromDate) ?: throw IllegalArgumentException("Invalid date format")
        val currentDate = Date()

        // Calculate the time difference in milliseconds
        val diffInMillis = currentDate.time - startDate.time

        // Calculate the time difference in days, hours, and minutes
        val diffInMinutes = diffInMillis / (1000 * 60)
        val days = diffInMinutes / (24 * 60)
        val hours = (diffInMinutes % (24 * 60)) / 60
        val minutes = diffInMinutes % 60

        // Calculate the total number of cigarettes smoked and total money spent
        val totalCigarettesSmoked = ceil(perMinuteSmoked * diffInMinutes * 100) / 100
        val totalMoneySpent = ceil(perMinuteSpent * diffInMinutes * 100) / 100

        // Construct the result string
        val valDatString = StringBuilder()
        if (days > 0) {
            valDatString.append("${formatNumberWithCommas(days)} day(s) ")
        }
        if (hours > 0) {
            valDatString.append("${formatNumberWithCommas(hours)} hour(s) ")
        }
        if (minutes > 0) {
            valDatString.append("${formatNumberWithCommas(minutes)} minute(s)")
        } else{
            // @todo need to check
            valDatString.append("0 minute")
        }
        val dayString = valDatString.toString().trim()
        return arrayOf(
            mapOf("totalCigarettesSmoked" to totalCigarettesSmoked as Number),
            mapOf("totalMoneySpent" to totalMoneySpent as Number),
            mapOf("dayString" to dayString)
        )
    }

    /**
     * Calculates the amount of money spent on cigarettes per minute.
     *
     * This function takes the number of cigarettes smoked per day and the price of a single
     * cigarette, then calculates the average amount of money spent on cigarettes per minute. The
     * result is rounded up to two decimal places.
     *
     * @param smokesPerDay The number of cigarettes smoked per day.
     * @param cigarettePrice The price of a single cigarette.
     * @return A `Double` representing the amount of money spent on cigarettes per minute, rounded
     *   up to two decimal places.
     */
    private fun perMinuteSpentMoney(smokesPerDay: Int, cigarettePrice: Double): Double {
        val cigarettesPerMinute = smokesPerDay.toDouble() / 1440
        return ceil(cigarettesPerMinute * cigarettePrice * 100) /
                100 // Round up to two decimal places
    }

    /**
     * Calculates the average number of cigarettes smoked per minute.
     *
     * This function takes the total number of cigarettes smoked per day and calculates the average
     * number of cigarettes smoked per minute. The result is rounded up to two decimal places.
     *
     * @param smokesPerDay The number of cigarettes smoked per day.
     * @return A `Double` representing the average number of cigarettes smoked per minute, rounded
     *   up to two decimal places.
     */
    private fun perMinuteSmokedCigarette(smokesPerDay: Int): Double {
        return ceil(smokesPerDay.toDouble() / 1440 * 100) / 100 // Round up to two decimal places
    }

    /**
     * Creates a JSON object from the provided form data string.
     *
     * This function takes a string containing form data in JSON format and parses it into a
     * `JSONObject`.
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
     * This function opens the file named `formData.json` from the application's internal storage,
     * reads its contents line by line, and returns the entire content as a single string.
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
     * @deprecated v0.1 - not in use
     *
     * Calculates the difference between the provided date string and the current UTC time.
     *
     * This function takes a date string in the format "yyyy-MM-dd HH:mm:ss", parses it to a date
     * object, and then calculates the difference between this date and the current time in UTC. The
     * result is returned as a string representing the difference in days, hours, and minutes.
     *
     * For example:
     * - Input: "2024-05-17 12:00:00" -> Output: "1 day(s) 5 hour(s) 30 minute(s)" (assuming the
     *   current time is 2024-05-18 17:30:00 UTC)
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
        val diffInMillis = date.time - currentTime.time

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
     * This function checks if a file named `formData.json` exists in the application's files
     * directory. If the file exists, it attempts to delete it and logs the result.
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
     * This function takes a `Number` as input and returns a `String` representation of the number
     * with commas inserted at every thousandth place.
     *
     * For example:
     * - Input: 1234567 -> Output: "1,234,567"
     * - Input: 1234567.89 -> Output: "1,234,567.89"
     *
     * @param number The number to be formatted. This can be any subclass of `Number`.
     * @return A `String` representation of the number with commas as thousands separators.
     */
    private fun formatNumberWithCommas(number: Any): String {
        val numberString = number.toString()
        val parts = numberString.split(".")
        val wholePart = parts[0]
        val decimalPart = if (parts.size > 1) "." + parts[1] else ""
        val pattern = Regex("\\B(?=(\\d{3})+(?!\\d))")
        return wholePart.replace(pattern, ",") + decimalPart
    }
}

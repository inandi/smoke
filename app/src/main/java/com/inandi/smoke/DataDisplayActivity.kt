/**
 * DataDisplayActivity is responsible for displaying user's progress
 *
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

import android.app.AlertDialog
import android.content.Intent
import android.content.pm.PackageManager
import android.Manifest
import android.os.Build
import android.os.Bundle
import android.util.Log
import android.view.MenuInflater
import android.view.MenuItem
import android.view.View
import android.view.WindowMetrics
import android.widget.ImageButton
import android.widget.PopupMenu
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
import android.graphics.BitmapFactory
import android.os.Environment
import android.widget.Button
import android.widget.ImageView
import android.widget.Toast
import java.io.FileOutputStream

private const val TAG = "DataDisplayActivity"

/**
 * Activity to display data related to quit smoking progress, and handle ad loading and popup menu.
 */
class DataDisplayActivity : ComponentActivity() {

    private lateinit var textViewDisplayCount: TextView
    private lateinit var textViewDisplayMoney: TextView
    private lateinit var textViewDisplayDay: TextView
    private lateinit var textViewDisplayTotalHowManyCigSmoked: TextView
    private lateinit var textViewDisplayTotalHowMuchMoneySpent: TextView
    private val PERMISSION_REQUEST_CODE = 1001
    private val isMobileAdsInitializeCalled = AtomicBoolean(false)
    private val initialLayoutComplete = AtomicBoolean(false)
    private lateinit var binding: ActivityDataDisplayBinding
    private lateinit var adView: AdView
    private lateinit var googleMobileAdsConsentManager: GoogleMobileAdsConsentManager

    private lateinit var dataSet: DataSet
    private lateinit var setGetData: SetGetData

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
     * @since 0.1
     */
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_data_display)
        dataSet = DataSet()
        setGetData = SetGetData()

        // Initialize the ViewBinding
        binding = ActivityDataDisplayBinding.inflate(layoutInflater)
        setContentView(binding.root)

        loadGoogleAds()

        // Update the TextView elements with data from the JSON object
        loadData()

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

        // Set up the popup menu button
        val buttonShowPopup = binding.activityAboutBody.buttonAction
        buttonShowPopup.setOnClickListener { view ->
            showPopupMenu(view)
        }

        // Set up the penalty button and its click listener
        val penaltyButton = findViewById<Button>(R.id.button_add_penalty)
        penaltyButton.setOnClickListener {
            // Define the action to be performed when the button is clicked
            showPenaltyDialog()
        }
    }

    override fun onRequestPermissionsResult(
        requestCode: Int,
        permissions: Array<out String>,
        grantResults: IntArray,
    ) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults)
        if (requestCode == PERMISSION_REQUEST_CODE) {
            if ((grantResults.isNotEmpty() && grantResults[0] == PackageManager.PERMISSION_GRANTED)) {
                downloadUserDataFile()
            } else {
                Toast.makeText(
                    this,
                    "Permission denied. Unable to download file.",
                    Toast.LENGTH_LONG
                ).show()
            }
        }
    }

    private fun showPenaltyDialog() {
        updatePenaltyJson()
        loadData()
    }

    /**
     * Loads an image from the assets folder and sets it to the specified ImageView.
     *
     * @param assetPath The path to the image file in the assets folder. Must not be null.
     * @throws IOException If an error occurs while loading the image from assets.
     * @since 0.1
     */
    private fun loadImageFromAssets(assetPath: String) {
        val imageView: ImageView = findViewById(R.id.image_view)
        val assetManager = assets
        try {
            val inputStream = assetManager.open(assetPath)
            val bitmap = BitmapFactory.decodeStream(inputStream)
            imageView.setImageBitmap(bitmap)
        } catch (e: IOException) {
            e.printStackTrace()
        }
    }

    /**
     * Displays a popup menu anchored to the given view.
     *
     * This function creates and displays a popup menu when the specified view is clicked. The menu
     * items are inflated from the `popup_menu.xml` resource file. It also sets up a listener to handle
     * menu item clicks.
     *
     * @param view The view to which the popup menu will be anchored.
     * @since 0.1
     */
    private fun showPopupMenu(view: View) {
        val popup = PopupMenu(this, view)
        val inflater: MenuInflater = popup.menuInflater
        inflater.inflate(R.menu.popup_menu, popup.menu)
        popup.setOnMenuItemClickListener { menuItem ->
            handleMenuItemClick(menuItem)
        }
        popup.show()
    }

    /**
     * Handles the click events for the popup menu items.
     *
     * This function processes the click events for the popup menu items based on their IDs.
     * It performs different actions depending on which menu item was clicked. If the "Reload" menu
     * item is clicked, it calls `loadData()`. If the "Reset" menu item is clicked, it shows a
     * confirmation dialog for resetting.
     *
     * @param menuItem The menu item that was clicked.
     * @return `true` if the menu item click was handled, `false` otherwise.
     * @since 0.1
     */
    private fun handleMenuItemClick(menuItem: MenuItem): Boolean {
        return when (menuItem.itemId) {
            R.id.menu_option_reload -> {
                loadData()
                true
            }

            R.id.menu_option_reset -> {
                showResetConfirmationDialog()
                true
            }

            R.id.menu_option_download -> {
                checkPermissionsAndDownload()
                true
            }

            else -> false
        }
    }

    private fun checkPermissionsAndDownload() {
        if (checkSelfPermission(Manifest.permission.WRITE_EXTERNAL_STORAGE) != PackageManager.PERMISSION_GRANTED) {
            requestPermissions(
                arrayOf(
                    Manifest.permission.WRITE_EXTERNAL_STORAGE,
                    Manifest.permission.READ_EXTERNAL_STORAGE
                ),
                PERMISSION_REQUEST_CODE
            )
        } else {
            downloadUserDataFile()
        }
    }

    private fun downloadUserDataFile() {
        val fileName = MainActivity.FORM_DATA_FILENAME
        try {
            val fileInputStream = openFileInput(fileName)
            val fileContent = fileInputStream.bufferedReader().use { it.readText() }
            fileInputStream.close()

            val downloadsFolder =
                Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_DOWNLOADS)
            val outFile = File(downloadsFolder, fileName)
            val fileOutputStream = FileOutputStream(outFile)
            fileOutputStream.use {
                it.write(fileContent.toByteArray())
            }
            Toast.makeText(this, "File downloaded to: ${outFile.absolutePath}", Toast.LENGTH_LONG)
                .show()
        } catch (e: Exception) {
            e.printStackTrace()
            Toast.makeText(this, "Download failed: ${e.message}", Toast.LENGTH_LONG).show()
        }
    }

    /**
     * Displays a confirmation dialog for resetting.
     *
     * This function shows an AlertDialog to confirm whether the user wants to perform a reset action.
     * If the user selects "Yes", the `performResetAction` function is called. If the user selects "No",
     * the dialog is dismissed without taking any action.
     */
    private fun showResetConfirmationDialog() {
        val builder = AlertDialog.Builder(this)
        builder.setMessage("Are you sure you want to do it?")
            .setPositiveButton("Yes") { dialog, id ->
                performResetAction()
            }
            .setNegativeButton("No") { dialog, id ->
                dialog.dismiss()
            }
        builder.create().show()
    }

    /**
     * Performs the reset action by deleting form data and navigating to the main screen.
     *
     * This function first deletes the form data file by calling `deleteFormDataFile()`.
     * After the file is deleted, it navigates to the main screen by calling `navigateToMainScreen()`.
     */
    private fun performResetAction() {
        deleteFormDataFile()
        navigateToMainScreen()
    }

    /**
     * Navigates to the main screen of the application.
     *
     * This function starts the `MainActivity` by creating an intent and passing the current activity
     * context. It effectively transitions the user from the current `DataDisplayActivity` to the main
     * screen of the application.
     */
    private fun navigateToMainScreen() {
        startActivity(Intent(this@DataDisplayActivity, MainActivity::class.java))
    }

    /**
     * Loads Google ads, sets up the consent manager, and initializes the Mobile Ads SDK.
     */
    private fun loadGoogleAds() {
//        binding = ActivityDataDisplayBinding.inflate(layoutInflater)
//        setContentView(binding.root)

        adView = AdView(this)
        binding.activityAboutBody.adViewContainer.addView(adView)

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
        binding.activityAboutBody.adViewContainer.viewTreeObserver.addOnGlobalLayoutListener {
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
        textViewDisplayTotalHowManyCigSmoked = findViewById(R.id.displayTotalHowManyCigSmoked)
        textViewDisplayTotalHowMuchMoneySpent = findViewById(R.id.displayTotalHowMuchMoneySpent)
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

    private fun loadBanner() {
        // This is an ad unit ID for a test ad. Replace with your own banner ad unit ID.
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
     * Loads data from various sources and updates TextView elements with formatted information.
     *
     * This function performs the following tasks:
     * 1. Refreshes the JSON data.
     * 2. Initializes TextView elements for displaying data.
     * 3. Reads form data from a file and creates a JSONObject.
     * 4. Retrieves upcoming award name.
     * 5. Initializes variables and extracts relevant data from the JSON object.
     * 6. Calculates per minute metrics for money spent and cigarettes smoked.
     * 7. Prepares display data including total cigarettes smoked, total money spent, and formatted time strings.
     * 8. Updates TextView elements with the calculated and formatted information.
     *
     * @see updateJson
     * @see initializeDataDisplayTextView
     * @see readDataFromFile
     * @see createJsonObjectFromFormData
     * @see setGetData.getNextAwardDetailFromStatusKeyOfJsonObject
     * @see prepareDisplayData
     * @see perMinuteSpentMoney
     * @see perMinuteSmokedCigarette
     *
     * @throws IllegalArgumentException If there is an error parsing the JSON data or if the start date or
     * next award date has an invalid format.
     * @since 0.1
     */
    private fun loadData() {
        // Refresh the JSON data
        updateJson()

        // Initialize TextView elements for displaying data
        initializeDataDisplayTextView()

        // Read form data from file and create JSONObject
        val formData = readDataFromFile()
        val jsonObjectFormData = createJsonObjectFromFormData(formData)

        // Retrieve upcoming award name
        val upComingAwardName = setGetData.getNextAwardDetailFromStatusKeyOfJsonObject(
            jsonObjectFormData,
            "next_award_detail",
            "animal"
        )

        // Initialize variables
        var finalCountrySymbol: String? = null

        val varOriginalObject = jsonObjectFormData.optJSONObject("original")
        val varStatusObject = jsonObjectFormData.optJSONObject("status")

        val varCigarettePrice = varOriginalObject?.optDouble("cigarettePrice") ?: 0.0
        val varCountryObject = varOriginalObject?.optJSONObject("country")

        val varCountryName = varCountryObject?.optString("country_name")
        val varCurrencyName = varCountryObject?.optString("currency_name")
        val varCountrySymbol = varCountryObject?.optString("currency_symbol")

        val varNextAwardDatetime = varStatusObject?.optString("next_award_datetime") ?: ""

        // Determine the final country symbol
        finalCountrySymbol =
            if (isRtlLanguage(varCountrySymbol)) {
                "($varCurrencyName) "
            } else {
                varCountrySymbol
            }

        val varCreatedOn = varOriginalObject?.optString("created_on") ?: ""
        val varSmokesPerDay = varOriginalObject?.optInt("smokesPerDay") ?: 0
        val varStartYear = varOriginalObject?.optString("startYear")
        val varTotalMoneySpent = varOriginalObject?.optString("total_money_spent")
        val varTotalSmoked = varOriginalObject?.optString("total_smoked")

        // Calculate per minute spent money & cigarette smoked
        val perMinuteSpent = perMinuteSpentMoney(varSmokesPerDay, varCigarettePrice)
        val perMinuteSmoked = perMinuteSmokedCigarette(varSmokesPerDay)

        // Prepare display data
        val displayData =
            prepareDisplayData(varCreatedOn, varNextAwardDatetime, perMinuteSpent, perMinuteSmoked)

        // Extract relevant data from displayData
        val totalCigarettesSmoked: Number? =
            displayData
                .find { it.containsKey("totalCigarettesSmoked") }
                ?.get("totalCigarettesSmoked") as? Number

        val totalCigarettesSmokePending: Number? =
            displayData
                .find { it.containsKey("totalCigarettesSmokePending") }
                ?.get("totalCigarettesSmokePending") as? Number

        val totalMoneySpent: Number? =
            displayData.find { it.containsKey("totalMoneySpent") }?.get("totalMoneySpent")
                    as? Number

        val totalMoneySpentPending: Number? =
            displayData.find { it.containsKey("totalMoneySpentPending") }
                ?.get("totalMoneySpentPending")
                    as? Number

        val timeCompletedString =
            displayData.find { it.containsKey("timeCompletedString") }?.get("timeCompletedString")
        val timePendingString =
            displayData.find { it.containsKey("timePendingString") }?.get("timePendingString")

        // Update TextView elements with the calculated and formatted information
        textViewDisplayCount.text =
            getString(
                R.string.displayCountMsgTemplate,
                totalCigarettesSmoked?.let { setGetData.formatNumberWithCommas(it) },
                totalCigarettesSmokePending?.let { setGetData.formatNumberWithCommas(it) },
                upComingAwardName
            )

        textViewDisplayMoney.text =
            getString(
                R.string.displayMoneyMsgTemplate,
                finalCountrySymbol,
                totalMoneySpent?.let { setGetData.formatNumberWithCommas(it) },
                finalCountrySymbol,
                totalMoneySpentPending?.let { setGetData.formatNumberWithCommas(it) },
                upComingAwardName
            )

        textViewDisplayDay.text =
            getString(
                R.string.displayDayMsgTemplate,
                timeCompletedString,
                timePendingString,
                upComingAwardName
            )

        textViewDisplayTotalHowManyCigSmoked.text = getString(
            R.string.displayTotalHowManyCigSmokedMsgTemplate,
            varTotalSmoked
        )

        textViewDisplayTotalHowMuchMoneySpent.text = getString(
            R.string.displayTotalHowMuchMoneySpentMsgTemplate,
            finalCountrySymbol,
            varTotalMoneySpent
        )

        // load image
        val nextAwardPath: String? = setGetData.getNextAwardDetailFromStatusKeyOfJsonObject(
            jsonObjectFormData,
            "next_award_detail",
            "imageUrl"
        )
        nextAwardPath?.let {
            val strippedAssetPath = stripAssetPrefix(it)
            loadImageFromAssets(strippedAssetPath)
        }

        if (varStatusObject != null) {
            updatePenaltyButtonUI(varStatusObject)
        };
    }

    fun calculateTotalSpent(
        perMinuteSpent: Double,
        varStartYear: Int,
        varCreatedOn: String,
    ): String {
        // Define date format
        val dateFormat = SimpleDateFormat("yyyy-MM-dd HH:mm:ss", Locale.getDefault())

        // Parse varCreatedOn to Date
        val createdOnDate = dateFormat.parse(varCreatedOn)

        // Create a date for 1st January of varStartYear
        val startDate = dateFormat.parse("$varStartYear-01-01 00:00:00")

        // Check if the dates are parsed correctly
        if (createdOnDate == null || startDate == null) {
            throw IllegalArgumentException("Invalid date format")
        }

        // Calculate the difference in milliseconds
        val diffInMillis = createdOnDate.time - startDate.time

        // Convert milliseconds to minutes
        val diffInMinutes = diffInMillis / (1000 * 60)

        // Calculate the total money spent
        val totalMoneySpent = perMinuteSpent * diffInMinutes

        // Round the result to 2 decimal places and format it as a string
        return String.format("%.2f", totalMoneySpent)
    }

    /**
     * Strips the "file:///android_asset/" prefix from the given file path.
     *
     * @param filePath The file path to be processed. Must not be null.
     * @return The file path with the "file:///android_asset/" prefix removed. Returns the input filePath if the prefix is not present.
     * @since 0.1
     */
    private fun stripAssetPrefix(filePath: String): String {
        val prefix = "file:///android_asset/"
        return if (filePath.startsWith(prefix)) {
            filePath.removePrefix(prefix)
        } else {
            filePath
        }
    }

    /**
     * Checks if the given text is in a right-to-left (RTL) language.
     *
     * This function examines the first character of the provided text and determines if it belongs to
     * a right-to-left language script, such as Arabic or Hebrew.
     *
     * @param text The text to check for RTL language.
     * @return `true` if the text is in an RTL language, `false` otherwise.
     * @since 0.1
     */
    private fun isRtlLanguage(text: String?): Boolean {
        return text?.let {
            val firstChar = it.getOrNull(0)
            firstChar != null &&
                    (Character.getDirectionality(firstChar) == Character.DIRECTIONALITY_RIGHT_TO_LEFT ||
                            Character.getDirectionality(firstChar) ==
                            Character.DIRECTIONALITY_RIGHT_TO_LEFT_ARABIC)
        } ?: false
    }

    private fun updateJson() {
        val formData = readDataFromFile()
        val jsonObjectFormData = createJsonObjectFromFormData(formData)

        val statusObject = jsonObjectFormData.getJSONObject("status")
        val nextAwardDateTimeString = statusObject.getString("next_award_datetime")

        val varOriginalObject = jsonObjectFormData.optJSONObject("original")
        val varCreatedOnString = varOriginalObject?.optString("created_on") ?: ""
//        val dateFormat = SimpleDateFormat("yyyy-MM-dd HH:mm:ss")
//        dateFormat.timeZone = TimeZone.getTimeZone("UTC") // Assuming the date is in UTC
//        val varCreatedOn: Date = dateFormat.parse(varCreatedOnString)!!

        val sdf = SimpleDateFormat("yyyy-MM-dd HH:mm:ss")
        sdf.timeZone = TimeZone.getTimeZone("UTC")
        val nextAwardDateTime = sdf.parse(nextAwardDateTimeString)

        val currentTime = Calendar.getInstance(TimeZone.getTimeZone("UTC")).time

        // temp
//        val tenHoursAgo = Calendar.getInstance(TimeZone.getTimeZone("UTC"))
//        tenHoursAgo.add(Calendar.HOUR_OF_DAY, +35)
//        val currentTime=tenHoursAgo.time
        //temp

        if (currentTime.after(nextAwardDateTime)) {
            // Perform actions if current time in UTC is greater than next_award_datetime
            println("Current time in UTC is greater than next_award_datetime.")
            // Add your additional actions here

            // only update next award in form json
            var oneTimeUpdate = false;

            val progressArray = dataSet.quitSmokingProgress()
            val jsonObjectAwardAchievedProgressId = JSONObject()
            for (item in progressArray) {
                val progressId = item[0]
                val hourDuration = item[5].toDouble()
                val minutesDuration = hourDuration * 60L
                val nextProspectAwardDatetimeString =
                    setGetData.addMinutesToDateTime(varCreatedOnString, minutesDuration)
                val nextProspectAwardDatetime = sdf.parse(nextProspectAwardDatetimeString)

                if (currentTime.after(nextProspectAwardDatetime)) {
                    val jsonObjectAwardAchieved = JSONObject()
                    jsonObjectAwardAchieved.put("datetime", nextProspectAwardDatetimeString)
                    // score is on percentage
                    jsonObjectAwardAchieved.put("score", "40.12")
                    jsonObjectAwardAchievedProgressId.put(progressId, jsonObjectAwardAchieved)
                } else {
                    if (!oneTimeUpdate) {
//                        val outputFormat = SimpleDateFormat("yyyy-MM-dd HH:mm:ss")
                        statusObject.put(
                            "next_award_detail",
                            setGetData.getSmokingProgressById(progressId)
                        )
                        statusObject.put("next_award_datetime", nextProspectAwardDatetimeString)
                        oneTimeUpdate = true
                    }
                }
            }
            statusObject.put("award_achieved_timeline", jsonObjectAwardAchievedProgressId)
            val mainActivity = MainActivity()
            deleteFormDataFile()
            mainActivity.saveDataToFile(jsonObjectFormData, this)
        } else {
            println("Current time in UTC is not greater than next_award_datetime.")
        }
    }

    /**
     * Updates the UI of the penalty button based on the number of cigarettes smoked today.
     *
     * @param statusObject The status JSON object containing information about cigarettes smoked today.
     */
    private fun updatePenaltyButtonUI(statusObject: JSONObject) {
        // Get current date in UTC format
        val currentDate = setGetData.getCurrentDateTime("yyyy-MM-dd")

        // Check if 'smoked_today' key exists
        val smokedTodayObject = statusObject.optJSONObject("smoked_today")
        var smokedTodayValue = 0
        if (smokedTodayObject != null) {
            // If it exists, check if the current date exists as a key
            if (smokedTodayObject.has(currentDate)) {
                smokedTodayValue = smokedTodayObject.getInt(currentDate)
            }
        }
        
        // Find the penalty button in the layout
        val penaltyButton = findViewById<Button>(R.id.button_add_penalty)
        // Create the new text for the penalty button
        val newText = "One Cigarette, add penalty ($smokedTodayValue)"
        // Update the text of the penalty button
        penaltyButton.text = newText
    }

    /**
     * Updates the penalty information in the JSON data.
     * This function is triggered when the user incurs a penalty.
     * @since 0.1
     */
    private fun updatePenaltyJson() {
        // Read the form data from a file and convert it to a JSON object
        val formData = readDataFromFile()
        val jsonObjectFormData = createJsonObjectFromFormData(formData)

        // Extract the 'original' and 'status' JSON objects from the form data
        val varOriginalObject = jsonObjectFormData.optJSONObject("original")
        val statusObject = jsonObjectFormData.getJSONObject("status")

        // Get the current date in UTC format and the current year
        val currentDate = setGetData.getCurrentDateTime("yyyy-MM-dd")
        val currentYear = setGetData.getCurrentDateTime("yyyy")

        // Check if 'smoked_today' key exists in the status object
        val smokedTodayObject = statusObject.optJSONObject("smoked_today")
        val yearStatusObject = statusObject.optJSONObject("year_status")
        var smokedTodayValue = 0

        if (smokedTodayObject == null) {
            // If 'smoked_today' does not exist, create it with the current date set to 1
            val newSmokedTodayObject = JSONObject()
            smokedTodayValue = 1
            newSmokedTodayObject.put(currentDate, smokedTodayValue)
            statusObject.put("smoked_today", newSmokedTodayObject)
        } else {
            // If 'smoked_today' exists, check if the current date exists as a key
            if (smokedTodayObject.has(currentDate)) {
                // If the current date exists, increment its value
                smokedTodayValue = smokedTodayObject.getInt(currentDate) + 1
                smokedTodayObject.put(currentDate, smokedTodayValue)
            } else {
                // If the current date does not exist, reset all values to 0 and set the current date to 1
                smokedTodayObject.keys().forEach { key ->
                    smokedTodayObject.put(key, 0)
                }
                smokedTodayValue = 1
                statusObject.put("smoked_today", null)
                smokedTodayObject.put(currentDate, smokedTodayValue)
            }
            statusObject.put("smoked_today", smokedTodayObject)
        }

        // Update 'year_status' with the new data
        updateYearStatus(statusObject, yearStatusObject, currentDate, currentYear, smokedTodayValue)

        // Calculate the number of minutes one cigarette covers based on smokesPerDay
        val smokesPerDay = varOriginalObject.optInt("smokesPerDay", 0)
        // one cigarette covers this many minutes
        val smokeCoversMinute = ((24 / smokesPerDay) * 60).toDouble() // 24 hours * 60 minutes

        // Calculate the final value based on smoked_today and smokeCoversMinute, 2 is penalty
        val minutesDuration = smokedTodayValue * smokeCoversMinute * 2
        val currentNextAwardDatetime = statusObject.optString("next_award_datetime") ?: ""
        val nextProspectAwardDatetimeString =
            setGetData.addMinutesToDateTime(currentNextAwardDatetime, minutesDuration)
        statusObject.put("next_award_datetime", nextProspectAwardDatetimeString)

        // Save the updated data back to the file
        val mainActivity = MainActivity()
        deleteFormDataFile()
        mainActivity.saveDataToFile(jsonObjectFormData, this)
    }

    private fun updateYearStatus(
        statusObject: JSONObject?,
        yearStatusObject: JSONObject?,
        currentDate: String,
        currentYear: String,
        smokedTodayValue: Int,
    ) {
        if (yearStatusObject == null) {
            val smokeCurrentYear = JSONObject()
            val smokeCurrentDay = JSONObject()
            smokeCurrentDay.put(currentDate, smokedTodayValue)
            smokeCurrentYear.put(currentYear, smokeCurrentDay)
            statusObject?.put("year_status", smokeCurrentYear)
        } else {
            if (yearStatusObject.has(currentYear)) {
                val smokeCurrentYear = yearStatusObject.getJSONObject(currentYear)
                if (smokeCurrentYear.has(currentDate)) {
                    // Update the value for the current date
                    smokeCurrentYear.put(currentDate, smokedTodayValue)
                } else {
                    // Add the current date and set value to smokedTodayValue
                    smokeCurrentYear.put(currentDate, smokedTodayValue)
                }
            } else {
                // Add the current year with the current date and smokedTodayValue
                val smokeCurrentYear = JSONObject()
                smokeCurrentYear.put(currentDate, smokedTodayValue)
                yearStatusObject.put(currentYear, smokeCurrentYear)
            }
        }
    }

    /**
     * Prepares the display data based on start date, next award date, and per minute metrics.
     *
     * This function calculates the time difference between the current date and the start date,
     * and between the next award date and the current date. It then calculates the total cigarettes
     * smoked, total money spent, and the formatted time strings for both completed and pending times.
     *
     * @param startDate The start date in "yyyy-MM-dd HH:mm:ss" format (in UTC).
     * @param nextAwardDatetime The next award date in "yyyy-MM-dd HH:mm:ss" format (in UTC).
     * @param perMinuteSpent The average amount of money spent on cigarettes per minute.
     * @param perMinuteSmoked The average number of cigarettes smoked per minute.
     * @return An array of maps containing the following key-value pairs:
     *         - "totalCigarettesSmoked": The total number of cigarettes smoked.
     *         - "totalCigarettesSmokePending": The total number of pending cigarettes to smoke.
     *         - "totalMoneySpent": The total amount of money spent on cigarettes.
     *         - "totalMoneySpentPending": The total pending money to spend on cigarettes.
     *         - "timeCompletedString": The formatted string representing completed time.
     *         - "timePendingString": The formatted string representing pending time.
     * @throws IllegalArgumentException If the start date or next award date has an invalid format.
     * @since 0.1
     */
    private fun prepareDisplayData(
        startDate: String,
        nextAwardDatetime: String,
        perMinuteSpent: Double,
        perMinuteSmoked: Double,
    ): Array<Map<String, Any>> {
        val currentDate = setGetData.getCurrentDateTime();

        // Calculate the time (completed) difference in milliseconds
        val completedTimeDiffInMillis = setGetData.getSecondDifference(startDate, currentDate)
        setGetData.calculateTimeDifference(
            completedTimeDiffInMillis,
            perMinuteSpent,
            perMinuteSmoked
        )

        // Calculate the time (pending) difference in milliseconds
        val pendingTimeDiffInMillis = setGetData.getSecondDifference(currentDate, nextAwardDatetime)
        setGetData.calculateTimeDifference(
            pendingTimeDiffInMillis,
            perMinuteSpent,
            perMinuteSmoked,
            false
        )

        return arrayOf(
            mapOf("totalCigarettesSmoked" to setGetData.totalCigarettesSmoked as Number),
            mapOf("totalCigarettesSmokePending" to setGetData.totalCigarettesSmokePending as Number),
            mapOf("totalMoneySpent" to setGetData.totalMoneySpent as Number),
            mapOf("totalMoneySpentPending" to setGetData.totalMoneySpentPending as Number),
            mapOf("timeCompletedString" to setGetData.timeCompletedString),
            mapOf("timePendingString" to setGetData.timePendingString)
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
     * @since 0.1
     */
    fun perMinuteSpentMoney(smokesPerDay: Int, cigarettePrice: Double): Double {
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
     * @since 0.1
     */
    fun perMinuteSmokedCigarette(smokesPerDay: Int): Double {
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
     * @since 0.1
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
     * Deletes the form data file (formData.json) if it exists.
     *
     * This function checks if a file named `formData.json` exists in the application's files
     * directory. If the file exists, it attempts to delete it and logs the result.
     * - If the file is successfully deleted, it logs a success message.
     * - If the file fails to delete, it logs an error message.
     * - If the file does not exist, it logs a message indicating so.
     * @since 0.1
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

}

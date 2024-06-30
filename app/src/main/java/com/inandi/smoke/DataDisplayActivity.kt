/**
 * DataDisplayActivity is responsible for displaying user's progress
 *
 * @author Gobinda Nandi
 * @version 0.2
 * @since 2024-04-01
 * @copyright Copyright (c) 2024
 * @license This code is licensed under the MIT License.
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
import android.widget.EditText
import android.widget.ImageView
import android.widget.Toast
//import androidx.compose.ui.graphics.Color
import androidx.core.app.ActivityCompat
import com.db.williamchart.view.LineChartView
import org.json.JSONArray
import java.io.FileOutputStream
import android.graphics.Color
import android.widget.AdapterView
import android.widget.ArrayAdapter
import android.widget.Spinner
import com.db.williamchart.ExperimentalFeature
import kotlin.random.Random

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

    /**
     * This property calculates the appropriate AdSize for an adaptive banner ad
     * based on the current screen width and orientation.
     * Get the ad size with screen width.
     */
    private val adSize: AdSize
        get() {
            // Get display metrics of the device screen
            val displayMetrics = resources.displayMetrics
            // Calculate the width of the ad in pixels
            val adWidthPixels =
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.R) {
                    // For Android R (API level 30) and above, use WindowMetrics to get the current window width
                    val windowMetrics: WindowMetrics = this.windowManager.currentWindowMetrics
                    // Width in pixels
                    windowMetrics.bounds.width()
                } else {
                    // For Android versions below R, use DisplayMetrics to get the screen width
                    displayMetrics.widthPixels
                }

            // Get the screen density (e.g., 2.0 for 320 dpi)
            val density = displayMetrics.density

            // Convert the ad width from pixels to density-independent pixels (dp)
            val adWidth = (adWidthPixels / density).toInt()

            // Return the AdSize for the current orientation with the calculated ad width
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
//    @OptIn(ExperimentalFeature::class)
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

        val shareButton: Button = findViewById(R.id.button_share)
        // Set a click listener for the share button
        shareButton.setOnClickListener {
            share()
        }

    }

    /**
     * Generates a list of data points for the last specified number of days from the status JSON object.
     *
     * This method retrieves smoking data for the past specified number of days from the provided JSON object
     * and returns a list of date-value pairs to be used for displaying a line chart.
     *
     * @param days The number of days for which to generate the data points.
     * @param statusObject The JSON object containing the status data, including smoking records by date.
     * @return A sorted list of pairs where the first element is a string representing the date
     *         and the second element is a float representing the value for that date.
     * @since 0.2
     */
    private fun generateDataForLastDays(
        days: Int,
        statusObject: JSONObject,
    ): List<Pair<String, Float>> {
        // Define date format for parsing and formatting dates
        val dateFormat = SimpleDateFormat("yyyy-MM-dd", Locale.getDefault())

        // Initialize calendar to the current date
        val calendar = Calendar.getInstance()

        // List to store the generated data points
        val lineChartData = mutableListOf<Pair<String, Float>>()

        // Generate records for the specified number of days
        for (i in 0 until days) {
            // Format the current date as a string
            val date = dateFormat.format(calendar.time)

            // Extract the year from the date string
            val year = date.split("-")[0]

            // Retrieve the year data from the status JSON object
            val yearData = statusObject.optJSONObject("year_status")?.optJSONObject(year)

            // Retrieve the value for the current date, defaulting to 0 if not present
            val value = yearData?.optInt(date, 0)?.toFloat() ?: 0f

            // Add the date-value pair to the list
            lineChartData.add(date to value)

            // Move to the previous day
            calendar.add(Calendar.DAY_OF_YEAR, -1)
        }

        // Sort the data by date in ascending order
        return lineChartData.sortedBy { it.first }
    }

    /**
     * Loads and displays a line chart with the provided data set.
     *
     * This method configures a `LineChartView` with a given list of data points, sets up animation,
     * and handles data point touch events to display the selected data point information.
     *
     * @param lineSet A list of pairs where the first element is a string representing the label
     *                and the second element is a float representing the value for the line chart.
     * @since 0.2
     */
    @OptIn(ExperimentalFeature::class)
    private fun loadLineChart(lineSet: List<Pair<String, Float>>) {
        // Duration of the chart animation in milliseconds
        val animationDuration = 1000L

        // Find the LineChartView and TextView for displaying chart data
        val lineChart = findViewById<LineChartView>(R.id.lineChart)
        val tvChartData = findViewById<TextView>(R.id.tvChartData)

        // Set gradient fill colors for the line chart
        lineChart.gradientFillColors =
            intArrayOf(
                Color.parseColor("#70E805"),  // Start color
                Color.TRANSPARENT                       // End color
            )

        // Set the duration of the animation
        lineChart.animation.duration = animationDuration

        // Set the listener for data point touch events
        lineChart.onDataPointTouchListener = { index, _, _ ->
            // Display the data point information in the TextView
            tvChartData.text =
                "${lineSet.toList()[index].first}:  ${lineSet.toList()[index].second}"
        }

        // Animate the line chart with the provided data set
        lineChart.animate(lineSet)
    }

    /**
     * Shares the user's progress using a share intent.
     *
     * This method retrieves text from a hidden EditText field, formats it into a shareable template,
     * and initiates a share intent to allow the user to share their progress via available apps.
     * @since 0.2
     */
    private fun share() {
        // Retrieve the hidden field where the shareable information is stored
        val hiddenField: EditText = findViewById(R.id.hidden_field_share)
        val hiddenInfo = hiddenField.text.toString()

        // Create a template message including the user's progress and a download link for the app
        val template =
            "Check out my progress:\n\n$hiddenInfo\n\nDownload our app here: https://play.google.com/store/apps/details?id=com.inandi.smoke"

        // Prepare the share intent with the formatted template
        val shareIntent = Intent(Intent.ACTION_SEND)
        shareIntent.type = "text/plain"
        shareIntent.putExtra(Intent.EXTRA_TEXT, template)

        // Start the share activity using an intent chooser
        startActivity(Intent.createChooser(shareIntent, "Share"))
    }

    override fun onRequestPermissionsResult(
        requestCode: Int,
        permissions: Array<out String>,
        grantResults: IntArray,
    ) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults)
        if (requestCode == PERMISSION_REQUEST_CODE) {
            var allPermissionsGranted = true
            for (result in grantResults) {
                if (result != PackageManager.PERMISSION_GRANTED) {
                    allPermissionsGranted = false
                    break
                }
            }

            if (allPermissionsGranted) {
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

    /**
     * Displays the penalty dialog by updating the penalty JSON and loading the necessary data.
     *
     * This method first updates the penalty JSON data and then loads the relevant data required
     * to show the penalty dialog.
     * @since 0.2
     */
    private fun showPenaltyDialog() {
        // Update the penalty JSON data.
        updatePenaltyJson()

        // Load the necessary data to display the penalty dialog.
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
     * It performs different actions depending on which menu item was clicked.
     * If the "Reload" menu item is clicked, it calls `loadData()`.
     * If the "Reset" menu item is clicked, it shows a confirmation dialog for resetting.
     * If the "Download Progress" menu item is clicked, it `fromdata.json` will be downloaded to the device.
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
        if (ActivityCompat.checkSelfPermission(
                this,
                Manifest.permission.WRITE_EXTERNAL_STORAGE
            ) != PackageManager.PERMISSION_GRANTED
        ) {
            ActivityCompat.requestPermissions(
                this,
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
     * @since 0.1
     */
    private fun showResetConfirmationDialog() {
        val builder = AlertDialog.Builder(this)
        builder.setMessage("Are you sure you want to do it?")
            .setPositiveButton("Yes") { _, _ ->
                performResetAction()
            }
            .setNegativeButton("No") { dialog, _ ->
                dialog.dismiss()
            }
        builder.create().show()
    }

    /**
     * Performs the reset action by deleting form data and navigating to the main screen.
     *
     * This function first deletes the form data file by calling `deleteFormDataFile()`.
     * After the file is deleted, it navigates to the main screen by calling `navigateToMainScreen()`.
     * @since 0.1
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
     * @since 0.1
     */
    private fun navigateToMainScreen() {
        startActivity(Intent(this@DataDisplayActivity, MainActivity::class.java))
    }

    /**
     * Loads Google ads, sets up the consent manager, and initializes the Mobile Ads SDK.
     * @since 0.1
     */
    private fun loadGoogleAds() {
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

    /**
     * Initializes the TextView elements used for displaying various data on the screen.
     *
     * This method finds the TextView elements by their IDs and assigns them to the corresponding variables.
     * These TextView elements are used to display the count, money, day, total cigarettes smoked,
     * and total money spent in the UI.
     * @since 0.1
     */
    private fun initializeDataDisplayTextView() {
        // Find and assign the TextView for displaying the count
        textViewDisplayCount = findViewById(R.id.displayCount)

        // Find and assign the TextView for displaying the money
        textViewDisplayMoney = findViewById(R.id.displayMoney)

        // Find and assign the TextView for displaying the day
        textViewDisplayDay = findViewById(R.id.displayDay)

        // Find and assign the TextView for displaying the total number of cigarettes smoked
        textViewDisplayTotalHowManyCigSmoked = findViewById(R.id.displayTotalHowManyCigSmoked)

        // Find and assign the TextView for displaying the total money spent
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

    /**
     * Loads a banner ad into the AdView.
     *
     * This method configures the AdView with the appropriate ad unit ID and ad size,
     * creates an ad request, and starts loading the ad.
     * @since 0.1
     */
    private fun loadBanner() {
        // Set the ad unit ID for the AdView.
        // This is an ad unit ID for a test ad. Replace with your own banner ad unit ID.
        adView.adUnitId = "ca-app-pub-3940256099942544/9214589741"
        adView.setAdSize(adSize)

        // Create an ad request.
        val adRequest = AdRequest.Builder().build()

        // Start loading the ad in the background.
        adView.loadAd(adRequest)
    }

    /**
     * Initializes the Google Mobile Ads SDK and loads a banner ad.
     *
     * This method ensures that the Mobile Ads SDK is initialized only once.
     * If the SDK is already initialized, the method returns immediately.
     * After initialization, it attempts to load a banner ad if the initial layout is complete.
     * @since 0.1
     */
    private fun initializeMobileAdsSdk() {
        // Check if the Mobile Ads SDK has already been initialized.
        // If it has, return immediately to avoid re-initializing.
        if (isMobileAdsInitializeCalled.getAndSet(true)) {
            return
        }
        // Initialize the Mobile Ads SDK.
        MobileAds.initialize(this) {}
        // Load an ad.
        // Check if the initial layout is complete.
        // If it is, load a banner ad.
        if (initialLayoutComplete.get()) {
            loadBanner()
        }
    }

    /**
     * Loads and processes data for displaying various statistics and information in the UI.
     *
     * This method refreshes JSON data, initializes TextView elements, reads and processes form
     * data, and updates the UI with calculated and formatted information.
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

        // Extract original and status JSONObjects from formData.
        val varOriginalObject = jsonObjectFormData.optJSONObject("original")
        val varStatusObject = jsonObjectFormData.optJSONObject("status")

        // Retrieve cigarette price and country details.
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

        // Extract additional data from original JSONObject.
        val varCreatedOn = varOriginalObject?.optString("created_on") ?: ""
        val varSmokesPerDay = varOriginalObject?.optInt("smokesPerDay") ?: 0
        val varStartYear = varOriginalObject?.optString("startYear")
        val varTotalMoneySpent = varOriginalObject?.optString("total_money_spent")
        val varTotalSmoked = varOriginalObject?.optString("total_smoked")

        // Calculate per minute spent money & cigarette smoked based on the input
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

        // Prepare and set the share message.
        val shareMsg = getString(
            R.string.shareMsgTemplate,
            totalCigarettesSmoked?.let { setGetData.formatNumberWithCommas(it) },
            finalCountrySymbol,
            totalMoneySpent?.let { setGetData.formatNumberWithCommas(it) },
            timeCompletedString
        )
        val hiddenField: EditText = findViewById(R.id.hidden_field_share)
        hiddenField.setText(shareMsg)

        // Update additional TextView elements with total smoked and money spent data.
        textViewDisplayTotalHowManyCigSmoked.text = getString(
            R.string.displayTotalHowManyCigSmokedMsgTemplate,
            varTotalSmoked
        )

        textViewDisplayTotalHowMuchMoneySpent.text = getString(
            R.string.displayTotalHowMuchMoneySpentMsgTemplate,
            finalCountrySymbol,
            varTotalMoneySpent
        )

        // Load image for the next award.
        val nextAwardPath: String? = setGetData.getNextAwardDetailFromStatusKeyOfJsonObject(
            jsonObjectFormData,
            "next_award_detail",
            "imageUrl"
        )
        nextAwardPath?.let {
            val strippedAssetPath = stripAssetPrefix(it)
            loadImageFromAssets(strippedAssetPath)
        }

        // Update penalty button UI and setup chart dropdown if statusObject is not null.
        if (varStatusObject != null) {
            updatePenaltyButtonUI(varStatusObject)
            // set up chart dropdown
            setupChartSpinner(varStatusObject)
        };

    }

    /**
     * Sets up the chart options spinner and loads chart data based on the selected option.
     *
     * This method initializes the spinner with predefined options, sets an item selected listener
     * to handle user selection, and loads the corresponding chart data.
     *
     * @param statusObject The JSON object containing the status data for generating chart data.
     * @since 0.2
     */
    private fun setupChartSpinner(statusObject: JSONObject) {
        // Find the spinner view in the layout.
        val spinner: Spinner = findViewById(R.id.spinnerOptions)

//        val chartDataTextView: TextView = findViewById(R.id.tvChartData)

        // Create an ArrayAdapter using the string array and a default spinner layout
        ArrayAdapter.createFromResource(
            this,
            R.array.line_chart_options,
            android.R.layout.simple_spinner_item
        ).also { adapter ->
            // Specify the layout to use when the list of choices appears
            adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item)
            // Apply the adapter to the spinner
            spinner.adapter = adapter
        }

        // Set the listener for the spinner
        spinner.onItemSelectedListener = object : AdapterView.OnItemSelectedListener {

            override fun onItemSelected(
                parent: AdapterView<*>?,
                view: View?,
                position: Int,
                id: Long,
            ) {
                view?.let { // Use safe call operator to handle possible null view
                    when (position) {
                        0 -> { // "Last 7 days" selected
                            val data = generateDataForLastDays(7, statusObject)
                            loadLineChart(data)
                        }

                        1 -> { // "Last 2 weeks" selected
                            val data = generateDataForLastDays(14, statusObject)
                            loadLineChart(data)
                        }

                        2 -> { // "Last 30 days" selected
                            val data = generateDataForLastDays(30, statusObject)
                            loadLineChart(data)
                        }

                        3 -> { // "Last 3 months" selected
                            val data = generateDataForLastDays(90, statusObject)
                            loadLineChart(data)
                        }

                        4 -> { // "Last 12 months" selected
                            val data = generateDataForLastDays(365, statusObject)
                            loadLineChart(data)
                        }
                        // Handle other options if needed
                    }
                }
            }

            override fun onNothingSelected(parent: AdapterView<*>) {
                // Another interface callback
            }
        }
    }

    /**
     * Calculates the total money spent and cigarette smoked based on the rate of spending per minute and the duration
     * between a start year and a created date.
     *
     * This function computes the total money spent by determining the difference in time between the
     * start of the specified year and the creation date, then multiplying the difference by the per
     * minute spending rate.
     *
     * @param perMinuteSpent The rate of spending per minute.
     * @param varStartYear The starting year as an integer.
     * @param varCreatedOn The creation date as a string in the format "yyyy-MM-dd HH:mm:ss".
     * @return The total money spent, formatted as a string with 2 decimal places.
     * @throws IllegalArgumentException if the date format is invalid.
     * @since 0.2
     */
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
     * @return The file path with the "file:///android_asset/" prefix removed. Returns the input
     * filePath if the prefix is not present.
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

    /**
     * Updates the JSON data based on current conditions and status parameters.
     *
     * This method reads data from a file, processes it into a JSON object, and updates various
     * parameters such as next award date/time, progress timelines, and scores based on current time
     * conditions.
     * @since 0.1
     */
    private fun updateJson() {
        // Read form data from file and create JSONObject
        val formData = readDataFromFile()
        val jsonObjectFormData = createJsonObjectFromFormData(formData)

        // Extract necessary objects and values from the JSON data
        val statusObject = jsonObjectFormData.getJSONObject("status")
        val nextAwardDateTimeString = statusObject.getString("next_award_datetime")
        val existingAwardAchievedTimeline = statusObject.optJSONObject("award_achieved_timeline")

        val varOriginalObject = jsonObjectFormData.optJSONObject("original")
        val varCreatedOnString = varOriginalObject?.optString("created_on") ?: ""
        val varSmokesPerDay = varOriginalObject?.optInt("smokesPerDay") ?: 0

        // Define date format and parse next award date/time
        val sdf = SimpleDateFormat("yyyy-MM-dd HH:mm:ss")
        sdf.timeZone = TimeZone.getTimeZone("UTC")
        val nextAwardDateTime = sdf.parse(nextAwardDateTimeString)

        // Get current UTC time
        val currentTime = Calendar.getInstance(TimeZone.getTimeZone("UTC")).time

//        val tenHoursAgo = Calendar.getInstance(TimeZone.getTimeZone("UTC"))
//        tenHoursAgo.add(Calendar.HOUR_OF_DAY, +255)
//        val currentTime = tenHoursAgo.time

        // Perform actions if current time in UTC is greater than next_award_datetime
        if (currentTime.after(nextAwardDateTime)) {
            var oneTimeUpdate = false;
            val progressArray = dataSet.quitSmokingProgress()
            val jsonObjectAwardAchievedProgressId = JSONObject()

            // Iterate through each smoking progress item
            for (item in progressArray) {
                val progressId = item[0]
                val hourDuration = item[5].toDouble()
                val minutesDuration = hourDuration * 60L

                // Calculate the prospective next award datetime based on smoking progress
                val nextProspectAwardDatetimeString =
                    setGetData.addMinutesToDateTime(varCreatedOnString, minutesDuration)
                val nextProspectAwardDatetime = sdf.parse(nextProspectAwardDatetimeString)
                var currentScore = 100.00

                // Check if current time exceeds the next prospective award datetime
                if (currentTime.after(nextProspectAwardDatetime)) {
                    // Initialize or update award achievement details
                    if (existingAwardAchievedTimeline == null) {
                        // Initialize new award achieved timeline if not present
                        val jsonObjectAwardAchievedSmoked = JSONObject()
                        val jsonObjectAwardAchievedSmokedDetail = JSONObject()

                        jsonObjectAwardAchievedSmokedDetail.put(
                            "datetime",
                            nextProspectAwardDatetimeString
                        )
                        jsonObjectAwardAchievedSmokedDetail.put("score", currentScore)
                        jsonObjectAwardAchievedSmoked.put(
                            progressId,
                            jsonObjectAwardAchievedSmokedDetail
                        )

                        val jsonObjectAwardAchievedProgressId = setGetData.mergeJsonObjects(
                            jsonObjectAwardAchievedProgressId,
                            jsonObjectAwardAchievedSmoked
                        )
                    } else {
                        // Update existing award achieved timeline if present
                        if (existingAwardAchievedTimeline.has(progressId)) {
                            val award1Object =
                                existingAwardAchievedTimeline.optJSONObject(progressId)

                            if (award1Object.has("smoked")) {
                                // Calculate the number of cigarettes smoked for the current award
                                val smokedCurrentAward =
                                    (award1Object.optJSONArray("smoked") ?: JSONArray()).length()

                                val perMinuteSmoked = perMinuteSmokedCigarette(varSmokesPerDay)
                                val totalRangeOfSmokeCurrentAwardDuration =
                                    minutesDuration * perMinuteSmoked

                                // Calculate the current score based on smoking progress
                                currentScore = setGetData.calculatePercentage(
                                    (totalRangeOfSmokeCurrentAwardDuration - smokedCurrentAward),
                                    totalRangeOfSmokeCurrentAwardDuration
                                )

                            }

                            // Prepare JSON object for the achieved award
                            val jsonObjectAwardAchieved = JSONObject()
                            jsonObjectAwardAchieved.put("datetime", nextProspectAwardDatetimeString)
                            jsonObjectAwardAchieved.put("score", currentScore)

                            // Merge existing award data with the new award details
                            val awardAchievedTimelinetemp =
                                setGetData.mergeJsonObjects(award1Object, jsonObjectAwardAchieved)

                            existingAwardAchievedTimeline.put(
                                progressId,
                                awardAchievedTimelinetemp
                            )

                        } else {
                            // Add new award achieved if progressId not present
                            val jsonObjectAwardAchieved = JSONObject()
                            jsonObjectAwardAchieved.put("datetime", nextProspectAwardDatetimeString)
                            jsonObjectAwardAchieved.put("score", currentScore)
                            existingAwardAchievedTimeline.put(
                                progressId,
                                jsonObjectAwardAchieved
                            )
                        }
                    }
                } else {
                    // Update next award details if not already updated
                    if (!oneTimeUpdate) {
                        statusObject.put(
                            "next_award_detail",
                            setGetData.getSmokingProgressById(progressId)
                        )
                        statusObject.put("next_award_datetime", nextProspectAwardDatetimeString)
                        oneTimeUpdate = true
                    }
                }
            }

            // Merge and update the award achieved timeline in the main status object
            val awardAchievedTimeline = existingAwardAchievedTimeline?.let {
                setGetData.mergeJsonObjects(jsonObjectAwardAchievedProgressId, it)
            } ?: jsonObjectAwardAchievedProgressId

            statusObject.put("award_achieved_timeline", awardAchievedTimeline)

            // Save updated JSON data back to file
            val mainActivity = MainActivity()
            deleteFormDataFile()
            mainActivity.saveDataToFile(jsonObjectFormData, this)
        } else {
            // Handle case where current time in UTC is not greater than next_award_datetime
        }
    }

    /**
     * Updates the UI of the penalty button based on the number of cigarettes smoked today.
     *
     * @param statusObject The status JSON object containing information about cigarettes smoked today.
     * @since 0.1
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
                statusObject.put("smoked_today", smokedTodayObject)
            } else {
                val newSmokedTodayObject = JSONObject()
                smokedTodayValue = 1
                newSmokedTodayObject.put(currentDate, smokedTodayValue)
                statusObject.put("smoked_today", newSmokedTodayObject)
            }
        }

        val existingAwardAchievedTimeline = statusObject.optJSONObject("award_achieved_timeline")

        val nextAwardDetailNumber = setGetData.getNextAwardDetailFromStatusKeyOfJsonObject(
            jsonObjectFormData,
            "next_award_detail",
            "number"
        );

        if (existingAwardAchievedTimeline == null) {

            val jsonObjectAwardAchievedSmoked = JSONObject()
            val jsonObjectAwardAchievedSmokedDetail = JSONObject()

            if (!jsonObjectAwardAchievedSmokedDetail.has("smoked")) {
                jsonObjectAwardAchievedSmokedDetail.put("smoked", JSONArray())
            }

            val eachSmokeArray =
                jsonObjectAwardAchievedSmokedDetail.optJSONArray("smoked") ?: JSONArray()
            eachSmokeArray.put(setGetData.getCurrentDateTime())
            jsonObjectAwardAchievedSmokedDetail.put("smoked", eachSmokeArray)

            jsonObjectAwardAchievedSmoked.put(
                nextAwardDetailNumber,
                jsonObjectAwardAchievedSmokedDetail
            )
            statusObject.put("award_achieved_timeline", jsonObjectAwardAchievedSmoked)
        } else {

            if (existingAwardAchievedTimeline.has(nextAwardDetailNumber)) {
                val award1Object =
                    existingAwardAchievedTimeline.optJSONObject(nextAwardDetailNumber)

                if (award1Object.has("smoked")) {

                    val eachSmokeArray = award1Object.optJSONArray("smoked") ?: JSONArray()
                    eachSmokeArray.put(setGetData.getCurrentDateTime())
                    award1Object.put("smoked", eachSmokeArray)
                }

            } else {
                val jsonObjectAwardAchievedSmoked = JSONObject()
                val jsonObjectAwardAchievedSmokedDetail = JSONObject()

                if (!jsonObjectAwardAchievedSmokedDetail.has("smoked")) {
                    jsonObjectAwardAchievedSmokedDetail.put("smoked", JSONArray())
                }

                val eachSmokeArray =
                    jsonObjectAwardAchievedSmokedDetail.optJSONArray("smoked") ?: JSONArray()
                eachSmokeArray.put(setGetData.getCurrentDateTime())
                jsonObjectAwardAchievedSmokedDetail.put("smoked", eachSmokeArray)

                jsonObjectAwardAchievedSmoked.put(
                    nextAwardDetailNumber,
                    jsonObjectAwardAchievedSmokedDetail
                )
                statusObject.put(
                    "award_achieved_timeline",
                    setGetData.mergeJsonObjects(
                        existingAwardAchievedTimeline,
                        jsonObjectAwardAchievedSmoked
                    )
                )
            }
        }

//        addDateTimeToEachSmoke(statusObject, setGetData.getCurrentDateTime())

        // Update 'year_status' with the new data
        updateYearStatus(statusObject, yearStatusObject, currentDate, currentYear, smokedTodayValue)

        // Save the updated data back to the file
        val mainActivity = MainActivity()
        deleteFormDataFile()
        mainActivity.saveDataToFile(jsonObjectFormData, this)
    }

    /**
     * @deprecated 0.2 - This function is no longer in use.
     *
     * Adds a date and time entry to the "each_smoke" array within the given JSON object.
     *
     * This method ensures that the "each_smoke" array exists in the provided JSON object, then adds
     * the specified date and time to this array.
     *
     * @param statusObject The JSON object representing the status data.
     * @param dateTime The date and time to add, as a string.
     * @since 0.2
     */
    private fun addDateTimeToEachSmoke(statusObject: JSONObject, dateTime: String) {
        // Check if the "each_smoke" array exists in the JSON object; if not, create it.
        if (!statusObject.has("each_smoke")) {
            statusObject.put("each_smoke", JSONArray())
        }

        // Retrieve the "each_smoke" array from the JSON object, or create a new one if it doesn't exist.
        val eachSmokeArray = statusObject.optJSONArray("each_smoke") ?: JSONArray()
        // Add the dateTime string to the "each_smoke" array.
        eachSmokeArray.put(dateTime)
        // Update the "each_smoke" array in the JSON object with the new array.
        statusObject.put("each_smoke", eachSmokeArray)
    }

    /**
     * Updates the year status within the provided JSON objects by adding or updating the number of
     * smokes for the current date and year.
     *
     * This method ensures that the year status is accurately maintained within the status JSON object,
     * either by updating existing entries or creating new ones as necessary.
     *
     * @param statusObject The main status JSON object which may contain the "year_status".
     * @param yearStatusObject The JSON object representing the year status within the main status object.
     * @param currentDate The current date as a string, formatted as "yyyy-MM-dd".
     * @param currentYear The current year as a string, formatted as "yyyy".
     * @param smokedTodayValue The number of smokes recorded for the current date.
     * @since 0.2
     */
    private fun updateYearStatus(
        statusObject: JSONObject?,
        yearStatusObject: JSONObject?,
        currentDate: String,
        currentYear: String,
        smokedTodayValue: Int,
    ) {
        if (yearStatusObject == null) {
            // If yearStatusObject is null, create a new year status for the current year.
            val smokeCurrentYear = JSONObject()
            val smokeCurrentDay = JSONObject()
            smokeCurrentDay.put(currentDate, smokedTodayValue)
            smokeCurrentYear.put(currentYear, smokeCurrentDay)
            statusObject?.put("year_status", smokeCurrentYear)
        } else {
            if (yearStatusObject.has(currentYear)) {
                // If the current year already exists, update or add the current date within that year.
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

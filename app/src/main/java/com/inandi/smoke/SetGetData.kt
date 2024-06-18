package com.inandi.smoke

import org.json.JSONException
import java.text.SimpleDateFormat
import java.util.Calendar
import java.util.Date
import java.util.Locale
import java.util.TimeZone
import kotlin.math.ceil
import org.json.JSONObject
import kotlin.math.pow

class SetGetData {

    private val dataSet = DataSet()

    private var diffInMinutes: Long = 0
    private var days: Long = 0
    private var hours: Long = 0
    private var minutes: Long = 0
    var timeCompletedString: String = ""
    var timePendingString: String = ""
    var totalCigarettesSmoked: Number = 0
    var totalMoneySpent: Number = 0
    var totalCigarettesSmokePending: Number = 0
    var totalMoneySpentPending: Number = 0

    /**
     * Retrieves smoking progress information by ID from the data set.
     *
     * This function searches for smoking progress information in the data set based on the provided ID.
     * If a match is found, it creates and returns a JSONObject containing details such as number, time frame,
     * animal, description, image URL, and hour duration. If no match is found, it returns null.
     *
     * @param id The ID of the smoking progress to retrieve.
     * @return A JSONObject containing smoking progress details if found, or null if not found.
     * @since 0.1
     */
    fun getSmokingProgressById(id: String): JSONObject? {
        val progressArray = dataSet.quitSmokingProgress()
        for (item in progressArray) {
            if (item[0] == id) {
                val obj = JSONObject()
                obj.put("number", item[0])
                obj.put("timeFrame", item[1])
                obj.put("animal", item[2])
                obj.put("description", item[3])
                obj.put("imageUrl", item[4])
                obj.put("hourDuration", item[5])
                return obj
            }
        }
        return null // Return null if ID is not found
    }


    /**
     * Calculates and formats the difference in time given in seconds, and computes
     * the total cigarettes smoked and money spent based on the time difference and rates provided.
     *
     * @param diffInSeconds The time difference in seconds.
     * @param perMinuteSpent The amount of money spent per minute.
     * @param perMinuteSmoked The number of cigarettes smoked per minute.
     * @param completedScenario A boolean indicating if the scenario is completed or pending.
     * @since 0.1
     */
    fun calculateTimeDifference(
        diffInSeconds: Long,
        perMinuteSpent: Double,
        perMinuteSmoked: Double,
        completedScenario: Boolean = true,
    ) {
        // Convert the time difference from seconds to minutes
        diffInMinutes = diffInSeconds / 60

        // Calculate the number of days, hours, and minutes from the total minutes
        days = diffInMinutes / (24 * 60)
        hours = (diffInMinutes % (24 * 60)) / 60
        minutes = diffInMinutes % 60

        // Construct the result string for formatted time difference
        val valDatString = StringBuilder()
        if (days > 0) {
            valDatString.append("${formatNumberWithCommas(days)} day(s) ")
        }
        if (hours > 0) {
            valDatString.append("${formatNumberWithCommas(hours)} hour(s) ")
        }
        if (minutes > 0) {
            valDatString.append("${formatNumberWithCommas(minutes)} minute(s)")
        } else {
            valDatString.append("0 minute")
        }

        // Calculate and assign the total cigarettes smoked and money spent based on the scenario completion
        if (completedScenario) {
            totalCigarettesSmoked = calculateTotalCigarettesSmoked(diffInMinutes, perMinuteSmoked)
            totalMoneySpent = calculateTotalMoneySpent(diffInMinutes, perMinuteSpent)
            timeCompletedString = valDatString.toString().trim()
        } else {
            totalCigarettesSmokePending =
                calculateTotalCigarettesSmoked(diffInMinutes, perMinuteSmoked)
            totalMoneySpentPending = calculateTotalMoneySpent(diffInMinutes, perMinuteSpent)
            timePendingString = valDatString.toString().trim()
        }
    }

    /**
     * Calculates the total number of cigarettes smoked based on the difference in minutes
     * and per-minute smoking rate.
     *
     * This function computes the total number of cigarettes smoked by multiplying the per-minute smoking rate
     * with the difference in minutes and rounding the result to two decimal places.
     *
     * @param diffInMinutes The difference in minutes for which the cigarettes are smoked.
     * @param perMinuteSmoked The average number of cigarettes smoked per minute.
     * @return The total number of cigarettes smoked.
     * @since 0.1
     */
    private fun calculateTotalCigarettesSmoked(
        diffInMinutes: Long,
        perMinuteSmoked: Double,
    ): Double {
        return ceil(perMinuteSmoked * diffInMinutes * 100) / 100
    }

    /**
     * Calculates the total money spent based on the difference in minutes and per-minute spending rate.
     *
     * This function computes the total money spent on cigarettes by multiplying the per-minute spending rate
     * with the difference in minutes and rounding the result to two decimal places.
     *
     * @param diffInMinutes The difference in minutes for which the money is spent.
     * @param perMinuteSpent The average amount of money spent on cigarettes per minute.
     * @return The total amount of money spent on cigarettes.
     * @since 0.1
     */
    private fun calculateTotalMoneySpent(diffInMinutes: Long, perMinuteSpent: Double): Double {
        return ceil(perMinuteSpent * diffInMinutes * 100) / 100
    }

    /**
     * Retrieves the next award detail value from a JSON object under a specified status key.
     *
     * This function extracts the next award detail value from the provided JSON object under
     * the specified detail key and returns the value associated with the provided value key.
     *
     * @param jsonObject The JSON object from which to retrieve the next award detail.
     * @param detailKey The key under which the next award detail is stored in the status object.
     * @param valueKey The key corresponding to the value to be retrieved from the next award detail.
     * @return The value associated with the value key in the next award detail, or null if not found.
     * @since 0.1
     */
    fun getNextAwardDetailFromStatusKeyOfJsonObject(
        jsonObject: JSONObject,
        detailKey: String,
        valueKey: String,
    ): String? {
        // Check if "status" key exists and is a JSONObject
        val statusObject = jsonObject.optJSONObject("status") ?: return null

        // Check if detailKey exists and is a non-empty string
        val nextAwardDetail =
            statusObject.optString(detailKey).takeIf { it.isNotEmpty() } ?: return null

        // Parse the nextAwardDetail into a JSONObject
        val nextAwardJson = try {
            JSONObject(nextAwardDetail)
        } catch (e: JSONException) {
            return null
        }

        // Return the value associated with valueKey or null if it doesn't exist
        return nextAwardJson.optString(valueKey).takeIf { it.isNotEmpty() }
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
     * @param dateString The date string to be compared, in the format "yyyy-MM-dd HH:mm:ss".
     * @return A `String` representing the difference in days, hours, and minutes.
     * @since 0.1
     *
     * For example:
     * - Input: "2024-05-17 12:00:00" -> Output: "1 day(s) 5 hour(s) 30 minute(s)" (assuming the
     *   current time is 2024-05-18 17:30:00 UTC)
     */
    fun getDateDiff(dateString: String): String {
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
     * Adds the specified number of minutes to a given datetime string.
     *
     * @param datetime The input datetime string in "yyyy-MM-dd HH:mm:ss" format.
     * @param minutesToAdd The number of minutes to add.
     * @return The new datetime string after adding the specified minutes.
     * @since 0.1
     */
    fun addMinutesToDateTime(datetime: String, minutesToAdd: Double): String {
        val dateFormat = SimpleDateFormat("yyyy-MM-dd HH:mm:ss")
        dateFormat.timeZone = TimeZone.getTimeZone("UTC")

        // Parse the input datetime string
        val date = dateFormat.parse(datetime)

        // Calculate the new datetime by adding minutes
        val newDate = Date(date.time + (minutesToAdd * 60000).toLong())

        // Format the new datetime to string
        return dateFormat.format(newDate)
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
     * @since 0.1
     */
    fun formatNumberWithCommas(number: Any): String {
        val numberString = number.toString()
        val parts = numberString.split(".")
        val wholePart = parts[0]
        val decimalPart = if (parts.size > 1) "." + parts[1] else ""
        val pattern = Regex("\\B(?=(\\d{3})+(?!\\d))")
        return wholePart.replace(pattern, ",") + decimalPart
    }

    /**
     * Gets the current date and time formatted according to the specified format.
     *
     * @param format The desired date and time format (default is "yyyy-MM-dd HH:mm:ss").
     * @return The formatted current date and time.
     * @since 0.1
     */
    fun getCurrentDateTime(format: String = "yyyy-MM-dd HH:mm:ss"): String {
        val sdf = SimpleDateFormat(format, Locale.getDefault())
        sdf.timeZone = TimeZone.getTimeZone("UTC") // Set the time zone to UTC
        return sdf.format(Date()) // Format the current date and time
    }

    fun getCurrentDateTimePlusXDays(format: String = "yyyy-MM-dd HH:mm:ss"): String {
        val calendar = Calendar.getInstance(TimeZone.getTimeZone("UTC"))
        calendar.add(Calendar.DAY_OF_YEAR, 1) // Add two days to the current date
        val sdf = SimpleDateFormat(format, Locale.getDefault())
        sdf.timeZone = TimeZone.getTimeZone("UTC")
        return sdf.format(calendar.time)
    }

    fun calculatePercentage(part: Double, total: Double): Double {
        if (total.toInt() == 0 || part < 0) {
            return 0.0
        }
        val percentage = (part.toDouble() / total.toDouble()) * 100
        return ceil(percentage * 10.0.pow(2)) / 10.0.pow(2)
    }


    /**
     * Calculates the difference in seconds between two date strings.
     *
     * @param date1 The first date string in "yyyy-MM-dd HH:mm:ss" format.
     * @param date2 The second date string in "yyyy-MM-dd HH:mm:ss" format. This should bigger than date1.
     * @return The difference in seconds between the two dates.
     * @since 0.1
     */
    fun getSecondDifference(date1: String, date2: String): Long {
        val dateFormat = SimpleDateFormat("yyyy-MM-dd HH:mm:ss")
        val date1Obj = dateFormat.parse(date1)
        val date2Obj = dateFormat.parse(date2)
        val diff = date2Obj.time - date1Obj.time
        val seconds = diff / 1000
        return seconds
    }
}

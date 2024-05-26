package com.inandi.smoke

import java.text.SimpleDateFormat
import java.util.Calendar
import java.util.Date
import java.util.Locale
import java.util.TimeZone
import kotlin.math.ceil
import org.json.JSONObject

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
     * Calculates the time difference and related metrics based on the provided milliseconds and per-minute rates.
     *
     * This function calculates the time difference in minutes from the provided milliseconds and then computes
     * the number of days, hours, and minutes. It also constructs a result string representing the time difference.
     * Additionally, it calculates the total number of cigarettes smoked and the total money spent based on the
     * per-minute rates. If the `completedScenario` flag is true, it calculates for the completed scenario; otherwise,
     * it calculates for the pending scenario.
     *
     * @param diffInMillis The difference in milliseconds for which to calculate the time difference.
     * @param perMinuteSpent The average amount of money spent on cigarettes per minute.
     * @param perMinuteSmoked The average number of cigarettes smoked per minute.
     * @param completedScenario A boolean flag indicating whether to calculate for the completed scenario (default: true).
     */
    fun calculateTimeDifference(
        diffInMillis: Long,
        perMinuteSpent: Double,
        perMinuteSmoked: Double,
        completedScenario: Boolean = true
    ) {
        diffInMinutes = diffInMillis / (1000 * 60)
        days = diffInMinutes / (24 * 60)
        hours = (diffInMinutes % (24 * 60)) / 60
        minutes = diffInMinutes % 60

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
        } else {
            valDatString.append("0 minute")
        }
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
     */
    private fun calculateTotalCigarettesSmoked(
        diffInMinutes: Long,
        perMinuteSmoked: Double
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
     */
    fun getNextAwardDetailFromStatusKeyOfJsonObject(
        jsonObject: JSONObject,
        detailKey: String,
        valueKey: String
    ): String? {
        val statusObject = jsonObject.getJSONObject("status")
        val nextAwardDetail = statusObject.optString(detailKey)
        val nextAwardJson = JSONObject(nextAwardDetail)
        return nextAwardJson.optString(valueKey)
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
     * Adds the specified number of minutes to a given date and time string.
     *
     * This function parses the input date and time string using the format "yyyy-MM-dd HH:mm:ss",
     * then adds the specified number of minutes to it. The resulting date and time are formatted
     * back into a string using the same format and returned.
     *
     * @param dateTime The input date and time string in the format "yyyy-MM-dd HH:mm:ss".
     * @param minutesToAdd The number of minutes to add to the date and time.
     * @return A string representing the date and time after adding the specified minutes.
     */
    fun addMinutesToDateTime(dateTime: Date, minutesToAdd: Double): Date {
//        val formatter = SimpleDateFormat("yyyy-MM-dd HH:mm:ss")
//        val date = formatter.parse(dateTime)
//        val calendar = Calendar.getInstance()
//        calendar.time = dateTime
//        calendar.add(Calendar.MINUTE, minutesToAdd.toInt())
//        return formatter.format(calendar.time)

        val calendar = Calendar.getInstance()
        calendar.time = dateTime
        calendar.add(Calendar.MINUTE, minutesToAdd.toInt())
        return calendar.time
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

    /**
     * Retrieves the current timestamp in the format "yyyy-MM-dd HH:mm:ss".
     *
     * This function gets the current date and time, formats it as a string in the specified format,
     * and sets the timezone to GMT.
     *
     * @return A `String` representing the current timestamp in "yyyy-MM-dd HH:mm:ss" format.
     */
    fun getCurrentTimestamp(): Date {
        val dateFormat = SimpleDateFormat("yyyy-MM-dd HH:mm:ss")
        dateFormat.timeZone = TimeZone.getTimeZone("UTC") // Set timezone to GMT
        return Date()
    }
}

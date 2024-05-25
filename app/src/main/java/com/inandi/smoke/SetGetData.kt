package com.inandi.smoke

import android.content.Context
import android.widget.Toast
import java.text.SimpleDateFormat
import java.util.Date
import java.util.TimeZone
import org.json.JSONObject
import java.io.FileOutputStream
import java.util.Calendar
import java.util.Locale

class SetGetData {

    private val dataSet = DataSet()

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

    fun getNextAwardDetailFromStatusKeyOfJsonObject(jsonObject: JSONObject, detailKey: String, valueKey: String): String? {
        val statusObject = jsonObject.getJSONObject("status")
        val nextAwardDetail = statusObject.optString(detailKey)
        val nextAwardJson = JSONObject(nextAwardDetail)
        return nextAwardJson.optString(valueKey)
    }

    /**
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

     fun addMinutesToDateTime(dateTime: String, minutesToAdd: Long): String {
        val formatter = SimpleDateFormat("yyyy-MM-dd HH:mm:ss")
        val date = formatter.parse(dateTime)
        val calendar = Calendar.getInstance()
        calendar.time = date
        calendar.add(Calendar.MINUTE, minutesToAdd.toInt())
        return formatter.format(calendar.time)
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
     fun formatNumberWithCommas(number: Any): String {
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
    fun getCurrentTimestamp(): String {
        val dateFormat = SimpleDateFormat("yyyy-MM-dd HH:mm:ss")
        dateFormat.timeZone = TimeZone.getTimeZone("GMT") // Set timezone to GMT
        val currentTimeStamp = Date()
        return dateFormat.format(currentTimeStamp)
    }
}

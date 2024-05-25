package com.inandi.smoke

import android.content.Context
import android.widget.Toast
import java.text.SimpleDateFormat
import java.util.Date
import java.util.TimeZone
import org.json.JSONObject
import java.io.FileOutputStream
import java.util.Calendar

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




    fun getStatusValueFromJsonObject(jsonObject: JSONObject, detailKey: String, valueKey: String): String? {
        val statusObject = jsonObject.getJSONObject("status")
        val nextAwardDetail = statusObject.optString(detailKey)
        // @todo fix it
        val nextAwardJson = JSONObject(nextAwardDetail)
        return nextAwardJson.optString(valueKey)
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

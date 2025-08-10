package com.example.shotly.utils

import android.content.Context
import java.io.IOException
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

class LocalLogger(private val context: Context) {

    private val logFileName = "app_logs.txt"

    fun log(message: String) {
        val timestamp = SimpleDateFormat("yyyy-MM-dd HH:mm:ss", Locale.getDefault()).format(Date())
        val logEntry = "$timestamp - $message\n"
        writeToFile(logEntry)
    }

    private fun writeToFile(content: String) {
        try {
            context.openFileOutput(logFileName, Context.MODE_APPEND).use {
                it.write(content.toByteArray())
            }
        } catch (e: IOException) {
            e.printStackTrace()
        }
    }

    fun readLogs(): String {
        return try {
            context.openFileInput(logFileName).bufferedReader().use { it.readText() }
        } catch (e: IOException) {
            ""
        }
    }

    fun clearLogs() {
        context.deleteFile(logFileName)
    }
}

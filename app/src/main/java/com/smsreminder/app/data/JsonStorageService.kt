package com.smsreminder.app.data

import android.content.Context
import android.util.Log
import com.google.gson.Gson
import com.google.gson.GsonBuilder
import com.google.gson.reflect.TypeToken
import java.io.File
import java.io.FileReader
import java.io.FileWriter

class JsonStorageService(private val context: Context) {

    private val gson: Gson = GsonBuilder()
        .setPrettyPrinting()
        .create()

    private val lock = Any()

    companion object {
        private const val TAG = "JsonStorageService"
        const val FILE_REMINDERS = "reminders.json"
        const val FILE_HISTORY = "history_logs.json"
        const val FILE_SETTINGS = "settings.json"
    }

    fun <T> readFromFile(fileName: String, typeToken: TypeToken<T>, defaultValue: T): T {
        synchronized(lock) {
            val file = File(context.filesDir, fileName)
            if (!file.exists()) {
                writeToFile(fileName, defaultValue)
                return defaultValue
            }
            return try {
                FileReader(file).use { reader ->
                    gson.fromJson(reader, typeToken.type) ?: defaultValue
                }
            } catch (e: Exception) {
                Log.e(TAG, "Error reading from $fileName: ${e.message}", e)
                defaultValue
            }
        }
    }

    fun <T> writeToFile(fileName: String, data: T): Boolean {
        synchronized(lock) {
            return try {
                val file = File(context.filesDir, fileName)
                val tempFile = File(context.filesDir, "$fileName.tmp")

                FileWriter(tempFile).use { writer ->
                    gson.toJson(data, writer)
                }

                if (file.exists()) {
                    file.delete()
                }
                tempFile.renameTo(file)
            } catch (e: Exception) {
                Log.e(TAG, "Error writing to $fileName: ${e.message}", e)
                false
            }
        }
    }
}

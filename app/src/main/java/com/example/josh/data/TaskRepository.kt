package com.example.josh.data

import android.content.Context
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import kotlinx.serialization.decodeFromString
import kotlinx.serialization.encodeToString
import kotlinx.serialization.json.Json
import java.io.File
import java.time.Instant

class TaskRepository(private val context: Context) {

    private val fileName = "tasks.json"

    private fun file(): File = File(context.filesDir, fileName)

    // Updated JSON config
    private val json = Json {
        prettyPrint = true
        ignoreUnknownKeys = true   // prevents crashes if model expands
        isLenient = true           // accepts flexible JSON
    }

    suspend fun loadTasks(): List<Task> = withContext(Dispatchers.IO) {
        val f = file()

        // If file doesn't exist, return empty list
        if (!f.exists()) return@withContext emptyList()

        return@withContext try {
            val text = f.readText()
            json.decodeFromString<List<Task>>(text)
        } catch (e: Exception) {
            println("⚠ Failed to parse tasks.json → ${e.message}")
            emptyList()
        }
    }

    suspend fun saveTask(task: Task) = withContext(Dispatchers.IO) {
        val existing = loadTasks().toMutableList()

        // Insert most recent first
        existing.add(0, task)

        val f = file()
        f.writeText(json.encodeToString(existing))
    }

    fun nowIso(): String = Instant.now().toString()
}

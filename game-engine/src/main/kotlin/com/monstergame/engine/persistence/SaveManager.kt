package com.monstergame.engine.persistence

import com.monstergame.engine.model.SaveData
import kotlinx.serialization.encodeToString
import kotlinx.serialization.json.Json
import java.io.File

/**
 * Handles save/load operations.
 * The Android layer provides the path; this class is pure Kotlin.
 */
class SaveManager(private val saveDir: File) {

    private val json = Json {
        prettyPrint = false
        ignoreUnknownKeys = true
        encodeDefaults = true
    }

    companion object {
        const val SAVE_FILE_NAME = "save_slot_%d.json"
        const val MAX_SLOTS = 3
    }

    fun save(data: SaveData, slot: Int = 0): Result<Unit> = runCatching {
        require(slot in 0 until MAX_SLOTS) { "Invalid slot: $slot" }
        saveDir.mkdirs()
        val file = File(saveDir, SAVE_FILE_NAME.format(slot))
        file.writeText(json.encodeToString(data))
    }

    fun load(slot: Int = 0): Result<SaveData> = runCatching {
        require(slot in 0 until MAX_SLOTS) { "Invalid slot: $slot" }
        val file = File(saveDir, SAVE_FILE_NAME.format(slot))
        if (!file.exists()) error("No save in slot $slot")
        val text = file.readText()
        val data = json.decodeFromString<SaveData>(text)
        // Version migration
        if (data.version != SaveData.SAVE_VERSION) {
            migrate(data)
        } else {
            data
        }
    }

    fun delete(slot: Int): Result<Unit> = runCatching {
        require(slot in 0 until MAX_SLOTS) { "Invalid slot: $slot" }
        File(saveDir, SAVE_FILE_NAME.format(slot)).delete()
    }

    fun listSlots(): List<SaveSlotInfo> {
        return (0 until MAX_SLOTS).map { slot ->
            val file = File(saveDir, SAVE_FILE_NAME.format(slot))
            if (file.exists()) {
                runCatching {
                    val data = json.decodeFromString<SaveData>(file.readText())
                    SaveSlotInfo(
                        slot = slot,
                        exists = true,
                        playerName = data.playerName,
                        playtimeSeconds = data.playtimeSeconds,
                        badges = data.badges.size,
                        activeRegion = data.activeRegion,
                        lastModified = file.lastModified()
                    )
                }.getOrElse { SaveSlotInfo(slot = slot, exists = true, corrupt = true) }
            } else {
                SaveSlotInfo(slot = slot, exists = false)
            }
        }
    }

    fun hasSave(slot: Int): Boolean =
        File(saveDir, SAVE_FILE_NAME.format(slot)).exists()

    private fun migrate(data: SaveData): SaveData {
        // Future migration logic here; for v1 just return as-is
        return data
    }
}

data class SaveSlotInfo(
    val slot: Int,
    val exists: Boolean,
    val playerName: String = "",
    val playtimeSeconds: Long = 0L,
    val badges: Int = 0,
    val activeRegion: String = "KANTO",
    val lastModified: Long = 0L,
    val corrupt: Boolean = false
)

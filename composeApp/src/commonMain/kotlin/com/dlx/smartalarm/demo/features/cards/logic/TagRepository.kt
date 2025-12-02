package com.dlx.smartalarm.demo.features.cards.logic

import com.dlx.smartalarm.demo.core.model.Tag
import com.dlx.smartalarm.demo.core.model.TagColor
import com.dlx.smartalarm.demo.readFile
import com.dlx.smartalarm.demo.writeFile
import kotlinx.serialization.Serializable
import kotlinx.serialization.json.Json
import kotlinx.serialization.encodeToString
import kotlinx.serialization.decodeFromString

object TagRepository {
    private val json = Json { prettyPrint = true; ignoreUnknownKeys = true }
    private const val FILE_NAME = "tags.json"
    private const val FAVORITE_ID = "__favorite__"

    suspend fun load(): List<Tag> {
        val content = readFile(FILE_NAME)
        val loaded = if (content != null) json.decodeFromString<List<Tag>>(content) else emptyList()
        return ensureFavorite(loaded)
    }

    suspend fun save(all: List<Tag>) {
        val content = json.encodeToString(all)
        writeFile(FILE_NAME, content)
    }

    fun ensureFavorite(all: List<Tag>): List<Tag> {
        return if (all.any { it.id == FAVORITE_ID }) all else all + Tag(FAVORITE_ID, name = "Favorite", color = TagColor.Yellow)
    }

    fun favoriteId(): String = FAVORITE_ID

    fun create(name: String, color: TagColor = TagColor.Default): Tag {
        val base = name.trim()
        val id = base.lowercase().replace("\\s+".toRegex(), "-") + "-" + kotlin.random.Random.nextInt(0, Int.MAX_VALUE)
        return Tag(id = id, name = base, color = color)
    }
}

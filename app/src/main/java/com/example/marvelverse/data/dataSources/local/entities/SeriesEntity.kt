package com.example.marvelverse.data.dataSources.local.entities

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity("SERIES_TABLE")
data class SeriesEntity(
    @PrimaryKey(autoGenerate = true) val id: Int?,
    val title: String?,
    val description: String?,
    val resourceURI: String?,
    val comics: String?,
    val characters: String?,
    val creators: String?,
    val stories: String?,
    val events: String?,
    val thumbnail: String?,
)
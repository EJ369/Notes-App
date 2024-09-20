package com.example.mynotes.utils

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "NotesTable")
data class NotesEntity(
    @PrimaryKey(autoGenerate = true) val id: Int = 0,
    val title: String,
    val message: String,
    val date: String,
    val category: String,
    val pin: Boolean,
    val color: String
)

package com.example.mynotes.utils

import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase

@Database(entities = [NotesEntity::class], version = 1, exportSchema = false)
abstract class NotesDatabase : RoomDatabase() {
    abstract fun notesDao(): NotesDao

    companion object {
        private var instance: NotesDatabase? = null

        @Synchronized
        fun getInstance(context: Context): NotesDatabase {
            return instance ?: synchronized(this) {
                instance ?: buildDatabase(context).also { instance = it }
            }
        }

        private fun buildDatabase(context: Context): NotesDatabase {
            return Room.databaseBuilder(context.applicationContext, NotesDatabase::class.java, "Notes Database").build()
        }
    }
}
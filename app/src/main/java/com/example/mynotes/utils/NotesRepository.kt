package com.example.mynotes.utils

import androidx.lifecycle.LiveData
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext

class NotesRepository(private val notesDao: NotesDao) {
    val allNotes: LiveData<List<NotesEntity>> = notesDao.getAllNotes()

    fun insertNote(note: NotesEntity): Long {
        return notesDao.insertNote(note)
    }

    fun updateNote(note: NotesEntity): Int {
        return notesDao.updateNote(note)
    }

    suspend fun updatePin(id: Int, pin: Boolean) {
        withContext(Dispatchers.IO) {
            notesDao.updatePin(id, pin)
        }
    }

    suspend fun deleteNote(note: NotesEntity) {
        withContext(Dispatchers.IO) {
            notesDao.deleteNote(note)
        }
    }

    fun searchNotes(searchQuery: String): List<NotesEntity> {
        return notesDao.searchNotes(searchQuery)
    }

    fun notesByCategory(category: String): List<NotesEntity> {
        return if (category == "All") {
            notesDao.getNotes()
        } else {
            notesDao.notesByCategory(category)
        }
    }


}
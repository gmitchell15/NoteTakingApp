package com.example.assignment4.database

import androidx.room.*

@Dao
interface NoteDao {

    @Insert
    fun addNote(note : Note) : Long

    @Update
    fun updateNote(note : Note)

    @Delete
    fun deleteNote(note: Note)

    @Query("SELECT * FROM Note")
    fun getAllNotes() : List<Note>

    @Query("SELECT * FROM Note ORDER BY title")
    fun sortAllNotes() : List<Note>

    @Query("SELECT * FROM Note ORDER BY lastModified")
    fun sortAllNotesByDate() : List<Note>

    @Query("SELECT * FROM note WHERE id = :noteId")
    fun getNote(noteId : Long) : Note

}
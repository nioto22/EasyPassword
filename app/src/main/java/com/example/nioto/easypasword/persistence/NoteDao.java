package com.example.nioto.easypasword.persistence;

import android.arch.lifecycle.LiveData;
import android.arch.persistence.room.Dao;
import android.arch.persistence.room.Delete;
import android.arch.persistence.room.Insert;
import android.arch.persistence.room.Query;
import android.arch.persistence.room.Update;

import com.example.nioto.easypasword.model.Note;

import java.util.List;

/**
 * Created by Nioto on 12/11/2018.
 */

@Dao
public interface NoteDao {
    // Go further : https://developer.android.com/training/data-storage/room/accessing-data

    @Insert
    void insert(Note note);

    @Update
    void update(Note note);

    @Delete
    void delete(Note note);

    @Query("DELETE FROM note_table")
    void deleteAllNotes();

    @Query("SELECT * FROM note_table ORDER BY priority, title")
        // LiveData : To update automatically
    LiveData<List<Note>> getAllNotes();

    @Query("SELECT * FROM note_table ORDER BY priority, title")
    List<Note> getAllInstantNotes();

    // --------------------
    // SEARCH METHOD
    // --------------------
    @Query("SELECT * FROM note_table WHERE title LIKE :searchquery")
    LiveData<List<Note>> searchFor(String searchquery);
}

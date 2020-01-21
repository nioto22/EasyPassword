package com.example.nioto.easypasword.persistence;

import android.arch.persistence.db.SupportSQLiteDatabase;
import android.arch.persistence.room.Database;
import android.arch.persistence.room.Room;
import android.arch.persistence.room.RoomDatabase;
import android.content.Context;
import android.os.AsyncTask;
import android.support.annotation.NonNull;

import com.example.nioto.easypasword.model.Note;

/**
 * Created by Nioto on 12/11/2018.
 */

@Database(entities = {Note.class}, version = 2, exportSchema = false)
public abstract class NoteDatabase extends RoomDatabase {
    // Go further : https://developer.android.com/training/data-storage/room/


    // Instance because we need to create as Singleton (= one unique instance of it)
    private static NoteDatabase instance;

    public abstract NoteDao noteDao();

    // The singleton :
    // synchronized mean one at a time
    public static synchronized NoteDatabase getInstance(Context context){
        if (instance == null){
            // Creation of a Room database
            instance = Room.databaseBuilder(context.getApplicationContext(),
                    NoteDatabase.class, "note_database")
                    .fallbackToDestructiveMigration()
                    .addCallback(roomCallback)  // = for first instance
                    .build();
        }
        return instance;
    }

    // For first instance
    private static Callback roomCallback = new Callback(){
        @Override
        public void onCreate(@NonNull SupportSQLiteDatabase db) {
            super.onCreate(db);
            new PopulateDbAsyncTask(instance).execute();
        }
    };

    private static class PopulateDbAsyncTask extends AsyncTask<Void, Void, Void>{
        private NoteDao noteDao;

        private PopulateDbAsyncTask(NoteDatabase db){
            noteDao = db.noteDao();
        }

        @Override
        protected Void doInBackground(Void... voids) {
            noteDao.insert(new Note("Title 1", "Login 1","Password 1",null, 1));
            noteDao.insert(new Note("Title 2", "Login 2","Password 2",null, 2));
            noteDao.insert(new Note("Title 3", "Login 3","Password 3","Comment 3", 3));
            noteDao.insert(new Note("Title 4", "Login 4","Password 4","Comment 4", 1));
            return null;
        }
    }
}

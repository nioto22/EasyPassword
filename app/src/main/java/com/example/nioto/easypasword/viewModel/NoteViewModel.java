package com.example.nioto.easypasword.viewModel;

import android.app.Application;
import android.arch.lifecycle.AndroidViewModel;
import android.arch.lifecycle.LiveData;
import android.support.annotation.NonNull;

import com.example.nioto.easypasword.model.Note;
import com.example.nioto.easypasword.persistence.NoteRepository;

import java.util.List;

/**
 * Created by Nioto on 12/11/2018.
 */

// We use ViewModel then an Activity.observe to permit no saveInstance data
// And no memory leak

public class NoteViewModel extends AndroidViewModel{
    private NoteRepository repository;
    private LiveData<List<Note>> allNotes;
    private List<Note> allInstantNotes;

    // WARNING Never store context or reference of an Activity in the ViewModel cause of memoryLeak
    // so :
    public NoteViewModel(@NonNull Application application) {
         super(application);
        repository = new NoteRepository(application);
        allNotes = repository.getAllNotes();
    }

    public void insert(Note note){
        repository.insert(note);
    }

    public void update(Note note){
        repository.update(note);
    }

    public void delete(Note note){
        repository.delete(note);
    }

    public void deleteAllNotes(){
        repository.deleteAllNotes();
    }

    public LiveData<List<Note>> getAllNotes() {
        return allNotes;
    }

    public List<Note> getInstantAllNotes() {return allInstantNotes;}

    // ----------------
    // SEARCH METHODS
    // ----------------
    public LiveData<List<Note>> searchQuery(String query) {
        return repository.searchFor(query);
    }

}


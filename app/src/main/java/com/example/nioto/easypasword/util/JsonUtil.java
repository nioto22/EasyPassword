package com.example.nioto.easypasword.util;

import android.app.Application;

import com.example.nioto.easypasword.model.Note;
import com.example.nioto.easypasword.persistence.NoteRepository;
import com.google.gson.Gson;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.List;

public class JsonUtil {

    private NoteRepository mNoteRepository;
    private List<Note> mNoteList;

    public static String toJson(Note note){
        JSONObject jsonObject = new JSONObject();
        try {
            jsonObject.put("id", note.getId());
            jsonObject.put("title", note.getTitle());
            jsonObject.put("login", note.getLogin());
            jsonObject.put("password", note.getPassword());
            jsonObject.put("comment", note.getComment());
            jsonObject.put("priority", note.getPriority());


            return jsonObject.toString();
        } catch (JSONException e) {
            e.printStackTrace();
        }

        return null;
    }

    public String exportAllNotes(Application application){
        mNoteRepository = new NoteRepository(application);
        mNoteList = mNoteRepository.getAllInstantNotes();

        JSONObject noteObject = new JSONObject();
        JSONArray jsonArrayOfNotes = new JSONArray();

        String str = new Gson().toJson(mNoteList);
        try {
            JSONObject jsonObject = new JSONObject(str);
            return jsonObject.toString();
        } catch (JSONException e) {
            e.printStackTrace();
        }
        return null;
    }


}


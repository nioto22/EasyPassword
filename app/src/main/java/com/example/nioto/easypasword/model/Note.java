package com.example.nioto.easypasword.model;

import android.arch.persistence.room.Entity;
import android.arch.persistence.room.PrimaryKey;

/**
 * Created by Nioto on 12/11/2018.
 */


@Entity(tableName = "note_table")
public class Note {

    // Will correspond to our SqLite columns
    @PrimaryKey(autoGenerate = true)
    private int id;
    private String title;
    private String login;
    private String password;
    private String comment;
    private int priority;

    // To ignore a var as column : @Ignore
    // To change the name of a column (default = var name) put up to a var : @ColumnInfo(name = "priority_column")
    // To add columns of all vars of an object : @Embedded public Object object
    // More information here : https://developer.android.com/training/data-storage/room/defining-data#java


    public Note(String title, String login, String password, String comment, int priority) {
        this.title = title;
        this.login = login;
        this.password = password;
        this.comment = comment;
        this.priority = priority;
    }

    public void setId(int id) {
        this.id = id;
    }

    public void setComment(String comment) { this.comment = comment; }

    public int getId() {return id;}

    public String getTitle() {return title;}

    public String getLogin() {return login;}

    public String getPassword() { return password;}

    public int getPriority() { return priority;}

    public String getComment() { return comment;}
}

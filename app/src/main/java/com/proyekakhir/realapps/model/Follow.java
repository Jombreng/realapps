package com.proyekakhir.realapps.model;

import com.proyekakhir.realapps.R;

import java.util.Calendar;

public class Follow {
    private String id;
    private String authorId;
    private long createdDate;


    public Follow() {
        // Default constructor required for calls to DataSnapshot.getValue(Comment.class)
    }

    public Follow(String authorId) {
        this.id = authorId;
        this.createdDate = Calendar.getInstance().getTimeInMillis();
    }

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public long getCreatedDate() {
        return createdDate;
    }
}

package com.proyekakhir.realapps.model;

import java.util.Calendar;

public class Notifications {
    private String postId;
    private String notificationsId;
    private String text;
    private String authorId;
    private String type;

    public void setType(String type) {
        this.type = type;
    }

    public String getType() {

        return type;
    }

    public Notifications(String notificationsId) {
        this.notificationsId = notificationsId;
        this.createdDate = Calendar.getInstance().getTimeInMillis();
    }

    private long createdDate;

    public Notifications(){

    }

    public void setPostId(String postId) {
        this.postId = postId;
    }

    public void setNotificationsId(String notificationsId) {
        this.notificationsId = notificationsId;
    }

    public void setText(String text) {
        this.text = text;
    }

    public void setAuthorId(String authorId) {
        this.authorId = authorId;
    }

    public void setCreatedDate(long createdDate) {
        this.createdDate = createdDate;
    }

    public String getPostId() {

        return postId;
    }

    public String getNotificationsId() {
        return notificationsId;
    }

    public String getText() {
        return text;
    }

    public String getAuthorId() {
        return authorId;
    }

    public long getCreatedDate() {
        return createdDate;
    }
}

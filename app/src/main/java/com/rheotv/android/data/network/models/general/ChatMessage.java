package com.rheotv.android.data.network.models.general;

import com.google.gson.annotations.Expose;
import com.rheotv.android.data.network.models.postlisting.responses.Author;

public class ChatMessage {
    @Expose
    Author author;
    @Expose
    String message;
    @Expose
    long timestamp;

    public Author getAuthor() {
        return author;
    }

    public void setAuthor(Author author) {
        this.author = author;
    }

    public String getMessage() {
        return message;
    }

    public void setMessage(String message) {
        this.message = message;
    }

    public long getTimestamp() {
        return timestamp;
    }

    public void setTimestamp(long timestamp) {
        this.timestamp = timestamp;
    }
}

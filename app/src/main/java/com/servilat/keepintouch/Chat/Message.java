package com.servilat.keepintouch.Chat;

public class Message {
    String message;
    String time;
    User sender;

    public Message(String message, String time, User sender) {
        this.message = message;
        this.time = time;
        this.sender = sender;
    }

    public String getMessage() {
        return message;
    }

    public String getTime() {
        return time;
    }

    public User getSender() {
        return sender;
    }
}

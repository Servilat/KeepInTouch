package com.servilat.keepintouch.Dialog;

import org.json.JSONException;
import org.json.JSONObject;

import static com.servilat.keepintouch.Util.convertTime;

public class DialogsItem {
    private String dialogName;
    private String userMessage;
    private String messageTime;
    private String imageURL;
    private String userID;

    public DialogsItem(String dialogName, String userMessage, String messageTime, String imageURL, String userID) {
        this.dialogName = dialogName;
        this.userMessage = userMessage;
        this.messageTime = messageTime;
        this.imageURL = imageURL;
        this.userID = userID;
    }

    public DialogsItem(JSONObject message) {
        parseMessage(message);
    }


    public String getDialogName() {
        return dialogName;
    }

    public String getUserMessage() {
        return userMessage;
    }

    public String getMessageTime() {
        return messageTime;
    }

    public String getUserID() {
        return userID;
    }

    public String getImageURL() {
        return imageURL;
    }

    public void setUserMessage(String userMessage) {
        this.userMessage = userMessage;
    }

    public void setMessageTime(String messageTime) {
        this.messageTime = messageTime;
    }

    void parseMessage(JSONObject message) {
        try {
            this.userMessage = message.getString("body");
            this.messageTime = convertTime(message.getInt("date"));

            if (message.has("photo_100")) {
                this.imageURL = message.getString("photo_100");
            }

            if (message.has("chat_id")) {
                this.userID = String.valueOf(2000000000 + message.getInt("chat_id"));
                this.dialogName = message.getString("title");
            } else {
                this.userID = message.getString("user_id");
                this.dialogName = message.getString("first_name") + " " + message.getString("last_name");
            }

        } catch (JSONException e) {
            e.printStackTrace();
        }
    }
}

package com.servilat.keepintouch.Dialog;

import com.vk.sdk.api.model.VKApiDialog;

import org.json.JSONException;
import org.json.JSONObject;

import java.text.SimpleDateFormat;
import java.util.Date;

import static com.servilat.keepintouch.Util.convertTime;

public class DialogsItem {
    private String dialogName;
    private String userMessage;
    private String messageTime;
    private String imageURL;
    private String userID;
    private boolean readState;

    public DialogsItem(String dialogName, String imageURL, String userMessage, int messageTime, String user_id, boolean readState) {
        this.dialogName = dialogName;
        this.userMessage = userMessage;
        this.messageTime = convertTime(messageTime);
        this.imageURL = imageURL;
        this.userID = user_id;
        this.readState = readState;
    }

    public DialogsItem(VKApiDialog dialog) {
        this.dialogName = dialog.message.title;
        this.userMessage = dialog.message.body;
        this.messageTime = convertTime(dialog.message.date);
        this.userID = String.valueOf(dialog.message.user_id);
        this.readState = dialog.message.read_state;
    }

    public DialogsItem(JSONObject message) {
        parseMessage(message);
    }


    public String getDialogName() {
        return dialogName;
    }

    public void setDialogName(String dialogName) {
        this.dialogName = dialogName;
    }

    public String getUserMessage() {
        return userMessage;
    }

    public void setUserMessage(String userMessage) {
        this.userMessage = userMessage;
    }

    public String getMessageTime() {
        return messageTime;
    }

    public void setMessageTime(String messageTime) {
        this.messageTime = messageTime;
    }

    public String getUserID() {
        return userID;
    }

    public String getImageURL() {
        return imageURL;
    }

    public boolean getReadState() {
        return readState;
    }



    void parseMessage(JSONObject message) {
        try {
            readState = message.getInt("read_state") != 0;

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

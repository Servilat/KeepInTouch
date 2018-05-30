package com.servilat.keepintouch.Chat;

import java.io.Serializable;

public class User implements Serializable {
    String imageURL;
    String dialogName;
    String userID;

    public User(String imageURL, String dialogName, String userID) {
        this.imageURL = imageURL;
        this.dialogName = dialogName;
        this.userID = userID;
    }

    public String getDialogName() {
        return dialogName;
    }

    public String getImageURL() {
        return imageURL;
    }

    public String getUserID() {
        return userID;
    }
}

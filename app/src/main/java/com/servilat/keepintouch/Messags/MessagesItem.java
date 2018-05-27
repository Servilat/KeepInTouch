package com.servilat.keepintouch.Messags;

public class MessagesItem {
    private String member_name_list;
    private String user_message_item;
    private String time;
    private String imageURL;
    private int user_id;

    public MessagesItem(String member_name_list, String imageURL, String user_message_item, String time, int user_id) {
        this.member_name_list = member_name_list;
        this.user_message_item = user_message_item;
        this.time = time;
        this.imageURL = imageURL;
        this.user_id = user_id;
    }


    public String getMember_name_list() {
        return member_name_list;
    }

    public void setMember_name_list(String member_name_list) {
        this.member_name_list = member_name_list;
    }

    public String getUser_message_item() {
        return user_message_item;
    }

    public void setUser_message_item(String user_message_item) {
        this.user_message_item = user_message_item;
    }

    public String getTime() {
        return time;
    }

    public void setTime(String time) {
        this.time = time;
    }

    public int getUser_id() {
        return user_id;
    }

    public void setUser_id(int user_id) {
        this.user_id = user_id;
    }

    public String getImageURL() {
        return imageURL;
    }
}

package com.servilat.keepintouch.Messags;

import android.app.Activity;
import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.ImageView;
import android.widget.TextView;

import com.servilat.keepintouch.R;
import com.squareup.picasso.Picasso;

import java.util.ArrayList;
import java.util.List;

public class MessagesListFragmentAdapter extends ArrayAdapter<MessagesItem> {
    Context context;
    List<MessagesItem> messagesItems;

    public MessagesListFragmentAdapter(ArrayList<MessagesItem> messagesItems, Context context) {
        super(context, R.layout.message_item_layout, messagesItems);
        this.context = context;
        this.messagesItems = messagesItems;
    }

    private class ViewHolder {
        TextView dialogNameTextView;
        TextView messageTextView;
        TextView messageTimeTextView;
        ImageView dialogPhotoImageView;
    }

    @Override
    public int getCount() {
        return messagesItems.size();
    }

    @Override
    public MessagesItem getItem(int position) {
        return messagesItems.get(position);
    }

    @Override
    public long getItemId(int position) {
        return messagesItems.indexOf(getItem(position));
    }

    @Override
    public View getView(int position, View convertView, ViewGroup parent) {
        ViewHolder holder = null;

        MessagesItem messageItem = messagesItems.get(position);

        if (convertView == null) {
            LayoutInflater mInflater = (LayoutInflater) context.getSystemService(Activity.LAYOUT_INFLATER_SERVICE);
            convertView = mInflater.inflate(R.layout.message_item_layout, parent, false);

            holder = new ViewHolder();

            holder.dialogNameTextView = convertView.findViewById(R.id.preview_messages_name);
            holder.messageTextView = convertView.findViewById(R.id.preview_user_message);
            holder.messageTimeTextView = convertView.findViewById(R.id.preview_message_time);
            holder.dialogPhotoImageView = convertView.findViewById(R.id.preview_messages_photo);
            convertView.setTag(holder);
        } else {
            holder = (ViewHolder) convertView.getTag();
        }

        holder.messageTimeTextView.setText(messageItem.getMessageTime());
        holder.messageTextView.setText(messageItem.getUserMessage());
        holder.dialogNameTextView.setText(messageItem.getDialogName());
        if (!messageItem.getReadState()) {
            holder.messageTextView.setBackgroundColor(getContext().getResources().getColor(R.color.colorGrey));
        }
        Picasso.with(getContext())
                .load(messageItem.getImageURL())
                .placeholder(R.drawable.placeholder_person)
                .error(R.drawable.placeholder_person)
                .into(holder.dialogPhotoImageView);

        return convertView;
    }
}

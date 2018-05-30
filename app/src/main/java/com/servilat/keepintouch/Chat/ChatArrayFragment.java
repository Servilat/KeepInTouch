package com.servilat.keepintouch.Chat;

import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.EditText;

import com.servilat.keepintouch.R;
import com.servilat.keepintouch.Util;
import com.vk.sdk.api.VKApi;
import com.vk.sdk.api.VKApiConst;
import com.vk.sdk.api.VKParameters;
import com.vk.sdk.api.VKRequest;
import com.vk.sdk.api.VKResponse;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.time.Instant;
import java.util.ArrayList;
import java.util.Date;

public class ChatArrayFragment extends Fragment {
    public static final String CURRENT_USER_ID = "CURRENT_USER_ID";
    public static final String CURRENT_USER_ID_DIALOG_WITH = "CURRENT_USER_ID_DIALOG_WITH";

    RecyclerView messagesRecyclerView;
    ArrayList<Message> dialogMessages;
    MessageListAdapter messageListAdapter;
    LinearLayoutManager linearLayoutManager;
    User currentUserID;
    User currentUserIdDialogWith;
    int countOfMessages;
    int messagesOffset = 0;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_message_list, container, false);

        dialogMessages = new ArrayList<>();

        currentUserID = (User) getArguments().getSerializable(CURRENT_USER_ID);
        currentUserIdDialogWith = (User) getArguments().getSerializable(CURRENT_USER_ID_DIALOG_WITH);

        messageListAdapter = new MessageListAdapter(getContext(), dialogMessages, currentUserID);

        getMessageHistoryVK();

        messagesRecyclerView = (RecyclerView) view.findViewById(R.id.recyclerview_message_list);
        messagesRecyclerView.setAdapter(messageListAdapter);

        linearLayoutManager = new LinearLayoutManager(getContext());
        linearLayoutManager.setStackFromEnd(true);

        messagesRecyclerView.setLayoutManager(linearLayoutManager);

        return view;
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        getActivity().findViewById(R.id.button_chatbox_send).setOnClickListener(new View.OnClickListener() {
            EditText editText = getActivity().findViewById(R.id.edittext_chatbox);

            @Override
            public void onClick(View v) {
                String message = editText.getText().toString();
                if (!message.matches("[\\s]*")) {
                    VKRequest vkRequest = new VKRequest("messages.send", VKParameters.from(
                            VKApiConst.USER_ID, currentUserIdDialogWith.getUserID(),
                            VKApiConst.MESSAGE, message
                    ));
                    vkRequest.executeWithListener(new VKRequest.VKRequestListener() {
                        @Override
                        public void onComplete(VKResponse response) {
                            super.onComplete(response);
                            dialogMessages.add(new Message(message, Util.convertTime(System.currentTimeMillis() / 1000L), currentUserID));
                            messageListAdapter.notifyItemChanged(dialogMessages.size() - 1);
                            linearLayoutManager.scrollToPosition(dialogMessages.size() - 1);
                        }
                    });
                    editText.setText("");
                }
            }
        });
        messagesRecyclerView.addOnScrollListener(new RecyclerView.OnScrollListener() {
            private int top;

            @Override
            public void onScrolled(RecyclerView recyclerView, int dx, int dy) {
                super.onScrolled(recyclerView, dx, dy);
                int pastVisibleItems = linearLayoutManager.findFirstCompletelyVisibleItemPosition();

                if (linearLayoutManager.findFirstCompletelyVisibleItemPosition() == 0) {
                    if (messagesOffset < countOfMessages) {
                        messagesOffset += 20;
                        getMessageHistoryVK();
                    }
                }
            }
        });

    }

    void getMessageHistoryVK() {
        VKRequest vkRequest = new VKRequest("messages.getHistory",
                VKParameters.from(
                        VKApiConst.OFFSET, messagesOffset,
                        VKApiConst.COUNT, 20,
                        VKApiConst.USER_ID, currentUserIdDialogWith.getUserID()));
        vkRequest.executeWithListener(new VKRequest.VKRequestListener() {
            @Override
            public void onComplete(VKResponse response) {
                super.onComplete(response);
                try {
                    JSONObject responseVK = response.json.getJSONObject("response");
                    countOfMessages = responseVK.getInt("count");
                    JSONArray messages = responseVK.getJSONArray("items");
                    dialogMessages.addAll(0, parseVKMessages(messages));
                    messagesRecyclerView.getAdapter().notifyDataSetChanged();
                    linearLayoutManager.scrollToPositionWithOffset(20, 0);
/*
                    messagesRecyclerView.scroll ToPosition(20);
*/
                } catch (JSONException e) {
                    e.printStackTrace();
                }
            }
        });
    }

    private ArrayList<Message> parseVKMessages(JSONArray messagesJSONArray) {
        ArrayList<Message> messages = new ArrayList<>();
        try {
            for (int i = 0; i < messagesJSONArray.length(); i++) {
                JSONObject message = messagesJSONArray.getJSONObject(i);
                User userFrom;
                if (message.getString("from_id").equals(currentUserID.getUserID())) {
                    userFrom = currentUserID;
                } else {
                    userFrom = currentUserIdDialogWith;
                }

                messages.add(0, new Message(
                        message.getString("body"),
                        Util.convertTime(message.getLong("date")),
                        userFrom
                ));
            }
        } catch (JSONException e) {
            e.printStackTrace();
        }
        return messages;
    }

}

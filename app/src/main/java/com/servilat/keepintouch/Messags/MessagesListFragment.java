package com.servilat.keepintouch.Messags;

import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v4.app.ListFragment;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AbsListView;
import android.widget.ListView;

import com.servilat.keepintouch.MainActivity;
import com.servilat.keepintouch.R;
import com.servilat.keepintouch.SocialNetworks;
import com.servilat.keepintouch.Util;
import com.vk.sdk.api.VKParameters;
import com.vk.sdk.api.VKRequest;
import com.vk.sdk.api.VKResponse;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;

import static com.servilat.keepintouch.MainActivity.SOCIAL_NETWORK;

public class MessagesListFragment extends ListFragment {
    ArrayList<MessagesItem> messagesItems;
    MessagesListFragmentAdapter adapter;
    SocialNetworks currentSocialNetwork;
    ListView listView;
    private int dialogsCount;
    private int dialogOffset = 0;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        setHasOptionsMenu(true);
        messagesItems = new ArrayList<>();

        adapter = new MessagesListFragmentAdapter(messagesItems, getContext());
        Bundle arguments = getArguments();
        getVKDialogs();
        if (arguments != null) {
            switch ((SocialNetworks) arguments.getSerializable(SOCIAL_NETWORK)) {
                case VK:
                    currentSocialNetwork = SocialNetworks.VK;
                    break;
                case TELEGRAM:
                    currentSocialNetwork = SocialNetworks.TELEGRAM;
                    break;
                case FACEBOOK:
                    currentSocialNetwork = SocialNetworks.FACEBOOK;
                    break;
            }
        }

        setListAdapter(adapter);
        return inflater.inflate(R.layout.messages_layout, container, false);
    }

    @Override
    public void onCreateOptionsMenu(Menu menu, MenuInflater inflater) {
        inflater.inflate(R.menu.main, menu);
        super.onCreateOptionsMenu(menu, inflater);
    }

    private ArrayList<MessagesItem> getVKDialogs() {
        final ArrayList<MessagesItem> messagesItems = new ArrayList<>();
        VKRequest request = new VKRequest("execute", VKParameters.from("code", String.format(Util.executeCode, dialogOffset)));
        request.executeWithListener(new VKRequest.VKRequestListener() {
            @Override
            public void onComplete(VKResponse response) {
                super.onComplete(response);
                adapter.addAll(parseMessages(parseVKResponse(response.json)));
                adapter.notifyDataSetChanged();
            }
        });
        return messagesItems;
    }

    JSONArray parseVKResponse(JSONObject vkResponse) {
        JSONArray items = null;
        try {
            items = vkResponse.getJSONObject("response").getJSONArray("dialogs");
            dialogsCount = vkResponse.getJSONObject("response").getInt("count");
        } catch (JSONException e) {
            e.printStackTrace();
        }
        return items;
    }

    @Override
    public void onActivityCreated(@Nullable Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);
        listView = getListView();
        listView.setOnScrollListener(new AbsListView.OnScrollListener() {
            @Override
            public void onScrollStateChanged(AbsListView view, int scrollState) {

            }

            @Override
            public void onScroll(AbsListView view, int firstVisibleItem, int visibleItemCount, int totalItemCount) {
                if (listView.getChildAt(0) != null) {
                    if (listView.getFirstVisiblePosition() == 0 &&
                            listView.getChildAt(0).getTop() >= 0) {
                        if (dialogOffset + 15 < dialogsCount) {
                            dialogOffset += 15;
                            getVKDialogs();
                        }

                    }
                }
            }
        });
    }

    ArrayList<MessagesItem> parseMessages(JSONArray messages) {
        ArrayList<MessagesItem> messagesItems = new ArrayList<>();

        for (int i = 0; i < messages.length(); i++) {
            try {
                MessagesItem messagesItem = new MessagesItem(messages.getJSONObject(i));
                messagesItems.add(messagesItem);
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
        return messagesItems;
    }

    public SocialNetworks getCurrentSocialNetwork() {
        return currentSocialNetwork;
    }
}

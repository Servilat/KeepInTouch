package com.servilat.keepintouch.Dialog;

import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v4.app.FragmentTransaction;
import android.support.v4.app.ListFragment;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AbsListView;
import android.widget.AdapterView;
import android.widget.ListView;

import com.servilat.keepintouch.Chat.ChatArrayFragment;
import com.servilat.keepintouch.Chat.User;
import com.servilat.keepintouch.R;
import com.servilat.keepintouch.SocialNetworks;
import com.servilat.keepintouch.Util;
import com.vk.sdk.VKAccessToken;
import com.vk.sdk.api.VKParameters;
import com.vk.sdk.api.VKRequest;
import com.vk.sdk.api.VKResponse;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;

import static com.servilat.keepintouch.MainActivity.SOCIAL_NETWORK;

public class DialogsListFragment extends ListFragment {
    ArrayList<DialogsItem> dialogsItems;
    DialogsListFragmentAdapter adapter;
    SocialNetworks currentSocialNetwork;
    ListView listView;
    private int dialogsCount;
    private int dialogOffset = 0;

    /*@Override
    public void onListItemClick(ListView l, View v, int position, long id) {
        super.onListItemClick(l, v, position, id);

    }*/

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        setHasOptionsMenu(true);
        dialogsItems = new ArrayList<>();

        adapter = new DialogsListFragmentAdapter(dialogsItems, getContext());
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

    private ArrayList<DialogsItem> getVKDialogs() {
        final ArrayList<DialogsItem> dialogsItems = new ArrayList<>();
        VKRequest request = new VKRequest("execute", VKParameters.from("code", String.format(Util.executeCode, dialogOffset)));
        request.executeWithListener(new VKRequest.VKRequestListener() {
            @Override
            public void onComplete(VKResponse response) {
                super.onComplete(response);
                adapter.addAll(parseMessages(parseVKResponse(response.json)));
                adapter.notifyDataSetChanged();
            }
        });
        return dialogsItems;
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
            private int preLast;

            @Override
            public void onScrollStateChanged(AbsListView view, int scrollState) {

            }

            @Override
            public void onScroll(AbsListView view, int firstVisibleItem, int visibleItemCount, int totalItemCount) {
                final int lastItem = firstVisibleItem + visibleItemCount;
                if (lastItem == totalItemCount) {
                    if (preLast != lastItem) {
                        if (dialogOffset < dialogsCount) {
                            dialogOffset += 15;
                            getVKDialogs();
                        }
                        preLast = lastItem;
                    }
                }
            }
        });
        listView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                showDialog(position);
            }
        });
    }

    ArrayList<DialogsItem> parseMessages(JSONArray messages) {
        ArrayList<DialogsItem> dialogsItems = new ArrayList<>();

        for (int i = 0; i < messages.length(); i++) {
            try {
                DialogsItem dialogsItem = new DialogsItem(messages.getJSONObject(i));
                dialogsItems.add(dialogsItem);
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
        return dialogsItems;
    }

    public SocialNetworks getCurrentSocialNetwork() {
        return currentSocialNetwork;
    }

    void showDialog(int position) {
        ChatArrayFragment dialogsListFragment = new ChatArrayFragment();

        DialogsItem dialogsItem = dialogsItems.get(position);

        Bundle bundle = new Bundle();
        bundle.putSerializable("CURRENT_USER_ID", new User(
                "",
                "",
                VKAccessToken.currentToken().userId));
        bundle.putSerializable(ChatArrayFragment.CURRENT_USER_ID_DIALOG_WITH, new User(
                dialogsItem.getImageURL(),
                dialogsItem.getDialogName(),
                dialogsItem.getUserID()));

        dialogsListFragment.setArguments(bundle);

        FragmentTransaction fragmentTransaction = getActivity().getSupportFragmentManager().beginTransaction();
        fragmentTransaction.replace(R.id.frame_layout, dialogsListFragment, "visible_dialog");
        fragmentTransaction.addToBackStack(null);

        fragmentTransaction.setTransition(FragmentTransaction.TRANSIT_FRAGMENT_FADE);
        fragmentTransaction.commit();
    }
}

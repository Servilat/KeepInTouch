package com.servilat.keepintouch.Dialog;

import android.app.NotificationManager;
import android.content.Context;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentTransaction;
import android.support.v4.app.ListFragment;
import android.support.v4.app.NotificationCompat;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AbsListView;
import android.widget.AdapterView;
import android.widget.ListView;
import android.widget.Toast;

import com.android.volley.Request;
import com.android.volley.RequestQueue;
import com.android.volley.toolbox.StringRequest;
import com.android.volley.toolbox.Volley;
import com.servilat.keepintouch.Chat.Message;
import com.servilat.keepintouch.Chat.UserChatFragment;
import com.servilat.keepintouch.Chat.User;
import com.servilat.keepintouch.Login.LoginVKFragment;
import com.servilat.keepintouch.R;
import com.servilat.keepintouch.SocialNetworks;
import com.servilat.keepintouch.Util;
import com.vk.sdk.VKAccessToken;
import com.vk.sdk.VKSdk;
import com.vk.sdk.api.VKError;
import com.vk.sdk.api.VKParameters;
import com.vk.sdk.api.VKRequest;
import com.vk.sdk.api.VKResponse;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.List;

import static com.servilat.keepintouch.MainActivity.SOCIAL_NETWORK;

public class DialogsListFragment extends ListFragment {
    public static final int NOTIFY_ID = 70000;

    private String new_pts;
    private String server;
    private String key;
    private String ts;

    private Context context;
    private Fragment fragment;
    private RequestQueue requestQueue;
    private ArrayList<DialogsItem> dialogsItems;
    private DialogsListFragmentAdapter adapter;
    private SocialNetworks currentSocialNetwork;
    private ListView listView;
    private int dialogsCount;
    private int dialogOffset = 0;

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        context = getContext();
        requestQueue = Volley.newRequestQueue(context);
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        setHasOptionsMenu(true);
        getActivity().findViewById(R.id.toolbar).setBackgroundColor(context.getResources().getColor(R.color.colorVK));

        fragment = this;
        dialogOffset = 0;
        dialogsItems = new ArrayList<>();

        adapter = new DialogsListFragmentAdapter(dialogsItems, context);
        setListAdapter(adapter);

        Bundle arguments = getArguments();
        if (arguments != null) {
            switch ((SocialNetworks) arguments.getSerializable(SOCIAL_NETWORK)) {
                case VK:
                    getVKDialogs();
                    currentSocialNetwork = SocialNetworks.VK;
                    break;
                case TELEGRAM:
                    getFacebookDialogs();
                    currentSocialNetwork = SocialNetworks.TELEGRAM;
                    break;
                case FACEBOOK:
                    currentSocialNetwork = SocialNetworks.FACEBOOK;
                    break;
            }
        }

        setUpLongPullServer();
        return inflater.inflate(R.layout.messages_layout, container, false);
    }

    private void getFacebookDialogs() {

    }

    @Override
    public void onCreateOptionsMenu(Menu menu, MenuInflater inflater) {
        inflater.inflate(R.menu.main, menu);
        super.onCreateOptionsMenu(menu, inflater);
    }

    private ArrayList<DialogsItem> getVKDialogs() {
        final ArrayList<DialogsItem> dialogsItems = new ArrayList<>();
        VKRequest request = new VKRequest("execute", VKParameters.from("code", String.format(Util.executeCodeDialogs, dialogOffset)));
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
                        switch (currentSocialNetwork) {
                            case VK:
                                if (dialogOffset < dialogsCount) {
                                    dialogOffset += 15;
                                    getVKDialogs();
                                }
                                break;
                            case FACEBOOK:
                                break;
                        }
                        preLast = lastItem;
                    }
                }
            }
        });
        listView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                switch (currentSocialNetwork) {
                    case VK:
                        showDialog(position);
                        break;
                    case FACEBOOK:
                        break;
                }
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

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case R.id.action_change_user:
                VKSdk.logout();
                getActivity().getSupportFragmentManager().popBackStack();
                vkLogInWindow();
                return true;
            default:
                return super.onOptionsItemSelected(item);
        }
    }

    public SocialNetworks getCurrentSocialNetwork() {
        return currentSocialNetwork;
    }

    void showDialog(int position) {
        UserChatFragment dialogsListFragment = new UserChatFragment();

        DialogsItem dialogsItem = dialogsItems.get(position);

        Bundle bundle = new Bundle();
        bundle.putSerializable("CURRENT_USER_ID", new User(
                "",
                "",
                VKAccessToken.currentToken().userId));

        bundle.putSerializable(UserChatFragment.CURRENT_USER_ID_DIALOG_WITH, new User(
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

    void vkLogInWindow() {
        FragmentTransaction fragmentTransaction = getActivity().getSupportFragmentManager().beginTransaction();
        fragmentTransaction.replace(R.id.frame_layout, new LoginVKFragment(), "visible_fragment");
        fragmentTransaction.addToBackStack(null);
        fragmentTransaction.setTransition(FragmentTransaction.TRANSIT_FRAGMENT_FADE);
        fragmentTransaction.commit();
    }

    void setUpLongPullServer() {
        VKRequest vkRequest = new VKRequest("messages.getLongPollServer", VKParameters.from("need_pts", 1));
        vkRequest.executeWithListener(new VKRequest.VKRequestListener() {
            @Override
            public void onComplete(VKResponse response) {
                super.onComplete(response);
                try {
                    JSONObject vkResponse = response.json.getJSONObject("response");
                    setLongPullServerParameters(vkResponse);
                    startListenLongPullServer();
                } catch (JSONException e) {
                    e.printStackTrace();
                }
            }
        });
    }

    void startListenLongPullServer() {
        String request = String.format(Util.longPullServerRequest, server, key, ts);

        StringRequest stringRequest = new StringRequest(
                Request.Method.GET,
                request,
                response -> {
                    try {
                        JSONObject responseJSON = new JSONObject(response);
                        getLongPullHistory();
                        ts = responseJSON.getString("ts");
                    } catch (JSONException e) {
                        e.printStackTrace();
                    }
                }, error -> {
            Toast.makeText(context, "Response", Toast.LENGTH_SHORT);
        });
        stringRequest.setTag(this);
        requestQueue.add(stringRequest);
    }

    void getLongPullHistory() {
        VKRequest vkRequest = new VKRequest(
                "execute",
                VKParameters.from("code", String.format(Util.executeCodeNotifications, ts, new_pts)));

        vkRequest.executeWithListener(new VKRequest.VKRequestListener() {
            @Override
            public void onError(VKError error) {
                super.onError(error);
            }

            @Override
            public void onComplete(VKResponse response) {
                try {
                    super.onComplete(response);
                    JSONObject vkResponse = response.json.getJSONObject("response");
                    JSONArray messages = vkResponse.getJSONArray("messages");
                    if (messages.length() != 0) {
                        List<Message> messageList = getReceivedMessages(messages);
                        sendNotification(messageList);
                        if (fragment != null && fragment.isVisible()) {
                            setDialogsWithNewMessages(messageList);
                        }
                    }
                    new_pts = vkResponse.getString("new_pts");
                    startListenLongPullServer();
                } catch (JSONException e) {
                    e.printStackTrace();
                }
            }
        });
    }

    private void setDialogsWithNewMessages(List<Message> messageList) {
        label:
        for (Message message : messageList) {
            for (int i = 0; i < dialogsItems.size(); i++) {
                DialogsItem dialogsItem = dialogsItems.get(i);
                if (dialogsItem.getUserID().equals(message.getSender().getUserID())) {
                    dialogsItems.remove(dialogsItem);
                    dialogsItem.setMessageTime(message.getTime());
                    dialogsItem.setUserMessage(message.getMessage());
                    dialogsItems.add(0, dialogsItem);
                    break label;
                }
            }
            dialogsItems.add(0, new DialogsItem(
                    message.getSender().getDialogName(),
                    message.getMessage(),
                    message.getTime(),
                    message.getSender().getImageURL(),
                    message.getSender().getUserID()
            ));
        }
        adapter.notifyDataSetChanged();
    }

    private void sendNotification(List<Message> messageList) {
        NotificationCompat.Builder builder = new NotificationCompat.Builder(context);
        NotificationManager mNotificationManager =
                (NotificationManager) getActivity().getSystemService(Context.NOTIFICATION_SERVICE);
        for (Message message : messageList) {
            builder
                    .setSmallIcon(R.mipmap.ic_launcher)
                    .setContentTitle(message.getSender().getDialogName())
                    .setContentText(message.getMessage())
                    .setPriority(NotificationCompat.PRIORITY_MAX);
            mNotificationManager.notify(NOTIFY_ID, builder.build());
        }
    }

    void setLongPullServerParameters(JSONObject response) throws JSONException {
        new_pts = response.getString("pts");
        server = response.getString("server");
        key = response.getString("key");
        ts = response.getString("ts");
    }

    List<Message> getReceivedMessages(JSONArray messagesJSONArray) throws JSONException {
        List<Message> messageList = new ArrayList<>();

        for (int i = 0; i < messagesJSONArray.length(); i++) {
            JSONObject messageJSON = messagesJSONArray.getJSONObject(i);
            String userID;
            String dialogName;
            if (messageJSON.has("chat_id")) {
                userID = String.valueOf(messageJSON.getInt("chat_id") + 2000000000);
                dialogName = messageJSON.getString("title");
            } else {
                userID = messageJSON.getString("user_id");
                dialogName = messageJSON.getString("first_name") + " " + messageJSON.getString("last_name");
            }

            messageList.add(new Message(
                    messageJSON.getString("body"),
                    Util.convertTime(messageJSON.getLong("date")),
                    new User(
                            messageJSON.getString("photo_100"),
                            dialogName,
                            userID
                    )
            ));
        }
        return messageList;
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        requestQueue.cancelAll(this);
    }
}

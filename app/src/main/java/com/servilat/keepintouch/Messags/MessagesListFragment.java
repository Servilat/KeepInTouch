package com.servilat.keepintouch.Messags;

import android.os.Bundle;
import android.support.v4.app.ListFragment;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.View;
import android.view.ViewGroup;

import com.servilat.keepintouch.R;

import java.util.ArrayList;

public class MessagesListFragment extends ListFragment {
    ArrayList<MessagesItem> messagesItems;
    MessagesListFragmentAdapter adapter;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        setHasOptionsMenu(true);

        messagesItems = new ArrayList<>();
        messagesItems.add(new MessagesItem("Евгений Гаркавик", "https://pp.userapi.com/c845420/v845420182/3a5d6/TbXEYw2Ieic.jpg", "Hello world!", "20:51", 167589597));
        adapter = new MessagesListFragmentAdapter(messagesItems, getContext());
        setListAdapter(adapter);
        return inflater.inflate(R.layout.messages_layout, container, false);
    }

    @Override
    public void onCreateOptionsMenu(Menu menu, MenuInflater inflater) {
        inflater.inflate(R.menu.main, menu);
        super.onCreateOptionsMenu(menu, inflater);
    }
}

package com.servilat.keepintouch;

import android.content.Intent;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentTransaction;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.Toast;

import com.servilat.keepintouch.Messags.MessagesListFragment;
import com.squareup.picasso.Picasso;
import com.vk.sdk.VKAccessToken;
import com.vk.sdk.VKCallback;
import com.vk.sdk.VKScope;
import com.vk.sdk.VKSdk;
import com.vk.sdk.VKServiceActivity;
import com.vk.sdk.api.VKError;

import java.util.ArrayList;
import java.util.Arrays;

import static com.servilat.keepintouch.Util.showAlertMessage;

public class LoginVKFragment extends Fragment implements View.OnClickListener {
    String[] vkScope = {VKScope.MESSAGES, VKScope.OFFLINE, VKScope.STATUS};

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.login_vk_layout, container, false);

        Button button = view.findViewById(R.id.login_button_vk);

        button.setOnClickListener(this);

        return view;
    }

    @Override
    public void onClick(View v) {
        if (Util.isConnectedToInternet()) {
            loginAction();
        } else {
            showAlertMessage(
                    getActivity(),
                    getString(R.string.internet_connection_error_title),
                    getString(R.string.internet_connection_error_message));
        }
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        if (!VKSdk.onActivityResult(requestCode, resultCode, data, new VKCallback<VKAccessToken>() {
            @Override
            public void onResult(VKAccessToken res) {
                showMessagesList();
                Picasso.with(getContext()).load("https://pp.userapi.com/c831209/v831209900/e436c/4KHp30570DE.jpg").
                        placeholder(R.drawable.placeholder_person).
                        into((ImageView) getActivity().findViewById(R.id.nav_user_photo));
            }

            @Override
            public void onError(VKError error) {
                Toast.makeText(getContext(), "Error", Toast.LENGTH_LONG).show();

            }
        })) {
            super.onActivityResult(requestCode, resultCode, data);
        }
    }

    void showMessagesList() {
        MessagesListFragment messagesListFragment = new MessagesListFragment();

        FragmentTransaction fragmentTransaction = getActivity().getSupportFragmentManager().beginTransaction();
        fragmentTransaction.replace(R.id.frame_layout, messagesListFragment);
        fragmentTransaction.addToBackStack(null);
        fragmentTransaction.setTransition(FragmentTransaction.TRANSIT_FRAGMENT_FADE);
        fragmentTransaction.commit();
    }

    public void loginAction() {
        Intent intent = new Intent(getActivity(), VKServiceActivity.class);
        intent.putExtra("arg1", "Authorization");
        ArrayList<String> scope = new ArrayList<>(Arrays.asList(vkScope));
        intent.putStringArrayListExtra("arg2", scope);
        intent.putExtra("arg4", VKSdk.isCustomInitialize());
        startActivityForResult(intent, VKServiceActivity.VKServiceType.Authorization.getOuterCode());
    }
}

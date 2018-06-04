package com.servilat.keepintouch.Login;

import android.content.Intent;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentTransaction;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Toast;

import com.facebook.AccessToken;
import com.facebook.AccessTokenTracker;
import com.facebook.CallbackManager;
import com.facebook.FacebookCallback;
import com.facebook.FacebookException;
import com.facebook.login.LoginManager;
import com.facebook.login.LoginResult;
import com.servilat.keepintouch.Dialog.DialogsListFragment;
import com.servilat.keepintouch.Dialog.SendFilesDialog;
import com.servilat.keepintouch.R;
import com.servilat.keepintouch.SocialNetworks;
import com.servilat.keepintouch.Util;

import java.util.Arrays;

import static com.servilat.keepintouch.MainActivity.SOCIAL_NETWORK;
import static com.servilat.keepintouch.Util.showAlertMessage;

public class LoginFacebookFragment extends Fragment implements View.OnClickListener {
    CallbackManager callbackManager;
    Fragment fragment;
    LoginManager loginManager;
    AccessTokenTracker accessTokenTracker;

    public static final String[] FACEBOOK_PERMISSIONS = {"public_profile", "pages_messaging"};

    @Override
    public void onDestroy() {
        super.onDestroy();
        accessTokenTracker.startTracking();
    }

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.login_facebook_layout, container, false);

        accessTokenTracker = new AccessTokenTracker() {
            @Override
            protected void onCurrentAccessTokenChanged(AccessToken oldAccessToken, AccessToken currentAccessToken) {

            }
        };
        callbackManager = CallbackManager.Factory.create();
        loginManager = LoginManager.getInstance();

        this.fragment = this;

        return view;
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        loginManager.registerCallback(callbackManager, new FacebookCallback<LoginResult>() {
            @Override
            public void onSuccess(LoginResult loginResult) {
                getActivity().getSupportFragmentManager().popBackStack();
                showSendWindow();
            }

            @Override
            public void onCancel() {

            }

            @Override
            public void onError(FacebookException error) {

            }
        });

        getActivity().findViewById(R.id.login_button_facebook).setOnClickListener(this);
    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        callbackManager.onActivityResult(requestCode, resultCode, data);
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

    void loginAction() {
        loginManager.logInWithReadPermissions(fragment, Arrays.asList(FACEBOOK_PERMISSIONS));
    }

    private void showSendWindow() {
        SendFilesDialog sendFilesDialog = new SendFilesDialog();

        FragmentTransaction fragmentTransaction = getActivity().getSupportFragmentManager().beginTransaction();
        fragmentTransaction.replace(R.id.frame_layout, sendFilesDialog, "visible_messenger");
        fragmentTransaction.addToBackStack(null);
        fragmentTransaction.setTransition(FragmentTransaction.TRANSIT_FRAGMENT_FADE);
        fragmentTransaction.commit();
    }
}

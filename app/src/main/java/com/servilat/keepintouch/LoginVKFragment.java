package com.servilat.keepintouch;

import android.content.DialogInterface;
import android.content.Intent;
import android.os.Build;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.support.v7.app.AlertDialog;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.Toast;

import com.vk.sdk.VKAccessToken;
import com.vk.sdk.VKCallback;
import com.vk.sdk.VKScope;
import com.vk.sdk.VKSdk;
import com.vk.sdk.VKServiceActivity;
import com.vk.sdk.api.VKError;

import java.util.ArrayList;
import java.util.Arrays;

public class LoginVKFragment extends Fragment {
    String[] vkScope = {VKScope.MESSAGES, VKScope.OFFLINE, VKScope.STATUS};

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.login_vk_layout, container, false);
        Button button = (Button) view.findViewById(R.id.login_button_vk);
        button.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (Util.isConnectedToInternet()) {
                    loginAction();
                } else {
                    AlertDialog.Builder builder;
                    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
                        builder = new AlertDialog.Builder(getActivity(), android.R.style.Theme_Material_Light_Dialog_Alert);
                    } else {
                        builder = new AlertDialog.Builder(getActivity(), android.R.style.Theme_Holo_Light);
                    }
                    builder.setTitle("Internet connection error")
                            .setMessage("Some problems with Internet connection. Check your internet connection and try to log in again.")
                            .setPositiveButton(android.R.string.yes, new DialogInterface.OnClickListener() {
                                public void onClick(DialogInterface dialog, int which) {
                                    dialog.cancel();
                                }
                            })
                            .setIcon(android.R.drawable.ic_dialog_alert)
                            .setIconAttribute(android.R.attr.alertDialogIcon)
                            .show();
                }
            }
        });
        return view;
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
    }


    public void loginAction() {
        Intent intent = new Intent(getActivity(), VKServiceActivity.class);
        intent.putExtra("arg1", "Authorization");
        ArrayList<String> scope = new ArrayList<>(Arrays.asList(vkScope));
        intent.putStringArrayListExtra("arg2", scope);
        intent.putExtra("arg4", VKSdk.isCustomInitialize());
        startActivityForResult(intent, VKServiceActivity.VKServiceType.Authorization.getOuterCode());
    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        if (!VKSdk.onActivityResult(requestCode, resultCode, data, new VKCallback<VKAccessToken>() {
            @Override
            public void onResult(VKAccessToken res) {
                Toast.makeText(getContext(), VKAccessToken.currentToken().accessToken, Toast.LENGTH_LONG).show();
            }

            @Override
            public void onError(VKError error) {
                Toast.makeText(getContext(), "Error", Toast.LENGTH_LONG).show();

            }
        })) {
            super.onActivityResult(requestCode, resultCode, data);
        }
    }
}

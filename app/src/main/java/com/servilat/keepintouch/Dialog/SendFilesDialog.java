package com.servilat.keepintouch.Dialog;

import android.app.Activity;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentActivity;
import android.support.v4.app.FragmentTransaction;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Toast;

import com.facebook.login.LoginManager;
import com.facebook.messenger.MessengerUtils;
import com.facebook.messenger.ShareToMessengerParams;
import com.servilat.keepintouch.Login.LoginFacebookFragment;
import com.servilat.keepintouch.R;

public class SendFilesDialog extends Fragment {
    public static final int REQUEST_CODE_CHOOSER = 10;
    public static final int REQUEST_CODE_SHARE_TO_MESSENGER = 12;
    private Uri fileUri = null;
    private String mimeType;

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        setHasOptionsMenu(true);
        return inflater.inflate(R.layout.send_file_messenger, container, false);
    }

    @Override
    public void onCreateOptionsMenu(Menu menu, MenuInflater inflater) {
        inflater.inflate(R.menu.main, menu);
        super.onCreateOptionsMenu(menu, inflater);
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case R.id.action_change_user:
                LoginManager.getInstance().logOut();
                getActivity().getSupportFragmentManager().popBackStack();
                facebookLogInWindow();
                return true;
            default:
                return super.onOptionsItemSelected(item);
        }
    }

    @Override
    public void onActivityCreated(@Nullable Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);
        FragmentActivity activity = getActivity();
        activity.findViewById(R.id.messenger_send_button).setOnClickListener(v -> {
            if (fileUri != null) {
                ShareToMessengerParams shareToMessengerParams = ShareToMessengerParams.newBuilder(fileUri, mimeType).build();
                MessengerUtils.shareToMessenger(
                        getActivity(),
                        REQUEST_CODE_SHARE_TO_MESSENGER,
                        shareToMessengerParams
                );
            }
        });
        activity.findViewById(R.id.jpeg_png_button).setOnClickListener(v -> {
            sendIntent("image/*");
        });
        activity.findViewById(R.id.mp3_mp4_button).setOnClickListener(v -> {
            sendIntent("audio/mpeg");
        });
        activity.findViewById(R.id.gif_button).setOnClickListener(v -> {
            sendIntent("image/gif");
        });
        activity.findViewById(R.id.webp_button).setOnClickListener(v -> {
            sendIntent("image/webp");
        });
    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (requestCode == REQUEST_CODE_CHOOSER && resultCode == Activity.RESULT_OK) {
            fileUri = data.getData();
            Toast.makeText(getContext(), "File chosen", Toast.LENGTH_SHORT).show();
        } else {
            fileUri = null;
        }
    }

    void facebookLogInWindow() {
        FragmentTransaction fragmentTransaction = getActivity().getSupportFragmentManager().beginTransaction();
        fragmentTransaction.replace(R.id.frame_layout, new LoginFacebookFragment(), "visible_fragment");
        fragmentTransaction.addToBackStack(null);
        fragmentTransaction.setTransition(FragmentTransaction.TRANSIT_FRAGMENT_FADE);
        fragmentTransaction.commit();
    }

    void sendIntent(String mimeType) {
        Intent intent = new Intent(Intent.ACTION_OPEN_DOCUMENT);
        this.mimeType = (mimeType);
        intent.setType(mimeType);
        intent.addCategory(Intent.CATEGORY_OPENABLE);
        startActivityForResult(intent, REQUEST_CODE_CHOOSER);
    }
}

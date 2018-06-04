package com.servilat.keepintouch;

import android.os.Bundle;
import android.support.design.widget.NavigationView;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentTransaction;
import android.support.v4.view.GravityCompat;
import android.support.v4.widget.DrawerLayout;
import android.support.v7.app.ActionBarDrawerToggle;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.view.MenuItem;
import android.view.View;
import android.widget.ImageView;
import android.widget.TextView;

import com.facebook.AccessToken;
import com.facebook.GraphRequest;
import com.facebook.GraphResponse;
import com.servilat.keepintouch.Dialog.DialogsListFragment;
import com.servilat.keepintouch.Dialog.SendFilesDialog;
import com.servilat.keepintouch.Login.LoginFacebookFragment;
import com.servilat.keepintouch.Login.LoginTelegramFragment;
import com.servilat.keepintouch.Login.LoginVKFragment;
import com.squareup.picasso.Picasso;
import com.vk.sdk.VKAccessToken;
import com.vk.sdk.VKSdk;
import com.vk.sdk.api.VKApi;
import com.vk.sdk.api.VKApiConst;
import com.vk.sdk.api.VKParameters;
import com.vk.sdk.api.VKRequest;
import com.vk.sdk.api.VKResponse;
import com.vk.sdk.api.model.VKApiUserFull;
import com.vk.sdk.api.model.VKList;

import org.json.JSONException;
import org.json.JSONObject;

public class MainActivity extends AppCompatActivity
        implements NavigationView.OnNavigationItemSelectedListener {
    private DrawerLayout drawerLayout;
    private NavigationView navigationView;
    private Toolbar toolbar;
    private Fragment visibleFragment;
    private int colorID;
    public static final String SOCIAL_NETWORK = "SOCIAL_NETWORK";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        toolbar = findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);

        drawerLayout = findViewById(R.id.drawer_layout);
        ActionBarDrawerToggle toggle = new ActionBarDrawerToggle(
                this, drawerLayout, toolbar, R.string.navigation_drawer_open, R.string.navigation_drawer_close) {
            @Override
            public void onDrawerClosed(View drawerView) {
                super.onDrawerClosed(drawerView);
                if (visibleFragment != null) {
                    FragmentTransaction fragmentTransaction = getSupportFragmentManager().beginTransaction();
                    fragmentTransaction.replace(R.id.frame_layout, visibleFragment, "visible_fragment");
                    fragmentTransaction.addToBackStack(null);
                    fragmentTransaction.setTransition(FragmentTransaction.TRANSIT_FRAGMENT_FADE);
                    fragmentTransaction.commit();
                    toolbar.setBackgroundColor(getResources().getColor(colorID));

                }

            }
        };
        drawerLayout.addDrawerListener(toggle);
        toggle.syncState();

        navigationView = findViewById(R.id.nav_view);
        navigationView.setNavigationItemSelectedListener(this);
        navigationView.setItemIconTintList(null);
    }

    @Override
    public void onBackPressed() {
        DrawerLayout drawer = findViewById(R.id.drawer_layout);
        if (drawer.isDrawerOpen(GravityCompat.START)) {
            drawer.closeDrawer(GravityCompat.START);
        } else {
            super.onBackPressed();
        }
    }

    @Override
    public boolean onNavigationItemSelected(MenuItem item) {
        int id = item.getItemId();

        visibleFragment = null;
        switch (id) {
            case R.id.nav_facebook_messenger:
                colorID = R.color.colorFacebook;
                if (!FacebookIsLoggedIn()) {
                    visibleFragment = new LoginFacebookFragment();
                } else if (!checkMessengerOnVisibility()) {
                    setMainActivityAppearance(SocialNetworks.FACEBOOK);
                }
                break;
            case R.id.nav_vk:
                colorID = R.color.colorVK;
                if (!VKIsLoggedIn()) {
                    visibleFragment = new LoginVKFragment();
                } else if (!checkCurrentVisibleFragment(SocialNetworks.VK)) {
                    setMainActivityAppearance(SocialNetworks.VK);
                }
                break;
            case R.id.nav_telegram:
                colorID = R.color.colorTelegram;
                visibleFragment = new LoginTelegramFragment();
                break;
        }

        drawerLayout.closeDrawer(GravityCompat.START);
        return true;
    }

    private boolean checkMessengerOnVisibility() {
        SendFilesDialog fragment = (SendFilesDialog) getSupportFragmentManager().findFragmentByTag("visible_messenger");
        if (fragment != null && fragment.isVisible()) {
            return true;
        }
        return false;
    }

    void showUserDialogs(SocialNetworks socialNetwork) {
        DialogsListFragment dialogsListFragment = new DialogsListFragment();

        Bundle bundle = new Bundle();
        switch (socialNetwork) {
            case TELEGRAM:
                bundle.putSerializable(SOCIAL_NETWORK, SocialNetworks.TELEGRAM);
                break;
            case FACEBOOK:
                setMessengerAppearance();
                return;
            case VK:
                bundle.putSerializable(SOCIAL_NETWORK, SocialNetworks.VK);
        }

        dialogsListFragment.setArguments(bundle);
        FragmentTransaction fragmentTransaction = getSupportFragmentManager().beginTransaction();
        fragmentTransaction.replace(R.id.frame_layout, dialogsListFragment, "visible_dialogs");
        fragmentTransaction.addToBackStack(null);
        fragmentTransaction.setTransition(FragmentTransaction.TRANSIT_FRAGMENT_FADE);
        fragmentTransaction.commit();
    }

    public void setNavigationDrawerHeader(SocialNetworks socialNetwork) {
        switch (socialNetwork) {
            case VK:
                vkRequestForHeader();
                break;
            case FACEBOOK:
                facebookRequestForHeader();
                break;
            case TELEGRAM:
                telegramRequestForHeader();
        }
    }

    private void telegramRequestForHeader() {
    }

    private void facebookRequestForHeader() {
        GraphRequest graphRequest = GraphRequest.newMeRequest(
                AccessToken.getCurrentAccessToken(),
                new GraphRequest.GraphJSONObjectCallback() {
                    @Override
                    public void onCompleted(JSONObject object, GraphResponse response) {
                        try {
                            String name = object.getString("name");
                            String pictureURL = object.getJSONObject("picture").getJSONObject("data").getString("url");
                            fillNavigationDrawerHeader(name, "", pictureURL);
                        } catch (JSONException e) {

                        }
                    }
                });

        Bundle parameters = new Bundle();
        parameters.putString("fields", "name, picture.type(normal)");
        graphRequest.setParameters(parameters);

        graphRequest.executeAsync();
    }

    private void vkRequestForHeader() {
        VKRequest vkRequest = VKApi.users().get(VKParameters.from(
                VKApiConst.USER_IDS, VKAccessToken.currentToken().userId,
                VKApiConst.FIELDS, "photo_100, status"));

        vkRequest.executeWithListener(new VKRequest.VKRequestListener() {
            @Override
            public void onComplete(VKResponse response) {
                super.onComplete(response);
                VKApiUserFull currentVKUser = (VKApiUserFull) ((VKList) response.parsedModel).get(0);
                String status;
                try {
                    status = currentVKUser.fields.getString("status");
                } catch (JSONException e) {
                    status = "";
                }
                fillNavigationDrawerHeader(currentVKUser.toString(), status, currentVKUser.photo_100);
            }
        });
    }

    private void fillNavigationDrawerHeader(String fullName, String status, String photo) {
        ((TextView) findViewById(R.id.nav_user_name)).setText(fullName);
        ((TextView) findViewById(R.id.nav_user_status)).setText(status);
        Picasso.with(getApplicationContext())
                .load(photo)
                .placeholder(R.drawable.placeholder_person)
                .error(R.drawable.placeholder_person)
                .into((ImageView) findViewById(R.id.nav_user_photo));
    }

    boolean FacebookIsLoggedIn() {
        AccessToken accessToken = AccessToken.getCurrentAccessToken();
        return accessToken != null && !accessToken.isExpired();
    }

    boolean VKIsLoggedIn() {
        return VKSdk.isLoggedIn();
    }

    boolean checkCurrentVisibleFragment(SocialNetworks socialNetwork) {
        DialogsListFragment fragment = (DialogsListFragment) getSupportFragmentManager().findFragmentByTag("visible_dialogs");

        if (fragment != null && (fragment.getCurrentSocialNetwork() == socialNetwork) && fragment.isVisible()) {
            return true;
        }
        return false;
    }

    void setMainActivityAppearance(SocialNetworks socialNetwork) {
        setNavigationDrawerHeader(socialNetwork);
        showUserDialogs(socialNetwork);
    }

    void setMessengerAppearance() {
        SendFilesDialog sendFilesDialog = new SendFilesDialog();
        FragmentTransaction fragmentTransaction = getSupportFragmentManager().beginTransaction();
        fragmentTransaction.replace(R.id.frame_layout, sendFilesDialog, "visible_messenger");
        fragmentTransaction.addToBackStack(null);
        fragmentTransaction.setTransition(FragmentTransaction.TRANSIT_FRAGMENT_FADE);
        fragmentTransaction.commit();
    }
}

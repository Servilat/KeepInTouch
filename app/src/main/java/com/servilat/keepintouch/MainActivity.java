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

import com.servilat.keepintouch.Login.LoginFacebookFragment;
import com.servilat.keepintouch.Login.LoginTelegramFragment;
import com.servilat.keepintouch.Login.LoginVKFragment;
import com.servilat.keepintouch.Messags.MessagesListFragment;
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

import java.util.Map;

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

        toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);

        drawerLayout = (DrawerLayout) findViewById(R.id.drawer_layout);
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

        navigationView = (NavigationView) findViewById(R.id.nav_view);
        navigationView.setNavigationItemSelectedListener(this);
        navigationView.setItemIconTintList(null);
    }

    @Override
    public void onBackPressed() {
        DrawerLayout drawer = (DrawerLayout) findViewById(R.id.drawer_layout);
        if (drawer.isDrawerOpen(GravityCompat.START)) {
            drawer.closeDrawer(GravityCompat.START);
        } else {
            super.onBackPressed();
        }
    }

    /*@Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.main, menu);
        return true;
    }*/

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {

        int id = item.getItemId();

        if (id == R.id.action_search) {
            return true;
        }

        return super.onOptionsItemSelected(item);
    }

    @Override
    public boolean onNavigationItemSelected(MenuItem item) {
        int id = item.getItemId();

        visibleFragment = null;

        switch (id) {
            case R.id.nav_facebook_messenger:
                colorID = R.color.colorFacebook;
                visibleFragment = new LoginFacebookFragment();
                break;
            case R.id.nav_vk:
                colorID = R.color.colorVK;
                if (!VKSdk.isLoggedIn()) {
                    visibleFragment = new LoginVKFragment();
                } else {
                    MessagesListFragment fragment = (MessagesListFragment) getSupportFragmentManager().findFragmentByTag("visible_list");

                    if (fragment != null) {
                        if (fragment.getCurrentSocialNetwork() != SocialNetworks.VK) {
                            setNavigationDrawerHeader(SocialNetworks.VK);
                            showMessagesList(SocialNetworks.VK);
                        }
                    } else {
                        setNavigationDrawerHeader(SocialNetworks.VK);
                        showMessagesList(SocialNetworks.VK);
                    }
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

    void showMessagesList(SocialNetworks socialNetwork) {
        MessagesListFragment messagesListFragment = new MessagesListFragment();

        Bundle bundle = new Bundle();
        switch (socialNetwork) {
            case TELEGRAM:
                bundle.putSerializable(SOCIAL_NETWORK, SocialNetworks.TELEGRAM);
                break;
            case FACEBOOK:
                bundle.putSerializable(SOCIAL_NETWORK, SocialNetworks.FACEBOOK);
                break;
            case VK:
                bundle.putSerializable(SOCIAL_NETWORK, SocialNetworks.VK);
        }

        messagesListFragment.setArguments(bundle);
        FragmentTransaction fragmentTransaction = getSupportFragmentManager().beginTransaction();
        fragmentTransaction.replace(R.id.frame_layout, messagesListFragment, "visible_list");
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
    }

    private void vkRequestForHeader() {
        VKRequest vkRequest = VKApi.users().get(VKParameters.from(VKApiConst.USER_IDS, VKAccessToken.currentToken().userId,
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
}

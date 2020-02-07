package com.romellfudi.ussd.sample;

import android.content.Intent;
import android.content.IntentSender;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.design.widget.NavigationView;
import android.support.design.widget.Snackbar;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentTransaction;
import android.support.v4.view.GravityCompat;
import android.support.v4.widget.DrawerLayout;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.view.MenuItem;
import android.view.View;
import android.widget.ImageView;

import com.google.android.play.core.appupdate.AppUpdateInfo;
import com.google.android.play.core.appupdate.AppUpdateManager;
import com.google.android.play.core.appupdate.AppUpdateManagerFactory;
import com.google.android.play.core.install.InstallState;
import com.google.android.play.core.install.InstallStateUpdatedListener;
import com.google.android.play.core.install.model.AppUpdateType;
import com.google.android.play.core.install.model.InstallStatus;
import com.google.android.play.core.install.model.UpdateAvailability;
import com.google.android.play.core.tasks.Task;
import com.rbddevs.splashy.Splashy;
import com.romellfudi.ussd.BuildConfig;
import com.romellfudi.ussd.R;

/**
 * Main Activity
 *
 * @author Romell Domínguez
 * @version 1.0.b 23/02/2017
 * @since 1.0
 */
public class MainActivity extends AppCompatActivity
        implements NavigationView.OnNavigationItemSelectedListener,
        InstallStateUpdatedListener {

    private AppUpdateManager appUpdateManager;
    private Task<AppUpdateInfo> appUpdateInfoTask;
    private static final int REQUEST_CODE_IMMEDIATE_UPDATE = 1234;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        splashy();
        setContentView(R.layout.activity_main_menu);

        appUpdateManager = AppUpdateManagerFactory.create(this);
        appUpdateManager.registerListener(this);

        Toolbar toolbar = findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        getSupportActionBar().setTitle(getString(R.string.title_activity_cp1));
        NavigationView navigationView = findViewById(R.id.nav_view);
        navigationView.setNavigationItemSelectedListener(this);

        FragmentTransaction fragment = getSupportFragmentManager().beginTransaction();
        fragment.replace(R.id.fragment_layout, new MainFragment())
                .setTransition(FragmentTransaction.TRANSIT_FRAGMENT_OPEN)
                .addToBackStack(null)
                .commit();

    }

    private void checkUpdate() {
        appUpdateInfoTask = appUpdateManager.getAppUpdateInfo();
        appUpdateInfoTask.addOnSuccessListener(appUpdateInfo -> {
            showMessage("updateAvailability: " + appUpdateInfo.updateAvailability() +
                    " isUpdateTypeAllowed: " + appUpdateInfo.isUpdateTypeAllowed(AppUpdateType.IMMEDIATE));
            if (appUpdateInfo.updateAvailability() == UpdateAvailability.UPDATE_AVAILABLE
                    && appUpdateInfo.isUpdateTypeAllowed(AppUpdateType.IMMEDIATE)) {
                requestUpdate(appUpdateInfo);
            }
        });
    }

    private void requestUpdate(AppUpdateInfo appUpdateInfo) {
        try {
            appUpdateManager.startUpdateFlowForResult(
                    appUpdateInfo,
                    AppUpdateType.IMMEDIATE,
                    MainActivity.this,
                    REQUEST_CODE_IMMEDIATE_UPDATE);
        } catch (IntentSender.SendIntentException e) {
            showMessage(getString(R.string.request_error));
        }
    }

    private void splashy() {
        new Splashy(MainActivity.this)
                .setLogo(R.drawable.combine)
                .setLogoScaleType(ImageView.ScaleType.FIT_CENTER)
                .setAnimation(Splashy.Animation.GROW_LOGO_FROM_CENTER, 500)
                .setTitle(R.string.app_name)
                .setTitleColor(R.color.black)
                .setSubTitle("Version  " + BuildConfig.VERSION_NAME)
                .setProgressColor(R.color.black)
                .setBackgroundResource(R.color.splash)
                .setFullScreen(true)
                .setTime(2000)
                .show();
        Splashy.Companion.onComplete(() -> checkUpdate());
    }

    @Override
    public void onBackPressed() {
        DrawerLayout drawer = findViewById(R.id.drawer_layout);
        if (drawer.isDrawerOpen(GravityCompat.START)) {
            drawer.closeDrawer(GravityCompat.START);
        } else {
            finish();
        }
    }

    @Override
    public boolean onNavigationItemSelected(MenuItem item) {
        int id = item.getItemId();
        Fragment newFragment = null;
        String tittle = null;
        if (id == R.id.op1) {
            newFragment = new MainFragment();
            tittle = getResources().getString(R.string.title_activity_cp1);
        }
        FragmentTransaction ft = getSupportFragmentManager().beginTransaction();
        getSupportActionBar().setTitle(tittle);
        ft.replace(R.id.fragment_layout, newFragment)
                .setTransition(FragmentTransaction.TRANSIT_FRAGMENT_OPEN)
                .addToBackStack(null)
                .commit();
        DrawerLayout drawer = findViewById(R.id.drawer_layout);
        drawer.closeDrawer(GravityCompat.START);
        return true;
    }

    @Override
    public void onStateUpdate(InstallState installState) {
        if (installState.installStatus() == InstallStatus.DOWNLOADED) {
            showMessage(getString(R.string.been_downloaded));
            notifyUser();
        }
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        appUpdateManager.unregisterListener(MainActivity.this);
    }

    @Override
    protected void onResume() {
        super.onResume();
        appUpdateManager.getAppUpdateInfo().addOnSuccessListener(result -> {
            if (result.installStatus() == InstallStatus.DOWNLOADED) {
                notifyUser();
            }
        });
    }

    private void showMessage(String message) {
        Snackbar.make(findViewById(android.R.id.content), message, Snackbar.LENGTH_SHORT).show();
    }

    private void notifyUser() {
        Snackbar.make(findViewById(android.R.id.content), R.string.restart_update, Snackbar.LENGTH_INDEFINITE)
                .setAction(R.string.restart_update, new View.OnClickListener() {
                    @Override
                    public void onClick(View view) {
                        appUpdateManager.completeUpdate();
                        appUpdateManager.unregisterListener(MainActivity.this);
                    }
                }).show();
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        if (requestCode == REQUEST_CODE_IMMEDIATE_UPDATE) {
            if (resultCode != RESULT_OK) {
                checkUpdate();
            }
        }
    }
}

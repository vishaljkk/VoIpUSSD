package com.romellfudi.ussd.sample

import android.app.Activity
import android.content.Intent
import android.content.IntentSender.SendIntentException
import android.os.Bundle
import android.support.design.widget.NavigationView
import android.support.design.widget.Snackbar
import android.support.v4.app.Fragment
import android.support.v4.app.FragmentTransaction
import android.support.v4.view.GravityCompat
import android.support.v4.widget.DrawerLayout
import android.support.v7.app.AppCompatActivity
import android.support.v7.widget.Toolbar
import android.view.MenuItem
import android.view.View
import android.widget.ImageView
import com.google.android.play.core.appupdate.AppUpdateInfo
import com.google.android.play.core.appupdate.AppUpdateManager
import com.google.android.play.core.appupdate.AppUpdateManagerFactory
import com.google.android.play.core.install.InstallState
import com.google.android.play.core.install.InstallStateUpdatedListener
import com.google.android.play.core.install.model.AppUpdateType
import com.google.android.play.core.install.model.InstallStatus
import com.google.android.play.core.install.model.UpdateAvailability
import com.google.android.play.core.tasks.Task
import com.rbddevs.splashy.Splashy
import com.romellfudi.ussd.BuildConfig
import com.romellfudi.ussd.R

/**
 * Main Activity
 *
 * @author Romell Domínguez
 * @version 1.0.b 23/02/2017
 * @since 1.0
 */
class MainActivity : AppCompatActivity(), NavigationView.OnNavigationItemSelectedListener, InstallStateUpdatedListener {

    private lateinit var appUpdateManager: AppUpdateManager
    private lateinit var appUpdateInfoTask: Task<AppUpdateInfo>
    private val REQUEST_CODE_IMMEDIATE_UPDATE = 1234

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main_menu)
        splashy()
        val toolbar = findViewById<View>(R.id.toolbar) as Toolbar
        setSupportActionBar(toolbar)
        val navigationView = findViewById<View>(R.id.nav_view) as NavigationView
        navigationView.setNavigationItemSelectedListener(this)

        appUpdateManager = AppUpdateManagerFactory.create(this)
        appUpdateManager.registerListener(this)

        val fragment = supportFragmentManager.beginTransaction()
        fragment.replace(R.id.fragment_layout, MainFragment()) // f1_container is your FrameLayout container
        fragment.setTransition(FragmentTransaction.TRANSIT_FRAGMENT_OPEN)
        fragment.addToBackStack(null)
        fragment.commit()
        supportActionBar!!.title = getString(R.string.title_activity_cp1)

    }

    override fun onBackPressed() {
        val drawer = findViewById<View>(R.id.drawer_layout) as DrawerLayout
        if (drawer.isDrawerOpen(GravityCompat.START)) {
            drawer.closeDrawer(GravityCompat.START)
        } else {
            finish()
        }
    }

    override fun onNavigationItemSelected(item: MenuItem): Boolean {
        val id = item.itemId
        var newFragment: Fragment? = null
        var tittle: String? = null
        if (id == R.id.op1) {
            newFragment = MainFragment()
            tittle = resources.getString(R.string.title_activity_cp1)
        }
        val ft = supportFragmentManager.beginTransaction()
        supportActionBar!!.setTitle(tittle)
        ft.replace(R.id.fragment_layout, newFragment!!) // f1_container is your FrameLayout container
        ft.setTransition(FragmentTransaction.TRANSIT_FRAGMENT_OPEN)
        ft.addToBackStack(null)
        ft.commit()
        val drawer = findViewById<View>(R.id.drawer_layout) as DrawerLayout
        drawer.closeDrawer(GravityCompat.START)
        return true
    }

    private fun splashy() {
        Splashy(this@MainActivity)
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
                .show()
        Splashy.onComplete(object : Splashy.OnComplete {
            override fun onComplete() = checkUpdate()

        })
    }

    private fun checkUpdate() {
        appUpdateInfoTask = appUpdateManager.appUpdateInfo
        appUpdateInfoTask.addOnSuccessListener { appUpdateInfo: AppUpdateInfo ->
            showMessage("updateAvailability: " + appUpdateInfo.updateAvailability() +
                    " isUpdateTypeAllowed: " + appUpdateInfo.isUpdateTypeAllowed(AppUpdateType.IMMEDIATE))
            if (appUpdateInfo.updateAvailability() == UpdateAvailability.UPDATE_AVAILABLE
                    && appUpdateInfo.isUpdateTypeAllowed(AppUpdateType.IMMEDIATE)) {
                requestUpdate(appUpdateInfo)
            }
        }
    }

    private fun requestUpdate(appUpdateInfo: AppUpdateInfo) {
        try {
            appUpdateManager.startUpdateFlowForResult(
                    appUpdateInfo,
                    AppUpdateType.IMMEDIATE,
                    this@MainActivity,
                    REQUEST_CODE_IMMEDIATE_UPDATE)
        } catch (e: SendIntentException) {
            showMessage(getString(R.string.request_error))
        }
    }


    override fun onStateUpdate(installState: InstallState) {
        if (installState.installStatus() == InstallStatus.DOWNLOADED) {
            showMessage(getString(R.string.been_downloaded))
            notifyUser()
        }
    }

    override fun onDestroy() {
        super.onDestroy()
        appUpdateManager.unregisterListener(this@MainActivity)
    }

    override fun onResume() {
        super.onResume()
        appUpdateManager.appUpdateInfo.addOnSuccessListener { result: AppUpdateInfo ->
            if (result.installStatus() == InstallStatus.DOWNLOADED) {
                notifyUser()
            }
        }
    }

    private fun showMessage(message: String) {
        Snackbar.make(findViewById(android.R.id.content), message, Snackbar.LENGTH_SHORT).show()
    }

    private fun notifyUser() {
        Snackbar.make(findViewById(android.R.id.content), R.string.restart_update, Snackbar.LENGTH_INDEFINITE)
                .setAction(R.string.restart_update) {
                    appUpdateManager.completeUpdate()
                    appUpdateManager.unregisterListener(this@MainActivity)
                }.show()
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        if (requestCode == REQUEST_CODE_IMMEDIATE_UPDATE) {
            if (resultCode != Activity.RESULT_OK) {
                checkUpdate()
            }
        }
    }

}

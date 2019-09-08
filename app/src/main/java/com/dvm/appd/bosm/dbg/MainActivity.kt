package com.dvm.appd.bosm.dbg

import android.app.Activity
import android.app.NotificationChannel
import android.app.NotificationManager
import android.content.*
import android.net.ConnectivityManager
import android.net.Uri
import android.os.Build
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.util.Log
import android.view.Menu
import android.view.MenuItem
import android.view.View
import android.widget.Toast
import androidx.appcompat.app.AlertDialog
import androidx.core.view.isVisible
import androidx.navigation.NavController
import androidx.navigation.fragment.NavHostFragment
import androidx.navigation.ui.onNavDestinationSelected
import androidx.navigation.ui.setupWithNavController
import com.dvm.appd.bosm.dbg.di.AppModule
import com.dvm.appd.bosm.dbg.notification.FirebaseMessagingService
import com.dvm.appd.bosm.dbg.shared.NetworkChangeReciver
import com.google.android.material.bottomnavigation.BottomNavigationView
import com.google.android.material.snackbar.BaseTransientBottomBar
import com.google.android.material.snackbar.Snackbar
import com.google.android.play.core.appupdate.AppUpdateManagerFactory
import com.google.android.play.core.install.model.AppUpdateType
import com.google.android.play.core.install.model.UpdateAvailability
import com.google.firebase.FirebaseApp
import com.google.firebase.dynamiclinks.FirebaseDynamicLinks
import com.google.firebase.iid.FirebaseInstanceId
import kotlinx.android.synthetic.main.activity_main.*
import kotlinx.android.synthetic.main.checkbox.view.*

interface NetworkChangeNotifier {
    fun onNetworkStatusScahnged(isConnected: Boolean)
}

class MainActivity : AppCompatActivity(), NetworkChangeNotifier {

    private lateinit var bottomNav: BottomNavigationView
    private lateinit var navController: NavController
    private lateinit var sharedPreferences: SharedPreferences
    private var receiver: NetworkChangeReciver? = null
    private val REQUEST_CODE_UPDATE_IMMIDIATE = 101

    override fun onCreateOptionsMenu(menu: Menu?): Boolean {
        menuInflater.inflate(R.menu.actionbaritems, menu)
        return super.onCreateOptionsMenu(menu)
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)
        try {
            FirebaseApp.initializeApp(this)
        }
        catch (e: Exception){
            AlertDialog.Builder(this).setTitle("App Fucked Up Big Time")
                .setMessage("Please restart our app")
                .setNegativeButton("OK") { _, _ ->
                    finishAffinity()
                }
        }

        sharedPreferences = AppModule(application).providesSharedPreferences(application)
        setupNotificationChannel()
        checkForInvitation()
        checkNotificationPermissions()
        checkForUpdates()

        var navHostFragment = supportFragmentManager.findFragmentById(R.id.my_nav_host_fragment) as NavHostFragment
        navController = navHostFragment.navController
        bottomNav = findViewById(R.id.bottom_navigation_bar)
        bottomNav.setupWithNavController(navController)


        val filter = IntentFilter(ConnectivityManager.CONNECTIVITY_ACTION)
        receiver = NetworkChangeReciver(this)
        this.registerReceiver(receiver, filter)

    }

    /*Some devices explicitly need to specify autoStartup permissions explicitly. This function identifies if the device
    * falls under that category, and if it does, it shows an alert dialog box asking to give the necessary permissions*/
    private fun checkNotificationPermissions() {
        Log.d("Main Activity", "Shared Prefs = $sharedPreferences")
        val wantsNotifications = sharedPreferences.getBoolean("wantsNotification", true)
        if (wantsNotifications) {
            try {
                var intent: Intent? = null
                val manufacturer = android.os.Build.MANUFACTURER
                when {
                    "xiaomi".equals(manufacturer, ignoreCase = true) -> {
                        intent = Intent()
                        intent.component = ComponentName(
                            "com.miui.securitycenter",
                            "com.miui.permcenter.autostart.AutoStartManagementActivity"
                        )
                    }
                    "oppo".equals(manufacturer, ignoreCase = true) -> {
                        intent = Intent()
                        intent.component = ComponentName(
                            "com.coloros.safecenter",
                            "com.coloros.safecenter.permission.startup.StartupAppListActivity"
                        )
                    }
                    "vivo".equals(manufacturer, ignoreCase = true) -> {
                        intent = Intent()
                        intent.component = ComponentName(
                            "com.vivo.permissionmanager",
                            "com.vivo.permissionmanager.activity.BgStartUpManagerActivity"
                        )
                    }
                    "Letv".equals(manufacturer, ignoreCase = true) -> {
                        intent = Intent()
                        intent.component =
                            ComponentName(
                                "com.letv.android.letvsafe",
                                "com.letv.android.letvsafe.AutobootManageActivity"
                            )
                    }
                    "Honor".equals(manufacturer, ignoreCase = true) -> {
                        intent = Intent()
                        intent.component = ComponentName(
                            "com.huawei.systemmanager",
                            "com.huawei.systemmanager.optimize.process.ProtectActivity"
                        )
                    }
                }
                if (intent != null) {
                    val checkBoxView = View.inflate(this, R.layout.checkbox, null)
                    checkBoxView.checkbox_alertBox.setOnCheckedChangeListener { buttonView, isChecked ->
                        if (isChecked) {
                            sharedPreferences.edit().putBoolean("wantsNotification", false).apply()
                        } else {
                            sharedPreferences.edit().putBoolean("wantsNotification", true).apply()
                        }
                    }
                    val alertDialogBuilder = android.app.AlertDialog.Builder(this)
                    alertDialogBuilder.setTitle(resources.getString(R.string.alert_notification_title))
                    alertDialogBuilder.setMessage(resources.getString(R.string.alert_notification_message))
                        .setView(checkBoxView)
                        .setCancelable(false)
                        .setPositiveButton(
                            resources.getString(R.string.alert_notification_positive_button),
                            DialogInterface.OnClickListener { dialog, which ->
                                sharedPreferences.edit().putBoolean("wantsNotification", false).apply()
                                startActivity(intent)
                            })
                        .setNegativeButton(
                            resources.getString(R.string.alert_notification_negative_button),
                            DialogInterface.OnClickListener { dialog, which ->
                                dialog.cancel()
                            }).show()
                }

            } catch (e: Exception) {
                Log.e("AutoStart Execute", "Error in opening AutoStart = ${e.toString()}")
            }
        }
    }

    private fun checkForInvitation() {
        if (sharedPreferences.getBoolean("firstTime", true)) {
            // sharedPreferences.edit().putBoolean("firstTime", false).apply()
            FirebaseDynamicLinks.getInstance().getDynamicLink(intent).addOnSuccessListener {
                Log.d("Main Activity", "OnSuccess of dynamic link")
                var deepLink: Uri? = null
                if (it != null) {
                    deepLink = it.link
                    Toast.makeText(this, deepLink.getQueryParameter("invitedBy"), Toast.LENGTH_LONG).show()
                    Log.d("Main Activity", "Deep link received = $deepLink")
                } else {
                    Log.e("Main Activity", "Empty link found")
                }
            }.addOnFailureListener {
                Log.e("Main Activity", "Failed to get Link = $it")
            }
        }
    }

    /*This method is used for the initial setup of the notification channels
    If the notification chanel already exists, no action is taken, and hence it is safe to call this method every time the app starts*/
    private fun setupNotificationChannel() {
        startService(Intent(this, FirebaseMessagingService::class.java))
        // Notification Channels are only available for Oreo(Api Level 26) and onwards
        // Since support libraries don't have a library for setting up notification channels, this check is necessary
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val generalChannel = NotificationChannel(
                getString(R.string.chanel_id_general_notifications),
                getString(R.string.chanel_name_general_notifications),
                NotificationManager.IMPORTANCE_HIGH
            )
            generalChannel.description = getString(R.string.chanel_desc_general_notifications)
            generalChannel.canBypassDnd()

            val ratingsChannel = NotificationChannel(
                getString(R.string.chanel_id_rating_notifications),
                getString(R.string.chanel_name_rating_notifications),
                NotificationManager.IMPORTANCE_HIGH
            )
            ratingsChannel.description = getString(R.string.chanel_desc_rating_notifications)
            ratingsChannel.canBypassDnd()

            val statusChangeChannel = NotificationChannel(
                getString(R.string.chanel_id_status_change_notifications),
                getString(R.string.chanel_name_status_change_notifications),
                NotificationManager.IMPORTANCE_HIGH
            )
            statusChangeChannel.description = getString(R.string.chanel_desc_status_change_notifications)
            statusChangeChannel.canBypassDnd()

            val notificationManager = getSystemService(NOTIFICATION_SERVICE) as NotificationManager
            notificationManager.createNotificationChannels(listOf(generalChannel, ratingsChannel, statusChangeChannel))
        }
        FirebaseInstanceId.getInstance().instanceId.addOnSuccessListener {
            Log.d("Main Activity", "Recived New Token = ${it.token}}")
            // TODO send the new token to the backend server
        }.addOnFailureListener {
            Log.e("Main Activity", "Failed to recive token")
            setupNotificationChannel()
        }
    }

    private fun checkForUpdates() {
        val updateManager = AppUpdateManagerFactory.create(this)
        updateManager.appUpdateInfo.addOnSuccessListener {
            if (it.updateAvailability() == UpdateAvailability.UPDATE_AVAILABLE && it.isUpdateTypeAllowed(AppUpdateType.IMMEDIATE)) {
                updateManager.startUpdateFlowForResult(
                    it,
                    AppUpdateType.IMMEDIATE,
                    this,
                    REQUEST_CODE_UPDATE_IMMIDIATE
                )
            }
        }
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        return item.onNavDestinationSelected(navController) || super.onOptionsItemSelected(item)
    }

    override fun onResume() {
        startService(Intent(this, FirebaseMessagingService::class.java))
        val updateManager = AppUpdateManagerFactory.create(this)
        updateManager.appUpdateInfo.addOnSuccessListener {
            if (it.updateAvailability() == UpdateAvailability.DEVELOPER_TRIGGERED_UPDATE_IN_PROGRESS) {
                updateManager.startUpdateFlowForResult(
                    it,
                    AppUpdateType.IMMEDIATE,
                    this,
                    REQUEST_CODE_UPDATE_IMMIDIATE
                )
            }
        }
        super.onResume()
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        if (requestCode == REQUEST_CODE_UPDATE_IMMIDIATE) {
            if (resultCode != Activity.RESULT_OK) {
                // TODO Add analytics log here
                var builder = AlertDialog.Builder(this)
                builder.setTitle(resources.getString(R.string.title_update_failed))
                builder.setMessage(resources.getString(R.string.message_update_falied))
                builder.setNegativeButton(
                    "OK",
                    DialogInterface.OnClickListener { dialog, which ->
                        // TODO Restart app instead of closing it
                        finishAffinity()
                    }
                )
            }
        }
        super.onActivityResult(requestCode, resultCode, data)
    }

    override fun onDestroy() {
        this.unregisterReceiver(receiver)
        super.onDestroy()
    }

    fun hideCustomToolbarForLevel2Fragments() {
        fragmentName.isVisible = false
        cart.isVisible = false
        profile.isVisible = false
        notifications.isVisible = false
        bottom_navigation_bar.isVisible = false
        mainView.visibility = View.GONE
        textView7.isVisible = false
    }

    override fun onNetworkStatusScahnged(isConnected: Boolean) {
        if (isConnected) {
            val snackbar = Snackbar.make(this.coordinator_parent, "Back Online", Snackbar.LENGTH_SHORT)
            snackbar.view.setBackgroundColor(resources.getColor(R.color.colorGreen))
            snackbar.show()
        } else {
            Snackbar.make(this.coordinator_parent, "Not Connected to the internet", Snackbar.LENGTH_INDEFINITE).setBehavior(object : BaseTransientBottomBar.Behavior(){
                override fun canSwipeDismissView(child: View): Boolean {
                    return false
                }
            }).show()
        }
    }

}
/*private val navListener = BottomNavigationView.OnNavigationItemSelectedListener { item ->

        var selectedFragment: Fragment
        when (item.itemId) {
           R.id.action_events -> {
                selectedFragment = EventsFragment()
                supportFragmentManager.beginTransaction().replace(R.id.container, selectedFragment).commit()
               Toast.makeText(this,"events",Toast.LENGTH_LONG).show()
                return@OnNavigationItemSelectedListener true
            }
            R.id.action_food -> {
                selectedFragment = StallsFragment()
                supportFragmentManager.beginTransaction().replace(R.id.container, selectedFragment).addToBackStack(null).commit()
                Toast.makeText(this,"food",Toast.LENGTH_LONG).show()
                return@OnNavigationItemSelectedListener true
            }
            R.id.action_order_history -> {
                selectedFragment = OrdersFragment()
                supportFragmentManager.beginTransaction().replace(R.id.container, selectedFragment).addToBackStack(null).commit()
                Toast.makeText(this,"orderhistory",Toast.LENGTH_LONG).show()
                return@OnNavigationItemSelectedListener true
            }
            R.id.action_game -> {
                selectedFragment = EventsFragment()
                supportFragmentManager.beginTransaction().replace(R.id.container, selectedFragment).addToBackStack(null).commit()
                Toast.makeText(this,"game",Toast.LENGTH_LONG).show()
                return@OnNavigationItemSelectedListener true
            }
            R.id.action_more-> {
                selectedFragment = StallItemsFragment()
                supportFragmentManager.beginTransaction().replace(R.id.container, selectedFragment).addToBackStack(null).commit()
                Toast.makeText(this,"more",Toast.LENGTH_LONG).show()
                return@OnNavigationItemSelectedListener true
            }
        }
        false


    }*/
/*R.id.action_profile -> {

            // Open Profile Fragment with hidden bottom nav and toolbar
            Toast.makeText(this,"Profile Fragment",Toast.LENGTH_LONG).show()
            true
        }

        R.id.action_cart -> {

            // Open Cart Fragment with hidden bottom nav and toolbar
            CartFragment().show(supportFragmentManager, "CartFragment")
            Toast.makeText(this,"Cart Fragment",Toast.LENGTH_LONG).show()
            true
        }

        R.id.action_notifications -> {

            // Open Notifications Fragment with hidden bottom nav and toolbar
            Toast.makeText(this,"Notifications Fragment",Toast.LENGTH_LONG).show()
            true
        }

        else -> {
            // If we got here, the user's action was not recognized.
            // Invoke the superclass to handle it.
            super.onOptionsItemSelected(item)
        }
    }*/

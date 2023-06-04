package com.sverreskort.tackingassist

import android.Manifest
import android.animation.ObjectAnimator
import android.content.*
import android.content.pm.PackageManager
import android.location.Location
import android.net.Uri
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.os.IBinder
import android.provider.Settings
import android.util.Log
import android.view.View
import android.widget.Button
import android.widget.ImageView
import android.widget.TextView
import androidx.appcompat.app.AppCompatDelegate
import androidx.core.app.ActivityCompat
import androidx.localbroadcastmanager.content.LocalBroadcastManager
import com.google.android.material.snackbar.Snackbar
import com.sverreskort.android.tackingassist.*
import com.sverreskort.android.tackingassist.SharedPreferenceUtil
import java.text.SimpleDateFormat
import java.util.*

//private const val TAG = "MainActivity"
private const val REQUEST_FOREGROUND_ONLY_PERMISSIONS_REQUEST_CODE = 34

/**
 *  This app xxx
 */
class MainActivity : AppCompatActivity(), SharedPreferences.OnSharedPreferenceChangeListener {
    private var foregroundOnlyLocationServiceBound = false
    // Provides location updates for while-in-use feature.
    private var foregroundOnlyLocationService: ForegroundOnlyLocationService? = null
    // Listens for location broadcasts from ForegroundOnlyLocationService.
    private lateinit var foregroundOnlyBroadcastReceiver: ForegroundOnlyBroadcastReceiver
    private lateinit var sharedPreferences: SharedPreferences

    //GUI-components
    private lateinit var startButton: Button
    private lateinit var compassImageView: ImageView
    private lateinit var boatImageView: ImageView
    private lateinit var windImageView: ImageView
    private lateinit var speedPlotView: lineGraphView

    private val TAG = "MainActivity"

    //Current dynamics.
    private var boatHeading = 0.0f
    private var boatSpeed : Double = 0.0
    private var windBearing = 0.0f
    private var speedBuffer = RingBuffer(120)

    // Monitors connection to the while-in-use service.
    private val foregroundOnlyServiceConnection = object : ServiceConnection {

        override fun onServiceConnected(name: ComponentName, service: IBinder) {
            val binder = service as ForegroundOnlyLocationService.LocalBinder
            foregroundOnlyLocationService = binder.service
            foregroundOnlyLocationServiceBound = true
        }

        override fun onServiceDisconnected(name: ComponentName) {
            foregroundOnlyLocationService = null
            foregroundOnlyLocationServiceBound = false
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        // Force dark mode
        AppCompatDelegate.setDefaultNightMode(AppCompatDelegate.MODE_NIGHT_YES)
        delegate.applyDayNight()

        supportActionBar?.hide()

        setContentView(R.layout.activity_main)

        foregroundOnlyBroadcastReceiver = ForegroundOnlyBroadcastReceiver()

        sharedPreferences =
            getSharedPreferences(getString(R.string.preference_file_key), Context.MODE_PRIVATE)

        // function run in debug to achieve drawable hash-marks to the compass
        //val pathData = drawHashMarks(200)

        // finding and updating textViewWindDir
        val textViewWindDir = findViewById(R.id.textViewWindDir) as TextView
        textViewWindDir.text = "000"
        val textViewStarbCL = findViewById(R.id.textViewStarbCL) as TextView
        textViewStarbCL.text = "315"
        val textViewPortCL = findViewById(R.id.textViewPortCL) as TextView
        textViewPortCL.text = "45"

        //Find ImageView
        boatImageView = findViewById(R.id.imageViewBoat)
        windImageView = findViewById(R.id.imageViewWind)
        compassImageView = findViewById(R.id.imageViewCompass)
        speedPlotView = findViewById(R.id.SpeedPlotView)

        // TODO Later: Draw compass programatic, to make it responsive. Look here: https://www.kodeco.com/142-android-custom-view-tutorial

        startButton = findViewById(R.id.buttonStart)
        startButton.setOnClickListener {
            val enabled = sharedPreferences.getBoolean(
                SharedPreferenceUtil.KEY_FOREGROUND_ENABLED, false)

            if (enabled) {
                foregroundOnlyLocationService?.unsubscribeToLocationUpdates()
            } else {

                // Checks and requests if needed.
                if (foregroundPermissionApproved()) {
                    foregroundOnlyLocationService?.subscribeToLocationUpdates()
                        ?: Log.d(TAG, "Service Not Bound")
                } else {
                    requestForegroundPermissions()
                }
            }
        }
        fun updateCompassImage(windBearingOld:Float, windBearingNew:Float){
            //updating WindDir-texts
            textViewWindDir.text = windBearingNew.toInt().toString()
            val starbCL = reduseDeg(windBearingNew - 45)
            textViewStarbCL.text = starbCL.toInt().toString()
            val portCL = reduseDeg(windBearingNew + 45)
            textViewPortCL.text = portCL.toInt().toString()

            //Rotate compassImage
            var fromDegress = 360-windBearingOld
            var toDegress = 360-windBearingNew
            if (fromDegress>315 &&  toDegress<45)
                fromDegress -= 360
            if (fromDegress<45 &&  toDegress>315)
                toDegress   -= 360
            val compassAnimator = ObjectAnimator.ofFloat(compassImageView, View.ROTATION, fromDegress, toDegress)
            compassAnimator.duration = 400
            compassAnimator.start()
        }
        // get reference to button and set on-click listener
        val btn_click_wind_minus : Button = findViewById(R.id.buttonWindMinus5)
        btn_click_wind_minus.setOnClickListener {
            // your code to perform when the user clicks on the button
            Log.d(TAG, "You clicked btn_click_wind_minus")
            val windBearingOld = windBearing
            windBearing = reduseDeg(windBearing - 5.0f)

            updateCompassImage(windBearingOld, windBearing)
        }
        // get reference to button and set on-click listener
        val btn_click_wind_plus : Button = findViewById(R.id.buttonWindPlus5)
        btn_click_wind_plus.setOnClickListener {
            // your code to perform when the user clicks on the button
            Log.d(TAG, "You clicked btn_click_wind_plus")
            val windBearingOld = windBearing
            windBearing = reduseDeg(windBearing + 5.0f)

            updateCompassImage(windBearingOld, windBearing)
        }
        // get reference to button and set on-click listener
        val btn_click_Starb_CL : Button = findViewById(R.id.buttonStarbCL)
        btn_click_Starb_CL.setOnClickListener {
            // your code to perform when the user clicks on the button
            Log.d(TAG, "You clicked btn_click_Starb_CL")
            val windBearingOld = windBearing
            windBearing = reduseDeg(boatHeading + 45.0f)

            updateCompassImage(windBearingOld, windBearing)
        }
        // get reference to button and set on-click listener
        val btn_click_Port_CL : Button = findViewById(R.id.buttonPortCL)
        btn_click_Port_CL.setOnClickListener {
            // your code to perform when the user clicks on the button
            Log.d(TAG, "You clicked btn_click_Starb_CL")
            val windBearingOld = windBearing
            windBearing = reduseDeg(boatHeading - 45.0f)

            updateCompassImage(windBearingOld, windBearing)
        }
    }

    override fun onStart() {
        super.onStart()

        updateButtonState(
            sharedPreferences.getBoolean(SharedPreferenceUtil.KEY_FOREGROUND_ENABLED, false)
        )
        sharedPreferences.registerOnSharedPreferenceChangeListener(this)

        val serviceIntent = Intent(this, ForegroundOnlyLocationService::class.java)
        bindService(serviceIntent, foregroundOnlyServiceConnection, Context.BIND_AUTO_CREATE)
    }

    override fun onResume() {
        super.onResume()
        LocalBroadcastManager.getInstance(this).registerReceiver(
            foregroundOnlyBroadcastReceiver,
            IntentFilter(
                ForegroundOnlyLocationService.ACTION_FOREGROUND_ONLY_LOCATION_BROADCAST)
        )
    }

    override fun onPause() {
        LocalBroadcastManager.getInstance(this).unregisterReceiver(
            foregroundOnlyBroadcastReceiver
        )
        super.onPause()
    }

    override fun onStop() {
        if (foregroundOnlyLocationServiceBound) {
            unbindService(foregroundOnlyServiceConnection)
            foregroundOnlyLocationServiceBound = false
        }
        sharedPreferences.unregisterOnSharedPreferenceChangeListener(this)

        super.onStop()
    }

    override fun onSharedPreferenceChanged(sharedPreferences: SharedPreferences, key: String) {
        // Updates button states if new while in use location is added to SharedPreferences.
        if (key == SharedPreferenceUtil.KEY_FOREGROUND_ENABLED) {
            updateButtonState(sharedPreferences.getBoolean(
                SharedPreferenceUtil.KEY_FOREGROUND_ENABLED, false)
            )
        }
    }

    // Method checks if permissions approved.
    private fun foregroundPermissionApproved(): Boolean {
        return PackageManager.PERMISSION_GRANTED == ActivityCompat.checkSelfPermission(
            this,
            Manifest.permission.ACCESS_FINE_LOCATION
        )
    }

    // Method requests permissions.
    private fun requestForegroundPermissions() {
        val provideRationale = foregroundPermissionApproved()

        // If the user denied a previous request, but didn't check "Don't ask again", provide
        // additional rationale.
        if (provideRationale) {
            Snackbar.make(
                findViewById(R.id.activity_main),
                R.string.permission_rationale,
                Snackbar.LENGTH_LONG
            )
                .setAction(R.string.ok) {
                    // Request permission
                    ActivityCompat.requestPermissions(
                        this@MainActivity,
                        arrayOf(Manifest.permission.ACCESS_FINE_LOCATION),
                        REQUEST_FOREGROUND_ONLY_PERMISSIONS_REQUEST_CODE
                    )
                }
                .show()
        } else {
            Log.d(TAG, "Request foreground only permission")
            ActivityCompat.requestPermissions(
                this@MainActivity,
                arrayOf(Manifest.permission.ACCESS_FINE_LOCATION),
                REQUEST_FOREGROUND_ONLY_PERMISSIONS_REQUEST_CODE
            )
        }
    }

    // Handles permission result.
    override fun onRequestPermissionsResult(
        requestCode: Int,
        permissions: Array<String>,
        grantResults: IntArray
    ) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults)
        Log.d(TAG, "onRequestPermissionResult")

        when (requestCode) {
            REQUEST_FOREGROUND_ONLY_PERMISSIONS_REQUEST_CODE -> when {
                grantResults.isEmpty() ->
                    // If user interaction was interrupted, the permission request
                    // is cancelled and you receive empty arrays.
                    Log.d(TAG, "User interaction was cancelled.")

                grantResults[0] == PackageManager.PERMISSION_GRANTED ->
                    // Permission was granted.
                    foregroundOnlyLocationService?.subscribeToLocationUpdates()

                else -> {
                    // Permission denied.
                    updateButtonState(false)

                    Snackbar.make(
                        findViewById(R.id.activity_main),
                        R.string.permission_denied_explanation,
                        Snackbar.LENGTH_LONG
                    )
                        .setAction(R.string.settings) {
                            // Build intent that displays the App settings screen.
                            val intent = Intent()
                            intent.action = Settings.ACTION_APPLICATION_DETAILS_SETTINGS
                            val uri = Uri.fromParts(
                                "package",
                                BuildConfig.APPLICATION_ID,
                                null
                            )
                            intent.data = uri
                            intent.flags = Intent.FLAG_ACTIVITY_NEW_TASK
                            startActivity(intent)
                        }
                        .show()
                }
            }
        }
    }

    private fun updateButtonState(trackingLocation: Boolean) {
        if (trackingLocation) {
            startButton.text = getString(R.string.stop_location_updates_button_text)
        } else {
            startButton.text = getString(R.string.start_location_updates_button_text)
        }
    }


    /**
     * Receiver for location broadcasts from [ForegroundOnlyLocationService].
     */
    private inner class ForegroundOnlyBroadcastReceiver : BroadcastReceiver() {

        // here is the central action when a location is received
        override fun onReceive(context: Context, intent: Intent) {
            val location = intent.getParcelableExtra<Location>(
                ForegroundOnlyLocationService.EXTRA_LOCATION
            )

            if (location != null) {
                val boatHeadingOld = boatHeading
                boatHeading = location.bearing
                boatSpeed = location.speed * 1.9438452

                val date = Date(location.getTime())
                val simpleDateFormat = SimpleDateFormat("HH:mm:ss", Locale.getDefault() )
                val utc = simpleDateFormat.format(date.time)

                // finding and updating textViewClock
                val textViewClock = findViewById(R.id.textViewClock) as TextView
                textViewClock.text = String.format("UTC = %s", utc.toString())
                // finding and updating textViewSpeed
                val textViewSpeed = findViewById(R.id.textViewSpeed) as TextView
                textViewSpeed.text = String.format("%.1f kn", boatSpeed)
                // finding and updating textViewBearing
                val textViewHeading = findViewById(R.id.textViewHeading) as TextView
                textViewHeading.text = boatHeading.toInt().toString()

                //Rotate boatImage
                var fromDegress = boatHeadingOld-windBearing
                var toDegress = boatHeading-windBearing
                if (fromDegress>315 &&  toDegress<45)
                    fromDegress -= 360
                if (fromDegress<45 &&  toDegress>315)
                    toDegress -=  360
                val boatAnimator = ObjectAnimator.ofFloat(boatImageView, View.ROTATION, fromDegress, toDegress)
                boatAnimator.duration = 1000
                boatAnimator.start()

                val gpsNow = gpsDynamics(location.getTime(), boatSpeed.toFloat(),0f, boatHeading)
                speedBuffer.push(gpsNow)
                speedPlotView.importData(speedBuffer)
                speedPlotView.invalidate()
            }
        }



    }
}
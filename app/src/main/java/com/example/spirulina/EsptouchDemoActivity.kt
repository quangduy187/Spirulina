package com.example.spirulina

//import android.support.v7.app.AlertDialog
//import android.support.v7.app.AppCompatActivity

import android.Manifest
import android.app.ProgressDialog
import android.content.*
import android.content.pm.PackageManager
import android.location.LocationManager
import android.net.wifi.WifiInfo
import android.net.wifi.WifiManager
import android.os.AsyncTask
import android.os.Build
import android.os.Bundle
import android.util.Log
import android.view.Menu
import android.view.MenuItem
import android.view.View
import android.view.View.OnClickListener
import android.widget.*
import androidx.annotation.NonNull
import androidx.annotation.RequiresApi
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import com.espressif.iot.esptouch.EsptouchTask
import com.espressif.iot.esptouch.IEsptouchListener
import com.espressif.iot.esptouch.IEsptouchResult
import com.espressif.iot.esptouch.IEsptouchTask
import com.espressif.iot.esptouch.task.__IEsptouchTask
import com.espressif.iot.esptouch.util.ByteUtil
import com.espressif.iot.esptouch.util.TouchNetUtil
import kotlinx.android.synthetic.main.esptouch_demo_activity.*
import java.lang.ref.WeakReference
import java.util.*

class EsptouchDemoActivity : AppCompatActivity(), OnClickListener {

    private var mApSsidTV: TextView? = null
    private var mApBssidTV: TextView? = null
    private var mApPasswordET: EditText? = null
    private var mDeviceCountET: EditText? = null
    private var mPackageModeGroup: RadioGroup? = null
    private var mMessageTV: TextView? = null
    private var mConfirmBtn: Button? = null

    private val myListener = IEsptouchListener { result -> onEsptoucResultAddedPerform(result) }

    private var mTask: EsptouchAsyncTask4? = null

    private var mReceiverRegistered = false
    private val mReceiver = object : BroadcastReceiver() {
        override fun onReceive(context: Context, intent: Intent) {
            val action = intent.action ?: return

            val wifiManager = context.applicationContext
                .getSystemService(WIFI_SERVICE) as WifiManager

            when (action) {
                WifiManager.NETWORK_STATE_CHANGED_ACTION -> {
                    val wifiInfo: WifiInfo
                    if (intent.hasExtra(WifiManager.EXTRA_WIFI_INFO)) {
                        wifiInfo = intent.getParcelableExtra(WifiManager.EXTRA_WIFI_INFO)
                    } else {
                        wifiInfo = wifiManager.connectionInfo
                    }
                    onWifiChanged(wifiInfo)
                }
                LocationManager.PROVIDERS_CHANGED_ACTION -> onWifiChanged(wifiManager.connectionInfo)
            }
        }
    }

    private var mDestroyed = false

    private val isSDKAtLeastP: Boolean
        get() = Build.VERSION.SDK_INT >= 28

    @RequiresApi(Build.VERSION_CODES.M)
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.esptouch_demo_activity)

        mApSsidTV = findViewById(R.id.ap_ssid_text)
        mApBssidTV = findViewById(R.id.ap_bssid_text)
        mApPasswordET = findViewById(R.id.ap_password_edit)
        mDeviceCountET = findViewById(R.id.device_count_edit)
        mDeviceCountET!!.setText("1")
        mPackageModeGroup = findViewById(R.id.package_mode_group)
        mMessageTV = findViewById(R.id.message)
        mConfirmBtn = findViewById(R.id.confirm_btn)
        mConfirmBtn!!.isEnabled = false
        mConfirmBtn!!.setOnClickListener(this)

        setSupportActionBar(tbEspTouch)
        supportActionBar!!.setDisplayHomeAsUpEnabled(true)
        supportActionBar!!.title = "Smart Config"

        if (isSDKAtLeastP) {
            if (checkSelfPermission(Manifest.permission.ACCESS_COARSE_LOCATION) !== PackageManager.PERMISSION_GRANTED) {
                val permissions = arrayOf(Manifest.permission.ACCESS_COARSE_LOCATION)

                requestPermissions(permissions, REQUEST_PERMISSION)
            } else {
                registerBroadcastReceiver()
            }

        } else {
            registerBroadcastReceiver()
        }
    }

    override fun onRequestPermissionsResult(requestCode: Int, @NonNull permissions: Array<String>, @NonNull grantResults: IntArray) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults)

        when (requestCode) {
            REQUEST_PERMISSION -> if (grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                if (!mDestroyed) {
                    registerBroadcastReceiver()
                }
            }
        }
    }

    override fun onDestroy() {
        super.onDestroy()

        mDestroyed = true
        if (mReceiverRegistered) {
            unregisterReceiver(mReceiver)
        }
    }

    override fun onCreateOptionsMenu(menu: Menu): Boolean {
        menu.add(Menu.NONE, MENU_ITEM_ABOUT, 0, R.string.menu_item_about)
            //.setIcon(R.drawable.ic_info_outline_white_24dp)
            .setShowAsAction(MenuItem.SHOW_AS_ACTION_ALWAYS)
        return super.onCreateOptionsMenu(menu)
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        when (item.itemId) {
            MENU_ITEM_ABOUT -> {
                showAboutDialog()
                return true
            }
        }

        return super.onOptionsItemSelected(item)
    }

    private fun showAboutDialog() {
        val esptouchVer = IEsptouchTask.ESPTOUCH_VERSION
        var appVer = ""
        val packageManager = getPackageManager()
        try {
            val info = packageManager.getPackageInfo(getPackageName(), 0)
            appVer = info.versionName
        } catch (e: PackageManager.NameNotFoundException) {
            e.printStackTrace()
        }

        val items = arrayOf<CharSequence>(
            getString(R.string.about_app_version, appVer),
            getString(R.string.about_esptouch_version, esptouchVer)
        )
        AlertDialog.Builder(this)
            .setTitle(R.string.menu_item_about)
            //.setIcon(R.drawable.ic_info_outline_black_24dp)
            .setItems(items, null)
            .show()
    }

    private fun registerBroadcastReceiver() {
        val filter = IntentFilter(WifiManager.NETWORK_STATE_CHANGED_ACTION)
        if (isSDKAtLeastP) {
            filter.addAction(LocationManager.PROVIDERS_CHANGED_ACTION)
        }
        registerReceiver(mReceiver, filter)
        mReceiverRegistered = true
    }

    private fun onWifiChanged(info: WifiInfo?) {
        val disconnected = (info == null
                || info.networkId == -1
                || "<unknown ssid>" == info.ssid)
        if (disconnected) {
            mApSsidTV!!.text = ""
            mApSsidTV!!.tag = null
            mApBssidTV!!.text = ""
            mMessageTV!!.setText(R.string.no_wifi_connection)
            mConfirmBtn!!.isEnabled = false

            if (isSDKAtLeastP) {
                checkLocation()
            }

            if (mTask != null) {
                mTask!!.cancelEsptouch()
                mTask = null
                AlertDialog.Builder(this@EsptouchDemoActivity)
                    .setMessage(R.string.configure_wifi_change_message)
                    .setNegativeButton(android.R.string.cancel, null)
                    .show()
            }
        } else {
            var ssid = info!!.ssid
            if (ssid.startsWith("\"") && ssid.endsWith("\"")) {
                ssid = ssid.substring(1, ssid.length - 1)
            }
            mApSsidTV!!.text = ssid
            mApSsidTV!!.tag = ByteUtil.getBytesByString(ssid)
            val ssidOriginalData = TouchNetUtil.getOriginalSsidBytes(info)
            mApSsidTV!!.tag = ssidOriginalData

            val bssid = info.bssid
            mApBssidTV!!.text = bssid

            mConfirmBtn!!.isEnabled = true
            mMessageTV!!.text = ""
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
                val frequency = info.frequency
                if (frequency > 4900 && frequency < 5900) {
                    // Connected 5G wifi. Device does not support 5G
                    mMessageTV!!.setText(R.string.wifi_5g_message)
                }
            }
        }
    }

    private fun checkLocation() {
        val enable: Boolean
        val locationManager = getSystemService(Context.LOCATION_SERVICE) as LocationManager
        val locationGPS = locationManager.isProviderEnabled(LocationManager.GPS_PROVIDER)
        val locationNetwork = locationManager.isProviderEnabled(LocationManager.NETWORK_PROVIDER)
        enable = locationGPS || locationNetwork

        if (!enable) {
            mMessageTV!!.setText(R.string.location_disable_message)
        }
    }

    override fun onClick(v: View) {
        if (v === mConfirmBtn) {
            val ssid = if (mApSsidTV!!.tag == null)
                ByteUtil.getBytesByString(mApSsidTV!!.text.toString())
            else
                mApSsidTV!!.tag as ByteArray
            val password = ByteUtil.getBytesByString(mApPasswordET!!.text.toString())
            val bssid = TouchNetUtil.parseBssid2bytes(mApBssidTV!!.text.toString())
            val deviceCount = mDeviceCountET!!.text.toString().toByteArray()
            val broadcast = byteArrayOf(
                (if (mPackageModeGroup!!.checkedRadioButtonId == R.id.package_broadcast)
                    1
                else
                    0).toByte()
            )

            if (mTask != null) {
                mTask!!.cancelEsptouch()
            }
            mTask = EsptouchAsyncTask4(this)
            mTask!!.execute(ssid, bssid, password, deviceCount, broadcast)
        }
    }

    private fun onEsptoucResultAddedPerform(result: IEsptouchResult) {
        runOnUiThread {
            val text = result.bssid + " is connected to the wifi"
            Toast.makeText(
                this@EsptouchDemoActivity, text,
                Toast.LENGTH_SHORT
            ).show()
        }
    }

    private class EsptouchAsyncTask4 internal constructor(activity: EsptouchDemoActivity) :
        AsyncTask<ByteArray, Void, List<IEsptouchResult>>() {
        private val mActivity: WeakReference<EsptouchDemoActivity>

        // without the lock, if the user tap confirm and cancel quickly enough,
        // the bug will arise. the reason is follows:
        // 0. task is starting created, but not finished
        // 1. the task is cancel for the task hasn't been created, it do nothing
        // 2. task is created
        // 3. Oops, the task should be cancelled, but it is running
        private val mLock = Any()
        private var mProgressDialog: ProgressDialog? = null
        private var mResultDialog: AlertDialog? = null
        private var mEsptouchTask: IEsptouchTask? = null

        init {
            mActivity = WeakReference(activity)
        }

        internal fun cancelEsptouch() {
            cancel(true)
            if (mProgressDialog != null) {
                mProgressDialog!!.dismiss()
            }
            if (mResultDialog != null) {
                mResultDialog!!.dismiss()
            }
            if (mEsptouchTask != null) {
                mEsptouchTask!!.interrupt()
            }
        }

        override fun onPreExecute() {
            val activity = mActivity.get()
            mProgressDialog = ProgressDialog(activity)
            mProgressDialog!!.setMessage(activity!!.getString(R.string.configuring_message))
            mProgressDialog!!.setCanceledOnTouchOutside(false)
            mProgressDialog!!.setOnCancelListener {
                synchronized(mLock) {
                    if (__IEsptouchTask.DEBUG) {
                        Log.i(TAG, "progress dialog back pressed canceled")
                    }
                    if (mEsptouchTask != null) {
                        mEsptouchTask!!.interrupt()
                    }
                }
            }
            mProgressDialog!!.setButton(
                DialogInterface.BUTTON_NEGATIVE, activity.getText(android.R.string.cancel)
            ) { _, which ->
                synchronized(mLock) {
                    Log.i(TAG, "progress dialog cancel button canceled")
                    if (mEsptouchTask != null) {
                        mEsptouchTask!!.interrupt()
                    }
                }
            }
            mProgressDialog!!.show()
        }

        override fun doInBackground(vararg params: ByteArray): List<IEsptouchResult> {
            val activity = mActivity.get()
            val taskResultCount: Int
            synchronized(mLock) {
                val apSsid = params[0]
                val apBssid = params[1]
                val apPassword = params[2]
                val deviceCountData = params[3]
                val broadcastData = params[4]
                taskResultCount = if (deviceCountData.size == 0) -1 else Integer.parseInt(String(deviceCountData))
                val context = activity!!.getApplicationContext()
                mEsptouchTask = EsptouchTask(apSsid, apBssid, apPassword, context)
                mEsptouchTask!!.setPackageBroadcast(broadcastData[0].toInt() == 1)
                mEsptouchTask!!.setEsptouchListener(activity.myListener)
            }
            return mEsptouchTask!!.executeForResults(taskResultCount)
        }

        override fun onPostExecute(result: List<IEsptouchResult>?) {
            val activity = mActivity.get()
            mProgressDialog!!.dismiss()
            if (result == null) {
                mResultDialog = AlertDialog.Builder(activity!!)
                    .setMessage(R.string.configure_result_failed_port)
                    .setPositiveButton(android.R.string.ok, null)
                    .show()
                mResultDialog!!.setCanceledOnTouchOutside(false)
                return
            }

            val firstResult = result[0]
            // check whether the task is cancelled and no results received
            if (!firstResult.isCancelled) {
                // the task received some results including cancelled while
                // executing before receiving enough results
                if (firstResult.isSuc) {
                    val resultMsgList = ArrayList<CharSequence>(result.size)
                    for (touchResult in result) {
                        val message = activity!!.getString(
                            R.string.configure_result_success_item,
                            touchResult.bssid, touchResult.inetAddress.hostAddress
                        )
                        resultMsgList.add(message)
                    }

                    val items = arrayOfNulls<CharSequence>(resultMsgList.size)
                    mResultDialog = AlertDialog.Builder(activity!!)
                        .setTitle(R.string.configure_result_success)
                        .setItems(resultMsgList.toTypedArray(), null)
                        .setPositiveButton(android.R.string.ok, null)
                        .show()
                    mResultDialog!!.setCanceledOnTouchOutside(false)
                } else {
                    mResultDialog = AlertDialog.Builder(activity!!)
                        .setMessage(R.string.configure_result_failed)
                        .setPositiveButton(android.R.string.ok, null)
                        .show()
                    mResultDialog!!.setCanceledOnTouchOutside(false)
                }
            }

            activity!!.mTask = null
        }
    }

    companion object {
        private val TAG = "EsptouchDemoActivity"

        private val REQUEST_PERMISSION = 0x01

        private val MENU_ITEM_ABOUT = 0
    }
}

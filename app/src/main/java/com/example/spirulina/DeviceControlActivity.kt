package com.example.spirulina

import android.app.Dialog
import android.bluetooth.BluetoothAdapter
import android.bluetooth.BluetoothDevice
import android.bluetooth.BluetoothSocket
import android.content.Context
import android.content.Intent
import android.content.SharedPreferences
import android.net.ConnectivityManager
import android.os.Build
import android.os.Bundle
import android.os.VibrationEffect
import android.os.Vibrator
import android.text.TextUtils
import android.util.Log
import android.view.Menu
import android.view.MenuItem
import android.widget.Button
import android.widget.EditText
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import com.example.spirulina.Config.Config
import kotlinx.android.synthetic.main.activity_device_control.*
import org.eclipse.paho.android.service.MqttAndroidClient
import org.eclipse.paho.client.mqttv3.*
import java.io.IOException
import java.io.OutputStream
import java.io.UnsupportedEncodingException
import java.util.*


class DeviceControlActivity : AppCompatActivity() {

    private val serverUri = "tcp://broker.mqttdashboard.com"
    //    private val serverUri = "tcp://tailor.cloudmqtt.com:12236"
    private val USER = "soirjdac"
    private val PASSWORD = "sCGtXJrBihVc"
    private val clientId = "bkstar_mandevices_v1_0"
    private val subscriptionTopic = "actuator/+"
    lateinit var client: MqttAndroidClient

    private var bluetoothAdapter: BluetoothAdapter? = null
    //    private val DEVICE_ADDRESS = "00:21:13:05:A0:2D"  //địa chỉ của module HC-05
    private val DEVICE_ADDRESS = "98:D3:91:FD:A3:B5"  //địa chỉ của module HC-05
    private val PORT_UUID = UUID.fromString("00001101-0000-1000-8000-00805f9b34fb")
    private var device: BluetoothDevice? = null
    private var socket: BluetoothSocket? = null
    private var outputStream: OutputStream? = null
    private var command: String? = null
    private var blueState = false
    private var connect: Boolean = false
    private lateinit var iconBluetooth: MenuItem

    lateinit var mSharedPreferences: SharedPreferences
    private var mode: Int = 0
    private var statePump = false
    private var stateMist = false
    private var stateBlind = false
    private var stateSoda = false
    private var stateHarvest = false
    private var stateLed = false

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_device_control)
        setSupportActionBar(tbControl)
        supportActionBar!!.setDisplayHomeAsUpEnabled(true)

        //Lấy thông tin từ Selection Farm
        mSharedPreferences = getSharedPreferences(Config.SHARE_CODE, Context.MODE_PRIVATE)
        val name = mSharedPreferences.getString(Config.FARM_NAME, "")
        val id = mSharedPreferences.getString(Config.FARM_ID, "")
//        Toast.makeText(this, "$id  $name", Toast.LENGTH_LONG).show()
        supportActionBar!!.title = "$id $name"

        val connManager = getSystemService(Context.CONNECTIVITY_SERVICE) as ConnectivityManager
        val mWifi = connManager!!.getNetworkInfo(ConnectivityManager.TYPE_WIFI)

        if (mWifi.isConnected) {
            txtWiFiStatus.text = "Connected"
            txtWiFiStatus.setTextColor(resources.getColor(R.color.green))
        } else {
            txtWiFiStatus.text = "Disconnected"
            txtWiFiStatus.setTextColor(resources.getColor(R.color.red_error))
        }

        //Khởi tạo kết nối MQTT
        startMqtt()

        enableAllViews(false)

        //Floating button
        fabTao_ctrl.setOnClickListener {
            val intent = Intent(this@DeviceControlActivity, MainActivity::class.java)
            startActivity(intent)
        }
        fabControl_ctrl.setOnClickListener {
            fabmenu_ctrl.close(true)
        }
        fabEnvironment_ctrl.setOnClickListener {
            val intent = Intent(this@DeviceControlActivity, EnvironmentActivity::class.java)
            startActivity(intent)
        }

        //Bắt các sự kiện điều khiển
        btnAuto.setOnClickListener {
            mode = 1 //Chế độ tự động
            iconBluetooth.isVisible = false
            btnAuto.setBackgroundColor(resources.getColor(R.color.light_blue))
            btnManual.setBackgroundColor(resources.getColor(R.color.white))
            btnApp.setBackgroundColor(resources.getColor(R.color.white))
            btnBluetooth.setBackgroundColor(resources.getColor(R.color.white))
            publishMqttMessage("bkstar/actuator_mode/", "auto", false)
            enableAllViews(false)
        }
        btnManual.setOnClickListener {
            mode = 0 //Chế độ bằng tay với nút bấm
            iconBluetooth.isVisible = false
            btnManual.setBackgroundColor(resources.getColor(R.color.light_blue))
            btnAuto.setBackgroundColor(resources.getColor(R.color.white))
            btnApp.setBackgroundColor(resources.getColor(R.color.white))
            btnBluetooth.setBackgroundColor(resources.getColor(R.color.white))
            publishMqttMessage("bkstar/actuator_mode/", "manual", false)
            enableAllViews(false)
        }

        btnApp.setOnClickListener {
            mode = 2
            iconBluetooth.isVisible = false
            btnApp.setBackgroundColor(resources.getColor(R.color.light_blue))
            btnAuto.setBackgroundColor(resources.getColor(R.color.white))
            btnManual.setBackgroundColor(resources.getColor(R.color.white))
            btnBluetooth.setBackgroundColor(resources.getColor(R.color.white))
            publishMqttMessage("bkstar/actuator_mode/", "app", false)
            enableAllViews(true)
        }

        btnBluetooth.setOnClickListener {
            mode = 3
            iconBluetooth.isVisible = true
            btnBluetooth.setBackgroundColor(resources.getColor(R.color.light_blue))
            btnApp.setBackgroundColor(resources.getColor(R.color.white))
            btnAuto.setBackgroundColor(resources.getColor(R.color.white))
            btnManual.setBackgroundColor(resources.getColor(R.color.white))
            enableAllViews(true)
            // Kết nối Bluetooth
            if (!connect) {
                bluetoothAdapter = BluetoothAdapter.getDefaultAdapter()
                if (bluetoothAdapter == null)
                //Checks if the device supports bluetooth
                {
                    Toast.makeText(
                        applicationContext,
                        "Device doesn't support bluetooth",
                        Toast.LENGTH_SHORT
                    ).show()
                }
                if (!bluetoothAdapter!!.isEnabled)
                //Checks if bluetooth is enabled. If not, the program will ask permission from the user to enable it
                {
                    val enableAdapter = Intent(BluetoothAdapter.ACTION_REQUEST_ENABLE)
                    startActivityForResult(enableAdapter, 0)
                    try {
                        Thread.sleep(1000)
                    } catch (e: InterruptedException) {
                        e.printStackTrace()
                    }
                }
                val bondedDevices = bluetoothAdapter!!.bondedDevices

                if (bondedDevices.isEmpty())
                //Checks for paired bluetooth devices
                {
                    Toast.makeText(
                        applicationContext,
                        "Please pair the device first",
                        Toast.LENGTH_SHORT
                    ).show()
                } else {
                    for (iterator in bondedDevices) {
                        if (iterator.address == DEVICE_ADDRESS) {
                            device = iterator
                            break
                        }
                    }
                }
            }
        }

        //Bắt các sự kiện điều khiển
        imgPump.setOnClickListener {
            statePump = !statePump
            if (statePump) {
                imgPump.setImageResource(R.drawable.ic_pump_on)
                when (mode) {
                    2 -> publishMqttMessage("bkstar/actuator_1/", "pump_on", false)
                    3 -> send_command("x")
                }
                vibratePhone(500)
            } else {
                imgPump.setImageResource(R.drawable.ic_pump)
                when (mode) {
                    2 -> publishMqttMessage("bkstar/actuator_1/", "pump_off", false)
                    3 -> send_command("X")
                }
                vibratePhone(500)
            }
        }
        imgLed.setOnClickListener {
            stateLed = !stateLed
            if (stateLed) {
                imgLed.setImageResource(R.drawable.ic_diode_on)
                when (mode) {
                    2 -> publishMqttMessage("bkstar/actuator_6/", "led_on",false)
                    3 -> send_command("y")
                }
                vibratePhone(500)
            } else {
                imgLed.setImageResource(R.drawable.ic_diode_off)
                when (mode) {
                    2 -> publishMqttMessage("bkstar/actuator_6/", "led_off", false)
                    3 -> send_command("Y")
                }
                vibratePhone(500)
            }
        }
        imgMist.setOnClickListener {
            stateMist = !stateMist
            if (stateMist) {
                imgMist.setImageResource(R.drawable.ic_fog_on)
                when (mode) {
                    2 -> publishMqttMessage("bkstar/actuator_2/", "mist_on", false)
                    3 -> send_command("z")
                }
                vibratePhone(500)
            } else {
                imgMist.setImageResource(R.drawable.ic_fog)
                when (mode) {
                    2 -> publishMqttMessage("bkstar/actuator_2/", "mist_off", false)
                    3 -> send_command("Z")
                }
                vibratePhone(500)
            }
        }
        imgBlind.setOnClickListener {
            stateBlind = !stateBlind
            if (stateBlind) {
                imgBlind.setImageResource(R.drawable.ic_window_on)
                when (mode) {
                    2 -> publishMqttMessage("bkstar/actuator_3/", "blind_down", false)
                    3 -> send_command("a")
                }
                vibratePhone(500)

            } else {
                imgBlind.setImageResource(R.drawable.ic_window)
                when (mode) {
                    2 -> publishMqttMessage("bkstar/actuator_3/", "blind_up", false)
                    3 -> send_command("A")
                }
                vibratePhone(500)

            }
        }
        imgSoda.setOnClickListener {
            stateSoda = !stateSoda
            if (stateSoda) {
                imgSoda.setImageResource(R.drawable.ic_urn_on)
                when (mode) {
                    2 -> publishMqttMessage("bkstar/actuator_4/", "soda_on", false)
                    3 -> send_command("b")
                }
                vibratePhone(500)

            } else {
                imgSoda.setImageResource(R.drawable.ic_urn)
                when (mode) {
                    2 -> publishMqttMessage("bkstar/actuator_4/", "soda_off", false)
                    3 -> send_command("B")
                }
                vibratePhone(500)
            }
        }
        imgHarvest.setOnClickListener {
            stateHarvest = !stateHarvest
            if (stateHarvest) {
                imgHarvest.setImageResource(R.drawable.ic_heater_on)
                when (mode) {
                    2 -> publishMqttMessage("bkstar/actuator_5/", "harvest_on", false)
                    3 -> send_command("c")
                }
                vibratePhone(500)
            } else {
                imgHarvest.setImageResource(R.drawable.ic_hearter_off)
                when (mode) {
                    2 -> publishMqttMessage("bkstar/actuator_5/", "harvest_off", false)
                    3 -> send_command("C")
                }
                vibratePhone(500)
            }
        }
    }


    private fun btConnect(): Boolean {
        var connected = true
        try {
            socket =
                device!!.createRfcommSocketToServiceRecord(PORT_UUID) //Creates a socket to handle the outgoing connection
            socket!!.connect()
            Toast.makeText(applicationContext, "Connect successfully", Toast.LENGTH_LONG).show()
        } catch (e: Exception) {
            e.printStackTrace()
            connected = false
        }
        if (connected) {
            try {
                outputStream = socket!!.outputStream //gets the output stream of the socket
            } catch (e: IOException) {
                e.printStackTrace()
            }
        }
        return connected
    }

    private fun vibratePhone(ms: Long) {
        val vibrator = getSystemService(Context.VIBRATOR_SERVICE) as Vibrator
        if (Build.VERSION.SDK_INT >= 26) {
            vibrator.vibrate(VibrationEffect.createOneShot(200, VibrationEffect.DEFAULT_AMPLITUDE))
        } else {
            vibrator.vibrate(ms)
        }
    }

    private fun startMqtt() {
        val clientId = MqttClient.generateClientId()
        client = MqttAndroidClient(applicationContext, serverUri, clientId)
        val options = MqttConnectOptions()
        options.isAutomaticReconnect = true
        options.keepAliveInterval = 300         // tăng thời gian keepAlive để duy trì kết nối
        options.connectionTimeout = 240000
        options.userName = USER
        options.password = PASSWORD.toCharArray()
        try {
            val token: IMqttToken = client.connect(options)
            token.actionCallback = object : MqttCallbackExtended, IMqttActionListener {
                override fun onSuccess(asyncActionToken: IMqttToken?) {
                    Log.d("debug", "onSuccess")
                    Toast.makeText(
                        this@DeviceControlActivity,
                        "Kết nối thành công",
                        Toast.LENGTH_SHORT
                    ).show()
                }

                override fun onFailure(asyncActionToken: IMqttToken?, exception: Throwable?) {
                    Log.d("debug", "onFailure:  ${exception.toString()}")
                    Toast.makeText(
                        this@DeviceControlActivity,
                        "Kết nối thất bại",
                        Toast.LENGTH_SHORT
                    ).show()
                }

                override fun connectComplete(reconnect: Boolean, serverURI: String?) {
                    if (reconnect) {
                        Toast.makeText(
                            this@DeviceControlActivity,
                            "Reconnected to : $serverURI",
                            Toast.LENGTH_SHORT
                        ).show()
                        // Because Clean Session is true, we need to re-subscribe
                        //subscribeToTopic();
                    } else {
                        Toast.makeText(
                            this@DeviceControlActivity,
                            "Connected to : $serverURI",
                            Toast.LENGTH_SHORT
                        ).show()
                    }

                }

                override fun messageArrived(topic: String?, message: MqttMessage?) {
                    TODO("not implemented") //To change body of created functions use File | Settings | File Templates.
                }

                override fun connectionLost(cause: Throwable?) {
                    Toast.makeText(
                        this@DeviceControlActivity,
                        "Lost Connection",
                        Toast.LENGTH_SHORT
                    ).show()
                }

                override fun deliveryComplete(token: IMqttDeliveryToken?) {
                    TODO("not implemented") //To change body of created functions use File | Settings | File Templates.
                }

            }
        } catch (e: MqttException) {
            Log.d("debug", e.message)
        }
    }

    private fun publishMqttMessage(topic: String, payload: String, retain:Boolean) {
        //"actuator/+pump"
        val encodedPayload: ByteArray
        try {
            encodedPayload = payload.toByteArray()
            client.publish(topic, encodedPayload, 1, retain)
            Log.d("debug", "publish to actuator/+pump-on")
        } catch (e: UnsupportedEncodingException) {
            e.printStackTrace()
            Log.d("debug", e.message)
        }
    }

    private fun enableAllViews(state: Boolean) {
        imgHarvest.isClickable = state
        imgHarvest.isEnabled = state
        imgSoda.isClickable = state
        imgSoda.isEnabled = state
        imgMist.isEnabled = state
        imgMist.isClickable = state
        imgPump.isEnabled = state
        imgPump.isClickable = state
        imgBlind.isEnabled = state
        imgBlind.isClickable = state
        if (!state) {
            statePump = false
            stateMist = false
            stateBlind = false
            stateSoda = false
            stateHarvest = false
            imgHarvest.setImageResource(R.drawable.ic_harvest_off)
            imgSoda.setImageResource(R.drawable.ic_urn)
            imgBlind.setImageResource(R.drawable.ic_window)
            imgMist.setImageResource(R.drawable.ic_fog)
            imgPump.setImageResource(R.drawable.ic_pump)
        }
    }

    private fun send_command(ch: String?) {
        command = ch
        if (connect) {
            try {
                outputStream!!.write(command!!.toByteArray()) //transmits the value of command to the bluetooth module
            } catch (e: IOException) {
                e.printStackTrace()
            }
        } else {
            Toast.makeText(this, "Connect Bluetooth First!", Toast.LENGTH_SHORT).show()
        }
    }

    override fun onCreateOptionsMenu(menu: Menu?): Boolean {
        menuInflater.inflate(R.menu.mqtt_menu, menu)
        iconBluetooth = menu!!.findItem(R.id.itBluetooth)
        return true
    }

    override fun onOptionsItemSelected(item: MenuItem?): Boolean {
        when (item!!.itemId) {
            R.id.itMQTT -> {
                val intent = Intent(this@DeviceControlActivity, BluetoothSetupActivity::class.java)
                startActivity(intent)
            }

            R.id.itStart -> {
                val dialog = Dialog(this@DeviceControlActivity)
                dialog.setContentView(R.layout.dialog_update_time)

                val edtDay = dialog.findViewById<EditText>(R.id.edtRemainDay)
                val btnOK = dialog.findViewById<Button>(R.id.btnDialogOK)
                val btnCancel = dialog.findViewById<Button>(R.id.btnDialogCancel)

                btnOK.setOnClickListener {
                    val day: String = edtDay.text.toString()
                    if (!TextUtils.isEmpty(day)) {
                        publishMqttMessage("bkstar/harvest_day/", day, false)
                        dialog.dismiss()
                    } else {
                        Toast.makeText(
                            this@DeviceControlActivity,
                            "Chưa nhập dữ liệu!",
                            Toast.LENGTH_SHORT
                        ).show()
                    }
                }

                btnCancel.setOnClickListener {
                    dialog.dismiss()
                }
                dialog.show()
            }
            R.id.itBluetooth -> {
                //Kết nối với bluetooth
                if (!connect) {
                    connect = btConnect()
                    if (connect) {
                        blueState = true
                        item.setIcon(R.drawable.ic_bluetooth)
                        txtBlueStatus.text = "Connected"
                        txtBlueStatus.setTextColor(resources.getColor(R.color.green))
                        Toast.makeText(
                            applicationContext,
                            "Bluetooth Connected",
                            Toast.LENGTH_SHORT
                        ).show()
                    } else {
                        blueState = false
                        txtBlueStatus.text = "Disconnected"
                        txtBlueStatus.setTextColor(resources.getColor(R.color.red_error))
                        item.setIcon(R.drawable.ic_blueetooth_off)
                        Toast.makeText(applicationContext, "Try Again", Toast.LENGTH_SHORT).show()
                    }
                } else {
                    socket!!.close()
                    bluetoothAdapter!!.enable()
                    connect = false
                    blueState = false
                    txtBlueStatus.text = "Disconnected"
                    txtBlueStatus.setTextColor(resources.getColor(R.color.red_error))
                    item.setIcon(R.drawable.ic_blueetooth_off)
                    Toast.makeText(applicationContext, "Bluetooth Disconnected", Toast.LENGTH_SHORT)
                        .show()
                }
            }
        }
        return super.onOptionsItemSelected(item)
    }

    override fun onDestroy() {
        /*
        Giải pháp: Thêm một nút dùng để ngắt kết nối trước khi thoát
         */
        if (connect) {
            socket!!.close()
            bluetoothAdapter!!.enable()
        }
        client.disconnect()
        super.onDestroy()
    }
}

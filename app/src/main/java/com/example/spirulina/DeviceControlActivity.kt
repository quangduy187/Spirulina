package com.example.spirulina

import android.content.Context
import android.content.Intent
import android.content.SharedPreferences
import android.os.Bundle
import android.util.Log
import android.view.Menu
import android.view.MenuItem
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import com.example.spirulina.Config.Config
import kotlinx.android.synthetic.main.activity_device_control.*
import org.eclipse.paho.android.service.MqttAndroidClient
import org.eclipse.paho.client.mqttv3.*
import java.io.UnsupportedEncodingException




class DeviceControlActivity : AppCompatActivity() {

    private val serverUri = "tcp://broker.mqttdashboard.com"
    private val clientId = "bkstar_mandevices"
    private val subscriptionTopic = "actuator/+"
    private val USER = "ggwidzvz"
    private val PASSWORD = "M9aENgCBGMpz"
    lateinit var client: MqttAndroidClient


    lateinit var mSharedPreferences: SharedPreferences
    private var mode: Boolean = true
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
        val avatar = mSharedPreferences.getInt(Config.FARM_PIC, 0)
        Toast.makeText(this, "$id  $name", Toast.LENGTH_LONG).show()
        supportActionBar!!.title = "$id $name"

        //Khởi tạo kết nối MQTT
        startMqtt()

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
            mode = true //Chế độ tự động
            btnAuto.setBackgroundColor(resources.getColor(R.color.light_blue))
            btnManual.setBackgroundColor(resources.getColor(R.color.white))
            publishMqttMessage("bkstar/actuator_mode/","auto")
            enableAllViews(false)
        }
        btnManual.setOnClickListener {
            mode = false //Chế độ bằng tay
            btnManual.setBackgroundColor(resources.getColor(R.color.light_blue))
            btnAuto.setBackgroundColor(resources.getColor(R.color.white))
            publishMqttMessage("bkstar/actuator_mode/","manual")
            enableAllViews(true)
        }

        //Bắt các sự kiện điều khiển
        imgPump.setOnClickListener {
            statePump = !statePump
            if (statePump) {
                imgPump.setImageResource(R.drawable.ic_pump_on)
                publishMqttMessage("bkstar/actuator_1/","pump_on")
            } else {
                imgPump.setImageResource(R.drawable.ic_pump)
                publishMqttMessage("bkstar/actuator_1/","pump_off")
            }
        }
        imgLed.setOnClickListener {
            stateLed = !stateLed
            if(stateLed){
                imgLed.setImageResource(R.drawable.ic_diode_on)
                publishMqttMessage("bkstar/actuator_6/","led_on" )
            } else {
                imgLed.setImageResource(R.drawable.ic_diode_off)
                publishMqttMessage("bkstar/actuator_6/", "led_off")
            }
        }
        imgMist.setOnClickListener {
            stateMist = !stateMist
            if (stateMist) {
                imgMist.setImageResource(R.drawable.ic_fog_on)
                publishMqttMessage("bkstar/actuator_2/","mist_on")
            } else {
                imgMist.setImageResource(R.drawable.ic_fog)
                publishMqttMessage("bkstar/actuator_2/","mist_off")
            }
        }
        imgBlind.setOnClickListener {
            stateBlind = !stateBlind
            if (stateBlind) {
                imgBlind.setImageResource(R.drawable.ic_window_on)
                publishMqttMessage("bkstar/actuator_3/","blind_down")
            } else {
                imgBlind.setImageResource(R.drawable.ic_window)
                publishMqttMessage("bkstar/actuator_3/","blind_up")
            }
        }
        imgSoda.setOnClickListener {
            stateSoda = !stateSoda
            if (stateSoda) {
                imgSoda.setImageResource(R.drawable.ic_urn_on)
                publishMqttMessage("bkstar/actuator_4/","soda_on")
            } else {
                imgSoda.setImageResource(R.drawable.ic_urn)
                publishMqttMessage("bkstar/actuator_4/","soda_off")
            }
        }
        imgHarvest.setOnClickListener {
            stateHarvest = !stateHarvest
            if (stateHarvest) {
                imgHarvest.setImageResource(R.drawable.ic_harvest_on)
                publishMqttMessage("bkstar/actuator_5/","harvest_on")
            } else {
                imgHarvest.setImageResource(R.drawable.ic_harvest_off)
                publishMqttMessage("bkstar/actuator_5/","harvest_off")
            }
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
//            token.actionCallback = object : IMqttActionListener {
//                override fun onSuccess(asyncActionToken: IMqttToken?) {
//                    Log.d("debug", "onSuccess")
//                    Toast.makeText(this@DeviceControlActivity, "Kết nối thành công", Toast.LENGTH_SHORT).show()
//                }
//
//                override fun onFailure(asyncActionToken: IMqttToken?, exception: Throwable?) {
//                    Log.d("debug", "onFailure:  ${exception.toString()}")
//                    Toast.makeText(this@DeviceControlActivity, "Kết nối thất bại", Toast.LENGTH_SHORT).show()
//                }
//            }
            token.actionCallback = object : MqttCallbackExtended, IMqttActionListener {
                override fun onSuccess(asyncActionToken: IMqttToken?) {
                    Log.d("debug", "onSuccess")
                    Toast.makeText(this@DeviceControlActivity, "Kết nối thành công", Toast.LENGTH_SHORT).show()
                }

                override fun onFailure(asyncActionToken: IMqttToken?, exception: Throwable?) {
                    Log.d("debug", "onFailure:  ${exception.toString()}")
                    Toast.makeText(this@DeviceControlActivity, "Kết nối thất bại", Toast.LENGTH_SHORT).show()
                }

                override fun connectComplete(reconnect: Boolean, serverURI: String?) {
                    if (reconnect) {
                        Toast.makeText(this@DeviceControlActivity, "Reconnected to : $serverURI",Toast.LENGTH_SHORT).show()
                        // Because Clean Session is true, we need to re-subscribe
                        //subscribeToTopic();
                    } else {
                        Toast.makeText(this@DeviceControlActivity, "Connected to : $serverURI",Toast.LENGTH_SHORT).show()
                    }

                }

                override fun messageArrived(topic: String?, message: MqttMessage?) {
                    TODO("not implemented") //To change body of created functions use File | Settings | File Templates.
                }

                override fun connectionLost(cause: Throwable?) {
                    Toast.makeText(this@DeviceControlActivity,"Lost Connection", Toast.LENGTH_SHORT).show()
                }

                override fun deliveryComplete(token: IMqttDeliveryToken?) {
                    TODO("not implemented") //To change body of created functions use File | Settings | File Templates.
                }

            }
        } catch (e: MqttException) {
            Log.d("debug", e.message)
        }
    }

    private fun publishMqttMessage(topic: String, payload: String) {
        //"actuator/+pump"
        var encodedPayload: ByteArray
        try {
            encodedPayload = payload.toByteArray()
            client.publish(topic, encodedPayload,0,false)
            Log.d("debug","publish to actuator/+pump-on")
        } catch (e: UnsupportedEncodingException) {
            e.printStackTrace()
            Log.d("debug",e.message)
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

    override fun onCreateOptionsMenu(menu: Menu?): Boolean {
        menuInflater.inflate(R.menu.mqtt_menu, menu)
        return true
    }

    override fun onOptionsItemSelected(item: MenuItem?): Boolean {
        when (item!!.itemId) {
            R.id.itMQTT -> {
                //Connect MQTT
//                publishMqttMessage("bkstar/actuator_1/", "pump-on")
            }

        }
        return super.onOptionsItemSelected(item)
    }

    override fun onDestroy() {
        client.disconnect()
        super.onDestroy()
    }
}

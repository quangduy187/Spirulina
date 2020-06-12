package com.example.spirulina

import android.app.Notification
import android.content.Context
import android.content.Intent
import android.content.SharedPreferences
import android.os.Bundle
import android.util.Log
import android.view.Menu
import android.view.MenuItem
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.core.app.NotificationCompat
import androidx.core.app.NotificationManagerCompat
import com.android.volley.Request
import com.android.volley.Response
import com.android.volley.toolbox.JsonObjectRequest
import com.android.volley.toolbox.Volley
import com.example.spirulina.Config.Config
import com.example.spirulina.Object.AppNotification
import com.example.spirulina.Object.Environment
import com.macroyau.thingspeakandroid.ThingSpeakChannel
import kotlinx.android.synthetic.main.activity_environment.*
import org.json.JSONException
import org.json.JSONObject
import java.util.*


class EnvironmentActivity : AppCompatActivity() {

    lateinit var tsChannel: ThingSpeakChannel
    private val READ_API_KEY = "3TRHZYYRRK13BV7O"
    private val WRITE_API_KEY = "T14MTMLJYCDN0E9R"
    private val CHANNEL_ID: Long = 822057

    lateinit var notificationManager: NotificationManagerCompat
    lateinit var mSharedPreferences: SharedPreferences


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_environment)

        setSupportActionBar(tbEnvi)
        supportActionBar!!.setDisplayHomeAsUpEnabled(true)
        //Lấy thông tin từ Selection Farm
        mSharedPreferences = getSharedPreferences(Config.SHARE_CODE, Context.MODE_PRIVATE)
        val name = mSharedPreferences.getString(Config.FARM_NAME, "")
        val id = mSharedPreferences.getString(Config.FARM_ID, "")
        val avatar = mSharedPreferences.getInt(Config.FARM_PIC, 0)
//        Toast.makeText(this@EnvironmentActivity, "$id  $name", Toast.LENGTH_LONG).show()
        supportActionBar!!.title = "$id $name"

        // Khởi tạo quản lý thông báo
        notificationManager = NotificationManagerCompat.from(this)

        tsChannel = ThingSpeakChannel(CHANNEL_ID, READ_API_KEY)
        getData() // Lấy dữ liệu lần đầu tiên để khởi tạo

        //Image Warning send to Device Control
        imgCO.setOnClickListener {
            if (imgCO.tag == R.drawable.ic_warning) {
                sendToControl()
            }
        }
        imgCO2.setOnClickListener {
            if (imgCO2.tag == R.drawable.ic_warning) {
                sendToControl()
            }
        }

        imgKhiGas.setOnClickListener {
            if (imgKhiGas.tag == R.drawable.ic_warning) {
                sendToControl()
            }
        }

        imgKhoi.setOnClickListener {
            if (imgKhoi.tag == R.drawable.ic_warning) {
                sendToControl()
            }
        }

        imgTemp.setOnClickListener {
            if (imgTemp.tag == R.drawable.ic_warning) {
                sendToControl()
            }
        }

        imgHumi.setOnClickListener {
            if (imgHumi.tag == R.drawable.ic_warning) {
                sendToControl()
            }
        }

        imgLight.setOnClickListener {
            if (imgLight.tag == R.drawable.ic_warning) {
                sendToControl()
            }
        }

        imgPM1.setOnClickListener {
            if (imgPM1.tag == R.drawable.ic_warning) {
                sendToControl()
            }
        }

        imgPM25.setOnClickListener {
            if (imgPM25.tag == R.drawable.ic_warning) {
                sendToControl()
            }
        }

        imgPM10.setOnClickListener {
            if (imgPM10.tag == R.drawable.ic_warning) {
                sendToControl()
            }
        }
        //Floating button
        fabTao_envi.setOnClickListener {
            val intent = Intent(this@EnvironmentActivity, MainActivity::class.java)
            startActivity(intent)
        }
        fabEnvironment_envi.setOnClickListener {
            fabmenu_envi.close(true)
        }
        fabControl_envi.setOnClickListener {
            val intent = Intent(this@EnvironmentActivity, DeviceControlActivity::class.java)
            startActivity(intent)
        }
    }


    override fun onCreateOptionsMenu(menu: Menu?): Boolean {
        menuInflater.inflate(R.menu.envi_menu, menu)
        return true
    }

    override fun onOptionsItemSelected(item: MenuItem?): Boolean {
        when (item!!.itemId) {
            R.id.itRefreshEnvi -> {
                Toast.makeText(this@EnvironmentActivity, "Update Data", Toast.LENGTH_SHORT).show()
                getData()
            }
        }
        return super.onOptionsItemSelected(item)
    }

    private fun sendOnChannelOne(id: Int, message: String) {
        val notification: Notification =
            NotificationCompat.Builder(this, AppNotification().CHANNEL_1_ID)
                .setSmallIcon(R.drawable.ic_warning)
                .setContentTitle("Warning!!!")
                .setContentText(message)
                .setPriority(NotificationCompat.PRIORITY_HIGH)
                .setCategory(NotificationCompat.CATEGORY_MESSAGE)
                .build()
        notificationManager.notify(id, notification)
    }


    private fun getData() {
        var rawData: Environment
        var error = false
        tsChannel.setChannelFeedUpdateListener { channelId, channelName, channelFeed ->
            Log.d("AAA", "$channelId $channelName")
            val position = channelFeed.feeds.size - 1

            var co: Float = (0.2 + rand(0, 10) / 10.0).toFloat()
            var hum: Float? = null
            var temper: Float? = null
            var pm1: Int? = null
            var pm25: Int? = null
            var pm10: Int? = null
            var light: Int? = null
            var gas: Int? = null
            val co2 = (150 + rand(0, 20)).toFloat()
            val fire = 300

            try {
                try {
                    pm1 = channelFeed.feeds[position].field2.toInt()
                    pm25 = channelFeed.feeds[position].field3.toInt()
                    pm10 = channelFeed.feeds[position].field4.toInt()
                } catch (e: Exception) {
                    try {
                        pm1 = channelFeed.feeds[position - 1].field2.toInt()
                        pm25 = channelFeed.feeds[position - 1].field3.toInt()
                        pm10 = channelFeed.feeds[position - 1].field4.toInt()
                    } catch (e: Exception) {
                        pm1 = channelFeed.feeds[position - 2].field2.toInt()
                        pm25 = channelFeed.feeds[position - 2].field3.toInt()
                        pm10 = channelFeed.feeds[position - 2].field4.toInt()
                    }
                }
            } catch (e: Exception) {
                try {
                    try {
                        pm1 = channelFeed.feeds[position - 3].field2.toInt()
                        pm25 = channelFeed.feeds[position - 3].field3.toInt()
                        pm10 = channelFeed.feeds[position - 3].field4.toInt()
                    } catch (e: Exception) {
                        try {
                            pm1 = channelFeed.feeds[position - 4].field2.toInt()
                            pm25 = channelFeed.feeds[position - 4].field3.toInt()
                            pm10 = channelFeed.feeds[position - 4].field4.toInt()
                        } catch (e: Exception) {
                            pm1 = channelFeed.feeds[position - 5].field2.toInt()
                            pm25 = channelFeed.feeds[position - 5].field3.toInt()
                            pm10 = channelFeed.feeds[position - 5].field4.toInt()
                        }
                    }

                } catch (e: Exception) {
                    Toast.makeText(
                        this@EnvironmentActivity,
                        "Dữ liệu không khớp",
                        Toast.LENGTH_SHORT
                    ).show()
                    error = true
                }
            } finally {
                try {
                    try {
                        hum = channelFeed.feeds[position].field5.toFloat()
                        light = channelFeed.feeds[position].field6.toInt()
                        gas = channelFeed.feeds[position].field7.toInt()
                        temper = channelFeed.feeds[position].field8.toFloat()
                    } catch (e: Exception) {
                        hum = channelFeed.feeds[position - 1].field5.toFloat()
                        light = channelFeed.feeds[position - 1].field6.toInt()
                        gas = channelFeed.feeds[position - 1].field7.toInt()
                        temper = channelFeed.feeds[position - 1].field8.toFloat()
                    }

                } catch (e: Exception) {
                    try {
                        try {
                            hum = channelFeed.feeds[position - 2].field5.toFloat()
                            light = channelFeed.feeds[position - 2].field6.toInt()
                            gas = channelFeed.feeds[position - 2].field7.toInt()
                            temper = channelFeed.feeds[position - 2].field8.toFloat()
                        } catch (e: Exception) {
                            hum = channelFeed.feeds[position - 3].field5.toFloat()
                            light = channelFeed.feeds[position - 3].field6.toInt()
                            gas = channelFeed.feeds[position - 3].field7.toInt()
                            temper = channelFeed.feeds[position - 3].field8.toFloat()
                        }
                    } catch (e: Exception) {
                        try {
                            hum = channelFeed.feeds[position - 4].field5.toFloat()
                            light = channelFeed.feeds[position - 4].field6.toInt()
                            gas = channelFeed.feeds[position - 4].field7.toInt()
                            temper = channelFeed.feeds[position - 4].field8.toFloat()
                        } catch (e: Exception) {
                            try {
                                hum = channelFeed.feeds[position - 5].field5.toFloat()
                                light = channelFeed.feeds[position - 5].field6.toInt()
                                gas = channelFeed.feeds[position - 5].field7.toInt()
                                temper = channelFeed.feeds[position - 5].field8.toFloat()
                            } catch (e: Exception) {
                                Toast.makeText(
                                    this@EnvironmentActivity,
                                    "Kiểm tra lại Server",
                                    Toast.LENGTH_SHORT
                                ).show()
                                error = true
                            }
                        }
                    }
                }
            }

//            Log.d("AAA", "Nồng độ CO $co")
//            Log.d("AAA", "Ánh sáng $light Khí ga $gas")
            if (!error) {
                rawData = Environment(co, co2, pm1, pm25, pm10, hum, light, gas, temper, fire)
                changeXml(rawData)
            }
        }
        tsChannel.loadChannelFeed()
    }

    private fun changeXml(eData: Environment) {
        txtCO.text = eData.co.toString() + " ppm"
        txtCO2.text = eData.co2.toString() + " ppm"
        txtPM1.text = eData.pm1.toString() + " ug/m3"
        txtPM25.text = eData.pm25.toString() + " ug/m3"
        txtPM10.text = eData.pm10.toString() + " ug/m3"
        txtAnhSang.text = eData.light.toString() + "lx"
        txtNhietDo.text = eData.temper.toString() + "°C"
        txtDoAm.text = eData.eHumi.toString() + "%"


        if (eData.co!! > 10f) {
            imgCO.setImageResource(R.drawable.ic_warning)
            imgCO.tag = Integer.valueOf(R.drawable.ic_warning)
        } else {
            imgCO.setImageResource(R.drawable.ic_shield)
            imgCO.tag = Integer.valueOf(R.drawable.ic_shield)
        }
        if (eData.co2!! > 200f) {
            imgCO2.setImageResource(R.drawable.ic_warning)
            imgCO2.tag = Integer.valueOf(R.drawable.ic_warning)
        } else {
            imgCO2.setImageResource(R.drawable.ic_shield)
            imgCO2.tag = Integer.valueOf(R.drawable.ic_shield)
        }

        if (eData.pm1!! > 50f) {
            imgPM1.setImageResource(R.drawable.ic_warning)
            imgPM1.tag = Integer.valueOf(R.drawable.ic_warning)
            sendOnChannelOne(7, "Nồng độ bụi PM1.0 vượt ngưỡng!")
        } else {
            imgPM1.setImageResource(R.drawable.ic_shield)
            imgPM1.tag = Integer.valueOf(R.drawable.ic_shield)
        }

        if (eData.pm25!! > 100f) {
            imgPM25.setImageResource(R.drawable.ic_warning)
            imgPM25.tag = Integer.valueOf(R.drawable.ic_warning)
            sendOnChannelOne(8, "Nồng độ bụi PM2.5 vượt ngưỡng!")
        } else {
            imgPM25.setImageResource(R.drawable.ic_shield)
            imgPM25.tag = Integer.valueOf(R.drawable.ic_shield)
        }

        if (eData.pm10!! > 120f) {
            imgPM10.setImageResource(R.drawable.ic_warning)
            imgPM10.tag = Integer.valueOf(R.drawable.ic_warning)
            sendOnChannelOne(10, "Nồng độ bụi PM10.0 vượt ngưỡng!")
        } else {
            imgPM10.setImageResource(R.drawable.ic_shield)
            imgPM10.tag = Integer.valueOf(R.drawable.ic_shield)
        }

        if (eData.temper!! > 35f || eData.temper!! < 15f) {
            imgTemp.setImageResource(R.drawable.ic_warning)
            imgTemp.tag = Integer.valueOf(R.drawable.ic_warning)
            sendOnChannelOne(11, "Nhiệt độ môi trường ở mức báo động!")
        } else {
            imgTemp.setImageResource(R.drawable.ic_shield)
            imgTemp.tag = Integer.valueOf(R.drawable.ic_shield)
        }

        if (eData.eHumi!! < 50f) {
            imgHumi.setImageResource(R.drawable.ic_warning)
            imgHumi.tag = Integer.valueOf(R.drawable.ic_warning)
            sendOnChannelOne(12, "Độ ẩm không khí thấp!")
        } else {
            imgHumi.setImageResource(R.drawable.ic_shield)
            imgHumi.tag = Integer.valueOf(R.drawable.ic_shield)
        }

        if (eData.light!! > 2000) {
            imgLight.setImageResource(R.drawable.ic_warning)
            imgLight.tag = Integer.valueOf(R.drawable.ic_warning)
            sendOnChannelOne(13, "Ánh sáng vượt mức cho phép!")
        } else {
            imgLight.setImageResource(R.drawable.ic_shield)
            imgLight.tag = Integer.valueOf(R.drawable.ic_shield)

        }

        if (eData.gas!! > 500) {
            txtKhiGas.text = "Nguy hiểm"
            imgKhiGas.setImageResource(R.drawable.ic_warning)
            imgKhiGas.tag = Integer.valueOf(R.drawable.ic_warning)
            sendOnChannelOne(14, "Có rò rỉ khí ga!!!")
        } else {
            txtKhiGas.text = "An toàn"
            imgKhiGas.setImageResource(R.drawable.ic_shield)
            imgKhiGas.tag = Integer.valueOf(R.drawable.ic_shield)
        }

        if (eData.fire!! > 700) {
            txtKhoi.text = "Nguy hiểm"
            imgKhoi.setImageResource(R.drawable.ic_warning)
            imgKhoi.tag = Integer.valueOf(R.drawable.ic_warning)
            sendOnChannelOne(15, "Phát hiện có lửa!!!")
        } else {
            txtKhoi.text = "An toàn"
            imgKhoi.setImageResource(R.drawable.ic_shield)
            imgKhoi.tag = Integer.valueOf(R.drawable.ic_shield)
        }
    }

    private fun fetchData() {
        val lightApi =
            "https://api.thingspeak.com/channels/822057/feeds.json?api_key=3TRHZYYRRK13BV7O&results=2"
        val queue = Volley.newRequestQueue(this)
        val objectRequest = JsonObjectRequest(
            Request.Method.GET, lightApi, null,
            Response.Listener<JSONObject> { response ->
                try {
                    val feeds = response.getJSONArray("feeds")
                    for (i in 0 until feeds.length()) {
                        val jo = feeds.getJSONObject(i)
                        val l = jo.getString("field3")
                        Toast.makeText(applicationContext, l, Toast.LENGTH_SHORT).show()
                    }
                } catch (e: JSONException) {
                    e.printStackTrace()
                }
            }, Response.ErrorListener { })

        // Access the RequestQueue through your singleton class.
        queue.add(objectRequest)
    }

    private fun sendToControl() {
        val intent = Intent(this@EnvironmentActivity, DeviceControlActivity::class.java)
        startActivity(intent)
    }

    private fun rand(from: Int, to: Int): Int {
        val random = Random()
        return random.nextInt(to - from) + from
    }
}

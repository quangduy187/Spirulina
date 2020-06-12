package com.example.spirulina

import android.app.Notification
import android.content.Context
import android.content.Intent
import android.content.SharedPreferences
import android.os.Bundle
import android.util.Log
import android.view.Menu
import android.view.MenuItem
import android.view.View
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.ActionBarDrawerToggle
import androidx.appcompat.app.AppCompatActivity
import androidx.core.app.NotificationCompat
import androidx.core.app.NotificationManagerCompat
import androidx.core.view.GravityCompat
import com.example.spirulina.Config.Config
import com.example.spirulina.Object.AppNotification
import com.example.spirulina.Object.Spirulina
import com.google.android.material.navigation.NavigationView
import com.macroyau.thingspeakandroid.ThingSpeakChannel
import de.hdodenhof.circleimageview.CircleImageView
import kotlinx.android.synthetic.main.activity_main.*
import kotlinx.android.synthetic.main.main_screen.*
import java.util.*

class MainActivity : AppCompatActivity(), NavigationView.OnNavigationItemSelectedListener {

    lateinit var tsChannel: ThingSpeakChannel
    //    private val READ_API_KEY = "7HS6W36TS62SJ11R"
    private val READ_API_KEY = "W87B38VLJLACOZ85"
    private val WRITE_API_KEY = "LOLWWMTQTYKGED7W"
    private val CHANNEL_ID: Long = 913714

    lateinit var notificationManager: NotificationManagerCompat
    private lateinit var mSharedPreferences: SharedPreferences

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.main_screen)

        setSupportActionBar(tbMain)

        // thiết lập toggle cho navigation
        val toggle = ActionBarDrawerToggle(
            this@MainActivity,
            main_drawer,
            tbMain,
            R.string.navigation_drawer_open,
            R.string.navigation_drawer_close
        )
        main_drawer.addDrawerListener(toggle)
        toggle.syncState()

        //Lấy thông tin từ Selection Farm
        mSharedPreferences = getSharedPreferences(Config.SHARE_CODE, Context.MODE_PRIVATE)
        val name = mSharedPreferences.getString(Config.FARM_NAME, "")
        val id = mSharedPreferences.getString(Config.FARM_ID, "")
        val avatar = mSharedPreferences.getInt(Config.FARM_PIC, 0)
        Toast.makeText(this@MainActivity, "$id  $name", Toast.LENGTH_LONG).show()
        supportActionBar!!.title = "$id $name"

        // Khởi tạo quản lý thông báo
        notificationManager = NotificationManagerCompat.from(this)


        // Switch to Select Area
        val headerView = nav_view.getHeaderView(0)
        val imgHouse = headerView.findViewById<View>(R.id.imgHouse) as CircleImageView
        val txtHouse = headerView.findViewById<View>(R.id.txtHouse) as TextView
        txtHouse.text = name!!
        imgHouse.setImageResource(avatar)
        imgHouse.setOnClickListener {
            val intent = Intent(this@MainActivity, SelectionActivity::class.java)
            startActivity(intent)
        }

        //Image Warning send to Device Control
        imgNhietDo.setOnClickListener {
            if (imgNhietDo.tag == R.drawable.ic_warning) {
                sendToControl()
            }
        }

        imgAnhSang.setOnClickListener {
            if (imgAnhSang.tag == R.drawable.ic_warning) {
                sendToControl()
            }
        }

        imgWSpeed.setOnClickListener {
            if (imgWSpeed.tag == R.drawable.ic_warning) {
                sendToControl()
            }
        }

        imgWPressure.setOnClickListener {
            if (imgWPressure.tag == R.drawable.ic_warning) {
                sendToControl()
            }
        }

        imgWpH.setOnClickListener {
            if (imgWpH.tag == R.drawable.ic_warning) {
                sendToControl()
            }
        }

        imgWQuality.setOnClickListener {
            if (imgWQuality.tag == R.drawable.ic_warning) {
                sendToControl()
            }
        }

        tsChannel = ThingSpeakChannel(CHANNEL_ID, READ_API_KEY)
        getData() // Lấy dữ liệu lần đầu tiên để khởi tạo giá trị

        //Floating button
        fabEnvironment.setOnClickListener {
            val intent = Intent(this@MainActivity, EnvironmentActivity::class.java)
            startActivity(intent)
        }
        fabTao.setOnClickListener {
            fabmenu.close(true)
        }

        fabControl.setOnClickListener {
            val intent = Intent(this@MainActivity, DeviceControlActivity::class.java)
            startActivity(intent)
        }
    }

    private fun getData() {
        Log.d("AAA", "On here")
        var rawData: Spirulina
        var error = false
        tsChannel.setChannelFeedUpdateListener { channelId, channelName, channelFeed ->
            val position = channelFeed.feeds.size - 1
            val pressure = 1.5f
            var wTemp: Float = 22f
            var wNTU: Float? = null
            var pH: Float? = null
            var light: Int? = null
            var wSpeed: Int? = null
            var abs: Float? = null
            val sColor: Int = rand(180, 245)
            var remain = 12
            try {
                light = try {
                    try {
                        channelFeed.feeds[position].field3.toInt()
                    } catch (e: Exception) {
                        channelFeed.feeds[position - 1].field3.toInt()
                    }
                } catch (e: Exception) {
                    try {
                        channelFeed.feeds[position - 2].field3.toInt()
                    } catch (e: Exception) {
                        channelFeed.feeds[position - 3].field3.toInt()
                    }
                }
            } catch (e: Exception) {
                try {
                    light = try {
                        channelFeed.feeds[position - 4].field3.toInt()
                    } catch (e: Exception) {
                        try {
                            channelFeed.feeds[position - 5].field3.toInt()
                        } catch (e: Exception) {
                            channelFeed.feeds[position - 9].field3.toInt()
                        }
                    }

                } catch (e: Exception) {
                    Toast.makeText(this@MainActivity, "Dữ liệu không khớp", Toast.LENGTH_SHORT)
                        .show()
                    error = true
                }
            } finally {
                try {
                    try {
                        try {
                            wNTU = channelFeed.feeds[position].field2.toFloat()
                            wSpeed = channelFeed.feeds[position].field4.toInt()
                            pH = channelFeed.feeds[position].field5.toFloat()
                            abs = channelFeed.feeds[position].field6.toFloat()
                        } catch (e: Exception) {
                            wNTU = channelFeed.feeds[position - 1].field2.toFloat()
                            wSpeed = channelFeed.feeds[position - 1].field4.toInt()
                            pH = channelFeed.feeds[position - 1].field5.toFloat()
                            abs = channelFeed.feeds[position - 1].field6.toFloat()
                        }

                    } catch (e: Exception) {
                        try {
                            try {
                                wNTU = channelFeed.feeds[position - 2].field2.toFloat()
                                wSpeed = channelFeed.feeds[position - 2].field4.toInt()
                                pH = channelFeed.feeds[position - 2].field5.toFloat()
                                abs = channelFeed.feeds[position - 2].field6.toFloat()
                            } catch (e: Exception) {
                                wNTU = channelFeed.feeds[position - 3].field2.toFloat()
                                wSpeed = channelFeed.feeds[position - 3].field4.toInt()
                                pH = channelFeed.feeds[position - 3].field5.toFloat()
                                abs = channelFeed.feeds[position - 3].field6.toFloat()
                            }
                        } catch (e: Exception) {
                            wNTU = channelFeed.feeds[position - 4].field2.toFloat()
                            wSpeed = channelFeed.feeds[position - 4].field4.toInt()
                            pH = channelFeed.feeds[position - 4].field5.toFloat()
                            abs = channelFeed.feeds[position - 4].field6.toFloat()
                        }
                    }
                } catch (e: Exception) {
                    try {
                        wNTU = channelFeed.feeds[position - 5].field2.toFloat()
                        wSpeed = channelFeed.feeds[position - 5].field4.toInt()
                        pH = channelFeed.feeds[position - 5].field5.toFloat()
                        abs = channelFeed.feeds[position - 5].field6.toFloat()
                    } catch (e: Exception) {
                        Toast.makeText(this@MainActivity, "Kiểm tra lại Server", Toast.LENGTH_SHORT)
                            .show()
                        error = true
                    }
                }
            }

            try {
                wTemp = channelFeed.feeds[position - 5].field1.toFloat()
                remain = channelFeed.feeds[position - 5].field8.toInt()
            } catch (e: Exception) {
                try {
                    wTemp = channelFeed.feeds[position - 4].field1.toFloat()
                    remain = channelFeed.feeds[position - 4].field8.toInt()
                } catch (e: Exception) {
                    try {
                        wTemp = channelFeed.feeds[position - 3].field1.toFloat()
                        remain = channelFeed.feeds[position - 3].field8.toInt()
                    } catch (e: Exception) {
                        try {
                            wTemp = channelFeed.feeds[position - 2].field1.toFloat()
                            remain = channelFeed.feeds[position - 2].field8.toInt()
                        } catch (e: Exception) {
                            try {
                                wTemp = channelFeed.feeds[position - 1].field1.toFloat()
                                remain = channelFeed.feeds[position - 1].field8.toInt()
                            } catch (e: Exception) {
                                try {
                                    wTemp = channelFeed.feeds[position - 6].field1.toFloat()
                                    remain = channelFeed.feeds[position - 6].field8.toInt()
                                } catch (E: Exception) {
                                    try {
                                        wTemp = channelFeed.feeds[position - 7].field1.toFloat()
                                        remain = channelFeed.feeds[position - 7].field8.toInt()
                                    } catch (E: Exception) {
                                        try {
                                            wTemp = channelFeed.feeds[position - 8].field1.toFloat()
                                            remain = channelFeed.feeds[position - 8].field8.toInt()
                                        } catch (e: Exception) {
                                            try {
                                                wTemp = channelFeed.feeds[position].field1.toFloat()
                                                remain = channelFeed.feeds[position].field8.toInt()
                                            } catch (e: Exception) {
                                                Log.d("AAA", e.message)
                                                error = true
                                            }
                                        }
                                    }
                                }
                            }
                        }
                    }
                }
            }
            if (!error) {
                rawData = Spirulina(wTemp, wNTU, wSpeed, light, pressure, pH, abs, sColor)
                changeXML(rawData, remain)
            } else {
                sendOnChannelOne(2, "Kiểm tra lại Server")
            }
        }
        tsChannel.loadChannelFeed()
    }

    private fun changeXML(rawData: Spirulina, remain: Int) {
        txtHarvest.text = remain.toString()
        txtNhietDoNuoc.text = rawData.wTemp.toString() + "°C"
        txtAnhSang_tao.text = rawData.light.toString() + "lx"
        txtTocDoChay.text = rawData.wSpeed.toString() + "l/h"
        txtApSuat.text = rawData.wPressure.toString() + "pa"
        txtDopH.text = rawData.pH.toString()

        if (rawData.wTemp!! > 35 || rawData.wTemp!! < 25f) {
            imgNhietDo.setImageResource(R.drawable.ic_warning)
            imgNhietDo.tag = Integer.valueOf(R.drawable.ic_warning)
            sendOnChannelOne(3, "Điều kiện nhiệt độ không thuận lợi")
        } else {
            imgNhietDo.setImageResource(R.drawable.ic_shield)
            imgNhietDo.tag = Integer.valueOf(R.drawable.ic_shield)
        }

        if (rawData.light!! > 2000) {
            imgAnhSang.setImageResource(R.drawable.ic_warning)
            imgAnhSang.tag = Integer.valueOf(R.drawable.ic_warning)
            sendOnChannelOne(4, "Ánh sáng không thuận lợi")
        } else {
            imgAnhSang.setImageResource(R.drawable.ic_shield)
            imgAnhSang.tag = Integer.valueOf(R.drawable.ic_shield)
        }


        if (rawData.wSpeed!! > 1500) {
            imgWPressure.setImageResource(R.drawable.ic_warning)
            imgWPressure.tag = Integer.valueOf(R.drawable.ic_warning)
        } else {
            imgWPressure.setImageResource(R.drawable.ic_shield)
            imgWPressure.tag = Integer.valueOf(R.drawable.ic_shield)
        }

        if (rawData.wPressure!! > 2.0) {
            imgWPressure.setImageResource(R.drawable.ic_warning)
            imgWPressure.tag = Integer.valueOf(R.drawable.ic_warning)
        } else {
            imgWPressure.setImageResource(R.drawable.ic_shield)
            imgWPressure.tag = Integer.valueOf(R.drawable.ic_shield)
        }

        if (rawData.pH!! > 10.0 || rawData.pH!! < 7.0) {
            imgWpH.setImageResource(R.drawable.ic_warning)
            imgWpH.tag = Integer.valueOf(R.drawable.ic_warning)
            sendOnChannelOne(5, "Độ pH không đảm bảo, kiểm tra ngay!")
        } else {
            imgWpH.setImageResource(R.drawable.ic_shield)
            imgWpH.tag = Integer.valueOf(R.drawable.ic_shield)
        }

        if (rawData.wABS!! > 500.0) {
            txtChatLuongNuoc.text = "Dangerous"
            imgWQuality.setImageResource(R.drawable.ic_warning)
            imgWQuality.tag = Integer.valueOf(R.drawable.ic_warning)
            sendOnChannelOne(6, "Chất lượng nước không tốt!")
        } else {
            txtChatLuongNuoc.text = "Good"
            imgWQuality.setImageResource(R.drawable.ic_shield)
            imgWQuality.tag = Integer.valueOf(R.drawable.ic_shield)
        }

        if(remain == 0){
            sendOnChannelTwo(1, "Có thế thu hoạch!!!")
        }
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

    private fun sendOnChannelTwo(id: Int, message: String) {
        val notification: Notification =
            NotificationCompat.Builder(this, AppNotification().CHANNEL_2_ID)
                .setSmallIcon(R.drawable.ic_warning)
                .setContentTitle("Remind!!!")
                .setContentText(message)
                .setPriority(NotificationCompat.PRIORITY_LOW)
                .setCategory(NotificationCompat.CATEGORY_REMINDER)
                .build()
        notificationManager.notify(id, notification)
    }

    private fun sendToControl() {
        val intent = Intent(this@MainActivity, DeviceControlActivity::class.java)
        startActivity(intent)
    }

    private fun rand(from: Int, to: Int): Int {
        val random = Random()
        return random.nextInt(to - from) + from
    }

    override fun onBackPressed() {
        if (main_drawer.isDrawerOpen(GravityCompat.START)) {
            main_drawer.closeDrawer(GravityCompat.START)
        } else {
            super.onBackPressed()
        }
    }

    override fun onNavigationItemSelected(item: MenuItem): Boolean {
        when (item.itemId) {
//            R.id.itProfile -> {
//                val intent = Intent(this@MainActivity, ProfileActivity::class.java)
//                startActivity(intent)
//            }
            R.id.itLogout -> {
                val intent = Intent(this@MainActivity, SignInActivity::class.java)
                startActivity(intent)
            }
        }
        return true
    }

    override fun onCreateOptionsMenu(menu: Menu?): Boolean {
        menuInflater.inflate(R.menu.main_menu, menu)
        return true
    }

    override fun onOptionsItemSelected(item: MenuItem?): Boolean {
        when (item!!.itemId) {
//            R.id.itStart -> {
//                Toast.makeText(this@MainActivity, "Clicked on start", Toast.LENGTH_SHORT).show()
//                sendOnChannelOne(1, "Cảnh báo có khói!!")
//            }
            R.id.itSettingMain -> {
//                Toast.makeText(this@MainActivity, "Clicked on Setting", Toast.LENGTH_SHORT).show()
                val intent = Intent(this@MainActivity, EsptouchDemoActivity::class.java)
                startActivity(intent)
            }
            R.id.itRefresh -> {
                Toast.makeText(this@MainActivity, "Update Data", Toast.LENGTH_SHORT).show()
                getData()
            }
        }
        return super.onOptionsItemSelected(item)
    }
}

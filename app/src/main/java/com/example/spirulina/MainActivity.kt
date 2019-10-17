package com.example.spirulina

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
import androidx.core.view.GravityCompat
import com.example.spirulina.Config.Config
import com.example.spirulina.Object.Spirulina
import com.google.android.material.navigation.NavigationView
import com.macroyau.thingspeakandroid.ThingSpeakChannel
import com.macroyau.thingspeakandroid.ThingSpeakLineChart
import de.hdodenhof.circleimageview.CircleImageView
import kotlinx.android.synthetic.main.activity_main.*
import kotlinx.android.synthetic.main.main_screen.*

class MainActivity : AppCompatActivity(), NavigationView.OnNavigationItemSelectedListener {

    lateinit var  tsChannel: ThingSpeakChannel
    lateinit var  tsChart : ThingSpeakLineChart
    private val READ_API_KEY = "7HS6W36TS62SJ11R"
    private val WRITE_API_KEY = "LOLWWMTQTYKGED7W"
    private val CHANNEL_ID: Long = 881370
    private lateinit var mSharedPreferences: SharedPreferences

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.main_screen)

        setSupportActionBar(tbMain)

        // thiết lập toggle cho navigation
        val toggle = ActionBarDrawerToggle(this@MainActivity, main_drawer, tbMain, R.string.navigation_drawer_open, R.string.navigation_drawer_close)
        main_drawer.addDrawerListener(toggle)
        toggle.syncState()

        //Lấy thông tin từ Selection Farm
        mSharedPreferences = getSharedPreferences(Config.SHARE_CODE, Context.MODE_PRIVATE)
        val name = mSharedPreferences.getString(Config.FARM_NAME,"")
        val id = mSharedPreferences.getString(Config.FARM_ID,"")
        val avatar  = mSharedPreferences.getInt(Config.FARM_PIC,0)
        Toast.makeText(this@MainActivity,"$id  $name",Toast.LENGTH_LONG).show()
        supportActionBar!!.title = "$id $name"

        //tsChannel = ThingSpeakChannel(CHANNEL_ID,READ_API_KEY)
//        getData()

        // Switch to Select Area
        val headerView = nav_view.getHeaderView(0)
        val imgHouse = headerView.findViewById<View>(R.id.imgHouse) as CircleImageView
        val txtHouse = headerView.findViewById<View>(R.id.txtHouse) as TextView
        txtHouse.text = name!!
        imgHouse.setImageResource(avatar)
        imgHouse.setOnClickListener{
            val intent = Intent(this@MainActivity,SelectionActivity::class.java)
            startActivity(intent)
        }

        //Image Warning send to Device Control
        imgNhietDo.setOnClickListener {
            if (imgNhietDo.tag == R.drawable.ic_warning){
                sendToControl()
            }
        }

        imgAnhSang.setOnClickListener {
            if (imgAnhSang.tag == R.drawable.ic_warning){
                sendToControl()
            }
        }

        imgWSpeed.setOnClickListener {
            if(imgWSpeed.tag == R.drawable.ic_warning){
                sendToControl()
            }
        }

        imgWPressure.setOnClickListener {
            if(imgWPressure.tag == R.drawable.ic_warning){
                sendToControl()
            }
        }

        imgWpH.setOnClickListener {
            if(imgWpH.tag == R.drawable.ic_warning){
                sendToControl()
            }
        }

        imgWQuality.setOnClickListener {
            if(imgWQuality.tag == R.drawable.ic_warning){
                sendToControl()
            }
        }


        //getData() // Lấy dữ liệu lần đầu tiên để khởi tạo giá trị
        //Floating button
        fabEnvironment.setOnClickListener {
            val intent = Intent(this@MainActivity,EnvironmentActivity::class.java)
            startActivity(intent)
        }
        fabTao.setOnClickListener {
            fabmenu.close(true)
        }

        fabControl.setOnClickListener {
            val intent = Intent(this@MainActivity,DeviceControlActivity::class.java)
            startActivity(intent)
        }
    }

    private fun getData(){
        var rawData : Spirulina
        tsChannel.setChannelFeedUpdateListener { channelId, channelName, channelFeed ->
            Log.d("AAA", "$channelId $channelName")
            val position = channelFeed.feeds.size - 1
            val wTemp = channelFeed.feeds[position].field1.toFloat()
            val wNTU = channelFeed.feeds[position].field2.toFloat()
            val light = channelFeed.feeds[position].field3.toInt()
            val wSpeed = channelFeed.feeds[position].field4.toInt()
            val pressure = 1.5f
            val pH = channelFeed.feeds[position].field5.toFloat()
            val abs = channelFeed.feeds[position].field6.toInt()
            val sColor = channelFeed.feeds[position].field7.toLong()
            rawData = Spirulina(wTemp,wNTU,wSpeed,light,pressure,pH,abs,sColor)
            changeXML(rawData)
        }
    }

    private fun changeXML(rawData: Spirulina){
        txtNhietDoNuoc.text = rawData.wTemp.toString() + "°C"
        txtAnhSang_tao.text = rawData.light.toString() + "lx"
        txtTocDoChay.text = rawData.wSpeed.toString()  + "l/h"
        txtApSuat.text = rawData.wPressure.toString()  + "pa"
        txtDopH.text = rawData.pH.toString()
//        txtChatLuongNuoc.text = rawData.wABS.toString()

        if(rawData.wTemp > 30){
            imgNhietDo.setImageResource(R.drawable.ic_warning)
            imgNhietDo.tag = Integer.valueOf(R.drawable.ic_warning)
        } else {
            imgNhietDo.setImageResource(R.drawable.ic_shield)
            imgNhietDo.tag = Integer.valueOf(R.drawable.ic_shield)
        }

        if(rawData.light > 400){
            imgAnhSang.setImageResource(R.drawable.ic_warning)
            imgAnhSang.tag = Integer.valueOf(R.drawable.ic_warning)
        } else {
            imgAnhSang.setImageResource(R.drawable.ic_shield)
            imgAnhSang.tag = Integer.valueOf(R.drawable.ic_shield)
        }


        if(rawData.wSpeed > 1000){
            imgWPressure.setImageResource(R.drawable.ic_warning)
            imgWPressure.tag = Integer.valueOf(R.drawable.ic_warning)
        } else {
            imgWPressure.setImageResource(R.drawable.ic_shield)
            imgWPressure.tag = Integer.valueOf(R.drawable.ic_shield)
        }


        if(rawData.wPressure > 2.0){
            imgWPressure.setImageResource(R.drawable.ic_warning)
            imgWPressure.tag = Integer.valueOf(R.drawable.ic_warning)
        } else {
            imgWPressure.setImageResource(R.drawable.ic_shield)
            imgWPressure.tag = Integer.valueOf(R.drawable.ic_shield)
        }

        if(rawData.pH > 10.0 || rawData.pH < 3.0){
            imgWpH.setImageResource(R.drawable.ic_warning)
            imgWpH.tag = Integer.valueOf(R.drawable.ic_warning)
        } else {
            imgWpH.setImageResource(R.drawable.ic_shield)
            imgWpH.tag = Integer.valueOf(R.drawable.ic_shield)
        }

        if(rawData.wABS > 10.0){
            txtChatLuongNuoc.text = "Dangerous"
            imgWQuality.setImageResource(R.drawable.ic_warning)
            imgWQuality.tag = Integer.valueOf(R.drawable.ic_warning)
        } else {
            txtChatLuongNuoc.text = "Good"
            imgWQuality.setImageResource(R.drawable.ic_shield)
            imgWQuality.tag = Integer.valueOf(R.drawable.ic_shield)
        }
    }

    private fun sendToControl(){
        val intent = Intent(this@MainActivity,DeviceControlActivity::class.java)
        startActivity(intent)
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
        when(item!!.itemId){
            R.id.itShareFarm -> {
                Toast.makeText(this@MainActivity,"Clicked on share", Toast.LENGTH_SHORT).show()
            }
            R.id.itSetting -> {
                Toast.makeText(this@MainActivity,"Clicked on Setting", Toast.LENGTH_SHORT).show()
            }
            R.id.itRefresh -> {
                getData()
            }
        }
        return super.onOptionsItemSelected(item)
    }
}

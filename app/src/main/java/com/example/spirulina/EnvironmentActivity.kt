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
import com.android.volley.Request
import com.android.volley.Response
import com.android.volley.toolbox.JsonObjectRequest
import com.android.volley.toolbox.Volley
import com.example.spirulina.Config.Config
import com.example.spirulina.Object.Environment
import com.macroyau.thingspeakandroid.ThingSpeakChannel
import kotlinx.android.synthetic.main.activity_environment.*
import org.json.JSONException
import org.json.JSONObject


class EnvironmentActivity : AppCompatActivity() {

    lateinit var  tsChannel: ThingSpeakChannel
    private val READ_API_KEY = "3TRHZYYRRK13BV7O"
    private val WRITE_API_KEY = "T14MTMLJYCDN0E9R"
    private val CHANNEL_ID: Long = 822057
    lateinit var mSharedPreferences: SharedPreferences


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_environment)

        setSupportActionBar(tbEnvi)
        supportActionBar!!.setDisplayHomeAsUpEnabled(true)
        //Lấy thông tin từ Selection Farm
        mSharedPreferences = getSharedPreferences(Config.SHARE_CODE, Context.MODE_PRIVATE)
        val name = mSharedPreferences.getString(Config.FARM_NAME,"")
        val id = mSharedPreferences.getString(Config.FARM_ID,"")
        val avatar  = mSharedPreferences.getInt(Config.FARM_PIC,0)
        Toast.makeText(this@EnvironmentActivity,"$id  $name",Toast.LENGTH_LONG).show()
        supportActionBar!!.title = "$id $name"

        tsChannel = ThingSpeakChannel(CHANNEL_ID,READ_API_KEY)
        getData() // Lấy dữ liệu lần đầu tiên để khởi tạo

        //Image Warning send to Device Control
        imgCO.setOnClickListener {
            if (imgCO.tag == R.drawable.ic_warning){
                sendToControl()
            }
        }
        imgCO2.setOnClickListener {
            if (imgCO2.tag == R.drawable.ic_warning){
                sendToControl()
            }
        }

        imgKhiGas.setOnClickListener {
            if (imgKhiGas.tag == R.drawable.ic_warning){
                sendToControl()
            }
        }

        imgKhoi.setOnClickListener {
            if (imgKhoi.tag == R.drawable.ic_warning){
                sendToControl()
            }
        }

        imgTemp.setOnClickListener {
            if (imgTemp.tag == R.drawable.ic_warning){
                sendToControl()
            }
        }

        imgHumi.setOnClickListener {
            if (imgHumi.tag == R.drawable.ic_warning){
                sendToControl()
            }
        }

        imgLight.setOnClickListener {
            if (imgLight.tag == R.drawable.ic_warning){
                sendToControl()
            }
        }

        imgPM1.setOnClickListener {
            if (imgPM1.tag == R.drawable.ic_warning){
                sendToControl()
            }
        }

        imgPM25.setOnClickListener {
            if (imgPM25.tag == R.drawable.ic_warning){
                sendToControl()
            }
        }

        imgPM10.setOnClickListener {
            if (imgPM10.tag == R.drawable.ic_warning){
                sendToControl()
            }
        }
        //Floating button
        fabTao_envi.setOnClickListener {
            val intent = Intent(this@EnvironmentActivity,MainActivity::class.java)
            startActivity(intent)
        }
        fabEnvironment_envi.setOnClickListener {
            fabmenu_envi.close(true)
        }
        fabControl_envi.setOnClickListener {
            val intent = Intent(this@EnvironmentActivity,DeviceControlActivity::class.java)
            startActivity(intent)
        }
    }


    override fun onCreateOptionsMenu(menu: Menu?): Boolean {
        menuInflater.inflate(R.menu.envi_menu, menu)
        return true
    }

    override fun onOptionsItemSelected(item: MenuItem?): Boolean {
        when(item!!.itemId){
            R.id.itRefreshEnvi -> {
                Toast.makeText(this@EnvironmentActivity, "Update Data", Toast.LENGTH_SHORT).show()
                getData()
            }
        }
        return super.onOptionsItemSelected(item)
    }

    private fun getData(){
        var rawData : Environment
        tsChannel.setChannelFeedUpdateListener { channelId, channelName, channelFeed ->
            Log.d("AAA", "$channelId $channelName")
//            val humi = channelFeed.channel.field4
            val position = channelFeed.feeds.size - 1
            val co = channelFeed.feeds[position].field1.toFloat()
            val co2 = 200f
            val pm1 = channelFeed.feeds[position].field2.toInt()
            val pm25 = channelFeed.feeds[position].field3.toInt()
            val pm10 = channelFeed.feeds[position].field4.toInt()
            val hum = channelFeed.feeds[position].field5.toFloat()
            val light = channelFeed.feeds[position].field6.toInt()
            val gas = channelFeed.feeds[position].field7.toInt()
            val temper = channelFeed.feeds[position].field8.toFloat()
            val fire = 300

            Log.d("AAA", "Nồng độ CO $co")
            Log.d("AAA", "Ánh sáng $light Khí ga $gas")
            rawData = Environment(co,co2,pm1,pm25,pm10,hum,light,gas,temper,fire)
            changeXml(rawData)
        }
        tsChannel.loadChannelFeed()
    }

    private fun changeXml(eData: Environment){
        txtCO.text = eData.co.toString() + "ppm"
        txtCO2.text = eData.co2.toString() + "ppm"
        txtPM1.text = eData.pm1.toString()
        txtPM25.text = eData.pm25.toString()
        txtPM10.text = eData.pm10.toString()
        txtAnhSang.text = eData.light.toString() + "lx"
        txtNhietDo.text = eData.temper.toString() + "°C"
        txtDoAm.text = eData.eHumi.toString() + "%"


        if(eData.co > 1.5f) {
            imgCO.setImageResource(R.drawable.ic_warning)
            imgCO.tag = Integer.valueOf(R.drawable.ic_warning)
        } else {
            imgCO.setImageResource(R.drawable.ic_shield)
            imgCO.tag = Integer.valueOf(R.drawable.ic_shield)
        }
        if(eData.co2 > 200f){
            imgCO2.setImageResource(R.drawable.ic_warning)
            imgCO2.tag = Integer.valueOf(R.drawable.ic_warning)
        } else{
            imgCO2.setImageResource(R.drawable.ic_shield)
            imgCO2.tag = Integer.valueOf(R.drawable.ic_shield)
        }

        if(eData.pm1 > 40f) {
            imgPM1.setImageResource(R.drawable.ic_warning)
            imgPM1.tag = Integer.valueOf(R.drawable.ic_warning)
        } else{
            imgPM1.setImageResource(R.drawable.ic_shield)
            imgPM1.tag = Integer.valueOf(R.drawable.ic_shield)
        }

        if(eData.pm25 > 10f){
            imgPM25.setImageResource(R.drawable.ic_warning)
            imgPM25.tag = Integer.valueOf(R.drawable.ic_warning)
        } else{
            imgPM25.setImageResource(R.drawable.ic_shield)
            imgPM25.tag = Integer.valueOf(R.drawable.ic_shield)
        }

        if(eData.pm10 > 25f){
            imgPM10.setImageResource(R.drawable.ic_warning)
            imgPM10.tag = Integer.valueOf(R.drawable.ic_warning)
        } else{
            imgPM10.setImageResource(R.drawable.ic_shield)
            imgPM10.tag = Integer.valueOf(R.drawable.ic_shield)
        }

        if(eData.temper > 35f){
            imgTemp.setImageResource(R.drawable.ic_warning)
            imgTemp.tag = Integer.valueOf(R.drawable.ic_warning)
        } else{
            imgTemp.setImageResource(R.drawable.ic_shield)
            imgTemp.tag = Integer.valueOf(R.drawable.ic_shield)
        }

        if(eData.eHumi > 35f){
            imgHumi.setImageResource(R.drawable.ic_warning)
            imgHumi.tag = Integer.valueOf(R.drawable.ic_warning)
        } else{
            imgHumi.setImageResource(R.drawable.ic_shield)
            imgHumi.tag = Integer.valueOf(R.drawable.ic_shield)
        }


        if(eData.light > 19000){
            imgLight.setImageResource(R.drawable.ic_warning)
            imgLight.tag = Integer.valueOf(R.drawable.ic_warning)
        } else{
            imgLight.setImageResource(R.drawable.ic_shield)
            imgLight.tag = Integer.valueOf(R.drawable.ic_shield)

        }

        if(eData.gas > 500){
            txtKhiGas.text = "Nguy hiểm"
            imgKhiGas.setImageResource(R.drawable.ic_warning)
            imgKhiGas.tag = Integer.valueOf(R.drawable.ic_warning)
        } else{
            txtKhiGas.text = "An toàn"
            imgKhiGas.setImageResource(R.drawable.ic_shield)
            imgKhiGas.tag = Integer.valueOf(R.drawable.ic_shield)
        }

        if(eData.fire > 25){
            txtKhoi.text = "Nguy hiểm"
            imgKhoi.setImageResource(R.drawable.ic_warning)
            imgKhoi.tag = Integer.valueOf(R.drawable.ic_warning)
        } else{
            txtKhoi.text = "An toàn"
            imgKhoi.setImageResource(R.drawable.ic_shield)
            imgKhoi.tag = Integer.valueOf(R.drawable.ic_shield)
        }
    }

    private fun fetchData() {
        val lightApi = "https://api.thingspeak.com/channels/822057/feeds.json?api_key=3TRHZYYRRK13BV7O&results=2"
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

    private fun sendToControl(){
        val intent = Intent(this@EnvironmentActivity, DeviceControlActivity::class.java)
        startActivity(intent)
    }
}

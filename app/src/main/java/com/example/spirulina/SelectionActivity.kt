package com.example.spirulina

import android.app.Dialog
import android.content.Context
import android.content.Intent
import android.content.SharedPreferences
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.text.TextUtils
import android.util.Log
import android.view.Menu
import android.view.MenuItem
import android.widget.Button
import android.widget.EditText
import android.widget.Toast
import androidx.recyclerview.widget.DefaultItemAnimator
import androidx.recyclerview.widget.DividerItemDecoration
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.example.spirulina.Config.Config
import com.example.spirulina.Object.Farm
import kotlinx.android.synthetic.main.activity_selection.*

class SelectionActivity : AppCompatActivity(),PickFarmAdapter.AdapterCallback {
    override fun onMethodCallback() {
        val name = mSharedPreferences.getString(Config.FARM_NAME,"")
        val id = mSharedPreferences.getString(Config.FARM_ID,"")
        //Toast.makeText(this@SelectionActivity,"$id  $name",Toast.LENGTH_LONG).show()
        Log.d("AAA","$id + $name")
        sendToMain()
    }

    private lateinit var mSharedPreferences: SharedPreferences
    val list : ArrayList<Farm> = ArrayList()
    private lateinit var  customAdapter : PickFarmAdapter

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_selection)

        setSupportActionBar(tbSelect)
        supportActionBar!!.title = "Select a place"

        mSharedPreferences = getSharedPreferences(Config.SHARE_CODE, Context.MODE_PRIVATE)

        recyclerView.setHasFixedSize(true)
        val dividerItemDecoration = DividerItemDecoration(recyclerView.context, DividerItemDecoration.VERTICAL)
        recyclerView.addItemDecoration(dividerItemDecoration)
        val itemAnimator = DefaultItemAnimator()
        recyclerView.itemAnimator = itemAnimator
        recyclerView.layoutManager = LinearLayoutManager(this@SelectionActivity, RecyclerView.VERTICAL, false)

        list.add(Farm("Dàn Tảo 1", "FW01", R.drawable.ic_place))
        list.add(Farm("Dàn Tảo 2", "FW02", R.drawable.ic_map))
        customAdapter = PickFarmAdapter(list,this@SelectionActivity)
        recyclerView.adapter = customAdapter
    }

    private fun sendToMain(){
        val intent = Intent(this@SelectionActivity,MainActivity::class.java)
        startActivity(intent)
    }

    private fun addNodeDialog() {
        val dialog = Dialog(this)
        dialog.setContentView(R.layout.dialog_add_node)
        dialog.onBackPressed()
        dialog.setTitle("Add Node")
        val btnOk = dialog.findViewById<Button>(R.id.btnOk)
        val btnCancel = dialog.findViewById<Button>(R.id.btnCancel)
        val edtName  = dialog.findViewById<EditText>(R.id.edtSetNode)
        val edtID  = dialog.findViewById<EditText>(R.id.edtIdNode)

        btnCancel.setOnClickListener{
            dialog.dismiss()
        }
        btnOk.setOnClickListener{
            val name = edtName.text.toString()
            val id   = edtID.text.toString()
            if(!TextUtils.equals(name,"")||!TextUtils.equals(id,"")){
                list.add(Farm(name, id, R.drawable.ic_place))
                customAdapter.notifyDataSetChanged()
            } else {
                Toast.makeText(this,"Fill in the blank",Toast.LENGTH_SHORT).show()
            }
            dialog.dismiss()
        }
        dialog.show()
    }

    override fun onCreateOptionsMenu(menu: Menu?): Boolean {
        menuInflater.inflate(R.menu.select_menu,menu)
        return true
    }

    override fun onOptionsItemSelected(item: MenuItem?): Boolean {
        when(item!!.itemId){
            R.id.itAddFarm ->{
                addNodeDialog()
            }
        }
        return super.onOptionsItemSelected(item)
    }
}

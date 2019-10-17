package com.example.spirulina

import android.content.Context
import android.content.SharedPreferences
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.example.spirulina.Config.Config
import com.example.spirulina.Object.Farm

class PickFarmAdapter(private val farmList: ArrayList<Farm>, private val context: Context): RecyclerView.Adapter<PickFarmAdapter.FarmViewHolder>() {

    private lateinit var mSharedPreferences : SharedPreferences
    private var mAdapterCallBack: AdapterCallback
    init {
        this.mAdapterCallBack = context as AdapterCallback
    }
    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): FarmViewHolder {
        val inflater = LayoutInflater.from(parent.context)
        val view = inflater.inflate(R.layout.item_farm, parent, false)
        return FarmViewHolder(view)
    }

    override fun getItemCount(): Int {
         return farmList.size
    }

    override fun onBindViewHolder(holder: FarmViewHolder, position: Int) {
        val id = farmList[position].id
        val name = farmList[position].name
        val image = farmList[position].picture
        holder.farmId.text = id
        holder.farmName.text = name
        holder.imgFarm.setImageResource(image)
        mSharedPreferences = context.getSharedPreferences(Config.SHARE_CODE, Context.MODE_PRIVATE)
        holder.imgFarm.setOnClickListener {
            saveSharedPreStatus(id,name,image)
            mAdapterCallBack.onMethodCallback()
        }
    }

    inner class FarmViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView){
        val farmId = itemView.findViewById<View>(R.id.farmId) as TextView
        val farmName = itemView.findViewById<View>(R.id.farmName) as TextView
        val imgFarm = itemView.findViewById<View>(R.id.imgFarm) as ImageView
    }

    private fun saveSharedPreStatus(id: String, name : String,hinhanh: Int) {
        val editor = mSharedPreferences.edit()
        editor.putString(Config.FARM_ID, id)
        editor.putString(Config.FARM_NAME, name)
        editor.putInt(Config.FARM_PIC, hinhanh)
        editor.apply()
    }

    interface AdapterCallback {
        fun onMethodCallback()
    }
}
package com.swu.caresheep.ui.elder.connect

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.swu.caresheep.databinding.ItemElderConnectedGuardianBinding

class ElderConnectRVAdapter(private var guardianList: ArrayList<ConnectedGuardian>) :
    RecyclerView.Adapter<ElderConnectRVAdapter.ViewHolder>() {

    interface MyItemClickListener {
        fun onItemClick(guardian: ConnectedGuardian)
    }

    private lateinit var mItemClickListener: MyItemClickListener
    fun setMyItemClickListener(itemClickListener: MyItemClickListener) {
        mItemClickListener = itemClickListener
    }

    override fun onCreateViewHolder(
        viewGroup: ViewGroup,
        viewType: Int
    ): ViewHolder {
        val binding: ItemElderConnectedGuardianBinding =
            ItemElderConnectedGuardianBinding.inflate(
                LayoutInflater.from(viewGroup.context),
                viewGroup,
                false
            )

        return ViewHolder(binding)
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        holder.bind(guardianList[position])
    }

    override fun getItemCount(): Int {
        return guardianList.size
    }

    fun setData(items: ArrayList<ConnectedGuardian>) {
        this.guardianList = items
        notifyDataSetChanged()
    }

    inner class ViewHolder(val binding: ItemElderConnectedGuardianBinding) :
        RecyclerView.ViewHolder(binding.root) {
        fun bind(item: ConnectedGuardian) {
            binding.tvConnectedGuardian.text = item.name
        }
    }

}
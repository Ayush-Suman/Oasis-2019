package com.dvm.appd.bosm.dbg.wallet.views.adapters

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.TextView
import androidx.core.view.isVisible
import androidx.recyclerview.widget.RecyclerView
import com.dvm.appd.bosm.dbg.R
import com.dvm.appd.bosm.dbg.wallet.data.room.dataclasses.ModifiedStallItemsData
import kotlinx.android.synthetic.main.adapter_wallet_stall_items.view.*
import java.util.Collections.emptyList

class StallItemsAdapter(private val listener:OnAddClickedListener) :RecyclerView.Adapter<StallItemsAdapter.ItemsViewHolder>() {

    var stallItems :List<ModifiedStallItemsData> = emptyList()

    interface OnAddClickedListener{
        fun addButtonClicked(stallItem: ModifiedStallItemsData, quantity: Int)
        fun deleteCartItemClicked(itemId: Int)
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ItemsViewHolder {
        val view = LayoutInflater.from(parent.context).inflate(R.layout.adapter_wallet_stall_items,parent,false)
        return ItemsViewHolder(view)
    }

    override fun getItemCount(): Int = stallItems.size

    override fun onBindViewHolder(holder: ItemsViewHolder, position: Int) {

        holder.itemName.text = stallItems[position].itemName
        holder.price.text = "₹ ${stallItems[position].price}"
        holder.quantity.text = stallItems[position].quantity.toString()

        if (stallItems[position].quantity > 0){

            holder.quantity.isEnabled = true
            holder.quantity.isVisible = true
            holder.plus.isEnabled = true
            holder.plus.isVisible = true
            holder.minus.isEnabled = true
            holder.minus.isVisible = true
            holder.add.isEnabled = false
            holder.add.isVisible = false
            holder.quantity.text = stallItems[position].quantity.toString()

            holder.plus.setOnClickListener {

                listener.addButtonClicked(stallItems[position], stallItems[position].quantity + 1)
            }

            holder.minus.setOnClickListener {

                if (stallItems[position].quantity > 1){

                    listener.addButtonClicked(stallItems[position], stallItems[position].quantity - 1)
                }
                else{

                    listener.deleteCartItemClicked(stallItems[position].itemId)
                }
            }

        }
        else {

            holder.quantity.isEnabled = false
            holder.quantity.isVisible = false
            holder.plus.isEnabled = false
            holder.plus.isVisible = false
            holder.minus.isEnabled = false
            holder.minus.isVisible = false
            holder.add.isEnabled = true
            holder.add.isVisible = true

            holder.add.setOnClickListener {

                listener.addButtonClicked(stallItems[position], 1)
            }
        }
    }

    inner class ItemsViewHolder(view: View):RecyclerView.ViewHolder(view){

        val itemName: TextView = view.itemName
        val price: TextView = view. price
        val add: TextView = view.addBtn
        val plus: Button = view.plus
        val minus: Button = view.minus
        val quantity: TextView = view.quantity

    }
}
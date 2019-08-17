package com.dvm.appd.bosm.dbg.wallet.data.room.dataclasses

import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "stall_items")
data class StallItemsData(

    @PrimaryKey
    @ColumnInfo(name = "itemId")
    val itemId:Int,

    @ColumnInfo(name ="itemName")
    val itemName:String,

    @ColumnInfo(name = "stallId")
    val stallId:Int,

    @ColumnInfo(name = "price")
    val price:Int,

    @ColumnInfo(name = "isAvailable")
    val isAvailable:Boolean

    )
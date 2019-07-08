package com.dvm.appd.bosm.dbg.shared

import androidx.room.Database
import androidx.room.RoomDatabase
import com.dvm.appd.bosm.dbg.wallet.data.room.WalletDao
import com.dvm.appd.bosm.dbg.wallet.data.room.dataclasses.StallsData

@Database(entities = [StallsData::class],version = 1)
abstract class AppDatabase : RoomDatabase() {

    abstract fun walletDao():WalletDao

}
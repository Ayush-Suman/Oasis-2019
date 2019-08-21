package com.dvm.appd.bosm.dbg.profile.viewmodel

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import com.dvm.appd.bosm.dbg.profile.views.AddMoneyResult
import com.dvm.appd.bosm.dbg.wallet.data.repo.WalletRepository

class AddMoneyViewModel(val walletRepository: WalletRepository):ViewModel() {

    var result:LiveData<AddMoneyResult> = MutableLiveData()
    init{

    }
    fun addMoney(amount:Int){
        walletRepository.addMoneyBitsian(amount).subscribe({
            (result as MutableLiveData).postValue(it)
        },{
            (result as MutableLiveData).postValue(AddMoneyResult.Failure(it.message.toString()))
        })

    }
}
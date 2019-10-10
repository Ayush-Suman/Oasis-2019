package com.dvm.appd.oasis.dbg.wallet.viewmodel

import android.annotation.SuppressLint
import android.util.Log
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import com.dvm.appd.oasis.dbg.wallet.data.repo.WalletRepository
import com.dvm.appd.oasis.dbg.wallet.data.room.dataclasses.ModifiedCartData
import com.dvm.appd.oasis.dbg.wallet.data.room.dataclasses.ModifiedOrdersData

class OrdersViewModel(val walletRepository: WalletRepository): ViewModel(){

    var orders: LiveData<List<ModifiedOrdersData>> = MutableLiveData()
    var cartItems: LiveData<List<Pair<String, List<ModifiedCartData>>>> = MutableLiveData()
    var progressBarMark: LiveData<Int> = MutableLiveData(1)
    var error: LiveData<String> = MutableLiveData(null)

    init {

        walletRepository.addOrderListener()

        walletRepository.getAllOrders()
            .subscribe({
                Log.d("OrdersVM",it.toString())
                (orders as MutableLiveData).postValue(it)
                (error as MutableLiveData).postValue(null)
            },{
                Log.e("OrdersVM","Error",it)
                (error as MutableLiveData).postValue(it.message)
            })

        walletRepository.getAllModifiedCartItems()
            .subscribe({
                Log.d("CartVM", it.toString())
                (cartItems as MutableLiveData).postValue(it)
                (error as MutableLiveData).postValue(null)
            },{
                (error as MutableLiveData).postValue(it.message)
            })

    }

    fun fetchAllOrders(){
        walletRepository.updateOrders().subscribe({
            (progressBarMark as MutableLiveData).postValue(1)
            (error as MutableLiveData).postValue(null)
        },{
            (progressBarMark as MutableLiveData).postValue(1)
            (error as MutableLiveData).postValue(it.message)
            })
    }

    fun updateOtpSeen(orderId: Int){
        walletRepository.updateOtpSeen(orderId).subscribe({
            (progressBarMark as MutableLiveData).postValue(1)
            (error as MutableLiveData).postValue(null)
        },{
            (progressBarMark as MutableLiveData).postValue(1)
            (error as MutableLiveData).postValue(it.message)
        })
    }

    @SuppressLint("CheckResult")
    fun placeOrder(){
        walletRepository.placeOrder().subscribe({
            (progressBarMark as MutableLiveData).postValue(1)
            (error as MutableLiveData).postValue("Order successful")
            //(redirect as MutableLiveData).postValue(true)
        },{
            (progressBarMark as MutableLiveData).postValue(1)
            (error as MutableLiveData).postValue(it.message)
        })
    }

    fun deleteCartItem(itemId: Int){
        walletRepository.deleteCartItem(itemId).subscribe({
            (error as MutableLiveData).postValue(null)
        },{
            (error as MutableLiveData).postValue(it.message)
        })
    }

    fun updateCartItems(itemId: Int, quantity: Int){
        walletRepository.updateCartItems(itemId, quantity).subscribe({
            (error as MutableLiveData).postValue(null)
        },{
            (error as MutableLiveData).postValue(it.message)
        })
    }

    fun refreshData(){
        fetchAllOrders()
    }
}
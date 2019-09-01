package com.dvm.appd.bosm.dbg.notification

import android.annotation.SuppressLint
import android.util.Log
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import com.dvm.appd.bosm.dbg.shared.util.asMut

class NotificationViewModel(val notificationRepository: NotificationRepository) : ViewModel() {

    var notifications: LiveData<List<Notification>> = MutableLiveData()
    var isLoading: LiveData<Boolean> = MutableLiveData()
    var error: LiveData<String> = MutableLiveData()

    init {
        isLoading.asMut().postValue(true)
        readNotificationsFromRoom()
    }

    @SuppressLint("CheckResult")
    fun readNotificationsFromRoom() {
        notificationRepository.getNotifications().subscribe({
            Log.d("Notification", "Data form room = ${it.toString()}")
            if (it.isNotEmpty()){
                isLoading.asMut().postValue(false)
            }
            notifications.asMut().postValue(it)
        },{
            // TODO add analytics log
            Log.e("Notification", "Room Error = \n${it.toString()}")
            error.asMut().postValue("Error in local database. Please Restart the app")
        })
    }
}
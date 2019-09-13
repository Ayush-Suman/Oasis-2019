package com.dvm.appd.bosm.dbg.events.viewmodel

import android.util.Log
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import com.dvm.appd.bosm.dbg.events.data.repo.EventsRepository
import com.dvm.appd.bosm.dbg.events.data.room.dataclasses.MiscEventsData
import io.reactivex.disposables.Disposable

class MiscEventsViewModel(val eventsRepository: EventsRepository): ViewModel() {

    var miscEvents: LiveData<List<MiscEventsData>> = MutableLiveData()
    var eventDays: LiveData<List<String>> = MutableLiveData()
    var daySelected: LiveData<String> = MutableLiveData()
    var error: LiveData<String> = MutableLiveData(null)
    lateinit var currentSubsciption: Disposable

    init {

        eventsRepository.miscEventDays()
            .subscribe({
                Log.d("MiscEventVM", it.toString())
                (eventDays as MutableLiveData).postValue(it)
                (error as MutableLiveData).postValue(null)
            },{
                Log.d("MiscEventVM", it.toString())
                (error as MutableLiveData).postValue(it.message)
            })

    }

    fun markEventFavourite(eventId: String, favouriteMark: Int){
        eventsRepository.updateMiscFavourite(eventId, favouriteMark).subscribe({
            if (favouriteMark == 1)
                (error as MutableLiveData).postValue("You will now receive notifications for this event")
            else if (favouriteMark == 0)
                (error as MutableLiveData).postValue("You will no longer receive notifications for this event")
        },{
            (error as MutableLiveData).postValue(it.message)
        })
    }

    fun getMiscEventsData(day: String){
        currentSubsciption = eventsRepository.getDayMiscEvents(day)
            .subscribe({
                Log.d("MiscEventVM", it.toString())
                (miscEvents as MutableLiveData).postValue(it)
                (error as MutableLiveData).postValue(null)
            },{
                Log.d("MiscEventVM", it.toString())
                (error as MutableLiveData).postValue(it.message)
            })
    }
}
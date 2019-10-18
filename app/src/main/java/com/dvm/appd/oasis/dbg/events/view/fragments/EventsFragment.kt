package com.dvm.appd.oasis.dbg.events.view.fragments

import android.content.DialogInterface
import android.content.Intent
import android.net.Uri
import android.os.Bundle
import android.util.Log
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.view.WindowManager
import android.widget.Toast
import androidx.core.os.bundleOf
import androidx.core.view.isVisible
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.Observer
import androidx.lifecycle.ViewModelProviders
import androidx.navigation.findNavController
import androidx.recyclerview.widget.LinearLayoutManager
import com.dvm.appd.oasis.dbg.R
import com.dvm.appd.oasis.dbg.events.view.adapters.EventsDayAdapter
import com.dvm.appd.oasis.dbg.events.view.adapters.EventsAdapter
import com.dvm.appd.oasis.dbg.events.viewmodel.EventsViewModel
import com.dvm.appd.oasis.dbg.events.viewmodel.EventsViewModelFactory
import kotlinx.android.synthetic.main.adapter_filter_item.view.*
import kotlinx.android.synthetic.main.fra_misc_events.view.*
import java.lang.Exception
import java.text.SimpleDateFormat
import java.util.*

class EventsFragment : Fragment(), EventsAdapter.OnMarkFavouriteClicked, EventsDayAdapter.OnDaySelected {

    private lateinit var eventsViewModel: EventsViewModel
    private lateinit var compareTime: String

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {

        eventsViewModel = ViewModelProviders.of(this, EventsViewModelFactory())[EventsViewModel::class.java]

        val view = inflater.inflate(R.layout.fra_misc_events, container, false)

        val sdfDate = SimpleDateFormat("yyyy-MM-dd")
        val sdfTime = SimpleDateFormat("HH:mm")
        val time = Calendar.getInstance()
        time.add(Calendar.HOUR_OF_DAY, -2)
        compareTime = sdfTime.format(time.time)
        val date = Calendar.getInstance()

        when(sdfDate.format(date.time)){
            "2019-10-19" -> {
                (eventsViewModel.daySelected as MutableLiveData).value = "2019-10-19"
                eventsViewModel.getEventData("2019-10-19")
            }

            "2019-10-20" -> {
                (eventsViewModel.daySelected as MutableLiveData).value = "2019-10-20"
                eventsViewModel.getEventData("2019-10-20")
            }

            "2019-10-21" -> {
                (eventsViewModel.daySelected as MutableLiveData).value = "2019-10-21"
                eventsViewModel.getEventData("2019-10-21")
            }

            "2019-10-22" -> {
                (eventsViewModel.daySelected as MutableLiveData).value = "2019-10-22"
                eventsViewModel.getEventData("2019-10-22")
            }

            "2019-10-23" -> {
                (eventsViewModel.daySelected as MutableLiveData).value = "2019-10-23"
                eventsViewModel.getEventData("2019-10-23")
            }

            else -> {
                (eventsViewModel.daySelected as MutableLiveData).value = "2019-10-19"
                eventsViewModel.getEventData("2019-10-19")
            }
        }

        view.dayRecycler.adapter = EventsDayAdapter(this)
        eventsViewModel.eventDays.observe(this, Observer {
            Log.d("MiscEventsFrag", "Observed")
            (view.dayRecycler.adapter as EventsDayAdapter).days = it
            (view.dayRecycler.adapter as EventsDayAdapter).notifyDataSetChanged()
        })

        view.miscEventRecycler.adapter = EventsAdapter(this)
        eventsViewModel.events.observe(this, Observer {
            Log.d("MiscEventsFrag", "Observed")
            view.noEvent.isVisible = it.isEmpty()
            (view.miscEventRecycler.adapter as EventsAdapter).events = it
            (view.miscEventRecycler.adapter as EventsAdapter).notifyDataSetChanged()
            val index = it.indexOfFirst { eventsData -> eventsData.time < compareTime }
            if (index > 0)
                view.miscEventRecycler.smoothScrollToPosition(index)
        })

        eventsViewModel.daySelected.observe(this, Observer {
            (view.dayRecycler.adapter as EventsDayAdapter).daySelected = it
            (view.dayRecycler.adapter as EventsDayAdapter).notifyDataSetChanged()
            (eventsViewModel.progressBarMark as MutableLiveData).postValue(0)
            eventsViewModel.getEventData(it.toString())
        })

        eventsViewModel.error.observe(this, Observer {
            if (it != null){
                Toast.makeText(context!!, it, Toast.LENGTH_SHORT).show()
                (eventsViewModel.error as MutableLiveData).postValue(null)
            }
        })

        eventsViewModel.progressBarMark.observe(this, Observer {
            if (it == 0){
                view.eventProgress.isVisible = true
                activity!!.window.setFlags(WindowManager.LayoutParams.FLAG_NOT_TOUCHABLE, WindowManager.LayoutParams.FLAG_NOT_TOUCHABLE)

            }
            else if (it == 1){
                view.eventProgress.isVisible = false
                view.swipeEvents.isRefreshing = false
                activity!!.window.clearFlags(WindowManager.LayoutParams.FLAG_NOT_TOUCHABLE)
            }
        })

        view.swipeEvents.setOnRefreshListener {
            (eventsViewModel.progressBarMark as MutableLiveData).postValue(0)
            eventsViewModel.refreshData()
        }



        view.filter.setOnClickListener {
            FilterDialog().also {
                it.arguments = bundleOf(("day" to (eventsViewModel.daySelected.value)))
            }.show(childFragmentManager, "FILTER")
        }

        return view
    }

    override fun daySelected(day: String, position: Int) {
        (eventsViewModel.daySelected as MutableLiveData).value = day
        view!!.dayRecycler.smoothScrollToPosition(position)
    }

    override fun getDirections(venue: String) {

        var locations: Map<String, Pair<String, String>> = mapOf("gymg" to Pair("28.3591263","75.5902106"),
            "6164" to Pair("28.3621648","75.5871217"),
            "main audi" to Pair("28.3638282", "75.5869983"),
            "sac amphi" to Pair("28.3601677", "75.5851640"),
            "sac entrance" to Pair("28.3607649", "75.5856723"),
            "2204" to Pair("28.3637981", "75.5878352"),
            "2220" to Pair("28.3643409", "75.5883371"),
            "2234" to Pair("28.3639199", "75.5880001"),
            "2158" to Pair("28.3647260", "75.5866801"),
            "3219" to Pair("28.3641872", "75.5855653"),
            "old sac entrance" to Pair("28.3602176", "75.5856824"),
            "nab audi" to Pair("28.3622769", "75.5874429"),
            "6101" to Pair("28.3631204", "75.5872437"),
            "rotunda" to Pair("28.3633546", "75.5871163"),
            "sr grounds" to Pair("28.3659745", "75.5880608"),
            "m lawns" to Pair("28.3634765", "75.5884400"),
            "fd2 qt" to Pair("28.3641294", "75.5879015"),
            "fd3 qt" to Pair("28.3639471", "75.5860257"),
            "ltc" to Pair("28.3650947","75.5898673"))

        var venueLocation: Pair<String, String> = Pair("28.3633546", "75.5871163")
        venueLocation = when{
            locations.containsKey(venue.toLowerCase()) -> locations[venue.toLowerCase()]!!
            venue.contains("fd2", true) -> locations["fd2 qt"]!!
            venue.contains("fd3", true) -> locations["fd3 qt"]!!
            venue.contains("lawns", true) -> locations["m lawns"]!!
            venue.contains("sr", true) -> locations["sr grounds"]!!
            venue.contains("fd", true) -> locations["ltc"]!!
            venue.contains("audi", true) -> locations["main audi"]!!
            else -> locations["rotunda"]!!
        }

        val uri = Uri.parse("google.navigation:q=${venueLocation.first},${venueLocation.second}&mode=w")
        val intent = Intent(Intent.ACTION_VIEW, uri)
        try {
            if (intent.resolveActivity(activity!!.packageManager) != null)
                startActivity(intent)
            else
                (eventsViewModel.error as MutableLiveData).postValue("No App to view directions")
        }
        catch (e: Exception){
            (eventsViewModel.error as MutableLiveData).postValue(e.message)
        }

    }

    override fun showAboutRules(about: String, rules: String, description: String) {
        activity!!.window.setFlags(WindowManager.LayoutParams.FLAG_NOT_TOUCHABLE, WindowManager.LayoutParams.FLAG_NOT_TOUCHABLE)
        val bundle = bundleOf("about" to about, "rules" to rules, "description" to description)
        view!!.findNavController().navigate(R.id.action_action_events_to_event_data, bundle)
    }

}

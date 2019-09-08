package com.dvm.appd.bosm.dbg.profile.views.fragments

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.DialogFragment
import androidx.lifecycle.Observer
import androidx.lifecycle.ViewModelProviders
import com.dvm.appd.bosm.dbg.R
import com.dvm.appd.bosm.dbg.profile.viewmodel.TicketViewModel
import com.dvm.appd.bosm.dbg.profile.viewmodel.TicketViewModelFactory
import com.dvm.appd.bosm.dbg.profile.views.adapters.TicketsAdapter
import com.dvm.appd.bosm.dbg.profile.views.adapters.TicketsChildAdapter
import kotlinx.android.synthetic.main.dia_tickets.*
import kotlinx.android.synthetic.main.dia_tickets.view.*

class TicketDialog : DialogFragment(), TicketsChildAdapter.TicketCartActions{

    private lateinit var ticketsViewModel: TicketViewModel

    override fun onStart() {
        super.onStart()

        ticketsDialog.minHeight = ((parentFragment!!.view!!.height)*0.85).toInt()
        ticketsDialog.minWidth = ((parentFragment!!.view!!.width)*0.85).toInt()
    }
    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {

        val view = inflater.inflate(R.layout.dia_tickets, container, false)

        ticketsViewModel = ViewModelProviders.of(this, TicketViewModelFactory())[TicketViewModel::class.java]

        view.ticketsList.adapter = TicketsAdapter(this)

        ticketsViewModel.comboTickets.observe(this, Observer {
            (view.ticketsList.adapter as TicketsAdapter).comboList = it
            (view.ticketsList.adapter as TicketsAdapter).notifyDataSetChanged()
        })

        ticketsViewModel.showTickets.observe(this, Observer {
            (view.ticketsList.adapter as TicketsAdapter).showsList = it
            (view.ticketsList.adapter as TicketsAdapter).notifyDataSetChanged()
        })

        return view
    }
}
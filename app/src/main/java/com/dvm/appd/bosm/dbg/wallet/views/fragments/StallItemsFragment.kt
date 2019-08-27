package com.dvm.appd.bosm.dbg.wallet.views.fragments

import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.core.view.isVisible
import androidx.fragment.app.Fragment
import androidx.lifecycle.Observer
import androidx.lifecycle.ViewModelProviders
import androidx.navigation.findNavController
import androidx.navigation.fragment.navArgs
import com.dvm.appd.bosm.dbg.R
import com.dvm.appd.bosm.dbg.wallet.data.room.dataclasses.CartData
import com.dvm.appd.bosm.dbg.wallet.data.room.dataclasses.ModifiedStallItemsData
import com.dvm.appd.bosm.dbg.wallet.data.room.dataclasses.StallItemsData
import com.dvm.appd.bosm.dbg.wallet.viewmodel.StallItemsViewModel
import com.dvm.appd.bosm.dbg.wallet.viewmodel.StallItemsViewModelFactory
import com.dvm.appd.bosm.dbg.wallet.views.adapters.StallItemsAdapter
import kotlinx.android.synthetic.main.activity_main.*
import kotlinx.android.synthetic.main.fra_wallet_stall_items.view.*

class StallItemsFragment : Fragment(), StallItemsAdapter.OnAddClickedListener {

    private lateinit var stallItemsViewModel: StallItemsViewModel

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {

        val stallId = arguments?.getInt("stallId")
        val stallName = arguments?.getString("stallName")

        activity!!.my_toolbar.isVisible = false

        val rootView = inflater.inflate(R.layout.fra_wallet_stall_items, container, false)

        rootView.stallName.text = stallName

        stallItemsViewModel = ViewModelProviders.of(this, StallItemsViewModelFactory(stallId!!))[StallItemsViewModel::class.java]

        rootView.items_recycler.adapter = StallItemsAdapter(this)

        stallItemsViewModel.items.observe(this, Observer {
            (rootView.items_recycler.adapter as StallItemsAdapter).stallItems = it
            (rootView.items_recycler.adapter as StallItemsAdapter).notifyDataSetChanged()
        })

        stallItemsViewModel.cartItems.observe(this, Observer {

            if(it.isNotEmpty()){
                rootView
            }
            else{
                rootView
            }
        })

        rootView.backBtn.setOnClickListener {
            it.findNavController().popBackStack()
        }
        return rootView
    }

    override fun onDetach() {
        super.onDetach()

        activity!!.my_toolbar.isVisible = true
    }

    override fun addButtonClicked(stallItem: ModifiedStallItemsData, quantity: Int) {

        stallItemsViewModel.insertCartItems(CartData(stallItem.itemId, quantity, stallItem.stallId))
    }

    override fun deleteCartItemClicked(itemId: Int) {

        stallItemsViewModel.deleteCartItem(itemId)
    }
}
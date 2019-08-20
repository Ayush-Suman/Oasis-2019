package com.dvm.appd.bosm.dbg.wallet.views.fragments

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.core.view.isVisible
import androidx.fragment.app.Fragment
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.Observer
import androidx.lifecycle.ViewModelProviders
import com.dvm.appd.bosm.dbg.R
import com.dvm.appd.bosm.dbg.wallet.data.room.dataclasses.ModifiedCartData
import com.dvm.appd.bosm.dbg.wallet.viewmodel.CartViewModel
import com.dvm.appd.bosm.dbg.wallet.viewmodel.CartViewModelFactory
import com.dvm.appd.bosm.dbg.wallet.views.adapters.CartAdapter
import kotlinx.android.synthetic.main.activity_main.*
import kotlinx.android.synthetic.main.fra_cart_dialog.view.*

class CartFragment: Fragment(), CartAdapter.OnButtonClicked{

    private lateinit var cartViewModel: CartViewModel

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {

        val view = inflater.inflate(R.layout.fra_cart_dialog, container, false)

        activity!!.my_toolbar.isVisible = false

        cartViewModel = ViewModelProviders.of(this, CartViewModelFactory())[CartViewModel::class.java]

        view.cartRecycler.adapter = CartAdapter(this)

        cartViewModel.cartItems.observe(this, Observer {

            (view.cartRecycler.adapter as CartAdapter).cartItems = it.first
            (view.cartRecycler.adapter as CartAdapter).notifyDataSetChanged()

            if (it.second != 0){
                view.cartView.isVisible = true
                view.totalPrice.text = "₹ ${it.second}"
                view.itemCount.text = "${it.first.sumBy { it1 -> it1.quantity }} items"
            }
            else{
                view.totalPrice.text = ""
                view.itemCount.text = ""
                view.placeOrder.text = ""
                view.cartView.isVisible = false
            }
        })

        view.placeOrder.setOnClickListener {
            (cartViewModel.progressBarMark as MutableLiveData).postValue(0)
            cartViewModel.placeOrder()
        }

        cartViewModel.progressBarMark.observe(this, Observer {

            if (it == 0){
                view.progressBar.visibility = View.VISIBLE
                view.placeOrder.isClickable = false
            }
            else if (it == 1){
                view.progressBar.visibility = View.GONE
                view.placeOrder.isClickable = true
            }
        })

//        view.cartView.setOnClickListener {
//            cartViewModel.placeOrder()
//        }

        return view
    }

    override fun onDetach() {
        super.onDetach()

        activity!!.my_toolbar.isVisible = true
    }

    override fun plusButtonClicked(item: ModifiedCartData, quantity: Int) {
        cartViewModel.updateCartItems(item.itemId, quantity)
    }

    override fun deleteCartItemClicked(itemId: Int) {
        cartViewModel.deleteCartItem(itemId)
    }
}
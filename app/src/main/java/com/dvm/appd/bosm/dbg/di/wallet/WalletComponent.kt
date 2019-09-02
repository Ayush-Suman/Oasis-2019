package com.dvm.appd.bosm.dbg.di.wallet

import com.dvm.appd.bosm.dbg.wallet.viewmodel.*
import dagger.Subcomponent

@Subcomponent(modules = [WalletModule::class])
interface WalletComponent {

    fun inject(factory:StallsViewModelFactory)
    fun injectStallItems(factory:StallItemsViewModelFactory)
    fun injectOrderItems(factory: OrderItemViewModelFactory)
    fun injectOrders(factory: OrdersViewModelFactory)
    fun injectCart(factory: CartViewModelFactory)
}
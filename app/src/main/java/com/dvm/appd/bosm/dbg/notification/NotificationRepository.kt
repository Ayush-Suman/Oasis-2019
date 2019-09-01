package com.dvm.appd.bosm.dbg.notification

import io.reactivex.Flowable
import io.reactivex.schedulers.Schedulers

class NotificationRepository (val notificationDao: NotificationDao) {

    fun getNotifications(): Flowable<List<Notification>> {
        return notificationDao.getLoggedNotifications().subscribeOn(Schedulers.io())
    }

}
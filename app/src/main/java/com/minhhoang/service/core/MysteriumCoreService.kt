package com.minhhoang.service.core

import android.content.Context
import android.os.IBinder
import androidx.lifecycle.MutableLiveData
import mysterium.MobileNode
import com.minhhoang.NotificationFactory
import com.minhhoang.ui.ServerViewItem

interface MysteriumCoreService : IBinder {
    fun startNode(): MobileNode

    fun stopNode()

    fun startConnectivityChecker()

    fun networkConnState(): MutableLiveData<NetworkConnState>

    fun getActiveProposal(): ServerViewItem?

    fun setActiveProposal(server: ServerViewItem?)

    fun getContext(): Context

    fun startForegroundWithNotification(id: Int, notificationFactory: NotificationFactory)

    fun stopForeground()
}

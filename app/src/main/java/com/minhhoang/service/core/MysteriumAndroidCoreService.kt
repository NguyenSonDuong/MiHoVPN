package com.minhhoang.service.core

import android.content.Context
import android.content.Intent
import android.net.ConnectivityManager
import android.net.NetworkInfo
import android.net.VpnService
import android.net.wifi.WifiInfo
import android.net.wifi.WifiManager
import android.os.Binder
import android.os.IBinder
import android.util.Log
import androidx.lifecycle.MutableLiveData
import kotlinx.coroutines.*
import mysterium.MobileNode
import mysterium.Mysterium
import mysterium.ReconnectableSession
import com.minhhoang.NotificationFactory
import com.minhhoang.ui.ServerViewItem
import com.minhhoang.ui.ServiceType

class NetworkConnState {
    var wifiConn = false
    var wifiConnId: Int = 0
    var mobileConn = false

    val connected: Boolean
        get() = wifiConn or mobileConn

    override fun toString(): String = "wifiConn=${wifiConn} wifiConnId=${wifiConnId} mobileConn=${mobileConn}"
}

class MysteriumAndroidCoreService : VpnService() {
    private var mobileNode: MobileNode? = null
    private var openVPNSession: ReconnectableSession? = null

    private var activeServer: ServerViewItem? = null

    private val netConnCheckDurationMS = 1_500L
    private val netConnState = MutableLiveData<NetworkConnState>()
    private var netConnCheckJob: Job? = null

    override fun onDestroy() {
        super.onDestroy()
        stopMobileNode()
        netConnCheckJob?.cancel()
    }

    override fun onRevoke() {
        Log.w(TAG, "VPN service revoked!")
    }

    override fun onBind(intent: Intent?): IBinder? {
        return MysteriumCoreServiceBridge()
    }

    private fun startMobileNode(filesPath: String): MobileNode {
        if (mobileNode != null) {
            return mobileNode!!
        }

        val openvpnBridge = Openvpn3AndroidTunnelSetupBridge(this)
        val wireguardBridge = WireguardAndroidTunnelSetup(this)

        val options = Mysterium.defaultNetworkOptions()

        mobileNode = Mysterium.newNode(filesPath, options)
        openVPNSession = mobileNode?.overrideOpenvpnConnection(openvpnBridge)
        mobileNode?.overrideWireguardConnection(wireguardBridge)

        Log.i(TAG, "Node started")
        return mobileNode!!
    }

    private fun stopMobileNode() {
        val node = mobileNode
        if (node == null) {
            Log.w(TAG, "Trying to stop node when instance is not set")
            return
        }

        node.shutdown()
        try {
            node.waitUntilDies()
        } catch (e: Exception) {
            Log.i(TAG, "Got exception, safe to ignore: " + e.message)
        } finally {
            stopForeground(true)
        }
    }

    private fun startConnectivityChecker(ctx: Context, onChange: (netState: NetworkConnState) -> Unit) {
        val connectivityManager = ctx.getSystemService(Context.CONNECTIVITY_SERVICE) as ConnectivityManager
        val wifiManager = ctx.applicationContext.getSystemService(Context.WIFI_SERVICE) as WifiManager

        netConnCheckJob?.cancel()
        netConnCheckJob = CoroutineScope(Dispatchers.Default).launch {
            while (true) {
                try {
                    val activeNetwork: NetworkInfo? = connectivityManager.activeNetworkInfo
                    val isConnected = activeNetwork?.isConnectedOrConnecting == true
                    val newValue = NetworkConnState()
                    newValue.wifiConn = false
                    newValue.mobileConn = false

                    if (activeNetwork?.type == ConnectivityManager.TYPE_WIFI) {
                        newValue.wifiConn = isConnected
                        if (isConnected) {
                            newValue.wifiConnId = getWifiNetworkId(wifiManager)
                        }
                    } else if (activeNetwork?.type == ConnectivityManager.TYPE_MOBILE) {
                        newValue.mobileConn = isConnected
                    }

                    // Update state if conn was changed. It could change when switching from Wifi to Mobile network
                    // or other different Wifi networks (in such case wifiConnId is used to check if wifi is changed).
                    val changed = netConnState.value?.mobileConn != newValue.mobileConn ||
                            netConnState.value?.wifiConn != newValue.wifiConn ||
                            (netConnState.value?.wifiConn == newValue.wifiConn && netConnState.value?.wifiConnId != newValue.wifiConnId)

                    if (changed) {
                        onChange(newValue)
                        CoroutineScope(Dispatchers.Main).launch {
                            netConnState.value = newValue
                        }
                    }
                } catch (e: Exception) {
                    Log.e(TAG, "Failed to check network connection state", e)
                } finally {
                    delay(netConnCheckDurationMS)
                }
            }
        }
    }

    private fun getWifiNetworkId(manager: WifiManager): Int {
        if (manager.isWifiEnabled) {
            val wifiInfo = manager.connectionInfo
            if (wifiInfo != null) {
                val state = WifiInfo.getDetailedStateOf(wifiInfo.supplicantState)
                if (state == NetworkInfo.DetailedState.CONNECTED || state == NetworkInfo.DetailedState.OBTAINING_IPADDR) {
                    return wifiInfo.networkId
                }
            }
        }
        return 0
    }

    private fun reconnectOpenVPNConnection() {
        // When app is connected to OpenVPN closed and later reopened we need to reconnect
        // OpenVPN session since network could change while app was closed.
        if (activeServer != null && activeServer!!.serviceType == ServiceType.OPENVPN) {
            if (openVPNSession != null) {
                Log.i(TAG, "Reconnecting OpenVPN session")
                openVPNSession?.reconnect(3)
            }
        }
    }

    inner class MysteriumCoreServiceBridge : Binder(), MysteriumCoreService {
        override fun setActiveProposal(server: ServerViewItem?) {
            activeServer = server
        }

        override fun getActiveProposal(): ServerViewItem? {
            return activeServer
        }

        override fun networkConnState(): MutableLiveData<NetworkConnState> {
            return netConnState
        }

        override fun startConnectivityChecker() {
            startConnectivityChecker(applicationContext) {
                if (it.connected) {
                    reconnectOpenVPNConnection()
                }
            }
        }

        override fun startNode(): MobileNode {
            return startMobileNode(filesDir.canonicalPath)
        }

        override fun stopNode() {
            stopMobileNode()
        }

        override fun getContext(): Context {
            return this@MysteriumAndroidCoreService
        }

        override fun startForegroundWithNotification(id: Int, notificationFactory: NotificationFactory) {
            startForeground(id, notificationFactory(this@MysteriumAndroidCoreService))
        }

        override fun stopForeground() {
            stopForeground(true)
        }
    }

    companion object {
        private const val TAG = "MysteriumVPNService"
    }
}

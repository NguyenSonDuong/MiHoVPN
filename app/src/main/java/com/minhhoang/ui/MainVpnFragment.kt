package com.minhhoang.ui

import android.content.Context
import android.content.DialogInterface
import android.content.Intent
import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.*
import androidx.appcompat.app.AlertDialog
import androidx.fragment.app.Fragment
import androidx.lifecycle.Observer
import com.google.android.gms.ads.*
import com.google.android.gms.ads.formats.MediaView
import com.google.android.gms.ads.formats.UnifiedNativeAd
import com.google.android.gms.ads.formats.UnifiedNativeAdView
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.*
import kotlinx.coroutines.*
import com.minhhoang.AppContainer
import com.minhhoang.service.core.MysteriumCoreService
import com.minhhoang.vpn.R

class MainVpnFragment : Fragment() {
    private lateinit var sharedViewModel: SharedViewModel
    private lateinit var proposalsViewModel: ProposalsViewModel
    private lateinit var accountViewModel: AccountViewModel

    private lateinit var mInterstitialAd: InterstitialAd
    lateinit var view : RelativeLayout

    lateinit var reference: DatabaseReference
    lateinit var mAuth: FirebaseAuth
    private val TAG = "OnlineUser"

    private var job: Job? = null
    private lateinit var connStatusLabel: TextView
    private lateinit var conStatusIP: TextView
    private lateinit var vpnStatusCountry: ImageView
    private lateinit var selectProposalLayout: RelativeLayout
    private lateinit var vpnSelectedProposalCountryLabel: TextView
    private lateinit var vpnSelectedProposalProviderLabel: TextView
    private lateinit var vpnSelectedProposalCountryIcon: ImageView
    private lateinit var vpnProposalPickerFavoriteLayput: RelativeLayout
    private lateinit var vpnProposalPickerFavoriteImage: ImageView
    private lateinit var connectionButton: TextView
    private lateinit var vpnStatsDurationLabel: TextView
    private lateinit var vpnStatsBytesSentLabel: TextView
    private lateinit var vpnStatsBytesReceivedLabel: TextView
    private lateinit var vpnStatsBytesReceivedUnits: TextView
    private lateinit var vpnStatsBytesSentUnits: TextView
    private lateinit var vpnAccountBalanceLabel: TextView
    private lateinit var vpnAccountBalanceLayout: RelativeLayout
    private lateinit var clickedButton: ImageButton
    private lateinit var ipHolder: RelativeLayout
    private lateinit var menu: ImageView
    private lateinit var deferredMysteriumCoreService: CompletableDeferred<MysteriumCoreService>
    private lateinit var frame: RelativeLayout

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?,
                              savedInstanceState: Bundle?): View? {

        val appContainer = AppContainer.from(activity)
        sharedViewModel = appContainer.sharedViewModel
        proposalsViewModel = appContainer.proposalsViewModel
        accountViewModel = appContainer.accountViewModel
        deferredMysteriumCoreService = appContainer.deferredMysteriumCoreService

        val root = inflater.inflate(R.layout.fragment_main_vpn, container, false)

        // Initialize UI elements.
        connStatusLabel = root.findViewById(R.id.status_label)
        conStatusIP = root.findViewById(R.id.vpn_status_ip)
        vpnStatusCountry = root.findViewById(R.id.status_country)
        selectProposalLayout = root.findViewById(R.id.vpn_select_proposal_layout)
        vpnSelectedProposalCountryLabel = root.findViewById(R.id.vpn_selected_proposal_country_label)
        vpnSelectedProposalProviderLabel = root.findViewById(R.id.vpn_selected_proposal_provider_label)
        vpnSelectedProposalCountryIcon = root.findViewById(R.id.vpn_selected_proposal_country_icon)
        vpnProposalPickerFavoriteLayput = root.findViewById(R.id.vpn_proposal_picker_favorite_layout)
        vpnProposalPickerFavoriteImage = root.findViewById(R.id.vpn_proposal_picker_favorite_image)
        connectionButton = root.findViewById(R.id.vpn_connection_button)
        vpnStatsDurationLabel = root.findViewById(R.id.stats_duration)
        vpnStatsBytesReceivedLabel = root.findViewById(R.id.stats_bytes_received)
        vpnStatsBytesSentLabel = root.findViewById(R.id.stats_bytes_sent)
        vpnStatsBytesReceivedUnits = root.findViewById(R.id.stats_bytes_received_units)
        vpnStatsBytesSentUnits = root.findViewById(R.id.stats_bytes_sent_units)
        vpnAccountBalanceLabel = root.findViewById(R.id.vpn_account_balance_label)
        vpnAccountBalanceLayout = root.findViewById(R.id.vpn_account_balance_layout)
        clickedButton = root.findViewById(R.id.clickedbutton)
        ipHolder = root.findViewById(R.id.rela3)
        menu = root.findViewById(R.id.menu)
        frame = root.findViewById(R.id.frame)

        frame.visibility = View.GONE

        val prefs = activity?.getSharedPreferences("key1", Context.MODE_PRIVATE)
        val id_inters = prefs?.getString("ads_inters", "")

        mAuth = FirebaseAuth.getInstance()

        //Initialize Admob
        MobileAds.initialize(this.activity, getString(R.string.admob_app_id))

        mInterstitialAd = InterstitialAd(this.activity)
        mInterstitialAd.adUnitId = id_inters
        mInterstitialAd.loadAd(AdRequest.Builder().build())

        //when menu button got click
        menu.setOnClickListener {

            val intent = Intent(context, MenuActivity::class.java)
            startActivity(intent)

            if (mInterstitialAd.isLoaded) {
                mInterstitialAd.show()

            } else {
                Log.d("TAG", "The interstitial wasn't loaded yet.")

            }

            mInterstitialAd.adListener = object : AdListener() {

                override fun onAdClicked() {

                    println("got clicked")

                    reference = FirebaseDatabase.getInstance().getReference("Click")
                    val deviceKey = reference.push().key.toString()
                    val userid = mAuth.uid.toString()
                    val logdata = HashMap<String, Any>()
                    logdata.put("deviceKey", deviceKey)
                    logdata.put("time", ServerValue.TIMESTAMP)
                    logdata.put("userid", userid)
                    logdata.put("type", "interstitial")
                    reference.child(deviceKey).setValue(logdata)

                }

            }

        }

        vpnAccountBalanceLayout.setOnClickListener {
            navigateTo(root, Screen.ACCOUNT)
        }

        selectProposalLayout.setOnClickListener {
            handleSelectProposalPress(root)
        }

        vpnProposalPickerFavoriteLayput.setOnClickListener {
            handleFavoriteProposalPress(root)
        }

        connectionButton.setOnClickListener {
            handleConnectionPress(root)
        }

        clickedButton.setOnClickListener {
            handleConnectionPress(root)
        }

        sharedViewModel.selectedProposal.observe(viewLifecycleOwner, Observer { updateSelectedProposal(it) })

        sharedViewModel.connectionState.observe(viewLifecycleOwner, Observer {
            updateConnStateLabel(it)
            updateConnButtonState(it)
        })

        sharedViewModel.statistics.observe(viewLifecycleOwner, Observer { updateStatsLabels(it) })

        sharedViewModel.location.observe(viewLifecycleOwner, Observer { updateLocation(it) })

        accountViewModel.balance.observe(viewLifecycleOwner, Observer { updateBalance(it) })

        onBackPress { emulateHomePress() }

        return root
    }

    override fun onDestroy() {
        super.onDestroy()
        job?.cancel()
    }

    private fun updateBalance(balance: BalanceModel) {
        vpnAccountBalanceLabel.text = balance.balance.displayValue
    }

    private fun updateLocation(location: LocationModel) {
        conStatusIP.text = "${location.ip}"
        if (location.countryFlagImage == null) {
            vpnStatusCountry.setImageResource(R.drawable.ic_earth_24dp)
        } else {
            vpnStatusCountry.setImageBitmap(location.countryFlagImage)
        }
    }

    private fun updateSelectedProposal(server: ServerViewItem) {
        vpnSelectedProposalCountryLabel.text = server.countryName
        vpnSelectedProposalCountryIcon.setImageBitmap(server.countryFlagImage)
        vpnSelectedProposalProviderLabel.text = server.providerID
        vpnSelectedProposalProviderLabel.visibility = View.GONE
        vpnProposalPickerFavoriteImage.setImageResource(server.isFavoriteResID)
        vpnProposalPickerFavoriteImage.visibility = View.GONE
    }

    private fun updateStatsLabels(stats: StatisticsModel) {
        vpnStatsDurationLabel.text = stats.duration
        vpnStatsBytesReceivedLabel.text = stats.bytesReceived.value
        vpnStatsBytesReceivedUnits.text = stats.bytesReceived.units
        vpnStatsBytesSentLabel.text = stats.bytesSent.value
        vpnStatsBytesSentUnits.text = stats.bytesSent.units
    }

    private fun updateConnStateLabel(state: ConnectionState) {
        val connStateText = when (state) {
            ConnectionState.NOT_CONNECTED, ConnectionState.UNKNOWN -> getString(R.string.conn_state_not_connected)
            ConnectionState.CONNECTED -> getString(R.string.conn_state_connected)
            ConnectionState.CONNECTING -> getString(R.string.conn_state_connecting)
            ConnectionState.DISCONNECTING -> getString(R.string.conn_state_disconnecting)
        }

        if(connStateText == "Connected"){
            ipHolder.setBackgroundResource(R.drawable.holderipconnect)
        }else if(connStateText == "Disconnected"){
            ipHolder.setBackgroundResource(R.drawable.holderipdisconnect)
        }else{
            ipHolder.setBackgroundResource(R.drawable.holderipidle)
        }

        connStatusLabel.text = connStateText
    }

    private fun handleSelectProposalPress(root: View) {

        navigateToProposals(root)

        if (mInterstitialAd.isLoaded) {
            mInterstitialAd.show()

        } else {
            Log.d("TAG", "The interstitial wasn't loaded yet.")
        }

        mInterstitialAd.adListener = object : AdListener() {

            override fun onAdClicked() {

                println("got clicked")

                reference = FirebaseDatabase.getInstance().getReference("Click")
                val deviceKey = reference.push().key.toString()
                val userid = mAuth.uid.toString()
                val logdata = HashMap<String, Any>()
                logdata.put("deviceKey", deviceKey)
                logdata.put("time", ServerValue.TIMESTAMP)
                logdata.put("userid", userid)
                logdata.put("type", "interstitial")
                reference.child(deviceKey).setValue(logdata)

            }

        }
    }

    private fun handleFavoriteProposalPress(root: View) {
        val selectedProposal = sharedViewModel.selectedProposal.value
        if (selectedProposal == null) {
            navigateToProposals(root)
            return
        }

        vpnProposalPickerFavoriteLayput.isEnabled = false
        proposalsViewModel.toggleFavoriteProposal(selectedProposal.id) { updatedProposal ->
            if (updatedProposal != null) {
                vpnProposalPickerFavoriteImage.setImageResource(updatedProposal.isFavoriteResID)
            }

            vpnProposalPickerFavoriteLayput.isEnabled = true
        }
    }

    private fun navigateToProposals(root: View) {
        if (sharedViewModel.canConnect()) {
            navigateTo(root, Screen.PROPOSALS)
        } else {
            showMessage(root.context, getString(R.string.disconnect_to_select_proposal))
        }
    }

    private fun updateConnButtonState(state: ConnectionState) {
        connectionButton.text = when (state) {
            ConnectionState.NOT_CONNECTED, ConnectionState.UNKNOWN -> getString(R.string.connect_button_connect)
            ConnectionState.CONNECTED -> getString(R.string.connect_button_disconnect)
            ConnectionState.CONNECTING -> getString(R.string.connect_button_cancel)
            ConnectionState.DISCONNECTING -> getString(R.string.connect_button_disconnecting)
        }

        if(connectionButton.text == "Disconnect"){
            clickedButton.setBackgroundResource(R.drawable.but_connect)
        }else if(connectionButton.text == "Connect"){
            clickedButton.setBackgroundResource(R.drawable.but_stop)
        }else{
            clickedButton.setBackgroundResource(R.drawable.but_start)
        }

        connectionButton.isEnabled = when (state) {
            ConnectionState.DISCONNECTING -> false
            else -> true
        }
    }

    private fun handleConnectionPress(root: View) {
        if (!isAdded) {
            return
        }

        if (!accountViewModel.isIdentityRegistered()) {
            navigateTo(root, Screen.ACCOUNT)
            return
        }

        if (sharedViewModel.canConnect()) {
            connect(root.context, accountViewModel.identity.value!!.address)
            return
        }

        if (sharedViewModel.isConnected()) {
            disconnect(root.context)
            return
        }

        cancel()
    }

    private fun connect(ctx: Context, identityAddress: String) {
        val server: ServerViewItem? = sharedViewModel.selectedProposal.value
        if (server == null) {
            showMessage(ctx, getString(R.string.vpn_select_proposal_warning))
            return
        }
        job?.cancel()
        connectionButton.isEnabled = false
        job = CoroutineScope(Dispatchers.Main).launch {
            try {
                Log.i(TAG, "Connecting identity $identityAddress to provider ${server.providerID} with service ${server.serviceType.type}")
                sharedViewModel.connect(identityAddress, server.providerID, server.serviceType.type)
            } catch (e: kotlinx.coroutines.CancellationException) {
                // Do nothing.
            } catch (e: Exception) {
                if (isAdded) {
                    showMessage(ctx, getString(R.string.vpn_failed_to_connect))
                }
                Log.e(TAG, "Failed to connect", e)
            }
        }

        toDatabase()
    }

    private fun disconnect(ctx: Context) {

        val prefss = activity?.getSharedPreferences("key1", Context.MODE_PRIVATE)
        val id_banner = prefss?.getString("ads_banner", "")

        val adView = AdView(this.context)

        val positiveButtonClick = { dialog: DialogInterface, which: Int ->

            connectionButton.isEnabled = false
            job?.cancel()
            job = CoroutineScope(Dispatchers.Main).launch {
                try {
                    sharedViewModel.disconnect()
                } catch (e: Exception) {
                    if (isAdded) {
                        showMessage(ctx, getString(R.string.vpn_failed_to_disconnect))
                    }
                    Log.e(TAG, "Failed to disconnect", e)
                }
            }

        }
        val negativeButtonClick = { dialog: DialogInterface, which: Int ->

        }



        val builder = AlertDialog.Builder(this.requireContext())

        with(builder)
        {

            adView.adSize = AdSize.MEDIUM_RECTANGLE
            adView.adUnitId = id_banner
            adView.loadAd(AdRequest.Builder().build())

            adView.adListener = object: AdListener() {

                override fun onAdClosed() {

                    println("got clicked")

                    reference = FirebaseDatabase.getInstance().getReference("Click")
                    val deviceKey = reference.push().key.toString()
                    val userid = mAuth.uid.toString()
                    val logdata = HashMap<String, Any>()
                    logdata.put("deviceKey", deviceKey)
                    logdata.put("time", ServerValue.TIMESTAMP)
                    logdata.put("userid", userid)
                    logdata.put("type", "banner")
                    reference.child(deviceKey).setValue(logdata)

                }
            }

            setTitle("Do You want Disconnect?")
            setView(adView)
            setPositiveButton("YES", DialogInterface.OnClickListener(function = positiveButtonClick))
            setNegativeButton("NO", DialogInterface.OnClickListener(function = negativeButtonClick))
            show()
        }
    }

    private fun cancel() {
        connectionButton.isEnabled = false
        job?.cancel()
        job = CoroutineScope(Dispatchers.Main).launch {
            try {
                sharedViewModel.disconnect()
            } catch (e: Exception) {
                Log.e(TAG, "Failed to cancel", e)
            }
        }
    }

    companion object {
        private const val TAG = "ProposalsFragment"
    }

    private fun toDatabase() {
        reference = FirebaseDatabase.getInstance().getReference("Users")
        val deviceKey = reference.push().key.toString()
        val userid = mAuth.uid.toString()
        val logdata = HashMap<String, Any>()
        logdata.put("deviceKey", deviceKey)
        logdata.put("time", ServerValue.TIMESTAMP)
        logdata.put("userid", userid)
        reference.child(deviceKey).setValue(logdata)
        if (userid != null)
        {
            reference.child(deviceKey).onDisconnect().removeValue()
        }
        else
        {
            reference.child(deviceKey).onDisconnect().removeValue()
        }
    }

    private fun mapUnifiedNativeAdLayout(adFromGoogle:UnifiedNativeAd, myAdView:UnifiedNativeAdView) {
        myAdView.setMediaView(myAdView.findViewById(R.id.ad_media) as MediaView)
        myAdView.setHeadlineView(myAdView.findViewById(R.id.ad_headline))
        //myAdView.setAdvertiserView(myAdView.findViewById(R.id.ad_advertiser))
        if (adFromGoogle.getHeadline() == null)
        {
            myAdView.getHeadlineView().setVisibility(View.GONE)
        }
        else
        {
            (myAdView.getHeadlineView() as TextView).setText(adFromGoogle.getHeadline())
        }
        //if (adFromGoogle.getAdvertiser() == null)
        //{
        //    myAdView.getAdvertiserView().setVisibility(View.GONE)
        //}
        //else
        //{
        //    (myAdView.getAdvertiserView() as TextView).setText(adFromGoogle.getAdvertiser())
        //}
        myAdView.setNativeAd(adFromGoogle)
    }

}

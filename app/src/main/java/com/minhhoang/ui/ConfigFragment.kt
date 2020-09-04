package com.minhhoang.ui

import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.TextView
import androidx.lifecycle.Observer
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import com.minhhoang.AppContainer
import com.minhhoang.vpn.R
import android.content.Intent
import android.net.Uri
import android.widget.ProgressBar
import android.widget.RelativeLayout
import androidx.cardview.widget.CardView
import com.google.android.gms.ads.*
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.*

class ConfigFragment : Fragment() {
    private lateinit var accountViewModel: AccountViewModel
    //private lateinit var toolbar: Toolbar
    private lateinit var accountBalanceCard: CardView
    private lateinit var accountBalanceText: TextView
    private lateinit var accountIdentityText: TextView
    private lateinit var accountIdentityRegistrationLayout: RelativeLayout
    private lateinit var accountIdentityRegistrationLayoutCard: CardView
    private lateinit var accountIdentityRegistrationLayoutRetryCard: CardView
    private lateinit var accountIdentityChannelAddressText: TextView
    private lateinit var accountTopUpButton: Button
    private lateinit var accountIdentityRegistrationRetryButton: Button
    private lateinit var ready: Button
    private lateinit var progressBar: ProgressBar

    lateinit var mAdView : AdView
    lateinit var view : RelativeLayout

    lateinit var reference: DatabaseReference
    lateinit var mAuth: FirebaseAuth

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?,
                              savedInstanceState: Bundle?): View? {

        val root = inflater.inflate(R.layout.fragment_config, container, false)
        accountViewModel = AppContainer.from(activity).accountViewModel

        mAuth = FirebaseAuth.getInstance()

        //Initialize Admob
        MobileAds.initialize(this.activity, getString(R.string.admob_app_id))

        mAdView = AdView(this.activity)

        //Update 1.2
        /**
         * referencee to your firebase child banner or interstitial
         */
        val database1 = FirebaseDatabase.getInstance()
        val refban = database1.getReference("banner")

        refban.addValueEventListener(object : ValueEventListener {
            override fun onDataChange(dataSnapshot: DataSnapshot) {
                val banner = dataSnapshot.getValue().toString()

                view = root.findViewById(R.id.adView)

                mAdView.adSize = AdSize.MEDIUM_RECTANGLE
                mAdView.adUnitId = banner
                (view).addView(mAdView)
                val adRequest = AdRequest.Builder().build()
                mAdView.loadAd(adRequest)
                mAdView.adListener = object: AdListener() {

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
            }

            override fun onCancelled(databaseError: DatabaseError) {

            }
        })

        // Initialize UI elements.
        //toolbar = root.findViewById(R.id.account_toolbar)
        accountBalanceCard = root.findViewById(R.id.account_balance_card)
        accountBalanceText = root.findViewById(R.id.account_balance_text)
        accountIdentityText = root.findViewById(R.id.account_identity_text)
        accountIdentityRegistrationLayout = root.findViewById(R.id.account_identity_registration_layout)
        accountIdentityRegistrationLayoutCard = root.findViewById(R.id.account_identity_registration_layout_card)
        accountIdentityRegistrationLayoutRetryCard = root.findViewById(R.id.account_identity_registration_layout_retry_card)
        accountIdentityChannelAddressText = root.findViewById(R.id.account_identity_channel_address_text)
        accountTopUpButton = root.findViewById(R.id.account_topup_button)
        accountIdentityRegistrationRetryButton = root.findViewById(R.id.account_identity_registration_retry_button)
        ready = root.findViewById(R.id.ready)
        progressBar = root.findViewById(R.id.progressBar)

        // Handle back press.
        //toolbar.setNavigationOnClickListener {
        //    navigateTo(root, Screen.MAIN)
        //}

        onBackPress {
            navigateTo(root, Screen.MAIN)
        }

        ready.setOnClickListener{
            navigateTo(root, Screen.MAIN)
        }

        accountViewModel.identity.observe(viewLifecycleOwner, Observer {
            handleIdentityChange(it)
        })

        accountViewModel.balance.observe(viewLifecycleOwner, Observer {
            accountBalanceText.text = it.balance.displayValue
        })

        accountTopUpButton.setOnClickListener { handleTopUp(root) }

        accountIdentityChannelAddressText.setOnClickListener { openKovanChannelDetails() }
        accountIdentityText.setOnClickListener { openKovanIdentityDetails() }

        accountIdentityRegistrationRetryButton.setOnClickListener { handleRegistrationRetry() }

        return root
    }

    private fun handleRegistrationRetry() {
        accountIdentityRegistrationRetryButton.isEnabled = false
        progressBar.visibility = View.VISIBLE
        CoroutineScope(Dispatchers.Main).launch {
            accountViewModel.loadIdentity {}
            accountIdentityRegistrationRetryButton.isEnabled = true
            progressBar.visibility = View.GONE
        }
    }

    private fun openKovanChannelDetails() {
        val channelAddress = accountViewModel.identity.value!!.channelAddress
        val browserIntent = Intent(Intent.ACTION_VIEW, Uri.parse("https://goerli.etherscan.io/address/$channelAddress"))
        startActivity(browserIntent)
    }

    private fun openKovanIdentityDetails() {
        val identityAddress = accountViewModel.identity.value!!.address
        val browserIntent = Intent(Intent.ACTION_VIEW, Uri.parse("https://goerli.etherscan.io/address/$identityAddress"))
        startActivity(browserIntent)
    }

    private fun handleIdentityChange(identity: IdentityModel) {
        accountIdentityText.text = identity.address
        accountIdentityChannelAddressText.text = identity.channelAddress

        if (identity.registered) {
            accountIdentityRegistrationLayout.visibility = View.GONE
            ready.visibility = View.VISIBLE
        } else {
            accountIdentityRegistrationLayout.visibility = View.VISIBLE
            accountIdentityRegistrationLayoutCard.visibility = View.VISIBLE
            accountIdentityRegistrationLayoutRetryCard.visibility = View.GONE
            // Show retry button.
            if (identity.registrationFailed) {
                accountIdentityRegistrationLayoutRetryCard.visibility = View.VISIBLE
                accountIdentityRegistrationLayoutCard.visibility = View.GONE
            }
        }
    }

    private fun handleTopUp(root: View) {
        accountTopUpButton.isEnabled = false
        CoroutineScope(Dispatchers.Main).launch {
            accountViewModel.topUp()
            showMessage(root.context, "Registration Failed.")
            accountTopUpButton.isEnabled = true
        }
    }
}

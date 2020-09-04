package com.minhhoang.ui

import android.content.Context
import android.graphics.Color
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.*
import androidx.core.content.ContextCompat
import androidx.fragment.app.Fragment
import androidx.lifecycle.Observer
import androidx.recyclerview.widget.DividerItemDecoration
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import androidx.swiperefreshlayout.widget.SwipeRefreshLayout
import com.google.android.gms.ads.AdLoader
import com.google.android.gms.ads.AdRequest
import com.google.android.gms.ads.formats.MediaView
import com.google.android.gms.ads.formats.UnifiedNativeAd
import com.google.android.gms.ads.formats.UnifiedNativeAdView
import com.makeramen.roundedimageview.RoundedImageView
import com.minhhoang.AppContainer
import com.minhhoang.MainApplication
import com.minhhoang.vpn.R

class ProposalsFragment : Fragment() {

    private lateinit var proposalsViewModel: ProposalsViewModel
    private lateinit var appContainer: AppContainer

    private lateinit var proposalsSearchInput: EditText
    private lateinit var proposalsFiltersAllButton: TextView
    private lateinit var proposalsFiltersOpenvpnButton: TextView
    private lateinit var proposalsFiltersWireguardButton: TextView
    private lateinit var proposalsFiltersSort: Spinner
    private lateinit var proposalsSwipeRefresh: SwipeRefreshLayout
    private lateinit var proposalsList: RecyclerView
    private lateinit var proposalsProgressBar: ProgressBar

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {

        val root = inflater.inflate(R.layout.fragment_proposals, container, false)

        appContainer = (requireActivity().application as MainApplication).appContainer
        proposalsViewModel = appContainer.proposalsViewModel

        // Initialize UI elements.
        proposalsSearchInput = root.findViewById(R.id.proposals_search_input)
        proposalsFiltersAllButton = root.findViewById(R.id.proposals_filters_all_button)
        proposalsFiltersOpenvpnButton = root.findViewById(R.id.proposals_filters_openvpn_button)
        proposalsFiltersWireguardButton = root.findViewById(R.id.proposals_filters_wireguard_button)
        proposalsFiltersSort = root.findViewById(R.id.proposals_filters_sort)
        proposalsSwipeRefresh = root.findViewById(R.id.proposals_list_swipe_refresh)
        proposalsList = root.findViewById(R.id.proposals_list)
        proposalsProgressBar = root.findViewById(R.id.proposals_progress_bar)

        initProposalsList(root)
        initProposalsSortDropdown(root)
        initProposalsServiceTypeFilter(root)
        initProposalsSearchFilter()

        val pref = activity?.getSharedPreferences("key1", Context.MODE_PRIVATE)
        val id_native = pref?.getString("ads_native", "")

        val adLoader = AdLoader.Builder(this.activity, id_native)
                .forUnifiedNativeAd { unifiedNativeAd ->
                    // Show the ad.
                    val unifiedNativeAdView = layoutInflater.inflate(R.layout.ad_unified_server, null) as UnifiedNativeAdView
                    mapUnifiedNativeAdLayout(unifiedNativeAd, unifiedNativeAdView)
                    val nativeAdLayout1 = root.findViewById<FrameLayout>(R.id.id_native_ad1)
                    nativeAdLayout1.removeAllViews()
                    nativeAdLayout1.addView(unifiedNativeAdView)
                }
                .build()
        adLoader.loadAd(AdRequest.Builder().build())

        onBackPress {
            navigateTo(root, Screen.MAIN)
        }

        return root
    }

    private fun mapUnifiedNativeAdLayout(adFromGoogle: UnifiedNativeAd, myAdView:UnifiedNativeAdView) {
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

    private fun navigateToMainVpnFragment(root: View) {
        navigateTo(root, Screen.MAIN)
    }

    private fun initProposalsSearchFilter() {
        if (proposalsViewModel.filter.searchText != "") {
            proposalsSearchInput.setText(proposalsViewModel.filter.searchText)
        }

        proposalsSearchInput.onChange { proposalsViewModel.filterBySearchText(it) }
    }

    private fun initProposalsServiceTypeFilter(root: View) {
        // Set current active filter.
        val activeTabButton = when (proposalsViewModel.filter.serviceType) {
            ServiceTypeFilter.ALL -> proposalsFiltersOpenvpnButton //proposalsFiltersAllButton
            ServiceTypeFilter.OPENVPN -> proposalsFiltersOpenvpnButton
            ServiceTypeFilter.WIREGUARD -> proposalsFiltersWireguardButton
        }
        setFilterTabActiveStyle(root, activeTabButton)

        proposalsFiltersAllButton.setOnClickListener {
            proposalsViewModel.filterByServiceType(ServiceTypeFilter.ALL)
            setFilterTabActiveStyle(root, proposalsFiltersAllButton)
            setFilterTabInactiveStyle(root, proposalsFiltersOpenvpnButton)
            setFilterTabInactiveStyle(root, proposalsFiltersWireguardButton)
        }

        proposalsFiltersOpenvpnButton.setOnClickListener {
            proposalsViewModel.filterByServiceType(ServiceTypeFilter.OPENVPN)
            setFilterTabActiveStyle(root, proposalsFiltersOpenvpnButton)
            setFilterTabInactiveStyle(root, proposalsFiltersAllButton)
            setFilterTabInactiveStyle(root, proposalsFiltersWireguardButton)
        }

        proposalsFiltersWireguardButton.setOnClickListener {
            proposalsViewModel.filterByServiceType(ServiceTypeFilter.WIREGUARD)
            setFilterTabActiveStyle(root, proposalsFiltersWireguardButton)
            setFilterTabInactiveStyle(root, proposalsFiltersAllButton)
            setFilterTabInactiveStyle(root, proposalsFiltersOpenvpnButton)
        }
    }

    private fun initProposalsSortDropdown(root: View) {
        ArrayAdapter.createFromResource(root.context, R.array.proposals_sort_types, android.R.layout.simple_spinner_item).also { adapter ->
            adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item)
            proposalsFiltersSort.adapter = adapter
            proposalsFiltersSort.onItemSelected { item -> proposalsViewModel.sortBy(item) }
        }
    }

    private fun initProposalsList(root: View) {
        proposalsList.layoutManager = LinearLayoutManager(root.context)
        val items = ArrayList<ServerViewItem>()
        val proposalsListAdapter = ProposalsListAdapter(items) { handleSelectedProposal(root, it) }
        proposalsList.adapter = proposalsListAdapter
        proposalsList.addItemDecoration(DividerItemDecoration(root.context, DividerItemDecoration.VERTICAL))

        proposalsSwipeRefresh.setOnRefreshListener {
            proposalsViewModel.refreshProposals {
                proposalsSwipeRefresh.isRefreshing = false
            }
        }

        // Subscribe to proposals changes.
        proposalsViewModel.getProposals().observe(viewLifecycleOwner, Observer { newItems ->
            items.clear()
            items.addAll(newItems)
            proposalsListAdapter.notifyDataSetChanged()

            // Hide progress bar once proposals are loaded.
            proposalsList.visibility = View.VISIBLE
            proposalsProgressBar.visibility = View.GONE
        })

        // Subscribe to proposals counters.
        proposalsViewModel.getProposalsCounts().observe(viewLifecycleOwner, Observer { counts ->
            proposalsFiltersAllButton.text = "All (${counts.all})" //All (${counts.all})
            proposalsFiltersOpenvpnButton.text = "Openvpn" //Openvpn (${counts.openvpn})
            proposalsFiltersWireguardButton.text = "Wireguard" //Wireguard (${counts.wireguard})
        })

        proposalsViewModel.initialProposalsLoaded.observe(viewLifecycleOwner, Observer {loaded ->
            if (loaded) {
                return@Observer
            }

            // If initial proposals failed to load during app init try to load them explicitly.
            proposalsList.visibility = View.GONE
            proposalsProgressBar.visibility = View.VISIBLE
            proposalsViewModel.refreshProposals {}
        })
    }

    private fun handleClose(root: View) {
        hideKeyboard(root)
        navigateToMainVpnFragment(root)
    }

    private fun handleSelectedProposal(root: View, server: ServerViewItem) {
        hideKeyboard(root)
        proposalsViewModel.selectProposal(server)
        navigateToMainVpnFragment(root)
    }

    private fun setFilterTabActiveStyle(root: View, btn: TextView) {
        //btn.setBackgroundColor(ContextCompat.getColor(root.context, R.color.Orange))
        btn.setTextColor(ContextCompat.getColor(root.context, R.color.Orange))
    }

    private fun setFilterTabInactiveStyle(root: View, btn: TextView) {
        btn.setBackgroundColor(Color.TRANSPARENT)
        btn.setTextColor(ContextCompat.getColor(root.context, R.color.ColorMain))
    }
}

class ProposalsListAdapter(private var list: List<ServerViewItem>, private var onItemClickListener: (ServerViewItem) -> Unit)
    : RecyclerView.Adapter<ProposalsListAdapter.ProposalViewHolder>() {

    //val prefs = ProposalViewHolder().getSharedPreferences("key1", Context.MODE_PRIVATE)
    //val server1 = prefs?.getString("server", "")

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ProposalViewHolder {
        val inflater = LayoutInflater.from(parent.context)
        return ProposalViewHolder(inflater, parent)
    }

    override fun onBindViewHolder(holder: ProposalViewHolder, position: Int) {
        val item: ServerViewItem = list[position]
        holder.bind(item)
        holder.itemView.setOnClickListener {
            onItemClickListener(item)
        }
    }

    override fun getItemCount(): Int = list.take(server).size

    inner class ProposalViewHolder(inflater: LayoutInflater, parent: ViewGroup) :
            RecyclerView.ViewHolder(inflater.inflate(R.layout.server_list_item, parent, false)) {

        private var countryFlag: RoundedImageView? = null
        private var countryName: TextView? = null
        private var providerID: TextView? = null
        private var serviceType: ImageView? = null
        private var qualityLevel: ImageView? = null
        private var favorite: ImageView? = null

        init {
            countryFlag = itemView.findViewById(R.id.proposal_item_country_flag)
            countryName = itemView.findViewById(R.id.proposal_item_country_name)
            providerID = itemView.findViewById(R.id.proposal_item_provider_id)
            serviceType = itemView.findViewById(R.id.proposal_item_service_type)
            qualityLevel = itemView.findViewById(R.id.proposal_item_quality_level)
            favorite = itemView.findViewById(R.id.proposal_item_favorite)
        }

        fun bind(item: ServerViewItem) {
            countryFlag?.setImageBitmap(item.countryFlagImage)
            countryName?.text = item.countryName
            providerID?.text = item.providerID
            serviceType?.setImageResource(item.serviceTypeResID)
            qualityLevel?.setImageResource(item.qualityResID)
            favorite?.setImageResource(item.isFavoriteResID)
        }
    }

}

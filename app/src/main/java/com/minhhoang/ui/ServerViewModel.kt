package com.minhhoang.ui

import android.util.Log
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.launch
import com.minhhoang.service.core.NodeRepository
import com.minhhoang.db.AppDatabase
import com.minhhoang.db.FavoriteProposal

enum class ServiceType(val type: String) {
    UNKNOWN("unknown"),
    OPENVPN("openvpn"),
    WIREGUARD("wireguard");

    companion object {
        fun parse(type: String): ServiceType {
            return values().find { it.type == type } ?: UNKNOWN
        }
    }
}

enum class ServiceTypeFilter(val type: String) {
    ALL("all"),
    OPENVPN("openvpn"),
    WIREGUARD("wireguard")
}

enum class ProposalSortType(val type: Int) {
    COUNTRY(1),
    QUALITY(0);

    companion object {
        fun parse(type: Int): ProposalSortType {
            if (type == 0) {
                return QUALITY
            }
            return QUALITY //country
        }
    }
}

enum class QualityLevel(val level: Int) {
    UNKNOWN(0),
    LOW(1),
    MEDIUM(2),
    HIGH(3);

    companion object {
        fun parse(level: Int): QualityLevel {
            return values().find { it.level == level } ?: UNKNOWN
        }
    }
}

class ProposalsCounts(
        val openvpn: Int,
        val wireguard: Int
) {
    val all: Int
        get() = openvpn + wireguard
}

class ProposalsFilter(
        var serviceType: ServiceTypeFilter = ServiceTypeFilter.ALL,
        var searchText: String = "",
        var sortBy: ProposalSortType = ProposalSortType.QUALITY //country
)

val server: Int = 600

class ProposalsViewModel(private val sharedViewModel: SharedViewModel, private val nodeRepository: NodeRepository, private val appDatabase: AppDatabase) : ViewModel() {
    var filter = ProposalsFilter()
    var initialProposalsLoaded = MutableLiveData<Boolean>()

    private var favoriteProposals: MutableMap<String, FavoriteProposal> = mutableMapOf()
    private var allServers: List<ServerViewItem> = listOf()
    private val proposals = MutableLiveData<List<ServerViewItem>>()
    private val proposalsCounts = MutableLiveData<ProposalsCounts>()

    suspend fun load() {
        favoriteProposals = loadFavoriteProposals()
        loadInitialProposals(false, favoriteProposals)
    }

    fun getProposals(): LiveData<List<ServerViewItem>> {
        return proposals
    }

    fun getProposalsCounts(): LiveData<ProposalsCounts> {
        return proposalsCounts
    }

    fun filterByServiceType(type: ServiceTypeFilter) {
        if (filter.serviceType == type) {
            return
        }

        filter.serviceType = type
        proposals.value = filterAndSortProposals(filter, allServers)
    }

    fun filterBySearchText(value: String) {
        val searchText = value.toLowerCase()
        if (filter.searchText == searchText) {
            return
        }

        filter.searchText = searchText
        proposals.value = filterAndSortProposals(filter, allServers)
    }

    fun sortBy(type: Int) {
        val sortBy = ProposalSortType.parse(type)
        if (filter.sortBy == sortBy) {
            return
        }

        filter.sortBy = sortBy
        proposals.value = filterAndSortProposals(filter, allServers)
    }

    fun refreshProposals(done: () -> Unit) {
        viewModelScope.launch {
            loadInitialProposals(refresh = true, favoriteProposals = favoriteProposals)
            done()
        }
    }

    fun selectProposal(item: ServerViewItem) {
        sharedViewModel.selectProposal(item)
    }

    fun toggleFavoriteProposal(proposalID: String, done: (updatedServer: ServerViewItem?) -> Unit) {
        viewModelScope.launch {
            val proposal = allServers.find { it.id == proposalID }
            if (proposal == null) {
                done(null)
                return@launch
            }

            val favoriteProposal = FavoriteProposal(proposalID)
            if (proposal.isFavorite) {
                deleteFavoriteProposal(favoriteProposal)
            } else {
                insertFavoriteProposal(favoriteProposal)
            }

            proposal.toggleFavorite()
            proposals.value = filterAndSortProposals(filter, allServers)
            done(proposal)
        }
    }

    private suspend fun loadFavoriteProposals(): MutableMap<String, FavoriteProposal> {
        val favorites = appDatabase.favoriteProposalDao().getAll()
        return favorites.map { it.id to it }.toMap().toMutableMap()
    }

    private suspend fun insertFavoriteProposal(proposal: FavoriteProposal) {
        try {
            appDatabase.favoriteProposalDao().insert(proposal)
            favoriteProposals[proposal.id] = proposal
        } catch (e: Exception) {
            Log.e(TAG, "Failed to insert favorite server", e)
        }
    }

    private suspend fun deleteFavoriteProposal(proposal: FavoriteProposal) {
        try {
            appDatabase.favoriteProposalDao().delete(proposal)
            favoriteProposals.remove(proposal.id)
        } catch (e: Exception) {
            Log.e(TAG, "Failed to delete favorite server", e)
        }
    }
    
    fun test(){
           
    }

    private suspend fun loadInitialProposals(refresh: Boolean = false, favoriteProposals: MutableMap<String, FavoriteProposal>) {

        try {
            
            val nodeProposals = nodeRepository.proposals(refresh)
            allServers = nodeProposals.map { ServerViewItem.parse(it, favoriteProposals) }

            proposalsCounts.value = ProposalsCounts(
                    openvpn = allServers.take(server).count{ it.serviceType == ServiceType.OPENVPN },
                    wireguard = allServers.take(server).count { it.serviceType == ServiceType.WIREGUARD }
            )
            proposals.value = filterAndSortProposals(filter, allServers)
            initialProposalsLoaded.value = true
        } catch (e: Exception) {
            Log.e(TAG, "Failed to load initial server", e)
            proposals.value = listOf()
            initialProposalsLoaded.value = false
        }
    }

    private fun filterAndSortProposals(filter: ProposalsFilter, allServers: List<ServerViewItem>): List<ServerViewItem> {
        return allServers.asSequence()
                // Filter by service type.
                .filter {
                    when (filter.serviceType) {
                        ServiceTypeFilter.OPENVPN -> it.serviceType == ServiceType.OPENVPN
                        ServiceTypeFilter.WIREGUARD -> it.serviceType == ServiceType.WIREGUARD
                        else -> true
                    }
                }
                // Filter by search value.
                .filter {
                    when (filter.searchText) {
                        "" -> true
                        else -> it.countryName.toLowerCase().contains(filter.searchText) or it.providerID.contains(filter.searchText)
                    }
                }
                // Sort by country or quality.
                .sortedWith(
                        //quality
                        if (filter.sortBy == ProposalSortType.QUALITY)
                            //compare by descending
                            compareByDescending { it.qualityLevel }
                        else
                            //countryname
                            compareBy { it.qualityLevel }
                )
                .toList()
    }

    companion object {
        const val TAG: String = "ProposalsViewModel"
    }
}

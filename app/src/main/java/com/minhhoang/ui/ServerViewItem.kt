package com.minhhoang.ui

import android.graphics.Bitmap
import com.minhhoang.service.core.ProposalItem
import com.minhhoang.db.FavoriteProposal
import com.minhhoang.vpn.R

class ServerViewItem constructor(
        val id: String,
        val providerID: String,
        val serviceType: ServiceType,
        val countryCode: String
) {
    var countryFlagImage: Bitmap? = null
    var serviceTypeResID: Int = R.drawable.service_openvpn
    var qualityResID: Int = R.drawable.quality_unknown
    var qualityLevel: QualityLevel = QualityLevel.UNKNOWN
    var countryName: String = ""
    var isFavorite: Boolean = false
    var isFavoriteResID: Int = R.drawable.ic_star_border_black_24dp

    fun toggleFavorite() {
        isFavorite = !isFavorite
        isFavoriteResID = if (isFavorite) {
            R.drawable.ic_star_black_24dp
        } else {
            R.drawable.ic_star_border_black_24dp
        }
    }

    companion object {
        fun parse(proposal: ProposalItem, favoriteProposals: Map<String, FavoriteProposal>): ServerViewItem {
            val res = ServerViewItem(
                    id = proposal.providerID+proposal.serviceType,
                    providerID = proposal.providerID,
                    serviceType = ServiceType.parse(proposal.serviceType),
                    countryCode = proposal.countryCode.toLowerCase())

            if (Countries.bitmaps.contains(res.countryCode)) {
                res.countryFlagImage = Countries.bitmaps[res.countryCode]
                res.countryName = Countries.values[res.countryCode]?.name ?: ""
            }

            res.serviceTypeResID = mapServiceTypeResourceID(res.serviceType)
            res.qualityLevel = QualityLevel.parse(proposal.qualityLevel)
            res.qualityResID = mapQualityLevelResourceID(res.qualityLevel)
            res.isFavorite = favoriteProposals.containsKey(res.id)
            if (res.isFavorite) {
                res.isFavoriteResID = R.drawable.ic_star_black_24dp
            }

            return res
        }

        private fun mapServiceTypeResourceID(serviceType: ServiceType): Int {
            return when(serviceType) {
                ServiceType.OPENVPN -> R.drawable.service_openvpn
                ServiceType.WIREGUARD -> R.drawable.service_wireguard
                else -> R.drawable.service_openvpn
            }
        }

        private fun mapQualityLevelResourceID(qualityLevel: QualityLevel): Int {
            return when(qualityLevel) {
                QualityLevel.HIGH -> R.drawable.quality_high
                QualityLevel.MEDIUM -> R.drawable.quality_medium
                QualityLevel.LOW -> R.drawable.quality_low
                QualityLevel.UNKNOWN -> R.drawable.quality_unknown
            }
        }
    }
}

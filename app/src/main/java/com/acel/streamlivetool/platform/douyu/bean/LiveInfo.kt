package com.acel.streamlivetool.platform.douyu.bean

data class LiveInfo(
    val `data`: Data,
    val error: Int,
    val msg: String
) {

    data class Data(
        val cdnsWithName: List<CdnsWithName>,
        val client_ip: String,
        val eticket: Any,
        val h265_p2p: Int,
        val h265_p2p_cid: Int,
        val h265_p2p_cids: String,
        val inNA: Int,
        val isPassPlayer: Int,
        val is_mixed: Boolean,
        val mixedCDN: String,
        val mixed_live: String,
        val mixed_url: String,
        val multirates: List<Multirate>,
        val online: Int,
        val p2p: Int,
        val p2pCid: Long,
        val p2pCids: String,
        val p2pMeta: Any,
        val player_1: String,
        val rate: Int,
        val rateSwitch: Int,
        val room_id: Int,
        val rtmp_cdn: String,
        val rtmp_live: String,
        val rtmp_url: String,
        val smt: Int,
        val streamStatus: Int
    )

    data class CdnsWithName(
        val cdn: String,
        val isH265: Boolean,
        val name: String
    )

    data class Multirate(
        val bit: Int,
        val highBit: Int,
        val name: String,
        val rate: Int
    ) {
        override fun equals(other: Any?): Boolean {
            if (this === other) return true
            if (other !is Multirate) return false

            if (rate != other.rate) return false

            return true
        }

        override fun hashCode(): Int {
            return rate
        }
    }
}
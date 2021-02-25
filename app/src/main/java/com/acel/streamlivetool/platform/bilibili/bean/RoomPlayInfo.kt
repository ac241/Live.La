package com.acel.streamlivetool.platform.bilibili.bean

data class RoomPlayInfo(
    val code: Int,
    val `data`: Data,
    val message: String,
    val ttl: Int
){

    data class Data(
        val all_special_types: List<Any>,
        val encrypted: Boolean,
        val hidden_till: Int,
        val is_hidden: Boolean,
        val is_locked: Boolean,
        val is_portrait: Boolean,
        val is_sp: Int,
        val live_status: Int,
        val live_time: Int,
        val lock_till: Int,
        val need_p2p: Int,
        val play_url: PlayUrl,
        val pwd_verified: Boolean,
        val room_id: Int,
        val room_shield: Int,
        val short_id: Int,
        val special_type: Int,
        val uid: Int
    )

    data class PlayUrl(
        val accept_quality: List<String>,
        val current_qn: Int,
        val current_quality: Int,
        val durl: List<Durl>,
        val quality_description: List<QualityDescription>

    )

    data class Durl(
        val length: Int,
        val order: Int,
        val p2p_type: Int,
        val ptag: Int,
        val stream_type: Int,
        val url: String
    )

    data class QualityDescription(
        val desc: String,
        val qn: Int
    ){
        override fun equals(other: Any?): Boolean {
            if (this === other) return true
            if (other !is QualityDescription) return false
            if (qn != other.qn) return false
            return true
        }

        override fun hashCode(): Int {
            return qn
        }
    }
}

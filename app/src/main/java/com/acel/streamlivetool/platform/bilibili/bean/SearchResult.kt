package com.acel.streamlivetool.platform.bilibili.bean

data class SearchResult(
    val code: Int,
    val `data`: Data,
    val message: String,
    val ttl: Int
) {
    data class Data(
        val cost_time: CostTime,
        val egg_hit: Int,
        val exp_list: ExpList,
        val numPages: Int,
        val numResults: Int,
        val page: Int,
        val pagesize: Int,
        val result: List<Result>,
        val rqt_type: String,
        val seid: String,
        val show_column: Int,
        val suggest_keyword: String
    )

    data class CostTime(
        val as_doc_request: String,
        val as_request: String,
        val as_request_format: String,
        val as_response_format: String,
        val deserialize_response: String,
        val illegal_handler: String,
        val main_handler: String,
        val params_check: String,
        val save_cache: String,
        val total: String
    )

    data class ExpList(
        val `5510`: Boolean
    )

    data class Result(
        val area: Int,
        val attentions: Int,
        val hit_columns: List<String>,
        val is_live: Boolean,
        val live_status: Int,
        val live_time: String,
        val rank_index: Int,
        val rank_offset: Int,
        val rank_score: Int,
        val roomid: Int,
        val tags: String,
        val type: String,
        val uface: String,
        val uid: Int,
        val uname: String
    )
}
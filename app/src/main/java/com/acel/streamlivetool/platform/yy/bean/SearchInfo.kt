package com.acel.streamlivetool.platform.yy.bean

import com.google.gson.annotations.SerializedName


data class SearchInfo(
    @SerializedName("data")
    val `data`: Data,
    @SerializedName("desc")
    val desc: String,
    @SerializedName("disp")
    val disp: String,
    @SerializedName("status")
    val status: Int
) {
    data class Data(
        @SerializedName("n")
        val n: Int,
        @SerializedName("q")
        val q: String,
        @SerializedName("s")
        val s: Int,
        @SerializedName("searchResult")
        val searchResult: SearchResult,
        @SerializedName("t")
        val t: Int
    ) {
        data class SearchResult(
            @SerializedName("response")
            val response: Response,
            @SerializedName("responseHeader")
            val responseHeader: ResponseHeader,
            @SerializedName("total")
            val total: Int,
            @SerializedName("videoIsGame")
            val videoIsGame: Int
        ) {
            data class Response(
                @SerializedName("121")
                val x121: X121,
                @SerializedName("2")
                val x2: X2,
                @SerializedName("-3")
                val x3: X3
            ) {
                data class X121(
                    @SerializedName("docs")
                    val docs: List<Any>,
                    @SerializedName("numFound")
                    val numFound: Int,
                    @SerializedName("start")
                    val start: Int
                )

                data class X2(
                    @SerializedName("docs")
                    val docs: List<Doc>,
                    @SerializedName("numFound")
                    val numFound: Int,
                    @SerializedName("start")
                    val start: Int
                ) {
                    data class Doc(
                        @SerializedName("asid")
                        val asid: String,
                        @SerializedName("dataType")
                        val dataType: Int,
                        @SerializedName("iconUrl")
                        val iconUrl: String,
                        @SerializedName("liveOn")
                        val liveOn: String,
                        @SerializedName("name")
                        val name: String,
                        @SerializedName("sid")
                        val sid: String
                    )

                }

                data class X3(
                    @SerializedName("docs")
                    val docs: List<Any>,
                    @SerializedName("numFound")
                    val numFound: Int,
                    @SerializedName("start")
                    val start: Int
                )
            }

            data class ResponseHeader(
                @SerializedName("correctWord")
                val correctWord: String,
                @SerializedName("QTime")
                val qTime: Int,
                @SerializedName("status")
                val status: Int,
                @SerializedName("token")
                val token: String
            )
        }
    }
}









package com.acel.streamlivetool.platform.huya

import android.util.Base64
import io.jsonwebtoken.ExpiredJwtException
import io.jsonwebtoken.Jwts
import io.jsonwebtoken.SignatureAlgorithm
import io.jsonwebtoken.SignatureException

object HuyaWSHelper {
    const val heartBeat =
        "{\"command\":\"subscribeNotice\",\"data\":[\"getMessageNotice\"],\"reqId\":\"123456789\"}"

    private fun generateJWT(appId: String, secret: String, iat: Long, exp: Long): String? {
        val map: MutableMap<String, Any> = hashMapOf(
            Pair("appId", appId),
            Pair("iat", iat),
            Pair("exp", exp)
        )
        val key = Base64.encodeToString(secret.toByteArray(), 0)
        val compactJws = Jwts.builder()
            .addClaims(map)
            .setHeaderParam("typ", "JWT")
            .signWith(SignatureAlgorithm.HS256, key)
            .compact()
        try {
            Jwts.parser().setSigningKey(key).parseClaimsJws(compactJws)
        } catch (e: SignatureException) {
            e.printStackTrace()
        } catch (e: ExpiredJwtException) {
            e.printStackTrace()
        }
        return compactJws
    }

    fun getWSUrl(roomId: String, appId: String, secret: String): String {

        val cur = System.currentTimeMillis()
        val iat = cur / 1000
        val exp = (cur + 60 * 1000) / 1000 //过期时间
        val token = generateJWT(appId, secret, iat, exp)

        return "ws://ws-apiext.huya.com/index.html?do=comm" +
                "&roomId=$roomId" +
                "&appId=$appId" +
                "&iat=$iat" +
                "&exp=$exp" +
                "&sToken=$token"
    }
}
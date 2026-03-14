package com.example.ftprcar.data.api

import okhttp3.ResponseBody
import org.json.JSONObject
import retrofit2.Response

fun Throwable.toUserError(defaultMessage: String): String {
    val detail = localizedMessage?.trim().orEmpty()
    return if (detail.isNotEmpty()) "$defaultMessage: $detail" else defaultMessage
}

fun <T> Response<T>.toUserError(defaultMessage: String): String {
    val detail = parseApiError(errorBody())
    return if (detail.isNotEmpty()) "$defaultMessage: $detail" else "$defaultMessage (${code()})"
}

private fun parseApiError(body: ResponseBody?): String {
    val raw = body?.string()?.trim().orEmpty()
    if (raw.isEmpty()) return ""

    return try {
        val json = JSONObject(raw)
        json.optString("error").takeIf(String::isNotBlank)
            ?: json.optJSONArray("errors")
                ?.let { array ->
                    buildList {
                        for (index in 0 until array.length()) {
                            val item = array.optJSONObject(index) ?: continue
                            val error = item.optString("error").trim()
                            if (error.isNotEmpty()) add(error)
                        }
                    }.distinct().joinToString("; ")
                }
            ?: raw.take(180)
    } catch (_: Exception) {
        raw.take(180)
    }
}

package model

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable


@Serializable
data class ApiEnv(
    @SerialName("prefix") val prefix: String?,
    @SerialName("main_url") val mainUrl: String?,
    @SerialName("chat_url") val chatUrl: String?
)
import kotlinx.serialization.json.Json

val jsonFormat = Json {
    explicitNulls = false
    ignoreUnknownKeys = true
}
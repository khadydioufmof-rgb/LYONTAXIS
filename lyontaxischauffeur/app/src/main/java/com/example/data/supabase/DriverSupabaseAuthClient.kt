package com.example.data.supabase

import com.example.BuildConfig
import org.json.JSONArray
import com.squareup.moshi.JsonClass
import retrofit2.Response
import retrofit2.Retrofit
import retrofit2.converter.moshi.MoshiConverterFactory
import retrofit2.http.Body
import retrofit2.http.Header
import retrofit2.http.POST
import retrofit2.http.Query

@JsonClass(generateAdapter = true)
data class PasswordLoginRequest(
    val email: String,
    val password: String
)

@JsonClass(generateAdapter = true)
data class PasswordLoginResponse(
    val access_token: String = "",
    val refresh_token: String = "",
    val expires_in: Long = 2_592_000,
    val token: String? = null,
    val user: SupabaseAuthUser?
) {
    fun normalized() = copy(access_token = token ?: access_token)
}

@JsonClass(generateAdapter = true)
data class SupabaseAuthUser(
    val id: String,
    val email: String?
)

private interface DriverSupabaseAuthApi {
    @POST("driver/auth/login")
    suspend fun signIn(
        @Body request: PasswordLoginRequest
    ): Response<PasswordLoginResponse>
}

class DriverSupabaseAuthClient {
    private val api: DriverSupabaseAuthApi = Retrofit.Builder()
        .baseUrl(BuildConfig.LYONTAXIS_API_URL.trimEnd('/') + "/")
        .addConverterFactory(MoshiConverterFactory.create())
        .build()
        .create(DriverSupabaseAuthApi::class.java)

    val isConfigured: Boolean
        get() = BuildConfig.LYONTAXIS_API_URL.isNotBlank()

    suspend fun signIn(email: String, password: String): Result<PasswordLoginResponse> {
        if (!isConfigured) return Result.failure(IllegalStateException("L'API LyonTaxis n'est pas configuree"))

        return runCatching {
            val response = api.signIn(PasswordLoginRequest(email.trim(), password))
            check(response.isSuccessful) { "Connexion LyonTaxis refusee (${response.code()})" }
            requireNotNull(response.body()) { "LyonTaxis a renvoye une session vide" }.normalized()
        }
    }

    suspend fun getAssignedRides(accessToken: String): Result<String> = runCatching {
        require(isConfigured) { "L'API LyonTaxis n'est pas configuree" }
        val request = okhttp3.Request.Builder()
            .url(BuildConfig.LYONTAXIS_API_URL.trimEnd('/') + "/trips")
            .header("Authorization", "Bearer $accessToken")
            .build()
        okhttp3.OkHttpClient().newCall(request).execute().use { response ->
            check(response.isSuccessful) { "Lecture des courses LyonTaxis refusee (${response.code})" }
            val payload = response.body?.string().orEmpty()
            org.json.JSONObject(payload).optJSONArray("trips")?.toString() ?: "[]"
        }
    }

    suspend fun getNearestReservation(
        accessToken: String,
        driverLatitude: Double,
        driverLongitude: Double
    ): Result<DriverReservation?> = runCatching {
        val payload = getAssignedRides(accessToken).getOrThrow()
        val rides = JSONArray(payload)
        var nearest: DriverReservation? = null

        for (index in 0 until rides.length()) {
            val ride = rides.getJSONObject(index)
            val pickup = ride.optJSONObject("pickup_location") ?: continue
            val latitude = pickup.optDouble("latitude", Double.NaN)
            val longitude = pickup.optDouble("longitude", Double.NaN)
            if (latitude.isNaN() || longitude.isNaN()) continue

            val distance = distanceKm(driverLatitude, driverLongitude, latitude, longitude)
            if (nearest == null || distance < nearest.distanceKm) {
                nearest = DriverReservation(
                    id = ride.optString("id"),
                    pickupAddress = pickup.optString("address", "Point de prise en charge"),
                    dropoffAddress = ride.optJSONObject("dropoff_location")?.optString("address", "Destination") ?: "Destination",
                    vehicle = ride.optString("vehicle_category", ride.optString("vehicle", "Eco")),
                    fare = ride.optDouble("fare", 0.0),
                    distanceKm = distance
                )
            }
        }

        nearest
    }

    private fun distanceKm(lat1: Double, lon1: Double, lat2: Double, lon2: Double): Double {
        val earthRadius = 6371.0
        val latitudeDelta = Math.toRadians(lat2 - lat1)
        val longitudeDelta = Math.toRadians(lon2 - lon1)
        val a = kotlin.math.sin(latitudeDelta / 2) * kotlin.math.sin(latitudeDelta / 2) +
            kotlin.math.cos(Math.toRadians(lat1)) * kotlin.math.cos(Math.toRadians(lat2)) *
            kotlin.math.sin(longitudeDelta / 2) * kotlin.math.sin(longitudeDelta / 2)
        return earthRadius * 2 * kotlin.math.atan2(kotlin.math.sqrt(a), kotlin.math.sqrt(1 - a))
    }
}

data class DriverReservation(
    val id: String,
    val pickupAddress: String,
    val dropoffAddress: String,
    val vehicle: String,
    val fare: Double,
    val distanceKm: Double
)

package com.example.data.supabase

import com.example.BuildConfig
import com.squareup.moshi.Json
import com.squareup.moshi.JsonClass
import retrofit2.Response
import retrofit2.Retrofit
import retrofit2.converter.moshi.MoshiConverterFactory
import retrofit2.http.Body
import retrofit2.http.GET
import retrofit2.http.Header
import retrofit2.http.Headers
import retrofit2.http.PATCH
import retrofit2.http.POST
import retrofit2.http.Query

@JsonClass(generateAdapter = true)
data class SupabaseProfileDto(
  val id: String,
  val name: String = "",
  val email: String? = null,
  val phone_number: String? = null,
  val gender: String? = null,
  val birthday: String? = null,
  val emergency_contact: String? = null,
  val home_address: String? = null,
  val member_level: String = "Membre",
  val cash_balance: Double = 0.0,
  val integral_points: Int = 0,
  val coupons_count: Int = 0,
  val referral_code: String? = null,
  val avatar_seed: String? = null
)

@JsonClass(generateAdapter = true)
data class SupabaseLocationDto(
  val id: String = "",
  val title: String = "",
  val address: String = "",
  val distance_km: Double = 0.0,
  val latitude: Double,
  val longitude: Double
)

@JsonClass(generateAdapter = true)
data class SupabaseRideInsert(
  val vehicle_category: String,
  val pickup_latitude: Double,
  val pickup_longitude: Double,
  val dropoff_latitude: Double,
  val dropoff_longitude: Double,
  val pickup_address: String,
  val dropoff_address: String,
  val passenger_count: Int,
  val special_requests: String? = null
)

@JsonClass(generateAdapter = true)
data class SupabaseRideDto(
  val id: Long,
  val pickup_location: SupabaseLocationDto,
  val dropoff_location: SupabaseLocationDto,
  @Json(name = "vehicle_category") val vehicle: String,
  val status: String,
  val fare: Double,
  val distance_km: Double = 0.0,
  val duration_min: Int = 0,
  val driver: SupabaseDriverDto? = null,
  @Json(name = "payment_method") val payment_method_title: String = "cash",
  val created_at: String = ""
)

@JsonClass(generateAdapter = true)
data class SupabaseDriverDto(
  val name: String = "Chauffeur LyonTaxis"
)

@JsonClass(generateAdapter = true)
data class SupabaseNotificationDto(
  val id: Long,
  val type: String,
  val title: String,
  val description: String,
  val is_read: Boolean = false,
  val created_at: String = ""
)

interface SupabaseDataApi {
  @GET("user/profile")
  suspend fun getProfile(
    @Header("Authorization") authorization: String,
  ): Response<ProfileResponse>

  @PATCH("user/profile")
  suspend fun updateProfile(
    @Header("Authorization") authorization: String,
    @Body profile: SupabaseProfileDto
  ): Response<Unit>

  @POST("trips")
  suspend fun createRide(
    @Header("Authorization") authorization: String,
    @Body ride: SupabaseRideInsert
  ): Response<Unit>

  @GET("trips")
  suspend fun getRides(
    @Header("Authorization") authorization: String,
  ): Response<TripListResponse>

  @GET("user/notifications")
  suspend fun getNotifications(
    @Header("Authorization") authorization: String
  ): Response<NotificationListResponse>
}

@JsonClass(generateAdapter = true)
data class ProfileResponse(val user: SupabaseProfileDto)

@JsonClass(generateAdapter = true)
data class TripListResponse(val trips: List<SupabaseRideDto> = emptyList())

@JsonClass(generateAdapter = true)
data class NotificationListResponse(val notifications: List<SupabaseNotificationDto> = emptyList())

class SupabaseDataClient(
  private val api: SupabaseDataApi = createApi()
) {
  suspend fun getProfile(accessToken: String, userId: String): Result<SupabaseProfileDto?> = runCatching {
    val response = api.getProfile(
      authorization = "Bearer $accessToken"
    )
    check(response.isSuccessful) { "LyonTaxis profile read failed (${response.code()})" }
    response.body()?.user
  }

  suspend fun updateProfile(accessToken: String, profile: SupabaseProfileDto): Result<Unit> = runCatching {
    val response = api.updateProfile(
      authorization = "Bearer $accessToken",
      profile = profile
    )
    check(response.isSuccessful) { "Supabase profile update failed (${response.code()})" }
  }

  suspend fun createRide(accessToken: String, ride: SupabaseRideInsert): Result<Unit> = runCatching {
    val response = api.createRide(
      authorization = "Bearer $accessToken",
      ride = ride
    )
    check(response.isSuccessful) { "LyonTaxis ride creation failed (${response.code()})" }
  }

  suspend fun getRides(accessToken: String, userId: String): Result<List<SupabaseRideDto>> = runCatching {
    val response = api.getRides(
      authorization = "Bearer $accessToken"
    )
    check(response.isSuccessful) { "LyonTaxis rides read failed (${response.code()})" }
    response.body()?.trips.orEmpty()
  }

  suspend fun getNotifications(accessToken: String, userId: String): Result<List<SupabaseNotificationDto>> = runCatching {
    val response = api.getNotifications(
      authorization = "Bearer $accessToken"
    )
    check(response.isSuccessful) { "LyonTaxis notifications read failed (${response.code()})" }
    response.body()?.notifications.orEmpty()
  }

  companion object {
    private fun createApi(): SupabaseDataApi {
      val configuredUrl = BuildConfig.LYONTAXIS_API_URL.trimEnd('/')
      val baseUrl = (configuredUrl.ifBlank { "https://invalid.supabase.local" }) + "/"
      return Retrofit.Builder()
        .baseUrl(baseUrl)
        .addConverterFactory(MoshiConverterFactory.create())
        .build()
        .create(SupabaseDataApi::class.java)
    }
  }
}

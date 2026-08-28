package com.example.data.supabase

import com.example.BuildConfig
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
  val id: String,
  val title: String,
  val address: String,
  val distance_km: Double = 0.0,
  val latitude: Double,
  val longitude: Double
)

@JsonClass(generateAdapter = true)
data class SupabaseRideInsert(
  val user_id: String,
  val pickup_location: SupabaseLocationDto,
  val dropoff_location: SupabaseLocationDto,
  val vehicle: String,
  val status: String,
  val fare: Double,
  val base_fare: Double,
  val distance_fare: Double,
  val time_fare: Double,
  val stop_fee: Double,
  val service_fee: Double,
  val discount: Double,
  val tip: Double,
  val distance_km: Double,
  val duration_min: Int,
  val payment_method_title: String,
  val preferences: Map<String, Boolean>
)

@JsonClass(generateAdapter = true)
data class SupabaseRideDto(
  val id: String,
  val pickup_location: SupabaseLocationDto,
  val dropoff_location: SupabaseLocationDto,
  val vehicle: String,
  val status: String,
  val fare: Double,
  val distance_km: Double = 0.0,
  val duration_min: Int = 0,
  val driver: SupabaseDriverDto? = null,
  val payment_method_title: String = "Espèces LyonTaxis",
  val created_at: String = ""
)

@JsonClass(generateAdapter = true)
data class SupabaseDriverDto(
  val name: String = "Chauffeur LyonTaxis"
)

@JsonClass(generateAdapter = true)
data class SupabaseNotificationDto(
  val id: String,
  val type: String,
  val title: String,
  val description: String,
  val is_read: Boolean = false,
  val created_at: String = ""
)

interface SupabaseDataApi {
  @GET("rest/v1/profiles")
  suspend fun getProfile(
    @Header("apikey") apiKey: String,
    @Header("Authorization") authorization: String,
    @Query("id") idFilter: String,
    @Query("select") select: String = "*"
  ): Response<List<SupabaseProfileDto>>

  @Headers("Prefer: return=minimal")
  @PATCH("rest/v1/profiles")
  suspend fun updateProfile(
    @Header("apikey") apiKey: String,
    @Header("Authorization") authorization: String,
    @Query("id") idFilter: String,
    @Body profile: SupabaseProfileDto
  ): Response<Unit>

  @Headers("Prefer: return=minimal")
  @POST("rest/v1/rides")
  suspend fun createRide(
    @Header("apikey") apiKey: String,
    @Header("Authorization") authorization: String,
    @Body ride: SupabaseRideInsert
  ): Response<Unit>

  @GET("rest/v1/rides")
  suspend fun getRides(
    @Header("apikey") apiKey: String,
    @Header("Authorization") authorization: String,
    @Query("user_id") userFilter: String,
    @Query("select") select: String = "*",
    @Query("order") order: String = "created_at.desc"
  ): Response<List<SupabaseRideDto>>

  @GET("rest/v1/notifications")
  suspend fun getNotifications(
    @Header("apikey") apiKey: String,
    @Header("Authorization") authorization: String,
    @Query("user_id") userFilter: String,
    @Query("select") select: String = "*",
    @Query("order") order: String = "created_at.desc"
  ): Response<List<SupabaseNotificationDto>>
}

class SupabaseDataClient(
  private val api: SupabaseDataApi = createApi()
) {
  suspend fun getProfile(accessToken: String, userId: String): Result<SupabaseProfileDto?> = runCatching {
    val response = api.getProfile(
      apiKey = BuildConfig.SUPABASE_ANON_KEY,
      authorization = "Bearer $accessToken",
      idFilter = "eq.$userId"
    )
    check(response.isSuccessful) { "Supabase profile read failed (${response.code()})" }
    response.body()?.firstOrNull()
  }

  suspend fun updateProfile(accessToken: String, profile: SupabaseProfileDto): Result<Unit> = runCatching {
    val response = api.updateProfile(
      apiKey = BuildConfig.SUPABASE_ANON_KEY,
      authorization = "Bearer $accessToken",
      idFilter = "eq.${profile.id}",
      profile = profile
    )
    check(response.isSuccessful) { "Supabase profile update failed (${response.code()})" }
  }

  suspend fun createRide(accessToken: String, ride: SupabaseRideInsert): Result<Unit> = runCatching {
    val response = api.createRide(
      apiKey = BuildConfig.SUPABASE_ANON_KEY,
      authorization = "Bearer $accessToken",
      ride = ride
    )
    check(response.isSuccessful) { "Supabase ride creation failed (${response.code()})" }
  }

  suspend fun getRides(accessToken: String, userId: String): Result<List<SupabaseRideDto>> = runCatching {
    val response = api.getRides(
      apiKey = BuildConfig.SUPABASE_ANON_KEY,
      authorization = "Bearer $accessToken",
      userFilter = "eq.$userId"
    )
    check(response.isSuccessful) { "Supabase rides read failed (${response.code()})" }
    response.body().orEmpty()
  }

  suspend fun getNotifications(accessToken: String, userId: String): Result<List<SupabaseNotificationDto>> = runCatching {
    val response = api.getNotifications(
      apiKey = BuildConfig.SUPABASE_ANON_KEY,
      authorization = "Bearer $accessToken",
      userFilter = "eq.$userId"
    )
    check(response.isSuccessful) { "Supabase notifications read failed (${response.code()})" }
    response.body().orEmpty()
  }

  companion object {
    private fun createApi(): SupabaseDataApi {
      val configuredUrl = BuildConfig.SUPABASE_URL.trimEnd('/')
      val baseUrl = (configuredUrl.ifBlank { "https://invalid.supabase.local" }) + "/"
      return Retrofit.Builder()
        .baseUrl(baseUrl)
        .addConverterFactory(MoshiConverterFactory.create())
        .build()
        .create(SupabaseDataApi::class.java)
    }
  }
}

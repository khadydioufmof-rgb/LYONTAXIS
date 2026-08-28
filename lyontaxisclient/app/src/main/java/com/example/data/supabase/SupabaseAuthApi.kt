package com.example.data.supabase

import com.example.BuildConfig
import com.squareup.moshi.JsonClass
import retrofit2.Response
import retrofit2.Retrofit
import retrofit2.converter.moshi.MoshiConverterFactory
import retrofit2.http.Body
import retrofit2.http.Header
import retrofit2.http.POST
import retrofit2.http.Query

@JsonClass(generateAdapter = true)
data class SupabaseOtpRequest(
  val email: String? = null,
  val phone: String? = null,
  val create_user: Boolean = true
)

@JsonClass(generateAdapter = true)
data class SupabaseVerifyOtpRequest(
  val email: String? = null,
  val phone: String? = null,
  val token: String,
  val type: String
)

@JsonClass(generateAdapter = true)
data class SupabaseRefreshTokenRequest(
  val refresh_token: String
)

@JsonClass(generateAdapter = true)
data class SupabaseSessionResponse(
  val access_token: String,
  val refresh_token: String,
  val expires_in: Long,
  val token_type: String,
  val user: SupabaseUserResponse?
)

@JsonClass(generateAdapter = true)
data class SupabaseUserResponse(
  val id: String,
  val email: String?,
  val phone: String?
)

interface SupabaseAuthApi {
  @POST("auth/v1/otp")
  suspend fun requestOtp(
    @Header("apikey") apiKey: String,
    @Header("Authorization") authorization: String,
    @Body request: SupabaseOtpRequest
  ): Response<Unit>

  @POST("auth/v1/token")
  suspend fun verifyOtp(
    @Query("grant_type") grantType: String,
    @Header("apikey") apiKey: String,
    @Header("Authorization") authorization: String,
    @Body request: SupabaseVerifyOtpRequest
  ): Response<SupabaseSessionResponse>

  @POST("auth/v1/token")
  suspend fun refreshSession(
    @Query("grant_type") grantType: String,
    @Header("apikey") apiKey: String,
    @Header("Authorization") authorization: String,
    @Body request: SupabaseRefreshTokenRequest
  ): Response<SupabaseSessionResponse>
}

class SupabaseAuthClient(
  private val api: SupabaseAuthApi = createApi()
) {
  val isConfigured: Boolean
    get() = BuildConfig.SUPABASE_URL.isNotBlank() && BuildConfig.SUPABASE_ANON_KEY.isNotBlank()

  suspend fun requestOtp(identifier: String): Result<Unit> {
    if (!isConfigured) return Result.failure(IllegalStateException("Supabase is not configured"))
    val normalized = identifier.trim()
    val request = if (normalized.contains("@")) {
      SupabaseOtpRequest(email = normalized)
    } else {
      SupabaseOtpRequest(phone = normalized)
    }
    return runCatching {
      val response = api.requestOtp(
        apiKey = BuildConfig.SUPABASE_ANON_KEY,
        authorization = "Bearer ${BuildConfig.SUPABASE_ANON_KEY}",
        request = request
      )
      check(response.isSuccessful) { "Supabase OTP request failed (${response.code()})" }
    }
  }

  suspend fun verifyOtp(identifier: String, code: String): Result<SupabaseSessionResponse> {
    if (!isConfigured) return Result.failure(IllegalStateException("Supabase is not configured"))
    val normalized = identifier.trim()
    val request = if (normalized.contains("@")) {
      SupabaseVerifyOtpRequest(email = normalized, token = code, type = "email")
    } else {
      SupabaseVerifyOtpRequest(phone = normalized, token = code, type = "sms")
    }
    return runCatching {
      val response = api.verifyOtp(
        grantType = "otp",
        apiKey = BuildConfig.SUPABASE_ANON_KEY,
        authorization = "Bearer ${BuildConfig.SUPABASE_ANON_KEY}",
        request = request
      )
      check(response.isSuccessful) { "Supabase OTP verification failed (${response.code()})" }
      requireNotNull(response.body()) { "Supabase returned an empty session" }
    }
  }

  suspend fun refreshSession(refreshToken: String): Result<SupabaseSessionResponse> {
    if (!isConfigured) return Result.failure(IllegalStateException("Supabase is not configured"))
    return runCatching {
      val response = api.refreshSession(
        grantType = "refresh_token",
        apiKey = BuildConfig.SUPABASE_ANON_KEY,
        authorization = "Bearer ${BuildConfig.SUPABASE_ANON_KEY}",
        request = SupabaseRefreshTokenRequest(refreshToken)
      )
      check(response.isSuccessful) { "Supabase session refresh failed (${response.code()})" }
      requireNotNull(response.body()) { "Supabase returned an empty refreshed session" }
    }
  }

  companion object {
    private fun createApi(): SupabaseAuthApi {
      val configuredUrl = BuildConfig.SUPABASE_URL.trimEnd('/')
      val baseUrl = (configuredUrl.ifBlank { "https://invalid.supabase.local" }) + "/"
      return Retrofit.Builder()
        .baseUrl(baseUrl)
        .addConverterFactory(MoshiConverterFactory.create())
        .build()
        .create(SupabaseAuthApi::class.java)
    }
  }
}

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
data class SupabaseOtpRequest(val phone_or_email: String)

@JsonClass(generateAdapter = true)
data class SupabaseVerifyOtpRequest(
  val phone_or_email: String,
  val code: String
)

@JsonClass(generateAdapter = true)
data class SupabaseRefreshTokenRequest(
  val refresh_token: String
)

@JsonClass(generateAdapter = true)
data class SupabaseSessionResponse(
  val access_token: String = "",
  val refresh_token: String = "",
  val expires_in: Long = 2_592_000,
  val token_type: String = "Bearer",
  val token: String? = null,
  val user: SupabaseUserResponse?
) {
  fun normalized(): SupabaseSessionResponse = copy(access_token = token ?: access_token)
}

@JsonClass(generateAdapter = true)
data class SupabaseUserResponse(
  val id: String,
  val email: String?,
  val phone: String?
)

interface SupabaseAuthApi {
  @POST("auth/request-otp")
  suspend fun requestOtp(
    @Body request: SupabaseOtpRequest
  ): Response<Unit>

  @POST("auth/verify-otp")
  suspend fun verifyOtp(
    @Body request: SupabaseVerifyOtpRequest
  ): Response<SupabaseSessionResponse>
}

class SupabaseAuthClient(
  private val api: SupabaseAuthApi = createApi()
) {
  val isConfigured: Boolean
    get() = BuildConfig.LYONTAXIS_API_URL.isNotBlank()

  suspend fun requestOtp(identifier: String): Result<Unit> {
    if (!isConfigured) return Result.failure(IllegalStateException("L'API LyonTaxis n'est pas configuree"))
    val normalized = identifier.trim()
    val request = SupabaseOtpRequest(phone_or_email = normalized)
    return runCatching {
      val response = api.requestOtp(request)
      check(response.isSuccessful) { "LyonTaxis OTP request failed (${response.code()})" }
    }
  }

  suspend fun verifyOtp(identifier: String, code: String): Result<SupabaseSessionResponse> {
    if (!isConfigured) return Result.failure(IllegalStateException("L'API LyonTaxis n'est pas configuree"))
    val normalized = identifier.trim()
    val request = SupabaseVerifyOtpRequest(phone_or_email = normalized, code = code)
    return runCatching {
      val response = api.verifyOtp(request)
      check(response.isSuccessful) { "LyonTaxis OTP verification failed (${response.code()})" }
      requireNotNull(response.body()) { "LyonTaxis returned an empty session" }.normalized()
    }
  }

  suspend fun refreshSession(refreshToken: String): Result<SupabaseSessionResponse> {
    return Result.failure(IllegalStateException("Laravel Sanctum ne prend pas encore en charge le refresh token"))
  }

  companion object {
    private fun createApi(): SupabaseAuthApi {
      val configuredUrl = BuildConfig.LYONTAXIS_API_URL.trimEnd('/')
      val baseUrl = (configuredUrl.ifBlank { "https://invalid.supabase.local" }) + "/"
      return Retrofit.Builder()
        .baseUrl(baseUrl)
        .addConverterFactory(MoshiConverterFactory.create())
        .build()
        .create(SupabaseAuthApi::class.java)
    }
  }
}

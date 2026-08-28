package com.example.data

import com.squareup.moshi.JsonClass
import retrofit2.Retrofit
import retrofit2.converter.moshi.MoshiConverterFactory
import retrofit2.http.GET
import retrofit2.http.Headers
import retrofit2.http.Query

@JsonClass(generateAdapter = true)
data class NominatimResult(
  val place_id: Long,
  val display_name: String,
  val lat: String,
  val lon: String,
  val type: String? = null
)

private interface NominatimApi {
  @Headers("User-Agent: LyonTaxis/1.0 (contact@lyontaxis.fr)")
  @GET("search")
  suspend fun search(
    @Query("q") query: String,
    @Query("format") format: String = "jsonv2",
    @Query("addressdetails") addressDetails: Int = 1,
    @Query("limit") limit: Int = 6,
    @Query("countrycodes") countryCodes: String = "fr",
    @Query("viewbox") viewBox: String = "4.70,45.85,4.95,45.65",
    @Query("bounded") bounded: Int = 1
  ): List<NominatimResult>
}

class LyonGeocoder {
  private val api = Retrofit.Builder()
    .baseUrl("https://nominatim.openstreetmap.org/")
    .addConverterFactory(MoshiConverterFactory.create())
    .build()
    .create(NominatimApi::class.java)

  suspend fun search(query: String): List<NominatimResult> = api.search(query.trim())
}

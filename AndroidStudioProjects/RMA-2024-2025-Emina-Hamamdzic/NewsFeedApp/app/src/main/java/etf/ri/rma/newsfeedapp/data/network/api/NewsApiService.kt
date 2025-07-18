package etf.ri.rma.newsfeedapp.data.network.api


import etf.ri.rma.newsfeedapp.dto.NewsResponse
import retrofit2.http.GET
import retrofit2.http.Query

interface NewsApiService {

    // Dohvata 3 top vijesti iz određene kategorije
    @GET("news/top")
    suspend fun getTopStoriesByCategory(
        @Query("api_token") apiKey: String,
        @Query("categories") category: String,
        @Query("language") language: String = "en",
        @Query("limit") limit: Int = 3
    ): NewsResponse

    @GET("news/similar")
    suspend fun getSimilarStories(
        @Query("api_token") apiKey: String,
        @Query("uuid") uuid: String
    ): NewsResponse






}
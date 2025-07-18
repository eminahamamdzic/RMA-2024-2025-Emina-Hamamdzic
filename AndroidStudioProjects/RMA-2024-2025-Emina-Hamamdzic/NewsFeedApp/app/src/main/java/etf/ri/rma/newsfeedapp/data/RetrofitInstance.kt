package etf.ri.rma.newsfeedapp.data


import etf.ri.rma.newsfeedapp.data.network.api.ImagaApiService
import etf.ri.rma.newsfeedapp.data.network.api.NewsApiService
import okhttp3.Credentials
import okhttp3.OkHttpClient
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory


object RetrofitInstance {

    private const val BASE_URL = "https://api.thenewsapi.com/v1/"
    private const val IMAGGA_API_KEY = "acc_0e4f4cb4e7262f9"
    private const val IMAGGA_API_SECRET = "d1d5c60d6df6e6bf980fa776a52528be"



    val newsApi: NewsApiService by lazy {
        Retrofit.Builder()
            .baseUrl(BASE_URL)
            .addConverterFactory(GsonConverterFactory.create())
            .build()
            .create(NewsApiService::class.java)
    }

    private val imaggaClient: OkHttpClient by lazy {
        OkHttpClient.Builder()
            .addInterceptor { chain ->
                val credentials = Credentials.basic(IMAGGA_API_KEY, IMAGGA_API_SECRET)
                val request = chain.request().newBuilder()
                    .addHeader("Authorization", credentials)
                    .build()
                chain.proceed(request)
            }
            .build()
    }

    val imaggaApi: ImagaApiService by lazy {
        Retrofit.Builder()
            .baseUrl("https://api.imagga.com/v2/")
            .client(imaggaClient)
            .addConverterFactory(GsonConverterFactory.create())
            .build()
            .create(ImagaApiService::class.java)
    }


}
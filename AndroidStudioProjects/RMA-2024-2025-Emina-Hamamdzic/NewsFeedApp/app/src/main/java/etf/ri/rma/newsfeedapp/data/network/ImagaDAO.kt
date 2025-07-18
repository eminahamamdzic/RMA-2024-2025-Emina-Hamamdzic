package etf.ri.rma.newsfeedapp.data.network


import android.util.Log
import android.util.Patterns
import etf.ri.rma.newsfeedapp.data.network.api.ImagaApiService
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext


class ImagaDAO(private var api: ImagaApiService? = null) {

    fun setApiService(service: ImagaApiService) {
        this.api = service
    }

    private val tagCache = mutableMapOf<String, List<String>>()

    suspend fun getTags(imageUrl: String): List<String> = withContext(Dispatchers.IO) {
        if (!Patterns.WEB_URL.matcher(imageUrl).matches()) {
            throw InvalidImageURLException("Neispravan URL slike: $imageUrl")
        }

        if (tagCache.containsKey(imageUrl)) {
            return@withContext tagCache[imageUrl]!!
        }

        val service = this@ImagaDAO.api ?: throw IllegalStateException("API nije inicijaliziran")

        try {
            val response = service.getTags(imageUrl)
            Log.d("IMAGGA_RESPONSE", response.toString())

            val tags = response.result.tags.mapNotNull { it.tag.en?.takeIf { en -> en.isNotBlank() } }


            Log.d("IMAGGA", "Dohvaćeni tagovi: $tags")

            tagCache[imageUrl] = tags
            return@withContext tags
        } catch (e: Exception) {
            Log.e("IMAGGA", "Greška prilikom dohvata tagova: ${e.message}")
            return@withContext emptyList()
        }
    }
}

package etf.ri.rma.newsfeedapp.data.network


import android.util.Log
import etf.ri.rma.newsfeedapp.data.network.api.NewsApiService
import etf.ri.rma.newsfeedapp.data.network.exception.InvalidUUIDException
import etf.ri.rma.newsfeedapp.data.NewsDatabase
import etf.ri.rma.newsfeedapp.data.SavedNewsDAO
import etf.ri.rma.newsfeedapp.db.entity.NewsTagCrossRef
import etf.ri.rma.newsfeedapp.db.entity.TagEntity
import etf.ri.rma.newsfeedapp.dto.toEntity
import etf.ri.rma.newsfeedapp.dto.toNewsItem
import etf.ri.rma.newsfeedapp.model.NewsItem
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext



class NewsDAO(private var api: NewsApiService? = null) {

    fun setApiService(service: NewsApiService) {
        this.api = service
    }

    private val allStories = mutableListOf<NewsItem>()
    private val categoryLastFetch = mutableMapOf<String, Long>()
    val similarStoriesCache = mutableMapOf<String, List<NewsItem>>()

    companion object {
        private const val API_KEY = "WwJfL3BClhy1T3sUtRPKeJv3qQOr6KGq6sSCxsle"
    }
    fun loadLocalStories(local: List<NewsItem>) {
        if (allStories.isEmpty()) {
            allStories.addAll(local)
        }
    }
    suspend fun getTopStoriesByCategory(category: String, dao: SavedNewsDAO): List<NewsItem> = withContext(Dispatchers.IO) {


    val api = this@NewsDAO.api ?: throw IllegalStateException("API service not initialized")
        val now = System.currentTimeMillis()
        val lastCall = categoryLastFetch[category] ?: 0L


        if (now - lastCall <= 30_000) {
            return@withContext allStories.filter { it.category == category }
        }
        Log.d("NEWS_FETCH", "Posljednji fetch za $category: $lastCall, sada: $now, razlika: ${now - lastCall}")

        val response = api.getTopStoriesByCategory(API_KEY, category)
        val newStories = response.items.map { it.toNewsItem(category) }.take(3)

        newStories.forEach { newItem ->
            val existing = allStories.find { it.uuid == newItem.uuid }
            if (existing != null) {
                existing.isFeatured = true
                allStories.remove(existing)
                allStories.add(0, existing)
            } else {
                allStories.add(0, newItem.copy(isFeatured = true))
            }

            val id = dao.saveNews(newItem.toEntity())


            val matchedTag = dao.getAllTags().find { it.value.equals(newItem.category, ignoreCase = true) }
            val tagId = matchedTag?.id ?: dao.addTag(TagEntity(value = newItem.category))

            dao.addNewsTagCrossRef(NewsTagCrossRef(newsId = id, tagId = tagId))



        }

        allStories.filter {
            it.category == category && newStories.none { new -> new.uuid == it.uuid }
        }.forEach { it.isFeatured = false }

        categoryLastFetch[category] = now

        return@withContext allStories
            .filter { it.category == category }
            .sortedWith(compareByDescending<NewsItem> { it.isFeatured }
                .thenByDescending { it.publishedDate }).take(3)
    }


    suspend fun getSimilarStories(uuid: String): List<NewsItem> = withContext(Dispatchers.IO) {
        if (!uuid.matches(Regex("^[a-fA-F0-9\\-]{36}$")))
            throw InvalidUUIDException("UUID nije validan")

        similarStoriesCache[uuid]?.let { return@withContext it }

        val api = this@NewsDAO.api ?: throw IllegalStateException("API service not initialized")
        val response = api.getSimilarStories(API_KEY, uuid)
        val result = response.items.map { it.toNewsItem() }.take(2)
        similarStoriesCache[uuid] = result
        return@withContext result
    }

    fun getAllStories(): List<NewsItem> = allStories

    fun addStory(item: NewsItem) { allStories.add(item) }
}

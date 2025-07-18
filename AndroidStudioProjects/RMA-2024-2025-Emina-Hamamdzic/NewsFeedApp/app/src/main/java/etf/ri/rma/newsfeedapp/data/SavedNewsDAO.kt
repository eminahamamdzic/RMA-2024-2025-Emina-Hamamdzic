package etf.ri.rma.newsfeedapp.data

import android.util.Log
import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Transaction
import etf.ri.rma.newsfeedapp.db.entity.NewsEntity
import etf.ri.rma.newsfeedapp.db.entity.NewsTagCrossRef
import etf.ri.rma.newsfeedapp.db.entity.TagEntity
import etf.ri.rma.newsfeedapp.db.relations.NewsWithTags
import etf.ri.rma.newsfeedapp.dto.toEntity
import etf.ri.rma.newsfeedapp.dto.toNewsItem
import etf.ri.rma.newsfeedapp.model.NewsItem

@Dao
interface SavedNewsDAO {

    @Insert(onConflict = OnConflictStrategy.Companion.REPLACE)
    suspend fun saveNews(news: NewsEntity): Long

    @Insert(onConflict = OnConflictStrategy.Companion.REPLACE)
    suspend fun addTag(tag: TagEntity): Long

    @Insert(onConflict = OnConflictStrategy.Companion.IGNORE)
    suspend fun addNewsTagCrossRef(crossRef: NewsTagCrossRef)

    @Transaction
    @Query("SELECT * FROM News")
    suspend fun allNewsInternal(): List<NewsWithTags>

    @Transaction
    @Query("SELECT * FROM News WHERE category = :category")
    suspend fun getNewsWithCategoryInternal(category: String): List<NewsWithTags>

    @Transaction
    @Query("""
        SELECT * FROM News 
        INNER JOIN NewsTags ON News.id = NewsTags.newsId
        INNER JOIN Tags ON Tags.id = NewsTags.tagId
        WHERE Tags.value IN (:tags)
        GROUP BY News.id
    """)
    suspend fun getNewsByTags(tags: List<String>): List<NewsWithTags>

    @Transaction
    @Query("""
        SELECT * FROM News 
        WHERE category = :category 
        AND id != :newsId
        ORDER BY publishedDate DESC
        LIMIT 3
    """)
    suspend fun getSimilarNews(category: String, newsId: Long): List<NewsWithTags>

    @Query("SELECT * FROM Tags")
    suspend fun getAllTags(): List<TagEntity>

    @Query("SELECT * FROM News WHERE uuid = :uuid LIMIT 1")
    suspend fun getByUUID(uuid: String): NewsEntity?

    @Insert(onConflict = OnConflictStrategy.Companion.IGNORE)
    suspend fun insertNews(news: NewsEntity): Long

    @Transaction
    @Query("SELECT * FROM News WHERE id = :newsId")
    suspend fun getTagsInternal(newsId: Int): NewsWithTags?

    @Transaction
    @Query("""
    SELECT DISTINCT News.* FROM News 
    INNER JOIN NewsTags ON News.id = NewsTags.newsId
    INNER JOIN Tags ON Tags.id = NewsTags.tagId
    WHERE Tags.value IN (:tags)
    ORDER BY 
        CASE WHEN publishedDate IS NULL OR publishedDate = '' THEN 1 ELSE 0 END,
        publishedDate DESC
""")
    suspend fun getSimilarNewsInternal(tags: List<String>): List<NewsEntity>



    @Transaction
    suspend fun saveNews(news: NewsItem): Boolean {
        val existing = getByUUID(news.uuid)
        if (existing != null) return false
        insertNews(news.toEntity())
        return true
    }

    @Transaction
    suspend fun addTags(tags: List<String>, newsId: Int): Int {
        val allTags = getAllTags()
        var newTagsCount = 0

        for (tag in tags.distinctBy { it.lowercase() }) {
            val existing = allTags.find { it.value.equals(tag, ignoreCase = true) }
            Log.d("TAG_INSERT", "Obrađujem tag: $tag")

            val tagId = if (existing != null) {
                existing.id.toLong()
            } else {
                newTagsCount++
                val insertedId = addTag(TagEntity(value = tag))
                insertedId
            }

            addNewsTagCrossRef(NewsTagCrossRef(newsId = newsId.toLong(), tagId = tagId))
        }

        return newTagsCount
    }

    @Transaction
    suspend fun allNews(): List<NewsItem> {
        return allNewsInternal().map { rel ->
            rel.news.toNewsItem().copy(imageTags = ArrayList(rel.tags))
        }
    }

    @Transaction
    suspend fun getNewsWithCategory(category: String): List<NewsItem> {
        return getNewsWithCategoryInternal(category).map { rel ->
            rel.news.toNewsItem().copy(imageTags = ArrayList(rel.tags))
        }
    }

    @Transaction
    suspend fun getTags(newsId: Int): List<String> {

        return getTagsInternal(newsId)?.tags?.map { it.value } ?: emptyList()
    }

    @Transaction
    suspend fun getSimilarNews(tags: List<String>): List<NewsItem> {
        return getSimilarNewsInternal(tags).map { it.toNewsItem() }
    }


}
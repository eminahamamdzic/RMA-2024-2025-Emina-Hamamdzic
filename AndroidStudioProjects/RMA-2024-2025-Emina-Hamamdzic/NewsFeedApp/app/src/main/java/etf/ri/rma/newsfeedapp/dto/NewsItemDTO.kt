package etf.ri.rma.newsfeedapp.dto


import com.google.gson.annotations.SerializedName
import etf.ri.rma.newsfeedapp.db.entity.NewsEntity
import etf.ri.rma.newsfeedapp.model.NewsItem
import java.text.SimpleDateFormat
import java.util.Locale
import java.util.TimeZone

data class NewsItemDTO(
    @SerializedName("uuid") val uuid: String,
    @SerializedName("title") val title: String,
    @SerializedName("snippet") val description: String?,
    @SerializedName("image_url") val imageUrl: String?,
    @SerializedName("published_at") val date: String,
    @SerializedName("source") val source: String?,
    @SerializedName("categories") val categoryList: List<String>?

)

fun NewsItem.toEntity(): NewsEntity = NewsEntity(
uuid = uuid,
title = title,
snippet = snippet,
imageUrl = imageUrl,
category = category,
isFeatured = isFeatured,
source = source,
publishedDate = publishedDate
)




fun NewsItemDTO.toNewsItem(categoryOverride: String? = null): NewsItem {
    val formattedDate = try {
        val inputFormat = SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss'Z'", Locale.getDefault())
        inputFormat.timeZone = TimeZone.getTimeZone("UTC")

        val parsedDate = inputFormat.parse(this.date)
        if (parsedDate != null) {
            val outputFormat = SimpleDateFormat("dd-MM-yyyy", Locale.getDefault())
            outputFormat.format(parsedDate)
        } else {
            this.date // možda je već u ispravnom formatu
        }
    } catch (e: Exception) {
        this.date // fallback ako je već formatiran datum
    }


    return NewsItem(
        uuid = this.uuid,
        title = this.title,
        snippet = this.description ?: "No description",
        imageUrl = this.imageUrl ?: "",
        category = categoryOverride ?: this.categoryList?.firstOrNull() ?: "Uncategorized",
        isFeatured = false,
        source = this.source ?: "Unknown",
        publishedDate = formattedDate,
        imageTags = arrayListOf()
    )
}





package etf.ri.rma.newsfeedapp.dto

import etf.ri.rma.newsfeedapp.db.entity.NewsEntity
import etf.ri.rma.newsfeedapp.model.NewsItem

fun NewsEntity.toNewsItem(): NewsItem {
    return NewsItem(
        uuid = this.uuid,
        title = this.title,
        snippet = this.snippet,
        imageUrl = this.imageUrl,
        category = this.category,
        isFeatured = this.isFeatured,
        source = this.source,
        publishedDate = this.publishedDate,
        imageTags = arrayListOf() // tagovi se dodaju posebno
    )
}

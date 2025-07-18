package etf.ri.rma.newsfeedapp.db.relations

import androidx.room.Embedded
import androidx.room.Junction
import androidx.room.Relation
import etf.ri.rma.newsfeedapp.db.entity.NewsEntity
import etf.ri.rma.newsfeedapp.db.entity.NewsTagCrossRef
import etf.ri.rma.newsfeedapp.db.entity.TagEntity

data class NewsWithTags(
    @Embedded val news: NewsEntity,
    @Relation(
        parentColumn = "id",
        entityColumn = "id",
        associateBy = Junction(
            value = NewsTagCrossRef::class,
            parentColumn = "newsId",
            entityColumn = "tagId"
        )
    )
    val tags: List<TagEntity>
)

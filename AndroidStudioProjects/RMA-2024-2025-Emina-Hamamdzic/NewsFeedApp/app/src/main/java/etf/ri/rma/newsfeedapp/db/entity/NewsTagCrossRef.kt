package etf.ri.rma.newsfeedapp.db.entity

import androidx.room.Entity
import androidx.room.ForeignKey

@Entity(
    tableName = "NewsTags",
    primaryKeys = ["newsId", "tagId"],
    foreignKeys = [
        ForeignKey(
            entity = NewsEntity::class,
            parentColumns = ["id"],
            childColumns = ["newsId"],
            onDelete = ForeignKey.CASCADE
        ),
        ForeignKey(
            entity = TagEntity::class,
            parentColumns = ["id"],
            childColumns = ["tagId"],
            onDelete = ForeignKey.CASCADE
        )
    ]
)
data class NewsTagCrossRef(
    val newsId: Long,
    val tagId: Long
)

package etf.ri.rma.newsfeedapp.db.entity

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "Tags")
data class TagEntity(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val value: String
)

package etf.ri.rma.newsfeedapp.data

import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase
import etf.ri.rma.newsfeedapp.db.entity.NewsEntity
import etf.ri.rma.newsfeedapp.db.entity.NewsTagCrossRef
import etf.ri.rma.newsfeedapp.db.entity.TagEntity
import etf.ri.rma.newsfeedapp.dto.toEntity
import etf.ri.rma.newsfeedapp.dto.toNewsItem
import etf.ri.rma.newsfeedapp.model.NewsItem

@Database(
    entities = [NewsEntity::class, TagEntity::class, NewsTagCrossRef::class],
    version = 1,
    exportSchema = false
)

abstract class NewsDatabase : RoomDatabase() {

    abstract fun savedNewsDAO(): SavedNewsDAO

    companion object {
        @Volatile
        private var INSTANCE: NewsDatabase? = null

        fun getDatabase(context: Context): NewsDatabase {
            return INSTANCE ?: synchronized(this) {
                val instance = Room.databaseBuilder(
                    context.applicationContext,
                    NewsDatabase::class.java,
                    "news-db"
                ) .fallbackToDestructiveMigration()
                    .build()
                INSTANCE = instance
                instance
            }
        }
    }
}
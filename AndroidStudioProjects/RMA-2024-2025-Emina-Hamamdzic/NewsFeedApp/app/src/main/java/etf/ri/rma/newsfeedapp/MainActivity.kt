package etf.ri.rma.newsfeedapp

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.runtime.LaunchedEffect
import etf.ri.rma.newsfeedapp.data.RetrofitInstance
import etf.ri.rma.newsfeedapp.navigation.NewsFeedNavGraph
import etf.ri.rma.newsfeedapp.ui.theme.NewsFeedAppTheme
import etf.ri.rma.newsfeedapp.data.network.newsDAO
import etf.ri.rma.newsfeedapp.data.NewsDatabase
import etf.ri.rma.newsfeedapp.db.entity.NewsTagCrossRef
import etf.ri.rma.newsfeedapp.db.entity.TagEntity
import etf.ri.rma.newsfeedapp.dto.toEntity

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {
            NewsFeedAppTheme {
                LaunchedEffect(Unit) {
                    // ROOM BAZA
                    val db = NewsDatabase.getDatabase(applicationContext)
                    val dao = db.savedNewsDAO()

                    // Postavljanje API servisa
                    newsDAO.setApiService(RetrofitInstance.newsApi)

                    // Učitavanje lokalnih vijesti (dummy data)
                    val newsList = etf.ri.rma.newsfeedapp.data.NewsData.getAllNews()
                    newsDAO.loadLocalStories(newsList)
                }
                NewsFeedNavGraph()
            }
        }
    }
}


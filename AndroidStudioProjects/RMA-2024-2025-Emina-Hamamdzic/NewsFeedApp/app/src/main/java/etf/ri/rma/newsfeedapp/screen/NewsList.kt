package etf.ri.rma.newsfeedapp.screen

import android.util.Log
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.unit.dp
import androidx.navigation.NavController
import etf.ri.rma.newsfeedapp.model.NewsItem


@Composable
fun NewsList(newsItems: List<NewsItem>, navController: NavController){
    // PROVJERA DUPLIKATA PO UUID
    LaunchedEffect(newsItems) {
        val duplikati = newsItems.groupBy { it.uuid }.filter { it.value.size > 1 }
        if (duplikati.isNotEmpty()) {
            Log.d("DUPLIKATI", "Nađeni duplikati: ${duplikati.keys}")
        }
    }

    LazyColumn(
        modifier= Modifier.run {
            fillMaxSize()
                .testTag("news_list")
        },
        contentPadding = PaddingValues(8.dp),
        verticalArrangement = Arrangement.spacedBy(12.dp)
    ){
        items(newsItems.distinctBy { it.uuid }, key = { it.uuid }) { newsItem ->
        if (newsItem.isFeatured) {
                FeaturedNewsCard(newsItem = newsItem, navController = navController)
            } else {
                StandardNewsCard(newsItem = newsItem, navController = navController)
            }
        }

    }
}
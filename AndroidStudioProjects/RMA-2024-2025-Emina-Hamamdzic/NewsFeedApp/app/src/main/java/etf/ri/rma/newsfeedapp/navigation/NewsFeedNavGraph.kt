package etf.ri.rma.newsfeedapp.navigation

import androidx.compose.runtime.Composable
import androidx.compose.ui.platform.LocalContext
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import etf.ri.rma.newsfeedapp.data.NewsDatabase
import etf.ri.rma.newsfeedapp.dto.toNewsItem
import etf.ri.rma.newsfeedapp.screen.NewsDetailsScreen
import etf.ri.rma.newsfeedapp.screen.NewsFeedScreen
import etf.ri.rma.newsfeedapp.screen.FilterScreen
import kotlinx.coroutines.runBlocking


@Composable
fun NewsFeedNavGraph(){
    val navController= rememberNavController()

    NavHost( navController=navController, startDestination ="home") {

        composable("home") {
            NewsFeedScreen(navController)
        }
        composable("filters") {
            FilterScreen(navController)
        }
        composable("details/{uuid}") { backStackEntry ->
            val uuid = backStackEntry.arguments?.getString("uuid")
            val context = LocalContext.current
            val db = NewsDatabase.getDatabase(context)
            val entity = runBlocking {
                db.savedNewsDAO().getByUUID(uuid!!)
            }

            val newsItem = entity?.toNewsItem()

            if (newsItem != null) {
                NewsDetailsScreen(newsItem = newsItem, navController = navController)
            }
        }





    }
}
package etf.ri.rma.newsfeedapp.screen


import android.util.Log
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.semantics.testTag
import androidx.compose.ui.unit.dp
import androidx.navigation.NavController
import java.util.TimeZone
import java.text.SimpleDateFormat
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.sp
import coil.compose.AsyncImage
import etf.ri.rma.newsfeedapp.data.network.newsDAO
import etf.ri.rma.newsfeedapp.data.network.isConnected
import etf.ri.rma.newsfeedapp.data.NewsDatabase
import etf.ri.rma.newsfeedapp.dto.toNewsItem
import etf.ri.rma.newsfeedapp.data.RetrofitInstance
import etf.ri.rma.newsfeedapp.data.network.ImagaDAO
import etf.ri.rma.newsfeedapp.model.NewsItem
import kotlinx.coroutines.launch
import java.util.Locale


@Composable
fun NewsDetailsScreen(newsItem: NewsItem, navController: NavController) {
    val context = LocalContext.current
    val imagaDAO = remember { ImagaDAO(RetrofitInstance.imaggaApi) }


    val coroutineScope = rememberCoroutineScope()

    val db = remember { NewsDatabase.getDatabase(context) }
    val dao = remember { db.savedNewsDAO() }

    val publishedDateFormatted = try {
        val inputFormat = SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss.SSS'Z'", Locale.getDefault())
        inputFormat.timeZone = TimeZone.getTimeZone("UTC")
        val outputFormat = SimpleDateFormat("dd-MM-yyyy", Locale.getDefault())
        val date = inputFormat.parse(newsItem.publishedDate)
        if (date != null) outputFormat.format(date) else newsItem.publishedDate
    } catch (e: Exception) {
        Log.e("DATUM", "Greška prilikom parsiranja: ${e.message}")
        newsItem.publishedDate ?: "Nepoznat datum"
    }


    var imageTags by remember { mutableStateOf<List<String>>(emptyList()) }
    var relatedNews by remember { mutableStateOf<List<NewsItem>>(emptyList()) }


    LaunchedEffect(newsItem.imageUrl) {
        try {
            if (!newsItem.imageUrl.isNullOrBlank() && !newsItem.imageUrl.endsWith(".ico") &&
                newsItem.imageUrl.startsWith("http")
            ) {
                imageTags = imagaDAO.getTags(newsItem.imageUrl)

            } else {
                imageTags = listOf("Greška pri dohvatu tagova")
            }
        } catch (e: Exception) {
            Log.e("TAGOVI", "Greška: ${e.message}")
            imageTags = listOf("Greška pri dohvatu tagova")
        }
    }





    LaunchedEffect(newsItem.uuid) {
        coroutineScope.launch {
            try {
                if (isConnected(context)) {
                    val result = try {
                        val fetched = newsDAO.getSimilarStories(newsItem.uuid)
                        if (fetched.isNotEmpty()) {
                            fetched.forEach {
                                dao.saveNews(it)
                                val id = dao.getByUUID(it.uuid)?.id
                                if (id != null) {
                                    val tags = imagaDAO.getTags(it.imageUrl ?: "")
                                    dao.addTags(tags, id.toInt())
                                }
                            }
                            fetched
                        } else {
                            newsDAO.getAllStories()
                                .filter { it.category == newsItem.category && it.uuid != newsItem.uuid }
                                .take(2)
                        }
                    } catch (e: Exception) {
                        newsDAO.getAllStories()
                            .filter { it.category == newsItem.category && it.uuid != newsItem.uuid }
                            .take(2)
                    }
                    relatedNews = result
                } else {
                    val id = dao.getByUUID(newsItem.uuid)?.id ?: return@launch
                    val tags = dao.getTags(id.toInt()).take(2)
                    val localSimilar = dao.getSimilarNews(tags)
                        .filter { it.uuid != newsItem.uuid }
                        .take(3)
                    relatedNews = localSimilar
                }
            } catch (e: Exception) {
                Log.e("SIMILAR", "Greška: ${e.message}")
            }
        }
    }



    Column(
        modifier = Modifier
            .fillMaxSize()
            .verticalScroll(rememberScrollState())
            .padding(horizontal = 16.dp, vertical = 32.dp),
        verticalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        Text(newsItem.title, fontSize = 20.sp, fontWeight = FontWeight.Bold)
        Text(newsItem.snippet, fontSize = 16.sp)

        if (!newsItem.imageUrl.isNullOrBlank()) {
            AsyncImage(
                model = newsItem.imageUrl,
                contentDescription = "Slika vijesti",
                contentScale = ContentScale.Crop,
                modifier = Modifier.fillMaxWidth().height(200.dp)
            )
        }

        Row { Text("Kategorija: ", fontWeight = FontWeight.Bold); Text(newsItem.category) }
        Row { Text("Izvor: ", fontWeight = FontWeight.Bold); Text(newsItem.source) }
        Row { Text("Datum: ", fontWeight = FontWeight.Bold); Text(publishedDateFormatted) }

        Text("Tagovi slike:", fontWeight = FontWeight.Bold)
        if (imageTags.isEmpty()) {
            Text("Nema tagova za prikaz.")
        } else {
            Column(verticalArrangement = Arrangement.spacedBy(4.dp)) {
                imageTags.forEachIndexed { index, tag ->
                    Text("• $tag", modifier = Modifier.semantics {
                        testTag = "details_image_tag_${index + 1}"
                    })
                }
            }
        }

        Spacer(modifier = Modifier.height(16.dp))
        Text("Povezane vijesti iz iste kategorije", fontWeight = FontWeight.Bold)

        if (relatedNews.isEmpty()) {
            Text("Nema povezanih vijesti za prikaz.")
        } else {
            relatedNews.forEachIndexed { index, item ->
                Text(
                    text = item.title,
                    modifier = Modifier
                        .clickable {
                            coroutineScope.launch { dao.saveNews(item) }
                            navController.navigate("details/${item.uuid}")
                        }
                        .padding(vertical = 4.dp)
                        .semantics { testTag = "related_news_title_${index + 1}" }
                )
            }
        }

        Spacer(modifier = Modifier.weight(1f))

        Button(
            onClick = {
                coroutineScope.launch { dao.saveNews(newsItem) }
                navController.navigate("home") {
                    popUpTo("home") { inclusive = true }
                    launchSingleTop = true
                }
            },
            modifier = Modifier.align(Alignment.CenterHorizontally),
            colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF4CAF50)),
            shape = MaterialTheme.shapes.small
        ) {
            Text("Zatvori detalje")
        }
    }
}
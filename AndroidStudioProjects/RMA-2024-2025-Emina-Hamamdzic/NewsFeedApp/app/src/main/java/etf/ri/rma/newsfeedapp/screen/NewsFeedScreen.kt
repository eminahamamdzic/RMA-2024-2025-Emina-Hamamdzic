package etf.ri.rma.newsfeedapp.screen

import android.util.Log
import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.ui.Modifier
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.semantics.testTag
import androidx.compose.ui.unit.dp
import androidx.navigation.NavController
import androidx.navigation.compose.currentBackStackEntryAsState
import etf.ri.rma.newsfeedapp.model.NewsItem
import java.text.SimpleDateFormat
import etf.ri.rma.newsfeedapp.data.network.newsDAO
import java.util.*
import etf.ri.rma.newsfeedapp.data.NewsDatabase
import etf.ri.rma.newsfeedapp.data.network.isConnected
import androidx.compose.ui.platform.LocalContext
import etf.ri.rma.newsfeedapp.dto.toNewsItem


@Composable
fun NewsFeedScreen(navController: NavController) {
    val context = LocalContext.current
    val db = NewsDatabase.getDatabase(context).savedNewsDAO()

    var newsToShow by remember { mutableStateOf<List<NewsItem>>(emptyList()) }


    val navBackStackEntry by navController.currentBackStackEntryAsState()
    val selectedCategoryState = rememberSaveable { mutableStateOf("Sve") }
    val selectedCategory by selectedCategoryState

    // Primijeni promjenu iz FilterScreen
    LaunchedEffect(navBackStackEntry?.savedStateHandle) {
        navBackStackEntry?.savedStateHandle?.get<String>("filters_category")?.let {
            selectedCategoryState.value = it
        }
    }
    fun formatDate(isoDate: String): String {
        return try {
            val inputFormat = SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss.SSSSSS'Z'", Locale.getDefault())
            inputFormat.timeZone = TimeZone.getTimeZone("UTC")
            val outputFormat = SimpleDateFormat("dd.MM.yyyy.", Locale.getDefault())
            val date = inputFormat.parse(isoDate)
            if (date != null) outputFormat.format(date) else isoDate
        } catch (e: Exception) {
            isoDate
        }
    }


    // Filtriraj po kategoriji
    LaunchedEffect(selectedCategory) {
        val mapped = when (selectedCategory) {
            "Sport" -> "sports"
            "Nauka" -> "science"
            "Tehnologija" -> "tech"
            "Politika" -> "politics"
            "Obrazovanje" -> "entertainment"
            "Sve" -> "general"
            else -> "general"
        }
        Log.d("KATEGORIJA", "Kliknuta kategorija: $selectedCategory (mapped: $mapped)")
        val connected = isConnected(context)

        try {
            if (isConnected(context)) {
                val apiNews = newsDAO.getTopStoriesByCategory(mapped, db)

                val allLocal = db.allNews()
                allLocal.forEach {
                    val updated = it.copy(isFeatured = false)
                    db.saveNews(updated)
                }

                val featured = apiNews.take(3).map { it.copy(isFeatured = true) }
                val others = apiNews.drop(3)

                val all = (featured + others).distinctBy { it.uuid }
                all.forEach { db.saveNews(it) }

                newsToShow = db.getNewsWithCategory(mapped)
            } else {
                newsToShow = db.getNewsWithCategory(mapped)
            }

            Log.d("VIJESTI", "Prikazujem ${newsToShow.size} vijesti za $mapped")
        } catch (e: Exception) {
            Log.e("API_GRESKA", "Neuspješno dohvaćanje: ${e.message}")
            newsToShow = db.getNewsWithCategory(mapped)
        }

    }

    // nepoželjne riječi i datum
    val unwantedWords = navBackStackEntry
        ?.savedStateHandle?.get<String>("filters_badwords")
        ?.split(",")?.filter { it.isNotBlank() } ?: emptyList()

    val dateRange = navBackStackEntry?.savedStateHandle?.get<Pair<Long, Long>>("filters_daterange")

    val filteredNews = newsToShow.filter { news ->
        unwantedWords.none { badWord ->
            news.title.contains(badWord, ignoreCase = true) || news.snippet.contains(badWord, ignoreCase = true)
        } && (dateRange == null || run {
            val newsDate = try {
                SimpleDateFormat("dd-MM-yyyy", Locale.getDefault()).parse(news.publishedDate)?.time
            } catch (e: Exception) { null }
            newsDate != null && newsDate in dateRange.first..dateRange.second
        })
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        FilterChips(
            navController = navController,
            selectedCategory = selectedCategory,
            onCategorySelected = { category ->
                selectedCategoryState.value = category
                navController.currentBackStackEntry?.savedStateHandle?.set("filters_category", category)
            }
        )

        if (filteredNews.isEmpty()) {
            MessageCard("Nema pronađenih vijesti u kategoriji [$selectedCategory]")
        } else {
            NewsList(newsItems = filteredNews, navController = navController)
        }
    }
    LaunchedEffect(Unit) {
        val sveVijesti = db.allNews()
        Log.d("BAZA", "Ukupno sačuvanih vijesti u bazi: ${sveVijesti.size}")
    }




}

@Composable
fun FilterChips(
    navController: NavController,
    selectedCategory: String,
    onCategorySelected: (String) -> Unit
) {
    val categoryTagMap = mapOf(
        "Sve" to "filter_chip_all",
        "Politika" to "filter_chip_pol",
        "Sport" to "filter_chip_spo",
        "Nauka" to "filter_chip_sci",
        "Tehnologija" to "filter_chip_tech",
        "Obrazovanje" to "filter_chip_none"
    )

    Column(
        modifier = Modifier
            .fillMaxWidth()
            .padding(14.dp),
        verticalArrangement = Arrangement.spacedBy(8.dp)
    ) {
        Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
            FilterChip(
                selected = false,
                onClick = { navController.navigate("filters") },
                label = { Text("Više filtera ...") },
                modifier = Modifier.semantics { this.testTag = "filter_chip_more" }
            )
            categoryTagMap.entries.take(3).forEach { (category, tag) ->
                FilterChip(
                    selected = selectedCategory == category,
                    onClick = { onCategorySelected(category) },
                    label = { Text(category) },
                    modifier = Modifier.semantics { this.testTag = tag }
                )
            }
        }
        Row(horizontalArrangement = Arrangement.spacedBy(8.dp), modifier = Modifier.padding(top = 4.dp)) {
            categoryTagMap.entries.drop(3).forEach { (category, tag) ->
                FilterChip(
                    selected = selectedCategory == category,
                    onClick = { onCategorySelected(category) },
                    label = { Text(category) },
                    modifier = Modifier.semantics { this.testTag = tag }
                )
            }
        }
    }
}

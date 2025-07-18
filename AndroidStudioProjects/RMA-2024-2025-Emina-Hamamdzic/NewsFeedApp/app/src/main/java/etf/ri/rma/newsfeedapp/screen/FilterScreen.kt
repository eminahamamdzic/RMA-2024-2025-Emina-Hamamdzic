package etf.ri.rma.newsfeedapp.screen

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.semantics.testTag
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import androidx.navigation.NavController
import etf.ri.rma.newsfeedapp.data.network.imagaDAO
import kotlinx.coroutines.launch
import java.text.SimpleDateFormat
import etf.ri.rma.newsfeedapp.data.network.newsDAO
import etf.ri.rma.newsfeedapp.data.NewsDatabase
import java.util.*

@Composable
fun FilterScreen(navController: NavController) {
    val context = LocalContext.current
    val db = NewsDatabase.getDatabase(context).savedNewsDAO()

    val categoryToApiMap = mapOf(
        "Sve" to "general",
        "Sport" to "sports",
        "Nauka" to "science",
        "Tehnologija" to "tech",
        "Politika" to "politics",
        "Obrazovanje" to "entertainment"
    )

    val previousCategory = navController.previousBackStackEntry
        ?.savedStateHandle?.get<String>("filters_category") ?: "Sve"

    var selectedCategory by remember { mutableStateOf(previousCategory) }
    var unwantedWord by remember { mutableStateOf("") }
    val unwantedWordsList = remember { mutableStateListOf<String>() }

    val scope = rememberCoroutineScope()
    var showDateDialog by remember { mutableStateOf(false) }
    var dateRange by remember { mutableStateOf<Pair<Long, Long>?>(null) }

    val dateFormatter = SimpleDateFormat("dd-MM-yyyy", Locale.getDefault())

    val scrollState = rememberScrollState()

    Column(
        modifier = Modifier
            .fillMaxSize()
            .verticalScroll(scrollState)
            .padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        // CHIPOVI
        Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                categoryToApiMap.keys.take(3).forEach { category ->
                    FilterChip(
                        selected = selectedCategory == category,
                        onClick = {
                            selectedCategory = category
                            navController.previousBackStackEntry?.savedStateHandle?.set("filters_category", category)

                            scope.launch {
                                val stories = newsDAO.getTopStoriesByCategory(categoryToApiMap[category]!!, db)

                                stories.forEach {
                                    val added = db.saveNews(it)
                                    if (added && it.imageUrl != null) {
                                        try {
                                            val tags = imagaDAO.getTags(it.imageUrl)
                                            val id = db.getByUUID(it.uuid)?.id?.toInt()
                                            if (id != null) {
                                                db.addTags(tags, id)
                                            }
                                        } catch (_: Exception) {}
                                    }
                                }
                            }
                        },
                        label = { Text(category) },
                        modifier = Modifier.semantics { testTag = "filter_chip_${category.lowercase()}" }
                    )
                }
            }
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                categoryToApiMap.keys.drop(3).forEach { category ->
                    FilterChip(
                        selected = selectedCategory == category,
                        onClick = {
                            selectedCategory = category
                            navController.previousBackStackEntry?.savedStateHandle?.set("filters_category", category)

                            scope.launch {
                                val stories = newsDAO.getTopStoriesByCategory(categoryToApiMap[category]!!, db)
                                stories.forEach {
                                    val added = db.saveNews(it)
                                    if (added && it.imageUrl != null) {
                                        try {
                                            val tags = imagaDAO.getTags(it.imageUrl)
                                            val id = db.getByUUID(it.uuid)?.id?.toInt()
                                            if (id != null) {
                                                db.addTags(tags, id)
                                            }
                                        } catch (_: Exception) {}
                                    }
                                }
                            }
                        },
                        label = { Text(category) },
                        modifier = Modifier.semantics { testTag = "filter_chip_${category.lowercase()}" }
                    )
                }
            }
        }

        // DATUMI
        if (dateRange != null) {
            Text("Raspon datuma")
            Text(
                text = dateRange?.let {
                    val start = Calendar.getInstance().apply { timeInMillis = it.first }
                    val end = Calendar.getInstance().apply { timeInMillis = it.second }
                    "${dateFormatter.format(start.time)};${dateFormatter.format(end.time)}"
                } ?: "",
                modifier = Modifier.semantics { testTag = "filter_daterange_display" }
            )
        }

        Button(
            onClick = { showDateDialog = true },
            modifier = Modifier.semantics { testTag = "filter_daterange_button" },
            colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF4CAF50))
        ) {
            Text("Izaberi datume")
        }

        if (showDateDialog) {
            DateRangeDialog(
                onDismissRequest = { showDateDialog = false },
                onDateSelected = { dateRange = it }
            )
        }

        // NEPOŽELJNE RIJEČI
        Text("Nepoželjne riječi")

        Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
            TextField(
                value = unwantedWord,
                onValueChange = { unwantedWord = it },
                modifier = Modifier
                    .weight(1f)
                    .semantics { testTag = "filter_unwanted_input" },
                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Text)
            )

            Button(
                onClick = {
                    if (unwantedWord.isNotBlank() && unwantedWordsList.none {
                            it.equals(unwantedWord, ignoreCase = true)
                        }
                    ) {
                        unwantedWordsList.add(unwantedWord)
                    }
                    unwantedWord = ""
                },
                modifier = Modifier.semantics { testTag = "filter_unwanted_add_button" },
                colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF4CAF50))
            ) {
                Text("Dodaj")
            }
        }

        Column(
            modifier = Modifier
                .fillMaxWidth()
                .semantics { testTag = "filter_unwanted_list" },
            verticalArrangement = Arrangement.spacedBy(4.dp)
        ) {
            unwantedWordsList.forEach { word ->
                Text(word)
            }
        }


        Spacer(modifier = Modifier.weight(1f))

        // DUGME PRIMIJENI
        Button(
            onClick = {
                navController.previousBackStackEntry?.savedStateHandle?.apply {
                    set("filters_category", selectedCategory)
                    set("filters_badwords", unwantedWordsList.joinToString(","))
                    dateRange?.let { set("filters_daterange", it) }
                }
                navController.popBackStack()
            },
            modifier = Modifier
                .fillMaxWidth()
                .semantics { testTag = "filter_apply_button" },
            colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF4CAF50))
        ) {
            Text("Primijeni filtere")
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun DateRangeDialog(
    onDismissRequest: () -> Unit,
    onDateSelected: (Pair<Long, Long>) -> Unit
) {
    val state = rememberDateRangePickerState()

    DatePickerDialog(
        onDismissRequest = onDismissRequest,
        confirmButton = {
            TextButton(onClick = {
                val start = state.selectedStartDateMillis
                val end = state.selectedEndDateMillis
                if (start != null && end != null) {
                    onDateSelected(Pair(start, end))
                }
                onDismissRequest()
            }) {
                Text("OK")
            }
        },
        dismissButton = {
            TextButton(onClick = onDismissRequest) {
                Text("Otkaži")
            }
        }
    ) {
        DateRangePicker(state = state, showModeToggle = false)
    }
}

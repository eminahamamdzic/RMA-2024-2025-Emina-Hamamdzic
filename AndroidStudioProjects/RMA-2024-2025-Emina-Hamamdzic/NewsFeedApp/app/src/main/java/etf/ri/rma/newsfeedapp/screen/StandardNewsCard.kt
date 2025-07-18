package etf.ri.rma.newsfeedapp.screen

import coil.compose.AsyncImage
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.navigation.NavController
import etf.ri.rma.newsfeedapp.data.TempState
import etf.ri.rma.newsfeedapp.model.NewsItem


@Composable
fun StandardNewsCard(newsItem: NewsItem, navController: NavController){

    val isClicked by remember { mutableStateOf(false) }

    Card(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 8.dp)
            .clickable { TempState.selectedNewsItem = newsItem
                navController.navigate("details/${newsItem.uuid}")
            },
        colors = CardDefaults.cardColors(
            containerColor = getCardColor(newsItem.category, isClicked = isClicked)

        ),
        elevation = CardDefaults.cardElevation(2.dp),
        shape = RoundedCornerShape(8.dp)
    )
    {
        Row(modifier= Modifier.padding(8.dp)){

            AsyncImage(
                model = newsItem.imageUrl,
                contentDescription = "image",
                contentScale = ContentScale.Crop,
                modifier = Modifier.size(75.dp)
            )

            Spacer(modifier = Modifier.width(4.dp))

            Column(modifier= Modifier.weight(1f)){
                Text(
                    text= newsItem.title,
                    style= MaterialTheme.typography.titleSmall,
                    maxLines = 2,
                    overflow = TextOverflow.Ellipsis
                )
                Spacer(modifier = Modifier.height(4.dp))

                Text(
                    text=newsItem.snippet,
                    style= MaterialTheme.typography.bodyMedium,
                    maxLines = 2
                )
                Spacer(modifier= Modifier.height(8.dp))

                Text(
                    text= "${newsItem.source} • ${newsItem.publishedDate}",
                    style =  MaterialTheme.typography.labelSmall,
                    color= MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
        }
    }
}
@Composable
fun getCardColor(category: String, isClicked: Boolean): Color {
    if (isClicked) return Color(0xFFD3D3D3)

    return when (category) {
        "Sport" -> Color(0xFFADD8E6)
        "Nauka" -> Color(0xFF90EE90)
        "Tehnologija"-> Color(0xFF90EE90)
        "Politika" -> Color(0xFFE6E6FA)
        else -> MaterialTheme.colorScheme.surface
    }
}

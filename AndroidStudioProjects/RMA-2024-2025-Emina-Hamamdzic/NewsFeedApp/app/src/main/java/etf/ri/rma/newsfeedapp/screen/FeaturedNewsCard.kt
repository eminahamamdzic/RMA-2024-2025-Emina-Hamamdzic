package etf.ri.rma.newsfeedapp.screen


import coil.compose.AsyncImage
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.navigation.NavController
import etf.ri.rma.newsfeedapp.model.NewsItem
import etf.ri.rma.newsfeedapp.data.TempState



@Composable
fun FeaturedNewsCard(newsItem: NewsItem, navController: NavController){

    Card(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 8.dp)
            .clickable { TempState.selectedNewsItem = newsItem
                navController.navigate("details/${newsItem.uuid}")
            },
        elevation = CardDefaults.cardElevation(6.dp)


    ) {
        Column(modifier= Modifier.padding(8.dp).background(Color(0xFFFFFACD))){

            AsyncImage(
                model = newsItem.imageUrl,
                contentDescription = "image",
                contentScale = ContentScale.Crop,
                modifier = Modifier
                    .fillMaxWidth()
                    .height(140.dp)
                    .clip(RoundedCornerShape(8.dp))
            )


            Spacer(modifier= Modifier.height(4.dp))

            Text(
                text = newsItem.title,
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.Bold,
                maxLines = 2

            )
            Spacer(modifier= Modifier.height(2.dp))

            Text(
                text= newsItem.snippet,
                style = MaterialTheme.typography.bodySmall,
                maxLines = 2
            )

            Spacer(modifier= Modifier.height(2.dp))

            Text(
                text= "${newsItem.source} • ${newsItem.publishedDate}",
                style = MaterialTheme.typography.labelSmall,
                color= MaterialTheme.colorScheme.onSurfaceVariant


            )
        }
    }
}





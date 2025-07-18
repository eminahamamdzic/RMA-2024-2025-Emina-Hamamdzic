package etf.ri.rma.newsfeedapp.dto

import com.google.gson.annotations.SerializedName


data class NewsResponse(
    @SerializedName("data") val items: List<NewsItemDTO>
)
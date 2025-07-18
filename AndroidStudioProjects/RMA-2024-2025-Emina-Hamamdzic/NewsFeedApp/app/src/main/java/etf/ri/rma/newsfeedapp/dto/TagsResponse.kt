package etf.ri.rma.newsfeedapp.dto


import com.google.gson.annotations.SerializedName

data class TagsResponse(
    @SerializedName("result") val result: TagResult
)

data class TagResult(
    @SerializedName("tags") val tags: List<TagItem>
)

data class TagItem(
    @SerializedName("tag") val tag: TagText
)

data class TagText(
    @SerializedName("en") val en: String
)
import androidx.compose.runtime.*
import kotlinx.serialization.json.JsonObject

object States {
    var currentTerm: String = ""
    var videos = mutableStateListOf<JsonObject>()
    var syncState: SyncState by mutableStateOf(SyncState.OUT_DATE)
    var pageState by mutableStateOf(PageState.INDEX)

    var queryType: Int = -1
    var queryVideos: List<Pair<String, String>>? = null

    var lessonNow by mutableStateOf("---")
    var lessons = mutableStateListOf<JsonObject>()
    var termNow by mutableStateOf("---")
    var terms = mutableStateListOf<JsonObject>()
    var cookie by mutableStateOf("")

}
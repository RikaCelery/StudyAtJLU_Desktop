
import androidx.compose.runtime.*
import kotlinx.coroutines.Job
import kotlinx.serialization.json.JsonObject
import utils.DB
import utils.conf.Conf
import java.io.File
import java.util.concurrent.ConcurrentHashMap

object States {
    var currentTerm: String = ""
    var videos = mutableStateListOf<JsonObject>()
    var syncState: SyncState by mutableStateOf(SyncState.OUT_DATE)
    var pageState by mutableStateOf(PageState.INDEX)
    var downloadFolder: File = File(".").canonicalFile
        get() = File(Conf.savePath).canonicalFile


    val progress = mutableStateMapOf<String, Float>()
    val progressInfo = mutableStateMapOf<String, String>()

    val tasks = ConcurrentHashMap<String, Job>()

    var currentJob: Job? = null

    var queryType: Int = -1
    var queryVideos: List<Pair<String, String>>? = null

    var lessonNow by mutableStateOf("---")
    var lessons = mutableStateListOf<JsonObject>()
    var termNow by mutableStateOf("---")
    var terms = mutableStateListOf<JsonObject>()
    var cookie: String by mutableStateOf(DB.getValue("cookie_cache")?:"")


}
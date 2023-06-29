
import androidx.compose.runtime.*
import kotlinx.coroutines.Job
import kotlinx.serialization.decodeFromString
import kotlinx.serialization.encodeToString
import kotlinx.serialization.json.Json
import kotlinx.serialization.json.JsonObject
import utils.DB
import utils.conf.Conf
import utils.objectArray
import java.io.File
import java.util.concurrent.ConcurrentHashMap

object States {
    var currentTerm: String = ""
    var videos = mutableStateListOf<JsonObject>()
    var syncState: SyncState by mutableStateOf(SyncState.OUT_DATE)
    var pageState by mutableStateOf(PageState.INDEX)
    val downloadFolder: File
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
    var cookie: String by mutableStateOf("")

    fun saveAll(){
        DB.setValue("currentTerm",currentTerm)
        DB.setValue("videos_cache", Json.Default.encodeToString<List<JsonObject>>(videos))
        DB.setValue("query_type", queryType.toString())
        DB.setValue("query_videos", Json.Default.encodeToString(queryVideos))
        DB.setValue("lesson_now", lessonNow)
        DB.setValue("lessons", Json.Default.encodeToString<List<JsonObject>>(lessons))
        DB.setValue("term_now", termNow)
        DB.setValue("terms", Json.Default.encodeToString<List<JsonObject>>(terms))
        DB.setValue("cookie_cache", cookie)
    }
    fun loadAll(){
        currentTerm = DB.getValue("currentTerm")?:""
        videos.addAll(DB.getValue("videos_cache")?.let { Json.parseToJsonElement(it).objectArray }?.toTypedArray()?: arrayOf())
        queryType = DB.getValue("query_type")?.toInt()?:-1
        queryVideos = DB.getValue("query_videos")?.let { Json.decodeFromString<List<Pair<String, String>>>(it) }?: listOf()
        lessonNow = DB.getValue("lesson_now")?:"---"
        lessons.addAll(DB.getValue("lessons")?.let { Json.parseToJsonElement(it).objectArray }?.toTypedArray()?: arrayOf())
        termNow = DB.getValue("term_now")?:"---"
        terms.addAll(DB.getValue("terms")?.let { Json.parseToJsonElement(it).objectArray }?.toTypedArray()?: arrayOf())
        cookie = DB.getValue("cookie_cache")?:""
    }
}
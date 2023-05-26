import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.material.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.Settings
import androidx.compose.runtime.Composable
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.unit.dp
import io.ktor.client.call.*
import io.ktor.client.request.*
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.serialization.json.JsonObject
import kotlinx.serialization.json.buildJsonObject
import kotlinx.serialization.json.put
import utils.*

suspend fun updateVideoList() {
    States.queryVideos?.runCatching {
        val body = client.get(QUERY_LIVE_AND_RECORD) {
            parameter("roomType", 0)
            parameter("identity", 2)
            forEach { parameter(it.first, it.second) }

            header("Cookie", States.cookie)
        }.body<JsonObject>()
        println(body)
        body.Object("data").Array("dataList").filter { it.StringOrNull("isOpen") != null }
    }?.onFailure {
        it.printStackTrace()
    }?.getOrNull()?.runCatching {
        States.videos.clear()
        States.videos.addAll(map {
            buildJsonObject {
                put("info", buildString {
                    append(it.String("courseName"))
                    append(":")
                    append(it.String("teacherName"))
                    append("\n  ")
                    append(
                        "${it.String("currentWeek")}周 " + "星期${it.String("currentDay")}" + "(${it.String("roomName")}) " + "${
                            it.String(
                                "currentDate"
                            )
                        } " + it.String("timeRange")
                    )

                })
                put("res_id", it.String("resourceId"))
                put("id", it.String("id"))
            }
        })
        println(States.videos)
    }?.onFailure {
        it.printStackTrace()
    }
}

suspend fun updateLessonList() {
    runCatching {
        println("updateLessonList")
        val body = client.get("https://ilearntec.jlu.edu.cn/studycenter/platform/classroom/myClassroom") {
            header("Cookie", States.cookie)
            parameter("termYear", States.currentTerm.substringAfter('-').substringBefore('-'))
            parameter("term", States.currentTerm.substringAfterLast('-'))
        }.body<JsonObject>()
        println(body)
        body
    }?.onFailure {
        it.printStackTrace()
    }?.getOrNull()?.runCatching {
        Object("data").Array("dataList")
    }?.onSuccess {
        States.lessons.clear()
        States.lessons.addAll(it.map {
            it as JsonObject
        })
    }
}

@Composable
@Suppress("FunctionName")
fun MainPage() {
    MaterialTheme {
        val mainScope = rememberCoroutineScope()
        Column {
            TopBar(cookieString = States.cookie,
                setCookieString = { States.cookie = it.trim() },
                filter1Name = "按课程筛选",
                filter1Content = States.lessons,
                setFilter1 = {
                    States.lessonNow = ((it.String("courseName") + it.String("teacherName")))
                    println(it)
                    States.queryType = 0
                    States.queryVideos = listOf(
                        "termId" to States.currentTerm,
                        "submitStatus" to "0",
                        "teachClassId" to it.String("classroomId"),
                    )
                    mainScope.launch { updateVideoList() }
                },
                current1 = States.lessonNow,
                filter2Name = "按学期筛选",
                filter2Content = States.terms,
                setFilter2 = {
                    States.termNow = (it.String("year") + it.String("num"))
                    States.lessonNow = "---"
                    States.currentTerm = it.String("id")
                    States.queryVideos = listOf("termYear" to it.String("year"), "term" to it.String("num"))
                    mainScope.launch { updateLessonList() }
                    println(it)
                },
                current2 = States.termNow,
                fnIcon = when (States.pageState) {
                    PageState.INDEX -> Icons.Filled.Settings
                    PageState.SETTINGS -> Icons.Filled.ArrowBack
                },
                fnDescription = when (States.pageState) {
                    PageState.INDEX -> "Settings"
                    PageState.SETTINGS -> "Back"
                },
                onFnClick = {
                    println(States.pageState)
                    when (States.pageState) {
                        PageState.INDEX -> States.pageState = PageState.SETTINGS
                        PageState.SETTINGS -> States.pageState = PageState.INDEX
                    }
                },
                syncState = States.syncState,
                onSync = {
                    if (States.syncState != SyncState.SYNCING) {
                        syncCourses(mainScope)
                    }
                })
            when (States.pageState) {
                PageState.INDEX -> {
                    LazyColumn {
                        items(States.videos.size, { States.videos[it].hashCode() }) {
                            val id = States.videos[it].String("res_id")
                            Box(Modifier.fillMaxWidth().height(50.dp).background(Color.Transparent).clickable {}) {
                                Row(
                                    Modifier.fillMaxSize()/*.background(Color.Cyan)*/,
                                    verticalAlignment = Alignment.CenterVertically,
                                    horizontalArrangement = Arrangement.SpaceBetween
                                ) {

                                    //info
//                                    Text(
//                                        "${States.videos[it].String("courseName")}${States.videos[it].String("roomName")}${
//                                            States.videos[it].String(
//                                                "currentWeek"
//                                            )
//                                        }${States.videos[it].String("currentDay")}${States.videos[it].String("scheduleTimeStart")}",
//                                        modifier = Modifier.weight(1f, false)
//                                    )
                                    Text(
                                        States.videos[it].String("info"), modifier = Modifier.weight(1f, false)
                                    )
                                    //button:
                                    Row() {
                                        //play
                                        val modifier = androidx.compose.ui.Modifier.size(40.dp)
                                        val modifier1 = androidx.compose.ui.Modifier.padding(5.dp)
                                        IconButton({}, modifier) {
                                            Icon(painterResource("play_arrow.svg"), "download", modifier1)
                                        }
                                        //download use when to switch
                                        val downloaded = DownloadState.NOT_DOWNLOADED
                                        when (downloaded) {
                                            DownloadState.NOT_DOWNLOADED -> IconButton({}, modifier) {
                                                Icon(painterResource("download.svg"), "download", modifier1)
                                            }

                                            DownloadState.HALF_DOWNLOADED -> IconButton({}, modifier) {
                                                Icon(
                                                    painterResource("half_downloaded.svg"), "download", modifier1
                                                )
                                            }

                                            DownloadState.DOWNLOADED -> IconButton({}, modifier) {
                                                Icon(painterResource("done_outline.svg"), "download", modifier1)
                                            }
                                        }
                                        //open folder
                                        IconButton({}, modifier) {
                                            Icon(painterResource("folder_open.svg"), "folder_open", modifier1)
                                        }
                                    }
                                }
                                Column(Modifier.fillMaxSize(), verticalArrangement = Arrangement.SpaceBetween) {
                                    LinearProgressIndicator(
                                        0.4f, androidx.compose.ui.Modifier.height(5.dp).fillMaxWidth()
                                    )
                                    LinearProgressIndicator(
                                        0.6f, androidx.compose.ui.Modifier.height(5.dp).fillMaxWidth()
                                    )
                                }
                            }
                            Spacer(androidx.compose.ui.Modifier.height(4.dp))
                            Spacer(
                                androidx.compose.ui.Modifier.height(2.dp).fillMaxWidth().background(Color.Gray)
                            )
                            Spacer(androidx.compose.ui.Modifier.height(4.dp))
                        }
                    }
                }

                PageState.SETTINGS -> {
                    SettingPage()
                }
            }

        }
    }
}

private fun syncCourses(mainScope: CoroutineScope) {
    States.syncState = SyncState.SYNCING
    mainScope.launch(Dispatchers.Default) {
        runCatching {
            val body = client.post(QUERY_ALL_TERM) {
                header("Cookie", States.cookie)
            }.body<JsonObject>()
            require(body.String("status") == "1") {
                "failed to parse term list."
            }
            body
        }.onFailure {
            it.printStackTrace()
        }.getOrNull()?.runCatching {
            States.terms.clear()
            States.terms.addAll(Object("data").ObjectArray("dataList"))

            if (States.currentTerm.isEmpty()) {
                Object("data").Array("dataList").first().let {
                    println(it)
                    States.currentTerm = it.String("id")
                    States.termNow = it.String("year") + it.String("name")
                }
            }
            updateLessonList()
            if (States.queryVideos == null) {
                Object("data").Array("dataList").first().let {
                    println(it)
                    States.queryVideos = listOf("termYear" to it.String("year"), "term" to it.String("num"))
                    States.lessonNow = "---"
                }
                updateVideoList()
            }
        }?.onFailure {
            it.printStackTrace()
        }
        States.syncState = SyncState.SYNCED
    }
}
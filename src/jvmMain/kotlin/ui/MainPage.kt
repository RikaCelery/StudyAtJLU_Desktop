package ui

import PageState
import QUERY_ALL_TERM
import QUERY_LIVE_AND_RECORD
import SettingPage
import States
import SyncState
import TopBar
import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
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
import client
import downloadVideo
import io.ktor.client.call.*
import io.ktor.client.request.*
import kotlinx.coroutines.*
import kotlinx.serialization.json.JsonObject
import kotlinx.serialization.json.buildJsonObject
import kotlinx.serialization.json.put
import logOut
import utils.*
import java.awt.Desktop

suspend fun updateVideoList() {
    States.syncState = SyncState.SYNCING
    require(States.queryVideos != null)
    logOut(States.queryVideos)
    States.videos.clear()
    States.queryVideos?.runCatching {
        val body = client.get(QUERY_LIVE_AND_RECORD) {
            parameter("roomType", 0)
            parameter("identity", 2)
            forEach { parameter(it.first, it.second) }

            header("Cookie", States.cookie)
        }.body<JsonObject>()
//        logOut(body)
        body.Object("data").Array("dataList").filter { it.StringOrNull("isOpen") != null }
    }?.onFailure {
        if (it is CancellationException) {

        } else {
            States.syncState = SyncState.FAILED(it)
        }
        logOut(it.stackTraceToString())
    }?.getOrNull()?.runCatching {
        States.videos.clear()
        States.videos.addAll(map {
            buildJsonObject {
                put("lessonName", it.String("courseName"))
                put("date", it.String("scheduleTimeStart").substringBefore(' '))
                put("timeRange", it.String("timeRange").replace(':', '点'))
                put("info", buildString {
                    append(it.String("courseName"))
                    append(":")
                    append(it.String("teacherName"))
                    append("\n  ")
                    append(
                        "${it.String("currentWeek")}周 " + "星期${it.String("currentDay")}" + "(${it.StringOrNull("roomName")}) " + "${
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
        logOut(States.videos.toList())
    }?.onFailure {
        if (it is CancellationException) {

        } else {
            States.syncState = SyncState.FAILED(it)
        }
        logOut(it.stackTraceToString())
    }?.onSuccess {
        States.syncState = SyncState.SYNCED
    }

}
//贝塞尔曲线
fun calculateY(t: Float): Float {
    val tSquared = t * t
    val oneMinusT = 1 - t
    val oneMinusTSquared = oneMinusT * oneMinusT

    val y = oneMinusTSquared * 0.0 + 2 * oneMinusT * t * 0.7 + tSquared * 0.7

    return y.toFloat()
}
suspend fun updateTermVideoList(termId: String) {
    States.syncState = SyncState.SYNCING
    require(States.queryVideos != null)
    logOut(States.queryVideos)
    States.videos.clear()
    States.queryVideos?.runCatching {
        updateLessonList()
        States.syncState = SyncState.SYNCING
        States.lessons.map {
            client.get(QUERY_LIVE_AND_RECORD) {
                parameter("roomType", 0)
                parameter("identity", 2)
                parameter("submitStatus", 0)
                parameter("termId", termId)
                parameter("teachClassId", it.String("classroomId"))
                header("Cookie", States.cookie)
            }.body<JsonObject>().Object("data").ObjectArray("dataList").filter { it.StringOrNull("isOpen") != null }
        }.flatten()
    }?.onFailure {
        if (it is CancellationException) {

        } else {
            States.syncState = SyncState.FAILED(it)
        }
        logOut(it.stackTraceToString())
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
                        "${it.String("currentWeek")}周 " + "星期${it.String("currentDay")}" + "(${it.StringOrNull("roomName")}) " + "${
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
        logOut(States.videos.toList())
    }?.onFailure {
        if (it is CancellationException) {

        } else {
            States.syncState = SyncState.FAILED(it)
        }
        logOut(it.stackTraceToString())
    }?.onSuccess {
        States.syncState = SyncState.SYNCED
    }

}

suspend fun updateLessonList() {
    States.syncState = SyncState.SYNCING
    runCatching {
        logOut("updateLessonList")
        val body = client.get("https://ilearntec.jlu.edu.cn/studycenter/platform/classroom/myClassroom") {
            header("Cookie", States.cookie)
            parameter("termYear", States.currentTerm.substringAfter('-').substringBefore('-'))
            parameter("term", States.currentTerm.substringAfterLast('-'))
        }.body<JsonObject>()
        logOut(body)
        body
    }.onFailure {
        if (it is CancellationException) {

        } else {
            States.syncState = SyncState.FAILED(it)
        }
        logOut(it.stackTraceToString())
    }.getOrNull()?.runCatching {
        Object("data").Array("dataList")
    }?.onSuccess {
        States.lessons.clear()
        States.lessons.addAll(it.map {
            it as JsonObject
        })
    }?.onFailure {
        if (it is CancellationException) {

        } else {
            States.syncState = SyncState.FAILED(it)
        }
        logOut(it.stackTraceToString())
    }?.onSuccess {
        States.syncState = SyncState.SYNCED
    }
}

@OptIn(ExperimentalFoundationApi::class, ExperimentalMaterialApi::class)
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
                    States.lessonNow = it.String("courseName")/* + it.String("teacherName")*/
                    logOut(it)
                    States.queryType = 0
                    States.queryVideos = listOf(
                        "termId" to States.currentTerm,
                        "submitStatus" to "0",
                        "teachClassId" to it.String("classroomId"),
                    )
                    States.currentJob?.cancel()
                    States.currentJob = mainScope.launch { updateVideoList() }
                },
                current1 = States.lessonNow,
                filter2Name = "按学期筛选",
                filter2Content = States.terms,
                setFilter2 = {
                    States.termNow = (it.String("year") + it.String("name"))
                    States.lessonNow = "---"
                    States.currentTerm = it.String("id")
                    States.queryVideos = listOf("termYear" to it.String("year"), "term" to it.String("num"))
                    States.currentJob?.cancel()
                    States.currentJob = mainScope.launch {
                        updateLessonList()
                        if (States.currentTerm == States.terms.first().String("id")) updateVideoList()
                        else updateTermVideoList(States.currentTerm)
                    }
                    logOut(it)
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
                    logOut(States.pageState)
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
                        items(States.videos, { it.hashCode() }) { lessonInfo ->
                            Box(Modifier.animateItemPlacement()) {
                                val id = lessonInfo.String("res_id")
                                Spacer(Modifier.height(4.dp))
                                Box(Modifier.fillMaxWidth().height(80.dp).background(Color.Transparent).clickable {}) {
                                    Row(
                                        Modifier.fillMaxSize()/*.background(Color.Cyan)*/,
                                        verticalAlignment = Alignment.CenterVertically,
                                        horizontalArrangement = Arrangement.SpaceBetween
                                    ) {
                                        Text(
                                            lessonInfo.String("info"), modifier = Modifier.weight(1f, false)
                                        )
                                        //button:
                                        Row() {
                                            //play
                                            val modifier = Modifier.size(40.dp)
                                            val modifier1 = Modifier.padding(5.dp)

                                            val lessonName =
                                                lessonInfo.String("lessonName").replace(':', '_').replace("*", "")

                                            val date =
                                                lessonInfo.String("date")
                                                    .replace(':', '_')

                                            val folder =
                                                States.downloadFolder.resolve(lessonName).resolve(date).canonicalFile
                                            val teacherFile = folder.resolve(
                                                "${
                                                    date.plus(' ').plus(lessonInfo.String("timeRange"))
                                                } $date 教师机位.mp4"
                                            )
                                            val pcFile = folder.resolve(
                                                "${
                                                    date.plus(' ').plus(lessonInfo.String("timeRange"))
                                                } HDMI.mp4"
                                            )
//
                                            IconButton({
                                                val file = folder.resolve("index.html")
                                                println(file)
                                                if (file.exists().not()) {
                                                    runBlocking() {
                                                        downloadVideo(folder, teacherFile, pcFile, id, 2)
                                                    }
                                                }
                                                try {
                                                    Desktop.getDesktop().browse(file.toURI())
                                                } catch (e: Exception) {
                                                    e.printStackTrace()
//                                                    AlertDialog({}, {}, text = { Text("无法打开页面:${e.localizedMessage}") })
                                                }
                                            }, modifier) {
                                                Icon(painterResource("play_arrow.svg"), "download", modifier1)
                                            }
                                            //download use when to switch
                                            if (teacherFile.exists()) IconButton({}, modifier) {
                                                Icon(painterResource("done_outline.svg"), "download", modifier1)
                                            } else if (States.tasks[id + "_1"]?.isActive == true) {
                                                IconButton({
                                                    States.tasks[id + "_1"]?.cancel()
                                                }, modifier) {
                                                    Icon(
                                                        painterResource("half_downloaded.svg"), "download", modifier1
                                                    )
                                                }
                                            } else IconButton({
                                                mainScope.launch {
                                                    downloadVideo(folder, teacherFile, pcFile, id)
                                                }
                                            }, modifier) {
                                                Icon(painterResource("download.svg"), "download", modifier1)
                                            }


                                            if (pcFile.exists()) IconButton({}, modifier) {
                                                Icon(painterResource("done_outline.svg"), "download", modifier1)
                                            } else if (States.tasks[id + "_2"]?.isActive == true) {
                                                IconButton({
                                                    States.tasks[id + "_2"]?.cancel()
                                                }, modifier) {
                                                    Icon(
                                                        painterResource("half_downloaded.svg"), "download", modifier1
                                                    )
                                                }
                                            } else IconButton({
                                                mainScope.launch {
                                                    downloadVideo(folder, teacherFile, pcFile, id, 1)
                                                }
                                            }, modifier) {
                                                Icon(painterResource("download.svg"), "download", modifier1)
                                            }

                                            //open folder
                                            IconButton({
                                                val file = folder
                                                println(file)
                                                try {
                                                    Desktop.getDesktop().open(file)
                                                } catch (e: Exception) {
                                                    e.printStackTrace()
//                                                    AlertDialog({}, {}, text = { Text("无法打开文件夹:${e.localizedMessage}") })
                                                }
                                            }, modifier) {
                                                Icon(painterResource("folder_open.svg"), "folder_open", modifier1)
                                            }
                                        }
                                    }
                                    Column(Modifier.fillMaxSize(), verticalArrangement = Arrangement.SpaceBetween) {
                                        Row(Modifier.fillMaxWidth(), verticalAlignment = Alignment.Top) {
                                            States.progress[id + "_1"]?.let {

                                                LinearProgressIndicator(
                                                    it,
                                                    Modifier.height(8.dp).weight(1f),
                                                    color = Color.hsv(calculateY(it) * 120, 1f, 1f)
                                                )
                                            } ?: Spacer(Modifier.height(8.dp).weight(1f))
                                            Text(States.progressInfo[id + "_1"] ?: "")
                                        }
                                        Row(Modifier.fillMaxWidth(), verticalAlignment = Alignment.Bottom) {
                                            States.progress[id + "_2"]?.let {
                                                LinearProgressIndicator(
                                                    it,
                                                    Modifier.height(8.dp).weight(1f),
                                                    color = Color.hsv(calculateY(it) * 120, 1f, 1f)
                                                )
                                            } ?: Spacer(Modifier.height(8.dp).weight(1f))
                                            Text(States.progressInfo[id + "_2"] ?: "")
                                        }
                                    }
                                }
                                Spacer(Modifier.height(4.dp))
                                Spacer(
                                    Modifier.height(2.dp).fillMaxWidth().background(Color.Gray)
                                )
                            }
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
    States.currentJob?.cancel()
    States.currentJob = mainScope.launch(Dispatchers.Default) {
        runCatching {
            val body = client.post(QUERY_ALL_TERM) {
                header("Cookie", States.cookie)
            }.body<JsonObject>()
            require(body.String("status") == "1") {
                "failed to parse term list."
            }
            body
        }.onFailure {
            if (it is CancellationException) {

            } else {
                States.syncState = SyncState.FAILED(it)
            }
            logOut(it.stackTraceToString())
        }.getOrNull()?.runCatching {
            States.terms.clear()
            States.terms.addAll(Object("data").ObjectArray("dataList"))

            if (States.currentTerm.isEmpty()) {
                Object("data").Array("dataList").first().let {
                    logOut(it)
                    States.currentTerm = it.String("id")
                    States.termNow = it.String("year") + it.String("name")
                }
            }
            updateLessonList()

            if (States.queryVideos == null) {
                Object("data").Array("dataList").first().let {
                    logOut(it)
                    States.queryVideos = listOf("termYear" to it.String("year"), "term" to it.String("num"))
                    States.lessonNow = "---"
                }
                updateVideoList()
            }
        }?.onFailure {
            if (it is CancellationException) {

            } else {
                States.syncState = SyncState.FAILED(it)
            }
            logOut(it.stackTraceToString())
        }?.onSuccess {
            States.syncState = SyncState.SYNCED
        }
    }
}
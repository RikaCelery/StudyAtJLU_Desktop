import androidx.compose.animation.core.Animatable
import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.tween
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.Settings
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.rotate
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Window
import androidx.compose.ui.window.application
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.runBlocking
import kotlinx.coroutines.withContext

@Composable
@Suppress("FunctionName")
fun App() {
    MaterialTheme {
        Column {
            TopBar(cookieString = States.cookie,
                setCookieString = { States.cookie = it },
                filter1Name = "按课程筛选",
                filter1Content = States.lessons,
                setFilter1 = { States.lessonFilter = (it);println(it) },
                current1 = States.lessonFilter,
                filter2Name = "按学期筛选",
                filter2Content = States.terms,
                setFilter2 = { States.termFilter = (it);println(it) },
                current2 = States.lessonFilter,
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
                syncState = SyncState.OUT_DATE,
                onSync = {
                    SyncState.FAILED()
                }
            )
            when (States.pageState) {
                PageState.INDEX -> {
                    LazyColumn() {
                        for (i in 0..300)
                            item {
                                Box(Modifier.fillMaxWidth().height(50.dp).background(Color.Transparent).clickable {}) {
                                    Row(
                                        Modifier.fillMaxSize().background(Color.Cyan),
                                        verticalAlignment = Alignment.CenterVertically,
                                        horizontalArrangement = Arrangement.SpaceBetween
                                    ) {
                                        //info
                                        Text("$i", modifier = Modifier.weight(1f, false))
                                        //button:
                                        Row() {
                                            //play
                                            val modifier = Modifier.size(40.dp)
                                            val modifier1 = Modifier.padding(5.dp)
                                            IconButton({}, modifier) {
                                                Icon(painterResource("play_arrow.svg"), "download", modifier1)
                                            }
                                            //download use when to switch
                                            val downloaded = DownloadState.DOWNLOADED
                                            when (downloaded) {
                                                DownloadState.NOT_DOWNLOADED -> IconButton({}, modifier) {
                                                    Icon(painterResource("download.svg"), "download", modifier1)
                                                }

                                                DownloadState.HALF_DOWNLOADED -> IconButton({}, modifier) {
                                                    Icon(painterResource("half_downloaded.svg"), "download", modifier1)
                                                }

                                                DownloadState.DOWNLOADED -> IconButton({}, modifier) {
                                                    Icon(painterResource("half_downloaded.svg"), "download", modifier1)
                                                }
                                            }



                                            IconButton({}, modifier) {
                                                Icon(painterResource("done_outline.svg"), "download", modifier1)
                                            }
                                            //open folder
                                            IconButton({}, modifier) {
                                                Icon(painterResource("folder_open.svg"), "folder_open", modifier1)
                                            }
                                        }
                                    }
                                    Column(Modifier.fillMaxSize(), verticalArrangement = Arrangement.SpaceBetween) {
                                        LinearProgressIndicator(0.4f, Modifier.height(5.dp).fillMaxWidth())
                                        LinearProgressIndicator(0.6f, Modifier.height(5.dp).fillMaxWidth())
                                    }
                                }
                                Spacer(Modifier.height(4.dp))
                                Spacer(Modifier.height(2.dp).fillMaxWidth().background(Color.Gray))
                                Spacer(Modifier.height(4.dp))
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

@Composable
@Suppress("FunctionName")
fun ColumnScope.SettingPage() {

}

@Composable
@Suppress("FunctionName")
fun TopBar(
    cookieString: String,
    setCookieString: (String) -> Unit,
    filter1Name: String,
    filter1Content: List<String>,
    setFilter1: (String) -> Unit,
    current1: String? = null,
    filter2Name: String,
    filter2Content: List<String>,
    setFilter2: (String) -> Unit,
    current2: String? = null,
    syncState: SyncState,
    onSync: () -> Unit,
    fnIcon: ImageVector,
    fnDescription: String,
    onFnClick: () -> Unit,
) {
    Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
        TextField(
            cookieString, setCookieString, Modifier.weight(1f, true).height(50.dp),
        )
        Spacer(Modifier.width(2.dp))
        IconButton(onClick = onSync) {
            val rotationState = remember { Animatable(0f) }
            LaunchedEffect(Unit) {

                rotationState.animateTo(
                    targetValue = rotationState.value - 360f,
                    animationSpec = infiniteRepeatable(
                        animation = tween(durationMillis = 1000, delayMillis = 500),
                        repeatMode = RepeatMode.Restart
                    )
                )


            }

            when (syncState) {
                is SyncState.FAILED -> Icon(painterResource("sync_problem.svg"), "refresh list")
                SyncState.OUT_DATE -> Icon(painterResource("sync.svg"), "refresh list")
                SyncState.SYNCED -> Icon(painterResource("done_outline.svg"), "refresh list")
                SyncState.SYNCING -> {
                    Icon(painterResource("sync.svg"), "refresh list", Modifier.rotate(rotationState.value))
                }
            }
        }
        Spacer(Modifier.width(2.dp))
        CDropDownMenu(filter1Name, current1, filter1Content, setFilter1, Modifier)
        Spacer(Modifier.width(2.dp))
        CDropDownMenu(filter2Name, current2, filter2Content, setFilter2, Modifier)
        Spacer(Modifier.width(2.dp))
        OutlinedButton(
            onFnClick,
            Modifier.size(50.dp),
            shape = CircleShape,
            contentPadding = PaddingValues(0.dp)
        ) {
            Icon(fnIcon, fnDescription)
        }

    }
}

@Composable
fun CDropDownMenu(
    filter1Name: String,
    current1: String?,
    filter1Content: List<String>,
    setFilter1: (String) -> Unit,
    modifier: Modifier = Modifier,
) {
    var display by remember { mutableStateOf(false) }
    Box(modifier.height(50.dp).width(120.dp), Alignment.Center) {
        OutlinedButton(
            { display = !display },
            Modifier.fillMaxSize(),
            contentPadding = PaddingValues(horizontal = 10.dp)
        ) {
            Column(verticalArrangement = Arrangement.Center, horizontalAlignment = Alignment.CenterHorizontally) {
                Text(
                    filter1Name,
                    softWrap = false,
                    overflow = TextOverflow.Ellipsis,
                    color = Color.Black,
                    fontWeight = FontWeight.Bold
                )
                if (current1 != null)
                    Text(
                        current1,
                        softWrap = false,
                        overflow = TextOverflow.Ellipsis,
                        color = Color.Gray,
                        fontSize = 12.sp,
                        fontWeight = FontWeight.Thin
                    )
            }
        }
        DropdownMenu(expanded = display, onDismissRequest = { display = false }) {
            for (item in filter1Content) DropdownMenuItem(onClick = { display = false;setFilter1(item) }) {
                Text(text = item, softWrap = true, overflow = TextOverflow.Clip)
            }
        }
    }
}

fun main() = application {
    Window(onCloseRequest = ::exitApplication) {
        App()
    }
}

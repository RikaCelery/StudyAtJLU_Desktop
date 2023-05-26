import io.ktor.client.call.*
import io.ktor.client.request.*
import io.ktor.client.statement.*
import io.ktor.http.*
import io.ktor.utils.io.streams.*
import kotlinx.coroutines.*
import kotlinx.coroutines.CancellationException
import kotlinx.serialization.json.JsonObject
import utils.Array
import utils.Object
import utils.String
import utils.StringOrNull
import java.io.File
import java.io.FileOutputStream
import kotlin.io.path.Path
import kotlin.io.path.pathString


suspend fun download(baseFolder:File,query: Pair<String,String>,cookie:String) {
    val body = client.get(QUERY_LIVE_AND_RECORD) {
        parameter("roomType", 0)
        parameter("identity", 2)
        parameter(query.first, query.second)
        header("Cookie", cookie)
    }.body<JsonObject>()
    val all = body.runCatching {
        Object("data").Array("dataList").mapNotNull {
            if (it.StringOrNull("resourceId") != null) Triple(
                it.String("resourceId"), it.String("scheduleTimeStart"), it.String("scheduleTimeEnd")
            )
            else null
        }
    }.onFailure {
        println(body)
    }.getOrThrow()
    for (res in all) {
        val info = runCatching {
            client.get(QUERY_VIDEO_INFO)
                .body<JsonObject>().Object("data")
        }.onFailure {
            println(
                client.get(QUERY_VIDEO_INFO) {
                    parameter("resourceId", res.first)
                }
                    .body<String>()
            )
        }.getOrNull()
        if (info == null) {
            println("failed: $res")
            continue
        }
        val folderName = info.String("resourceName").replace(':', '_').replace('*', '_')
        // 忽略以下课程
        if (folderName.contains("习近平新时代中国特色社会主义思想概论")) continue
        if (folderName.contains("IA32")) continue
        if (folderName.contains("直播课")) continue
        if (folderName.contains("微积分")) continue
        if (folderName.contains("Java")) continue

        val subFolderName = res.second.substringBefore(' ').replace(':', '_').replace('*', '_')
        //同时下载电脑画面和教师画面，但是下载其实是跑满的所以没必要这么干
//        supervisorScope {
        val htmlFile = baseFolder.resolve(folderName).resolve(subFolderName).apply { if (exists().not()) mkdirs() }
            .resolve("index.html")
        var hdmiFilePath = ""
        var teacherFilePath = ""
        for (videoInfo in info.Array("videoList")) {
            val fileName =
                res.second.substringAfter(' ').replace(':', '_').plus(' ').plus(videoInfo.String("videoName"))
            if (fileName.contains("教师")) teacherFilePath = "$fileName.mp4"
            if (fileName.contains("HDMI")) hdmiFilePath = "$fileName.mp4"
            // 只有算法课下载教师画面
            if (fileName.contains("教师") && !folderName.contains("算法")) continue
            val url = Url(videoInfo.String("videoPath"))
            println("prepare to download $folderName/$subFolderName/$fileName")

            val tmpFile = baseFolder.resolve(folderName).resolve(subFolderName).resolve("$fileName.tmp")
            val finalFile = baseFolder.resolve(folderName).resolve(subFolderName).resolve("$fileName.mp4")
            if (finalFile.exists()) continue
            if (tmpFile.exists()) tmpFile.delete()
            withContext(Dispatchers.IO) {
                tmpFile.outputStream().use {
                    client.prepareGet(url) {}.execute { httpResponse ->
                        try {
                            receiveStream(httpResponse, folderName, subFolderName, fileName, it)
                        } catch (e: CancellationException) {
                            tmpFile.delete()
                        }
                    }
                }
                tmpFile.renameTo(finalFile)
            }
        }
        //播放器
        htmlFile.writeText(
            """<head><title></title><meta charset="UTF-8" /></head><body><div id="tooltip">00:00 / 00:00</div><div class="container" id="videos"><div class="hdmi"><video src="./{HDMI}" id="hdmi" ontimeupdate="updateProgressBar()"></video><input type="range" id="progressBar1" min="0" max="100" step="0.1" value="0" oninput="seekTo(this.value)"></div><div class="teacher"><video src="./{TEACHER}" muted id="teacher"></video></div></div></body><script>var tooltip = document.getElementById("tooltip");document.addEventListener("keydown", function (event) {if (event.code === "Space") {togglePlay();event.preventDefault();};});var video = document.getElementById("hdmi");var video2 = document.getElementById("teacher");var progressBar = document.getElementById("progressBar1");function updateTooltip() {var currentTime = formatTime(video.currentTime);var duration = formatTime(video.duration);tooltip.textContent = currentTime + " / " + duration;};setTimeout(updateTooltip, 2000);video.addEventListener("timeupdate", updateTooltip);hideTooltip = undefined;document.addEventListener("mousemove", function (event) {clearTimeout(hideTooltip);tooltip.style.opacity = 1;tooltip.style.top = 10+event.clientY + window.pageYOffset + "px";tooltip.style.left = event.clientX + window.pageXOffset + "px";hideTooltip = setTimeout(() => tooltip.style.opacity = 0, 700);});document.addEventListener("mouseenter", function () {tooltip.style.opacity = 1;});document.addEventListener("mouseleave", function () {tooltip.style.opacity = 0;});function formatTime(time) {var minutes = Math.floor(time / 60);var seconds = Math.floor(time % 60);return (minutes < 10 ? "0" : "") + minutes + ":" + (seconds < 10 ? "0" : "") + seconds;};function updateProgressBar() {var currentTime = (video.currentTime / video.duration) * 100;progressBar.value = currentTime;};function seekTo(value) {var timeToSeek = (value / 100) * video.duration;video.currentTime = timeToSeek;video2.currentTime = timeToSeek;};function togglePlay() {var video1 = document.getElementById("hdmi");if (video1.paused) {video1.play();} else {video1.pause();};var video2 = document.getElementById("teacher");if (video2.paused) {video2.play();} else {video2.pause();};};</script><style>* {padding: 0;margin: 0;box-sizing: border-box;}body {background-color: black;}::-webkit-scrollbar {display: none;}.container {width: 100vw;height: 100vh;display: flex;flex-wrap: wrap;}#tooltip {position: absolute;padding: 5px;background-color: rgba(0, 0, 0, 0.383);color: white;font-size: 12px;opacity: 0;pointer-events: none;border-radius: 20px;transition: opacity ease 0.3s;}video,input {width: 100%;/* height: 100vh; */flex-grow: 1;}</style>""".replace(
                "{HDMI}", hdmiFilePath
            ).replace("{TEACHER}", teacherFilePath)
        )
//        }
    }
    client.close()
}



private suspend fun receiveStream(
    httpResponse: HttpResponse,
    folderName: String,
    subFolderName: String,
    tmpFileName: String,
    fs: FileOutputStream,
) {
    val channel = httpResponse.bodyAsChannel()
    println("downloading ${Path(folderName,subFolderName,tmpFileName).normalize().pathString}")
    val contentLength = httpResponse.contentLength()?.toString() ?: "unknown"
    val start = System.currentTimeMillis()
    withContext(Dispatchers.IO) {
        while (!channel.isClosedForRead) {
            val packet = channel.readRemaining(DOWNLOAD_BUFFER_SIZE)
            print(buildString {
                append("write: ")
                if (contentLength.all(Char::isDigit)) {
                    append("%.2f%%".format(channel.totalBytesRead.times(100.0).div(contentLength.toLong())))
                } else {
                    append("Nan%")
                }
                append(' ')
                append(channel.totalBytesRead)
                append('/')
                append(contentLength)
                append(' ')
                val speed = channel.totalBytesRead.times(1f).div(System.currentTimeMillis() - start).times(1000)
                append("%.2fMB/s".format(speed.div(UNIT_MB)))
                append('\r')
            })
            fs.writePacket(packet)
        }
    }
    println()
    println("downloaded ${Path(folderName,subFolderName,tmpFileName).normalize().pathString}")
}
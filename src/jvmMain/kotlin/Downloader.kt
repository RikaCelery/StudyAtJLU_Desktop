
import io.ktor.client.call.*
import io.ktor.client.request.*
import io.ktor.client.statement.*
import io.ktor.http.*
import io.ktor.utils.io.streams.*
import kotlinx.coroutines.*
import kotlinx.serialization.json.JsonObject
import utils.Array
import utils.Object
import utils.String
import java.io.File
import java.io.FileOutputStream
import kotlin.io.path.pathString

suspend fun downloadVideo(folder:File, teacherFile: File, pcFile: File, resourceId: String, type: Int = 0) {

    val info = runCatching {
        client.get(QUERY_VIDEO_INFO) {
            parameter("resourceId", resourceId)
        }.body<JsonObject>().Object("data")
    }.onFailure {}.getOrNull()
    if (info == null) {
        logOut("failed: $resourceId")
        return
    }

    println(info)

    val htmlFile =
        folder.apply { if (exists().not()) mkdirs() }.resolve("index.html")

    val videoInfos = info.Array("videoList")

    val teacherVideoInfo = videoInfos.single { it.String("videoName").contains("教师") }
    val pcVideoInfo = videoInfos.single { it.String("videoName").contains("HDMI") }

    val teacherUrl = Url(teacherVideoInfo.String("videoPath"))
    val pcUrl = Url(pcVideoInfo.String("videoPath"))

    supervisorScope {
        if (type == 0) {
            if (States.tasks.get(resourceId + "_1")?.isActive == true)
                return@supervisorScope
            States.tasks[resourceId + "_1"] = launch {
                runCatching {
                    downloadToFile(teacherFile, teacherUrl) { current, total, totalTime, time ->
                        if (total != -1L) {
                            val progress = current.times(1f).div(total)
                            States.progress[resourceId + "_1"] = progress

                            val speed = current.times(1f).div(totalTime).times(1000)

                            States.progressInfo[resourceId + "_1"] =
                                "%.2f%% %.2fMB/s".format(progress * 100, speed.div(UNIT_MB))
                        }
                    }
                }.onSuccess {
                    States.progress.remove(resourceId + "_1")
                    States.progressInfo.remove(resourceId + "_1")
                }.onFailure {
                    if (it is CancellationException) {
                        States.progressInfo.put(resourceId + "_1", "Cancelled")

                    } else {
                        States.progressInfo.put(resourceId + "_1", "Failed")
                        it.printStackTrace()
                    }
                }
            }
        } else if (type==1) {
            if (States.tasks.get(resourceId + "_2")?.isActive == true)
                return@supervisorScope
            States.tasks[resourceId + "_2"] = launch {
                runCatching {
                    downloadToFile(pcFile, pcUrl) { current, total, totalTime, time ->
                        if (total != -1L) {
                            val progress = current.times(1f).div(total)
                            States.progress[resourceId + "_2"] = progress
                            val speed = current.times(1f).div(totalTime).times(1000)
                            States.progressInfo[resourceId + "_2"] =
                                "%.2f%% %.2fMB/s".format(progress * 100, speed.div(UNIT_MB))
                        }
                    }
                }.onSuccess {
                    States.progress.remove(resourceId + "_2")
                    States.progressInfo.remove(resourceId + "_2")
                }.onFailure {
                    if (it is CancellationException) {
                        States.progressInfo.put(resourceId + "_2", "Cancelled")

                    } else {
                        States.progressInfo.put(resourceId + "_2", "Failed")
                        it.printStackTrace()
                    }
                }
            }
        }
    }
    //播放器
    htmlFile.writeText(
        """<head><title></title><meta charset="UTF-8"/></head><body><div id="tooltip">00:00 / 00:00</div><div class="container" id="videos"><div class="hdmi"><video src="{HDMI}" id="hdmi" ontimeupdate="updateProgressBar()"></video><input type="range" id="progressBar1" min="0" max="100" step="0.1" value="0" oninput="seekTo(this.value)"></div><div class="teacher"><video src="{TEACHER}" muted id="teacher"></video></div></div></body><script>var tooltip=document.getElementById("tooltip");document.addEventListener("keydown",function(event){if(event.code==="Space"){togglePlay();event.preventDefault();};});var video=document.getElementById("hdmi");var video2=document.getElementById("teacher");var progressBar=document.getElementById("progressBar1");progressBar.focus();if(video.duration.toString()=='NaN')video2.removeAttribute("muted");function updateTooltip(){var currentTime=formatTime(video.currentTime);var duration=formatTime(video.duration);tooltip.textContent=currentTime+"/"+duration;};setTimeout(updateTooltip,2000);video.addEventListener("timeupdate",updateTooltip);hideTooltip=undefined;document.addEventListener("mousemove",(event)=>{clearTimeout(hideTooltip);tooltip.style.opacity=1;tooltip.style.top=10+event.clientY+window.pageYOffset+"px";tooltip.style.left=event.clientX+window.pageXOffset+"px";hideTooltip=setTimeout(()=>tooltip.style.opacity=0,700);});document.addEventListener("mouseenter",function(){tooltip.style.opacity=1;});document.addEventListener("mouseleave",()=>{tooltip.style.opacity=0;});function formatTime(time){var minutes=Math.floor(time/60);var seconds=Math.floor(time%60);return(minutes<10?"0":"")+minutes+":"+(seconds<10?"0":"")+seconds;};function updateProgressBar(){var currentTime=(video.currentTime/video.duration)*100;progressBar.value=currentTime;};function seekTo(value){var timeToSeek=(value/100)*video.duration;video.currentTime=timeToSeek;video2.currentTime=timeToSeek;};function togglePlay(){var video1=document.getElementById("hdmi");if(video1.paused){video1.play();}else{video1.pause();};var video2=document.getElementById("teacher");if(video2.paused){video2.play();}else{video2.pause();};};progressBar.addEventListener("focusout",function(){progressBar.focus()})</script><style>*{padding:0;margin:0;box-sizing:border-box;}body{background-color:black;}::-webkit-scrollbar{display:none;}.container{width:100vw;height:100vh;display:flex;flex-wrap:wrap;}#tooltip{position:absolute;padding:5px;background-color:rgba(0,0,0,0.383);color:white;font-size:12px;opacity:0;pointer-events:none;border-radius:20px;transition:opacityease0.3s;}video,input{width:100%;flex-grow:1;}</style>"""
            .replace(
                "{HDMI}", pcFile.toRelativeString(folder)
            ).replace("{TEACHER}", teacherFile.toRelativeString(folder))
    )
}

suspend fun downloadToFile(
    finalFile: File,
    url: Url,
    onProgress: (current: Long, total: Long, totalTimeCost: Long, currentTimeCost: Long) -> Unit = { _, _, _, _ -> },

    ) {
    val tmpFile = finalFile.parentFile.resolve("${finalFile.name}.tmp")
    if (finalFile.exists()) return
    if (tmpFile.exists()) tmpFile.delete()
    withContext(Dispatchers.IO) {
        tmpFile.outputStream().use {
            client.prepareGet(url) {}.execute { httpResponse ->
                try {
                    it.receiveStream(httpResponse, finalFile, onProgress)
                } catch (e: CancellationException) {
                    tmpFile.delete()
                }
            }
        }
        tmpFile.renameTo(finalFile)
    }
}


private suspend fun FileOutputStream.receiveStream(
    httpResponse: HttpResponse,
    file: File,
    onProgress: (current: Long, total: Long, totalTimeCost: Long, currentTimeCost: Long) -> Unit = { _, _, _, _ -> },
) {
    val channel = httpResponse.bodyAsChannel()
    logOut("downloading ${(file.toPath()).normalize().pathString}")
    val contentLength = httpResponse.contentLength() ?: -1L
    val start = System.currentTimeMillis()
    withContext(Dispatchers.IO) {
        var last = start
        while (!channel.isClosedForRead) {
            last = System.currentTimeMillis()
            val packet = channel.readRemaining(DOWNLOAD_BUFFER_SIZE)
            writePacket(packet)
//            print(buildString {
//                append("write: ")
//                if (contentLength != -1L) {
//                    append("%.2f%%".format(channel.totalBytesRead.times(100.0).div(contentLength)))
//                } else {
//                    append("Nan%")
//                }
//                append(' ')
//                append(channel.totalBytesRead)
//                append('/')
//                append(contentLength)
//                append(' ')
//                val speed = channel.totalBytesRead.times(1f).div(System.currentTimeMillis() - start).times(1000)
//                append("%.2fMB/s".format(speed.div(UNIT_MB)))
//                append('\r')
//            })
            val now = System.currentTimeMillis()
            onProgress(channel.totalBytesRead, contentLength, now - start, now - last)
        }
    }
    logOut()
    logOut("downloaded ${file.toPath()}.normalize().pathString}")
}

enum class PageState {
    INDEX,
    SETTINGS
}

import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.material.MaterialTheme
import androidx.compose.material.Surface
import androidx.compose.ui.window.Window
import androidx.compose.ui.window.application
import io.ktor.client.*
import io.ktor.client.engine.okhttp.*
import io.ktor.client.plugins.*
import io.ktor.client.plugins.compression.*
import io.ktor.client.plugins.contentnegotiation.*
import io.ktor.serialization.kotlinx.json.*
import io.ktor.serialization.kotlinx.xml.*
import kotlinx.serialization.json.Json
import ui.*
import utils.OkHttpUtil
import java.io.File
import java.io.PrintStream

val client = HttpClient(OkHttp) {
//    install(Logging) {
//        logger = Logger.SIMPLE
//        level = LogLevel.INFO
//    }
    install(ContentNegotiation) {
        json(Json {
            ignoreUnknownKeys = true
            encodeDefaults = true
            prettyPrint = true
        })
        xml()
    }
    install(UserAgent) {
        agent = "Ktor client"
    }
    install(ContentEncoding) {
        deflate(1.0F)
        gzip(0.9F)
    }
    install(HttpRequestRetry) {
        retryOnServerErrors(maxRetries = 5)
        constantDelay(300, 2000)
    }
    install(HttpTimeout) {
        connectTimeoutMillis = 3_000
    }
    engine {
        config {
            followRedirects(true)
            sslSocketFactory(OkHttpUtil.ignoreInitedSslFactory, OkHttpUtil.IGNORE_SSL_TRUST_MANAGER_X509)
            hostnameVerifier(OkHttpUtil.ignoreSslHostnameVerifier)
        }
    }
}
val logFile = File("log.txt")


inline fun logOut(any: Any?) {
    val stackTrace = Thread.currentThread().stackTrace
    val line = stackTrace[2].lineNumber
    val message = buildString {
        append("[")
        append(stackTrace[1].methodName)
        append("](")
        append(line)
        append(")")
        append(any)
    }
    println(message)
    logFile.appendText(message + "\n")
}

@Synchronized
fun logOut() {
    println()
    logFile.appendText("\n")
}

fun main() {
    val outErr = File("err.txt").outputStream()
    val err = PrintStream(outErr)
    val outOut = File("out.txt").outputStream()
    val out = PrintStream(outOut)
    System.setErr(err)
    System.setErr(out)
    States.loadAll()
    try{
        app()
    }catch (e:Exception){
        e.printStackTrace()
        File("data.db").deleteOnExit()
        err.close()
        outErr.close()
        out.close()
        outOut.close()
        File("err.txt").renameTo(File("错误日志(StdErr).txt"))
        File("out.txt").renameTo(File("错误日志(StdOut).txt"))
    }
}
fun app(){
    application {
        Window(onCloseRequest = {
            States.saveAll()
            exitApplication()
        }, title = "Study at JLU") {
            val darkTheme = isSystemInDarkTheme()&&false
            val colors = if (darkTheme) DarkColorPalette else LightColorPalette

            MaterialTheme(
                colors = colors,
                typography = DefaultTypography,
                shapes = DefaultShapes
            ) {
                Surface(color = MaterialTheme.colors.background) {
                    MainPage()
                }
            }
        }
    }
}
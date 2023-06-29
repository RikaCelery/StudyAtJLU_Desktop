
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
import ui.MainPage
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

@Synchronized
fun logOut(any: Any?) {
    println(any)
    logFile.appendText(any.toString() + "\n")
}

@Synchronized
fun logOut() {
    println()
    logFile.appendText("\n")
}

fun main() {
    val err = PrintStream(File("err.txt").outputStream())
    val out = PrintStream(File("out.txt").outputStream())
    System.setErr(err)
    System.setErr(out)
    States.loadAll()
    application {
        Window(onCloseRequest = {
            States.saveAll()
            exitApplication()
        }) {
            MainPage()
        }
    }
}

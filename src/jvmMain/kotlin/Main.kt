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
import io.ktor.client.plugins.cookies.*
import io.ktor.client.plugins.logging.*
import io.ktor.client.request.*
import io.ktor.http.*
import io.ktor.http.content.*
import io.ktor.serialization.kotlinx.json.*
import io.ktor.util.*
import io.ktor.util.date.*
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.launch
import kotlinx.coroutines.sync.Mutex
import kotlinx.coroutines.sync.withLock
import kotlinx.serialization.decodeFromString
import kotlinx.serialization.encodeToString
import kotlinx.serialization.json.Json
import ui.*
import utils.DB
import utils.OkHttpUtil
import utils.fillDefaults
import utils.matches
import java.io.File
import java.io.PrintStream
import java.util.concurrent.atomic.AtomicLong
import kotlin.math.min

object CCookiesStorage : CookiesStorage {
    fun exportToJsonString(): String {
        return Json.Default.encodeToString(
            container
        )
    }

    fun loadFromJsonString(jsonString: String) {
        try {
            container.addAll(Json.Default.decodeFromString(jsonString))
        } catch (e: Exception) {
            e.printStackTrace()
        }
    }

    private val container: MutableList<Cookie> = mutableListOf(
        Cookie(
            "decviceId",
            "D3307E06-7629-4A1F-867D-4D75AFF19EBD",
            domain = "ilearntec.jlu.edu.cn",
            path = "/"
        )
    )
    private val oldestCookie: AtomicLong = AtomicLong(0L)
    private val mutex = Mutex()

    override suspend fun get(requestUrl: Url): List<Cookie> = mutex.withLock {

        val date = GMTDate()
        if (date.timestamp >= oldestCookie.get()) cleanup(date.timestamp)

        return@withLock container.filter { it.matches(requestUrl) }.also {
            println("[COOKIE-GET]\n    get cookie: ${it.map { it.name + "=" + it.value }} for $requestUrl")
        }
    }

    override suspend fun addCookie(requestUrl: Url, cookie: Cookie): Unit = mutex.withLock {
        with(cookie) {
            if (name.isBlank()) return@withLock
        }
        println("[COOKIE-SET]\n" +
                "    "+cookie.name + "=" + cookie.value + " for " + requestUrl)
        container.removeAll { it.name == cookie.name && it.matches(requestUrl) }
        container.add(cookie.fillDefaults(requestUrl))
        cookie.expires?.timestamp?.let { expires ->
            if (oldestCookie.get() > expires) {
                oldestCookie.set(expires)
            }
        }
    }

    override fun close() {
    }

    private fun cleanup(timestamp: Long) {
        container.removeAll { cookie ->
            val expires = cookie.expires?.timestamp ?: return@removeAll false
            expires < timestamp
        }

        val newOldest = container.fold(Long.MAX_VALUE) { acc, cookie ->
            cookie.expires?.timestamp?.let { min(acc, it) } ?: acc
        }

        oldestCookie.set(newOldest)
    }
}

val client = HttpClient(OkHttp) {
    install(Logging) {
        logger = object : Logger {
            val mutex = Mutex()
            override fun log(message: String) {
                GlobalScope.launch {
                    mutex.withLock {
                        println("[HTTP]\n" + message.lines().map { "    $it" }.joinToString("\n"))
                    }
                }
            }
        }
        level = LogLevel.INFO
    }
    install(DefaultRequest) {
        headers {
            set("Accept", "*/*")
            set("Accept-Charset", "*")
        }
    }
    install(ContentNegotiation) {
        json(Json {
            ignoreUnknownKeys = true
            encodeDefaults = true
            prettyPrint = true
        })
    }
    install(HttpRedirect) {
        checkHttpMethod = false
        allowHttpsDowngrade = true
    }
    install(HttpCookies) {
        storage = CCookiesStorage
    }
    install(UserAgent) {
        agent =
            "Mozilla/5.0 (Macintosh; Intel Mac OS X 10_15_7) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/126.0.0.0 Safari/537.36"
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
        socketTimeoutMillis = Long.MAX_VALUE
        connectTimeoutMillis = 15_000
    }
    engine {
        config {
//            proxy(ProxyConfig(Proxy.Type.HTTP, InetSocketAddress("127.0.0.1", 6725)))
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
//    System.setErr(err)
//    System.setErr(out)
    States.loadAll()
    if (DB.getValue("cookie_store") != null)
        CCookiesStorage.loadFromJsonString(DB.getValue("cookie_store")!!)
    try {
        app()
    } catch (e: Exception) {
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

fun app() {
    application {
        Window(onCloseRequest = {
            States.saveAll()
            DB.setValue("cookie_store", CCookiesStorage.exportToJsonString())
            exitApplication()
        }, title = "Study at JLU") {
            val darkTheme = isSystemInDarkTheme()
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
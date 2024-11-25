package cas

import CAS_URL
import io.ktor.client.*
import io.ktor.client.request.*
import io.ktor.client.request.forms.*
import io.ktor.client.statement.*
import io.ktor.http.*
import kotlinx.serialization.json.Json
import logOut
import org.jsoup.Jsoup
import utils.String
import java.util.Base64

object CasLogin {
    var logined = false
    suspend fun login(client: HttpClient, username: String, password: String): Boolean {
if (logined) return true
        try {
            val casResp = client.get(CAS_URL){
                parameter("service", "https://jwcidentity.jlu.edu.cn/iplat-pass-jlu/thirdLogin/jlu/login")
            }
            require(casResp.status.isSuccess())
            val casHtml = Jsoup.parse(casResp.bodyAsText())
            val casEvent = casHtml.selectFirst("#loginForm > input:nth-child(10)")?.attr("value")
            val casExecution = casHtml.selectFirst("#loginForm > input:nth-child(9)")?.attr("value")
            val casNonce = casHtml.selectFirst("#lt")?.attr("value")
            require(casNonce != null && casEvent != null && casExecution != null) { "CAS Nonce is null" }

            val casTicketResp = client.prepareForm(CAS_URL, formParameters = parameters {
                append("rsa", strEnc(username + password + casNonce, "1", "2", "3"))
                append("ul", username.length.toString())
                append("pl", password.length.toString())
                append("sl", "0")
                append("lt", casNonce)
                append("execution", casExecution)
                append("_eventId", casEvent)
            }) {
                parameter("service", "https://jwcidentity.jlu.edu.cn/iplat-pass-jlu/thirdLogin/jlu/login")
            }.execute()

            val htmlTicket = Jsoup.parse(casTicketResp.bodyAsText())
            //cas_username = html_cas.select_one('#username').attrs['value']
            //cas_password = html_cas.select_one('#password').attrs['value']
            val casUsername = htmlTicket.selectFirst("#username")?.attr("value")
            val casPassword = htmlTicket.selectFirst("#password")?.attr("value")
            require(casUsername != null && casPassword != null) { ;"CAS Username or Password is null" }

            val ts0 = System.currentTimeMillis()
            val ilearnGetNonceResp0 = client.get("https://ilearn.jlu.edu.cn/cas-server/login") {
                parameter("service", "https://ilearntec.jlu.edu.cn/")
                parameter("get-lt", "true")
                parameter("callback", "jsonpcallback")
                parameter("n", ts0 + 1)
                parameter("_", ts0)
            }
            require(ilearnGetNonceResp0.status.isSuccess()) { "Get CAS Nonce Failed" }
            val ilearnCasReturn0 = Json.Default.parseToJsonElement(
                ilearnGetNonceResp0.bodyAsText().substring(14, ilearnGetNonceResp0.bodyAsText().length - 2)
            )

            val ts = System.currentTimeMillis()
            val ilearnGetNonceResp = client.get("https://ilearn.jlu.edu.cn/cas-server/login") {
                parameter("service", "https://ilearntec.jlu.edu.cn/")
                parameter("username", casUsername)
                parameter("password", Base64.getEncoder().encodeToString(casPassword.toByteArray()))
                parameter("callback", "logincallback")
                parameter("lt", ilearnCasReturn0.String("lt"))
                parameter("execution", ilearnCasReturn0.String("execution"))
                parameter("n", ts + 1)
                parameter("isajax", "true")
                parameter("isframe", "true")
                parameter("_eventId", "submit")
                parameter("_", ts)
            }
            require(ilearnGetNonceResp.status.isSuccess()) { "Ilraen CAS Login Failed" }
            val ilearnCasReturn = Json.Default.parseToJsonElement(
                ilearnGetNonceResp.bodyAsText().substring(14, ilearnGetNonceResp.bodyAsText().length - 4)
            )

            client.get("https://ilearn.jlu.edu.cn/iplat/ssoservice") {
                parameter("ssoservice", "https://ilearntec.jlu.edu.cn/")
                parameter("ticket", ilearnCasReturn.String("ticket"))
            }
            client.get("https://ilearntec.jlu.edu.cn/coursecenter/main/index")
            logined = true
            return true
        } catch (e: Exception) {
            logOut(e.message)
            e.printStackTrace()
            return false
        }

    }
}
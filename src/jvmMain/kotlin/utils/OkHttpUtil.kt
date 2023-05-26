package utils

import java.security.KeyManagementException
import java.security.NoSuchAlgorithmException
import java.security.SecureRandom
import java.security.cert.X509Certificate
import javax.net.ssl.*

/**
 *
 * @author Vania
 */
object OkHttpUtil {
    /**
     * X509TrustManager instance which ignored SSL certification
     */
    val IGNORE_SSL_TRUST_MANAGER_X509: X509TrustManager = object : X509TrustManager {
        override fun checkClientTrusted(chain: Array<X509Certificate>, authType: String) {}
        override fun checkServerTrusted(chain: Array<X509Certificate>, authType: String) {}
        override fun getAcceptedIssuers(): Array<X509Certificate> {
            return arrayOf()
        }
    }

    @get:Throws(NoSuchAlgorithmException::class, KeyManagementException::class)
    val ignoreInitedSslFactory: SSLSocketFactory
        /**
         * Get initialized SSLContext instance which ignored SSL certification
         *
         * @return
         * @throws NoSuchAlgorithmException
         * @throws KeyManagementException
         */
        get() {
            val sslContext = SSLContext.getInstance("SSL")
            sslContext.init(null, arrayOf<TrustManager>(IGNORE_SSL_TRUST_MANAGER_X509), SecureRandom())
            return sslContext.socketFactory
        }
    val ignoreSslHostnameVerifier: HostnameVerifier
        /**
         * Get HostnameVerifier which ignored SSL certification
         *
         * @return
         */
        get() = HostnameVerifier { arg0, arg1 -> true }
}
package utils.conf

import logOut
import java.io.File

object Conf {
    private val home = System.getProperty("user.home")
    private val configFile = File(home, ".downloader.conf")
    private val config: MutableMap<String, String> = mutableMapOf()

    init {
        if (configFile.exists()) {
            configFile.readLines().forEach {
                val (key, value) = it.split("=")
                config[key] = value
            }
        }else{
            configFile.createNewFile()
            setConf("savepath",".")
        }
    }

    fun setConf(key: String, value: String) {
        config[key] = value
        configFile.writeText(config.map { "${it.key}=${it.value}" }.joinToString("\n"))
        logOut("$key set to $value")
    }

    fun getConf(key: String): String? {
        return config[key]
    }
}
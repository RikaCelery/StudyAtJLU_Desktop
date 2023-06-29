package utils.conf

import utils.DB

object Conf {
    private var savePathCache: String? = null
    var savePath: String
        get() {
            val path = if (savePathCache != null) {
                savePathCache
            } else {
                savePathCache = DB.getValue("save_path")
                println("cache path $savePathCache")
                savePathCache
            }
            return path ?: "."
        }
        set(value) {
            if (value != savePathCache) {
                savePathCache = value
                DB.setValue("save_path", value)
                println("update savePathCache to $value")
            }
            println("set save path to $value")
        }


}
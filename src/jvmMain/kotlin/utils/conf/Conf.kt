package utils.conf

import utils.DB
import kotlin.io.path.Path
import kotlin.io.path.absolutePathString

object Conf {
    private var savePathCache: String? = null
    var savePath: String
        get() {
            val path = if (savePathCache != null) {
                savePathCache!!
            } else {
                val pathString = Path(".").absolutePathString()
                DB.getValue("save_path")?.let {
                    savePathCache = DB.getValue("save_path")
                } ?: run {
                    DB.setValue("save_path", pathString)
                }
                savePathCache?:pathString
            }
            return path
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
package utils.conf

import utils.DB
import kotlin.io.path.Path
import kotlin.io.path.absolutePathString

object Conf {
    private var savePathCache: String? = null
    private var _savePath : Result<String?>? = null
    var savePath: String
        get() {
            val path = if (_savePath != null) {
                _savePath!!.getOrNull()
            } else {
                _savePath = Result.success(DB.getValue("save_path"))
                println("cache path ${_savePath!!.getOrNull()}")
                _savePath!!.getOrNull()
            }
            return path ?: Path(".").absolutePathString()
        }
        set(value) {
            if (value != _savePath?.getOrNull()) {
                _savePath = Result.success(value)
                DB.setValue("save_path", value)
                println("update savePathCache to $value")
            }
            println("set save path to $value")
        }


}
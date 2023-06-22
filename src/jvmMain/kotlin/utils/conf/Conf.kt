package utils.conf

import utils.createTable
import utils.getSavePathFromDB
import utils.setupDatabase
import utils.updateSavePath

object Conf {
    private var savePath:String?=null
    init {
        createTable()
        setupDatabase()
        savePath= getSavePathFromDB()
        println("save path is $savePath")
    }
    fun setSavePath(string: String){
        savePath=string
        updateSavePath(string)
        println("set save path to $savePath")
    }
}
package utils

import org.jetbrains.exposed.dao.id.IntIdTable
import org.jetbrains.exposed.sql.*
import org.jetbrains.exposed.sql.transactions.transaction
object Conf:IntIdTable(){


    val savePath=text("savePath").uniqueIndex()
}
fun updateSavePath(savePath: String) {
    transaction {
        val rowCount = Conf.selectAll().count()
        if (rowCount.toInt() == 0) {
            // 插入默认值
            Conf.insert {
                it[Conf.savePath] = "."
            }
        } else {
            // 更新现有值
            Conf.update {
                it[Conf.savePath] = savePath
            }
        }
    }
}
fun getSavePathFromDB(): String {
    return transaction {
        createTable()
        setupDatabase()
        Conf.selectAll().map { it[Conf.savePath] }.firstOrNull() ?: ""
    }
}

// 设置数据库连接
fun setupDatabase() {
    Database.connect("jdbc:sqlite:data.db", "org.sqlite.JDBC")
}
// 创建表
fun createTable() {
    transaction {
        SchemaUtils.create(Conf)
    }
}

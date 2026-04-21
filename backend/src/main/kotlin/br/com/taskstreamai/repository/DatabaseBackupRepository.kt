package br.com.taskstreamai.repository

import org.slf4j.LoggerFactory
import org.springframework.beans.factory.annotation.Value
import org.springframework.jdbc.core.JdbcTemplate
import org.springframework.stereotype.Repository
import java.io.File

private const val EXECUTING_LOG = "Executing {}"

@Repository
class DatabaseBackupRepository(
    val jdbcTemplate: JdbcTemplate,
    @Value("\${database.backup.base-path}")
    private var basePath: String
) {

    val logger = LoggerFactory.getLogger(this.javaClass)

    fun createSqlDatabaseBackup(filename: String): String {

        val home = System.getProperty("user.home")
        val sqlFilename = "$home/$basePath/sql/$filename.sql"

        val sql = "SCRIPT TO '$sqlFilename' COMPRESSION ZIP"

        return executeDatabaseBackup(sqlFilename, sql)

    }

    fun createDbDatabaseBackup(filename: String): String {

        val home = System.getProperty("user.home")
        val dbFilename = "$home/$basePath/db/$filename.db.zip"
        val sql = "BACKUP TO '$dbFilename'"

        return executeDatabaseBackup(dbFilename, sql)

    }

    fun restoreDatabaseBackup(filename: String): String {
        logger.info("Restoring database backup for filename {}", filename);

        val home = System.getProperty("user.home")
        val file = File("$home/$basePath/sql/$filename.sql")

        if (!file.exists()) {
            throw RuntimeException("Database backup does not exist for $filename zip");
        }

        logger.info("File found starting restoring");

        val dropObjects = "DROP ALL OBJECTS"

        logger.info(EXECUTING_LOG, dropObjects);

        jdbcTemplate.execute(dropObjects);

        val restoreDatabase = "RUNSCRIPT FROM '${file.absolutePath}' COMPRESSION ZIP"

        logger.info(EXECUTING_LOG, restoreDatabase);

        jdbcTemplate.execute(restoreDatabase);

        logger.info("Database backup for filename {} successful restored", filename);

        return filename;
    }

    private fun executeDatabaseBackup(filename: String, sql: String) : String {

        logger.info("Creating database backup for filename {}", filename)

        logger.info(EXECUTING_LOG, sql)

        jdbcTemplate.execute(sql);

        logger.info("Database backup for filename {} successful created", filename);

        return filename;

    }

}
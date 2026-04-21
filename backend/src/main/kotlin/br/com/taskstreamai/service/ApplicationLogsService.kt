package br.com.taskstreamai.service

import br.com.taskstreamai.dto.LogDTO
import org.springframework.beans.factory.annotation.Value
import org.springframework.stereotype.Service
import java.nio.file.Files
import kotlin.io.path.Path

@Service
class ApplicationLogsService(
    @Value("\${logging.file.name}")
    private val logfilePath: String
) {

    companion object {
        const val LOG_LIMIT_LINES = 500
    }

    fun retrieveLogs(logLimit: Int): LogDTO {
        val logFile = Files.readAllLines(Path(logfilePath))

        val logSize = logFile.size
        val logs = mutableListOf<String>()
        if (logSize <= logLimit) {
            logFile.forEach {
                logs.add(it.trim())
            }
            return LogDTO(
                title = logfilePath,
                logs = logs,
            )
        }

        val logStart = logSize - logLimit

        logFile.slice(logStart..(logSize-1)).forEach {
            logs.add(it.trim())
        }

        return LogDTO(
            title = logfilePath,
            logs = logs
        )
    }

}
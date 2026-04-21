package br.com.taskstreamai

import org.springframework.boot.autoconfigure.SpringBootApplication
import org.springframework.boot.runApplication
import org.springframework.scheduling.annotation.EnableScheduling

@SpringBootApplication
@EnableScheduling
class TaskStreamAiApplication

fun main(args: Array<String>) {
	runApplication<TaskStreamAiApplication>(*args)
}

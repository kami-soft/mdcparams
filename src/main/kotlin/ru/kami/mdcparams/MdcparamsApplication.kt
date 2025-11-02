package ru.kami.mdcparams

import org.springframework.boot.autoconfigure.SpringBootApplication
import org.springframework.boot.runApplication

/* Задачи:
- создать аннотацию для полей и аннотацию принудительного анализа бинов, которые должны будут анализироваться
- создать бин-постпроцессор, который будет анализировать предустановленный перечень бинов и принудительные бины,
   делать для них прокси, которые будут пихать в MDC аннотированные пажраметры
- дополнить опенапи-генерацию
 */

@SpringBootApplication
class MdcparamsApplication

fun main(args: Array<String>) {
	runApplication<MdcparamsApplication>(*args)
}

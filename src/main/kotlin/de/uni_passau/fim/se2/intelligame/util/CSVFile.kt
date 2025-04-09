package de.uni_passau.fim.se2.intelligame.util

import java.io.File

class CSVFile(private val headers: List<String>) {
    private val content: StringBuilder = StringBuilder()

    fun appendLine(data: List<String>) {
        content.appendLine(data.joinToString(","))
    }

    fun appendLine(data: String) {
        content.appendLine(data)
    }

    fun save(path: String) {
        val file = File(path)

        file.parentFile?.mkdirs()

        if(!file.exists()) {
            file.parentFile.mkdirs()

            val data = headers.joinToString(",") + "\n" + content
            file.writeText(data)
        }else{
            file.appendText(content.toString())
        }

        content.clear()
    }
}
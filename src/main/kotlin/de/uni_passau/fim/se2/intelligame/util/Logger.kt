/*
 * Copyright 2023 IntelliGame contributors
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package de.uni_passau.fim.se2.intelligame.util

import com.intellij.openapi.project.Project
import java.io.BufferedWriter
import java.io.File
import java.io.FileWriter
import java.io.Writer
import java.sql.Timestamp

object Logger {
    private const val LOG_FILE_NAME = "EvaluationLogs.txt"

    enum class Kind {
        Notification, Main, Debug, Error
    }

    fun logStatus(text: String, kind: Kind, project: Project?) {
        if (project == null) {
            println("LOGGER - Invalid project : (${kind.name}) - $text")
            return
        }

        try {
            val path = project.basePath + "${File.separator}.evaluation${File.separator}$LOG_FILE_NAME"
            val output: Writer = BufferedWriter(FileWriter(File(path), true))

            val timestamp = Timestamp(System.currentTimeMillis()).toString()
            val data = "$timestamp - ${kind.name} - $text"
            output.appendLine(data)
            output.close()

            println(data)
        } catch (_: Exception) {}
    }
}
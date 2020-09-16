package org.mikeneck.ktfmt

import com.facebook.ktfmt.format
import org.gradle.api.tasks.SourceTask
import org.gradle.api.tasks.TaskAction
import java.io.File

open class KtFmtTask: SourceTask() {

    @TaskAction
    open fun execute() {
        val source = this.source.filter { it.name.endsWith(".kt") || it.name.endsWith(".kts") }
        val formatted = source.map { formatFile(it) }
        if (formatted.all { it.isRight }) {
            val files = formatted.filter { e -> e.mapRight { it.second.updated }.mapLeft { false } }
            files.forEach { e ->
                    e.onRight { r -> r.first.writeText(r.second.formatted, Charsets.UTF_8) }
                        .onLeft { /* do nothing */ }
                }
            logger.lifecycle("formatted {} files", files.size)
        } else {
            formatted.filter { e -> e.mapRight { false }.mapLeft { true } }
                .forEach { e ->
                    e.onLeft { l ->
                        logger.error("error: file={}, error={}", l.first, l.second, l.second)
                    }
                }
        }
    }


    fun formatFile(file: File): Either<FormatFailed, Pair<File, FormatResult>>  =
        either { file.readText(Charsets.UTF_8) }.mapError { KtFmtException.from(it) }
            .flatMap { either { file to (it to format(it)) }.mapError { KtFmtException.from(it) } }.mapError { file to it }
}

typealias FormatResult = Pair<String, String>

val FormatResult.original: String get() = this.first
val FormatResult.formatted: String get() = this.second
val FormatResult.updated: Boolean get() = this.first != this.second

typealias FormatFailed = Pair<File, KtFmtException>

package org.mikeneck.ktfmt

import com.facebook.ktfmt.ParseError
import com.google.googlejavaformat.java.FormatterException
import java.io.IOException

class KtFmtException: Exception {

    constructor(formatterException: FormatterException): super(formatterException)

    constructor(parseError: ParseError): super(parseError)

    constructor(ioException: IOException): super(ioException)

    companion object {
        @Throws(IllegalStateException::class)
        fun from(throwable: Throwable): KtFmtException = when(throwable) {
            is FormatterException -> KtFmtException(throwable)
            is ParseError -> KtFmtException(throwable)
            is IOException -> KtFmtException(throwable)
            else -> throw IllegalStateException(throwable)
        }
    }
}

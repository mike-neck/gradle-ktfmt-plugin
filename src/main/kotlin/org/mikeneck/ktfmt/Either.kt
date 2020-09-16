package org.mikeneck.ktfmt

interface Either <L: Any, R: Any>: OnLeft<L> {
    fun <N: Any> map(f: (R) -> N): Either<L, N>
    fun <N: Any> flatMap(f: (R) -> Either<L, N>): Either<L, N>
    fun onRight(c: (R) -> Unit): OnLeft<L>
    fun <T: Throwable> throwIfLeft(f: (L) -> T): R
    fun <K: Any> mapError(f: (L) -> K): Either<K, R>
    val isRight: Boolean get() = false
    fun <T: Any> mapRight(f: (R) -> T): MapLeft<L, T>
}

interface OnLeft<L: Any> {
    fun onLeft(a: (L) -> Unit): Unit
}

interface MapLeft<L: Any, T: Any> {
    fun mapLeft(m: (L) -> T): T
}

internal class Right<L: Any, R: Any>(val value: R): Either<L, R> {
    override val isRight: Boolean get() = true

    override fun <N : Any> map(f: (R) -> N): Either<L, N> = Right(f(value))

    override fun <N : Any> flatMap(f: (R) -> Either<L, N>): Either<L, N> = f(value)

    override fun onRight(c: (R) -> Unit): OnLeft<L> = object : OnLeft<L> {
        override fun onLeft(a: (L) -> Unit) = c(value)
    }

    override fun <T : Throwable> throwIfLeft(f: (L) -> T): R = value

    override fun onLeft(a: (L) -> Unit) = Unit

    override fun <K : Any> mapError(f: (L) -> K): Either<K, R> =Right(value)

    override fun <T : Any> mapRight(f: (R) -> T): MapLeft<L, T> =
        object : MapLeft<L, T> {
            override fun mapLeft(m: (L) -> T): T = f(value)
        }
}

internal class Left<L: Any, R: Any>(val value: L): Either<L, R> {
    override fun <N : Any> map(f: (R) -> N): Either<L, N> = Left(value)

    override fun <N : Any> flatMap(f: (R) -> Either<L, N>): Either<L, N> = Left(value)

    override fun onRight(c: (R) -> Unit): OnLeft<L> = object : OnLeft<L> {
        override fun onLeft(a: (L) -> Unit) = a(value)
    }

    override fun <T : Throwable> throwIfLeft(f: (L) -> T): R = throw f(value)

    override fun onLeft(a: (L) -> Unit) = a(value)

    override fun <K : Any> mapError(f: (L) -> K): Either<K, R> = Left(f(value))

    override fun <T : Any> mapRight(f: (R) -> T): MapLeft<L, T> =
        object : MapLeft<L, T> {
            override fun mapLeft(m: (L) -> T): T = m(value)
        }
}

fun <R: Any> either(action: () -> R): Either<Throwable, R> = runCatching {
    action()
}.fold(
    onSuccess = { Right(it) },
    onFailure = { Left(it) }
)

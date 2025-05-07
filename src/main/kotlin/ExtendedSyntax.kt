package se.gu

object ExtendedSyntax {

    inline fun <T> T?.ifPresent(action: (T) -> Unit) {
        if (this != null) action(this)
    }

    inline fun <T, R> T?.map(transform: (T) -> R): R? =
        if (this != null) transform(this) else null

    inline fun <X, Y> combine(xs: List<X>, ys: List<Y>, combinator: (X, Y) -> Unit) {
        val xsIter = xs.iterator()
        val ysIter = ys.iterator()
        while (xsIter.hasNext() && ysIter.hasNext()) {
            combinator(xsIter.next(), ysIter.next())
        }
    }
}

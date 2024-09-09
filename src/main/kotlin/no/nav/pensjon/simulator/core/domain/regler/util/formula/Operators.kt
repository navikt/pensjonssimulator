package no.nav.pensjon.simulator.core.domain.regler.util.formula

import no.nav.pensjon.simulator.core.domain.regler.util.formula.Operator.*


/**
 * Comparators
 */
operator fun Number.compareTo(i: Int): Int {
    return when (this) {
        is Int -> Integer.compare(this, i)
        is Double -> this.compareTo(i.toDouble())
        else -> {
            this.compareTo(i)
        }
    }
}
operator fun Int.compareTo(i: Number): Int = i.compareTo(this)

/**
 * Operators with syntax
 */
enum class Operator(val syntax: String) {
    PLUS(" + "),
    MINUS(" - "),
    TIMES(" * "),
    DIVIDE(" / "),
    MODULUS(" % ")
}

/**
 * Plus
 */
operator fun Number.plus(right: Formel): Formel = Formel(this).expand(PLUS, right)
operator fun Formel.plus(right: Number): Formel = this.expand(PLUS, Formel(right))
operator fun Formel.plus(right: Formel): Formel = this.expand(PLUS, right)

/**
 * Minus
 */
operator fun Number.minus(right: Formel): Formel = Formel(this).expand(MINUS, right)
operator fun Formel.minus(right: Number): Formel = this.expand(MINUS, Formel(right))
operator fun Formel.minus(right: Formel): Formel = this.expand(MINUS, right)

/**
 * Times
 */
operator fun Number.times(right: Formel): Formel = Formel(this).expand(TIMES, right)
operator fun Formel.times(right: Number): Formel = this.expand(TIMES, Formel(right))
operator fun Formel.times(right: Formel): Formel = this.expand(TIMES, right)

/**
 * Division
 */
operator fun Number.div(right: Formel): Formel = Formel(this).expand(DIVIDE, right)
operator fun Formel.div(right: Number): Formel = this.expand(DIVIDE, Formel(right))
operator fun Formel.div(right: Formel): Formel = this.expand(DIVIDE, right)

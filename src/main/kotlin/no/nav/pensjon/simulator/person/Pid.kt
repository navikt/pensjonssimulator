package no.nav.pensjon.simulator.person

/**
 * Person identifier, e.g. fødselsnummer (FNR).
 */
@JvmInline
value class Pid(val argument: String) {

    val isValid: Boolean
        get() = argument.length == FOEDSELSNUMMER_LENGTH

    val value: String
        get() = if (isValid) argument else "invalid"

    val displayValue : String
        get() = redact(value)

    override fun toString(): String = displayValue

    companion object {
        private const val FOEDSELSNUMMER_LENGTH = 11
        private const val PERSONNUMMER_START_INDEX = 6

        fun redact(pid: String?): String =
            pid?.let {
                if (it.length == FOEDSELSNUMMER_LENGTH)
                    it.substring(0, PERSONNUMMER_START_INDEX) + "*****"
                else
                    "?(${it.length})?"
            } ?: "<null>"
    }
}

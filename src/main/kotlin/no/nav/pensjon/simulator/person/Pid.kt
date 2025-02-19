package no.nav.pensjon.simulator.person

/**
 * Person identifier, e.g. fødselsnummer (FNR).
 */
class Pid(argument: String) {

    val isValid = argument.length == FOEDSELSNUMMER_LENGTH
    val value = if (isValid) argument else "invalid"
    val displayValue = redact(value)

    override fun toString(): String = displayValue

    override fun hashCode(): Int = value.hashCode()

    override fun equals(other: Any?): Boolean =
        (other as? Pid)?.let { value == it.value } == true

    companion object {
        private const val FOEDSELSNUMMER_LENGTH = 11
        private const val PERSONNUMMER_START_INDEX = 6

        fun redact(pid: String?): String =
            pid?.let {
                if (it.length == FOEDSELSNUMMER_LENGTH)
                    it.substring(0, 8) + "***"
                else
                    "?(${it.length})?"
            } ?: "<null>"
    }
}

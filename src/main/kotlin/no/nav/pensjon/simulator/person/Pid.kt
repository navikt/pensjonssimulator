package no.nav.pensjon.simulator.person

/**
 * Person identifier, e.g. fødselsnummer (FNR).
 */
data class Pid(val argument: String) {

    // NB:
    // Do not make this an inline value class, since @JsonIgnore will then fail to ignore it, e.g. in PenPerson

    val isValid = argument.length == FOEDSELSNUMMER_LENGTH
    val value = if (isValid) argument else "invalid"
    val displayValue = redact(value)

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

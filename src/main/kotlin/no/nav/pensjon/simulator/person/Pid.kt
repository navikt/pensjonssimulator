package no.nav.pensjon.simulator.person

/**
 * Person identifier, e.g. fødselsnummer (FNR).
 */
class Pid(argument: String) {

    val isValid = argument.length == FNR_LENGTH
    val value = if (isValid) argument else "invalid"
    val displayValue = if (isValid) value.substring(0, PERSONNUMMER_START_INDEX) + "*****" else value

    override fun toString(): String {
        return displayValue
    }

    override fun hashCode(): Int {
        return value.hashCode()
    }

    override fun equals(other: Any?): Boolean {
        return (other as? Pid)?.let { value == it.value } ?: false
    }

    companion object {
        private const val FNR_LENGTH = 11
        private const val PERSONNUMMER_START_INDEX = 6
    }
}

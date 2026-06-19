package no.nav.pensjon.simulator.person

object FoedselsnummerUtil {
    private val FOEDSELSNUMMER_REGEX = """[0-9]{2}([0-9]{4})[0-9]{5}""".toRegex()

    /**
     * Redakterer fødselsnummer slik at bare de 4 tallene som representerer måned og år forblir synlige.
     */
    fun redact(value: String?) =
        value?.let { FOEDSELSNUMMER_REGEX.replace(it, "**$1*****") }
}
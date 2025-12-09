package no.nav.pensjon.simulator.generelt.organisasjon

/**
 * Representerer organisasjonsnummer (no.wikipedia.org/wiki/Organisasjonsnummer)
 */
@JvmInline
value class Organisasjonsnummer(val value: String) {

    init {
        require(value.length == REQUIRED_LENGTH) {
            "Feil lengde (${value.length}) på organisasjonsnummer; må være $REQUIRED_LENGTH"
        }
    }

    override fun toString(): String = value

    companion object {
        val nav = Organisasjonsnummer("889640782")
        private const val REQUIRED_LENGTH = 9
    }
}

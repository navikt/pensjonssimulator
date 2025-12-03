package no.nav.pensjon.simulator.alder

import java.time.LocalDate

/**
 * Inneholder pensjonsrelatart alder og den datoen som alderen representerer relativt til fødselsdato.
 * "Pensjonsrelatert" vil si at det er første dag i måneden som brukes.
 */
@ConsistentCopyVisibility
data class PensjonAlderDato private constructor(
    val alder: Alder,
    val dato: LocalDate
) {
    constructor(foedselDato: LocalDate, alder: Alder)
            : this(alder, datoVedAlder(foedselDato, alder))

    constructor(foedselDato: LocalDate, dato: LocalDate)
            : this(alderVedDato(foedselDato, dato), dato)

    private companion object {
        /**
         * Pensjonsrelatert dato er første dag i måneden etter 'aldersbasert' dato.
         */
        private fun datoVedAlder(foedselsdato: LocalDate, alder: Alder): LocalDate =
            foedselsdato
                .plusYears(alder.aar.toLong())
                .withDayOfMonth(1) // første dag i...
                .plusMonths(alder.maaneder.toLong() + 1L) // ...måneden etter 'aldersbasert' dato

        // TODO compare this with SimuleringRequestConverter.convertDatoFomToAlder
        private fun alderVedDato(foedselsdato: LocalDate, dato: LocalDate): Alder =
            Alder.from(foedselsdato, dato)
    }
}

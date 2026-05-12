package no.nav.pensjon.simulator.core.domain.regler.grunnlag

import java.time.LocalDate

// 2026-04-23
/**
 * Objektet inneholder informasjon om den månedlige faste utgiften en bruker har hatt i forbindelse med
 * opphold på en institusjon. Det inneholder også tidsrommet brukeren var innlagt.
 */
class InstOpphFasteUtgifterperiode {
    //	instOpphFasteUtgifterperiodeId	long	Unik identifikasjon av objektet.
    //	fomDato	Date	Dato bruker ble innlagt
    //	tomDato	Date	Dato bruker ble skrevet ut.
    //	fasteUtgifter	int	månedlig fast utgift bruker hadde på
    /**
     * Unik identifikasjon av objektet
     */
    var instOpphFasteUtgifterperiodeId: Long = 0

    /**
     * Dato bruker ble innlagt
     */
    var fomLd: LocalDate? = null

    /**
     * Dato bruker ble skrevet ut
     */
    var tomLd: LocalDate? = null

    /**
     * månedlig fast utgift bruker hadde på institusjonen
     */
    var fasteUtgifter = 0
}

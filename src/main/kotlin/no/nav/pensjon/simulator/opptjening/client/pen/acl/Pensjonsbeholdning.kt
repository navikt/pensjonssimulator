package no.nav.pensjon.simulator.opptjening.client.pen.acl

import no.nav.pensjon.simulator.core.domain.regler.enum.BeholdningtypeEnum
import no.nav.pensjon.simulator.opptjening.Pensjonsbeholdning as DomainPensjonsbeholdning
import java.time.LocalDate

/**
 * NB: This class must be named 'Pensjonsbeholdning', due to API constraints.
 */
open class Pensjonsbeholdning : PenBeholdning {
    override var beholdningsTypeEnum: BeholdningtypeEnum = BeholdningtypeEnum.PEN_B
    var fomLd: LocalDate? = null
    var tomLd: LocalDate? = null

    constructor() : super() {
        beholdningsTypeEnum = BeholdningtypeEnum.PEN_B
    }

    constructor(source: Pensjonsbeholdning) : super(source) {
        fomLd = source.fomLd
        tomLd = source.tomLd
    }

    fun toInternalValue() =
        DomainPensjonsbeholdning(
            aar = ar,
            fom = fomLd,
            tom = tomLd,
            totalbeloep = totalbelop,
            opptjening = opptjening,
            loennsvekstinformasjon = lonnsvekstInformasjon,
            reguleringsinformasjon = reguleringsInformasjon,
            formelkode = formelKodeEnum,
            merknadListe = merknadListe
        )
}
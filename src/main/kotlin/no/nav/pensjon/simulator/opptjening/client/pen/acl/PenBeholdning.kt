package no.nav.pensjon.simulator.opptjening.client.pen.acl

import com.fasterxml.jackson.annotation.JsonSubTypes
import com.fasterxml.jackson.annotation.JsonTypeInfo
import no.nav.pensjon.simulator.core.domain.regler.Merknad
import no.nav.pensjon.simulator.core.domain.regler.Opptjening
import no.nav.pensjon.simulator.core.domain.regler.beregning2011.LonnsvekstInformasjon
import no.nav.pensjon.simulator.core.domain.regler.beregning2011.ReguleringsInformasjon
import no.nav.pensjon.simulator.core.domain.regler.enum.BeholdningtypeEnum
import no.nav.pensjon.simulator.core.domain.regler.enum.FormelKodeEnum
import no.nav.pensjon.simulator.core.domain.regler.grunnlag.AfpOpptjening
import no.nav.pensjon.simulator.core.domain.regler.grunnlag.Garantipensjonsbeholdning
import no.nav.pensjon.simulator.core.domain.regler.grunnlag.Garantitilleggsbeholdning
import no.nav.pensjon.simulator.core.domain.reglerextend.beregning2011.copy
import no.nav.pensjon.simulator.core.domain.reglerextend.copy

/**
 * Corresponds with Beholdning in pensjon-pen.
 */
@JsonSubTypes(
    JsonSubTypes.Type(value = Garantitilleggsbeholdning::class),
    JsonSubTypes.Type(value = AfpOpptjening::class),
    JsonSubTypes.Type(value = Garantipensjonsbeholdning::class),
    JsonSubTypes.Type(value = Pensjonsbeholdning::class)
)
@JsonTypeInfo(use = JsonTypeInfo.Id.NAME, include = JsonTypeInfo.As.PROPERTY, property = "type")
abstract class PenBeholdning protected constructor() {
    var ar = 0
    var totalbelop = 0.0
    var opptjening: Opptjening? = null
    var lonnsvekstInformasjon: LonnsvekstInformasjon? = null
    var reguleringsInformasjon: ReguleringsInformasjon? = null
    var formelKodeEnum: FormelKodeEnum? = null
    abstract var beholdningsTypeEnum: BeholdningtypeEnum
    var merknadListe: MutableList<Merknad> = mutableListOf()

    protected constructor(source: PenBeholdning) : this() {
        ar = source.ar
        totalbelop = source.totalbelop
        reguleringsInformasjon = source.reguleringsInformasjon?.let(::ReguleringsInformasjon)
        opptjening = source.opptjening?.let(::Opptjening)
        beholdningsTypeEnum = source.beholdningsTypeEnum
        formelKodeEnum = source.formelKodeEnum
        merknadListe = source.merknadListe.map { it.copy() }.toMutableList()
        lonnsvekstInformasjon = source.lonnsvekstInformasjon?.copy()
    }
}
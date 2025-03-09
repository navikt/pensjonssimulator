package no.nav.pensjon.simulator.core.domain.regler.grunnlag

import com.fasterxml.jackson.annotation.JsonSubTypes
import com.fasterxml.jackson.annotation.JsonTypeInfo
import no.nav.pensjon.simulator.core.domain.regler.Merknad
import no.nav.pensjon.simulator.core.domain.regler.Opptjening
import no.nav.pensjon.simulator.core.domain.regler.beregning2011.LonnsvekstInformasjon
import no.nav.pensjon.simulator.core.domain.regler.beregning2011.ReguleringsInformasjon
import no.nav.pensjon.simulator.core.domain.regler.enum.BeholdningtypeEnum
import no.nav.pensjon.simulator.core.domain.regler.enum.FormelKodeEnum
import no.nav.pensjon.simulator.core.domain.regler.kode.BeholdningsTypeCti
import no.nav.pensjon.simulator.core.domain.regler.kode.FormelKodeCti

@JsonSubTypes(
    JsonSubTypes.Type(value = Garantitilleggsbeholdning::class),
    JsonSubTypes.Type(value = AfpOpptjening::class),
    JsonSubTypes.Type(value = Garantipensjonsbeholdning::class),
    JsonSubTypes.Type(value = Pensjonsbeholdning::class)
)
@JsonTypeInfo(use = JsonTypeInfo.Id.NAME, include = JsonTypeInfo.As.PROPERTY, property = "type")
abstract class Beholdning protected constructor() {
    var ar = 0
    var totalbelop = 0.0
    var opptjening: Opptjening? = null
    var lonnsvekstInformasjon: LonnsvekstInformasjon? = null
    var reguleringsInformasjon: ReguleringsInformasjon? = null
    var formelkode: FormelKodeCti? = null
    var formelKodeEnum: FormelKodeEnum? = null
    abstract var beholdningsType: BeholdningsTypeCti
    abstract var beholdningsTypeEnum: BeholdningtypeEnum
    var merknadListe: MutableList<Merknad> = mutableListOf() // SIMDOM-EDIT: List -> MutableList

    protected constructor(source: Beholdning) : this() {
        ar = source.ar
        totalbelop = source.totalbelop
        reguleringsInformasjon = source.reguleringsInformasjon?.let(::ReguleringsInformasjon)
        opptjening = source.opptjening?.let(::Opptjening)
        beholdningsType = BeholdningsTypeCti(source.beholdningsType)
        beholdningsTypeEnum = source.beholdningsTypeEnum
        formelkode = source.formelkode?.let(::FormelKodeCti)
        formelKodeEnum = source.formelKodeEnum
        source.merknadListe.forEach { merknadListe.add(Merknad(it)) }
        lonnsvekstInformasjon = source.lonnsvekstInformasjon?.let(::LonnsvekstInformasjon)
    }
}

package no.nav.pensjon.simulator.core.domain.regler.grunnlag

import com.fasterxml.jackson.annotation.JsonSubTypes
import com.fasterxml.jackson.annotation.JsonTypeInfo
import no.nav.pensjon.simulator.core.domain.regler.Merknad
import no.nav.pensjon.simulator.core.domain.regler.Opptjening
import no.nav.pensjon.simulator.core.domain.regler.beregning2011.LonnsvekstInformasjon
import no.nav.pensjon.simulator.core.domain.regler.beregning2011.ReguleringsInformasjon
import no.nav.pensjon.simulator.core.domain.regler.kode.BeholdningsTypeCti
import no.nav.pensjon.simulator.core.domain.regler.kode.FormelKodeCti
import java.io.Serializable

@JsonSubTypes(
    JsonSubTypes.Type(value = Garantitilleggsbeholdning::class),
    JsonSubTypes.Type(value = AfpOpptjening::class),
    JsonSubTypes.Type(value = Garantipensjonsbeholdning::class),
    JsonSubTypes.Type(value = Pensjonsbeholdning::class)
)
@JsonTypeInfo(use = JsonTypeInfo.Id.NAME, include = JsonTypeInfo.As.PROPERTY, property = "type")
abstract class Beholdning(

    var ar: Int = 0,
    var totalbelop: Double = 0.0,
    var opptjening: Opptjening? = null,
    var lonnsvekstInformasjon: LonnsvekstInformasjon? = null,
    var reguleringsInformasjon: ReguleringsInformasjon? = null,
    var formelkode: FormelKodeCti? = null,
    var beholdningsType: BeholdningsTypeCti? = null, // SIMDOM-MOVE
    var merknadListe: MutableList<Merknad> = mutableListOf()
) : Serializable {

    protected constructor(aBeholdning: Beholdning) : this() {
        ar = aBeholdning.ar
        totalbelop = aBeholdning.totalbelop
        if (aBeholdning.reguleringsInformasjon != null) {
            reguleringsInformasjon = ReguleringsInformasjon(aBeholdning.reguleringsInformasjon!!)
        }
        if (aBeholdning.opptjening != null) {
            opptjening = Opptjening(aBeholdning.opptjening!!)
        }
        if (aBeholdning.beholdningsType != null) {
            beholdningsType = BeholdningsTypeCti(aBeholdning.beholdningsType)
        }
        if (aBeholdning.formelkode != null) {
            this.formelkode = FormelKodeCti(aBeholdning.formelkode!!)
        }
        for (m in aBeholdning.merknadListe) {
            merknadListe.add(Merknad(m))
        }
        if (aBeholdning.lonnsvekstInformasjon != null) {
            lonnsvekstInformasjon = LonnsvekstInformasjon(aBeholdning.lonnsvekstInformasjon!!)
        }
    }
}

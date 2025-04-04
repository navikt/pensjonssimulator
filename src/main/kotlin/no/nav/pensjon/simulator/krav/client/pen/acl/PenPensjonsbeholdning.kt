package no.nav.pensjon.simulator.krav.client.pen.acl

import no.nav.pensjon.simulator.core.domain.regler.Merknad
import no.nav.pensjon.simulator.core.domain.regler.Opptjening
import no.nav.pensjon.simulator.core.domain.regler.beregning2011.LonnsvekstInformasjon
import no.nav.pensjon.simulator.core.domain.regler.beregning2011.ReguleringsInformasjon
import no.nav.pensjon.simulator.core.domain.regler.enum.BeholdningtypeEnum
import no.nav.pensjon.simulator.core.domain.regler.enum.FormelKodeEnum
import java.util.Date

/**
 * 'Flattened' variant of Pensjonsbeholdning + Beholdning.
 */
class PenPensjonsbeholdning {
    var type: String? = null // avoids UnrecognizedPropertyException
    // From PEN domain:
    var fom: Date? = null
    var tom: Date? = null
    // From regler domain (Beholdning):
    var ar: Int = 0
    var totalbelop: Double = 0.0
    var opptjening: Opptjening? = null
    var lonnsvekstInformasjon: LonnsvekstInformasjon? = null
    var reguleringsInformasjon: ReguleringsInformasjon? = null
    var formelkodeEnum: FormelKodeEnum? = null
    var formelKodeEnum: FormelKodeEnum? = null // need both variants
    var beholdningsTypeEnum: BeholdningtypeEnum? = null
    var merknadListe: MutableList<Merknad> = mutableListOf()
}

package no.nav.pensjon.simulator.core.domain.regler.to

import no.nav.pensjon.simulator.core.domain.regler.beregning2011.BeregningsvilkarPeriode
import no.nav.pensjon.simulator.core.domain.regler.enum.KravlinjeTypeEnum
import no.nav.pensjon.simulator.core.domain.regler.enum.RegelverkTypeEnum
import no.nav.pensjon.simulator.core.domain.regler.grunnlag.Persongrunnlag
import no.nav.pensjon.simulator.core.domain.regler.grunnlag.Uttaksgrad
import java.util.*

// 2025-03-10
/**
 * Dataoverføringsobjekt, inndata, for tjenesten fastsettTrygdetid.
 */
class TrygdetidRequest : ServiceRequest() {
    /**
     * Virkningstidspunktets fom. for ønsket ytelse.
     */
    var virkFom: Date? = null

    /**
     * Tom for trygdetiden som skal beregnes. Kun for AP2011, AP2016 og AP2025.
     */
    var virkTom: Date? = null

    /**
     * Første virkningstidspunkt,denne må være satt dersom personen er SOKER i persongrunnlaget.
     */
    var brukerForsteVirk: Date? = null

    /**
     * Type ytelse (AP,UP osv)
     */
    var hovedKravlinjeType: KravlinjeTypeEnum? = null

    /**
     * Persongrunnlag for personen.
     * Dersom ytelsesType er UP må uforegrunnlag og uforehistorikk være utfylt.
     */
    var persongrunnlag: Persongrunnlag? = null

    /**
     * Angir om personen har bodd eller arbeidet i utlandet.
     */
    var boddEllerArbeidetIUtlandet = false

    /**
     * Regelverktype bestemmer om trygdetid skal regnes etter gamle eller nye regler.
     */
    var regelverkTypeEnum: RegelverkTypeEnum? = null

    /**
     * Sorterer på nyeste fomDato - denne blir uttaksgradListe.get(0)
     */
    var uttaksgradListe: List<Uttaksgrad> = mutableListOf()
        set(value) {
            field = value.sortedByDescending { it.fomDato }
        }

    var redusertFTTUT: Boolean? = null

    /**
     * Liste av beregningsvilkarPerioder, påkrevd ved uføretrygd.
     */
    var beregningsvilkarPeriodeListe: List<BeregningsvilkarPeriode> = mutableListOf()
}

package no.nav.pensjon.simulator.core.domain.regler.to

import no.nav.pensjon.simulator.core.domain.regler.beregning2011.BeregningsvilkarPeriode
import no.nav.pensjon.simulator.core.domain.regler.enum.KravlinjeTypeEnum
import no.nav.pensjon.simulator.core.domain.regler.enum.RegelverkTypeEnum
import no.nav.pensjon.simulator.core.domain.regler.grunnlag.Persongrunnlag
import no.nav.pensjon.simulator.core.domain.regler.grunnlag.Uttaksgrad
import no.nav.pensjon.simulator.core.domain.regler.kode.KravlinjeTypeCti
import java.util.*

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
    var ytelsesType: KravlinjeTypeCti? = null
    var ytelsesTypeEnum: KravlinjeTypeEnum? = null
        get() {
            return field ?: ytelsesType?.let { KravlinjeTypeEnum.valueOf(it.kode) }
        }
        set(value) {
            field = value
            ytelsesType = value?.let { KravlinjeTypeCti(it.name) }
        }

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

    var uttaksgradListe: List<Uttaksgrad> = mutableListOf()
        set(value) {
            sorterUttaksgradListe()
            field = value
        }

    var redusertFTTUT: Boolean? = null

    /**
     * Liste av beregningsvilkarPerioder, påkrevd ved uføretrygd.
     */
    var beregningsvilkarPeriodeListe: List<BeregningsvilkarPeriode> = mutableListOf()

    /**
     * Sorterer på nyeste fomDato - denne blir uttaksgradListe.get(0)
     */
    private fun sorterUttaksgradListe() {
        Collections.sort(uttaksgradListe, Collections.reverseOrder())
    }
}

package no.nav.pensjon.simulator.core.domain.regler.afpoppgjor

import no.nav.pensjon.simulator.core.domain.regler.grunnlag.AfpTpoUpGrunnlag
import no.nav.pensjon.simulator.core.domain.regler.util.DateCompareUtil
import java.util.*

class AfpUtbetalingsperiode(
    /**
     * Periodens fradato - not null
     */
    var virkFom: Date? = null,

    /**
     * Periodens tildato
     */
    var virkTom: Date? = null,

    /**
     * Brutto månedlig AFP i perioden
     */
    var fullAFP: Int = 0,

    /**
     * Utbetalt månedlig AFP i perioden
     */
    var utbetaltAFP: Int = 0,

    /**
     * Forventet inntekt i perioden
     * (årlig FPI / 12 * antall måneder i perioden)
     */
    var fpi: Int = 0,

    /**
     * Tidligere pensjonsgivende inntekts faktor
     */
    var tpi_faktor: Double = 0.0,

    /**
     * Graden av utbetalt pensjon i forhold til brtto pensjon
     */
    var afpPensjonsgrad: Int = 0,

    /**
     * Objekt som inneholder informasjon om TP-ordningers uførepensjonsgrunnlag. Dette er manuelt registrerte data og ikke hentet fra TP-registeret eller andre eksterne kilder.
     */
    var afpTpoUpGrunnlag: AfpTpoUpGrunnlag? = null,

    /**
     * Flagg som sier om UP-grunnlaget fra TPO er benyttet i beregning. Brukes for å bestemme om UP-grunnlaget skal benyttes i etteroppgjørsberegningen.
     */
    var afpTpoUpGrunnlagAnvendt: Boolean = false
) : Comparable<AfpUtbetalingsperiode> {

    override fun compareTo(other: AfpUtbetalingsperiode): Int {
        return DateCompareUtil.compareTo(virkFom, other.virkFom)
    }
}

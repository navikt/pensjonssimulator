package no.nav.pensjon.simulator.core.afp.offentlig.pre2025

import no.nav.pensjon.simulator.core.domain.regler.grunnlag.AfpHistorikk
import no.nav.pensjon.simulator.core.domain.regler.grunnlag.Persongrunnlag
import no.nav.pensjon.simulator.core.domain.regler.krav.Kravhode
import no.nav.pensjon.simulator.core.legacy.util.DateUtil.findEarliestDateByDay
import no.nav.pensjon.simulator.core.legacy.util.DateUtil.getRelativeDateByDays
import no.nav.pensjon.simulator.core.legacy.util.DateUtil.isAfterByDay
import no.nav.pensjon.simulator.core.util.toDate
import no.nav.pensjon.simulator.core.util.toLocalDate
import no.nav.pensjon.simulator.normalder.NormAlderService
import no.nav.pensjon.simulator.uttak.UttakUtil.uttakDato
import org.springframework.stereotype.Component
import java.time.LocalDate

/**
 * Corresponds to SimulerEndringAvAPCommand (AFP offentlig part) in PEN.
 */
@Component
class Pre2025OffentligAfpEndringBeregner(private val normAlderService: NormAlderService) {

    // SimulerEndringAvAPCommand.beregnAfpOffentlig
    fun beregnAfp(kravhode: Kravhode, foersteUttakDato: LocalDate): Pre2025OffentligAfpResult {
        val soekerGrunnlag: Persongrunnlag = kravhode.hentPersongrunnlagForSoker()

        // Ref. no.nav.domain.pensjon.kjerne.grunnlag.Persongrunnlag.setAfpHistorikkListe in PEN:
        val historikk: AfpHistorikk? = soekerGrunnlag.afpHistorikkListe.firstOrNull()

        if (historikk == null || historikk.virkTom != null) {
            return Pre2025OffentligAfpResult(simuleringResult = null, kravhode = kravhode)
        }

        val normAlderDato: LocalDate? = soekerGrunnlag.fodselsdato.toLocalDate()?.let {
            uttakDato(it, normAlderService.normAlder(it))
        }

        //TODO support LocalDate in findEarliestDateByDay:
        val virkningTom = getRelativeDateByDays(findEarliestDateByDay(normAlderDato!!.toDate(), foersteUttakDato.toDate()), -1)

        // Remove AFP-historikk if calculated virkningTom is before virkFom:
        if (historikk.virkFom?.let { isAfterByDay(virkningTom, it, false) } == true) {
            historikk.virkTom = virkningTom
        } else {
            soekerGrunnlag.afpHistorikkListe = mutableListOf()
        }

        return Pre2025OffentligAfpResult(simuleringResult = null, kravhode = kravhode)
    }
}
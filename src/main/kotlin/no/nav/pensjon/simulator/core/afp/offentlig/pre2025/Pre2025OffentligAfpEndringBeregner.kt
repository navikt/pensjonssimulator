package no.nav.pensjon.simulator.core.afp.offentlig.pre2025

import no.nav.pensjon.simulator.core.domain.regler.grunnlag.AfpHistorikk
import no.nav.pensjon.simulator.core.domain.regler.grunnlag.Persongrunnlag
import no.nav.pensjon.simulator.core.domain.regler.krav.Kravhode
import no.nav.pensjon.simulator.core.legacy.util.DateUtil.findEarliestDateByDay
import no.nav.pensjon.simulator.core.legacy.util.DateUtil.getRelativeDateByDays
import no.nav.pensjon.simulator.core.legacy.util.DateUtil.isAfterByDay
import no.nav.pensjon.simulator.core.util.toNorwegianDateAtNoon
import no.nav.pensjon.simulator.core.util.toNorwegianLocalDate
import no.nav.pensjon.simulator.normalder.NormAlderService
import org.springframework.stereotype.Component
import java.time.LocalDate
import java.util.*

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
            return Pre2025OffentligAfpResult(simuleringResult = null, kravhode)
        }

        val normAlderDato = normAlderService.normAlderDato(soekerGrunnlag.fodselsdato!!.toNorwegianLocalDate())

        //TODO support LocalDate in findEarliestDateByDay:
        val virkningTom: Date = getRelativeDateByDays(
            date = findEarliestDateByDay(
                first = normAlderDato.toNorwegianDateAtNoon(),
                second = foersteUttakDato.toNorwegianDateAtNoon()
            )!!,
            days = -1
        )

        // Remove AFP-historikk if calculated virkningTom is before virkFom:
        if (historikk.virkFom?.let { isAfterByDay(virkningTom, it, false) } == true) {
            historikk.virkTom = virkningTom
        } else {
            soekerGrunnlag.afpHistorikkListe = mutableListOf()
        }

        return Pre2025OffentligAfpResult(simuleringResult = null, kravhode)
    }
}

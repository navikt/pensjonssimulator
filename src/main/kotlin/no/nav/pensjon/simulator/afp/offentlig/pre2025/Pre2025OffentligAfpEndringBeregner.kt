package no.nav.pensjon.simulator.afp.offentlig.pre2025

import no.nav.pensjon.simulator.core.domain.regler.grunnlag.AfpHistorikk
import no.nav.pensjon.simulator.core.domain.regler.grunnlag.Persongrunnlag
import no.nav.pensjon.simulator.core.domain.regler.krav.Kravhode
import no.nav.pensjon.simulator.core.legacy.util.DateUtil.isAfterByDay
import no.nav.pensjon.simulator.normalder.NormertPensjonsalderService
import org.springframework.stereotype.Component
import java.time.LocalDate

/**
 * Beregner "gammel" (pre-2025) offentlig AFP ved endring av alderspensjon.
 * -----------------------------
 * Corresponds to SimulerEndringAvAPCommand ('offentlig AFP' part) in PEN.
 */
@Component
class Pre2025OffentligAfpEndringBeregner(private val normalderService: NormertPensjonsalderService) {

    // PEN: SimulerEndringAvAPCommand.beregnAfpOffentlig
    fun beregnAfp(kravhode: Kravhode, foersteUttakDato: LocalDate): Pre2025OffentligAfpResult {
        val soekerGrunnlag: Persongrunnlag = kravhode.hentPersongrunnlagForSoker()

        // Ref. no.nav.domain.pensjon.kjerne.grunnlag.Persongrunnlag.setAfpHistorikkListe in PEN:
        val historikk: AfpHistorikk? = soekerGrunnlag.afpHistorikkListe.firstOrNull()

        if (historikk == null || historikk.virkTomLd != null) {
            return Pre2025OffentligAfpResult(simuleringResult = null, kravhode)
        }

        val normalderDato: LocalDate =
            normalderService.normalderDato(soekerGrunnlag.fodselsdatoLd!!)

        val virkningTom: LocalDate = earlierOf(normalderDato, foersteUttakDato).minusDays(1)

        // Remove AFP-historikk if calculated virkningTom is before virkFom:
        if (historikk.virkFomLd?.let { isAfterByDay(virkningTom, it, allowSameDay = false) } == true) {
            historikk.virkTomLd = virkningTom
        } else {
            soekerGrunnlag.afpHistorikkListe = mutableListOf()
        }

        return Pre2025OffentligAfpResult(simuleringResult = null, kravhode)
    }

    private companion object {
        fun earlierOf(a: LocalDate, b: LocalDate): LocalDate =
            if (a.isBefore(b)) a else b
    }
}

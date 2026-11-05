package no.nav.pensjon.simulator.afp.offentlig.tidsbegrenset

import no.nav.pensjon.simulator.core.domain.regler.grunnlag.AfpHistorikk
import no.nav.pensjon.simulator.core.domain.regler.grunnlag.Persongrunnlag
import no.nav.pensjon.simulator.core.domain.regler.krav.Kravhode
import no.nav.pensjon.simulator.core.legacy.util.DateUtil.isAfterByDay
import no.nav.pensjon.simulator.normalder.NormertPensjonsalderService
import org.springframework.stereotype.Component
import java.time.LocalDate

/**
 * Beregner tidsbegrenset offentlig AFP ved endring av alderspensjon.
 */
@Component
class TidsbegrensetOffentligAfpEndringBeregner(private val normalderService: NormertPensjonsalderService) {

    // PEN: SimulerEndringAvAPCommand.beregnAfpOffentlig
    fun beregnAfp(kravhode: Kravhode, foersteUttakDato: LocalDate): TidsbegrensetOffentligAfpResult {
        val soekerGrunnlag: Persongrunnlag = kravhode.hentPersongrunnlagForSoker()
        val historikk: AfpHistorikk? = afpHistorikk(soekerGrunnlag)

        if (historikk == null || historikk.virkTomLd != null) {
            return TidsbegrensetOffentligAfpResult(kravhode = kravhode)
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

        return TidsbegrensetOffentligAfpResult(
            registrerteFremtidigePensjonspoeng = afpHistorikk(soekerGrunnlag)?.afpFpp,
            kravhode = kravhode
        )
    }

    /**
     * Det antas at AFP-historikken hentet fra PEN inneholder maks. ett element.
     * (PEN velger elementene som har status "i bruk", og det antas at maks. ett har denne statusen.)
     */
    private fun afpHistorikk(grunnlag: Persongrunnlag): AfpHistorikk? =
        grunnlag.afpHistorikkListe.firstOrNull()

    private companion object {
        fun earlierOf(a: LocalDate, b: LocalDate): LocalDate =
            if (a.isBefore(b)) a else b
    }
}
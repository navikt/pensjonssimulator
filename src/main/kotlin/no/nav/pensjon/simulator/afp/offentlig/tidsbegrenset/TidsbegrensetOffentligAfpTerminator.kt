package no.nav.pensjon.simulator.afp.offentlig.tidsbegrenset

import no.nav.pensjon.simulator.afp.offentlig.tidsbegrenset.TidsbegrensetOffentligAfpFoerstegangBeregner.Companion.AFP_VIRKNING_TOM_ALDER_AAR
import no.nav.pensjon.simulator.core.domain.regler.grunnlag.AfpHistorikk
import no.nav.pensjon.simulator.core.domain.regler.grunnlag.Persongrunnlag
import no.nav.pensjon.simulator.core.domain.regler.krav.Kravhode
import no.nav.pensjon.simulator.core.legacy.util.DateUtil.findEarliestDateByDay
import no.nav.pensjon.simulator.core.legacy.util.DateUtil.firstDayOfMonthAfterUserTurnsGivenAge
import no.nav.pensjon.simulator.core.legacy.util.DateUtil.isAfterByDay
import no.nav.pensjon.simulator.core.util.toNorwegianDateAtNoon
import no.nav.pensjon.simulator.core.util.toNorwegianLocalDate
import java.time.LocalDate
import java.util.*

/**
 * Tidsbegrenset offentlig AFP uten sluttdato (virkning t.o.m.) får satt en sluttdato (termineres).
 * Dette er en betingelse for å kunne starte alderspensjon.
 */
object TidsbegrensetOffentligAfpTerminator {

    // PEN: SimulerFleksibelAPCommand.beregnAfpOffentlig
    //  and SimulerEndringAvAPCommand.beregnAfpOffentlig
    fun terminateTidsbegrensetOffentligAfp(
        kravhode: Kravhode,
        foersteUttakDato: LocalDate?,
    ): TidsbegrensetOffentligAfpResult {
        val persongrunnlag: Persongrunnlag = kravhode.hentPersongrunnlagForSoker()
        val afpHistorikk: AfpHistorikk? = persongrunnlag.afpHistorikkListe.firstOrNull()

        if (afpHistorikk == null || afpHistorikk.virkTomLd != null) {
            return TidsbegrensetOffentligAfpResult(simuleringResult = null, kravhode)
        }

        val alderspensjonFom: Date? = persongrunnlag.fodselsdatoLd?.let {
            firstDayOfMonthAfterUserTurnsGivenAge(foedselsdato = it, alderAar = AFP_VIRKNING_TOM_ALDER_AAR)
        }

        val dagenEtterAfpVirkningTom: LocalDate? =
            findEarliestDateByDay(
                first = alderspensjonFom,
                second = foersteUttakDato?.toNorwegianDateAtNoon()
            )?.toNorwegianLocalDate()

        val virkningTom: LocalDate? = dagenEtterAfpVirkningTom?.minusDays(1)

        // Remove AFP-historikk if calculated virkning-t.o.m. is before virkning-f.o.m.:
        if (afpHistorikk.virkFomLd != null && isAfterByDay(virkningTom, afpHistorikk.virkFomLd, false)) {
            afpHistorikk.virkTomLd = virkningTom
        } else {
            persongrunnlag.afpHistorikkListe = mutableListOf()
        }

        return TidsbegrensetOffentligAfpResult(simuleringResult = null, kravhode)
    }
}
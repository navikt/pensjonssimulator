package no.nav.pensjon.simulator.afp.offentlig.pre2025

import no.nav.pensjon.simulator.afp.offentlig.pre2025.Pre2025OffentligAfpFoerstegangBeregner.Companion.AFP_VIRKNING_TOM_ALDER_AAR
import no.nav.pensjon.simulator.core.domain.regler.grunnlag.*
import no.nav.pensjon.simulator.core.domain.regler.krav.Kravhode
import no.nav.pensjon.simulator.core.legacy.util.DateUtil.findEarliestDateByDay
import no.nav.pensjon.simulator.core.legacy.util.DateUtil.firstDayOfMonthAfterUserTurnsGivenAge
import no.nav.pensjon.simulator.core.legacy.util.DateUtil.getRelativeDateByDays
import no.nav.pensjon.simulator.core.legacy.util.DateUtil.isAfterByDay
import no.nav.pensjon.simulator.core.util.toNorwegianDateAtNoon
import java.time.LocalDate
import java.util.*

/**
 * Pre-2025 offentlig AFP uten sluttdato (virkning t.o.m.) får satt en sluttdato (termineres).
 * Dette er en betingelse for å kunne starte alderspensjon.
 */
object Pre2025OffentligAfpTerminator {

    // PEN: SimulerFleksibelAPCommand.beregnAfpOffentlig
    //  and SimulerEndringAvAPCommand.beregnAfpOffentlig
    fun terminatePre2025OffentligAfp(
        kravhode: Kravhode,
        foersteUttakDato: LocalDate?,
    ): Pre2025OffentligAfpResult {
        val persongrunnlag: Persongrunnlag = kravhode.hentPersongrunnlagForSoker()
        val afpHistorikk: AfpHistorikk? = persongrunnlag.afpHistorikkListe.firstOrNull()

        if (afpHistorikk == null || afpHistorikk.virkTom != null) {
            return Pre2025OffentligAfpResult(simuleringResult = null, kravhode)
        }

        val alderspensjonFom: Date? = persongrunnlag.fodselsdato?.let {
            firstDayOfMonthAfterUserTurnsGivenAge(foedselsdato = it, alderAar = AFP_VIRKNING_TOM_ALDER_AAR)
        }

        val dagenEtterAfpVirkningTom: Date? =
            findEarliestDateByDay(
                first = alderspensjonFom,
                second = foersteUttakDato?.toNorwegianDateAtNoon()
            )

        val virkningTom: Date? = dagenEtterAfpVirkningTom?.let { getRelativeDateByDays(date = it, days = -1) }

        // Remove AFP-historikk if calculated virkning-t.o.m. is before virkning-f.o.m.:
        if (afpHistorikk.virkFom != null && isAfterByDay(virkningTom, afpHistorikk.virkFom, false)) {
            afpHistorikk.virkTom = virkningTom
        } else {
            persongrunnlag.afpHistorikkListe = mutableListOf()
        }

        return Pre2025OffentligAfpResult(simuleringResult = null, kravhode)
    }
}

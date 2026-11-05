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

//TODO Slå sammen med TidsbegrensetOffentligAfpEndringBeregner ?
/**
 * Tidsbegrenset offentlig AFP uten sluttdato (virkning t.o.m.) får satt en sluttdato (termineres).
 * Dette er en betingelse for å kunne starte alderspensjon.
 */
object TidsbegrensetOffentligAfpTerminator {

    // PEN: SimulerFleksibelAPCommand.beregnAfpOffentlig
    //  and SimulerEndringAvAPCommand.beregnAfpOffentlig
    fun terminateTidsbegrensetOffentligAfp(
        kravhode: Kravhode,
        foersteUttakDato: LocalDate?
    ): TidsbegrensetOffentligAfpResult {
        val soekerGrunnlag: Persongrunnlag = kravhode.hentPersongrunnlagForSoker()
        val historikk: AfpHistorikk? = afpHistorikk(soekerGrunnlag)

        if (historikk == null || historikk.virkTomLd != null) {
            return TidsbegrensetOffentligAfpResult(kravhode = kravhode)
        }

        val alderspensjonFom: Date? = soekerGrunnlag.fodselsdatoLd?.let {
            firstDayOfMonthAfterUserTurnsGivenAge(foedselsdato = it, alderAar = AFP_VIRKNING_TOM_ALDER_AAR)
        }

        val dagenEtterAfpVirkningTom: LocalDate? =
            findEarliestDateByDay(
                first = alderspensjonFom,
                second = foersteUttakDato?.toNorwegianDateAtNoon()
            )?.toNorwegianLocalDate()

        val virkningTom: LocalDate? = dagenEtterAfpVirkningTom?.minusDays(1)

        // Remove AFP-historikk if calculated virkning-t.o.m. is before virkning-f.o.m.:
        if (historikk.virkFomLd != null && isAfterByDay(virkningTom, historikk.virkFomLd, false)) {
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
}
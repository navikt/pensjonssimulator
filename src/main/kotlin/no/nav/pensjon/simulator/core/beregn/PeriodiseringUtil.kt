package no.nav.pensjon.simulator.core.beregn

import no.nav.pensjon.simulator.core.domain.SakType
import no.nav.pensjon.simulator.core.domain.regler.enum.KravlinjeTypeEnum
import no.nav.pensjon.simulator.core.domain.regler.grunnlag.Pensjonsbeholdning
import no.nav.pensjon.simulator.core.domain.regler.grunnlag.Uttaksgrad
import no.nav.pensjon.simulator.core.domain.regler.krav.Kravhode
import no.nav.pensjon.simulator.core.legacy.util.DateUtil.isBeforeByDay
import no.nav.pensjon.simulator.core.util.toNorwegianDateAtNoon
import java.time.LocalDate

// Extracts from VilkarsprovOgBeregnAlderHelper + SimpleFpenService
object PeriodiseringUtil {

    // VilkarsprovOgBeregnAlderHelper.periodiserGrunnlagAndModifyKravhode
    fun periodiserGrunnlagAndModifyKravhode(
        virkFom: LocalDate,
        kravhode: Kravhode,
        beholdningListe: List<Pensjonsbeholdning>,
        sakType: SakType?
    ): Kravhode =
        periodiserGrunnlag(virkFom, kravhode, sakType).also {
            retainBeholdningerUpToVirkFom(it, beholdningListe, virkFom)
            setLivsvarigFulltUttak(it, virkFom)
            removeNonAlderspensjonKravlinjer(it)
        }

    // SimpleFpenService.periodiserGrunnlag
    fun periodiserGrunnlag(virkFom: LocalDate, kravhode: Kravhode, sakType: SakType?): Kravhode =
        BehandlingPeriodeUtil.periodiserGrunnlag(
            virkningFom = virkFom,
            virkningTom = null,
            originalKravhode = kravhode,
            periodiserFomTomDatoUtenUnntak = true,
            sakType = sakType
        )

    // VilkarsprovOgBeregnAlderHelper.clearBeholdningslisteOnKravhodeAndAddNewBeholdningerWithFomBeforeOrSameAsVirkDatoFom
    private fun retainBeholdningerUpToVirkFom(
        kravhode: Kravhode,
        beholdningListe: List<Pensjonsbeholdning>,
        virkFom: LocalDate
    ) {
        val retainedBeholdninger = beholdningListe.filter {
            isBeforeByDay(it.fom, virkFom, true)
        }

        kravhode.hentPersongrunnlagForSoker().also {
            it.clearBeholdningListe()
            it.addBeholdninger(retainedBeholdninger)
        }
    }

    // VilkarsprovOgBeregnAlderHelper.setUttaksgradListeOnKravhodeWithFullUttakAndVirkDatoFom
    private fun setLivsvarigFulltUttak(kravhode: Kravhode, virkningFom: LocalDate) {
        val uttaksgrad = Uttaksgrad().apply {
            fomDato = virkningFom.toNorwegianDateAtNoon()
            tomDato = null
            uttaksgrad = 100
            // uttaksgradKopiert = false <----- seemingly unused in simulering context
        }.also { it.finishInit() }

        kravhode.uttaksgradListe = mutableListOf(uttaksgrad)
    }

    // VilkarsprovOgBeregnAlderHelper.removeAllNonAPKravlinjerFromKravhode
    private fun removeNonAlderspensjonKravlinjer(kravhode: Kravhode) {
        kravhode.kravlinjeListe.removeIf { it.kravlinjeTypeEnum != KravlinjeTypeEnum.AP }
    }
}

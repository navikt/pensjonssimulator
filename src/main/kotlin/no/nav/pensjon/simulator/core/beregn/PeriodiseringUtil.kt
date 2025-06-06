package no.nav.pensjon.simulator.core.beregn

import no.nav.pensjon.simulator.core.domain.regler.enum.KravlinjeTypeEnum
import no.nav.pensjon.simulator.core.domain.regler.enum.SakTypeEnum
import no.nav.pensjon.simulator.core.domain.regler.grunnlag.Pensjonsbeholdning
import no.nav.pensjon.simulator.core.domain.regler.grunnlag.Uttaksgrad
import no.nav.pensjon.simulator.core.domain.regler.krav.Kravhode
import no.nav.pensjon.simulator.core.legacy.util.DateUtil.isBeforeByDay
import no.nav.pensjon.simulator.core.util.toNorwegianDateAtNoon
import java.time.LocalDate

// Extracts from VilkarsprovOgBeregnAlderHelper + SimpleFpenService in PEN
object PeriodiseringUtil {

    // PEN: VilkarsprovOgBeregnAlderHelper.periodiserGrunnlagAndModifyKravhode
    fun periodiserGrunnlagAndModifyKravhode(
        virkningFom: LocalDate,
        kravhode: Kravhode,
        beholdningListe: List<Pensjonsbeholdning>,
        sakType: SakTypeEnum?
    ): Kravhode =
        periodiserGrunnlag(virkningFom, kravhode, sakType).also {
            retainBeholdningerFramTilDato(it, beholdningListe, virkningFom)
            setLivsvarigFulltUttak(it, virkningFom)
            removeNonAlderspensjonKravlinjer(it)
        }

    // SimpleFpenService.periodiserGrunnlag
    fun periodiserGrunnlag(virkningFom: LocalDate, kravhode: Kravhode, sakType: SakTypeEnum?): Kravhode =
        BehandlingPeriodeUtil.periodiserGrunnlag(
            virkningFom,
            virkningTom = null,
            originalKravhode = kravhode,
            periodiserFomTomDatoUtenUnntak = true,
            sakType
        )

    // VilkarsprovOgBeregnAlderHelper.clearBeholdningslisteOnKravhodeAndAddNewBeholdningerWithFomBeforeOrSameAsVirkDatoFom
    private fun retainBeholdningerFramTilDato(
        kravhode: Kravhode,
        beholdningListe: List<Pensjonsbeholdning>,
        dato: LocalDate
    ) {
        val retainedBeholdninger = beholdningListe.filter {
            isBeforeByDay(it.fom, dato, allowSameDay = true)
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
        }

        kravhode.uttaksgradListe = mutableListOf(uttaksgrad)
    }

    // VilkarsprovOgBeregnAlderHelper.removeAllNonAPKravlinjerFromKravhode
    private fun removeNonAlderspensjonKravlinjer(kravhode: Kravhode) {
        kravhode.kravlinjeListe.removeIf { it.kravlinjeTypeEnum != KravlinjeTypeEnum.AP }
    }
}

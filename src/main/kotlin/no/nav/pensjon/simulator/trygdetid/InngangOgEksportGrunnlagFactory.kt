package no.nav.pensjon.simulator.trygdetid

import no.nav.pensjon.simulator.core.domain.regler.TTPeriode
import no.nav.pensjon.simulator.core.domain.regler.enum.LandkodeEnum
import no.nav.pensjon.simulator.core.domain.regler.enum.RegelverkTypeEnum
import no.nav.pensjon.simulator.core.domain.regler.grunnlag.InngangOgEksportGrunnlag
import no.nav.pensjon.simulator.core.domain.regler.grunnlag.Persongrunnlag
import no.nav.pensjon.simulator.core.krav.isAlderspensjon2011
import no.nav.pensjon.simulator.core.krav.isAlderspensjon2016
import no.nav.pensjon.simulator.core.krav.isAlderspensjon2025
import no.nav.pensjon.simulator.core.util.toNorwegianDateAtNoon

// PEN:
// no.nav.service.pensjon.simulering.support.command.simulerendringavap.utenlandsopphold.InngangOgEksportGrunnlagFactory
object InngangOgEksportGrunnlagFactory {

    private const val MILLISEKUNDER_PER_AAR = 31536000000L // 365 * 24 * 60 * 60 * 1000
    private const val MINIMUM_TRYGDETID_ANTALL_AAR = 1

    fun newInngangOgEksportGrunnlagForSimuleringUtland(persongrunnlag: Persongrunnlag, regelverkType: RegelverkTypeEnum) =
        InngangOgEksportGrunnlag().apply {
            eksportforbud = null
            fortsattMedlemFT = true

            if (regelverkType.isAlderspensjon2011) {
                val antallArTrygdetidKapittel19 = trygdetidAntallAar(persongrunnlag.trygdetidPerioder)
                treArTrygdetidNorge = antallArTrygdetidKapittel19 >= MINIMUM_TRYGDETID_ANTALL_AAR
            }

            if (regelverkType.isAlderspensjon2016) {
                val antallArTrygdetidKapittel19 = trygdetidAntallAar(persongrunnlag.trygdetidPerioder)
                val antallArTrygdetidKapittel20 = trygdetidAntallAar(persongrunnlag.trygdetidPerioderKapittel20)
                treArTrygdetidNorge = antallArTrygdetidKapittel19 >= MINIMUM_TRYGDETID_ANTALL_AAR
                treArTrygdetidNorgeKap20 = antallArTrygdetidKapittel20 >= MINIMUM_TRYGDETID_ANTALL_AAR
            }

            if (regelverkType.isAlderspensjon2025) {
                val antallArTrygdetidKapittel20 = trygdetidAntallAar(persongrunnlag.trygdetidPerioderKapittel20)
                treArTrygdetidNorgeKap20 = antallArTrygdetidKapittel20 >= MINIMUM_TRYGDETID_ANTALL_AAR
            }
        }

    private fun trygdetidAntallAar(periodeListe: List<TTPeriode>) =
        (trygetidMillisekunder(periodeListe) / MILLISEKUNDER_PER_AAR).toFloat()

    private fun trygetidMillisekunder(periodeListe: List<TTPeriode>) =
        periodeListe
            .filter { it.landEnum == LandkodeEnum.NOR }
            .sumOf { it.tomLd!!.toNorwegianDateAtNoon().time - it.fomLd!!.toNorwegianDateAtNoon().time }
}

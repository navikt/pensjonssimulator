package no.nav.pensjon.simulator.core.trygd

import no.nav.pensjon.simulator.core.domain.regler.TTPeriode
import no.nav.pensjon.simulator.core.domain.regler.enum.LandkodeEnum
import no.nav.pensjon.simulator.core.domain.regler.grunnlag.InngangOgEksportGrunnlag
import no.nav.pensjon.simulator.core.domain.regler.grunnlag.Persongrunnlag
import no.nav.pensjon.simulator.core.domain.regler.krav.Kravhode
import no.nav.pensjon.simulator.core.krav.isAlderspensjon2011
import no.nav.pensjon.simulator.core.krav.isAlderspensjon2016
import no.nav.pensjon.simulator.core.krav.isAlderspensjon2025

// no.nav.service.pensjon.simulering.support.command.simulerendringavap.utenlandsopphold.InngangOgEksportGrunnlagFactory
object InngangOgEksportGrunnlagFactory {

    private const val MILLISEKUNDER_PER_AR = 31536000000L // 365 * 24 * 60 * 60 * 1000
    private const val MINIMUM_TRYGDETID_ANTALL_AR = 1

    fun newInngangOgEksportGrunnlagForSimuleringUtland(persongrunnlag: Persongrunnlag, kravhode: Kravhode) =
        InngangOgEksportGrunnlag().apply {
            eksportforbud = null
            fortsattMedlemFT = true
            val regelverkType = kravhode.regelverkTypeEnum

            if (regelverkType?.isAlderspensjon2011 == true) {
                val antallArTrygdetidKapittel19 = trygdetidAntallAar(persongrunnlag.trygdetidPerioder)
                treArTrygdetidNorge = antallArTrygdetidKapittel19 >= MINIMUM_TRYGDETID_ANTALL_AR
            }

            if (regelverkType?.isAlderspensjon2016 == true) {
                val antallArTrygdetidKapittel19 = trygdetidAntallAar(persongrunnlag.trygdetidPerioder)
                val antallArTrygdetidKapittel20 = trygdetidAntallAar(persongrunnlag.trygdetidPerioderKapittel20)
                treArTrygdetidNorge = antallArTrygdetidKapittel19 >= MINIMUM_TRYGDETID_ANTALL_AR
                treArTrygdetidNorgeKap20 = antallArTrygdetidKapittel20 >= MINIMUM_TRYGDETID_ANTALL_AR
            }

            if (regelverkType?.isAlderspensjon2025 == true) {
                val antallArTrygdetidKapittel20 = trygdetidAntallAar(persongrunnlag.trygdetidPerioderKapittel20)
                treArTrygdetidNorgeKap20 = antallArTrygdetidKapittel20 >= MINIMUM_TRYGDETID_ANTALL_AR
            }
        }

    private fun trygdetidAntallAar(periodeListe: List<TTPeriode>) =
        (trygetidMillisekunder(periodeListe) / MILLISEKUNDER_PER_AR).toFloat()

    private fun trygetidMillisekunder(periodeListe: List<TTPeriode>) =
        periodeListe
            .filter { it.land!!.kode == LandkodeEnum.NOR.name }
            .sumOf { it.tom!!.time - it.fom!!.time }
}

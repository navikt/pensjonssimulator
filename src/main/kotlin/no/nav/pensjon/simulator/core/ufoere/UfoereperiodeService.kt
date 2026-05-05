package no.nav.pensjon.simulator.core.ufoere

import no.nav.pensjon.simulator.core.domain.regler.enum.SimuleringTypeEnum
import no.nav.pensjon.simulator.core.domain.regler.grunnlag.Persongrunnlag
import no.nav.pensjon.simulator.core.krav.UttakGradKode
import no.nav.pensjon.simulator.core.spec.SimuleringSpec
import no.nav.pensjon.simulator.normalder.NormertPensjonsalderService
import org.springframework.stereotype.Service
import java.time.LocalDate

@Service
class UfoereperiodeService(
    private val normalderService: NormertPensjonsalderService
) {
    fun ufoereperiodeTom(spec: SimuleringSpec, persongrunnlag: Persongrunnlag): LocalDate {
        val dagenFoerUbetingetUttaksdato: LocalDate =
            normalderService.normalderOppnaasDato(foedselsdato = persongrunnlag.fodselsdatoLd!!)
                .plusMonths(1)
                .withDayOfMonth(1)
                .minusDays(1)

        val uttakFom: LocalDate = spec.foersteUttakDato!!
        val tidligUttak: Boolean = uttakFom.isBefore(dagenFoerUbetingetUttaksdato)

        return if (tidligUttak && skalTerminereUfoereperiodeVedUttaksdato(spec))
            uttakFom.minusDays(1)
        else
            dagenFoerUbetingetUttaksdato
    }

    /**
     * Uføretrygd må avsluttes før uttak av pensjon i følgende tilfeller:
     * - Ved uttak (helt eller gradert) av alderspensjon i kombinasjon med privat AFP
     * - Ved helt uttak av alderspensjon
     */
    private fun skalTerminereUfoereperiodeVedUttaksdato(spec: SimuleringSpec): Boolean =
        spec.type == SimuleringTypeEnum.ALDER_M_AFP_PRIVAT ||
                setOf(
                    SimuleringTypeEnum.ALDER,
                    SimuleringTypeEnum.ALDER_M_GJEN
                ).contains(spec.type) && spec.uttakGrad == UttakGradKode.P_100

}

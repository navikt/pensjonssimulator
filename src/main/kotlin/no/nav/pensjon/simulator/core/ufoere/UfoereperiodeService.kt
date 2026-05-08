package no.nav.pensjon.simulator.core.ufoere

import no.nav.pensjon.simulator.core.domain.regler.grunnlag.Persongrunnlag
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

        return if (tidligUttak && spec.kreverAvsluttetUfoeretrygd)
            uttakFom.minusDays(1)
        else
            dagenFoerUbetingetUttaksdato
    }
}

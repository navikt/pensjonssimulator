package no.nav.pensjon.simulator.alderspensjon.api.samhandler.acl.v3

import no.nav.pensjon.simulator.afp.privat.PrivatAfpPeriode
import no.nav.pensjon.simulator.core.domain.regler.grunnlag.Uttaksgrad
import no.nav.pensjon.simulator.core.result.PensjonPeriode
import no.nav.pensjon.simulator.core.result.SimulatorOutput
import no.nav.pensjon.simulator.core.result.SimulertBeregningInformasjon
import no.nav.pensjon.simulator.person.GeneralPersonService
import no.nav.pensjon.simulator.person.Pid
import no.nav.pensjon.simulator.tech.time.Time
import org.springframework.stereotype.Component
import java.time.LocalDate

/**
 * PEN: SimulerAlderspensjonResponseV3Converter
 * + SimulertFleksibelAlderspensjonPeriodeConverter, UttaksgradPeriodeConverter
 */
@Component
class AlderspensjonOgPrivatAfpResultMapperV3(
    private val personService: GeneralPersonService,
    private val time: Time
) {
    fun map(simuleringResult: SimulatorOutput, pid: Pid): AlderspensjonOgPrivatAfpResultV3 {
        val alderspensjon = simuleringResult.alderspensjon
        val foedselsdato = personService.foedselsdato(pid)
        val idag = time.today()

        // NB: uttakGradListe is taken from spec (ref. SimuleringResultPreparer line 325):
        val uttakListe: List<Uttaksgrad> = alderspensjon?.uttakGradListe.orEmpty()

        val harUttak = uttakListe.any { it.tasUt(dato = idag) }

        return AlderspensjonOgPrivatAfpResultV3(
            // NB: In V3 for TPO simulertBeregningInformasjonListe is taken from alderspensjon not pensjonPeriodeListe:
            alderspensjonsperioder = alderspensjon?.pensjonPeriodeListe.orEmpty()
                .map { pensjonsperiode(source = it, foedselsdato) },
            privatAfpPerioder = simuleringResult.privatAfpPeriodeListe.map(::privatAfpPeriode),
            harUttak,
            harTidligereUttak = harUttak.not() && harHattUttakFoer(uttakListe, dato = idag)
        )
    }

    private companion object {

        private fun privatAfpPeriode(source: PrivatAfpPeriode) =
            PrivatAfpPeriodeResultV3(
                belop = source.aarligBeloep ?: 0,
                alder = source.alderAar ?: 0
            )

        private fun pensjonsperiode(source: PensjonPeriode, foedselsdato: LocalDate) =
            AlderspensjonsperiodeResultV3(
                arligUtbetaling = source.beloep ?: 0,
                datoFom = source.alderAar?.let {
                    foersteDagMaanedenEtterBursdag(foedselsdato, alderAar = it).toString()
                } ?: "",
                alder = source.alderAar ?: 0,
                uttaksgradPeriode = source.simulertBeregningInformasjonListe.map(::uttak)
            )

        private fun uttak(source: SimulertBeregningInformasjon) =
            UttaksperiodeResultV3(
                startmaned = source.startMaaned ?: 0,
                uttaksgrad = source.uttakGrad?.toInt() ?: 0
            )

        private fun harHattUttakFoer(uttakListe: List<Uttaksgrad>, dato: LocalDate): Boolean =
            uttakListe.none { it.tasUt(dato) } && uttakListe.any { it.tattUtFoer(dato) }

        private fun foersteDagMaanedenEtterBursdag(foedselsdato: LocalDate, alderAar: Int): LocalDate =
            foedselsdato
                .plusYears(alderAar.toLong())
                .plusMonths(1)
                .withDayOfMonth(1)
    }
}
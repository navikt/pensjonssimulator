package no.nav.pensjon.simulator.hybrid

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
import no.nav.pensjon.simulator.uttak.Uttaksgrad as UttaksgradEnum

@Component
class AlderspensjonOgPrivatAfpResultPreparer(
    private val personService: GeneralPersonService,
    private val time: Time
) {
    fun result(
        simulatorOutput: SimulatorOutput,
        pid: Pid,
        harLoependePrivatAfp: Boolean
    ): AlderspensjonOgPrivatAfpResult {
        val alderspensjon = simulatorOutput.alderspensjon
        val foedselsdato = personService.foedselsdato(pid)
        val idag = time.today()

        // NB: uttakGradListe is taken from spec (ref. SimuleringResultPreparer line 325):
        val uttakListe: List<Uttaksgrad> = alderspensjon?.uttakGradListe.orEmpty()

        val harNaavaerendeUttak = uttakListe.any { it.tasUt(dato = idag) }

        return AlderspensjonOgPrivatAfpResult(
            suksess = true,
            alderspensjonsperiodeListe = alderspensjon?.pensjonPeriodeListe.orEmpty()
                .map { pensjonsperiode(source = it, foedselsdato) },
            privatAfpPeriodeListe = simulatorOutput.privatAfpPeriodeListe.map(::privatAfpPeriode),
            harNaavaerendeUttak,
            harTidligereUttak = harNaavaerendeUttak.not() && harHattUttakFoer(uttakListe, dato = idag),
            harLoependePrivatAfp
        )
    }

    private companion object {

        private fun privatAfpPeriode(source: PrivatAfpPeriode) =
            PrivatAfpPeriode(
                alderAar = source.alderAar ?: 0,
                beloep = source.aarligBeloep ?: 0
            )

        private fun pensjonsperiode(source: PensjonPeriode, foedselsdato: LocalDate) =
            Alderspensjonsperiode(
                alderAar = source.alderAar ?: 0,
                beloep = source.beloep ?: 0,
                fom = source.alderAar?.let {
                    foersteDagMaanedenEtterBursdag(foedselsdato, alderAar = it).toString()
                } ?: "",
                uttaksperiodeListe = source.simulertBeregningInformasjonListe.map(::uttak)
            )

        private fun uttak(source: SimulertBeregningInformasjon) =
            Uttaksperiode(
                startmaaned = source.startMaaned ?: 0,
                uttaksgrad = UttaksgradEnum.from(source.uttakGrad?.toInt() ?: 0)
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
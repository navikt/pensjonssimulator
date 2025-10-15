package no.nav.pensjon.simulator.hybrid.api.samhandler.acl.v3

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
 * Anti-corruption layer (ACL).
 * Maps from the 'free' internal domain object to version 3 of the externally constrained data transfer object.
 * The object represents a result of the 'simuler alderspensjon & privat AFP' service.
 * ----------
 * PEN: SimulerAlderspensjonResponseV3Converter
 *    + SimulertFleksibelAlderspensjonPeriodeConverter, UttaksgradPeriodeConverter
 */
@Component
class AlderspensjonOgPrivatAfpResultMapperV3(
    private val personService: GeneralPersonService,
    private val time: Time
) {
    /**
     * Takes a result in the form of a domain object and maps it to a data transfer object (DTO).
     */
    fun toDto(simuleringResult: SimulatorOutput, pid: Pid): AlderspensjonOgPrivatAfpResultV3 {
        val alderspensjon = simuleringResult.alderspensjon
        val foedselsdato = personService.foedselsdato(pid)
        val idag = time.today()

        // NB: uttakGradListe is taken from spec (ref. SimuleringResultPreparer line 325):
        val uttakListe: List<Uttaksgrad> = alderspensjon?.uttakGradListe.orEmpty()

        val harNaavaerendeUttak = uttakListe.any { it.tasUt(dato = idag) }

        return AlderspensjonOgPrivatAfpResultV3(
            // NB: In V3 for TPO simulertBeregningInformasjonListe is taken from alderspensjon not pensjonPeriodeListe:
            alderspensjonsperioder = alderspensjon?.pensjonPeriodeListe.orEmpty()
                .map { pensjonsperiode(source = it, foedselsdato) },
            privatAfpPerioder = simuleringResult.privatAfpPeriodeListe.map(::privatAfpPeriode),
            harNaavaerendeUttak,
            harTidligereUttak = harNaavaerendeUttak.not() && harHattUttakFoer(uttakListe, dato = idag)
        )
    }

    private companion object {

        private fun privatAfpPeriode(source: PrivatAfpPeriode) =
            ApOgPrivatAfpPrivatAfpPeriodeResultV3(
                alder = source.alderAar ?: 0,
                beloep = source.aarligBeloep ?: 0
            )

        private fun pensjonsperiode(source: PensjonPeriode, foedselsdato: LocalDate) =
            ApOgPrivatAfpAlderspensjonsperiodeResultV3(
                alder = source.alderAar ?: 0,
                beloep = source.beloep ?: 0,
                datoFom = source.alderAar?.let {
                    foersteDagMaanedenEtterBursdag(foedselsdato, alderAar = it).toString()
                } ?: "",
                uttaksperiode = source.simulertBeregningInformasjonListe.map(::uttak)
            )

        private fun uttak(source: SimulertBeregningInformasjon) =
            ApOgPrivatAfpUttaksperiodeResultV3(
                startmaaned = source.startMaaned ?: 0,
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
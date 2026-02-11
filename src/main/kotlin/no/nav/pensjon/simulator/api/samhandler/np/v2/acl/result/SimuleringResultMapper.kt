package no.nav.pensjon.simulator.api.samhandler.np.v2.acl.result

import no.nav.pensjon.simulator.orch.AlderspensjonOgPrivatAfpResult
import no.nav.pensjon.simulator.orch.Alderspensjonsperiode
import no.nav.pensjon.simulator.orch.PrivatAfpPeriode
import no.nav.pensjon.simulator.orch.Uttaksperiode
import no.nav.pensjon.simulator.validity.Problem

/**
 * Anti-corruption layer (ACL).
 * Maps from the 'free' internal domain object to the externally constrained data transfer object.
 * The object represents a result of the 'simuler alderspensjon & privat AFP' service.
 */
object SimuleringResultMapper {
    /**
     * Takes a result in the form of a domain object and maps it to a data transfer object (DTO).
     */
    fun toDto(source: AlderspensjonOgPrivatAfpResult) =
        SimuleringResultDto(
            suksess = source.suksess,
            alderspensjonsperioder = source.alderspensjonsperiodeListe.map(::pensjonsperiode),
            privatAfpPerioder = source.privatAfpPeriodeListe.map(::privatAfpPeriode),
            harNaavaerendeUttak = source.harNaavaerendeUttak,
            harTidligereUttak = source.harTidligereUttak,
            harLoependePrivatAfp = source.harLoependePrivatAfp,
            problem = source.problem?.let(::problem)
        )

    private fun privatAfpPeriode(source: PrivatAfpPeriode) =
        ApOgPrivatAfpPrivatAfpPeriodeResultDto(
            alder = source.alderAar,
            beloep = source.beloep
        )

    private fun pensjonsperiode(source: Alderspensjonsperiode) =
        ApOgPrivatAfpAlderspensjonsperiodeResultDto(
            alder = source.alderAar,
            beloep = source.beloep,
            datoFom = source.fom,
            uttaksperiode = source.uttaksperiodeListe.map(::uttak)
        )

    private fun uttak(source: Uttaksperiode) =
        ApOgPrivatAfpUttaksperiodeResultDto(
            startmaaned = source.startmaaned,
            uttaksgrad = source.uttaksgrad.prosentsats
        )

    private fun problem(source: Problem) =
        ProblemDto(
            kode = ProblemTypeDto.entries.firstOrNull { it.internalValue == source.type } ?: ProblemTypeDto.SERVERFEIL,
            beskrivelse = source.beskrivelse
        )
}

package no.nav.pensjon.simulator.hybrid.api.samhandler.acl.v3

import no.nav.pensjon.simulator.hybrid.*

/**
 * Anti-corruption layer (ACL).
 * Maps from the 'free' internal domain object to version 3 of the externally constrained data transfer object.
 * The object represents a result of the 'simuler alderspensjon & privat AFP' service.
 */
object AlderspensjonOgPrivatAfpResultMapperV3 {
    /**
     * Takes a result in the form of a domain object and maps it to a data transfer object (DTO).
     */
    fun toDto(source: AlderspensjonOgPrivatAfpResult) =
        AlderspensjonOgPrivatAfpResultV3(
            suksess = source.suksess,
            alderspensjonsperioder = source.alderspensjonsperiodeListe.map(::pensjonsperiode),
            privatAfpPerioder = source.privatAfpPeriodeListe.map(::privatAfpPeriode),
            harNaavaerendeUttak = source.harNaavaerendeUttak,
            harTidligereUttak = source.harTidligereUttak,
            harLoependePrivatAfp = source.harLoependePrivatAfp,
            problem = source.problem?.let(::problem)
        )

    private fun privatAfpPeriode(source: PrivatAfpPeriode) =
        ApOgPrivatAfpPrivatAfpPeriodeResultV3(
            alder = source.alderAar,
            beloep = source.beloep
        )

    private fun pensjonsperiode(source: Alderspensjonsperiode) =
        ApOgPrivatAfpAlderspensjonsperiodeResultV3(
            alder = source.alderAar,
            beloep = source.beloep,
            datoFom = source.fom,
            uttaksperiode = source.uttaksperiodeListe.map(::uttak)
        )

    private fun uttak(source: Uttaksperiode) =
        ApOgPrivatAfpUttaksperiodeResultV3(
            startmaaned = source.startmaaned,
            uttaksgrad = source.uttaksgrad.prosentsats
        )

    private fun problem(source: Problem) =
        ProblemV3(
            kode = ProblemTypeV3.valueOf(source.type.name),
            beskrivelse = source.beskrivelse
        )
}

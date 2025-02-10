package no.nav.pensjon.simulator.alderspensjon.api.tpo.direct.acl.v4

import no.nav.pensjon.simulator.alderspensjon.spec.AlderspensjonSpec
import no.nav.pensjon.simulator.alderspensjon.spec.GradertUttakSpec
import no.nav.pensjon.simulator.alderspensjon.spec.PensjonInntektSpec
import no.nav.pensjon.simulator.core.krav.UttakGradKode
import no.nav.pensjon.simulator.person.Pid
import no.nav.pensjon.simulator.tech.web.BadRequestException
import java.time.LocalDate

/**
 * Maps from V4 data transfer objects (received from the API)
 * to domain objects that represent specification for 'simuler alderspensjon'
 */
object AlderspensjonSpecMapperV4 {

    fun fromDto(source: AlderspensjonSpecV4) =
        AlderspensjonSpec(
            pid = source.personId?.let(::Pid) ?: missing("personId"),
            gradertUttak = source.gradertUttak?.let(::gradertUttak),
            heltUttakFom = source.heltUttakFraOgMedDato?.let(LocalDate::parse) ?: missing("heltUttakFraOgMedDato"),
            antallAarUtenlandsEtter16 = source.aarIUtlandetEtter16 ?: 0,
            epsHarPensjon = source.epsPensjon == true,
            epsHarInntektOver2G = source.eps2G == true,
            fremtidigInntektListe = source.fremtidigInntektListe.orEmpty().map(::inntekt),
            livsvarigOffentligAfpRettFom = source.rettTilAfpOffentligDato?.let(LocalDate::parse)
        )

    @OptIn(ExperimentalStdlibApi::class)
    private fun gradertUttak(source: GradertUttakSpecV4) =
        GradertUttakSpec(
            fom = source.fraOgMedDato?.let(LocalDate::parse) ?: missing("gradertUttak.fraOgMedDato"),
            uttaksgrad = UttakGradKode.entries.firstOrNull { it.value.toInt() == source.uttaksgrad }
                ?: UttakGradKode.P_100
        )

    private fun inntekt(source: PensjonInntektSpecV4) =
        PensjonInntektSpec(
            aarligBeloep = source.aarligInntekt ?: 0,
            fom = source.fraOgMedDato?.let(LocalDate::parse) ?: missing("fremtidigInntektListe[x].fraOgMedDato")
        )

    private fun missing(valueName: String): Nothing {
        throw BadRequestException("$valueName missing")
    }
}

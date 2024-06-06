package no.nav.pensjon.simulator.alderspensjon.api.acl

import no.nav.pensjon.simulator.alderspensjon.AlderspensjonSpec
import no.nav.pensjon.simulator.alderspensjon.GradertUttakSpec
import no.nav.pensjon.simulator.alderspensjon.InntektSpec
import no.nav.pensjon.simulator.alderspensjon.Uttaksgrad
import no.nav.pensjon.simulator.person.Pid
import no.nav.pensjon.simulator.tech.web.BadRequestException
import org.springframework.util.StringUtils.hasLength
import java.time.LocalDate

object AlderspensjonSpecMapperV4 {

    fun fromSpecV4(source: AlderspensjonSpecV4) =
        AlderspensjonSpec(
            pid = source.personId?.let(::pid) ?: missing("personId"),
            gradertUttak = source.gradertUttak?.let(::gradertUttak),
            heltUttakFom = source.heltUttakFraOgMedDato?.let(LocalDate::parse) ?: missing("heltUttakFraOgMedDato"),
            antallAarUtenlandsEtter16Aar = source.arIUtlandetEtter16 ?: 0,
            epsHarPensjon = source.epsPensjon ?: false,
            epsHarInntektOver2G = source.eps2G ?: false,
            fremtidigInntektListe = source.fremtidigInntektListe.orEmpty().map(::inntekt),
            rettTilAfpOffentligDato = source.rettTilAfpOffentligDato?.let(LocalDate::parse)
        )

    private fun pid(value: String) = if (hasLength(value)) Pid(value) else missing("personId")

    private fun gradertUttak(source: GradertUttakV4) =
        GradertUttakSpec(
            uttaksgrad = Uttaksgrad.from(source.uttaksgrad),
            fom = source.fraOgMedDato?.let(LocalDate::parse) ?: missing("gradertUttak.fraOgMedDato")
        )

    private fun inntekt(source: PensjonInntektSpecV4) =
        InntektSpec(
            aarligBeloep = source.arligInntekt ?: 0,
            fom = source.fraOgMedDato?.let(LocalDate::parse) ?: missing("fremtidigInntekt.fraOgMedDato")
        )

    private fun <T> missing(valueName: String): T {
        throw BadRequestException("$valueName missing")
    }
}

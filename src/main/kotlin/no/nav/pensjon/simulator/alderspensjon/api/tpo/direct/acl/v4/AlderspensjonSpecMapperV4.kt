package no.nav.pensjon.simulator.alderspensjon.api.tpo.direct.acl.v4

import no.nav.pensjon.simulator.alderspensjon.api.tpo.viapen.AlderspensjonSpec
import no.nav.pensjon.simulator.alderspensjon.api.tpo.viapen.GradertUttakSpec
import no.nav.pensjon.simulator.alderspensjon.api.tpo.viapen.InntektSpec
import no.nav.pensjon.simulator.alderspensjon.api.tpo.viapen.Uttaksgrad
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
            antallAarUtenlandsEtter16Aar = source.aarIUtlandetEtter16 ?: 0,
            epsHarPensjon = source.epsPensjon ?: false,
            epsHarInntektOver2G = source.eps2G ?: false,
            fremtidigInntektListe = source.fremtidigInntektListe.orEmpty().map(::inntekt),
            rettTilAfpOffentligDato = source.rettTilAfpOffentligDato?.let(LocalDate::parse)
        )

    private fun pid(value: String) = if (hasLength(value)) Pid(value) else missing("personId")

    private fun gradertUttak(source: GradertUttakSpecV4) =
        GradertUttakSpec(
            uttaksgrad = Uttaksgrad.from(source.uttaksgrad),
            fom = source.fraOgMedDato?.let(LocalDate::parse) ?: missing("gradertUttak.fraOgMedDato")
        )

    private fun inntekt(source: PensjonInntektSpecV4) =
        InntektSpec(
            aarligBeloep = source.aarligInntekt ?: 0,
            fom = source.fraOgMedDato?.let(LocalDate::parse) ?: missing("fremtidigInntekt.fraOgMedDato")
        )

    private fun <T> missing(valueName: String): T {
        throw BadRequestException("$valueName missing")
    }
}

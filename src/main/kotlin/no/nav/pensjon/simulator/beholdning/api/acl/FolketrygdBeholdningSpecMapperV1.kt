package no.nav.pensjon.simulator.beholdning.api.acl

import no.nav.pensjon.simulator.beholdning.FolketrygdBeholdningSpec
import no.nav.pensjon.simulator.beholdning.InntektSpec
import no.nav.pensjon.simulator.person.Pid
import no.nav.pensjon.simulator.tech.web.BadRequestException
import org.springframework.util.StringUtils.hasLength
import java.time.LocalDate

object FolketrygdBeholdningSpecMapperV1 {

    fun fromSpecV1(dto: FolketrygdBeholdningSpecV1) =
        FolketrygdBeholdningSpec(
            pid = dto.personId.let { if (hasLength(it)) Pid(it) else missing("personId") },
            uttakFom = dto.uttaksdato.let { if (hasLength(it)) LocalDate.parse(it) else missing("uttaksdato") },
            fremtidigInntektListe = dto.fremtidigInntektListe.orEmpty().map(::inntekt),
            antallAarUtenlandsEtter16Aar = dto.arIUtlandetEtter16 ?: 0,
            epsHarPensjon = dto.epsPensjon ?: false,
            epsHarInntektOver2G = dto.eps2G ?: false
        )

    private fun inntekt(dto: BeholdningInntektSpecV1) =
        InntektSpec(
            inntektAarligBeloep = dto.arligInntekt ?: 0,
            inntektFom = dto.fraOgMedDato.let {
                if (hasLength(it)) LocalDate.parse(it) else missing("fraOgMedDato (fremtidigInntekt)")
            }
        )

    private fun <T> missing(valueName: String): T {
        throw BadRequestException("$valueName missing")
    }
}

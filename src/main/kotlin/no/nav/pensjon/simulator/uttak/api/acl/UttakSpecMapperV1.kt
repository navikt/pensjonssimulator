package no.nav.pensjon.simulator.uttak.api.acl

import no.nav.pensjon.simulator.person.Pid
import no.nav.pensjon.simulator.tech.web.BadRequestException
import no.nav.pensjon.simulator.uttak.GradertUttakSpec
import no.nav.pensjon.simulator.uttak.InntektSpec
import no.nav.pensjon.simulator.uttak.TidligstMuligUttakSpec
import no.nav.pensjon.simulator.uttak.UttakGrad
import org.springframework.util.StringUtils.hasLength

object UttakSpecMapperV1 {
    fun fromSpecV1(dto: TidligstMuligUttakSpecV1) =
        TidligstMuligUttakSpec(
            pid = dto.personId.let { if (hasLength(it)) Pid(it) else missing("personId") },
            foedselDato = dto.fodselsdato,
            gradertUttak = gradertUttakSpec(dto),
            rettTilOffentligAfpFom = dto.rettTilAfpOffentligDato,
            antallAarUtenlandsEtter16Aar = 0,
            fremtidigInntektListe = dto.fremtidigInntektListe.orEmpty().map(::inntektSpec),
            epsHarPensjon = false,
            epsHarInntektOver2G = false
        )

    private fun gradertUttakSpec(dto: TidligstMuligUttakSpecV1): GradertUttakSpec? =
        uttakGrad(dto.uttaksgrad).let {
            if (it == UttakGrad.HUNDRE_PROSENT)
                null // not gradert uttak
            else
                GradertUttakSpec(grad = it, heltUttakFom = dto.heltUttakFraOgMedDato)
        }

    private fun uttakGrad(prosentsats: Int?): UttakGrad =
        prosentsats?.let(UttakGrad::from) ?: UttakGrad.HUNDRE_PROSENT

    private fun inntektSpec(dto: UttakInntektSpecV1) =
        InntektSpec(
            fom = dto.fraOgMedDato,
            aarligBeloep = dto.arligInntekt ?: 0
        )

    private fun <T> missing(valueName: String): T {
        throw BadRequestException("$valueName missing")
    }
}

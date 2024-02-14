package no.nav.pensjon.simulator.uttak.api.acl

import no.nav.pensjon.simulator.person.Pid
import no.nav.pensjon.simulator.uttak.*
import java.time.LocalDate

object UttakSpecMapperV1 {
    fun fromSpecV1(dto: TidligstMuligUttakSpecV1) =
        TidligstMuligUttakSpec(
            pid = Pid(dto.personId!!),
            foedselDato = dto.fodselsdato!!,
            gradertUttak = gradertUttakSpec(dto),
            rettTilOffentligAfpFom = dto.rettTilAfpOffentligDato,
            antallAarUtenlandsEtter16Aar = 0,
            fremtidigInntektListe = dto.fremtidigInntektListe?.map(::inntektSpec).orEmpty(),
            epsHarPensjon = false,
            epsHarInntektOver2G = false
        )

    private fun gradertUttakSpec(dto: TidligstMuligUttakSpecV1): GradertUttakSpec? =
        uttakGrad(dto.uttaksgrad).let {
            if (it == UttakGrad.HUNDRE_PROSENT)
                null // not gradert uttak
            else
                GradertUttakSpec(grad = it, heltUttakFom = dto.heltUttakFraOgMedDato ?: missingValue())
        }

    private fun uttakGrad(prosentsats: Int?): UttakGrad =
        prosentsats?.let(UttakGrad::from) ?: UttakGrad.HUNDRE_PROSENT

    private fun missingValue(): LocalDate {
        throw RuntimeException("heltUttakFraOgMedDato missing")
    }

    private fun inntektSpec(dto: InntektSpecV1) =
        InntektSpec(
            fom = LocalDate.parse(dto.fraOgMedDato),
            aarligBeloep = dto.arligInntekt
        )
}

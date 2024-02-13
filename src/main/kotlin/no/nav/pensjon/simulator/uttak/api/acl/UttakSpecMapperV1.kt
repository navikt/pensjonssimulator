package no.nav.pensjon.simulator.uttak.api.acl

import no.nav.pensjon.simulator.person.Pid
import no.nav.pensjon.simulator.uttak.*
import java.time.LocalDate

object UttakSpecMapperV1 {
    fun fromSpecV1(dto: TidligstMuligUttakSpecV1) =
        TidligstMuligUttakSpec(
            pid = Pid(dto.personId!!),
            foedselDato = dto.fodselsdato!!,
            uttakGrad = dto.uttaksgrad?.let(UttakGrad::from) ?: UttakGrad.HUNDRE_PROSENT,
            rettTilOffentligAfpFom = dto.rettTilAfpOffentligDato,
            antallAarUtenlandsEtter16Aar = 0, //dto.antallAarUtenlandsEtter16Aar ?: 0,
            fremtidigInntektListe = dto.fremtidigInntektListe?.map(::fromInntektSpecV1).orEmpty(),
            epsHarPensjon = false, //dto.epsHarPensjon ?: false,
            epsHarInntektOver2G = false //dto.epsHarInntektOver2G ?: false
        )


    private fun fromInntektSpecV1(dto: InntektSpecV1) =
        InntektSpec(
            fom = LocalDate.parse(dto.fraOgMedDato),
            aarligBeloep = dto.arligInntekt
        )
}

package no.nav.pensjon.simulator.afp.offentlig.fra2025.beregning

import no.nav.pensjon.simulator.afp.offentlig.fra2025.beregning.domain.AlderForDelingstall
import no.nav.pensjon.simulator.alder.Alder
import java.time.LocalDate

object AlderForDelingstallBeregner {
    private val hoyesteAlderForDelingstall = Alder(70, 0)
    private const val LAVEST_MULIG_UTTAKSALDER = 62 //normAlder?

    fun bestemAldreForDelingstall(fodselsdato: LocalDate, uttaksdato: LocalDate): List<AlderForDelingstall> {

        val aarGammelVedUttak = uttaksdato.year - fodselsdato.year

        if (aarGammelVedUttak == LAVEST_MULIG_UTTAKSALDER) {
            val alderVedUttak = Alder.from(fodselsdato, uttaksdato)

            val datoForSisteTilvekstAvAfpBeholdninger = LocalDate.of(uttaksdato.year + 1, 1, 1)
            val alderVedSisteTilvekstAvAfpBeholdninger = Alder.from(fodselsdato, datoForSisteTilvekstAvAfpBeholdninger)

            return listOf(
                AlderForDelingstall(alderVedUttak, uttaksdato),
                AlderForDelingstall(alderVedSisteTilvekstAvAfpBeholdninger, datoForSisteTilvekstAvAfpBeholdninger)
            )
        }
        if (aarGammelVedUttak >= hoyesteAlderForDelingstall.aar) {
            return listOf(AlderForDelingstall(hoyesteAlderForDelingstall, uttaksdato))
        }

        return listOf(
            AlderForDelingstall(
                Alder.from(fodselsdato, uttaksdato),
                uttaksdato
            )
        )
    }
}
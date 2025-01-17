package no.nav.pensjon.simulator.afp.offentlig.livsvarig

import no.nav.pensjon.simulator.afp.offentlig.livsvarig.client.LivsvarigOffentligAfpClient
import no.nav.pensjon.simulator.core.afp.offentlig.livsvarig.LivsvarigOffentligAfpResult
import no.nav.pensjon.simulator.core.krav.FremtidigInntekt
import no.nav.pensjon.simulator.core.util.PensjonTidUtil.LIVSVARIG_OFFENTLIG_AFP_OPPTJENING_ALDERSGRENSE_AAR
import no.nav.pensjon.simulator.inntekt.Inntekt
import no.nav.pensjon.simulator.person.Pid
import no.nav.pensjon.simulator.tech.time.Time
import org.springframework.stereotype.Service
import java.time.LocalDate
import java.time.Period
import java.util.stream.Stream

@Service
class LivsvarigOffentligAfpService(
    private val client: LivsvarigOffentligAfpClient,
    private val time: Time
) {
    fun beregnAfp(
        pid: Pid,
        foedselsdato: LocalDate,
        forventetAarligInntektBeloep: Int,
        fremtidigeInntekter: List<FremtidigInntekt>,
        virkningDato: LocalDate,
    ): LivsvarigOffentligAfpResult? {
        val fom: LocalDate = foersteAarMedUregistrertInntekt()
        val til: LocalDate = sisteAarMedAfpOpptjeningInntekt(foedselsdato)

        val fremtidigInntektListe: List<Inntekt> =
            if (fremtidigeInntekter.isEmpty()) {
                if (fom.isBefore(til))
                    aarligInntektListe(fom, til, aarligBeloep = forventetAarligInntektBeloep)
                else
                    emptyList()
            } else {
                fremtidigeInntekter.map { Inntekt(it.aarligInntektBeloep, it.fom) }
            }

        return client.simuler(
            LivsvarigOffentligAfpSpec(
                pid,
                foedselsdato,
                fom = virkningDato,
                fremtidigInntektListe
            )
        )
    }

    private fun foersteAarMedUregistrertInntekt(): LocalDate =
        time.today().minusYears(1)

    private companion object {

        private fun sisteAarMedAfpOpptjeningInntekt(foedselsdato: LocalDate): LocalDate =
            foedselsdato.plusYears(LIVSVARIG_OFFENTLIG_AFP_OPPTJENING_ALDERSGRENSE_AAR)

        private fun aarligInntektListe(fom: LocalDate, til: LocalDate, aarligBeloep: Int): List<Inntekt> =
            aarligeDatoer(fom, til)
                .map { inntektVedAaretsStart(it, aarligBeloep) }
                .toList()

        private fun aarligeDatoer(fom: LocalDate, til: LocalDate): Stream<LocalDate> =
            fom.datesUntil(til, Period.ofYears(1))

        private fun inntektVedAaretsStart(dato: LocalDate, aarligBeloep: Int) =
            Inntekt(
                aarligBeloep,
                fom = dato.withMonth(1).withDayOfMonth(1)
            )
    }
}

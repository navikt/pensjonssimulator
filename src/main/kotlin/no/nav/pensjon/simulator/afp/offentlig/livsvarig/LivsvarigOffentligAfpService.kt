package no.nav.pensjon.simulator.afp.offentlig.livsvarig

import mu.KotlinLogging
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
    val log = KotlinLogging.logger {}

    fun beregnAfp(
        pid: Pid,
        foedselsdato: LocalDate,
        forventetAarligInntektBeloep: Int,
        fremtidigeInntekter: List<FremtidigInntekt>?,
        virkningDato: LocalDate,
    ): LivsvarigOffentligAfpResult? {
        if (valid(foedselsdato.year, virkningDato.year).not()) return null

        val fom: LocalDate = foersteAarMedUregistrertInntekt()
        val til: LocalDate = sisteAarMedAfpOpptjeningInntekt(foedselsdato)

        val fremtidigInntektListe: List<Inntekt> =
            fremtidigeInntekter?.map { Inntekt(it.aarligInntektBeloep, it.fom) }
                ?: forventedeInntekter(fom, til, forventetAarligInntektBeloep)

        return client.simuler(
            LivsvarigOffentligAfpSpec(
                pid,
                foedselsdato,
                fom = virkningDato,
                fremtidigInntektListe
            )
        )
    }

    private fun forventedeInntekter(fom: LocalDate, til: LocalDate, forventetAarligBeloep: Int): List<Inntekt> =
        if (fom.isBefore(til))
            aarligInntektListe(fom, til, aarligBeloep = forventetAarligBeloep)
        else
            emptyList()

    private fun valid(foedselAar: Int, virkningFomAar: Int): Boolean =
        when {
            foedselAar < MINIMUM_FOEDSEL_AAR -> {
                log.warn { "LvOfAFP - fødselsår $foedselAar er for tidlig - minimum er $MINIMUM_FOEDSEL_AAR" }
                false
            }

            virkningFomAar < foedselAar + LIVSVARIG_OFFENTLIG_AFP_OPPTJENING_ALDERSGRENSE_AAR -> {
                log.warn { "LvOfAFP - virkningsår $virkningFomAar er for tidlig - minimum er ${foedselAar + LIVSVARIG_OFFENTLIG_AFP_OPPTJENING_ALDERSGRENSE_AAR}" }
                false
            }

            else -> true
        }

    private fun foersteAarMedUregistrertInntekt(): LocalDate =
        time.today().minusYears(1)

    private companion object {
        private const val MINIMUM_FOEDSEL_AAR = 1963

        private fun sisteAarMedAfpOpptjeningInntekt(foedselsdato: LocalDate): LocalDate =
            foedselsdato.plusYears(LIVSVARIG_OFFENTLIG_AFP_OPPTJENING_ALDERSGRENSE_AAR)

        private fun aarligInntektListe(fom: LocalDate, til: LocalDate, aarligBeloep: Int): List<Inntekt> =
            aarligeDatoer(fom, til)
                .map { inntektVedAaretsStart(dato = it, aarligBeloep) }
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

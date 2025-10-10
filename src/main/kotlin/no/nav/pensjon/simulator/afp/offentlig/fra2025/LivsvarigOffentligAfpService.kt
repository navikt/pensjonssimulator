package no.nav.pensjon.simulator.afp.offentlig.fra2025

import mu.KotlinLogging
import no.nav.pensjon.simulator.afp.offentlig.OffentligAfpConstants.OVERGANG_PRE2025_TIL_LIVSVARIG_OFFENTLIG_AFP_FOEDSEL_AAR
import no.nav.pensjon.simulator.afp.offentlig.fra2025.beregning.LivsvarigOffentligAfpBeregningService
import no.nav.pensjon.simulator.afp.offentlig.fra2025.grunnlag.LivsvarigOffentligAfpResult
import no.nav.pensjon.simulator.core.krav.FremtidigInntekt
import no.nav.pensjon.simulator.inntekt.Inntekt
import no.nav.pensjon.simulator.person.Pid
import no.nav.pensjon.simulator.tech.time.Time
import org.springframework.stereotype.Service
import java.time.LocalDate
import java.time.Period
import java.util.stream.Stream

@Service
class LivsvarigOffentligAfpService(
    private val service: LivsvarigOffentligAfpBeregningService,
    private val time: Time
) {
    val log = KotlinLogging.logger {}

    fun beregnAfp(
        pid: Pid,
        foedselsdato: LocalDate,
        forventetAarligInntektBeloep: Int,
        fremtidigeInntekter: List<FremtidigInntekt>,
        brukFremtidigInntekt: Boolean,
        virkningDato: LocalDate,
    ): LivsvarigOffentligAfpResult? {
        if (valid(foedselsdato.year, virkningDato.year).not()) return null

        val fom: LocalDate = foersteAarMedUregistrertInntekt()
        val til: LocalDate = sisteAarMedAfpOpptjeningInntekt(foedselsdato)

        val fremtidigInntektListe: List<Inntekt> =
            if (brukFremtidigInntekt) fremtidigeInntekter.map { Inntekt(it.aarligInntektBeloep, it.fom) }
            else forventedeInntekter(fom, til, forventetAarligInntektBeloep)

        val result = service.simuler(LivsvarigOffentligAfpSpec(
            pid,
            foedselsdato,
            fom = virkningDato,
            fremtidigInntektListe
        ))

        return result
    }

    private fun forventedeInntekter(fom: LocalDate, til: LocalDate, forventetAarligBeloep: Int): List<Inntekt> =
        if (fom.isBefore(til))
            aarligInntektListe(fom, til, aarligBeloep = forventetAarligBeloep)
        else
            emptyList()

    private fun valid(foedselAar: Int, virkningFomAar: Int): Boolean =
        when {
            foedselAar < mimimumFoedselAar() -> {
                log.warn { "LvOfAFP - fødselsår $foedselAar er for tidlig - minimum er ${mimimumFoedselAar()}" }
                false
            }

            virkningFomAar < minimumVirkningFomAar(foedselAar) -> {
                log.warn { "LvOfAFP - virkningsår $virkningFomAar er for tidlig - minimum er ${minimumVirkningFomAar(foedselAar)}" }
                false
            }

            else -> true
        }

    private fun foersteAarMedUregistrertInntekt(): LocalDate =
        time.today().minusYears(1)

    private companion object {
        private const val LIVSVARIG_OFFENTLIG_AFP_OPPTJENING_ALDERSGRENSE_AAR: Long = 62 // normert?

        private fun mimimumFoedselAar(): Int =
            OVERGANG_PRE2025_TIL_LIVSVARIG_OFFENTLIG_AFP_FOEDSEL_AAR

        private fun minimumVirkningFomAar(foedselAar: Int): Long =
            foedselAar + LIVSVARIG_OFFENTLIG_AFP_OPPTJENING_ALDERSGRENSE_AAR

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

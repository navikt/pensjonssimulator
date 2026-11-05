package no.nav.pensjon.simulator.afp.offentlig.tidsbegrenset

import no.nav.pensjon.simulator.afp.offentlig.OffentligAfpConstants.minsteUttaksalderForAfp
import no.nav.pensjon.simulator.alder.Alder
import no.nav.pensjon.simulator.core.domain.regler.enum.GrunnlagsrolleEnum
import no.nav.pensjon.simulator.core.domain.regler.enum.SimuleringTypeEnum
import no.nav.pensjon.simulator.core.domain.regler.grunnlag.Persongrunnlag
import no.nav.pensjon.simulator.core.domain.regler.simulering.Simulering
import no.nav.pensjon.simulator.core.exception.PersonForUngException
import no.nav.pensjon.simulator.validity.BadSpecException
import java.time.LocalDate

/**
 * Validerer spesifikasjonen for simulering av førstegangsuttak av tidsbegrenset offentlig AFP.
 */
object TidsbegrensetOffentligAfpSpecValidator {

    // PEN: SimulerPensjonsberegningCommand.validateInput
    fun validateInput(spec: Simulering, normalder: Alder) {
        val simuleringType = spec.simuleringTypeEnum ?: badSpec(mangel = "simuleringType")
        val uttaksdato: LocalDate = spec.uttaksdatoLd ?: badSpec(mangel = "uttaksdato")

        if (SimuleringTypeEnum.AFP == simuleringType && spec.afpOrdningEnum == null)
            badSpec(mangel = "AFP-ordning")

        val soekerGrunnlag: Persongrunnlag =
            spec.persongrunnlagListe.firstOrNull { it.harRolle(rolle = GrunnlagsrolleEnum.SOKER) }
                ?: badSpec(mangel = "persongrunnlag for søker")

        soekerGrunnlag.fodselsdatoLd?.let {
            validateAlder(it, uttaksdato, simuleringType, normalder)
        } ?: badSpec(mangel = "fødselsdato for søker")
    }

    private fun validateAlder(
        date: LocalDate,
        uttaksdato: LocalDate,
        simuleringType: SimuleringTypeEnum,
        normalder: Alder
    ) {
        val foedselsmaaned: Int = date.monthValue
        val uttaksmaaned = uttaksdato.monthValue
        val uttaksalderAar = uttaksdato.year - date.year

        if (SimuleringTypeEnum.ALDER == simuleringType) {
            //TODO should 'uttakMaaned <= foedselMaaned' be 'uttakMaaned <= foedselMaaned + normalder.maaneder'?
            if (uttaksalderAar < normalder.aar || uttaksalderAar == normalder.aar && uttaksmaaned <= foedselsmaaned) {
                throw PersonForUngException("Alderspensjon;${normalder.aar};${normalder.maaneder}")
            }
        }

        if (SimuleringTypeEnum.AFP == simuleringType) {
            val minstealderAar = minsteUttaksalderForAfp.aar

            if (uttaksalderAar < minstealderAar || uttaksalderAar == minstealderAar && uttaksmaaned <= foedselsmaaned) {
                throw PersonForUngException("AFP;$minstealderAar;0")
            }
        }
    }

    private fun badSpec(mangel: String): Nothing {
        throw BadSpecException("Spec for tidsbegrenset AFP mangler $mangel")
    }
}
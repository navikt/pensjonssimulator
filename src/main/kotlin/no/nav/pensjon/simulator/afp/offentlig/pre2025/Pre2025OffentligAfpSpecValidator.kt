package no.nav.pensjon.simulator.afp.offentlig.pre2025

import no.nav.pensjon.simulator.afp.offentlig.OffentligAfpConstants.AFP_MIN_AGE
import no.nav.pensjon.simulator.alder.Alder
import no.nav.pensjon.simulator.core.domain.regler.enum.GrunnlagsrolleEnum
import no.nav.pensjon.simulator.core.domain.regler.enum.SimuleringTypeEnum
import no.nav.pensjon.simulator.core.domain.regler.grunnlag.Persongrunnlag
import no.nav.pensjon.simulator.core.domain.regler.simulering.Simulering
import no.nav.pensjon.simulator.core.exception.PersonForUngException
import no.nav.pensjon.simulator.core.util.toNorwegianLocalDate
import no.nav.pensjon.simulator.validity.BadSpecException
import java.time.LocalDate

/**
 * Validerer spesifikasjonen for simulering av førstegangsuttak av offentlig AFP
 * i henhold til reglene som gjaldt før 2025 ("gammel offentlig AFP").
 */
object Pre2025OffentligAfpSpecValidator {

    // PEN: SimulerPensjonsberegningCommand.validateInput
    fun validateInput(spec: Simulering, normalder: Alder) {
        val simuleringType = spec.simuleringTypeEnum ?: throw BadSpecException("Spec for tidsbegrenset AFP mangler simuleringType")
        val uttaksdato: LocalDate = spec.uttaksdatoLd ?: throw BadSpecException("Spec for tidsbegrenset AFP mangler uttaksdato")

        if (SimuleringTypeEnum.AFP == simuleringType && spec.afpOrdningEnum == null) {
            throw BadSpecException("Spec for tidsbegrenset AFP mangler AFP-ordning")
        }

        // PEN: SimulerPensjonsberegningCommand.findPersongrunnlagWithGivenRole
        val soekerGrunnlag: Persongrunnlag =
            spec.persongrunnlagListe.firstOrNull { hasRolle(persongrunnlag = it, rolle = GrunnlagsrolleEnum.SOKER) }
                ?: throw BadSpecException("Spec for tidsbegrenset AFP mangler persongrunnlag for søker")

        val foedselsdato = soekerGrunnlag.fodselsdato!!.toNorwegianLocalDate()
        val foedselsmaaned: Int = foedselsdato.monthValue
        val uttaksmaaned = uttaksdato.monthValue
        val uttaksalderAar = uttaksdato.year - foedselsdato.year

        if (SimuleringTypeEnum.ALDER == simuleringType) {
            //TODO should 'uttakMaaned <= foedselMaaned' be 'uttakMaaned <= foedselMaaned + normalder.maaneder'?
            if (uttaksalderAar < normalder.aar || uttaksalderAar == normalder.aar && uttaksmaaned <= foedselsmaaned) {
                throw PersonForUngException("Alderspensjon;${normalder.aar};${normalder.maaneder}")
            }
        }

        if (SimuleringTypeEnum.AFP == simuleringType) {
            if (uttaksalderAar < AFP_MIN_AGE || uttaksalderAar == AFP_MIN_AGE && uttaksmaaned <= foedselsmaaned) {
                throw PersonForUngException("AFP;$AFP_MIN_AGE;0")
            }
        }
    }

    private fun hasRolle(persongrunnlag: Persongrunnlag, rolle: GrunnlagsrolleEnum) =
        persongrunnlag.personDetaljListe.any { rolle == it.grunnlagsrolleEnum }
}

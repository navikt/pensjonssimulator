package no.nav.pensjon.simulator.afp.offentlig.pre2025

import no.nav.pensjon.simulator.alder.Alder
import no.nav.pensjon.simulator.core.domain.regler.enum.GrunnlagsrolleEnum
import no.nav.pensjon.simulator.core.domain.regler.enum.SimuleringTypeEnum
import no.nav.pensjon.simulator.core.domain.regler.grunnlag.Persongrunnlag
import no.nav.pensjon.simulator.core.domain.regler.simulering.Simulering
import no.nav.pensjon.simulator.core.exception.BadSpecException
import no.nav.pensjon.simulator.core.exception.PersonForUngException
import no.nav.pensjon.simulator.core.util.NorwegianCalendar
import java.util.*

/**
 * Validerer spesifikasjonen for simulering av førstegangsuttak av offentlig AFP
 * i henhold til reglene som gjaldt før 2025 ("gammel offentlig AFP").
 */
object Pre2025OffentligAfpSpecValidator {

    private const val AFP_MIN_AGE: Int = 62

    // PEN: SimulerPensjonsberegningCommand.validateInput
    fun validateInput(spec: Simulering, normalder: Alder) {
        val simuleringType = spec.simuleringTypeEnum ?: throw BadSpecException("Pre2025-AFP-spec mangler simuleringType")
        val uttaksdato: Date = spec.uttaksdato ?: throw BadSpecException("Pre2025-AFP-spec mangler uttaksdato")

        if (SimuleringTypeEnum.AFP == simuleringType && spec.afpOrdningEnum == null) {
            throw BadSpecException("Pre2025-AFP-spec mangler AFP-ordning")
        }

        // PEN: SimulerPensjonsberegningCommand.findPersongrunnlagWithGivenRole
        val soekerGrunnlag: Persongrunnlag =
            spec.persongrunnlagListe.firstOrNull { hasRolle(persongrunnlag = it, rolle = GrunnlagsrolleEnum.SOKER) }
                ?: throw BadSpecException("Pre2025-AFP-spec mangler persongrunnlag for søker")

        val soekerFoedselsdato: Calendar = NorwegianCalendar.forNoon(soekerGrunnlag.fodselsdato!!)
        val uttakDato: Calendar = NorwegianCalendar.forNoon(uttaksdato)
        val foedselMaaned = soekerFoedselsdato[Calendar.MONTH]
        val uttakMaaned = uttakDato[Calendar.MONTH]
        val uttakAlderAar = uttakDato[Calendar.YEAR] - soekerFoedselsdato[Calendar.YEAR]

        if (SimuleringTypeEnum.ALDER == simuleringType) {
            //TODO should 'uttakMaaned <= foedselMaaned' be 'uttakMaaned <= foedselMaaned + normalder.maaneder'?
            if (uttakAlderAar < normalder.aar || uttakAlderAar == normalder.aar && uttakMaaned <= foedselMaaned) {
                throw PersonForUngException("Alderspensjon;${normalder.aar};${normalder.maaneder}")
            }
        }

        if (SimuleringTypeEnum.AFP == simuleringType) {
            if (uttakAlderAar < AFP_MIN_AGE || uttakAlderAar == AFP_MIN_AGE && uttakMaaned <= foedselMaaned) {
                throw PersonForUngException("AFP;$AFP_MIN_AGE;0")
            }
        }
    }

    private fun hasRolle(persongrunnlag: Persongrunnlag, rolle: GrunnlagsrolleEnum) =
        persongrunnlag.personDetaljListe.any { rolle == it.grunnlagsrolleEnum }
}

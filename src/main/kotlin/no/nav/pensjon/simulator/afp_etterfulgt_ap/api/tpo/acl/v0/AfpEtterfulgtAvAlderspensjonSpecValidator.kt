package no.nav.pensjon.simulator.afp_etterfulgt_ap.api.tpo.acl.v0

import no.nav.pensjon.simulator.afp_etterfulgt_ap.api.tpo.acl.v0.spec.AfpEtterfulgtAvAlderspensjonSivilstandSpecV0
import no.nav.pensjon.simulator.core.exception.BadSpecException
import no.nav.pensjon.simulator.person.Pid
import java.time.LocalDate
import java.time.format.DateTimeParseException

object AfpEtterfulgtAvAlderspensjonSpecValidator {

    fun validateSpec(dto: AfpEtterfulgtAvAlderspensjonSpecV0) =
        validateMissingFields(dto)
            .apply {
                validatePersonId(personId)
                validateSivilstand(sivilstandVedPensjonering)
                validateUttakFraOgMedDato(uttakFraOgMedDato)
            }

    private fun validatePersonId(personId: String) {
        if (!Pid(personId).isValid) {
            throw BadSpecException("personId er ugyldig")
        }
    }

    private fun validateSivilstand(sivilstandVedPensjonering: String) {
        val valid = runCatching {
            enumValueOf<AfpEtterfulgtAvAlderspensjonSivilstandSpecV0>(sivilstandVedPensjonering.uppercase())
        }.getOrNull()

        if (valid == null) {
            throw BadSpecException(
                "$sivilstandVedPensjonering er ukjent sivilstand. Tillate verdier: ${
                    AfpEtterfulgtAvAlderspensjonSivilstandSpecV0.values().joinToString { it.name }
                }"
            )
        }
    }

    private fun validateUttakFraOgMedDato(uttakFraOgMedDato: String) {
        try {
            if (LocalDate.parse(uttakFraOgMedDato).dayOfMonth != 1) {
                throw BadSpecException("uttakFraOgMedDato må være første dag i en måned")
            }
        } catch (e: DateTimeParseException) {
            throw BadSpecException("uttakFraOgMedDato er ikke en gyldig dato")
        }
    }

    private fun validateMissingFields(dto: AfpEtterfulgtAvAlderspensjonSpecV0): AfpEtterfulgtAvAlderspensjonSpecV0.AfpEtterfulgtAvAlderspensjonValidatedSpecV0 {
        if (dto.personId == null) throw BadSpecException("personId missing")
        if (dto.sivilstandVedPensjonering == null) throw BadSpecException("sivilstandVedPensjonering missing")
        if (dto.uttakFraOgMedDato == null) throw BadSpecException("uttakFraOgMedDato missing")
        //if(dto.fremtidigAarligInntektTilUttak == null) is Optional
        if (dto.inntektSisteMaanedOver1G == null) throw BadSpecException("inntektSisteMaanedOver1G missing")
        if (dto.fremtidigAarligInntektUnderUttak == null) throw BadSpecException("fremtidigAarligInntektUnderUttak missing")
        if (dto.aarIUtlandetEtter16 == null) throw BadSpecException("aarIUtlandetEtter16 missing")
        if (dto.epsPensjon == null) throw BadSpecException("epsPensjon missing")
        if (dto.eps2G == null) throw BadSpecException("eps2G missing")

        return AfpEtterfulgtAvAlderspensjonSpecV0.AfpEtterfulgtAvAlderspensjonValidatedSpecV0(
            dto.personId,
            dto.sivilstandVedPensjonering,
            dto.uttakFraOgMedDato,
            dto.fremtidigAarligInntektTilUttak,
            dto.inntektSisteMaanedOver1G,
            dto.fremtidigAarligInntektUnderUttak,
            dto.aarIUtlandetEtter16,
            dto.epsPensjon,
            dto.eps2G
        )
    }
}
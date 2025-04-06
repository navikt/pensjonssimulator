package no.nav.pensjon.simulator.afp_etterfulgt_ap.api.tpo.acl.v0.spec

import no.nav.pensjon.simulator.afp_etterfulgt_ap.api.tpo.acl.v0.spec.AfpEtterfulgtAvAlderspensjonSpecV0.AfpEtterfulgtAvAlderspensjonValidatedSpecV0
import no.nav.pensjon.simulator.core.exception.BadSpecException
import no.nav.pensjon.simulator.person.Pid
import java.time.LocalDate
import java.time.format.DateTimeParseException
import kotlin.reflect.KProperty1

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

    private fun validateMissingFields(dto: AfpEtterfulgtAvAlderspensjonSpecV0): AfpEtterfulgtAvAlderspensjonValidatedSpecV0 =
        AfpEtterfulgtAvAlderspensjonValidatedSpecV0(
            requireFieldValue(dto, AfpEtterfulgtAvAlderspensjonSpecV0::personId),
            requireFieldValue(dto, AfpEtterfulgtAvAlderspensjonSpecV0::sivilstandVedPensjonering),
            requireFieldValue(dto, AfpEtterfulgtAvAlderspensjonSpecV0::uttakFraOgMedDato),
            dto.fremtidigAarligInntektTilAfpUttak,
            requireFieldValue(dto, AfpEtterfulgtAvAlderspensjonSpecV0::inntektSisteMaanedOver1G),
            requireFieldValue(dto, AfpEtterfulgtAvAlderspensjonSpecV0::fremtidigAarligInntektUnderAfpUttak),
            requireFieldValue(dto, AfpEtterfulgtAvAlderspensjonSpecV0::aarIUtlandetEtter16),
            requireFieldValue(dto, AfpEtterfulgtAvAlderspensjonSpecV0::epsPensjon),
            requireFieldValue(dto, AfpEtterfulgtAvAlderspensjonSpecV0::eps2G)
        )

    fun <T, R> requireFieldValue(instance: T, field: KProperty1<T, R?>): R {
        return field.get(instance)
            ?: throw BadSpecException("${field.name} missing")
    }
}
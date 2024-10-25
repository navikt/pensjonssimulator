package no.nav.pensjon.simulator.alderspensjon.client.pen.acl

import no.nav.pensjon.simulator.alderspensjon.api.tpo.viapen.*
import java.time.LocalDate

object PenAlderspensjonResultMapper {

    fun fromDto(source: PenAlderspensjonResult) =
        AlderspensjonResult(
            simuleringSuksess = source.simuleringSuksess ?: false,
            aarsakListeIkkeSuksess = source.aarsakListeIkkeSuksess.orEmpty().map(::status),
            alderspensjon = source.alderspensjon.orEmpty().map(::alderspensjonFraFolketrygden),
            forslagVedForLavOpptjening = source.alternativerVedForLavOpptjening?.let(::forslagVedForLavOpptjening),
            harUttak = source.harUttak ?: false
        )

    private fun alderspensjonFraFolketrygden(source: PenAlderspensjonFraFolketrygden) =
        AlderspensjonFraFolketrygden(
            fom = source.fraOgMedDato?.let(LocalDate::parse) ?: LocalDate.MAX,
            delytelseListe = source.delytelseListe.orEmpty().map(::delytelse)
                .filter { it.pensjonType != PensjonType.NONE },
            uttaksgrad = Uttaksgrad.from(source.uttaksgrad)
        )

    private fun delytelse(source: PenPensjonDelytelse) =
        PensjonDelytelse(
            pensjonType = PenPensjonType.toInternalValue(source.pensjonsType),
            beloep = source.belop ?: 0
        )

    private fun forslagVedForLavOpptjening(source: PenForslagVedForLavOpptjening) =
        ForslagVedForLavOpptjening(
            gradertUttak = source.gradertUttak?.let(::gradertUttak),
            heltUttakFom = source.heltUttakFraOgMedDato
        )

    private fun gradertUttak(source: PenGradertUttak) =
        GradertUttak(
            fom = source.fraOgMedDato,
            uttaksgrad = Uttaksgrad.from(source.uttaksgrad)
        )

    private fun status(source: PenPensjonSimuleringStatus) =
        PensjonSimuleringStatus(
            statusKode = PenPensjonSimuleringStatusKode.toInternalValue(source.statusKode),
            statusBeskrivelse = source.statusBeskrivelse ?: ""
        )
}

enum class PenPensjonType(val externalValue: String, val internalValue: PensjonType) {
    GARANTIPENSJON(externalValue = "GAP", internalValue = PensjonType.GARANTIPENSJON),
    INNTEKTSPENSJON(externalValue = "IP", internalValue = PensjonType.INNTEKTSPENSJON),
    MINSTENIVAATILLEGG_INDVIDUELT(externalValue = "MIN_NIVA_TILL_INDV", internalValue = PensjonType.NONE);

    companion object {
        private val values = PenPensjonType.entries.toTypedArray()

        fun toInternalValue(value: String?): PensjonType = fromExternalValue(value)?.internalValue ?: PensjonType.NONE

        private fun fromExternalValue(value: String?): PenPensjonType? =
            values.singleOrNull { it.externalValue == value }
    }
}

enum class PenPensjonSimuleringStatusKode(val externalValue: String, val internalValue: PensjonSimuleringStatusKode) {
    AVSLAG_FOR_LAV_OPPTJENING(
        externalValue = "UTILSTREKKELIG_OPPTJENING",
        internalValue = PensjonSimuleringStatusKode.AVSLAG_FOR_LAV_OPPTJENING
    ),
    AVSLAG_FOR_KORT_TRYGDETID(
        externalValue = "AVSLAG_FOR_KORT_TRYGDETID",
        internalValue = PensjonSimuleringStatusKode.AVSLAG_FOR_KORT_TRYGDETID
    ),
    BRUKER_FOEDT_FOER_1943(
        externalValue = "BRUKER_FOEDT_FOER_1943",
        internalValue = PensjonSimuleringStatusKode.BRUKER_FOEDT_FOER_1943
    ),
    BRUKER_HAR_IKKE_LOEPENDE_ALDERSPENSJON(
        externalValue = "BRUKER_HAR_IKKE_LOEPENDE_ALDERSPENSJON",
        internalValue = PensjonSimuleringStatusKode.BRUKER_HAR_IKKE_LOEPENDE_ALDERSPENSJON
    ),
    BRUKER_HAR_LOEPENDE_ALDERSPENSJON_PAA_GAMMELT_REGELVERK(
        externalValue = "BRUKER_HAR_LOEPENDE_ALDERSPENSJON_PAA_GAMMELT_REGELVERK",
        internalValue = PensjonSimuleringStatusKode.BRUKER_HAR_LOEPENDE_ALDERSPENSJON_PAA_GAMMELT_REGELVERK
    ),
    UGYLDIG_ENDRING_AV_UTTAKSGRAD(
        externalValue = "UGYLDIG_ENDRING_AV_UTTAKSGRAD",
        internalValue = PensjonSimuleringStatusKode.UGYLDIG_ENDRING_AV_UTTAKSGRAD
    ),
    ANNET(externalValue = "ANNET", internalValue = PensjonSimuleringStatusKode.ANNET);

    companion object {
        private val values = PenPensjonSimuleringStatusKode.entries.toTypedArray()

        fun toInternalValue(value: String?): PensjonSimuleringStatusKode =
            fromExternalValue(value)?.internalValue ?: PensjonSimuleringStatusKode.NONE

        private fun fromExternalValue(value: String?): PenPensjonSimuleringStatusKode? =
            values.singleOrNull { it.externalValue == value }
    }
}

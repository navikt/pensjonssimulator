package no.nav.pensjon.simulator.alderspensjon.api.acl

import java.time.LocalDate

object AlderspensjonMapperV1 {

    fun resultV1(success: Boolean) =
        AlderspensjonResultV1(
            simuleringSuksess = success,
            aarsakListeIkkeSuksess = if (success) emptyList() else listOf(simuleringStatus()),
            alderspensjon = if (success) listOf(alderspensjonFraFolketrygden()) else emptyList(),
            alternativerVedForLavOpptjening = if (success) emptyList() else listOf(alternativerVedForLavOpptjening()),
            harUttak = false
        )

    private fun simuleringStatus() =
        PensjonSimuleringStatusV1(
            statusKode = PensjonSimuleringStatusKodeV1.AVSLAG_FOR_LAV_OPPTJENING,
            statusBeskrivelse = "For lav opptjening"
        )

    private fun alderspensjonFraFolketrygden() =
        AlderspensjonFraFolketrygdenV1(
            fraOgMedDato = LocalDate.of(2034, 5, 6),
            delytelseListe = listOf(delytelse()),
            uttaksgrad = 100
        )

    private fun alternativerVedForLavOpptjening() =
        PensjonAlternativerVedForLavOpptjeningV1(
            alderspensjonVedTidligstMuligUttak = alderspensjonFraFolketrygden(),
            alderspensjonVedHoyestMuligGrad = alderspensjonFraFolketrygden()
        )

    private fun delytelse() =
        PensjonDelytelseV1(
            pensjonsType = PensjonTypeV1.inntektsPensjon,
            belop = 123000
        )
}

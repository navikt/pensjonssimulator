package no.nav.pensjon.simulator.afp.offentlig.pre2025.api.acl.v0.spec

import no.nav.pensjon.simulator.person.Pid.Companion.redact
import no.nav.pensjon.simulator.tech.json.Stringifier.textAsString

data class AfpEtterfulgtAvAlderspensjonSpecV0(
    val personId: String?,
    val sivilstandVedPensjonering: String?,
    val uttakFraOgMedDato: String?,
    val fremtidigAarligInntektTilAfpUttak: Int?, //bruk siste lignet inntekt, når denne er ikke med
    val inntektSisteMaanedOver1G: Boolean?,
    val fremtidigAarligInntektUnderAfpUttak: Int?,
    val aarIUtlandetEtter16: Int?,
    val epsPensjon: Boolean?,
    val eps2G: Boolean?,
) {
    data class AfpEtterfulgtAvAlderspensjonValidatedSpecV0(
        val personId: String,
        val sivilstandVedPensjonering: String,
        val uttakFraOgMedDato: String,
        val fremtidigAarligInntektTilAfpUttak: Int?, //bruk siste lignet inntekt, når denne er ikke med
        val inntektSisteMaanedOver1G: Boolean,
        val fremtidigAarligInntektUnderAfpUttak: Int,
        val aarIUtlandetEtter16: Int,
        val epsPensjon: Boolean,
        val eps2G: Boolean,
    )

    override fun toString() =
        "{ \"personId\": ${textAsString(redact(personId))}, " +
                "\"sivilstandVedPensjonering\": ${textAsString(sivilstandVedPensjonering)}, " +
                "\"uttakFraOgMedDato\": ${textAsString(uttakFraOgMedDato)}, " +
                "\"fremtidigAarligInntektTilAfpUttak\": $fremtidigAarligInntektTilAfpUttak, " +
                "\"inntektSisteMaanedOver1G\": $inntektSisteMaanedOver1G, " +
                "\"fremtidigAarligInntektUnderAfpUttak\": $fremtidigAarligInntektUnderAfpUttak, " +
                "\"aarIUtlandetEtter16\": $aarIUtlandetEtter16, " +
                "\"epsPensjon\": $epsPensjon, " +
                "\"eps2G\": $eps2G }"
}

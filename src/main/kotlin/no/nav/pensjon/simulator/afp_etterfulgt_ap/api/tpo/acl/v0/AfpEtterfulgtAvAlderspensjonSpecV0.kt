package no.nav.pensjon.simulator.afp_etterfulgt_ap.api.tpo.acl.v0

import no.nav.pensjon.simulator.person.Pid.Companion.redact

data class AfpEtterfulgtAvAlderspensjonSpecV0(
    val personId: String?,
    val sivilstandVedPensjonering: String?,
    val uttakFraOgMedDato: String?,
    val fremtidigAarligInntektTilUttak: Int?, //bruk siste lignet inntekt, når denne er ikke med
    val inntektSisteMaanedOver1G: Boolean?,
    val fremtidigAarligInntektUnderUttak: Int?,
    val aarIUtlandetEtter16: Int?,
    val epsPensjon: Boolean?,
    val eps2G: Boolean?,
) {

    data class AfpEtterfulgtAvAlderspensjonValidatedSpecV0(
        val personId: String,
        val sivilstandVedPensjonering: String,
        val uttakFraOgMedDato: String,
        val fremtidigAarligInntektTilUttak: Int?, //bruk siste lignet inntekt, når denne er ikke med
        val inntektSisteMaanedOver1G: Boolean,
        val fremtidigAarligInntektUnderUttak: Int,
        val aarIUtlandetEtter16: Int,
        val epsPensjon: Boolean,
        val eps2G: Boolean,
    )

    override fun toString(): String {
        return "personId:" + redact(personId) +
                ", sivilstandVedPensjonering:" + sivilstandVedPensjonering +
                ", uttakFraOgMedDato:" + uttakFraOgMedDato +
                ", fremtidigAarligInntektTilUttak:" + fremtidigAarligInntektTilUttak +
                ", inntektSisteMaanedOver1G:" + inntektSisteMaanedOver1G +
                ", fremtidigAarligInntektUnderUttak:" + fremtidigAarligInntektUnderUttak +
                ", aarIUtlandetEtter16:" + aarIUtlandetEtter16 +
                ", epsPensjon:" + epsPensjon +
                ", eps2G:" + eps2G
    }
}
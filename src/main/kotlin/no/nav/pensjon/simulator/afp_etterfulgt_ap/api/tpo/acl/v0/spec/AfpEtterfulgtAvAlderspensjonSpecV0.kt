package no.nav.pensjon.simulator.afp_etterfulgt_ap.api.tpo.acl.v0.spec

import no.nav.pensjon.simulator.person.Pid.Companion.redact

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

    override fun toString(): String {
        return "personId:" + redact(personId) +
                ", sivilstandVedPensjonering:" + sivilstandVedPensjonering +
                ", uttakFraOgMedDato:" + uttakFraOgMedDato +
                ", fremtidigAarligInntektTilUttak:" + fremtidigAarligInntektTilAfpUttak +
                ", inntektSisteMaanedOver1G:" + inntektSisteMaanedOver1G +
                ", fremtidigAarligInntektUnderUttak:" + fremtidigAarligInntektUnderAfpUttak +
                ", aarIUtlandetEtter16:" + aarIUtlandetEtter16 +
                ", epsPensjon:" + epsPensjon +
                ", eps2G:" + eps2G
    }
}
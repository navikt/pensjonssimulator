package no.nav.pensjon.simulator.tjenestepensjon.pre2025.api.acl.v2

import com.fasterxml.jackson.annotation.JsonIgnoreProperties
import no.nav.pensjon.simulator.core.domain.regler.enum.LandkodeEnum
import no.nav.pensjon.simulator.trygdetid.UtlandPeriode
import java.time.LocalDate

@JsonIgnoreProperties(ignoreUnknown = true)
data class UtenlandsperiodeForSimuleringV2(
    val land: String,
    val arbeidetIUtland: Boolean = false,
    val periodeFom: LocalDate,
    val periodeTom: LocalDate? = null
) {
    private val irregularLandEnums: Map<String, LandkodeEnum> =
        mapOf(
            "???" to LandkodeEnum.P_UKJENT,
            "349" to LandkodeEnum.P_SPANSKE_OMR_AFRIKA,
            "546" to LandkodeEnum.P_SIKKIM,
            "556" to LandkodeEnum.P_YEMEN,
            "669" to LandkodeEnum.P_PANAMAKANALSONEN
        )

    fun utlandPeriode() =
        UtlandPeriode(
            land = mapLand(land),
            arbeidet = arbeidetIUtland,
            fom = periodeFom,
            tom = periodeTom
        )

    private fun mapLand(land: String): LandkodeEnum =
        irregularLandEnums[land] ?: LandkodeEnum.valueOf(land)
}
package no.nav.pensjon.simulator.tjenestepensjon.pre2025

import no.nav.pensjon.simulator.tjenestepensjon.pre2025.api.acl.v3.StillingsprOffCodeV3

data class StillingsprosentSpec(
    val stillingsprosentOffHeltUttak: Int?,
    val stillingsprosentOffGradertUttak: Int?,
) {
    constructor(heltUttak: StillingsprosentCode?, gradertUttak: StillingsprosentCode?) :
            this(
                stillingsprosentOffHeltUttak = StillingsprosentCode.toInt(heltUttak),
                stillingsprosentOffGradertUttak = StillingsprosentCode.toInt(gradertUttak),
            )

    //TODO remove dependency on DTO
    constructor(heltUttak: StillingsprOffCodeV3?, gradertUttak: StillingsprOffCodeV3?) :
            this(
                stillingsprosentOffHeltUttak = StillingsprOffCodeV3.toInt(heltUttak),
                stillingsprosentOffGradertUttak = StillingsprOffCodeV3.toInt(gradertUttak),
            )
}

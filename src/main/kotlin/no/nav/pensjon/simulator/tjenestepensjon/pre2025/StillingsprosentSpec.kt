package no.nav.pensjon.simulator.tjenestepensjon.pre2025

import no.nav.pensjon.simulator.tjenestepensjon.pre2025.api.acl.v2.StillingsprOffCodeV2
import no.nav.pensjon.simulator.tjenestepensjon.pre2025.api.acl.v3.StillingsprOffCodeV3

data class StillingsprosentSpec(
    val stillingsprosentOffHeltUttak: Int?,
    val stillingsprosentOffGradertUttak: Int?,
){
    constructor(heltUttak: StillingsprOffCodeV2?, gradertUttak: StillingsprOffCodeV2?) : this(
        StillingsprOffCodeV2.toInt(heltUttak), StillingsprOffCodeV2.toInt(gradertUttak),

    )constructor(heltUttak: StillingsprOffCodeV3?, gradertUttak: StillingsprOffCodeV3?) : this(
        StillingsprOffCodeV3.toInt(heltUttak), StillingsprOffCodeV3.toInt(gradertUttak),
    )
}

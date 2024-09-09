package no.nav.pensjon.simulator.core.domain.regler.grunnlag

import java.io.Serializable

class GarantitilleggsbeholdningGrunnlag(
        /**
         * Delingstall ved fylte 67 år for årskull født 1962.
         */
        var dt67_1962: Double = 0.0,

        /**
         * Forholdstall ved fylte 67 år for årskull født 1962.
         */
        var ft67_1962: Double = 0.0
) : Serializable {
    constructor(garantitilleggsbeholdningGrunnlag: GarantitilleggsbeholdningGrunnlag) : this() {
        this.dt67_1962 = garantitilleggsbeholdningGrunnlag.dt67_1962
        this.ft67_1962 = garantitilleggsbeholdningGrunnlag.ft67_1962
    }
}

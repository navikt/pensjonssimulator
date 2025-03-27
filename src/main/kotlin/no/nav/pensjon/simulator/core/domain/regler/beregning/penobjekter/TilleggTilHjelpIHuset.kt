package no.nav.pensjon.simulator.core.domain.regler.beregning.penobjekter

import no.nav.pensjon.simulator.core.domain.regler.beregning.Ytelseskomponent
import no.nav.pensjon.simulator.core.domain.regler.enum.YtelseskomponentTypeEnum

// 2025-03-23
/**
 * Brukes kun av BEF270 til G-omregning.
 */
class TilleggTilHjelpIHuset : Ytelseskomponent() {
    var grunnlagForUtbetaling = 0

    override var ytelsekomponentTypeEnum: YtelseskomponentTypeEnum = YtelseskomponentTypeEnum.HJELP_I_HUS
}

package no.nav.pensjon.simulator.core.domain.regler.beregning.penobjekter

import no.nav.pensjon.simulator.core.domain.regler.beregning.Ytelseskomponent
import no.nav.pensjon.simulator.core.domain.regler.enum.YtelseskomponentTypeEnum

// Copied from pensjon-regler-api 2026-01-16
/**
 * Mén-del krigspensjon, ref.:
 * - lovdata.no/lov/1946-12-13-22/§8
 * - lovdata.no/nav/rundskriv/v2-31-00nr2#KAPITTEL_2
 * ----------
 * Brukes kun til G-omregning i BEF270.
 */
class Mendel : Ytelseskomponent() {
    // NB: This is 'val' in pensjon-regler-api
    override var ytelsekomponentTypeEnum: YtelseskomponentTypeEnum = YtelseskomponentTypeEnum.MENDEL
}

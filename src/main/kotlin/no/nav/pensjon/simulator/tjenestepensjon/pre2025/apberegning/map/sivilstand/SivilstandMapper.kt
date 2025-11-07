package no.nav.pensjon.simulator.tjenestepensjon.pre2025.apberegning.map.sivilstand

import no.nav.pensjon.simulator.core.domain.SivilstatusType
import no.nav.pensjon.simulator.tjenestepensjon.pre2025.api.SivilstandKode

object SivilstandMapper {

    fun map(sivilstatus: SivilstatusType): SivilstandKode {
        return when (sivilstatus) {
            SivilstatusType.ENKE, SivilstatusType.GJPA -> SivilstandKode.ENKE
            SivilstatusType.GIFT, SivilstatusType.SEPR -> SivilstandKode.GIFT
            SivilstatusType.REPA, SivilstatusType.SEPA -> SivilstandKode.REGISTRERT_PARTNER
            SivilstatusType.SKIL, SivilstatusType.SKPA -> SivilstandKode.SKILT
            else -> SivilstandKode.UGIFT
        }
    }
}
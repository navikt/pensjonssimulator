package no.nav.pensjon.simulator.core.person.eps

import no.nav.pensjon.simulator.core.domain.SimuleringType
import no.nav.pensjon.simulator.core.domain.SivilstatusType
import no.nav.pensjon.simulator.core.spec.SimuleringSpec
import java.util.EnumSet

/**
 * Utility functions for EPS (ektefelle/partner/samboer).
 */
object EpsUtil {
    // VilkarsprovOgBeregnAlderHelper.isEktefelleMottarPensjon
    fun epsMottarPensjon(spec: SimuleringSpec): Boolean {
        if (isAlderWithGjenlevende(spec)) {
            return false
        }

        // The sivilstandsjekk is needed because ESB default sets epsPensjon to true when a samhandler is calling the simulering service.
        return if (EnumSet.of(SivilstatusType.SAMB, SivilstatusType.GIFT, SivilstatusType.REPA)
                .contains(spec.sivilstatus)
        )
            spec.epsHarPensjon
        else
            false
    }

    private fun isAlderWithGjenlevende(spec: SimuleringSpec): Boolean =
        EnumSet.of(SimuleringType.ALDER_M_GJEN, SimuleringType.ENDR_ALDER_M_GJEN).contains(spec.type)
}

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
        if (gjelderGjenlevenderett(spec.type)) {
            return false
        }

        //TODO check this statement:
        // "The sivilstandsjekk is needed because ESB default sets epsPensjon to true when a samhandler is calling the simulering service"
        return if (erEps(spec.sivilstatus))
            spec.epsHarPensjon
        else
            false
    }

    fun erEps(sivilstatus: SivilstatusType) =
        EnumSet.of(SivilstatusType.GIFT, SivilstatusType.REPA, SivilstatusType.SAMB).contains(sivilstatus)

    fun gjelderGjenlevenderett(simuleringType: SimuleringType) =
        EnumSet.of(SimuleringType.ALDER_M_GJEN, SimuleringType.ENDR_ALDER_M_GJEN).contains(simuleringType)
}

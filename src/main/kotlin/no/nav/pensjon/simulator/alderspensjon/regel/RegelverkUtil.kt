package no.nav.pensjon.simulator.alderspensjon.regel

import no.nav.pensjon.simulator.core.domain.regler.enum.RegelverkTypeEnum
import no.nav.pensjon.simulator.core.exception.PersonForGammelException

object RegelverkUtil {

    /**
     * www.stortinget.no/no/Saker-og-publikasjoner/Publikasjoner/Innstillinger/Odelstinget/2008-2009/inno-200809-067/7/
     */
    private const val FLEKSIBEL_PENSJON_MINSTE_FOEDSELSAAR = 1943

    private const val OVERGANGSREGLER_MINSTE_FOEDSELSAAR = 1954

    private const val KAPITTEL_20_MINSTE_FOEDSELSAAR = 1963

    fun regelverkType(foedselsaar: Int): RegelverkTypeEnum =
        when {
            foedselsaar < FLEKSIBEL_PENSJON_MINSTE_FOEDSELSAAR -> forGammel()
            foedselsaar < OVERGANGSREGLER_MINSTE_FOEDSELSAAR -> RegelverkTypeEnum.N_REG_G_OPPTJ
            foedselsaar < KAPITTEL_20_MINSTE_FOEDSELSAAR -> RegelverkTypeEnum.N_REG_G_N_OPPTJ
            else -> RegelverkTypeEnum.N_REG_N_OPPTJ
        }

    private fun forGammel(): Nothing {
        throw PersonForGammelException(
            message = "Regelverk for personer født før $FLEKSIBEL_PENSJON_MINSTE_FOEDSELSAAR er ikke støttet"
        )
    }
}
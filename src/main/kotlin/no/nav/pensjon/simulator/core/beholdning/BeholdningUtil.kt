package no.nav.pensjon.simulator.core.beholdning

import no.nav.pensjon.simulator.core.domain.regler.enum.BeholdningtypeEnum
import no.nav.pensjon.simulator.core.domain.regler.grunnlag.Beholdning
import no.nav.pensjon.simulator.core.domain.regler.grunnlag.Pensjonsbeholdning

object BeholdningUtil {
    // Updated annually. In PEN stored in T_APPL_PARAMETER; context.getSisteGyldigeOpptjeningsAr()
    const val SISTE_GYLDIGE_OPPTJENING_AAR = 2023 // valid until November 2025

    // TypedInformationListeUtils.findElementOfType
    fun findElementOfType(list: List<Beholdning>, type: BeholdningtypeEnum) =
        list.find { type == it.beholdningsTypeEnum } as? Pensjonsbeholdning

    // ArligInformasjonListeUtils.sortedSubset
    fun sortedSubset(list: List<Pensjonsbeholdning>, year: Int) =
        list.filter { it.ar == year }
            .toMutableList()
            .sortedBy { it.ar } // ArligInformasjonAscendingComparator
}

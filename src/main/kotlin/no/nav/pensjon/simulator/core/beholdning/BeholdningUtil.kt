package no.nav.pensjon.simulator.core.beholdning

import no.nav.pensjon.simulator.core.domain.regler.enum.BeholdningtypeEnum
import no.nav.pensjon.simulator.core.domain.regler.grunnlag.Beholdning
import no.nav.pensjon.simulator.core.domain.regler.grunnlag.Pensjonsbeholdning

object BeholdningUtil {

    // TypedInformationListeUtils.findElementOfType
    fun findElementOfType(list: List<Beholdning>, type: BeholdningtypeEnum) =
        list.find { type == it.beholdningsTypeEnum } as? Pensjonsbeholdning

    // ArligInformasjonListeUtils.sortedSubset
    fun sortedSubset(list: List<Pensjonsbeholdning>, year: Int) =
        list.filter { it.ar == year }
            .toMutableList()
            .sortedBy { it.ar } // ArligInformasjonAscendingComparator
}
